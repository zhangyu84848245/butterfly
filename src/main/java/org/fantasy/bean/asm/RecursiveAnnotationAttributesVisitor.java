package org.fantasy.bean.asm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.log4j.Logger;
import org.fantasy.bean.annotation.Provider;
import org.fantasy.util.AnnotationUtils;
import org.fantasy.util.ClassUtils;






public class RecursiveAnnotationAttributesVisitor extends AbstractRecursiveAnnotationVisitor {

	private static final Logger LOG = Logger.getLogger(RecursiveAnnotationAttributesVisitor.class);
	private final String annotationType;
	
	public RecursiveAnnotationAttributesVisitor(String annotationType, AnnotationAttributes attributes) {
		super(attributes);
		this.annotationType = annotationType;
	}


	public final void visitEnd() {
		ClassLoader classLoader = ClassUtils.getClassLoader();
		try {
			Class<?> annotationClass = classLoader.loadClass(annotationType);
			doVisitEnd(annotationClass);
		} catch (ClassNotFoundException e) {
			LOG.error("Could not load annotation class " + annotationType, e);
		}
	}
	
	protected void doVisitEnd(Class<?> annotationClass) {
		registerDefaultValues(annotationClass);
	}
	
	private void registerDefaultValues(Class<?> annotationClass) {
		if(Modifier.isPublic(annotationClass.getModifiers())) {
			Method[] annotationAttributes = annotationClass.getMethods();
			/**
			 * [
			 * 		public abstract boolean java.lang.annotation.Annotation.equals(java.lang.Object), 
			 * 		public abstract java.lang.String java.lang.annotation.Annotation.toString(), 
			 * 		public abstract int java.lang.annotation.Annotation.hashCode(), 
			 * 		public abstract java.lang.Class java.lang.annotation.Annotation.annotationType()
			 * ]
			 */
			for(Method annotationAttribute : annotationAttributes) {
				String attributeName = annotationAttribute.getName();
				Object defaultValue = annotationAttribute.getDefaultValue();
				if(defaultValue != null && !this.attributes.containsKey(attributeName)) {
					if(defaultValue instanceof Annotation) {
						defaultValue = AnnotationAttributes.fromMap(AnnotationUtils.getAnnotationAttributes((Annotation)defaultValue, false, true));
					} else if(defaultValue instanceof Annotation[]) {
						Annotation[] realAnnotations = (Annotation[])defaultValue;
						AnnotationAttributes[] mappedAnnotations = new AnnotationAttributes[realAnnotations.length];
						for(int i = 0; i < realAnnotations.length; i++) {
							mappedAnnotations[i] = AnnotationAttributes.fromMap(AnnotationUtils.getAnnotationAttributes(realAnnotations[i], false, true));
						}
						defaultValue = mappedAnnotations;
					}
					this.attributes.put(attributeName, defaultValue);
				}
			}
		}
	}
	
//	public static void main(String[] args) {
//		AnnotationAttributes attributes = new AnnotationAttributes();
//		RecursiveAnnotationAttributesVisitor raav = new RecursiveAnnotationAttributesVisitor("org.fantasy.bean.annotation.TestAnno", attributes);
//		raav.visitEnd();
//	}
	
}
