package org.fantasy.bean.asm;

import java.util.LinkedHashMap;
import java.util.Map;

public class AnnotationAttributes extends LinkedHashMap<String, Object> {

	private static final long serialVersionUID = 6321327444386785764L;

	public AnnotationAttributes() {}

	public AnnotationAttributes(Map<String, Object> map) {
		super(map);
	}

	public AnnotationAttributes(int initialCapacity) {
		super(initialCapacity);
	}

	public static AnnotationAttributes fromMap(Map<String, Object> map) {
		if (map == null) {
			return null;
		}
		
		if(map instanceof AnnotationAttributes) {
			return (AnnotationAttributes)map;
		}
		
		return new AnnotationAttributes(map);
	}

	public String getString(String key) {
		return doGet(key, String.class);
	}

	public String[] getStringArray(String key) {
		return doGet(key, String[].class);
	}
	
	public Object[] getObjectArray(String key) {
		return doGet(key, Object[].class);
	}

	public boolean getBoolean(String key) {
		return doGet(key, Boolean.class);
	}

	public <N extends Number> N getNumber(String key) {
		return (N)doGet(key, Integer.class);
	}

	public <E extends Enum<?>> E getEnum(String key) {
		return (E)doGet(key, Enum.class);
	}

	public <T> Class<? extends T> getClass(String key) {
		return doGet(key, Class.class);
	}

	public Class<?>[] getClassArray(String key) {
		return doGet(key, Class[].class);
	}
	
	public AnnotationAttributes getAnnotation(String key) {
		return doGet(key, AnnotationAttributes.class);
	}

	public AnnotationAttributes[] getAnnotationArray(String key) {
		return doGet(key, AnnotationAttributes[].class);
	}
	
	private <T> T doGet(String key, Class<T> expectedType) {
		return (T)get(key);
	}
	
}
