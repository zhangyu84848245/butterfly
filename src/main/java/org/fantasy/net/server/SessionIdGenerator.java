package org.fantasy.net.server;

import java.util.concurrent.atomic.AtomicLong;

import org.fantasy.common.Generator;

public class SessionIdGenerator implements Generator<Long, Integer> {

	private static final AtomicLong SESSION_ID;
	static {
		long sessionId = 0;
		sessionId = ( System.currentTimeMillis() << 24 ) >>> 8;
		sessionId =  sessionId | (1 << 56);

		SESSION_ID = new AtomicLong(sessionId);
	}

	public Long generate(Integer delta) {
		return SESSION_ID.getAndIncrement();
	}

}
