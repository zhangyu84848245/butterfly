package org.fantasy.net.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;
import org.fantasy.conf.Configurable;
import org.fantasy.conf.Configuration;
import org.fantasy.net.ChannelException;
import org.fantasy.util.StringUtils;

public class Listener extends Thread implements Configurable, Closeable {

	private static final Logger LOG = Logger.getLogger(Listener.class);
	public static final int DEFAULT_READ_THREAD_NUM = 1 << 3;

	private Selector selector;
	private InetSocketAddress address;
	private int listenPort;
	private boolean running = true;
	private ExecutorService executorService;
	private Configuration conf;
	private ThreadFactory threadFactory = new NamedThreadFactory();
	private Chooser<Reader> taskChooser;
	RpcServer server;
	private Writer writer;
	private NioSessionManager sessionManager = new NioSessionManager();
	private Reader[] readers;
	
	
	public Listener(SelectorProvider selectorProvider, String bindAddress, int port, RpcServer server, Writer writer, Configuration conf) throws IOException {
		if(StringUtils.isEmpty(bindAddress))
			throw new IllegalArgumentException("address");
		if(port < 0)
			port = RpcServer.DEFAULT_PORT;
		
		this.server = server;
		this.server.configureBlocking(false);
		address = new InetSocketAddress(bindAddress, port);
		this.selector = selectorProvider.openSelector();
		this.server.bind(address, server.getSocketOption().getBacklog());
		this.listenPort = server.getLocalPort();
		server.register(selector, SelectionKey.OP_ACCEPT);
		this.setName("Listening on " + listenPort);
		this.setDaemon(true);
		this.conf = conf;
		int receiveThreads = conf.getInt("rpc.read.thread.number");
		if(receiveThreads < 0) {
			receiveThreads = DEFAULT_READ_THREAD_NUM;
		}
		executorService = Executors.newFixedThreadPool(receiveThreads, threadFactory);
		readers = new Reader[receiveThreads];
		for(int i = 0; i < receiveThreads; i++) {
			Reader reader = new Reader();
			readers[i] = reader;
			executorService.execute(reader);
		}
		taskChooser = new ReceiverChooser(readers);
		this.writer = writer;
	}

	public void run() {
		while(running) {
			SelectionKey key = null;
			try {
				selector.select();
				Set<SelectionKey> keys = selector.selectedKeys();
				for(Iterator<SelectionKey> iterator = keys.iterator();iterator.hasNext();) {
					key = iterator.next();
					iterator.remove();
					if(key.isValid()) {
						if(key.isAcceptable()) {
							doAccept(key);
						}
					} 
				}
				keys.clear();
			} catch (OutOfMemoryError e) {
				if(
						e instanceof OutOfMemoryError || 
						(e.getCause() != null && e.getCause() instanceof OutOfMemoryError) || 
						(e.getMessage() != null && e.getMessage().contains("java.lang.OutOfMemoryError"))
				) {
					LOG.fatal("Out of Memory in server select", e);
				}
				closeSession(key, e);
			} catch(Exception e) {
				closeSession(key, e);
			}
		}
		LOG.info("Stopping server");
		
		synchronized(this) {
			try {
				server.close();
				selector.close();
				server = null;
				selector = null;
				List<NioSession> sessionList = sessionManager.getSessions();
				for(Iterator<NioSession> iterator = sessionList.iterator();iterator.hasNext();) {
					NioSession session = iterator.next();
					session.close();
				}
				sessionList.clear();
			} catch (IOException e) {
				LOG.error("Error in closing selector");
			}
		}
	}


	private void doAccept(SelectionKey key) throws IOException {
		ServerSocketChannel ssc = (ServerSocketChannel)key.channel();
		SocketChannel channel = null;
		while((channel = ssc.accept()) != null) {
			
			try {
				channel.configureBlocking(false);
			} catch (IOException e) {
				channel.close();
				throw new ChannelException("Configure channel error!", e);
			}

			assert taskChooser != null;
			Reader reader = taskChooser.next();
			SelectionKey rKey = null;
			try {
				reader.startAdd();
				rKey = reader.registerRead(channel);
			} finally {
				reader.finishAdd();
			}

			NioSession session = new NioSession(channel, rKey, conf, writer, server, sessionManager);
			rKey.attach(session);
			sessionManager.addSession(session);

		}
		
	}

	private void closeSession(SelectionKey sKey, Throwable cause) {
		if(sKey != null) {
			NioSession session = (NioSession)sKey.attachment();
			if(session != null) {
				sessionManager.removeSession(session);
				session.close();
				session = null;
			}
			sKey.attach(null);
		}
		LOG.error("Disconnected from client", cause);
	}

	public void setConfig(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return conf;
	}

	public void close() {
		
		if(selector != null) {
			selector.wakeup();
			Thread.yield();
		}
		this.running = false;
//		try {
//			selector.close();
//		} catch (IOException e) {
//		}
		
		try {
			if(readers != null) {
				for(Reader reader : readers) {
					reader.close();
				}
			}
		} catch (IOException e) {
			// ignore
		}
		executorService.shutdownNow();

		try {
			sessionManager.close();
		} catch (IOException e) {
		}
		
		// RpcServer has closed  see line 106
//		this.server.close();
	}
	
	
}
