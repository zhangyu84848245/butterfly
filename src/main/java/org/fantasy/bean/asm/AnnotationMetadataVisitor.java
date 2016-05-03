package org.fantasy.bean.asm;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.fantasy.util.ClassUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class AnnotationMetadataVisitor extends ClassMetadataVisitor implements AnnotationMetadata {

	// 注解的类名
	private final Set<String> annotationSet = new LinkedHashSet<String>();
	private final Map<String, Set<String>> metaAnnotationMap = new LinkedHashMap<String, Set<String>>(4);
	private final Map<String, AnnotationAttributes> attributeMap = new LinkedHashMap<String, AnnotationAttributes>(4);
	private final Set<MethodMetadata> methodMetadataSet = new LinkedHashSet<MethodMetadata>(4);
	
	public Set<String> getAnnotationTypes() {
		return this.annotationSet;
	}
	
	public Set<String> getMetaAnnotationTypes(String annotationType) {
		return this.metaAnnotationMap.get(annotationType);
	}
	
	public boolean hasAnnotation(String annotationType) {
		return this.annotationSet.contains(annotationType);
	}
	
	public boolean hasMetaAnnotation(String metaAnnotationType) {
		Collection<Set<String>> allMetaTypes = this.metaAnnotationMap.values();
		for (Set<String> metaTypes : allMetaTypes) {
			if (metaTypes.contains(metaAnnotationType)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isAnnotated(String annotationType) {
		return this.attributeMap.containsKey(annotationType);
	}
	
	public Map<String, Object> getAnnotationAttributes(String annotationType) {
		return getAnnotationAttributes(annotationType, false);
	}
	
	public Map<String, Object> getAnnotationAttributes(String annotationType, boolean classValuesAsString) {
		AnnotationAttributes raw = this.attributeMap.get(annotationType);
		return convertClassValues(raw, classValuesAsString);
	}
	
	private AnnotationAttributes convertClassValues(AnnotationAttributes original, boolean classValuesAsString) {
		if(original == null) {
			return null;
		}
		ClassLoader classLoader = ClassUtils.getClassLoader();
		AnnotationAttributes result = new AnnotationAttributes(original.size());
		for(Iterator<Map.Entry<String, Object>> iterator = original.entrySet().iterator();iterator.hasNext();) {
			Map.Entry<String, Object> entry = iterator.next();
			Object value = entry.getValue();
			try {
				if(value instanceof AnnotationAttributes) {
					value = convertClassValues((AnnotationAttributes)value, classValuesAsString);
				} else if(value instanceof AnnotationAttributes[]) {
					AnnotationAttributes[] values = (AnnotationAttributes[])value;
					for(int i = 0; i < values.length; i++) {
						values[i] = convertClassValues(values[i], classValuesAsString);
					}
				} else if(value instanceof Type) {
					value = classValuesAsString ? ((Type)value).getClassName() : classLoader.loadClass(((Type)value).getClassName());
				} else if(value instanceof Type[]) {
					Type[] array = (Type[])value;
					Object[] convArray = (classValuesAsString ? new String[array.length] : new Class[array.length]);
					for (int i = 0; i < array.length; i++) {
						convArray[i] = (classValuesAsString ? array[i].getClassName() : classLoader.loadClass(array[i].getClassName()));
					}
					value = convArray;
				} else if(classValuesAsString) {
					if (value instanceof Class) {
						value = ((Class<?>) value).getName();
					} else if (value instanceof Class[]) {
					
						Class<?>[] clazzArray = (Class[]) value;
						String[] newValue = new String[clazzArray.length];
						for (int i = 0; i < clazzArray.length; i++) {
							newValue[i] = clazzArray[i].getName();
						}
						value = newValue;
					}
				}
				result.put(entry.getKey(), value);
			} catch(Exception e) {
				
			}
		}
		return result;
	}
	
	public AnnotationVisitor visitAnnotation(String typeDescriptor, boolean visible) {
		// 注解的类名
		String className = Type.getType(typeDescriptor).getClassName();
		annotationSet.add(className);
		return new AnnotationAttributeVisitor(className, this.attributeMap, this.metaAnnotationMap);
	}

	public MethodVisitor visitMethod(int access, String name, String methodDescriptor, String signature, String[] exceptions) {
		return new MethodMetadataVisitor(name, access, getClassName(), methodDescriptor, this.methodMetadataSet, exceptions);
	}
	
	public boolean hasAnnotatedMethods(String annotationType) {
		for(MethodMetadata methodMetadata : methodMetadataSet) {
			if(methodMetadata.isAnnotated(annotationType)) {
				return true;
			}
		}
		return false;
	}
	
	public Set<MethodMetadata> getAnnotatedMethods(String annotationType) {
		Set<MethodMetadata> annotatedMethods = new LinkedHashSet<MethodMetadata>(4);
		for (MethodMetadata methodMetadata : this.methodMetadataSet) {
			if (methodMetadata.isAnnotated(annotationType)) {
				annotatedMethods.add(methodMetadata);
			}
		}
		return annotatedMethods;
	}
}
