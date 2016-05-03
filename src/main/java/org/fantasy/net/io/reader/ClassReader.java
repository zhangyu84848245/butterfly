package org.fantasy.net.io.reader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.fantasy.net.io.unsafe.ObjectInput;
import org.fantasy.util.ClassUtils;

public class ClassReader extends AbstractReader<Class<?>> {

	public static final Map<String, Class<?>> PRIMITIVE_CLASSES = new HashMap<String, Class<?>>();
	
	static {
		PRIMITIVE_CLASSES.put("void", void.class);
		PRIMITIVE_CLASSES.put("boolean", boolean.class);
		PRIMITIVE_CLASSES.put("java.lang.Boolean", Boolean.class);
		PRIMITIVE_CLASSES.put("byte", byte.class);
		PRIMITIVE_CLASSES.put("java.lang.Byte", Byte.class);
		PRIMITIVE_CLASSES.put("char", char.class);
		PRIMITIVE_CLASSES.put("java.lang.Character", Character.class);
		PRIMITIVE_CLASSES.put("short", short.class);
		PRIMITIVE_CLASSES.put("java.lang.Short", Short.class);
		PRIMITIVE_CLASSES.put("int", int.class);
		PRIMITIVE_CLASSES.put("java.lang.Integer", Integer.class);
		PRIMITIVE_CLASSES.put("long", long.class);
		PRIMITIVE_CLASSES.put("java.lang.Long", Long.class);
		PRIMITIVE_CLASSES.put("float", float.class);
		PRIMITIVE_CLASSES.put("java.lang.Float", Float.class);
		PRIMITIVE_CLASSES.put("double", double.class);
		PRIMITIVE_CLASSES.put("java.lang.Double", Double.class);
	}
	
	public Class<?> read(ObjectInput oin) throws IOException, ClassNotFoundException {
		String className = oin.readUTF();
		Class<?> clazz = PRIMITIVE_CLASSES.get(className);
		if(clazz != null)
			return clazz;
		ClassLoader loader = ClassUtils.getClassLoader();
		return Class.forName(className, false, loader);
	}

	
}
