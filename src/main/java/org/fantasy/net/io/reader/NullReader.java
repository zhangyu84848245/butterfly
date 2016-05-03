package org.fantasy.net.io.reader;

import java.io.IOException;
import java.io.Serializable;

import org.fantasy.net.io.unsafe.ObjectInput;

public class NullReader implements Reader<Serializable>{

	public Serializable read(ObjectInput oin) throws IOException, ClassNotFoundException {
		return null;
	}

	
	
}
