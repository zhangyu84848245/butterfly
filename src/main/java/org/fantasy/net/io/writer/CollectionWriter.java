package org.fantasy.net.io.writer;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;

import org.fantasy.net.io.unsafe.ObjectOutput;
import org.fantasy.util.Constant;

public class CollectionWriter extends AbstractWriter<Collection<? extends Serializable>> /** implements Writer<Collection<? extends Serializable>> */ {
//	public static final byte TYPE_COLLECTION = (byte)0x13;
//	private ObjectOutput oout;
//	
//	public CollectionWriter(ObjectOutput oout) {
//		this.oout = oout;
//	}
	public void write(Collection<? extends Serializable> c, ObjectOutput oout) throws IOException {
		oout.writeByte(Constant.TYPE_COLLECTION);
		Class<?> clazz = c.getClass();
		oout.writeUTF(clazz.getName());
		oout.writeInt(c.size());

		for(Iterator<? extends Serializable> iterator = c.iterator();iterator.hasNext();) {
			Serializable object = iterator.next();
			oout.writeObject(object);
		}
//		oout.writeByte(Constant.WRITE_END);
	}

	
}
