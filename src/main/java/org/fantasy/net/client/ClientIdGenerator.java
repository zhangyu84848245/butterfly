package org.fantasy.net.client;

import java.util.concurrent.atomic.AtomicLong;

import org.fantasy.common.Generator;

public class ClientIdGenerator implements Generator<Long, Integer> {

	private static final AtomicLong SEQUENCE = new AtomicLong(1);
	private static final long UNIQUE;
	private static final Object LOCK = new Object();
	static {
		UNIQUE = System.currentTimeMillis();
	}

	public ClientIdGenerator() {
	}
	
	public Long generate(Integer delta) {
		synchronized(LOCK) {
			return UNIQUE + SEQUENCE.getAndIncrement();
		}
	}

}

