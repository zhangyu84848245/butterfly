package org.fantasy.net.io.writer;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectOutput;
import org.fantasy.util.Constant;

public class StringWriter extends AbstractWriter<String> {

//	public final static byte TYPE_STRING = (byte)0x10;

	public void write(String str, ObjectOutput oout) throws IOException {
		oout.writeByte(Constant.TYPE_STRING);
		oout.writeUTF(str);
//		oout.writeByte(Constant.WRITE_END);
	}

}
