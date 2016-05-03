package org.fantasy.net.io.array.writer;

import java.io.DataOutput;
import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectOutput;

public class CharArrayWriter implements ArrayWriter {
	
//	private DataOutput dout;
//	
//	public CharArrayWriter(DataOutput dout) {
//		this.dout = dout;
//	}

	public void writeArray(Object object, ObjectOutput oout) throws IOException {
		char[] charArray = (char[])object;
		oout.writeInt(charArray.length);
		for(char c : charArray)
			oout.writeChar(c);
	}

	
}
