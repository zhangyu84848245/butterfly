package org.fantasy.bean.asm;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.objectweb.asm.ClassReader;

public class ClassFileMetadataReader implements MetadataReader {

	
	private final ClassMetadata classMetadata;

	private final AnnotationMetadata annotationMetadata;
	
	public ClassFileMetadataReader(File file) throws IOException {
		BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
		ClassReader classReader = null;
		try {
			classReader = new ClassReader(in);
		} finally {
			in.close();
		}
		
		AnnotationMetadataVisitor annotationVisitor = new AnnotationMetadataVisitor();
		classReader.accept(annotationVisitor, ClassReader.SKIP_DEBUG);
		this.annotationMetadata = annotationVisitor;
		this.classMetadata = annotationVisitor;
	}

	public ClassMetadata getClassMetadata() {
		return classMetadata;
	}

	public AnnotationMetadata getAnnotationMetadata() {
		return annotationMetadata;
	}

	
}
