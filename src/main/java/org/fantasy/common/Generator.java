package org.fantasy.common;

public interface Generator<T, P> {

	public T generate(P parameter);

}
