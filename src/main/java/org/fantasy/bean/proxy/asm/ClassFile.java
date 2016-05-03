package org.fantasy.bean.proxy.asm;

public class ClassFile {

	
	private byte[] byteCodes;
	private String className;
	
	public ClassFile(byte[] byteCodes, String className) {
		this.byteCodes = byteCodes;
		this.className = className;
	}

	public byte[] getByteCodes() {
		return byteCodes;
	}

	public String getClassName() {
		return className;
	}

	public String toString() {
		return className;
	}

}
