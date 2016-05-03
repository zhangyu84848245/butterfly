package org.fantasy.net.io.reader;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectInput;

public class EnumReader extends AbstractReader<Enum> {

	public Enum read(ObjectInput oin) throws IOException, ClassNotFoundException {
		String enumType = oin.readUTF();
		String name = oin.readUTF();
		return Enum.valueOf((Class<Enum>)Class.forName(enumType), name);
	}

}
