package org.fantasy.net.io.array.reader;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectInput;

public interface ArrayReader<T> {

	public T[] readArray(ObjectInput oin) throws IOException, ClassNotFoundException;
}
