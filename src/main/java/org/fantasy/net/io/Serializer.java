package org.fantasy.net.io;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Serializer<T> {
	
	/**
	 * 序列化的字节数
	 * @return
	 * @throws IOException
	 */
	public int serializedBytes() throws IOException;
	/**
	 * 序列化
	 * @param object
	 * @return
	 * @throws IOException
	 */
	public ByteBuffer serialize(T object) throws IOException;
	/**
	 * 关闭
	 * @throws IOException
	 */
	public void close() throws IOException;
}
