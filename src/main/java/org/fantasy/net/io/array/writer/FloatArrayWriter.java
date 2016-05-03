package org.fantasy.net.io.array.writer;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectOutput;

public class FloatArrayWriter implements ArrayWriter {

//	private DataOutput dout;
//	
//	public FloatArrayWriter(DataOutput dout) {
//		this.dout = dout;
//	}
	
	public void writeArray(Object object, ObjectOutput oout) throws IOException {
		float[] floatArray = (float[])object;
		oout.writeInt(floatArray.length);
		for(float f : floatArray)
			oout.writeFloat(f);
	}

}
