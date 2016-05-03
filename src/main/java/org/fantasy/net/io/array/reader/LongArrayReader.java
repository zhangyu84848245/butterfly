package org.fantasy.net.io.array.reader;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectInput;

public class LongArrayReader implements ArrayReader<Long> {

	public Long[] readArray(ObjectInput oin) throws IOException {
		int length = oin.readInt();
		Long[] longArray = new Long[length];
		for(int i = 0; i < length; i++)
			longArray[i] = oin.readLong();
		return longArray;
	}

}
