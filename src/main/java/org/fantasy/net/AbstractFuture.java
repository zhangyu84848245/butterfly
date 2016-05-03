package org.fantasy.net;

import java.util.concurrent.TimeUnit;

import org.fantasy.net.proto.RpcResponse;
import org.fantasy.util.Constant;

public abstract class AbstractFuture<V> implements Future<V> {

	// 可见性
	volatile boolean done = false;
	public V response;
	private short waiters;
	public long startTime;
	public int rpcTimeout;
	private long id;
	private Throwable cause;
	
	public AbstractFuture(long id) {
		this.id = id;
		this.startTime = System.currentTimeMillis();
		this.rpcTimeout = Constant.DEFAULT_CALL_TIMEOUT;
	}
	
	public long getId() {
		return id;
	}
	
	public boolean isDone() {
		return done && response != null;
	}

	public V get() throws InterruptedException {
		await();
		if(isDone()) {
			return response;
		}
		return null;
	}

	public V get(long timeout, TimeUnit unit) throws InterruptedException {
		await(timeout, unit);
		if(isDone()) {
			return response;
		}
		return null;
	}

	public Future<V> await() throws InterruptedException {
		if(isDone()) {
			return this;
		}

		if(Thread.interrupted()) {
			throw new InterruptedException("Thread is interrupted");
		}

		synchronized(this) {
			while(!isDone()) {
				incWaiters();
				try {
					wait();
				} finally {
					decWaiters();
				}
			}
		}
		return this;
	}

	public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
		long timeoutNanos = unit.toNanos(timeout);
		if(isDone()) {
			return true;
		}
		
		if(timeout <= 0) {
			return isDone();
		}
		
		if(Thread.interrupted()) {
			throw new InterruptedException("Thread is interrupted");
		}
		
		long startTime = System.nanoTime();
		boolean interrupted = false;
		
		try {
			
			synchronized(this) {
				if(isDone()) {
					return true;
				}

				if(timeout <= 0) {
					return isDone();
				}
				
				incWaiters();
				try {
					for(;;) {
						try {
							wait( timeoutNanos / 1000000, (int) (timeoutNanos % 1000000) );
						} catch (InterruptedException e) {
							interrupted = true;
						}
						if(isDone()) {
							return true;
						} else {
							timeoutNanos -= (System.nanoTime() - startTime);
							if(timeoutNanos <= 0) {
								return isDone();
							}
						}
					}
				} finally {
					decWaiters();
				}
			}
			
		} finally {
			if(interrupted) {
				Thread.currentThread().interrupt();
			}
		}
	}

	
	private void incWaiters() {
        if ( waiters == Short.MAX_VALUE ) {
            throw new IllegalStateException("too many waiters: " + this);
        }
        waiters ++;
    }

    private void decWaiters() {
        waiters --;
    }
	
    
    public void setComplete() {
		synchronized(this) {
			this.done = true;
			if(waiters > 0) {
				notifyAll();
			}
		}
	}
    
    
    public void setResponse(V response) {
		this.response = response;
		setComplete();
	}

    public void setCause(Throwable cause) {
		this.cause = cause;
	}

	public boolean hasError() {
		return cause != null;
	}

	public Throwable getCause() {
		return cause;
	}

}
