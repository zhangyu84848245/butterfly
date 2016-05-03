package org.fantasy.util;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;



public abstract class ClassUtils {

	
	public static ClassLoader getClassLoader() {
		ClassLoader cl = null;
		cl = Thread.currentThread().getContextClassLoader();
		if(cl == null) {
			cl = ClassUtils.class.getClassLoader();
			if(cl == null) {
				cl = ClassLoader.getSystemClassLoader();
			}
		}
		return cl;
	}
	
	private static final Map<ClassLoader, Map<String, WeakReference<Class<?>>>> CLASS_CACHE = new WeakHashMap<ClassLoader, Map<String,WeakReference<Class<?>>>>();
	public static final Class<?> NEGATIVE_CACHE_SENTINEL = NegativeCacheSentinel.class;
	
	public static Class<?> forName(String name) {
		ClassLoader loader = getClassLoader();
		Map<String, WeakReference<Class<?>>> map = null;
		synchronized(CLASS_CACHE) {
			map = CLASS_CACHE.get(loader);
			if(map == null) {
				map = Collections.synchronizedMap(new WeakHashMap<String, WeakReference<Class<?>>>());
				CLASS_CACHE.put(loader, map);
			}
		}
		
		Class<?> clazz = null;
		
		WeakReference<Class<?>> weakRef = map.get(name);
		
		if(weakRef != null) {
			clazz = weakRef.get();
		}

		if(clazz == null) {
			try {
				// 避免出错时一直执行Class.forName
				clazz = Class.forName(name, true, loader);
			} catch (ClassNotFoundException e) {
				map.put(name, new WeakReference<Class<?>>(NEGATIVE_CACHE_SENTINEL));
				return null;
			}
			map.put(name, new WeakReference<Class<?>>(clazz));
			return clazz;
		} else if(clazz == NEGATIVE_CACHE_SENTINEL) {
			return null;
		} else {
			return clazz;
		}

	}
	
	private static abstract class NegativeCacheSentinel {}
	
	
	public static String getClassSignature(Class<?> clazz) {
		StringBuilder result = new StringBuilder();
		if(clazz.isArray()) {
			result.append("[");
			clazz = clazz.getComponentType();
		}
		if(clazz.isPrimitive()) {
			if (clazz == Integer.TYPE) {
				result.append('I');
            } else if (clazz == Byte.TYPE) {
            	result.append('B');
            } else if (clazz == Long.TYPE) {
            	result.append('J');
            } else if (clazz == Float.TYPE) {
            	result.append('F');
            } else if (clazz == Double.TYPE) {
            	result.append('D');
            } else if (clazz == Short.TYPE) {
            	result.append('S');
            } else if (clazz == Character.TYPE) {
            	result.append('C');
            } else if (clazz == Boolean.TYPE) {
            	result.append('Z');
            } else if (clazz == Void.TYPE) {
            	result.append('V');
            } else {
                throw new InternalError();
            }
		} else {
			result.append("L" + clazz.getName().replace('.', '/') + ';');
		}
		return result.toString();
	}

}
