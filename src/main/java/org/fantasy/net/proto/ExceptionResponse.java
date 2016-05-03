package org.fantasy.net.proto;

import java.io.Serializable;

public class ExceptionResponse implements Serializable {

	private static final long serialVersionUID = 8395675506233143935L;
	private String message;
	private String exceptionClassName;
	private String host;
	private int port;

	
	private ExceptionResponse() {
	}

	public String getMessage() {
		return message;
	}

	public String getExceptionClassName() {
		return exceptionClassName;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public static Builder newBuilder() {
		return Builder.create();
	}

	public static final class Builder {

		private String _message;
		private String _exceptionClassName;
		private String _host;
		private int _port;

		private Builder() {
			
		}

		public Builder message(String _message) {
			this._message = _message;
			return this;
		}

		public Builder exceptionClassName(String _exceptionClassName) {
			this._exceptionClassName = _exceptionClassName;
			return this;
		}

		public Builder host(String _host) {
			this._host = _host;
			return this;
		}

		public Builder port(int _port) {
			this._port = _port;
			return this;
		}

		public static Builder create() {
			return new Builder();
		}

		public ExceptionResponse build() {
			return buildPartial();
		}

		public ExceptionResponse buildPartial() {
			ExceptionResponse response = new ExceptionResponse();
			response.exceptionClassName = this._exceptionClassName;
			response.message = this._message;
			response.host = this._host;
			response.port = this._port;
			return response;
		}
	}
}
