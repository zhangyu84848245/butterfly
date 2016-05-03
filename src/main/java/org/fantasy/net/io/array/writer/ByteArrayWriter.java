package org.fantasy.net.io.array.writer;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectOutput;

public class ByteArrayWriter implements ArrayWriter {

//	private DataOutput dout;
//	
//	public ByteArrayWriter(DataOutput dout) {
//		this.dout = dout;
//	}

	public void writeArray(Object object, ObjectOutput oout) throws IOException {
		byte[] buf = (byte[])object;
		oout.writeInt(buf.length);
		oout.write(buf, 0, buf.length);
	}
	
	
}
