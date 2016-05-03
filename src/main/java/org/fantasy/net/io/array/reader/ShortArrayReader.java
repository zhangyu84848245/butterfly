package org.fantasy.net.io.array.reader;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectInput;

public class ShortArrayReader implements ArrayReader<Short> {
	
	public Short[] readArray(ObjectInput oin) throws IOException {
		int length = oin.readInt();
		Short[] shortArray = new Short[length];
		for(int i = 0; i < length; i++)
			shortArray[i] = oin.readShort();
		return shortArray;
	}

}
