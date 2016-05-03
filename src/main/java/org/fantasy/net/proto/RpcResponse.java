package org.fantasy.net.proto;

import java.io.Serializable;
import java.util.Arrays;

public class RpcResponse implements Serializable, Response {

	private static final long serialVersionUID = 6888774744099368082L;
	private Object result;
	private long id = -1;
	private ExceptionResponse error;
//	private byte[] buf;
	private RpcResponse() {
//		buf = new byte[440000];
//		Arrays.fill(buf, (byte)1);
	}

	public Object getResult() {
		return result;
	}

	public long getId() {
		return id;
	}

	public ExceptionResponse getError() {
		return error;
	}

	public void setError(ExceptionResponse error) {
		this.error = error;
	}

	public boolean hasException() {
		return error != null;
	}

	public static Builder newBuilder() {
		return Builder.create();
	}

	public static final class Builder {

		private Object _result;
		private long _id;
		private ExceptionResponse _error;

		private Builder() {
			
		}

		public Builder requestId(long _id) {
			this._id = _id;
			return this;
		}

		public Builder result(Object _result) {
			this._result = _result;
			return this;
		}

		public Builder error(ExceptionResponse _error) {
			this._error = _error;
			return this;
		}

		public static Builder create() {
			return new Builder();
		}

		private RpcResponse buildPartial() {
			RpcResponse response = new RpcResponse();
			response.error = this._error;
			response.id = this._id;
			response.result = this._result;
			return response;
		}

		public RpcResponse build() {
			return buildPartial();
		}

	}

}
