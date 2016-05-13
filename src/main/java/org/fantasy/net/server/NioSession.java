package org.fantasy.net.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;
import org.fantasy.common.Generator;
import org.fantasy.conf.Configuration;
import org.fantasy.net.IOCallback;
import org.fantasy.net.NetUtils;
import org.fantasy.net.Packet;
import org.fantasy.net.io.Deserializer;
import org.fantasy.net.io.Serialization;
import org.fantasy.net.io.SerializationException;
import org.fantasy.net.io.SerializationFactory;
import org.fantasy.net.io.Serializer;
import org.fantasy.net.proto.ExceptionResponse;
import org.fantasy.net.proto.PacketHeader;
import org.fantasy.net.proto.RpcRequest;
import org.fantasy.net.proto.RpcResponse;
import org.fantasy.util.ReflectionUtils;

public class NioSession implements Closeable {

	private static final Logger LOG = Logger.getLogger(NioSession.class);

	SocketChannel channel;
	private SelectionKey sKey;
	private long sessionId;
	private Socket socket;
	private InetAddress remoteAddress;
	public static final String SERIALIZER_FACTORY_KEY = "rpc.io.default.serializerFactory";
	private Serialization<Serializable> serialization;
	private Configuration conf;
	private RequestProcessor requestProcessor;
	private Writer writer;
	private Generator<Long, Integer> sessionIdGenerator;
	private boolean closed = false;
	protected NioSessionManager sessionManager;
	RpcServer server;
	private long clientId;
	
	public NioSession(SocketChannel channel, SelectionKey sKey, Configuration conf, Writer sender/**, long touch */, RpcServer server, NioSessionManager sessionManager) throws SocketException {
		this.channel = channel;
		this.sKey = sKey;
		this.socket = channel.socket();
		socket.setTcpNoDelay(true);
		socket.setSoLinger(false, -1);
		this.sKey.interestOps(SelectionKey.OP_READ);
		this.conf = conf;
		this.serialization = SerializationFactory.get("unsafe");
		requestProcessor = new RequestProcessor(this);
		requestProcessor.setDaemon(true);
		requestProcessor.start();
		this.writer = sender;
		sessionIdGenerator = new SessionIdGenerator();
		this.sessionId = sessionIdGenerator.generate(null);
		this.sessionManager = sessionManager;
		this.server = server;
	}
	
	public void doRead() throws IOException {
		if(!isChannelOpen()) {
			LOG.error("Trying to do i/o, but channel is not open or has closed!");
			return;
		}
		PacketHeader header = null;
		try {
			header = NetUtils.readRequestHeader(channel);
			if(header == null)
				throw new IOException("May be channel closed?");
			read0(new IOCallback<Serializable>() {
				public void call(Serializable object) {
					requestProcessor.process(object);
				}
			}, header.getDataLength());
		} catch (IOException e) {
			// 将管道读空
			skipFully();
			// 此处主要判断序列化是的IOException
			if(e instanceof SerializationException) {
				requestProcessor.process(RpcRequest.newBuilder().error(e).id(header.getId()).build());
			} else {
				// 可能是客户端强制关闭造成的异常
				throw e;
			}
		}
	}
	
	private void skipFully() {
		ByteBuffer buf = ByteBuffer.allocate(256);
		try {
			// fix bug 
			// -1 / 0
			while((channel.read(buf)) > 0) {
				buf.clear();
			}
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			// ignore
		} finally {
			buf.clear();
			buf = null;
		}
	}
	
	private void read0(IOCallback<Serializable> callback, int dataLength) throws IOException {
		if(dataLength > 0) {
			Packet packet = new Packet(dataLength);
			Deserializer<Serializable> deserializer = serialization.getDeserializer();
			try {
				while(true) {
					NetUtils.channelRead(channel, packet.getData());
					if(packet.isDone())
						break;
				}
				try {
					callback.call(deserializer.deserialize(packet.getData()));
				} catch (IOException e) {
					throw new SerializationException(e);
				}
			} catch (ClassNotFoundException e) {
				LOG.error(e.getMessage());
				throw new IOException(e);
			} finally {
				try {
					deserializer.close();
				} catch (IOException e) {
					// ignore
				}
				deserializer = null;
				packet = null;
			}
		} else {
			throw new IOException("Read data length error!");
		}
	}

	private void write0(Serializable response, IOCallback<Packet> callback) throws IOException {
		ByteBuffer dataBuffer = null;
		Packet packet = null;
		Serializer<Serializable> serializer = serialization.getSerializer();
		try {
			// case : this line exception
			dataBuffer = serializer.serialize(response);
			packet = new Packet(serializer.serializedBytes(), dataBuffer, channel);
			callback.call(packet);
		} finally {
			// give a chance to write complete
//			synchronized(serializer) {
//				while((dataBuffer.position()!= dataBuffer.capacity())) {
//					try {
//						serializer.wait(20);
//					} catch (InterruptedException e) {
//						LOG.error(e.getMessage());
//						throw new IOException(e);
//					}
//				}
//			}
//			serializer.close();
			serializer = null;
			packet = null;
			dataBuffer = null;
		}
	}
	
	public void doWrite(final Serializable response) {
		try {
			write0(response, new IOCallback<Packet>() {
				public void call(Packet packet) {
					try {
						writer.doRespond(packet);
					} catch (IOException e) {
						handleException(e, response);
					}
				}
			});
		} catch (IOException e) {
			handleException(e, response);
		}
	}
	
	
	private void handleException(Exception e, Serializable response) {
		LOG.error(e);
		ExceptionResponse error = ExceptionResponse.newBuilder()
				.exceptionClassName(e.getClass().getName())
				.message(e.getMessage())
				.host(channel.socket().getLocalAddress().getHostAddress())
				.port(channel.socket().getLocalPort())
				.build();
		Field errorField = ReflectionUtils.getField(response.getClass(), "error");
		try {
			errorField.set(response, error);
		} catch (Exception ex) {
			LOG.error(ex);
			((RpcResponse)response).setError(error);
		}
		try {
			write0(error, new IOCallback<Packet>() {
				public void call(Packet packet) {
					try {
						writer.doRespond(packet);
					} catch (IOException e) {
						// ignore
					}
				}
			});
		} catch (IOException e1) {
			// ignore
		}
	}
	
	private boolean isChannelOpen() {
		return channel.isOpen();
	}
	
	public InetAddress getRemoteAddress() {
		return remoteAddress;
	}
	
	public void close() {
		
		if(!closed) {
			if(!channel.isOpen()) {
				return;
			}
			
			try {
				socket.shutdownOutput();
			} catch (IOException e) {
				// ignore
				LOG.error("Ignoring socket output shutdown exception.", e);
			}
			
			try {
				socket.shutdownInput();
			} catch (IOException e) {
				// ignore
				LOG.error("Ignoring socket input shutdown exception.", e);
			}
			
			
			try {
				if(channel.isOpen()) {
					channel.close();
				}
			} catch (IOException e) {
				// ignore
			}
			
			try {
				socket.close();
			} catch (IOException e) {
				// ignore
			}
			
//			if(sKey != null) {
//				sKey.cancel();
//			}
			
			requestProcessor.interrupt();
			closed = true;
		}
		
	}

	public long getSessionId() {
		return sessionId;
	}
	
	public Object getBean(String beanName) {
		return server.getBean(beanName);
	}

	public long getClientId() {
		return clientId;
	}

	public void setClientId(long clientId) {
		this.clientId = clientId;
	}

}
