package org.fantasy.net.proto;

import java.io.Serializable;

import org.fantasy.net.proto.ConnectRequest.Builder;

public class PacketHeader implements Serializable {

	private static final long serialVersionUID = -4937826681302524789L;

	private long id;
	private int dataLength;
	
	public long getId() {
		return id;
	}
	public int getDataLength() {
		return dataLength;
	}
	
	public static Builder newBuilder() {
		return Builder.create();
	}

	public static final class Builder {
		private long _id;
		private int _dataLength;
		
		private Builder() {
		}
		
		public Builder id(long _id) {
			this._id = _id;
			return this;
		}
		
		public Builder dataLength(int _dataLength) {
			this._dataLength = _dataLength;
			return this;
		}

		public static Builder create() {
			return new Builder();
		}

		public PacketHeader build() {
			return buildPartial();
		}

		private PacketHeader buildPartial() {
			PacketHeader header = new PacketHeader();
			header.id = this._id;
			header.dataLength = this._dataLength;
			return header;
		}

	}
}
