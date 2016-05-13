package org.fantasy.net.client;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.spi.SelectorProvider;
import java.util.concurrent.ThreadFactory;

import org.fantasy.conf.Configuration;
import org.fantasy.net.server.Chooser;
import org.fantasy.net.server.NamedThreadFactory;

public class ConnectionPool implements Closeable {
	
	
	private NioConnection[] connections;
	private Chooser<NioConnection> chooser;
	private int nThreads;
//	private ThreadFactory threadFactory = new NamedThreadFactory();
	private static final SelectorProvider SELECTOR_PROVIDER = SelectorProvider.provider();

	public ConnectionPool(Configuration conf) throws IOException {
		this.nThreads = conf.getInt("nio.connection.threads", Runtime.getRuntime().availableProcessors() * 2);
		connections = new NioConnection[nThreads];
		for(int i = 0; i < nThreads; i++) {
			connections[i] = new NioConnection(/** threadFactory, */SELECTOR_PROVIDER);
			// 此处不能直接执行线程, 否则  register 是会很慢
//			connections[i].start();
		}
		chooser = new ConnectionChooser(connections);
	}
	
	public NioConnection next() {
		return chooser.next();
	}

	public void close() throws IOException {
		for(int i = 0; i < nThreads; i++) {
			connections[i].stopThread();
		}
		chooser = null;
		nThreads = 0;
	}

}
