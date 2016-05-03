package org.fantasy.net.io.array.reader;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectInput;

public class BooleanArrayReader implements ArrayReader<Boolean> {

	
	
	public Boolean[] readArray(ObjectInput oin) throws IOException {
		int length = oin.readInt();
		Boolean[] booleanArray = new Boolean[length];
		for(int i = 0; i < length; i++)
			booleanArray[i] = oin.readBoolean();
		return booleanArray;
	}

}
