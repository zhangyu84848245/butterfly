package org.fantasy.net.io.array.reader;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectInput;

public class IntArrayReader implements ArrayReader<Integer> {

	public Integer[] readArray(ObjectInput oin) throws IOException {
		int length = oin.readInt();
		Integer[] intArray = new Integer[length];
		for(int i = 0; i < length; i++)
			intArray[i] = oin.readInt();
		return intArray;
	}

	
	
}
