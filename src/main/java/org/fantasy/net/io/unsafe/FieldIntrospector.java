package org.fantasy.net.io.unsafe;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.fantasy.util.ClassUtils;
import org.fantasy.util.ReflectionUtils;

import sun.misc.Unsafe;

/**
 * 
 * 字段的内省器
 * @author fantasy
 * @date 2016-03-25
 */
public class FieldIntrospector implements Comparable<FieldIntrospector> {
	
	private static final Unsafe UNSAFE;
	
	static {
		try {
			Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
			unsafeField.setAccessible(true);
			UNSAFE = (Unsafe)unsafeField.get(null);
		} catch (Exception e) {
			throw new Error();
		}
	}

	private Field field;
	private String name;
	private String signature;
	private Class<?> type;
	private long offset;
	
	
	public FieldIntrospector(Field field) {
		this.field = field;
		this.name = field.getName();
		this.type = field.getType();
		this.signature = ClassUtils.getClassSignature(type);
		this.offset = UNSAFE.objectFieldOffset(field);
	}
	
	public char getTypeCode() {
		return signature.charAt(0);
	}

	public Class<?> getType() {
		return type;
	}
	
	public boolean isPrimitive() {
		char code = signature.charAt(0);
		return code != 'L' && code != '[';
	}

	public String getName() {
		return name;
	}
	
	public Field getField() {
		return field;
	}
	
	/**
	 * 写入基本类型的值
	 * @param object
	 * @param output
	 * @throws IOException
	 */
	public void writePrimitiveField(Object object, DataOutput output) throws IOException {
		if(object == null)
			throw new NullPointerException();
//		long fieldOffset = this.offset;
		switch(getTypeCode()) {
			case 'Z':
				output.writeBoolean(UNSAFE.getBoolean(object, offset));
				break;
			case 'B':
				output.writeByte(UNSAFE.getByte(object, offset));
				break;
			case 'C':
				output.writeChar(UNSAFE.getChar(object, offset));
				break;
			case 'S':
				output.writeShort(UNSAFE.getChar(object, offset));
				break;
			case 'I':
				output.writeInt(UNSAFE.getInt(object, offset));
				break;
			case 'F':
				output.writeFloat(UNSAFE.getFloat(object, offset));
				break;
			case 'J':
				output.writeLong(UNSAFE.getLong(object, offset));
				break;
			case 'D':
				output.writeDouble(UNSAFE.getDouble(object, offset));
				break;
			default:
				throw new InternalError();
		}
	}
	
	
	public void readPrimitiveField(Object object, DataInput din) throws IOException {
		if(object == null)
			throw new NullPointerException();
		switch(getTypeCode()) {
			case 'Z':
				UNSAFE.putBoolean(object, offset, din.readBoolean());
				break;
			case 'B':
				UNSAFE.putByte(object, offset, din.readByte());
				break;
			case 'C':
				UNSAFE.putChar(object, offset, din.readChar());
				break;
			case 'S':
				UNSAFE.putShort(object, offset, din.readShort());
				break;
			case 'I':
				UNSAFE.putInt(object, offset, din.readInt());
				break;
			case 'F':
				UNSAFE.putFloat(object, offset, din.readFloat());
				break;
			case 'J':
				UNSAFE.putLong(object, offset, din.readLong());
				break;
			case 'D':
				UNSAFE.putDouble(object, offset, din.readDouble());
				break;
			default:
				throw new InternalError();
		}
	}
	
	public void writeObjectField(Object object, ObjectOutput oout) throws IOException {
		if(object == null)
			throw new NullPointerException();
		switch(getTypeCode()) {
			case 'L':
			case '[':
				oout.writeObject(UNSAFE.getObject(object, offset));
		}
	}
	
	public void readObjectField(Object object, ObjectInput oin) throws IOException, ClassNotFoundException {
		if(object == null)
			throw new NullPointerException();
		switch(getTypeCode()) {
			case 'L':
			case '[':
				UNSAFE.putObject(object, offset, oin.readObject());
		}
	}

	
	public int compareTo(FieldIntrospector other) {
		boolean p1 = isPrimitive(), p2 = other.isPrimitive();
		if(p1 != p2)
			return p1 ? -1 : 1;
		return getName().compareTo(other.getName());
	}

}
