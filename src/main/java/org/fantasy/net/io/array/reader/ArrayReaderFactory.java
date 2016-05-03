package org.fantasy.net.io.array.reader;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;






import org.fantasy.net.io.unsafe.ObjectInput;
import org.fantasy.net.io.unsafe.ObjectOutput;
import org.fantasy.net.io.writer.Writer;
import org.fantasy.util.ClassUtils;

public class ArrayReaderFactory {

	private ObjectInput oin;
	private static final ConcurrentMap<String, ArrayReader> READER_FACTORY = new ConcurrentHashMap<String, ArrayReader>();
	private static final ArrayReader OBJECT_ARRAY_READER;
	static {
		ArrayReader reader = null;
		// byte[]
		reader = new ByteArrayReader();
		READER_FACTORY.put(ClassUtils.getClassSignature(byte[].class), reader);
		READER_FACTORY.put(ClassUtils.getClassSignature(Byte[].class), reader);
		// boolean[]
		reader = new BooleanArrayReader();
		READER_FACTORY.put(ClassUtils.getClassSignature(boolean[].class), reader);
		READER_FACTORY.put(ClassUtils.getClassSignature(Boolean[].class), reader);
		// short[]
		reader = new ShortArrayReader();
		READER_FACTORY.put(ClassUtils.getClassSignature(short[].class), reader);
		READER_FACTORY.put(ClassUtils.getClassSignature(Short[].class), reader);
		// char[]
		reader = new CharArrayReader();
		READER_FACTORY.put(ClassUtils.getClassSignature(char[].class), reader);
		READER_FACTORY.put(ClassUtils.getClassSignature(Character[].class), reader);
		// int[]
		reader = new IntArrayReader();
		READER_FACTORY.put(ClassUtils.getClassSignature(int[].class), reader);
		READER_FACTORY.put(ClassUtils.getClassSignature(Integer[].class), reader);
		// float[]
		reader = new FloatArrayReader();
		READER_FACTORY.put(ClassUtils.getClassSignature(float[].class), reader);
		READER_FACTORY.put(ClassUtils.getClassSignature(Float[].class), reader);
		// long[]
		reader = new LongArrayReader();
		READER_FACTORY.put(ClassUtils.getClassSignature(long[].class), reader);
		READER_FACTORY.put(ClassUtils.getClassSignature(Long[].class), reader);
		// double[]
		reader = new DoubleArrayReader();
		READER_FACTORY.put(ClassUtils.getClassSignature(double[].class), reader);
		READER_FACTORY.put(ClassUtils.getClassSignature(Double[].class), reader);
		
		OBJECT_ARRAY_READER = new ObjectArrayReader();
	}
	public ArrayReaderFactory(ObjectInput oin) {
		this.oin = oin;
	}
	
	public <T> T[] readArray() throws IOException, ClassNotFoundException {
		String signature = oin.readUTF();
		ArrayReader<T> reader = READER_FACTORY.get(signature);
		if(reader == null)
			reader = OBJECT_ARRAY_READER;
		return reader.readArray(oin);
	}

}
