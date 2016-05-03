package org.fantasy.net.io.array.writer;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectOutput;

public class ObjectArrayWriter implements ArrayWriter {

	public void writeArray(Object object, ObjectOutput oout) throws IOException {
		Object[] oArray = (Object[])object;
		oout.writeInt(oArray.length);
		for(Object o : oArray)
			oout.writeObject(o);
	}

	
	
}
