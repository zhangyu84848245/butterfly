package org.fantasy.bean.proxy.asm;

import org.objectweb.asm.Type;

public class ClassInfo {
	private Type classType;
	private Type superType;
	private int access;
	private Type[] interfaceTypes;
	
	public ClassInfo(Type classType, Type superType, int access, Type[] interfaceTypes) {
		this.classType = classType;
		this.superType = superType;
		this.access = access;
		this.interfaceTypes = interfaceTypes;
	}

	public Type getClassType() {
		return classType;
	}

	public Type getSuperType() {
		return superType;
	}

	public int getAccess() {
		return access;
	}

	public Type[] getInterfaceTypes() {
		return interfaceTypes;
	}

}
