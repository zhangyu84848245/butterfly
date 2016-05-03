package org.fantasy.bean.proxy.asm;

import org.apache.log4j.Logger;
import org.fantasy.bean.proxy.asm.ProxyClassGenerator.HashEntry;
import org.fantasy.util.Constant;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class MethodMetadataWriter extends LocalVariablesSorter {

	private static final Logger LOG = Logger.getLogger(MethodMetadataWriter.class);

	private ClassMetadataWriter cmw;
	private MethodInfo methodInfo;
	
	@Deprecated
	public MethodMetadataWriter(ClassMetadataWriter cmw, MethodVisitor mv, int access, Signature sig, Type[] exceptionTypes, MethodInfo methodInfo) {
		super(access, Type.getArgumentTypes(sig.getDesc()), mv);
		this.cmw = cmw;
		this.methodInfo = methodInfo;
	}
	
	
	public MethodMetadataWriter(ClassMetadataWriter cmw, MethodVisitor mv, MethodInfo methodInfo) {
		super(methodInfo.getAccess(), Type.getArgumentTypes(methodInfo.getSignature().getDesc()), mv);
		this.cmw = cmw;
		this.methodInfo = methodInfo;
	}

	/**********************************************************************************************/
	/**
	 * NEW
	 * @param type
	 */
	public void newInstance(Type type) {
		visitTypeInsn(Opcodes.NEW, type);
	}
	
	public void newArray() {
		newArray(ProxyClassGenerator.OBJECT);
	}
	
	
	public void newArray(Type type) {
		if(AsmUtils.isPrimitive(type)) {
			mv.visitIntInsn(Opcodes.NEWARRAY, AsmUtils.NEWARRAY(type));
		} else {
			visitTypeInsn(Opcodes.ANEWARRAY, type);
		}
	}
	/**********************************************************************************************/
	private void visitTypeInsn(int opcode, Type type) {
		String desc;
		if(AsmUtils.isArray(type)) {
			desc = type.getDescriptor();
		} else {
			desc = type.getInternalName();
		}
		mv.visitTypeInsn(opcode, desc);
	}
	
	public void dup() {
		mv.visitInsn(Opcodes.DUP);
	}
	
	// invoke
	/**********************************************************************************************/
	// INVOKESTATIC
	public void invokeStatic(Type type, Signature sig) {
		invoke(Opcodes.INVOKESTATIC, type, sig);
	}
	/**
	 * INVOKESPECIAL
	 */
	public void invokeConstructor(Type type, Signature sig) {
		invoke(Opcodes.INVOKESPECIAL, type, sig);
	}
	/**
	 * INVOKEINTERFACE
	 */
	public void invokeInterface(Type type, Signature sig) {
		invoke(Opcodes.INVOKEINTERFACE, type, sig);
	}
	
	public void invokeVirtual(Type type, Signature sig) {
		invoke(Opcodes.INVOKEVIRTUAL, type, sig);
	}

	private void invoke(int opcode, Type type, Signature sig) {
		if( sig.getName().equals(Constant.CONSTRUCTOR_NAME) && ((opcode == Opcodes.INVOKEVIRTUAL) || (opcode == Opcodes.INVOKESTATIC)) ) {
			LOG.error("Invoke constructor error");
		}
		mv.visitMethodInsn(opcode, type.getInternalName(), sig.getName(), sig.getDesc(), opcode == Opcodes.INVOKEINTERFACE);
	}
	
	public void invokeSuper(MethodInfo mi) {
		// if abstract method then write a empty method
		if(AsmUtils.isAbstract(mi.getAccess())) {
			LOG.warn("Method " + mi.getSignature().getName() + " is a abstract method, no any invoke.");
		} else {
			loadThis();
			loadArgs();
			invokeSuper(mi.getSignature());
		}
	}
	
	public void invokeSuper(Signature sig) {
		invoke(Opcodes.INVOKESPECIAL, cmw.getClassInfo().getSuperType(), sig);
	}
	/**********************************************************************************************/
	// load
	public void loadThis() {
		if(AsmUtils.isStatic(methodInfo.getAccess()))
			throw new IllegalStateException("no 'this' pointer within static method");
		mv.visitVarInsn(Opcodes.ALOAD, 0);
	}

	public void loadArgs() {
		loadArgs(0, methodInfo.getArgumentTypes().length);
	}

	private void loadArgs(int fromArgs, int count) {
		int pos = methodInfo.getLocalOffset() + skipArgs(fromArgs);
		
		for(int i = 0; i < count; i++) {
			Type type = methodInfo.getArgumentTypes()[i + fromArgs];
			loadLocal(type, pos);
			pos += type.getSize();
		}
	}
	
	public void loadArgs(int index) {
		loadLocal(methodInfo.getArgumentTypes()[index], methodInfo.getLocalOffset() + skipArgs(index));
	}

	

	private int skipArgs(int numArgs) {
		int amount = 0;
		Type[] argumentTypes = methodInfo.getArgumentTypes();
		for(int i = 0; i < numArgs; i++) {
			amount += argumentTypes[i].getSize();
		}
		return amount;
	}

	/**********************************************************************************************/

	public void returnValue() {
		mv.visitInsn(methodInfo.getSignature().getReturnType().getOpcode(Opcodes.IRETURN));
	}

//	public void visitMaxs() {
//		if(!AsmUtils.isAbstract(methodInfo.getAccess())) {
//			mv.visitMaxs(0, 0);
//		}
//	}
	/**********************************************************************************************/
	// label
	public Label createLabel() {
		return new Label();
	}
	
	public void mark(Label label) {
		 mv.visitLabel(label);
	}
	
	/**********************************************************************************************/
	// field
	public void visitFieldInsn(int opcode, Type classType, String name, Type fieldType) {
		mv.visitFieldInsn(opcode, classType.getInternalName(), name, fieldType.getDescriptor());
	}
	public void getField(String name) {
		FieldInfo fi = cmw.getFieldInfo(name);
		int opcode = AsmUtils.isStatic(fi.getAccess()) ? Opcodes.GETSTATIC : Opcodes.GETFIELD;
		visitFieldInsn(opcode, cmw.getClassType(), name, fi.getType());
	}
	public void putField(String name) {
		FieldInfo fi = cmw.getFieldInfo(name);
		int opcode = AsmUtils.isStatic(fi.getAccess()) ? Opcodes.PUTSTATIC : Opcodes.PUTFIELD;
		visitFieldInsn(opcode, cmw.getClassType(), name, fi.getType());
	}
	/**********************************************************************************************/
	public void jump(int opcode, Label label) {
		mv.visitJumpInsn(opcode, label);
	}
	
	public void createArgsArray() {
		push(methodInfo.getArgumentTypes().length);
		newArray();
		Type[] argumentTypes = methodInfo.getArgumentTypes();
		for(int i = 0; i < argumentTypes.length;i++) {
			dup();
			push(i);
			loadArgs(i);
			Type type = argumentTypes[i];
			if(AsmUtils.isPrimitive(type)) {
				char desc = type.getDescriptor().charAt(0);
				String internalName = AsmUtils.getInternalName(desc);
				mv.visitMethodInsn(
						Opcodes.INVOKESTATIC, 
						internalName, 
						"valueOf", 
						"(" + desc + ")L" + internalName + ";", 
						Opcodes.INVOKEVIRTUAL == Opcodes.INVOKEINTERFACE
				);
			}
			
			aastore();
		}
	}
	
	public void aaload() {
		mv.visitInsn(Opcodes.AALOAD);
	}
	
	public void aaload(int index) {
		push(index);
		aaload();
	}
	
	public void aastore() {
		mv.visitInsn(Opcodes.AASTORE);
	}
	
	
	public void push(int i) {
		if(i < -1) {
			mv.visitLdcInsn(new Integer(i));
		} else if(i <= 5) {
			mv.visitInsn(AsmUtils.ICONST(i));
		} else if (i <= Byte.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.BIPUSH, i);
        } else if (i <= Short.MAX_VALUE) {
            mv.visitIntInsn(Opcodes.SIPUSH, i);
        } else {
            mv.visitLdcInsn(new Integer(i));
        }
	}

	public void tryCacthException(Block block, Type exceptionType) {
		if(block == null)
			throw new IllegalArgumentException("block");
		mv.visitTryCatchBlock(block.getStart(), block.getEnd(), block.getHandler(), exceptionType.getInternalName());
	}
	
	public Local newLocal(Type type) {
		return new Local(type, newLocal(type.getSize()));
	}
	
	
	public void storeLocal(Local local) {
		storeLocal(local.getType(), local.getIndex());
	}
	
	private void storeLocal(Type type, int pos) {
		mv.visitVarInsn(type.getOpcode(Opcodes.ISTORE), pos);
	}
	
	public void loadLocal(Local local) {
		loadLocal(local.getType(), local.getIndex());
	}
	
	private void loadLocal(Type type, int pos) {
		mv.visitVarInsn(type.getOpcode(Opcodes.ILOAD), pos);
	}
	
	public void pop() {
		mv.visitInsn(Opcodes.POP);
	}
	
	public void checkCast(Type type) {
		if(!type.equals(ProxyClassGenerator.OBJECT)) {
			visitTypeInsn(Opcodes.CHECKCAST, type);
		}
	}
	
	
	public void checkCast(String internalName) {
		mv.visitTypeInsn(Opcodes.CHECKCAST, internalName);
	}
	
	public void aThrow() {
		mv.visitInsn(Opcodes.ATHROW);
	}
	
	
	public void push(String value) {
		mv.visitLdcInsn(value);
	}
	
	
	public void instanceOf(Type type) {
		visitTypeInsn(Opcodes.INSTANCEOF, type);
	}
	
	
	public void visitMaxs() {
		if (!AsmUtils.isAbstract(methodInfo.getAccess())) {
			mv.visitMaxs(0, 0);
		}
	}
	
	public void aconstNull() {
		mv.visitInsn(Opcodes.ACONST_NULL);
	}
	
	public void switchProcess(int[] keys, SwitchProcessor processor) {
		int len = keys.length;
		Label[] labels = new Label[len];
		for(int i = 0; i < len; i++)
			labels[i] = createLabel();
		Label switchLabel = createLabel();
		mv.visitLookupSwitchInsn(switchLabel, keys, labels);
		for(int i = 0; i < len; i++) {
			mark(labels[i]);
			Label next = null;
			if(i == len - 1) {
				next = switchLabel;
			} else {
				next = labels[i + 1];
			}	
			processor.processCase(i, next);
		}
		processor.processDefault(switchLabel);
	}

	public void tableSwitch(int len, SwitchProcessor processor) {
		Label[] labels = new Label[len];
		for(int i = 0; i < len; i++)
			labels[i] = createLabel();
		Label switchLabel = createLabel();
		mv.visitTableSwitchInsn(1, len, switchLabel, labels);
		for(int i = 0; i < len; i++) {
			processor.processCase(i, labels[i]);
		}
		processor.processDefault(switchLabel);
	}

	public void staticGet(String owner, String name, String desc) {
		mv.visitFieldInsn(Opcodes.GETSTATIC, name, "TYPE", "Ljava/lang/Class;");
	}
}
