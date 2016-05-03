package org.fantasy.net.io.writer;

import java.io.IOException;

import org.fantasy.net.io.unsafe.ObjectOutput;
import org.fantasy.util.Constant;

public class EnumWriter extends AbstractWriter<Enum> /** implements Writer<Enum> */ {
	
//	public static final byte TYPE_ENUM = (byte)0x12;

	public EnumWriter() {
		
	}

	public void write(Enum e, ObjectOutput oout) throws IOException {
		oout.writeByte(Constant.TYPE_ENUM);
//		ClassIntrospector ci = ClassIntrospector.introspect(e.getClass());
//		oout.writeInt(ci.getClassHierarchy());
//		for(; ci != null; ci = ci.getParent()) {
//			oout.writeUTF(ci.getName());
//			oout.writeUTF(e.name());
//		}
		oout.writeUTF(e.getClass().getName());
		oout.writeUTF(e.name());

	}

}
