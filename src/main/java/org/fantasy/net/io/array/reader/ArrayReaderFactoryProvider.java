package org.fantasy.net.io.array.reader;

import org.fantasy.net.io.unsafe.ObjectInput;

public class ArrayReaderFactoryProvider {

	
	private ArrayReaderFactoryProvider() {
		
	}
	
	public static ArrayReaderFactory createFactory(ObjectInput oin) {
		return new ArrayReaderFactory(oin);
	}
}
