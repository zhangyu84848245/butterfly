package org.fantasy.net.io.writer;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fantasy.net.io.array.writer.ArrayWriterFactoryProvider;
import org.fantasy.net.io.reader.DateWriter;
import org.fantasy.net.io.unsafe.ObjectInput;
import org.fantasy.net.io.unsafe.ObjectOutput;
import org.fantasy.util.Constant;
//@SuppressWarnings("unchecked")
public class WriterFactory {

	private final ObjectOutput oout;
	private static final ConcurrentMap<Class<?>, Writer> WRITER_FACTORY = new ConcurrentHashMap<Class<?>, Writer>();
	private static final Writer OBJECT_WRITER;
	private static final List<Class<?>> CLASSES = new ArrayList<Class<?>>();
	
	static {
		Writer writer = null;
		Class<?> clazz = null;
		// String
		writer = new StringWriter();
		clazz = String.class;
		CLASSES.add(clazz);
		WRITER_FACTORY.put(clazz, writer);
		// Enum
		writer = new EnumWriter();
		clazz = Enum.class;
		CLASSES.add(clazz);
		WRITER_FACTORY.put(clazz, writer);
		// Collection
		writer = new CollectionWriter();
		clazz = Collection.class;
		CLASSES.add(clazz);
		WRITER_FACTORY.put(clazz, writer);
		// Map
		writer = new MapWriter();
		clazz = Map.class;
		CLASSES.add(clazz);
		WRITER_FACTORY.put(clazz, writer);
		// Class
		writer = new ClassWriter();
		clazz = Class.class;
		CLASSES.add(clazz);
		WRITER_FACTORY.put(clazz, writer);
		// Date
		writer = new DateWriter();
		clazz = Date.class;
		CLASSES.add(clazz);
		WRITER_FACTORY.put(clazz, writer);
		// Serializable
		OBJECT_WRITER = new ObjectWriter();
		clazz = Serializable.class;
		CLASSES.add(clazz);
		WRITER_FACTORY.put(clazz, OBJECT_WRITER);
	}
	
	public WriterFactory(ObjectOutput oout) {
		this.oout = oout;
	}
	
	public final void writeObject(Object object) throws IOException {
		if(object == null) {
			oout.writeByte(Constant.TYPE_NULL);
			return;
		}
		if(object.getClass().isArray()) {
			ArrayWriterFactoryProvider.createFactory(oout).writeArray(object);
			return;
		}
		Class<?> clazz = getInstanceType(object);
		if(clazz == null)
			throw new InternalError("Unable to serialize " + object.getClass().getName());
		WRITER_FACTORY.get(clazz).write(object, oout);
	}
	
	
	private Class<?> getInstanceType(Object object) {
		List<Class<?>> list = CLASSES;
		for(Iterator<Class<?>> iterator = list.iterator();iterator.hasNext();) {
			Class<?> clazz = iterator.next();
			if(clazz.isInstance(object))
				return clazz;
		}
		return null;
	}
	
	public static void close() {
		WRITER_FACTORY.clear();
		CLASSES.clear();
	}

}
