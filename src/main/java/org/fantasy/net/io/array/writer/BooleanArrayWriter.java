package org.fantasy.net.io.array.writer;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectOutput;

public class BooleanArrayWriter implements ArrayWriter {

	public void writeArray(Object object, ObjectOutput oout) throws IOException {
		boolean[] booleanArray = (boolean[])object;
		oout.writeInt(booleanArray.length);
		for(boolean b : booleanArray)
			oout.writeBoolean(b);
	}
}
