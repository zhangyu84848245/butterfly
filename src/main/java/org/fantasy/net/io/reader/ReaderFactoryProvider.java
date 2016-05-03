package org.fantasy.net.io.reader;

import org.fantasy.net.io.unsafe.ObjectInput;

public abstract class ReaderFactoryProvider {
	private ReaderFactoryProvider() {
		
	}
	
	public static ReaderFactory createReaderFactory(ObjectInput oin) {
		return new ReaderFactory(oin);
	}
}
