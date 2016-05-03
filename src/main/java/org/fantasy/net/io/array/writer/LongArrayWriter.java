package org.fantasy.net.io.array.writer;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectOutput;

public class LongArrayWriter implements ArrayWriter {

	public void writeArray(Object object, ObjectOutput oout) throws IOException {
		long[] longArray = (long[])object;
		oout.writeInt(longArray.length);
		for(long l : longArray)
			oout.writeLong(l);
	}

}
