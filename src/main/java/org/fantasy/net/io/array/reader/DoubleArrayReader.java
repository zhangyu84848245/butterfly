package org.fantasy.net.io.array.reader;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectInput;

public class DoubleArrayReader implements ArrayReader<Double> {

	public Double[] readArray(ObjectInput oin) throws IOException {
		int length = oin.readInt();
		Double[] doubleArray = new Double[length];
		for(int i = 0; i < length; i++)
			doubleArray[i] = oin.readDouble();
		return doubleArray;
	}


	
	
	
	
}
