package org.fantasy.net.io.reader;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fantasy.net.io.array.reader.ArrayReaderFactoryProvider;
import org.fantasy.net.io.unsafe.ObjectInput;
import org.fantasy.util.Constant;

public class ReaderFactory {

	private static final ConcurrentMap<Byte, Reader> READER_FACTORY = new ConcurrentHashMap<Byte, Reader>();

	static {
		Reader reader = null;
		// String
		reader = new StringReader();
		READER_FACTORY.put(Constant.TYPE_STRING, reader);
		// Enum
		reader = new EnumReader();
		READER_FACTORY.put(Constant.TYPE_ENUM, reader);
		// Collection
		reader = new CollectionReader();
		READER_FACTORY.put(Constant.TYPE_COLLECTION, reader);
		// Map
		reader = new MapReader();
		READER_FACTORY.put(Constant.TYPE_MAP, reader);
		// Class
		reader = new ClassReader();
		READER_FACTORY.put(Constant.TYPE_CLASS, reader);
		// Date
		reader = new DateReader();
		READER_FACTORY.put(Constant.TYPE_DATE, reader);
		// Object
		reader = new ObjectReader();
		READER_FACTORY.put(Constant.TYPE_OBJECT, reader);
		// null
		reader = new NullReader();
		READER_FACTORY.put(Constant.TYPE_NULL, reader);
	}
	
	private ObjectInput oin;
	
	public ReaderFactory(ObjectInput oin) {
		this.oin = oin;
	}
	

	public final Object readObject() throws ClassNotFoundException, IOException {
		byte flag = oin.readByte();
		if(flag == Constant.TYPE_ARRAY)
			return ArrayReaderFactoryProvider.createFactory(oin).readArray();
		Reader reader = READER_FACTORY.get(flag);
		if(reader == null)
			throw new IOException("Unable to deserialize");
		return reader.read(oin);
	}
}
