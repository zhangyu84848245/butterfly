package org.fantasy.common;

public interface ReferenceCache<K, V> extends Cache<K, V> {

	public void processQueue();

}
