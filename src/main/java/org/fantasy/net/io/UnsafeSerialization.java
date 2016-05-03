package org.fantasy.net.io;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;

import org.fantasy.net.io.unsafe.UnsafeObjectInputStream;
import org.fantasy.net.io.unsafe.UnsafeObjectOutputStream;
import org.fantasy.net.proto.Request;
import org.fantasy.net.proto.Response;
import org.fantasy.util.MemoryUtils;

public class UnsafeSerialization implements Serialization<Serializable> {

//	public boolean accept(Class<?> clazz) {
//		return Serializable.class.isAssignableFrom(clazz);
//	}

	public Serializer<Serializable> getSerializer() {
		return new UnsafeSerializer();
	}

	private static class UnsafeSerializer implements Serializer<Serializable> {
		UnsafeObjectOutputStream out = new UnsafeObjectOutputStream();
		public ByteBuffer serialize(Serializable object) throws IOException {
			out.mark();
			// 写长度
			out.writeInt(-1);
			out.writeLong(-1L);
			out.writeObject(object);
			ByteBuffer buffer = out.buffer();
			int oldWriteIndex = out.getIndex();
			int dataLength = oldWriteIndex - 12;
			out.reset();
			out.writeInt(dataLength);
			long id = (object instanceof Request) ? ((Request)object).getId() : ((Response)object).getId();
			out.writeLong(id);
			out.setIndex(oldWriteIndex);
			return buffer;
		}

		public void close() throws IOException {
			out.close();
		}

		public int serializedBytes() throws IOException {
			return out.getIndex();
		}

		
	}

	private static class UnsafeDeserializer implements Deserializer<Serializable> {
		private UnsafeObjectInputStream in = null;
		public UnsafeDeserializer() {
		}
		public Serializable deserialize(ByteBuffer buffer) throws IOException, ClassNotFoundException {
			in = new UnsafeObjectInputStream(buffer);
			return (Serializable)in.readObject();
		}

		public void close() throws IOException {
			in.close();
		}

		public int deserializedBytes() throws IOException {
			return in.getIndex();
		}
	}

	public Deserializer<Serializable> getDeserializer() {
		return new UnsafeDeserializer();
	}

}
