package org.fantasy.net.io.unsafe;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.fantasy.net.io.array.writer.ArrayWriterFactory;
import org.fantasy.net.io.writer.WriterFactoryProvider;
import org.fantasy.util.MemoryUtils;
import org.fantasy.util.ReflectionUtils;

public class UnsafeObjectOutputStream extends OutputStream implements ObjectOutput {
	
	private UnsafeOutputStream dout;

	public UnsafeObjectOutputStream() {
		this.dout = new UnsafeOutputStream();
	}

	public UnsafeObjectOutputStream(int initialCapacity, int maxCapacity) {
		this.dout = new UnsafeOutputStream(initialCapacity, maxCapacity);
	}


	public void writeObject(Object object) throws IOException {
		WriterFactoryProvider.createWriterFactory(this).writeObject(object);
	}

	public void write(int b) throws IOException {
		dout.write(b);
	}

	public void write(byte[] b) throws IOException {
		dout.write(b);
	}

	public void write(byte[] buf, int off, int len) throws IOException {
		dout.write(buf, off, len);
	}

	public void writeBoolean(boolean value) throws IOException {
		dout.writeBoolean(value);
	}

	public void writeByte(int value) throws IOException {
		dout.writeByte(value);
	}

	public void writeShort(int value) throws IOException {
		dout.writeShort(value);
	}

	public void writeChar(int value) throws IOException {
		dout.writeChar(value);
	}

	public void writeInt(int value) throws IOException {
		dout.writeInt(value);
	}

	public void writeLong(long value) throws IOException {
		dout.writeLong(value);
	}

	public void writeFloat(float value) throws IOException {
		dout.writeFloat(value);
	}

	public void writeDouble(double value) throws IOException {
		dout.writeDouble(value);
	}

	public void writeBytes(String s) throws IOException {
		dout.writeBytes(s);
	}

	public void writeChars(String s) throws IOException {
		dout.writeChars(s);
	}

	public void writeUTF(String str) throws IOException {
		dout.writeUTF(str);
	}

	public void flush() throws IOException {

	}

	public void close() throws IOException {
		dout.close();
	}

	public void mark() throws IOException {
		dout.mark();
	}

	public int markValue() throws IOException {
		return dout.markValue();
	}

	public int getIndex() {
		return dout.getIndex();
	}

	public void setIndex(int writeIndex) {
		dout.setIndex(writeIndex);
	}

	public ByteBuffer buffer() {
		return dout.getBuffer();
	}

	public void reset() throws IOException {
		dout.reset();
	}

	public void discardMark() throws IOException {
		dout.discardMark();
	}

}
