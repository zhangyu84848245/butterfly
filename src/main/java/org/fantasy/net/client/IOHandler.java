package org.fantasy.net.client;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.fantasy.conf.Configuration;
import org.fantasy.net.ChannelException;
import org.fantasy.net.Future;
import org.fantasy.net.IOCallback;
import org.fantasy.net.NetUtils;
import org.fantasy.net.Packet;
import org.fantasy.net.io.Deserializer;
import org.fantasy.net.io.Serialization;
import org.fantasy.net.io.SerializationFactory;
import org.fantasy.net.io.Serializer;
import org.fantasy.net.proto.ConnectResponse;
import org.fantasy.net.proto.ExceptionResponse;
import org.fantasy.net.proto.HeartbeatRequest;
import org.fantasy.net.proto.HeartbeatResponse;
import org.fantasy.net.proto.PacketHeader;
import org.fantasy.net.proto.Response;
import org.fantasy.net.proto.RpcRequest;
import org.fantasy.net.proto.RpcResponse;
import org.fantasy.util.Constant;
import org.fantasy.util.MemoryUtils;



public class IOHandler {

	private static final Logger LOG = Logger.getLogger(IOHandler.class);
	private Selector selector;
	private SelectionKey selectionKey;
	private Serialization<Serializable> serialization;
	// 写队列的链表
	private LinkedList<Packet> writeQueue = new LinkedList<Packet>();

	private Configuration conf;
	private RpcClient client;
	HeartbeatHandler heartbeatHandler;
	public Packet heartbeat;
	private int heartbeatFailedCounter = 0;
	private boolean initialize = false;
	
	private ConcurrentSkipListMap<Long, Future<?>> futures = new ConcurrentSkipListMap<Long, Future<?>>();
	
	public IOHandler(Selector selector, SelectionKey selectionKey, RpcClient client) {
		this.selector = selector;
		this.selectionKey = selectionKey;
		this.client = client;
		this.conf = client.getConf();
		this.serialization = SerializationFactory.get("unsafe");
	}

	
	private void write0(Serializable request, IOCallback<Packet> callback) {
		Serializer<Serializable> serializer = serialization.getSerializer();
		ByteBuffer buffer = null;
		Packet packet = null;
		try {
			buffer = serializer.serialize(request);
			packet = new Packet(serializer.serializedBytes(), buffer);
			callback.call(packet);
		} catch (IOException e) {
			LOG.error("Serialize request data error", e);
//			throw new RuntimeException(e);
		} finally {
//			synchronized(serializer) {
//				while(buffer.position() != buffer.capacity()) {
//					try {
//						serializer.wait(20);
//					} catch (InterruptedException e) {
//						throw new RuntimeException(e);
//					}
//				}
//			}
//			try {
//				serializer.close();
//			} catch (IOException e) {
//				// ignore
//			}
			// then write finished  ->  serializer.close();
			serializer = null;
			packet = null;
		}
		
	}

	private Packet getHeartbeat() {
		Serializer<Serializable> serializer = serialization.getSerializer();
		HeartbeatRequest heartbeat = null;
		ByteBuffer buf = null;
		try {
			heartbeat = HeartbeatRequest.newBuilder()
					.id(RpcClient.NEXT_REQUEST_ID.getAndIncrement())
					.clientId(client.clientId)
					.build();
			buf = serializer.serialize(heartbeat);
			int length = serializer.serializedBytes();
			byte[] array = new byte[length];
			MemoryUtils.copyMemory(null, MemoryUtils.directBufferAddress(buf), array, MemoryUtils.arrayBaseOffset(), length);
			return new Packet(serializer.serializedBytes(), ByteBuffer.wrap(array));
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				serializer.close();
			} catch (IOException e) {
			}
			serializer = null;
			heartbeat = null;
			buf = null;
		}
	}
	
	public void writeRequest(final Serializable request) {
		write0(request, new IOCallback<Packet>() {
			public void call(Packet packet) {
				synchronized(writeQueue) {
					writeQueue.addLast(packet);
					try {
						doWrite();
					} catch (IOException e) {
						LOG.error("Channel write data error", e);
						if(writeQueue.contains(packet))
							writeQueue.remove(packet);
						long id = ((RpcRequest)request).getId();
						Future<RpcRequest> future = (Future<RpcRequest>)futures.get(id);
						if(future != null)
							future.setCause(e);
						futures.remove(id);
						return;
					}
				}
				
			}
		});
	}
	
	public void addFuture(Future<?> future) {
		futures.put(future.getId(), future);
	}

	public void sendHeartBeat() {
		if(heartbeat == null)
			heartbeat = getHeartbeat();

		synchronized(writeQueue) {
			writeQueue.addLast(heartbeat);
			try {
				doWrite();
			} catch (IOException e) {
				LOG.error("Channel write data error", e);
				if(writeQueue.contains(heartbeat))
					writeQueue.remove(heartbeat);
				heartbeatFailedCounter++;
				if(heartbeatFailedCounter >= 5) {
					LOG.fatal("Server is hung up");
					LOG.info("Searching for the available servers");
					client.reconnect();
					heartbeatHandler.stop();
				}
				return;
			}
		}
	}

	public synchronized void enableWrite() {
		if(!selectionKey.isValid())
			return;
		final int ops = selectionKey.interestOps();
		if((ops & SelectionKey.OP_WRITE) == 0) {
			selectionKey.interestOps(ops | SelectionKey.OP_WRITE);
		}
	}
	
	public synchronized void disableWrite() {
		if(!selectionKey.isValid())
			return;
		int ops = selectionKey.interestOps();
		if((ops & SelectionKey.OP_WRITE) != 0) {
			selectionKey.interestOps(ops & (~SelectionKey.OP_WRITE));
		}
	}
	
	private void read0(IOCallback<Serializable> callback) throws IOException {
		SocketChannel channel = (SocketChannel)this.selectionKey.channel();
		if(channel == null)
			throw new ChannelException("Channel is null");

		PacketHeader header = NetUtils.readRequestHeader(channel);
		if(header == null)
			return;
		int dataLength = header.getDataLength();
		Deserializer<Serializable> deserializer = serialization.getDeserializer();
		Packet packet = new Packet(dataLength);
		try {
			while(true) {
				NetUtils.channelRead(channel, packet.getData());
				if(packet.isDone())
					break;
			}
			callback.call(deserializer.deserialize(packet.getData()));
		} catch (ClassNotFoundException e) {
			LOG.error(e.getMessage());
			throw new IOException(e.getMessage());
		} finally {
			deserializer.close();
			deserializer = null;
			packet = null;
		}
	}

	public void doRead() throws IOException {
		read0(new IOCallback<Serializable>() {
			public void call(Serializable object) {
				if(object instanceof RpcResponse) {
					RpcResponse response = (RpcResponse)object;
					long id = response.getId();
					Future<RpcResponse> future = (Future<RpcResponse>)futures.get(id);
					if(future != null)
						future.setResponse(response);
					futures.remove(id);
				} else if(object instanceof HeartbeatResponse) {
					HeartbeatResponse response = (HeartbeatResponse)object;
					if(client.getSessionId() != response.getSessionId())
						LOG.info("Session id mismatch");
					if(heartbeatFailedCounter != 0) {
						heartbeatFailedCounter = 0;
					}
				} else if(object instanceof ConnectResponse) {
					ConnectResponse response = (ConnectResponse)object;
					client.setSessionId(response.getSessionId());
					initialize = true;
					heartbeatHandler = new HeartbeatHandler(conf, IOHandler.this);
					heartbeatHandler.start();
				}
			}
		});
	}

	public void doWrite() throws IOException {
		SocketChannel channel = (SocketChannel)selectionKey.channel();
		if(channel == null) {
			throw new IOException("Socket is null");
		}
		boolean done = false;
		boolean setOpWrite = false;
		for(;;) {
			Packet packet = null;
			synchronized(writeQueue) {
				if(writeQueue.isEmpty()) {
					packet = null;
					done = true;
				} else {
					packet = writeQueue.getFirst();
				}
				
				if(packet != null) {
					int expectedWrittenBytes = packet.getLength();
					ByteBuffer originalBuf = packet.getData();
					int originalLimit = originalBuf.limit();
					while(originalBuf.hasRemaining()) {
						try {
							int writableBytes = Math.min(originalBuf.remaining(), Constant.MAX_BUFFER_SIZE);
							originalBuf.limit(originalBuf.position() + writableBytes);
							int writtenBytes = channel.write(originalBuf);
							// 写管道满
							if(writtenBytes == 0) {
								packet.setLength(expectedWrittenBytes);
								setOpWrite = true;
								break;
							}
							int remaining = expectedWrittenBytes - writtenBytes;
							if(remaining == 0) {
								writeQueue.removeFirst();
								MemoryUtils.freeDirectBuffer(packet.getData());
								packet = null;
								done = true;
								// update heartbeat
								if(initialize)
									heartbeatHandler.setLastExecutionTime(System.currentTimeMillis());
								break;
							}
							expectedWrittenBytes -= writtenBytes;
						} finally {
							// heartbeat
							if(!originalBuf.isDirect())
								originalBuf.clear();
							originalBuf.limit(originalLimit);
						}
					}
				}
				// 写操作未完成
				if(!done) {
					if(setOpWrite)
						enableWrite();
					break;
				}
				if(writeQueue.isEmpty()) {
					// buf 分多次发送的情况
					disableWrite();
					break;
				}
			}
		}
	}


	public HeartbeatHandler getHeartbeatHandler() {
		return heartbeatHandler;
	}

	
}
