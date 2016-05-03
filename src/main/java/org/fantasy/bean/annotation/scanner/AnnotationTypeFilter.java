package org.fantasy.bean.annotation.scanner;

import org.fantasy.bean.asm.AnnotationMetadata;

public class AnnotationTypeFilter implements TypeFilter {

	private AnnotationMetadata annotationMetadata;

	public AnnotationTypeFilter(AnnotationMetadata annotationMetadata) {
		this.annotationMetadata = annotationMetadata;
	}
	
	public boolean accept(String annotationType) {
		return annotationMetadata.getAnnotationTypes().contains(annotationType);
	}

	public AnnotationMetadata getAnnotationMetadata() {
		return annotationMetadata;
	}

}
