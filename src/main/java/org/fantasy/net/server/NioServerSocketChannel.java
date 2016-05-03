package org.fantasy.net.server;

import java.net.SocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public interface NioServerSocketChannel {

	public void configureBlocking(boolean block);
	
	public void bind(SocketAddress local, int backlog);
	
	public void register(Selector selector, int ops);

	public boolean isBound();
	
	public ServerSocketChannel getChannel();
	
	public int getLocalPort();
	
	public SocketConf getSocketOption();

	public void close();

}
