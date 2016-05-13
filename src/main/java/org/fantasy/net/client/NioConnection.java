package org.fantasy.net.client;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.apache.log4j.Logger;
import org.fantasy.net.ChannelException;
import org.fantasy.net.proto.ExceptionResponse;
import org.fantasy.net.proto.RpcResponse;
import org.fantasy.util.ClassUtils;
import org.fantasy.util.ReflectionUtils;


public class NioConnection extends Thread {

	private static final Logger LOG = Logger.getLogger(NioConnection.class);
	private static final int NOT_START = -1;
	private static final int STARTED = 0;
	private static final int SHUTDOWN = 1;
	private static final AtomicIntegerFieldUpdater<NioConnection> STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(NioConnection.class, "state");
	// 可见性
	volatile boolean running = true;
	protected Selector selector;// = Selector.open();
	private volatile int state = NOT_START;
	private SelectorProvider provider;
	public boolean init = false;

	public NioConnection(/** ThreadFactory threadFactory, */SelectorProvider provider) {
		this.provider = provider;
		openSelector();
	}
	
	private void openSelector() {
		try {
			selector = provider.openSelector();
		} catch (IOException e) {
			throw new ChannelException("failed to open a new selector", e);
		}
	}

	public void run() {
		while(running) {
			try {
				selector.select(100);
				Set<SelectionKey> keys = null;
				synchronized(this) {
					keys = selector.selectedKeys();
				}
				for(Iterator<SelectionKey> iterator = keys.iterator();iterator.hasNext();) {
					SelectionKey key = iterator.next();
					if(key.isValid()) {
						SocketChannel channel = (SocketChannel)key.channel();
						RpcClient client = (RpcClient)key.attachment();
						try {
							if(key.isConnectable()) {
								int ops = key.interestOps();
								ops = (ops & ~SelectionKey.OP_CONNECT);
								key.interestOps(ops);
								if(channel.finishConnect()) {
									key.interestOps(SelectionKey.OP_READ);
									client.sendConnectRequest();
								} else
									throw new Error();
							} else if(key.isReadable()) {
								client.getIoHandler().doRead();
							} else if(key.isWritable()) {
								client.getIoHandler().doWrite();
							}
						} catch(IOException e) {
							LOG.error(e);
						}
					} 
				}
				keys.clear();
			} catch (IOException e) {
				LOG.error(e);
			}
		}
	}
	
	public void startThread() {
		if(STATE_UPDATER.get(this) == NOT_START) {
			if(STATE_UPDATER.compareAndSet(this, NOT_START, STARTED)) {
				start();
			}
		}
	}
	
	
	public void stopThread() {
		if(STATE_UPDATER.get(this) == STARTED) {
			if(STATE_UPDATER.compareAndSet(this, STARTED, SHUTDOWN)) {
				running = false;
				interrupt();
			}
		}
	}
}
