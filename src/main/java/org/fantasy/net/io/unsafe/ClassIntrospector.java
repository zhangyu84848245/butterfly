package org.fantasy.net.io.unsafe;

import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.fantasy.util.ReflectionUtils;

/**
 * 类的内省器
 * @author fantasy
 */
public class ClassIntrospector implements Serializable {

	private static final long serialVersionUID = 8418139878863548752L;
	private static final Logger LOG = Logger.getLogger(ClassIntrospector.class);
	
	private static final Map<WeakKey<Class<?>>, Object> CACHE = new HashMap<WeakKey<Class<?>>, Object>();
	private static final ReferenceQueue<Class<?>> QUEUE = new ReferenceQueue<Class<?>>();
	private static final Object PENDING_MARKER = new Object();
	
	private String name;
	private Class<?> clazz;
	private ClassIntrospector parent;
	private Method writeObjectMethod;
	private Method readObjectMethod;
	private List<FieldIntrospector> fieldList;
	private int numOfPrimitiveField;
	private int numOfObjectField;
	
	private ClassIntrospector(Class<?> clazz) {
		this.name = clazz.getName();
		this.clazz = clazz;
		this.parent = introspect(clazz.getSuperclass());
		this.writeObjectMethod = ReflectionUtils.getMethod(clazz, "writeObject", new Class[]{ObjectOutput.class}, void.class);
		this.readObjectMethod = ReflectionUtils.getMethod(clazz, "readObject", new Class[]{ObjectInput.class}, void.class);
		this.fieldList = getSerializableFields(clazz);
	}
	
	/**
	 * 自省Class对象
	 * @param clazz
	 * @return
	 */
	public static ClassIntrospector introspect(Class<?> clazz) {
		// 必须实现序列化的接口
		if(clazz == null || (!Serializable.class.isAssignableFrom(clazz) && !clazz.isEnum()))
			return null;
		ClassIntrospector ci = null;
		WeakKey<Class<?>> key = new WeakKey<Class<?>>(clazz, QUEUE);
		synchronized(CACHE) {
			do {
				Object value = CACHE.get(key);
				if(value instanceof Reference)
					ci = (ClassIntrospector)((Reference)value).get();
				
				if(ci != null) {
					return ci;
				} else if(value == PENDING_MARKER) {
					try {
						CACHE.wait();
					} catch (InterruptedException e) {
					}
					continue;
				} else {
					CACHE.put(key, PENDING_MARKER);
					break;
				}
			} while(true);
		}
		
		try {
			ci = new ClassIntrospector(clazz);
		} finally {
			synchronized(CACHE) {
				if(ci != null) {
					CACHE.put(key, new SoftReference<ClassIntrospector>(ci));
				} else {
					CACHE.remove(key);
				}
				CACHE.notifyAll();
			}
		}
		return ci;

	}
	
	public boolean hasSerializableMethod() {
		return writeObjectMethod != null && readObjectMethod != null;
	}
	
	
	public int numOfPrimitiveField() {
		return numOfPrimitiveField;
	}

	public int numOfObjectField() {
		return numOfObjectField;
	}

	public Method getWriteObjectMethod() {
		return writeObjectMethod;
	}

	public Method getReadObjectMethod() {
		return readObjectMethod;
	}


	private List<FieldIntrospector> getSerializableFields(Class<?> clazz) {
		Field[] classFields = clazz.getDeclaredFields();
		List<FieldIntrospector> fieldList = new ArrayList<FieldIntrospector>();
		int mask = Modifier.STATIC | Modifier.TRANSIENT;
		for(int i = 0; i < classFields.length; i++) {
			if((classFields[i].getModifiers() & mask) == 0) {
				FieldIntrospector fi = new FieldIntrospector(classFields[i]);
				Class<?> type = fi.getType();
				if(type.isPrimitive())
					numOfPrimitiveField++;
				else
					numOfObjectField++;
				fieldList.add(fi);
			}
		}
		Collections.sort(fieldList);
		return fieldList;
	}


	public ClassIntrospector getParent() {
		return parent;
	}
	
	
	public void invokeWriteObject(Object obj, ObjectOutput objectOutput) throws IOException {
		if(writeObjectMethod != null) {
			try {
				writeObjectMethod.invoke(obj, new Object[]{ objectOutput });
			} catch(InvocationTargetException ex) {
				wrapException(ex);
			} catch(Exception e) {
				LOG.error(e.getMessage());
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	private void wrapException(InvocationTargetException ex) throws IOException {
		Throwable e = ex.getTargetException();
		LOG.error(e.getMessage());
		if(e instanceof IOException) 
			throw (IOException)e;
		
		if(e instanceof RuntimeException)
			throw (RuntimeException)e;
		
		if(e instanceof Error)
			throw (Error)e;
			
		IOException ioe = new IOException("unexpected exception type");
		ioe.initCause(e);
		throw ioe;
	}
	
	public void invokeReadObject(Serializable object, ObjectInput oin) throws IOException {
		if(readObjectMethod != null) {
			try {
				readObjectMethod.invoke(object, oin);
			} catch (InvocationTargetException e) {
				wrapException(e);
			} catch(Exception e) {
				LOG.error(e.getMessage());
			}
		} else {
			throw new UnsupportedOperationException();
		}
	}
	
	public void writeSerializableFields(Serializable object, ObjectOutput oout) throws IOException {
		int size = fieldList.size();
		if(size != numOfPrimitiveField + numOfObjectField)
			throw new IOException("Field number does not match");
		int i = 0;
		for(; i < numOfPrimitiveField; i++)
			fieldList.get(i).writePrimitiveField(object, oout);
		
		for(; i < size; i++)
			fieldList.get(i).writeObjectField(object, oout);
	}
	
	public void readSerializableFields(Serializable object, ObjectInput oin) throws IOException, ClassNotFoundException {
		int size = fieldList.size();
		if(size != numOfPrimitiveField + numOfObjectField)
			throw new IOException("Field number does not match");
		int i = 0;
		for(; i < numOfPrimitiveField; i++)
			fieldList.get(i).readPrimitiveField(object, oin);
		
		for(; i < size; i++)
			fieldList.get(i).readObjectField(object, oin);
	}

	public String getName() {
		return name;
	}
	
	/**
	 * 类的继承层次
	 * @return
	 */
	public int getClassHierarchy() {
		int hierarchy = 0;
		ClassIntrospector ci = this;
		for(; ci != null; ci = ci.getParent())
			hierarchy++;
		return hierarchy;
	}
	
	
	static class WeakKey<T> extends WeakReference<T> {
		
		private int hash;
		
		public WeakKey(T referent, ReferenceQueue<? super T> queue) {
			super(referent, queue);
			this.hash = System.identityHashCode(referent);
		}

		public int hashCode() {
			return hash;
		}

		public boolean equals(Object other) {
			if(other == null)
				return false;
			
			if(this == other)
				return true;
			
			if(other instanceof WeakKey) {
				WeakKey<T> that = (WeakKey<T>)other;
				T referent = get();
				return referent != null && referent == that.get();
			}
			return false;
		}
	}
	
}
