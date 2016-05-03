package org.fantasy.net.client;

import org.apache.log4j.Logger;
import org.fantasy.conf.Configuration;
import org.fantasy.util.Constant;

public class HeartbeatHandler implements Runnable {
	
	private static final Logger LOG = Logger.getLogger(HeartbeatHandler.class);
	private Configuration conf;
	// rpc.heartbeat.interval
	private long heartbeatInterval;
	private volatile long lastExecutionTime;
	private IOHandler ioHandler;
	private Thread thread;
	private boolean running = true;
	
	
	
	public HeartbeatHandler(Configuration conf, IOHandler ioHandler) {
		this.conf = conf;
		lastExecutionTime = System.currentTimeMillis();
		this.heartbeatInterval = conf.getInt("rpc.heartbeat.interval", Constant.DEFAULT_HEARTBEAT_INTERVAL);
		this.ioHandler = ioHandler;
	}


	public void run() {
		while(running) {
			long startTime = System.currentTimeMillis();
			if(startTime - lastExecutionTime >= heartbeatInterval) {
				lastExecutionTime = startTime;
				ioHandler.sendHeartBeat();
			}
			long waitTime = heartbeatInterval - (System.currentTimeMillis() - lastExecutionTime);
			synchronized(this) {
				if(waitTime > 0) {
					try {
						wait(waitTime);
					} catch (InterruptedException e) {
						LOG.error(e.getMessage(), e);
//						Thread.currentThread().interrupt();
					}
				}
			}
		}
	}

	public void setLastExecutionTime(long lastExecutionTime) {
		this.lastExecutionTime = lastExecutionTime;
	}
	
	public void start() {
		if(thread != null && thread.isAlive())
			return;
		thread = new Thread(this, "Heartbeat thread");
		thread.setDaemon(true);
		thread.start();
	}
	
	public void stop() {
		running = false;
		if(thread.isAlive()) {
			thread.interrupt();
		}
		thread = null;
	}
	
}
