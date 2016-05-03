package org.fantasy.util;

import java.lang.reflect.Field;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import sun.misc.Cleaner;
import sun.misc.Unsafe;

public class MemoryUtils {
	private static final Unsafe UNSAFE;
	private static final long ADDRESS_FIELD_OFFSET;
	private static final long CLEANER_FIELD_OFFSET;
	private static final long UNSAFE_COPY_THRESHOLD = 1024L * 1024L;
	private static final long ARRAY_BASE_OFFSET;
	
	private MemoryUtils() {
		
	}
	
	static {
		
		ByteBuffer direct = ByteBuffer.allocateDirect(1);
		Field addressField;
		try {
			addressField = Buffer.class.getDeclaredField("address");
			addressField.setAccessible(true);
			// heap buffer
			if(addressField.getLong(direct) == 0) {
				addressField = null;
			}
		} catch (Throwable e) {
			addressField = null;
		}
		
		Unsafe unsafe = null;
		if(addressField != null) {
			try {
				Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
				unsafeField.setAccessible(true);
				unsafe = (Unsafe) unsafeField.get(null);
			} catch (Throwable e) {
				unsafe = null;
			}
		}
		UNSAFE = unsafe;
		
		if(unsafe != null) {
			ADDRESS_FIELD_OFFSET = unsafe.objectFieldOffset(addressField);
		} else {
			ADDRESS_FIELD_OFFSET = -1;
		}
		
		
		Field cleanerField;
        long fieldOffset = -1;
        if (UNSAFE != null) {
            try {
            	cleanerField = direct.getClass().getDeclaredField("cleaner");
                cleanerField.setAccessible(true);
                sun.misc.Cleaner cleaner = (sun.misc.Cleaner) cleanerField.get(direct);
                cleaner.clean();
                fieldOffset = UNSAFE.objectFieldOffset(cleanerField);
            } catch (Throwable t) {
                fieldOffset = -1;
            }
        }
        CLEANER_FIELD_OFFSET = fieldOffset;
        freeDirectBuffer(direct);
        
        ARRAY_BASE_OFFSET = UNSAFE.arrayBaseOffset(byte[].class);
	}
	
	public static void freeDirectBuffer(ByteBuffer buffer) {
        if (CLEANER_FIELD_OFFSET == -1 || !buffer.isDirect()) {
            return;
        }
        try {
            Cleaner cleaner = (Cleaner) UNSAFE.getObject(buffer, CLEANER_FIELD_OFFSET);
            if (cleaner != null) {
                cleaner.clean();
            }
        } catch (Throwable t) {
        	t.printStackTrace();
        }
    }
	
	public static long directBufferAddress(ByteBuffer buffer) {
		return getLong(buffer, ADDRESS_FIELD_OFFSET);
	}
	
	public static void putByte(long address, byte b) {
		UNSAFE.putByte(address, b);
	}
	
	public static void putShort(long address, short s) {
		UNSAFE.putShort(address, s);
	}
	
	public static void putInt(long address, int i) {
		UNSAFE.putInt(address, i);
	}
	
	public static void putLong(long address, long l) {
		UNSAFE.putLong(address, l);
	}
	
	public static void putFloat(long address, float f) {
		UNSAFE.putFloat(address, f);
	}
	
	public static void putDouble(long address, double d) {
		UNSAFE.putDouble(address, d);
	}
	
	public static long getLong(Object object, long fieldOffset) {
		return UNSAFE.getLong(object, fieldOffset);
	}
	
	public static int getInt(long address) {
		return UNSAFE.getInt(address);
	}
	
	
	public static long arrayBaseOffset() {
		return UNSAFE.arrayBaseOffset(byte[].class);
	}
	
	
	public static void copyMemory(Object src, long srcOffset, Object dst, long dstOffset, long length) {
		while(length > 0) {
			long copySize = Math.min(UNSAFE_COPY_THRESHOLD, length);
			UNSAFE.copyMemory(src, srcOffset, dst, dstOffset, copySize);
			length -= copySize;
			srcOffset += copySize;
            dstOffset += copySize;
		}
	}
	
	
	public static byte getByte(long address) {
		return UNSAFE.getByte(address);
	}
	
	public static short getShort(long address) {
		return UNSAFE.getShort(address);
	}
	
	public static char getChar(long address) {
		return UNSAFE.getChar(address);
	}
	
	public static long getLong(long address) {
		return UNSAFE.getLong(address);
	}
	
	

//	static long objectFieldOffset(Field field) {
//		return UNSAFE.objectFieldOffset(field);
//	}
}
