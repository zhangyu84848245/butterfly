package org.fantasy.net.io.writer;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.fantasy.net.io.unsafe.ObjectOutput;
import org.fantasy.util.Constant;

public class MapWriter extends AbstractWriter<Map<? extends Serializable, ? extends Serializable>> /** implements Writer<Map<? extends Serializable, ? extends Serializable>> */ {
//	public static final byte TYPE_MAP = (byte)0x14;
//	private ObjectOutput oout;
//	
//	public MapWriter(ObjectOutput oout) {
//		this.oout = oout;
//	}
	
	public void write(Map<? extends Serializable, ? extends Serializable> map, ObjectOutput oout) throws IOException {
		oout.writeByte(Constant.TYPE_MAP);
		oout.writeUTF(map.getClass().getName());
		oout.writeInt(map.size());
		for(Iterator<?> iterator = map.entrySet().iterator();iterator.hasNext();) {
			Map.Entry<Serializable, Serializable> entry = (Entry<Serializable, Serializable>) iterator.next();
			Serializable key = entry.getKey();
			Serializable value = entry.getValue();
			oout.writeObject(key);
			oout.writeObject(value);
			
		}
//		oout.writeByte(Constant.WRITE_END);
	}
	
}
