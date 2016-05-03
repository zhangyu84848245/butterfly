package org.fantasy.util;

public class SystemPropertyUtil {

	
	
	private SystemPropertyUtil() {
		
	}
	
	
	public static String get(String key, String defaultValue) {
		if(key == null) 
			throw new NullPointerException("key");
		if(key.isEmpty())
			throw new IllegalArgumentException("key must not be empty.");
		String value = System.getProperty(key);
		if(value == null)
			value = defaultValue;
		return value;
	}

}
