package org.fantasy.net.io.reader;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectInput;

public interface Reader<T> {

	public T read(ObjectInput oin) throws IOException, ClassNotFoundException;

}
