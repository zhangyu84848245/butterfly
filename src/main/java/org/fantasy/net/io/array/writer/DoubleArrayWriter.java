package org.fantasy.net.io.array.writer;

import java.io.DataOutput;
import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectOutput;

public class DoubleArrayWriter implements ArrayWriter {

//	private DataOutput dout;
//	
//	public DoubleArrayWriter(DataOutput dout) {
//		this.dout = dout;
//	}

	public void writeArray(Object object, ObjectOutput oout) throws IOException {
		double[] doubleArray = (double[])object;
		oout.writeInt(doubleArray.length);
		for(double d : doubleArray)
			oout.writeDouble(d);
	}
	
	
	
}
