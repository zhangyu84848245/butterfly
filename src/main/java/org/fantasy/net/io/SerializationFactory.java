package org.fantasy.net.io;

import java.io.Serializable;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fantasy.common.SPILoader;

public class SerializationFactory {

	private static final ConcurrentMap<String, Serialization<Serializable>> SERIALIZATION_MAP = new ConcurrentHashMap<String, Serialization<Serializable>>();
	private static volatile boolean SERIALIZATION_LOADED = false;
	
	private SerializationFactory() {
		
	}

	private static void load() {
		synchronized(Serialization.class) {
			SPILoader<Serialization> serializationLoader = SPILoader.load(Serialization.class);
			for(Entry<String, Serialization> entry : serializationLoader)
				SERIALIZATION_MAP.put(entry.getKey(), entry.getValue());
			SERIALIZATION_LOADED = true;
		}
	}
	
	public static Serialization<Serializable> get(String key) {
		if(!SERIALIZATION_LOADED)
			load();
		return SERIALIZATION_MAP.get(key);
	}

}
