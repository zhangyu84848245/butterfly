package org.fantasy.common;

import java.util.concurrent.TimeUnit;

public class RetryCounter {
	
	private int maxAttempts;
	private long sleepInterval;
	private TimeUnit timeUnit;
	private int attempts;
	
	public RetryCounter(int maxAttempts, long sleepInterval, TimeUnit timeUnit) {
		this.maxAttempts = maxAttempts;
		this.sleepInterval = sleepInterval;
		this.timeUnit = timeUnit;
		attempts = 0;
	}
	
	public boolean shouldRetry() {
		return attempts < maxAttempts;
	}
	
	
	public void doRetry() {
		try {
			timeUnit.sleep(sleepInterval);
		} catch (InterruptedException e) {
			// ignore
		}
		attempts++;
	}
}
