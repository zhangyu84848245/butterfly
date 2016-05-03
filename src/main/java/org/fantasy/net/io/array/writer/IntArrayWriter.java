package org.fantasy.net.io.array.writer;

import java.io.DataOutput;
import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectOutput;

public class IntArrayWriter implements ArrayWriter {

//	private DataOutput dout;
//	
//	public IntArrayWriter(DataOutput dout) {
//		this.dout = dout;
//	}

	public void writeArray(Object object, ObjectOutput oout) throws IOException {
		int[] intArray = (int[])object;
		oout.writeInt(intArray.length);
		for(int i = 0; i < intArray.length; i++)
			oout.writeInt(intArray[i]);
	}

}
