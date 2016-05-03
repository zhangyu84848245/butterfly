package org.fantasy.net.io.writer;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectOutput;
import org.fantasy.util.Constant;

public class ClassWriter extends AbstractWriter<Class<?>> {

	
	public void write(Class<?> clazz, ObjectOutput oout) throws IOException {
		oout.writeByte(Constant.TYPE_CLASS);
		oout.writeUTF(clazz.getName());
	}

}
