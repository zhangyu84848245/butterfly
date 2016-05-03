package org.fantasy.service;

import java.util.EventObject;

public class ServiceStateChangeEvent extends EventObject {
	
	private static final long serialVersionUID = 7118273899002192439L;
	
	private STATE state;

	public ServiceStateChangeEvent(Service service, STATE state) {
		super(service);
		this.state = state;
	}

	public Service getService() {
		return (Service)super.getSource();
	}

	public STATE getState() {
		return state;
	}
	
	
}
