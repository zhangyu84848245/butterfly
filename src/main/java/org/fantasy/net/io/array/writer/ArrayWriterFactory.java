package org.fantasy.net.io.array.writer;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fantasy.net.io.unsafe.ObjectOutput;
import org.fantasy.net.io.writer.Writer;
import org.fantasy.util.ClassUtils;
import org.fantasy.util.Constant;

public class ArrayWriterFactory {

	private ObjectOutput oout;
	private static final ConcurrentMap<String, ArrayWriter> WRITER_FACTORY = new ConcurrentHashMap<String, ArrayWriter>();
	private static final ArrayWriter OBJECT_ARRAY_WRITER;
	static {
		ArrayWriter writer = null;
		// byte[]
		writer = new ByteArrayWriter();
		WRITER_FACTORY.put(ClassUtils.getClassSignature(byte[].class), writer);
		WRITER_FACTORY.put(ClassUtils.getClassSignature(Byte[].class), writer);
		// boolean[]
		writer = new BooleanArrayWriter();
		WRITER_FACTORY.put(ClassUtils.getClassSignature(boolean[].class), writer);
		WRITER_FACTORY.put(ClassUtils.getClassSignature(Boolean[].class), writer);
		// short[]
		writer = new ShortArrayWriter();
		WRITER_FACTORY.put(ClassUtils.getClassSignature(short[].class), writer);
		WRITER_FACTORY.put(ClassUtils.getClassSignature(Short[].class), writer);
		// char[]
		writer = new CharArrayWriter();
		WRITER_FACTORY.put(ClassUtils.getClassSignature(char[].class), writer);
		WRITER_FACTORY.put(ClassUtils.getClassSignature(Character[].class), writer);
		// int[]
		writer = new IntArrayWriter();
		WRITER_FACTORY.put(ClassUtils.getClassSignature(int[].class), writer);
		WRITER_FACTORY.put(ClassUtils.getClassSignature(Integer[].class), writer);
		// float[]
		writer = new FloatArrayWriter();
		WRITER_FACTORY.put(ClassUtils.getClassSignature(float[].class), writer);
		WRITER_FACTORY.put(ClassUtils.getClassSignature(Float[].class), writer);
		// long[]
		writer = new LongArrayWriter();
		WRITER_FACTORY.put(ClassUtils.getClassSignature(long[].class), writer);
		WRITER_FACTORY.put(ClassUtils.getClassSignature(Long[].class), writer);
		// double[]
		writer = new DoubleArrayWriter();
		WRITER_FACTORY.put(ClassUtils.getClassSignature(double[].class), writer);
		WRITER_FACTORY.put(ClassUtils.getClassSignature(Double[].class), writer);
		
		OBJECT_ARRAY_WRITER = new ObjectArrayWriter();
	}
	public ArrayWriterFactory(ObjectOutput oout) {
		this.oout = oout;
	}
	
	public void writeArray(Object object) throws IOException {
		Class<?> clazz = object.getClass();
		String signature = ClassUtils.getClassSignature(clazz);
		ArrayWriter writer = WRITER_FACTORY.get(signature);
		if(writer == null)
			writer = OBJECT_ARRAY_WRITER;
		oout.writeByte(Constant.TYPE_ARRAY);
		oout.writeUTF(signature);
		writer.writeArray(object, oout);
	}
	
}
