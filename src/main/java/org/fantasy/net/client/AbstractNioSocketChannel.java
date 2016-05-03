package org.fantasy.net.client;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;
import org.fantasy.net.ChannelException;
import org.fantasy.service.AbstractService;
import org.fantasy.util.Constant;



public abstract class AbstractNioSocketChannel extends AbstractService implements NioSocketChannel {

	private static final Logger LOG = Logger.getLogger(AbstractNioSocketChannel.class);
	private SocketChannel channel;
	private String bindAddress;
	private int port;
	private volatile SelectionKey selectionKey;
	private NioConnection connection;
	private Socket socket;
	public AbstractNioSocketChannel(SocketChannel channel, String bindAddress, int port) {
		this.channel = channel;
		try {
			this.channel.configureBlocking(false);
		} catch (IOException e) {
			try {
				channel.close();
			} catch (IOException ioe) {
				LOG.error("Close channel error");
			}
			throw new ChannelException(e);
		}
		this.socket = channel.socket();
//		 close
		try {
			this.socket.setSoLinger(false, -1);
			this.socket.setTcpNoDelay(true);
			// ack
			this.socket.setKeepAlive(true);
			// 设置超时
//			this.socket.setSoTimeout(60000);
		} catch (SocketException e) {
			throw new ChannelException(e);
		}

		this.bindAddress = bindAddress;
		this.port = port;
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public String getBindAddress() {
		return bindAddress;
	}

	public int getPort() {
		return port;
	}

	public boolean doConnect(SocketAddress remoteAddress) throws IOException  {
		boolean success = false;
		try {
			boolean connected = getChannel().connect(remoteAddress);
			if(!connected) {
				selectionKey().interestOps(SelectionKey.OP_CONNECT);
			}
			success = true;
			return connected;
		} finally {
			if(!success) {
				doClose();
			}
		}
	}

//	public boolean isActive() {
//		SocketChannel channel = getChannel();
//		return channel.isOpen() && channel.isConnected();
//	}
	
	public boolean isOpen() {
		return getChannel().isOpen();
	}

	protected SelectionKey selectionKey() {
        return selectionKey;
    }

	
	protected void doRegister() {
		try {
			this.selectionKey = getChannel().register(connection.selector, 0, this);
			return;
		} catch (ClosedChannelException e) {
			throw new ChannelException(e);
		}
	}

	public void doClose() {
		
		try {
			getChannel().close();
		} catch (IOException e) {
			LOG.error(e);
		}
		channel = null;
		if(selectionKey != null) {
			selectionKey.cancel();
			selectionKey.attach(null);
			selectionKey = null;
		}
		if(socket != null) {
			try {
				socket.shutdownInput();
			} catch (IOException e) {
				LOG.error(e);
			}
			try {
				socket.shutdownOutput();
			} catch (IOException e) {
				LOG.error(e);
			}
			try {
				socket.close();
			} catch (IOException e) {
				LOG.error(e);
			}
		}
	}

	public void setConnection(NioConnection connection) {
		this.connection = connection;
	}

	public NioConnection getConnection() {
		return connection;
	}

}
