package org.fantasy.net.client;

import java.util.concurrent.atomic.AtomicInteger;

import org.fantasy.net.server.Chooser;

public abstract class AbstractChooser<T> implements Chooser<T> {
	
	private T[] targets;
	private AtomicInteger index = new AtomicInteger();

	public AbstractChooser(T[] targets) {
		this.targets = targets;
	}
	public T next() {
		// 注意低位要是1
		return targets[index.getAndIncrement() & targets.length - 1];
	}
}
