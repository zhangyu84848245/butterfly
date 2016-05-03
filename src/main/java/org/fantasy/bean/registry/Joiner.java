package org.fantasy.bean.registry;

public interface Joiner<T> {

	public Joiner<T> join(Iterable<T> iterable);
	
	public Joiner<T> join(T key);
	
	public Joiner<T> join(T key, boolean useSeparator);
	
	public Joiner<T> join(Joiner<T> joiner);

}
