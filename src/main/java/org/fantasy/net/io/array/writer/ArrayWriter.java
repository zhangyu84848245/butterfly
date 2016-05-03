package org.fantasy.net.io.array.writer;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectOutput;

public interface ArrayWriter {
	
	public void writeArray(Object object, ObjectOutput oout) throws IOException;
	
}
