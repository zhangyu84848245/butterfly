package org.fantasy.net.server;

public interface SocketConf {

	public boolean isReuseAddress();
	
	public void setReuseAddress(boolean reuseAddress);
	
	public void setBacklog(int backlog);
	
	public int getBacklog();
	
}
