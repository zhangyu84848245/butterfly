package org.fantasy.net.io.writer;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectOutput;

public abstract class AbstractWriter<T> implements Writer<T> {

	public abstract void write(T object, ObjectOutput oout) throws IOException;
}
