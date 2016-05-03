package org.fantasy.net.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import org.apache.log4j.Logger;
import org.fantasy.common.Generator;
import org.fantasy.common.MethodDescriptor;
import org.fantasy.context.BeanFactoryContext;
import org.fantasy.net.ChannelException;
import org.fantasy.net.proto.ConnectRequest;
import org.fantasy.net.proto.ExceptionResponse;
import org.fantasy.net.proto.RpcRequest;
import org.fantasy.net.proto.RpcResponse;
import org.fantasy.net.server.RpcServer;
import org.fantasy.util.ClassUtils;
import org.fantasy.util.Constant;
import org.fantasy.util.ReflectionUtils;

public class RpcClient extends AbstractNioSocketChannel implements NioSocketChannel {

	private static final Logger LOG = Logger.getLogger(RpcServer.class);
	static final AtomicLong NEXT_REQUEST_ID = new AtomicLong(0);
	private static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();
	private static final AtomicLongFieldUpdater<RpcClient> SESSION_ID_UPDATER = AtomicLongFieldUpdater.newUpdater(RpcClient.class, "sessionId");
	private IOHandler ioHandler;
	private volatile long sessionId;
	public long clientId;
	private Generator<Long, Integer> clientIdGenerator = new ClientIdGenerator();
	private int connectTimeout;
	private BeanFactoryContext beanContext;
	
	private static SocketChannel newSocket(SelectorProvider provider) {
		try {
			return provider.openSocketChannel();
		} catch (IOException e) {
			throw new ChannelException("Failed to open a socket.", e);
		}
	}
	
	public RpcClient(String bindAddress, int port) {
		this(newSocket(DEFAULT_SELECTOR_PROVIDER), bindAddress, port);
		this.clientId = clientIdGenerator.generate(null);
	}
	
	public RpcClient(SocketChannel channel, String bindAddress, int port) {
		super(channel, bindAddress, port);
	}

	public void serviceInit() {
		super.doRegister();
		getConnection().startThread();
		this.ioHandler = new IOHandler(getConnection().selector, selectionKey(), this);
		this.connectTimeout = getConf().getInt("rpc.client.connect.timeout", Constant.DEFAULT_CLIENT_CONNECT_TIMEOUT);
	}

	public void serviceStart() {
		InetSocketAddress remoteAddress = new InetSocketAddress(getBindAddress(), getPort());
		try {
			connect(remoteAddress);
		} catch (Exception e) {
			throw new RpcClientException("Unable to connect to the specified IP and port;" + e.getMessage());
		}
	}

	public void serviceStop() {
		doClose();
	}

	private void connect(SocketAddress remoteAddress) throws IOException, ConnectTimeoutException {
		if(!isOpen())
			return;
		if(doConnect(remoteAddress)) {
			sendConnectRequest();
		}
		// give a chance to finish connect
		synchronized(this) {
			long startTime = System.currentTimeMillis();
			while(SESSION_ID_UPDATER.get(this) == 0) {
				try {
					wait(200);
					if(System.currentTimeMillis() - startTime >= connectTimeout) {
						throw new ConnectTimeoutException("Connect to server timed out");
					}
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	public IOHandler getIoHandler() {
		return ioHandler;
	}

	public long getSessionId() {
		return sessionId;
	}

	public void setSessionId(long sessionId) {
		if(SESSION_ID_UPDATER.compareAndSet(this, 0, sessionId)) {
//			SESSION_ID_UPDATER.set(this, sessionId);
		}
	}
	
	public void sendConnectRequest() {
		ConnectRequest connectRequest = ConnectRequest.newBuilder()
				.magic(Constant.MAGIC)
				.version(Constant.VERSION)
				.clientId(clientId)
				.build();
		ioHandler.writeRequest(connectRequest);
	}
	
	public RpcResponse sendRequest(MethodDescriptor md, Object[] args) {
		RpcRequest request = RpcRequest.newBuilder()
				.id(NEXT_REQUEST_ID.getAndIncrement())
				.methodDescriptor(md)
				.arguments(args)
				.build();
		RpcFuture future = new RpcFuture(request.getId());
		ioHandler.addFuture(future);
		ioHandler.writeRequest(request);
		if(future.hasError()) {
			LOG.error(future.getCause().getMessage());
			return null;
		}
		try {
			return future.get(future.rpcTimeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// Local Exception
			LOG.error("Current thread is interrupted", e);
			return null;
		}
	}

	public void setBeanContext(BeanFactoryContext beanContext) {
		this.beanContext = beanContext;
	}

	
	public void reconnect() {
		beanContext.reconnect(this);
	}
}
