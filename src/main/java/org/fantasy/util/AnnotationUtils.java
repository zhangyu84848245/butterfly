package org.fantasy.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class AnnotationUtils {

	
	public static Map<String, Object> getAnnotationAttributes(Annotation annotation, boolean classValuesAsString, boolean nestedAnnotationsAsMap) {
		Map<String, Object> attrs = new HashMap<String, Object>();
		Method[] methods = annotation.getClass().getDeclaredMethods();
		for(Method method : methods) {
			// 判断是注解方法
			if(method.getParameterTypes().length == 0 && method.getReturnType() != void.class) {
				try {
					Object value = method.invoke(annotation);
					if(classValuesAsString) {
						if(value instanceof Class) {
							value = ((Class<?>)value).getName();
						} else if(value instanceof Class[]) {
							Class<?>[] classArray = (Class<?>[])value;
							String[] newValue = new String[classArray.length];
							for(int i = 0; i < classArray.length; i++) {
								newValue[i] = classArray[i].getName();
							}
							value = newValue;
						}
					}
					
					if(nestedAnnotationsAsMap && value instanceof Annotation) {
						attrs.put(method.getName(), getAnnotationAttributes((Annotation)value, classValuesAsString, true));
					} else if(nestedAnnotationsAsMap && value instanceof Annotation[]) {
						Annotation[] annotationArray = (Annotation[])value;
						Map<String, Object>[] mappedAnnotations = new HashMap[annotationArray.length];
						for(int i = 0; i < annotationArray.length; i++) {
							mappedAnnotations[i] = getAnnotationAttributes(annotationArray[i], classValuesAsString, true);
						}
						attrs.put(method.getName(), mappedAnnotations);
					} else {
						attrs.put(method.getName(), value);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return attrs;
	}
}
