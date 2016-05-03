package org.fantasy.net.client;

import java.io.IOException;

public class RpcTimeoutException extends IOException {
	private static final long serialVersionUID = 4331903084071881521L;

	public RpcTimeoutException(String message) {
		super(message);
	}
}
