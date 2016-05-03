package org.fantasy.net.io.array.reader;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectInput;

public class FloatArrayReader implements ArrayReader<Float> {

	public Float[] readArray(ObjectInput oin) throws IOException {
		int length = oin.readInt();
		Float[] floatArray = new Float[length];
		for(int i = 0; i < length; i++)
			floatArray[i] = oin.readFloat();
		return floatArray;
	}

}
