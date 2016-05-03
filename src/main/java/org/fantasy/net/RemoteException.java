package org.fantasy.net;

import java.io.IOException;

public class RemoteException extends IOException {

	private static final long serialVersionUID = -7214191337884280076L;
	private String className;
	private String host;
	private int port;
	
	public RemoteException(String className, String message, String host, int port) {
		super(message);
		this.className = className;
		this.host = host;
		this.port = port;
	}
	
	public RemoteException(String message, Throwable cause) {
		super(message, cause);
	}

	public String getClassName() {
		return className;
	}

	public String toString() {
		return getClass().getName() + "(" + className + "): " + getMessage();
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

}
