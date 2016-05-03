package org.fantasy.net.server;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.fantasy.conf.Configuration;
import org.fantasy.net.Packet;
import org.fantasy.util.Constant;
import org.fantasy.util.MemoryUtils;

public class Writer extends Thread implements Closeable {

	private static final Logger LOG = Logger.getLogger(Writer.class);

	private Selector writeSelector;
	private int pending;
	private volatile boolean running = true;
	LinkedList<Packet> responseQueue = new LinkedList<Packet>();
//	public static final int NIO_BUFFER_LIMIT = 1024 * 64;
	private Configuration conf;
	
	public Writer(Configuration conf) throws IOException {
		this.setName("Write Thread");
		this.setDaemon(true);
		writeSelector = Selector.open();
		this.conf = conf;
	}
	
	public void run() {
		try {
			try {
				while(running) {
					waitPending();
					writeSelector.select();
					Iterator<SelectionKey> iterator = writeSelector.selectedKeys().iterator();
					while(iterator.hasNext()) {
						SelectionKey key = iterator.next();
						iterator.remove();
						try {
							if(key.isValid() && key.isWritable()) {
								doWrite(key);
							}
						} catch(Exception e) {
							LOG.error("Write error!", e);
						}
					}
				}
			} catch(OutOfMemoryError e) {
				if (
						e instanceof OutOfMemoryError || 
						(e.getCause() != null && e.getCause() instanceof OutOfMemoryError) || 
						(e.getMessage() != null && e.getMessage().contains("java.lang.OutOfMemoryError"))
				) {
					LOG.fatal("Run out of memory", e);
				}
			} catch (Exception e) {
				LOG.error("Exception in writer ", e);
			}
				
		} finally {
			try {
				writeSelector.close();
			} catch (IOException e) {
				LOG.error("Could't close selector, error!", e);
			}
		}
	}
	
	private void doWrite(SelectionKey key) throws IOException {

		Packet packet = (Packet)key.attachment();
		if(packet == null) {
			return;
		}
		
		if(key.channel() != packet.getChannel()) {
			throw new IOException("bad channel.");
		}
		
		synchronized(responseQueue) {
			if(writeResponse()) {
				try {
					if(responseQueue.isEmpty())
						key.interestOps(0);
				} catch(CancelledKeyException e) {
					LOG.warn("Exception while changing ops : " + e);
				}
			}
		}
	}
	
	public boolean writeResponse() throws IOException {
		boolean done = false;
		boolean setOpWrite = false;
		for(;;) {
			Packet packet = null;
			synchronized(responseQueue) {
				if(responseQueue.isEmpty()) {
					return true;
				} else {
					packet = responseQueue.getFirst();
					SocketChannel channel = packet.getChannel();
					if(channel == null) {
						throw new IOException("Socket channel is null");
					}
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
								responseQueue.removeFirst();
								// clean direct ByteBuffer
								MemoryUtils.freeDirectBuffer(packet.getData());
								packet = null;
								done = true;
								break;
							}
							expectedWrittenBytes -= writtenBytes;
						} finally {
							originalBuf.limit(originalLimit);
						}
					}
//					 写操作未完成
					if(!done) {
						if(setOpWrite)
							enableWrite(packet);
					}
					break;
				}
			}
		}
		return done;
	}
	
	public void doRespond(Packet packet) throws IOException {
		synchronized(responseQueue) {
			responseQueue.addLast(packet);
		}
		writeResponse();
	}

	private boolean enableWrite(Packet packet) {
		boolean finished = false;
		incPending();
		try {
			SocketChannel channel = packet.getChannel();
			writeSelector.wakeup();
			channel.register(writeSelector, SelectionKey.OP_WRITE, packet);
		} catch (ClosedChannelException e) {
			finished = true;
		} finally {
			decPending();
		}
		return finished;
	}

	private synchronized void incPending() {
		pending++;
	}

	private synchronized void decPending() {
		pending--;
		notify();
	}

	private synchronized void waitPending() throws InterruptedException {
		if(pending > 0) {
			wait();
		}
	}

	public void close() {
		writeSelector.wakeup();
		Thread.yield();

		running = false;
		responseQueue.clear();
	}

}
