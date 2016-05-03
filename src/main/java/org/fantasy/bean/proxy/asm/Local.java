package org.fantasy.bean.proxy.asm;

import org.objectweb.asm.Type;

public class Local {

	private Type type;
	private int index;
	
	public Local(Type type, int index) {
		this.type = type;
		this.index = index;
	}

	public Type getType() {
		return type;
	}

	public int getIndex() {
		return index;
	}

}
