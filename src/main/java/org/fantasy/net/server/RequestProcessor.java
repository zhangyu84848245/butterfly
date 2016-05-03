package org.fantasy.net.server;


import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.fantasy.common.MethodDescriptor;
import org.fantasy.net.client.ConnectionException;
import org.fantasy.net.client.HeartbeatException;
import org.fantasy.net.proto.ConnectRequest;
import org.fantasy.net.proto.ConnectResponse;
import org.fantasy.net.proto.ExceptionResponse;
import org.fantasy.net.proto.HeartbeatRequest;
import org.fantasy.net.proto.HeartbeatResponse;
import org.fantasy.net.proto.RpcRequest;
import org.fantasy.net.proto.RpcResponse;
import org.fantasy.util.ClassUtils;
import org.fantasy.util.Constant;
import org.fantasy.util.ReflectionUtils;


public class RequestProcessor extends Thread implements Processor/** , Configurable */ {

	private static final Logger LOG = Logger.getLogger(RequestProcessor.class);
	private static final ConcurrentMap<String, Class<?>> PRIMITIVE_CLASSES = new ConcurrentHashMap<String, Class<?>>();
	
	static {
		PRIMITIVE_CLASSES.put("byte", byte.class);
		PRIMITIVE_CLASSES.put("boolean", boolean.class);
		PRIMITIVE_CLASSES.put("short", short.class);
		PRIMITIVE_CLASSES.put("char", char.class);
		PRIMITIVE_CLASSES.put("int", int.class);
		PRIMITIVE_CLASSES.put("float", float.class);
		PRIMITIVE_CLASSES.put("long", long.class);
		PRIMITIVE_CLASSES.put("double", double.class);
		PRIMITIVE_CLASSES.put("void", void.class);
	}
	
	private LinkedBlockingQueue<Serializable> requestQueue = new LinkedBlockingQueue<Serializable>();
	private boolean running = true;
	private NioSession session;

	public RequestProcessor(NioSession session) {
		this.session = session;
	}

	public void process(Serializable request) {
		if(running) {
			requestQueue.offer(request);
		}
	}

	private ExceptionResponse createException(String className, String message) {
		return ExceptionResponse.newBuilder()
				.exceptionClassName(className)
				.message(message)
				.host(((InetSocketAddress)(session.channel.socket().getLocalSocketAddress())).getHostName())
				.port(session.channel.socket().getLocalPort())
				.build();
	}

	public void run() {
		try {
			while(running) {
				Serializable object = requestQueue.take();
				Serializable response = null;
				ExceptionResponse error = null;
				Object result = null;
				if(object != null) {
					if(object instanceof RpcRequest) {
						RpcRequest request = (RpcRequest)object;
						if(request.getError() != null) {
							error = createException(request.getError().getClass().getName(), request.getError().getMessage());
						} else {
							try {
								MethodDescriptor md = request.getMethodDescriptor();
								String[] pTypes = md.getParameterTypes();
								int len = pTypes.length;
								Class<?>[] parameterTypes = new Class[len];
								for(int i = 0; i < len; i++) {
									String pType = pTypes[i];
									Class<?> clazz = PRIMITIVE_CLASSES.get(pType);
									if(clazz == null)
										clazz = ClassUtils.forName(pType);
									parameterTypes[i] = clazz;
								}
								Object[] arguments = request.getArguments();
								// bean id
								String beanName = md.getDeclaringClass();
								// RpcServer throws
								Object bean = session.getBean(beanName);
//								Class<?>[] parameterTypes = new Class[arguments.length];
//								for(int i = 0; i < arguments.length; i++)
//									parameterTypes[i] = arguments[i].getClass();
								Method method = ReflectionUtils.getMethodByName(bean.getClass(), md.getName(), parameterTypes);
								result = method.invoke(bean, arguments);
							} catch (Throwable ex) {
								if(ex instanceof InvocationTargetException) {
									InvocationTargetException ite = (InvocationTargetException)ex;
									ex = ite.getCause().getCause();
								}
								error = createException(ex.getClass().getName(), ex.getMessage());
							}
						}
						response = RpcResponse.newBuilder().result(result).error(error).requestId(request.getId()).build();
					} else if(object instanceof HeartbeatRequest) {
						HeartbeatRequest request = (HeartbeatRequest)object;
						if(request.getClientId() != session.getClientId()) {
							error = createException(HeartbeatException.class.getName(), "Client id mismatch");
						}
						response = HeartbeatResponse.newBuilder().id(request.getId()).sessionId(session.getSessionId()).build();
					} else if(object instanceof ConnectRequest) {
						ConnectRequest request = (ConnectRequest)object;
						if(!request.getMagic().equals(Constant.MAGIC) || !request.getVersion().equals(Constant.VERSION)) {
							error = createException(ConnectionException.class.getName(), "Invalid connection request header");
						}
						session.setClientId(request.getClientId());
						response = ConnectResponse.newBuilder().sessionId(session.getSessionId()).exception(error).build();
					}
					session.doWrite(response);
				}
			}
		} catch (InterruptedException e) {
			LOG.error(e.getMessage(), e);
			Thread.currentThread().interrupt();
		}
		
	}

}
