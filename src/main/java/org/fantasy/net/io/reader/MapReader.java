package org.fantasy.net.io.reader;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.fantasy.net.io.unsafe.ObjectInput;

public class MapReader extends AbstractReader<Map<? extends Serializable, ? extends Serializable>> {

	public Map<Serializable, Serializable> read(ObjectInput oin) throws IOException, ClassNotFoundException {
		String className = oin.readUTF();
		Class<? extends Serializable> clazz = (Class<? extends Serializable>) Class.forName(className);
		int size = oin.readInt();
		Map<Serializable, Serializable> map = null;
		try {
			Constructor cons = clazz.getConstructor(new Class[]{int.class});
			map = (Map<Serializable, Serializable>) cons.newInstance(size);
		} catch (Exception e) {
			try {
				map = (Map<Serializable, Serializable>) clazz.newInstance();
			} catch (Exception e1) {
				
			}
		}
		
		if(map != null) {
			
		} else if(SortedMap.class.isAssignableFrom(clazz)) {
			map = new TreeMap<Serializable, Serializable>();
		} else if(Map.class.isAssignableFrom(clazz)) {
			map = new HashMap<Serializable, Serializable>();
		} else {
			throw new InternalError("Can't create Map instance " + className);
		}
		
		for(int i = 0; i < size; i++) {
			Serializable key = (Serializable)oin.readObject();
			Serializable value = (Serializable)oin.readObject();
			map.put(key, value);
		}
		
		return map;
	}

	
	
}
