package org.fantasy.net.server;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.apache.log4j.Logger;
import org.fantasy.net.Packet;

public class Reader extends Thread implements Closeable {

	private static final Logger LOG = Logger.getLogger(Reader.class);
	private Selector selector;
	private volatile boolean adding = false;
	private volatile boolean running = true;
	
	public Reader() {
		try {
			selector = Selector.open();
		} catch (IOException e) {
			throw new RuntimeException("Failed to open selector.", e);
		}
	}

	public void run() {
		try {
			doRead();
		} finally {
			try {
				selector.close();
			} catch (IOException e) {
				LOG.error("error ! closing selector.", e);
			}
		}
	}
	
	private synchronized void doRead() {
		while (running) {
			SelectionKey key = null;
			try {
				selector.select();
				if (adding) {
					this.wait(1000);
				}
				Set<SelectionKey> keys = selector.selectedKeys();
				for (Iterator<SelectionKey> iterator = keys.iterator(); iterator.hasNext();) {
					key = iterator.next();
					iterator.remove();
					if (key.isValid()) {
						if (key.isReadable()) {
							doRead(key);
						}
					}
				}
				keys.clear();
			} catch (InterruptedException e) {
				LOG.error("unexpectedly interrupted.", e);
			} catch (IOException e) {
				LOG.error("select() error!", e);
			} 
		}
	}
	
	
	private void doRead(SelectionKey key) throws InterruptedException {
		NioSession session = (NioSession)key.attachment();
		if(session == null)
			return;
		try {
			session.doRead(/** key */);
		} catch (IOException ioe) {
			LOG.error("Close session, client closed ?", ioe);
			// free session
			// gc
			session.sessionManager.removeSession(session);
			session.close();
			session = null;
			key.attach(null);
			key.cancel();
		}
	}
	
	public synchronized SelectionKey registerRead(SocketChannel socketChannel) throws ClosedChannelException {
		return socketChannel.register(selector, SelectionKey.OP_READ);
	}
	
	public void startAdd() {
		adding = true;
		selector.wakeup();
	}
	
	
	public synchronized void finishAdd() {
		adding = false;
		this.notify();
	}

	public void close() throws IOException {
		selector.wakeup();
		Thread.yield();
//		selector.close();
//		selector = null;
		running = false;
	}
	
	
}
