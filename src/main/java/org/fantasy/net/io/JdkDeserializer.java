package org.fantasy.net.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;

import org.fantasy.net.io.unsafe.UnsafeInputStream;

public class JdkDeserializer<T extends Serializable> implements Deserializer<T> {

	private InputStream in;
	
	private ObjectInputStream ois;
	private int offset;
	private ByteBuffer buffer;
	private boolean useUnsafe;
	public JdkDeserializer(boolean useUnsafe) {
		this.useUnsafe = useUnsafe;
	}

	public T deserialize(ByteBuffer bb) throws IOException, ClassNotFoundException {
		this.offset = bb.position();
		this.buffer = bb;
		if(useUnsafe)
			in = new UnsafeInputStream(bb);
		else
			in = new ByteArrayInputStream(bb.array());
		ois = new ObjectInputStream(in) {
			protected void readStreamHeader() throws IOException, StreamCorruptedException {
			}
		};
		return (T)ois.readObject();
	}

	public void close() throws IOException {
		if(ois != null) {
			ois.close();
			ois = null;
		}
		offset = 0;
	}

	public int deserializedBytes() throws IOException {
		return buffer.position() - offset;
	}
}
