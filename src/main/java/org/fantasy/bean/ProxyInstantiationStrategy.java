package org.fantasy.bean;

import java.lang.reflect.Method;

import org.fantasy.bean.annotation.Consumer;
import org.fantasy.bean.annotation.Provider;
import org.fantasy.bean.proxy.MethodInterceptor;
import org.fantasy.bean.proxy.ProxyConfig;
import org.fantasy.bean.proxy.ProxyConfigException;
import org.fantasy.bean.proxy.ProxyFactory;
import org.fantasy.bean.proxy.ProxyInvoker;
import org.fantasy.common.FactoryProvider;
import org.fantasy.common.MethodDescriptor;
import org.fantasy.common.RequestObject;
import org.fantasy.common.ResponseObject;
import org.fantasy.conf.Configuration;
import org.fantasy.net.RemoteException;
import org.fantasy.net.client.RpcClient;
import org.fantasy.net.proto.ExceptionResponse;
import org.fantasy.net.proto.RpcResponse;
import org.fantasy.util.ClassUtils;
import org.fantasy.util.ReflectionUtils;


public class ProxyInstantiationStrategy implements InstantiationStrategy {

	private Configuration conf;
	private FactoryProvider<ProxyFactory> factoryProvider;
	private ProxyFactory proxyFactory;
	private Class<?> annotationClass;
	
	public ProxyInstantiationStrategy(Configuration conf, Class<?> annotationClass) {
		this.annotationClass = annotationClass;
		this.conf = conf;
		this.factoryProvider = new FactoryProvider<ProxyFactory>(conf, ProxyFactory.class);
		this.proxyFactory = factoryProvider.get(conf.get("bean.proxy.factory", "default"));
	}

	public Object instantiate(final GenericBean serviceBean) {
		String className = annotationClass.equals(Provider.class) ? serviceBean.getRefClassName() : serviceBean.getBeanClassName();
		Class<?> proxyClass = ClassUtils.forName(className);
		ProxyConfig config = null;
		if(proxyClass.isInterface() && annotationClass.equals(Consumer.class)) {
			config = new ProxyConfig(proxyClass, null, new MethodInterceptor() {
				private RpcClient client;
				public void setTargetObject(Object targetObject) {
					throw new UnsupportedOperationException();
				}
				public Object getTargetObject() {
					throw new UnsupportedOperationException();
				}
				public Object intercept(ProxyInvoker invoker, Method method, Object... args) throws Throwable {
					Class<?>[] parameterTypes = method.getParameterTypes();
					String[] pTypes = new String[parameterTypes.length];
					for(int i = 0; i < parameterTypes.length; i++)
						pTypes[i] = parameterTypes[i].getName();
					Class<?>[] exceptionTypes = method.getExceptionTypes();
					String[] eTypes = new String[exceptionTypes.length];
					for(int i = 0; i < exceptionTypes.length; i++)
						eTypes[i] = exceptionTypes[i].getName();
					MethodDescriptor md = new MethodDescriptor(
							method.getModifiers(), 
							method.getName(), 
							serviceBean.getId(),
							pTypes, 
							method.getReturnType().getName(), 
							eTypes
					);
					RpcResponse response = client.sendRequest(md, args);
					if(response == null)
						return null;
					if(response.hasException()) {
						ExceptionResponse error = response.getError();
						Throwable cause = (Throwable)ReflectionUtils.newInstance(ClassUtils.forName(error.getExceptionClassName()));
						throw new RemoteException("Call remote method " + md.toString() + " from " + error.getHost() + ":" + error.getPort() + " error;" + error.getMessage(), cause);
					}
					return response.getResult();
				}
			}, null, true);
		} else {
			config = new ProxyConfig(proxyClass);
		}
		try {
			return proxyFactory.createProxy(config);
		} catch (ProxyConfigException e) {
			throw new RuntimeException(e);
		}
	}
	
	public Configuration getConf() {
		return conf;
	}

}
