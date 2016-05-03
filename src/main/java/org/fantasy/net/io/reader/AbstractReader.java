package org.fantasy.net.io.reader;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectInput;

public abstract class AbstractReader<T> implements Reader<T> {

	public abstract T read(ObjectInput oin) throws IOException, ClassNotFoundException;
}
