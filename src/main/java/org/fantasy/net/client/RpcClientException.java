package org.fantasy.net.client;

public class RpcClientException extends RuntimeException {

	private static final long serialVersionUID = 2937488498731255495L;

	public RpcClientException(String message) {
		super(message);
	}
	
	public RpcClientException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public RpcClientException(Throwable cause) {
		super(cause);
	}
}
