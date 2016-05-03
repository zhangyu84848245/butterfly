package org.fantasy.net.client;

public class ClientPingException extends RuntimeException {

	private static final long serialVersionUID = 5144872746195226804L;

	public ClientPingException() {
		super();
	}
	
	public ClientPingException(String message) {
		super(message);
	}
}
