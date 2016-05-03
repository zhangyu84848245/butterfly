package org.fantasy.bean.proxy.asm;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * 类的生成器
 * @author Administrator
 *
 */
public class ClassMetadataWriter extends ClassVisitor {

	private ClassInfo classInfo;
	private Map<String, FieldInfo> fieldMap = new HashMap<String, FieldInfo>();
//	private MethodGenerator staticInit;


	public ClassMetadataWriter(int api, int flag) {
		super(api, new ClassWriter(flag));
	}
	
	public void visitClass(int version, int access, String className, Type superType, Type[] interfaceTypes, String source) {
		Type classType = AsmUtils.getType(className);
		this.classInfo = new ClassInfo(classType, superType, access, interfaceTypes);
		cv.visit(
				version, 
				access, 
				classType.getInternalName(), 
				null, 
				superType == null ? null : superType.getInternalName(), 
				AsmUtils.toInternalNames(classInfo.getInterfaceTypes())
		);
		if(source != null)
			cv.visitSource(source, null);
	}
	
	
	public FieldVisitor visitField(int access, String fieldName, Type fieldType, Object value) {
		return visitField(access, fieldName, fieldType, null, value);
	}
	
	
	public FieldVisitor visitField(int access, String fieldName, Type fieldType, String signature, Object value) {
		FieldInfo fieldInfo = fieldMap.get(fieldName);
		if(fieldInfo != null)
			throw new IllegalArgumentException("Field \"" + fieldName + "\" has been declared");
		fieldInfo = new FieldInfo(access, fieldName, fieldType, value);
		fieldMap.put(fieldName, fieldInfo);
		return cv.visitField(access, fieldName, fieldType.getDescriptor(), signature, value);
	}
//	fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "$$_THREADLOCAL_$$", "Ljava/lang/ThreadLocal;", "", null);
	public int getAccess() {
		return classInfo.getAccess();
	}
	
	
//	public MethodGenerator getStaticInit() {
//		if(staticInit == null) {
//			this.staticInit = visitMethod(Opcodes.ACC_STATIC, STATIC_SIG, null, null);
//		}
//		return staticInit;
//	}
	
	public Type getClassType() {
		return classInfo.getClassType();
	}
	
	public FieldInfo getFieldInfo(String name) {
		FieldInfo field = fieldMap.get(name);
		if(field == null)
			throw new IllegalArgumentException("Field " + name + " is not declared in " + getClassType().getClassName());
		return field;
	} 
	
	
	public MethodMetadataWriter visitMethod(MethodInfo mi) {
		MethodVisitor mv = cv.visitMethod(
				mi.getAccess(), 
				mi.getSignature().getName(), 
				mi.getSignature().getDesc(), 
				null, 
				AsmUtils.toInternalNames(mi.getExceptionTypes())
		);
		return new MethodMetadataWriter(this, mv, mi);
	}
	
	@Deprecated
	public MethodMetadataWriter visitMethod(int access, Signature sig, Type[] exceptionTypes, MethodInfo mi) {
		MethodVisitor mv = cv.visitMethod(
				access, 
				sig.getName(), 
				sig.getDesc(), 
				null, 
				AsmUtils.toInternalNames(exceptionTypes)
		);
//		if(sig == STATIC_SIG) {
//			MethodVisitor wrapped = new MethodVisitor(Opcodes.ASM5, mv) {
//				public void visitMaxs(int maxStack, int maxLocals) {
//		
//				}
//				public void visitInsn(int insn) {
//					if (insn != Opcodes.RETURN) {
//						super.visitInsn(insn);
//					}
//				}
//			};
//			this.staticInit = new AsmMethodWriter(this, wrapped, access, sig, exceptionTypes);
//		} else {
			return new MethodMetadataWriter(this, mv, access, sig, exceptionTypes, mi);
//		}
	}

	public ClassInfo getClassInfo() {
		return classInfo;
	}
	
	
	public byte[] toByteArray() {
		cv.visitEnd();
		return ((ClassWriter)cv).toByteArray();
	}
}
