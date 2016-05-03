package org.fantasy.net.proto;

import java.io.Serializable;

public class HeartbeatRequest implements Serializable, Request {

	private static final long serialVersionUID = -2470167004299107567L;
	private int value;
	private long clientId;
	private long id;
	
	private HeartbeatRequest() {
		this.value = -1;
	}

	public int getValue() {
		return value;
	}

	public long getClientId() {
		return clientId;
	}

	public long getId() {
		return id;
	}

	public static Builder newBuilder() {
		return Builder.create();
	}
	
	public static final class Builder {
		private long _clientId;
		private long _id;
		private Builder() {
			
		}
		
		public Builder clientId(long _clientId) {
			this._clientId = _clientId;
			return this;
		}
		
		public Builder id(long _id) {
			this._id = _id;
			return this;
		}
		
		public static Builder create() {
			return new Builder();
		}
		
		public HeartbeatRequest build() {
			return buildPartial();
		}

		public HeartbeatRequest buildPartial() {
			HeartbeatRequest heartbeat = new HeartbeatRequest();
			heartbeat.clientId = this._clientId;
			heartbeat.id = this._id;
			return heartbeat;
		}
	}
	
}
