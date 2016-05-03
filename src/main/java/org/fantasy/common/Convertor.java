package org.fantasy.common;

public interface Convertor<O, T> {

	public T convert(O origin);

}
