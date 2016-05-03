package org.fantasy.net.proto;

import java.io.Serializable;

public class ConnectResponse implements Serializable, Response {
	private static final long serialVersionUID = 3988992695268131221L;
	private long sessionId;
	private ExceptionResponse exception;

	private ConnectResponse() {

	}

	public long getSessionId() {
		return sessionId;
	}

	public ExceptionResponse getException() {
		return exception;
	}

	public static Builder newBuilder() {
		return Builder.create();
	}

	public long getId() {
		return -1;
	}

	public static final class Builder {
		private long _sessionId;
		private ExceptionResponse _exception;

		private Builder() {
			super();
		}

		public Builder sessionId(long _sessionId) {
			this._sessionId = _sessionId;
			return this;
		}

		
		public Builder exception(ExceptionResponse _exception) {
			this._exception = _exception;
			return this;
		}

		public ConnectResponse build() {
			return buildPartial();
		}

		public ConnectResponse buildPartial() {
			ConnectResponse response = new ConnectResponse();
			response.sessionId = this._sessionId;
			response.exception = this._exception;
			return response;
		}

		public static Builder create() {
			return new Builder();
		}

	}
}
