package org.fantasy.net;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;



public interface Future<V> {

	boolean isDone();

	V get() throws InterruptedException;

	V get(long timeout, TimeUnit unit) throws InterruptedException;

	Future<V> await() throws InterruptedException;

	boolean await(long timeout, TimeUnit unit) throws InterruptedException;

	public long getId();
	
	public void setResponse(V response);
	
	boolean hasError();
	
	void setCause(Throwable cause);
}
