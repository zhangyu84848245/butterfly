package org.fantasy.net.server;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import org.fantasy.net.ChannelException;
import org.fantasy.service.AbstractService;
import org.apache.log4j.Logger;

public abstract class AbstractNioServerSocketChannel extends AbstractService implements NioServerSocketChannel {

	private static final Logger LOG = Logger.getLogger(AbstractNioServerSocketChannel.class);
	private ServerSocketChannel ch;
	private SocketConf socketConf;
	public static final int DEFAULT_PORT = 47047;
	String bindAddress;
	int port = DEFAULT_PORT;
	
	public AbstractNioServerSocketChannel(ServerSocketChannel ch, String bindAddress, int port) {
		this.bindAddress = bindAddress;
		if(port > 0) {
			this.port = port;
		}
		this.ch = ch;
		socketConf = new DefaultSocketConf(ch.socket());
	}

	public ServerSocketChannel getChannel() {
		return ch;
	}

	public void bind(SocketAddress local, int backlog) {
		try {
			getChannel().socket().bind(local, backlog);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean isBound() {
		return getChannel().socket().isBound();
	}

	public int getLocalPort() {
		return getChannel().socket().getLocalPort();
	}

	public void register(Selector selector, int ops) {
		try {
			ch.register(selector, ops);
		} catch (ClosedChannelException e) {
			throw new ChannelException(e);
		}
	}

	public void configureBlocking(boolean block) {
		try {
			ch.configureBlocking(block);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public SocketConf getSocketOption() {
		return socketConf;
	}

	public void close() {
		try {
			if(ch != null) {
				ch.socket().close();
				ch.close();
				ch = null;
			}
		} catch (IOException e) {
			// ignore
			LOG.error("Exception in closing channel or channel socket.", e);
		}
	}

}