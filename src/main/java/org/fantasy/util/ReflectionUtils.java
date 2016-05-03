package org.fantasy.util;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.fantasy.conf.Configurable;
import org.fantasy.conf.Configuration;

import sun.reflect.ReflectionFactory;

public class ReflectionUtils {
	private static final Logger LOG = Logger.getLogger(ReflectionUtils.class);
	
	public static final Method DEFINE_CLASS;
	static {
		try {
			Class loader = Class.forName("java.lang.ClassLoader");
			DEFINE_CLASS = loader.getDeclaredMethod("defineClass", new Class[]{
				String.class,
				byte[].class,
				Integer.TYPE,
				Integer.TYPE,
				ProtectionDomain.class
			});
			DEFINE_CLASS.setAccessible(true);
		} catch (Throwable ex) {
			throw new Error();
		}
	}
	
	public static Class<?> defineClass(String className, byte[] buf, ClassLoader loader, ProtectionDomain protectionDomain) throws Throwable {
		Class<?> theClass = (Class<?>)DEFINE_CLASS.invoke(loader, className, buf, 0, buf.length, protectionDomain);
		Class.forName(className, true, loader);
		return theClass;
	}
	
	public static <T> T newInstance(Class<T> clazz) {
		return newInstance(clazz, null);
	}
	
	private static final ReflectionFactory REFLECTION_FACTORY = ReflectionFactory.getReflectionFactory();
	
	public static <T> T newSerializableInstance(Class<T> clazz) throws InvocationTargetException {
		Class<?> cl = clazz;
		while(Serializable.class.isAssignableFrom(cl)) {
			if ((cl = cl.getSuperclass()) == null) {
				return null;
			}
		}
		try {
			Constructor<?> cons = cl.getDeclaredConstructor((Class<?>[]) null);
			cons = REFLECTION_FACTORY.newConstructorForSerialization(clazz, cons);
			cons.setAccessible(true);
			return (T)cons.newInstance();
		} catch (NoSuchMethodException e) {
			LOG.error(e.getMessage());
			return null;
		} catch (InstantiationException e) {
			LOG.error(e.getMessage());
			return null;
		} catch (IllegalAccessException e) {
			LOG.error(e.getMessage());
			return null;
		} catch (IllegalArgumentException e) {
			LOG.error(e.getMessage());
			return null;
		}
	}
	
	public static <T> T newInstance(Class<T> clazz, Configuration conf, Object ... constructorArgs) {
		T result = null;
		try {
			Class<?>[] consArgs = Constant.CLASS_EMPTY_ARGS;
			int len = constructorArgs.length;
			if(len != 0) {
				consArgs = new Class[constructorArgs.length];
				for(int i = 0; i < len; i++)
					consArgs[i] = constructorArgs[i].getClass();
			}

			Constructor<T> constructor = clazz.getDeclaredConstructor(consArgs);
			constructor.setAccessible(true);
			result = constructor.newInstance(constructorArgs);
			if(result instanceof Configurable) {
				((Configurable)result).setConfig(conf);
			}
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new RuntimeException(e);
		}
		
		return result;
	}

	public static <T> T getFieldValue(Object object, String fieldName) {
		try {
			Field field = getField(object.getClass(), fieldName);
			if(field != null) {
				field.setAccessible(true);
				return (T)field.get(object);
			}
		} catch (Exception e) {
			LOG.error(e);
		}
		return null;
	}
	public static Field getField(Class<?> theClass, String fieldName) {
		return getField(theClass, fieldName, null);
	}

	public static Field getField(Class<?> theClass, String fieldName, Class<?> type) {
		Class<?> searchClass = theClass;
		while(!Object.class.equals(searchClass) && searchClass != null) {
			Field[] fields = searchClass.getDeclaredFields();
			for(Field field : fields) {
				if(fieldName.equals(field.getName()) && ( type == null || type.equals(field.getType()) ) ) {
					return field;
				}
			}
			searchClass = searchClass.getSuperclass();
		}
		return null;
	}
	
	private static String getDescriptor(Class<?> clazz) {
		StringBuilder result = new StringBuilder();
		while (clazz.isArray()) {
			result.append('[');
			clazz = clazz.getComponentType();
		}
		if (clazz.isPrimitive()) {
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
			result.append('L' + clazz.getName().replace('.', '/') + ';');
		}
		return result.toString();
	}
	
	public static Method getMethod(Class<?> theClass, String name, Class<?>[] parameterTypes, Class<?> returnType) {
		try {
			Method method = theClass.getDeclaredMethod(name, parameterTypes);
			method.setAccessible(true);
			int modifier = method.getModifiers();
			return (method.getReturnType().equals(returnType) && (modifier & Modifier.STATIC) == 0 && (modifier & Modifier.PRIVATE) != 0) ? method : null;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static Method getMethodByName(Class<?> theClass, String name, Class<?>[] parameterTypes) throws NoSuchMethodException, SecurityException {
		Method method = theClass.getDeclaredMethod(name, parameterTypes);
		method.setAccessible(true);
		return method;
	}
	
	
	public static List<Method> getAllMethods(Class<?> theClass, List<Method> methods) {
		methods.addAll(Arrays.asList(theClass.getDeclaredMethods()));
		Class<?> superClass = theClass.getSuperclass();
		if(superClass == null)
			return methods;
		if(superClass != Object.class) {
			getAllMethods(superClass, methods);
		}
		Class<?>[] interfaces = theClass.getInterfaces();
		for(Class<?> interfaceClass : interfaces) {
			getAllMethods(interfaceClass, methods);
		}
		return methods;
	}
	
	
	public static String getMethodDescriptor(Method method) {
		StringBuilder result = new StringBuilder();
		Class<?>[] parameterTypes = method.getParameterTypes();
		result.append("(");
		for(Class<?> type : parameterTypes) {
			result.append(getDescriptor(type));
		}
		result.append(")");
		Class<?> returnType = method.getReturnType();
		result.append(getDescriptor(returnType));
		return result.toString();
	}
	
	
}
