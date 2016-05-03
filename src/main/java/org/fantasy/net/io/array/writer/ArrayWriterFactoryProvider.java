package org.fantasy.net.io.array.writer;

import org.fantasy.net.io.unsafe.ObjectOutput;

public class ArrayWriterFactoryProvider {

	
	private ArrayWriterFactoryProvider() {
		
	}
	
	public static ArrayWriterFactory createFactory(ObjectOutput oout) {
		return new ArrayWriterFactory(oout);
	}
}
