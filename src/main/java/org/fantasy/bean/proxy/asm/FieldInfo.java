package org.fantasy.bean.proxy.asm;

import org.objectweb.asm.Type;

public class FieldInfo {

	private int access;
	private String name;
	private Type type;
	private Object value;
	
	public FieldInfo(int access, String name, Type type, Object value) {
		this.access = access;
		this.name = name;
		this.type = type;
		this.value = value;
	}

	public int getAccess() {
		return access;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	public Object getValue() {
		return value;
	}
	
	
}
