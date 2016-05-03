package org.fantasy.net.io;

public interface Serialization<T> {

//	public boolean accept(Class<?> clazz);
	
	public Serializer<T> getSerializer();
	
	public Deserializer<T> getDeserializer();
	
}
