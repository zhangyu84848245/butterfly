package org.fantasy.net.io.array.reader;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectInput;

public class ObjectArrayReader implements ArrayReader<Object> {

	public Object[] readArray(ObjectInput oin) throws IOException, ClassNotFoundException {
		int length = oin.readInt();
		Object[] oArray = new Object[length];
		for(int i = 0; i < length; i++)
			oArray[i] = oin.readObject();
		return oArray;
	}

	
	
}
