package org.fantasy.net.server;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Locale;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

	private static final AtomicInteger THREAD_POOL_NUMBER = new AtomicInteger(1);
	private final ThreadGroup threadGroup;
	private final AtomicInteger threadNumber = new AtomicInteger(1);
	private static final String NAME_PATTERN = "pool-%d-thread-";// "pool-N-thread-"
	private final String threadNamePrefix;
	private UncaughtExceptionHandler uncaughtExceptionHandler;
	
	public NamedThreadFactory() {
		final SecurityManager s = System.getSecurityManager();
		this.threadGroup = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
		this.threadNamePrefix = String.format(Locale.ROOT, NAME_PATTERN, THREAD_POOL_NUMBER.getAndIncrement());
	}

	public Thread newThread(Runnable task) {
		Thread t = new Thread(threadGroup, task, threadNamePrefix + threadNumber.getAndIncrement(), 0);
		
		if(t.isDaemon()) {
			t.setDaemon(false);
		}
		
		if(t.getPriority() != Thread.NORM_PRIORITY) {
			t.setPriority(Thread.NORM_PRIORITY);
		}
		
		if(uncaughtExceptionHandler != null)
			t.setUncaughtExceptionHandler(uncaughtExceptionHandler);
		return t;
	}
	
	
}
