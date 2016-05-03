package org.fantasy.net.client;


public class ConnectionException extends RuntimeException {

	private static final long serialVersionUID = -4545039195935116856L;

	private String host;
	private int port;

	public ConnectionException(Throwable cause, String host, int port, String message) {
		super(message, cause);
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

}
