package org.fantasy.bean.asm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.fantasy.util.AnnotationUtils;




public class AnnotationAttributeVisitor extends RecursiveAnnotationAttributesVisitor {

	private final String annotationType;
	private final Map<String, AnnotationAttributes> attributesMap;
	private final Map<String, Set<String>> metaAnnotationMap;
	
	public AnnotationAttributeVisitor(String annotationType, Map<String, AnnotationAttributes> attributesMap, Map<String, Set<String>> metaAnnotationMap) {
		super(annotationType, new AnnotationAttributes());
		this.annotationType = annotationType;
		this.attributesMap = attributesMap;
		this.metaAnnotationMap = metaAnnotationMap;
	}

	protected void doVisitEnd(Class<?> annotationClass) {
		super.doVisitEnd(annotationClass);
		this.attributesMap.put(this.annotationType, this.attributes);
		registerMetaAnnotations(annotationClass);
	}

	private void registerMetaAnnotations(Class<?> annotationClass) {
		Set<String> metaAnnotationTypeNames = new LinkedHashSet<String>();
		for(Annotation metaAnnotation : annotationClass.getAnnotations()) {
			metaAnnotationTypeNames.add(metaAnnotation.annotationType().getName());
			if(Modifier.isPublic(metaAnnotation.annotationType().getModifiers())) {
				if(!this.attributesMap.containsKey(metaAnnotation.annotationType().getName())) {
					this.attributesMap.put(metaAnnotation.annotationType().getName(), AnnotationAttributes.fromMap(AnnotationUtils.getAnnotationAttributes(metaAnnotation, true, true)));
				}
				
				for(Annotation metaMetaAnnotation : metaAnnotation.annotationType().getAnnotations()) {
					metaAnnotationTypeNames.add(metaMetaAnnotation.annotationType().getName());
				}
			}
		}
		if (this.metaAnnotationMap != null) {
			this.metaAnnotationMap.put(annotationClass.getName(), metaAnnotationTypeNames);
		}
	}
}
