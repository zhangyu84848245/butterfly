package org.fantasy.bean.asm;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;




public class MethodMetadataVisitor extends MethodVisitor implements MethodMetadata {

	private final String name;
	private final int access;
	private final String declaringClassName;
	private final Set<MethodMetadata> methodMetadataSet;
	private final Map<String, AnnotationAttributes> attributeMap = new LinkedHashMap<String, AnnotationAttributes>(2);
	private String methodDescriptor;
	private String[] exceptionTypes;

	public MethodMetadataVisitor(String name, int access, String declaringClassName, String methodDescriptor, Set<MethodMetadata> methodMetadataSet, String[] exceptionTypes) {
		super(Opcodes.ASM5);
		this.access = access;
		this.name = name;
		this.declaringClassName = declaringClassName;
		this.methodMetadataSet = methodMetadataSet;
		this.methodDescriptor = methodDescriptor;
		this.exceptionTypes = exceptionTypes;
	}
	
	public String getMethodName() {
		return this.name;
	}

	public String getDeclaringClassName() {
		return this.declaringClassName;
	}

	public boolean isStatic() {
		return ((Opcodes.ACC_STATIC & access) != 0);
	}

	public boolean isFinal() {
		return ((Opcodes.ACC_FINAL & access) != 0);
	}
	
	public AnnotationVisitor visitAnnotation(String typeDescriptor, boolean visible) {
		String className = Type.getType(typeDescriptor).getClassName();
		this.methodMetadataSet.add(this);
		return new AnnotationAttributeVisitor(className, attributeMap, null);
	}

	public boolean isAnnotated(String annotationType) {
		return attributeMap.containsKey(annotationType);
	}

	public Map<String, Object> getAnnotationAttributes(String annotationType) {
		return this.attributeMap.get(annotationType);
	}

	public String getMethodDescriptor() {
		return methodDescriptor;
	}

	public String[] getExceptionTypes() {
		return exceptionTypes;
	}

	public int getAccess() {
		return access;
	}

}
