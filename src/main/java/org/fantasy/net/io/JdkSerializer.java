package org.fantasy.net.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import org.fantasy.net.io.unsafe.UnsafeOutputStream;

public class JdkSerializer implements Serializer<Serializable> {

	private OutputStream out;
	private ObjectOutputStream oos;
	private ByteBuffer buffer;
	private boolean useUsafe;
	
	public JdkSerializer(boolean useUsafe) {
		this.useUsafe = useUsafe;
		if(useUsafe)
			this.out = new UnsafeOutputStream();
		else
			this.out = new ByteArrayOutputStream();
		try {
			this.oos = new ObjectOutputStream(out) {
				protected void writeStreamHeader() throws IOException {
				}
			};
		} catch (IOException e) {
		}
	}

	public ByteBuffer serialize(Serializable object) throws IOException {
		if(object == null)
			return null;
		oos.writeObject(object);
		if(useUsafe)
			buffer = ((UnsafeOutputStream)out).getBuffer();
		else 
			buffer = ByteBuffer.wrap(((ByteArrayOutputStream)out).toByteArray());
		return buffer;
	}
	
	public int serializedBytes() throws IOException {
		return buffer.remaining();
	}

	public void close() throws IOException {
		if(oos != null) {
			oos.close();
			oos = null;
		}
		if(buffer != null)
			buffer.clear();
	}
}
