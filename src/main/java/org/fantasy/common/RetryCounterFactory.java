package org.fantasy.common;

import java.util.concurrent.TimeUnit;

public class RetryCounterFactory {

	private int maxAttempts;
	private long sleepInterval;
	public RetryCounterFactory(int maxAttempts, long sleepInterval) {
		this.maxAttempts = maxAttempts;
		this.sleepInterval = sleepInterval;
	}
	
	public RetryCounter create() {
		return new RetryCounter(maxAttempts, sleepInterval, TimeUnit.MILLISECONDS);
	}
	
	
}
