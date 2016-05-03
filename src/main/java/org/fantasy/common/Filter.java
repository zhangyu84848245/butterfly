package org.fantasy.common;


public interface Filter<T> {
	
	public boolean accept(T t);
}
