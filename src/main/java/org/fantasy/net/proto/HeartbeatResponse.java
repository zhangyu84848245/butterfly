package org.fantasy.net.proto;

import java.io.Serializable;

public class HeartbeatResponse implements Response, Serializable {

	private static final long serialVersionUID = 3408182275121633734L;
	private long sessionId;
	private long id;
	
	private HeartbeatResponse() {
	}
	
	public long getId() {
		return id;
	}

	public long getSessionId() {
		return sessionId;
	}
	
	public static Builder newBuilder() {
		return Builder.create();
	}

	public static final class Builder {
		private long _sessionId;
		private long _id;
		private Builder() {
			
		}

		public Builder sessionId(long _sessionId) {
			this._sessionId = _sessionId;
			return this;
		}
		
		public Builder id(long _id) {
			this._id = _id;
			return this;
		}

		public static Builder create() {
			return new Builder();
		}
		
		public HeartbeatResponse build() {
			return buildPartial();
		}

		public HeartbeatResponse buildPartial() {
			HeartbeatResponse response = new HeartbeatResponse();
			response.sessionId = this._sessionId;
			response.id = this._id;
			return response;
		}
	}
}
