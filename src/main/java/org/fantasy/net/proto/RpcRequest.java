package org.fantasy.net.proto;

import java.io.Serializable;
import java.util.Arrays;

import org.fantasy.common.MethodDescriptor;

public class RpcRequest implements Serializable, Request {

	private static final long serialVersionUID = 571553398991748473L;
	private MethodDescriptor md;
	private Object[] arguments;
	private long id = -1;
	private Throwable error;
//	private byte[] buf;
	
	private RpcRequest() {
//		buf = new byte[440000];
//		Arrays.fill(buf, (byte)1);
	}

	public MethodDescriptor getMethodDescriptor() {
		return md;
	}

	public Object[] getArguments() {
		return arguments;
	}

	public long getId() {
		return id;
	}
	
	public Throwable getError() {
		return error;
	}

	public static Builder newBuilder() {
		return Builder.create();
	}

	public static final class Builder {
		private MethodDescriptor _md;
		private Object[] _arguments;
		private long _id;
		private Throwable _error;
		
		private Builder() {
		}
		
		public Builder error(Throwable _error) {
			this._error = _error;
			return this;
		}
		
		public Builder methodDescriptor(MethodDescriptor _md) {
			this._md = _md;
			return this;
		}

		public Builder arguments(Object[] _arguments) {
			this._arguments = _arguments;
			return this;
		}

		public Builder id(long _id) {
			this._id = _id;
			return this;
		}

		public static Builder create() {
			return new Builder();
		}

		public RpcRequest build() {
			return buildPartial();
		}

		private RpcRequest buildPartial() {
			RpcRequest request = new RpcRequest();
			request.id = _id;
			request.md = _md;
			request.arguments = _arguments;
			request.error = _error;
			return request;
		}

	}
}
