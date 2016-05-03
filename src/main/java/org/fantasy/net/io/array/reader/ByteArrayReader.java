package org.fantasy.net.io.array.reader;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectInput;

public class ByteArrayReader implements ArrayReader<Byte> {

	public Byte[] readArray(ObjectInput oin) throws IOException {
		int length = oin.readInt();
		Byte[] byteArray = new Byte[length];
		for(int i = 0; i < length; i++)
			byteArray[i] = oin.readByte();
		return byteArray;
	}

}
