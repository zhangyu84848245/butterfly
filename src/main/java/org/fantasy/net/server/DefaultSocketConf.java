package org.fantasy.net.server;

import java.net.ServerSocket;
import java.net.SocketException;

public class DefaultSocketConf implements SocketConf {

	
	private ServerSocket serverSocket;
	

	private volatile int backlog = 128;
	
//	private RpcServer server;
	
	public DefaultSocketConf(/**  RpcServer server, */ServerSocket serverSocket) {
		if(serverSocket == null)
			throw new NullPointerException("serverSocket");
		this.serverSocket = serverSocket;
//		this.server = server;
	}


	public boolean isReuseAddress()  {
		try {
			return serverSocket.getReuseAddress();
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
	}


	public void setReuseAddress(boolean reuseAddress) {
		try {
			serverSocket.setReuseAddress(reuseAddress);
		} catch (SocketException e) {
			throw new RuntimeException(e);
		}
	}

	public void setBacklog(int backlog) {
		if(backlog < 0) {
			throw new IllegalArgumentException("backlog: " + backlog);
		}
		this.backlog = backlog;
	}


	public int getBacklog() {
		return backlog;
	}
	
	
	
}
