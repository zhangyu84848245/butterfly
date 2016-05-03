package org.fantasy.net.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;

public class JdkSerialization implements Serialization<Serializable> {

	static final class JdkSerializer implements Serializer<Serializable> {

		private ByteArrayOutputStream baos;
		private ObjectOutputStream oos;
		private ByteBuffer buffer;
		
		public JdkSerializer() {
			this.baos = new ByteArrayOutputStream();
			try {
				this.oos = new ObjectOutputStream(baos) {
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
			buffer = ByteBuffer.wrap(baos.toByteArray());
			return buffer;
		}
		
		public int serializedBytes() throws IOException {
			return buffer.remaining();
		}

		public void close() throws IOException {
			if(baos != null) 
				baos.close();
			
			if(oos != null)
				oos.close();
			
			if(buffer != null)
				buffer.clear();
		}
		
	}
	public Serializer<Serializable> getSerializer() {
		return new JdkSerializer();
	}
	
	static final class JdkDeserializer<T extends Serializable> implements Deserializer<T> {
		private ByteArrayInputStream bais;
		private ObjectInputStream ois;
		private int offset;
		private ByteBuffer buffer;
		public JdkDeserializer() {
		}

		public T deserialize(ByteBuffer bb) throws IOException, ClassNotFoundException {
			this.offset = bb.position();
			this.buffer = bb;
			bais = new ByteArrayInputStream(bb.array());
			ois = new ObjectInputStream(bais) {
				protected void readStreamHeader() throws IOException, StreamCorruptedException {
				}
			};
			return (T)ois.readObject();
		}

		public void close() throws IOException {
			if(ois != null)
				ois.close();
			if(bais != null)
				bais.close();
			offset = 0;
			buffer.clear();
		}

		public int deserializedBytes() throws IOException {
			return buffer.position() - offset;
		}

	}

	public Deserializer<Serializable> getDeserializer() {
		return new JdkDeserializer<Serializable>();
	}

//	public boolean accept(Class<?> clazz) {
//		return Serializable.class.isAssignableFrom(clazz);
//	}

}
