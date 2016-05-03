package org.fantasy.net;

public class ChannelException extends RuntimeException {

	private static final long serialVersionUID = 1454796499244559564L;

	public ChannelException() {
		
	}
	
	public ChannelException(String message) {
		super(message);
	}
	
	public ChannelException(Throwable cause) {
		super(cause);
	}
	
	public ChannelException(String message, Throwable cause) {
		super(message, cause);
	}
}
