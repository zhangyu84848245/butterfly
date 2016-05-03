package org.fantasy.util;

import java.lang.reflect.Array;

public class ObjectUtils {

	
	public static <T> T checkNull(T object, String message) {
		if(object == null) {
			throw new IllegalArgumentException(message);
		}
		return object;
	}
	
	public static <A, O extends A> Object[] addObjectToArray(A[] array, O object) {
		Class<?> componentType = Object.class;
		if(array != null) {
			componentType = array.getClass().getComponentType();
		} else if(object != null) {
			componentType = object.getClass();
		}
		
		int newArrayLength = array == null ? 1 : array.length + 1;
		A[] newArray = (A[])Array.newInstance(componentType, newArrayLength);
		if(array != null) {
			System.arraycopy(array, 0, newArray, 0, array.length);
		}
		newArray[newArrayLength - 1] = object;
		return newArray;
	}
	
}
