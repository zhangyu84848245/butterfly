package org.fantasy.bean.registry.lb;



public class ServerName {

	private String address;
	private int port;
//	private String hostName;
	
	public ServerName(String address, int port) {
		this.address = address;
		this.port = port;
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

//	public String getHostName() {
//		try {
//			return InetAddress.getByName(address).getHostName();
//		} catch (UnknownHostException e) {
//			return null;
//		}
//	}

	public int hashCode() {
		return address.hashCode() * 29 + port * 31;
	}


	public boolean equals(Object other) {
		if(this == other)
			return false;
		
		if(!(other instanceof ServerName))
			return false;
		
		ServerName that = (ServerName)other;
		return getAddress().equals(that.getAddress()) && getPort() == that.getPort();
//				( getAddress() != null && getAddress().equals(that.getAddress()) && getPort() == that.getPort() ) ||
//				( getAddress() == null && that.getAddress() == null && getPort() == that.getPort() );
	}


	public String toString() {
		return address + ":" + port;
	}

	
	
}
