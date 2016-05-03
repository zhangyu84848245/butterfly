package org.fantasy.net.io.unsafe;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.fantasy.net.io.reader.ReaderFactoryProvider;
import org.fantasy.util.Constant;

public class UnsafeObjectInputStream implements ObjectInput {
	
	private DataInput din; 
	
	public UnsafeObjectInputStream(ByteBuffer buffer) {
		this.din = new UnsafeInputStream(buffer);
	}
	
	public Object readObject() throws ClassNotFoundException, IOException {
		return ReaderFactoryProvider.createReaderFactory(this).readObject();
	}

	public void readFully(byte[] b) throws IOException {
		din.readFully(b);
	}

	public void readFully(byte[] b, int offset, int length) throws IOException {
		din.readFully(b, offset, length);
	}

	public int skipBytes(int n) throws IOException {
		return din.skipBytes(n);
	}

	public boolean readBoolean() throws IOException {
		return din.readBoolean();
	}

	public byte readByte() throws IOException {
		return din.readByte();
	}

	public int readUnsignedByte() throws IOException {
		return din.readUnsignedByte();
	}

	public short readShort() throws IOException {
		return din.readShort();
	}

	public int readUnsignedShort() throws IOException {
		return din.readUnsignedShort();
	}

	public char readChar() throws IOException {
		return din.readChar();
	}

	public int readInt() throws IOException {
		return din.readInt();
	}

	public long readLong() throws IOException {
		return din.readLong();
	}

	public float readFloat() throws IOException {
		return din.readFloat();
	}

	public double readDouble() throws IOException {
		return din.readDouble();
	}

	public String readLine() throws IOException {
		return din.readLine();
	}

	public String readUTF() throws IOException {
		return din.readUTF();
	}

	public int available() throws IOException {
		return ((UnsafeInputStream)din).available();
	}

	public void close() throws IOException {
		((UnsafeInputStream)din).close();
	}

	public int getIndex() {
		return ((UnsafeInputStream)din).getIndex();
	}

	public void setIndex(int index) {
		((UnsafeInputStream)din).setIndex(index);
	}

	
}
