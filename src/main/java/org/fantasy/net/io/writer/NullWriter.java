package org.fantasy.net.io.writer;

import java.io.IOException;
import java.io.Serializable;

import org.fantasy.net.io.unsafe.ObjectOutput;
import org.fantasy.util.Constant;

public class NullWriter implements Writer<Serializable> {

	public void write(Serializable object, ObjectOutput oout) throws IOException {
		oout.writeByte(Constant.TYPE_NULL);
	}

	
}
