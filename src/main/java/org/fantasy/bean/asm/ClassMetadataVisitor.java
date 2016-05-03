package org.fantasy.bean.asm;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class ClassMetadataVisitor extends ClassVisitor implements ClassMetadata {

	private String className;
	private int access;
	private String superClassName;
	private String[] interfaces;
	
	public ClassMetadataVisitor() {
		super(Opcodes.ASM5);
	}

	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		this.className = name.replace('/', '.');
		this.access = access;
		if (superName != null) {
			this.superClassName = superName.replace('/', '.');
		}
		this.interfaces = new String[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			this.interfaces[i] = interfaces[i].replace('/', '.');
		}
	}
	
	public String getClassName() {
		return className;
	}

	public boolean isInterface() {
		return (access & Opcodes.ACC_INTERFACE) != 0;
	}

	public boolean isAbstract() {
		return (access & Opcodes.ACC_ABSTRACT) != 0;
	}

	public boolean isFinal() {
		return (access & Opcodes.ACC_FINAL) != 0;
	}

	public String getSuperClassName() {
		return superClassName;
	}

	public boolean hasSuperClass() {
		return (superClassName != null);
	}

	public String[] getInterfaceNames() {
		return interfaces;
	}

	public boolean isConcrete() {
		return !(isInterface() || isAbstract());
	}

}
