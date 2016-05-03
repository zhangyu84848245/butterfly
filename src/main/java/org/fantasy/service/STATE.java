package org.fantasy.service;

public enum STATE {

	NOTINITIALIZE(0, "NOTINITIALIZE"),
	
	INITIALIZING(1, "INITIALIZING"),
	
	INITIALIZED(2, "INITIALIZED"),
	
	STARTING(3, "STARTING"),
	
	STARTED(4, "STARTED"),
	
	STOPPING(5, "STOPPING"),
	
	STOPPED(6, "STOPPED");
	
	private int value;
	private String stateName;
	
	private STATE(int value, String stateName) {
		this.value = value;
		this.stateName = stateName;
	}

	public int getValue() {
		return value;
	}

	public String getStateName() {
		return stateName;
	}
}
