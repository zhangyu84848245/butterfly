package org.fantasy.net.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public interface Deserializer<T> {

	public T deserialize(ByteBuffer bb) throws IOException, ClassNotFoundException;

	public void close() throws IOException;

	public int deserializedBytes() throws IOException;

}
