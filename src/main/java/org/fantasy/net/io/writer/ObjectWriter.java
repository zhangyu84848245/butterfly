package org.fantasy.net.io.writer;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.fantasy.net.io.unsafe.ClassIntrospector;
import org.fantasy.net.io.unsafe.ObjectOutput;
import org.fantasy.util.Constant;

public class ObjectWriter extends AbstractWriter<Serializable> /** implements Writer<Serializable> */ {

	
	
	public void write(Serializable object, ObjectOutput oout) throws IOException {
		oout.writeByte(Constant.TYPE_OBJECT);
		Class<?> clazz = object.getClass();
		String className = clazz.getName();
		oout.writeUTF(className);
		if(Constant.WRAPPER_CLASSES.get(className) != null) {
			writeWrapperClass(object,Constant.WRAPPER_CLASSES.get(className).charValue(), oout);
			return;
		}
		ClassIntrospector ci = ClassIntrospector.introspect(clazz);
		for(; ci != null; ci = ci.getParent()) {
			boolean hasRW = ci.hasSerializableMethod();
			oout.writeBoolean(hasRW);
			if(hasRW)
				ci.invokeWriteObject(object, oout);
			else
				ci.writeSerializableFields(object, oout);
		}
	}
	
	private void writeWrapperClass(Serializable object, char sig, ObjectOutput oout) throws IOException {
		switch(sig) {
			case 'Z': 
				oout.writeBoolean(((Boolean)object).booleanValue());
				break;
			case 'B':
				oout.writeByte(((Byte)object).byteValue());
				break;
			case 'C':
				oout.writeChar(((Character)object).charValue());
				break;
			case 'S':
				oout.writeShort(((Short)object).shortValue());
				break;
			case 'I':
				oout.writeInt(((Integer)object).intValue());
				break;
			case 'J':
				oout.writeLong(((Long)object).longValue());
				break;
			case 'F':
				oout.writeFloat(((Float)object).floatValue());
				break;
			case 'D':
				oout.writeDouble(((Double)object).doubleValue());
				break;
			default:
		}
	}

}
