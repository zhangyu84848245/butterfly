package org.fantasy.net.io.reader;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectInput;

public class StringReader extends AbstractReader<String> {

	public String read(ObjectInput oin) throws IOException, ClassNotFoundException {
		return oin.readUTF();
	}
	
}
