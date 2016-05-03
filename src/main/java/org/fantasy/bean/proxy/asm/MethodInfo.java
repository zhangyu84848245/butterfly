package org.fantasy.bean.proxy.asm;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
public class MethodInfo {

	private int access;
	private Signature signature;
	private Type[] argumentTypes;
	private Type[] exceptionTypes;
	private int pos = -1;
	private ClassInfo classInfo;
	private int localOffset;
	
	public MethodInfo(int access, Signature signature, Type[] argumentTypes, Type[] exceptionTypes) {
		this(-1, access, signature, argumentTypes, exceptionTypes);
	}
	
	public MethodInfo(int pos, int access, Signature signature, Type[] argumentTypes, Type[] exceptionTypes) {
		this.pos = pos;
		this.access = access;
		this.signature = signature;
		this.argumentTypes = argumentTypes;
		this.exceptionTypes = exceptionTypes;
		this.localOffset = (access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
	}
	
	public MethodInfo(ClassInfo classInfo, int access, Signature signature, Type[] exceptionTypes) {
		this(-1, access, signature, Type.getArgumentTypes(signature.getDesc()), exceptionTypes);
		this.classInfo = classInfo;
	}

	public int getAccess() {
		return access;
	}

	public Signature getSignature() {
		return signature;
	}

	public Type[] getArgumentTypes() {
		return argumentTypes;
	}

	public Type[] getExceptionTypes() {
		return exceptionTypes;
	}

	public int getPos() {
		return pos;
	}

	public int getLocalOffset() {
		return localOffset;
	}

}
