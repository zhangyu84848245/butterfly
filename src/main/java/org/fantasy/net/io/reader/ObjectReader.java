package org.fantasy.net.io.reader;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.fantasy.net.io.unsafe.ClassIntrospector;
import org.fantasy.net.io.unsafe.ObjectInput;
import org.fantasy.util.ClassUtils;
import org.fantasy.util.Constant;
import org.fantasy.util.ReflectionUtils;

public class ObjectReader extends AbstractReader<Serializable> {

	private static final Logger LOG = Logger.getLogger(ObjectReader.class);

	public Serializable read(ObjectInput oin) throws IOException, ClassNotFoundException {
		String className = oin.readUTF();
		if(Constant.WRAPPER_CLASSES.get(className) != null) {
			return readWrapperClass(Constant.WRAPPER_CLASSES.get(className).charValue(), oin);
		}
		Class<Serializable> clazz = (Class<Serializable>)resolveClass(className);
		Serializable object = null;
		try {
			object = ReflectionUtils.newSerializableInstance(clazz);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			LOG.error(cause.getMessage());
			throw new RuntimeException(cause);
		}//ReflectionUtils.newInstance(clazz);
		ClassIntrospector ci = ClassIntrospector.introspect(clazz);
		for(; ci != null; ci = ci.getParent()) {
			boolean hasRW = oin.readBoolean();
			if(hasRW)
				ci.invokeReadObject(object, oin);
			else
				ci.readSerializableFields(object, oin);
		}
		return object;
	}
	
	
	private Class<?> resolveClass(String className) {
		return ClassUtils.forName(className);
	}
	
	
	private Serializable readWrapperClass(char sig, ObjectInput oin) throws IOException {
		switch(sig) {
			case 'Z': 
				return new Boolean(oin.readBoolean());
			case 'B':
				return new Byte(oin.readByte());
			case 'C':
				return new Character(oin.readChar());
			case 'S':
				return new Short(oin.readShort());
			case 'I':
				return new Integer(oin.readInt());
			case 'J':
				return new Long(oin.readLong());
			case 'F':
				return new Float(oin.readFloat());
			case 'D':
				return new Double(oin.readDouble());
			default:
				return null;
		}
	}
}
