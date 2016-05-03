package org.fantasy.service;

import org.fantasy.conf.Configurable;
import org.fantasy.conf.Configuration;


public abstract class AbstractService implements Service, Configurable {

	private Configuration conf;
	private volatile STATE state = STATE.NOTINITIALIZE;
	public ServiceStateChangeSupport support = new ServiceStateChangeSupport(this);
	private Object objectLock = new Object();
	
	public void setConfig(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return conf;
	}

	private boolean isInState(STATE state) {
		return this.state == state;
	}
	
	public STATE enterState(STATE newState) {
		STATE oldState = this.state;
		this.state = newState;
		support.fireStateChangeListener(this, newState);
		return oldState;
	}

	public void initialize() {
		if(isInState(STATE.INITIALIZED)) {
			return;
		}
		if(isInState(STATE.NOTINITIALIZE)) {
			enterState(STATE.INITIALIZING);
			serviceInit();
			enterState(STATE.INITIALIZED);
		}
	}
	
	public void serviceInit() {
		
	}

	public void start() {
		if(isInState(STATE.STARTED)) {
			return;
		}
		synchronized(objectLock) {
			if(isInState(STATE.NOTINITIALIZE))
				initialize();
			if(isInState(STATE.INITIALIZED)) {
				enterState(STATE.STARTING);
				serviceStart();
				enterState(STATE.STARTED);
			}
		}
	}

	public void serviceStart() {

	}
	
	public void stop() {
		if(isInState(STATE.STOPPED)) {
			return;
		}
		synchronized(objectLock) {
			if(isInState(STATE.STARTED)) {
				enterState(STATE.STOPPING);
				serviceStop();
				enterState(STATE.STOPPED);
			}
		}
	}

	public void serviceStop() {
		
	}

}
