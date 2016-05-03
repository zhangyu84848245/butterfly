package org.fantasy.net.io.array.writer;

import java.io.DataOutput;
import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectOutput;

public class ShortArrayWriter implements ArrayWriter {

//	private DataOutput dout;
//	
//	public ShortArrayWriter(DataOutput dout) {
//		this.dout = dout;
//	}
	
	public void writeArray(Object object, ObjectOutput oout) throws IOException {
		short[] shortArray = (short[])object;
		oout.writeInt(shortArray.length);
		for(short s : shortArray) 
			oout.writeShort(s);
	}

	
}
