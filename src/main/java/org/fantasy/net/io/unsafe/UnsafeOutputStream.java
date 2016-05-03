package org.fantasy.net.io.unsafe;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;

import org.fantasy.util.MemoryUtils;

public class UnsafeOutputStream extends OutputStream implements MarkableDataOutput {

	public static final int DEFAULT_INITIAL_CAPACITY = 256;
	// 8MB
	public static final int DEFAULT_MAX_CAPACITY = 8 * 1024 * 1024;
	private static final boolean NATIVE_ORDER = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
	
	protected ByteBuffer buffer;
	private long memoryAddress;
	private int capacity;
	private int writeIndex;
	private int maxCapacity;
	private int mark = -1;
	
	public UnsafeOutputStream() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_MAX_CAPACITY);
	}
	
	public UnsafeOutputStream(int initialCapacity, int maxCapacity) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(initialCapacity);

		if(initialCapacity < 0) {
			throw new IllegalArgumentException("initialCapacity: " + initialCapacity);
		}

		if(maxCapacity < 0) {
			throw new IllegalArgumentException("maxCapacity: " + maxCapacity);
		}

		if(initialCapacity > maxCapacity) {
			throw new IllegalArgumentException("initialCapacity > maxCapacity");
		}

		this.maxCapacity = maxCapacity;
		setByteBuffer(buffer);
	}
	
	private void setByteBuffer(ByteBuffer buffer) {
		ByteBuffer oldBuffer = this.buffer;
		if(oldBuffer != null)
			MemoryUtils.freeDirectBuffer(oldBuffer);
		this.buffer = buffer;
		this.memoryAddress = MemoryUtils.directBufferAddress(buffer);
		this.capacity = buffer.remaining();
	}
	
	public void ensureWritable(int writeBytes) {
		if(writeBytes < 0)
			throw new IllegalArgumentException("writableBytes: " + writeBytes + " (expected: >= 0)");
		
		if(writeBytes <= writableBytes())
			return;
		
		if(writeBytes > maxCapacity - writeIndex)
			throw new IndexOutOfBoundsException(String.format("writerIndex(%d) + minWritableBytes(%d) exceeds maxCapacity(%d): %s", writeIndex, writeBytes, maxCapacity, this));
		
		int newCapacity = calculateNewCapacity(writeIndex + writeBytes);
		capacity(newCapacity);
	}
	
	
	public void capacity(int newCapacity) {
		if(newCapacity < 0 || newCapacity > maxCapacity()) {
			throw new IllegalArgumentException("newCapacity: " + newCapacity);
		}
		int oldCapacity = capacity();
		if(newCapacity > oldCapacity) {
			ByteBuffer oldBuffer = this.buffer;
			ByteBuffer newBuffer = ByteBuffer.allocateDirect(newCapacity);
//			oldBuffer.position(0).limit(oldCapacity);
			oldBuffer.clear();
			newBuffer.position(0).limit(oldCapacity);
			newBuffer.put(oldBuffer);
			newBuffer.clear();
			setByteBuffer(newBuffer);
		}
	}
	
	private int calculateNewCapacity(int minNewCapacity) {
		final int maxCapacity = this.maxCapacity;
		// 1M
		int threshold = 1048576;
		if(minNewCapacity == threshold)
			return threshold;
		if(minNewCapacity > threshold) {
			int newCapacity = minNewCapacity / threshold * threshold;
			if(newCapacity > maxCapacity - threshold) {
				newCapacity = maxCapacity;
			} else {
				newCapacity += threshold;
			}
			return newCapacity;
		}
		int newCapacity = 1024;
		while(newCapacity < minNewCapacity)
			newCapacity <<= 1;
		return Math.min(newCapacity, maxCapacity);
	}
	
	public int writableBytes() {
		return capacity - writeIndex;
	}

	public void writeBoolean(boolean b) throws IOException {
		writeByte(b ? 1 : 0);
	}


	public void writeByte(int b) throws IOException {
		ensureWritable(1);
		MemoryUtils.putByte(computeMemoryOffset(writeIndex++), (byte)b);
	}

	public void writeShort(int s) throws IOException {
		ensureWritable(2);
		MemoryUtils.putShort(computeMemoryOffset(writeIndex), NATIVE_ORDER ? (short)s : Short.reverseBytes((short)s));
		writeIndex += 2;
	}

	public void writeChar(int c) throws IOException {
		writeShort(c);
	}

	public void writeInt(int i) throws IOException {
		ensureWritable(4);
		MemoryUtils.putInt(computeMemoryOffset(writeIndex), NATIVE_ORDER ? i : Integer.reverseBytes(i));
		writeIndex += 4;
	}

	public void writeLong(long l) throws IOException {
		ensureWritable(8);
		MemoryUtils.putLong(computeMemoryOffset(writeIndex), NATIVE_ORDER ? l : Long.reverseBytes(l));
		writeIndex += 8;
	}

	public void writeFloat(float f) throws IOException {
		writeInt(Float.floatToRawIntBits(f));
	}

	public void writeDouble(double d) throws IOException {
		writeLong(Double.doubleToRawLongBits(d));
	}
	
	public void writeBytes(String str) throws IOException {
		int length = str.length();
		ensureWritable(length + 4);
		writeInt(length);
		byte[] buf = str.getBytes();
		write(buf);
	}

	public void writeChars(String str) throws IOException {
		writeBytes(str);
	}

	// UTF-8最大是3byte
	public void writeUTF(String str) throws IOException {
		int strlen = str.length();
        int utflen = 0;
        int c, count = 0;

        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);
            /** 1 ~ 127 之间占一个字节   */
            // 0x0001 => 0000 0001
            // 0x007F => 0111 1111
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            // 0x07FF => 0111 1111 1111
            // > 2047
            } else if (c > 0x07FF) {
            	utflen += 3;
            } else {
                utflen += 2;
            }
        }
        // short  类型的最大值    32767
        if (utflen > 32767)
            throw new UTFDataFormatException("encoded string too long: " + utflen + " bytes");

        byte[] buf = new byte[utflen + 2];
        ensureWritable(utflen + 2);


        buf[count++] = (byte) ((utflen >>> 8) & 0xFF);
        buf[count++] = (byte) ((utflen >>> 0) & 0xFF);

		int i = 0;
		for (i = 0; i < strlen; i++) {
			c = str.charAt(i);
			// 0x0001 => 0000 0001
            // 0x007F => 0111 1111
			if (!((c >= 0x0001) && (c <= 0x007F)))
				break;
			buf[count++] = (byte) c;
		}

		for (; i < strlen; i++) {
			c = str.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F)) {
				buf[count++] = (byte) c;
			// 0000 0000 0000 0000
			// 0x07FF => 0111 1111 1111
			} else if (c > 0x07FF) {
				/**  1110 xxxx  10xx xxxx 10xx xxxx  */
				// 0x0F => 1111
				// 0xE0 => 1110 0000
				buf[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
				// 0x3F => 0011 1111
				// 0x80 => 1000 0000
				buf[count++] = (byte) (0x80 | ((c >> 6) & 0x3F));
				// 0x3F => 0011 1111
				// 0x80 => 1000 0000
				buf[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
				/**  */
			} else {
				// 0xC0 => 1100 0000
				// 0x80 => 1000 0000
				// 0x1F => 0001 1111
				/**  110x xxxx 10xx xxxx  */
				buf[count++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
				buf[count++] = (byte) (0x80 | ((c >> 0) & 0x3F));
			}
		}
		write(buf);
	}

	public void write(int b) throws IOException {
		writeByte(b);
	}

	
	@Override
	public void write(byte[] b) throws IOException {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] src, int srcOffset, int length) throws IOException {
		ensureWritable(length);
		checkIndex(writeIndex, length);
		if(length != 0) {
			MemoryUtils.copyMemory(src, MemoryUtils.arrayBaseOffset() + srcOffset, null, computeMemoryOffset(writeIndex), length);
		}
		writeIndex += length;
	}
	
	private void checkIndex(int index, int length) {
		if(length < 0) {
			throw new IllegalArgumentException("length: " + length + "(expected: >= 0)");
		}
		if(index > capacity() - length) {
			throw new IndexOutOfBoundsException(String.format("index: %d, length: %d (expected: range(0, %d))", index, length, capacity()));
		}
	}
	

	public void flush() throws IOException {
	}

	public void close() throws IOException {
		flush();
		buffer.clear();
		MemoryUtils.freeDirectBuffer(buffer);
		buffer = null;
		memoryAddress = 0L;
		capacity = 0;
		writeIndex = 0;
		maxCapacity = 0;
		mark = 0;
	}

	public int capacity() {
		return capacity;
	}

	public int maxCapacity() {
		return maxCapacity;
	}

	private long computeMemoryOffset(int index) {
		return memoryAddress + index;
	}

	public void mark() throws IOException {
		mark = writeIndex;
	}

	public int markValue() throws IOException {
		return mark;
	}

	public int getIndex() {
		return writeIndex;
	}
	
	public void setIndex(int writeIndex) {
		this.writeIndex = writeIndex;
	}

	public ByteBuffer getBuffer() {
		buffer.limit(writeIndex);
		return buffer;
	}

	public void reset() throws IOException {
		int m = mark;
		if(m < 0)
			throw new InvalidMarkException();
		this.writeIndex = m;
	}

	public void discardMark() throws IOException {
		mark = -1;
	}

}