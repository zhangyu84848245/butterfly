package org.fantasy.net.io.unsafe;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.fantasy.util.MemoryUtils;

public class UnsafeInputStream extends InputStream implements DataInput, IndexAware {
	
	private static final boolean NATIVE_ORDER = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
	private ByteBuffer buffer;
	private int readIndex;
	private long memoryAddress;
	private int capacity;
	protected int mark = 0;
	
	public UnsafeInputStream(ByteBuffer buffer) {
		if(!buffer.isDirect())
			throw new IllegalArgumentException("Buffer must be direct memory.");
		this.buffer = buffer;
		this.memoryAddress = MemoryUtils.directBufferAddress(buffer);
		this.readIndex = 0;
		this.capacity = buffer.capacity();
	}
	
	public final int read() throws IOException {
		checkIndex(readIndex + 1);
		return MemoryUtils.getByte(computeMemoryOffset(readIndex++));
	}

	private void checkIndex(int index) {
		if(index > getCapacity())
			throw new IndexOutOfBoundsException(String.format("pos: %d (expected: range(0, %d))", index, getCapacity()));
	}
	
	private long computeMemoryOffset(int index) {
		return memoryAddress + index;
	}
	
	public final byte readByte() throws IOException {
		return (byte)read();
	}

	public final void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);
	}

	public void readFully(byte[] buf, int offset, int length) throws IOException {
		if(buf == null) {
			throw new NullPointerException();
		} else if(offset < 0 || length < 0 || length > buf.length - offset) {
			throw new IndexOutOfBoundsException();
		}
		MemoryUtils.copyMemory(null, computeMemoryOffset(readIndex), buf, offset + MemoryUtils.arrayBaseOffset(), length);
		readIndex += length;
	}

	public int skipBytes(int n) throws IOException {
		throw new UnsupportedOperationException();
	}

	public boolean readBoolean() throws IOException {
		int value = read();
		return value == 1 ? true : false;
	}

	public int readUnsignedByte() throws IOException {
		throw new UnsupportedOperationException();
	}

	public short readShort() throws IOException {
		checkIndex(readIndex + 2);
		short shortValue = MemoryUtils.getShort(computeMemoryOffset(readIndex));
		readIndex += 2;
		return NATIVE_ORDER ? shortValue : Short.reverseBytes(shortValue);
	}

	public int readUnsignedShort() throws IOException {
		throw new UnsupportedOperationException();
	}

	public char readChar() throws IOException {
		checkIndex(readIndex + 2);
		char charValue = MemoryUtils.getChar(computeMemoryOffset(readIndex));
		readIndex += 2;
		return NATIVE_ORDER ? charValue : Character.reverseBytes(charValue);
	}

	public int readInt() throws IOException {
		checkIndex(readIndex + 4);
		int intValue = MemoryUtils.getInt(computeMemoryOffset(readIndex));
		readIndex += 4;
		return NATIVE_ORDER ? intValue : Integer.reverseBytes(intValue);
	}

	public long readLong() throws IOException {
		checkIndex(readIndex + 8);
		long longValue = MemoryUtils.getLong(computeMemoryOffset(readIndex));
		readIndex += 8;
		return NATIVE_ORDER ? longValue : Long.reverseBytes(longValue);
	}

	public float readFloat() throws IOException {
		checkIndex(readIndex + 4);
		int intValue = MemoryUtils.getInt(computeMemoryOffset(readIndex));
		readIndex += 4;
		return Float.intBitsToFloat(NATIVE_ORDER ? intValue : Integer.reverseBytes(intValue));
	}

	public double readDouble() throws IOException {
		checkIndex(readIndex + 8);
		long longValue = MemoryUtils.getLong(computeMemoryOffset(readIndex));
		readIndex += 8;
		return Double.longBitsToDouble(NATIVE_ORDER ? longValue : Long.reverseBytes(longValue));
	}

	public String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}

	public String readUTF() throws IOException {
		int utflen = readShort();
		byte[] buf = new byte[utflen];
		char[] cBuf = new char[utflen];
		
		int char1, char2, char3;
		int count = 0;
		int charCount = 0;
		
		readFully(buf, 0, buf.length);

		while (count < utflen) {
			char1 = (int) buf[count] & 0xFF;
			if (char1 > 127)
				break;
			count++;
			cBuf[charCount++] = (char) char1;
		}

		while (count < utflen) {
			char1 = (int) buf[count] & 0xFF;
			// 高4位
			switch (char1 >> 4) {
				case 0: 
				case 1: 
				case 2: 
				case 3: 
				case 4: 
				case 5: 
				case 6: 
				case 7:
					/* 0xxxxxxx*/
					count++;
					cBuf[charCount++] = (char)char1;
					break;
				case 12: 
				case 13:
					/* 110x xxxx   10xx xxxx*/
					count += 2;
					
					if (count > utflen)
						throw new UTFDataFormatException("malformed input: partial character at end");
					
					char2 = (int)buf[count - 1];
					
					if ((char2 & 0xC0) != 0x80)
						throw new UTFDataFormatException("malformed input around byte " + count);
					
					cBuf[charCount++] = (char)(((char1 & 0x1F) << 6) | (char2 & 0x3F));
					
					break;
					
				case 14:
					/* 1110 xxxx  10xx xxxx  10xx xxxx */
					count += 3;
					if (count > utflen)
						throw new UTFDataFormatException("malformed input: partial character at end");

					char2 = (int) buf[count-2];
					char3 = (int) buf[count-1];
					if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80))
						throw new UTFDataFormatException("malformed input around byte " + (count-1));
	                            
					cBuf[charCount++]=(char)(((char1 & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));      
					break;
				default:
					/* 10xx xxxx,  1111 xxxx */
					throw new UTFDataFormatException("malformed input around byte " + count);
			}
		}
		return new String(cBuf, 0, charCount);
	}

	public int getCapacity() {
		return capacity;
	}

	public int available() throws IOException {
		return getCapacity() - readIndex;
	}

	@Override
	public void close() throws IOException {
		buffer.clear();
		MemoryUtils.freeDirectBuffer(buffer);
		buffer = null;
		readIndex = 0;
		memoryAddress = 0;
		capacity = 0;
		mark = 0;
	}

	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	public int read(byte[] b, int offset, int length) throws IOException {
		readFully(b, offset, length);
		return length;
	}

	public long skip(long n) throws IOException {
		if(n > capacity)
			throw new IndexOutOfBoundsException();
		int r = capacity - readIndex;
		int skipBytes = (r >= (int)n ? (int)n : r);
		readIndex += skipBytes;
		return skipBytes;
	}

	public void mark(int readlimit) {
		if(readlimit > capacity)
			throw new IndexOutOfBoundsException(String.format("mark: %d (expected: range(0, %d))", readIndex, getCapacity()));
		mark = readlimit;
	}

	
	public synchronized void reset() throws IOException {
		readIndex = mark;
	}

	public boolean markSupported() {
		return true;
	}

	public int getIndex() {
		return readIndex;
	}
	
	public void setIndex(int index) {
		this.readIndex = index;
	}
	
}
