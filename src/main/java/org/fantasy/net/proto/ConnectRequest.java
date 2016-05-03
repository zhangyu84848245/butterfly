package org.fantasy.net.proto;

import java.io.Serializable;
import java.util.Arrays;

public class ConnectRequest implements Serializable, Request {

	private static final long serialVersionUID = -112530851792242480L;
	
	private String magic;
	private String version;
	private long clientId;

	private ConnectRequest() {
	}

	public String getMagic() {
		return magic;
	}

	public String getVersion() {
		return version;
	}

	public static Builder newBuilder() {
		return Builder.create();
	}

	public long getClientId() {
		return clientId;
	}

	public long getId() {
		return -1;
	}

	public static final class Builder {

		private String _magic;
		private String _version;
		private long _clientId;
		
		private Builder() {
			
		}
		
		public Builder magic(String _magic) {
			this._magic = _magic;
			return this;
		}
		
		public Builder version(String _version) {
			this._version = _version;
			return this;
		}
		
		public Builder clientId(long _clientId) {
			this._clientId = _clientId;
			return this;
		}
		
		public static Builder create() {
			return new Builder();
		}
		
		public ConnectRequest build() {
			return buildPartial();
		}

		public ConnectRequest buildPartial() {
			ConnectRequest header = new ConnectRequest();
			header.magic = this._magic;
			header.version = this._version;
			header.clientId = this._clientId;
			return header;
		}
	}

}
