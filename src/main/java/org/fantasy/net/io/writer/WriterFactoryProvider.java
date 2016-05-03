package org.fantasy.net.io.writer;

import org.fantasy.net.io.unsafe.ObjectOutput;

public class WriterFactoryProvider {

	private WriterFactoryProvider() {
		
	}
	
	public static WriterFactory createWriterFactory(ObjectOutput oout) {
		return new WriterFactory(oout);
	}
}
