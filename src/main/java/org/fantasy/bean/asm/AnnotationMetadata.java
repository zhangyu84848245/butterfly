package org.fantasy.bean.asm;

import java.util.Map;
import java.util.Set;

public interface AnnotationMetadata extends ClassMetadata {
	
	Set<String> getAnnotationTypes();
	
	Set<String> getMetaAnnotationTypes(String annotationType);
	
	boolean hasAnnotation(String annotationType);
	
	boolean hasMetaAnnotation(String metaAnnotationType);
	
	boolean isAnnotated(String annotationType);
	
	Map<String, Object> getAnnotationAttributes(String annotationType);
	
	Map<String, Object> getAnnotationAttributes(String annotationType, boolean classValuesAsString);

	boolean hasAnnotatedMethods(String annotationType);

	Set<MethodMetadata> getAnnotatedMethods(String annotationType);
}
