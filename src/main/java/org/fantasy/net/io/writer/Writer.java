package org.fantasy.net.io.writer;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectOutput;

public interface Writer<T> {

	public void write(T object, ObjectOutput oout) throws IOException;

}
