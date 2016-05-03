package org.fantasy.bean.proxy.asm;

import java.io.FileOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.fantasy.bean.proxy.MethodInterceptor;
import org.fantasy.bean.proxy.MethodInterceptorAdapter;
import org.fantasy.bean.proxy.NameGeneratorStrategy;
import org.fantasy.bean.proxy.ProxyInvoker;
import org.fantasy.bean.proxy.ProxyNameGeneratorStrategy;
import org.fantasy.util.Constant;
import org.fantasy.util.ReflectionUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;




public class ProxyClassGenerator implements Assemble {

	private NameGeneratorStrategy generator = new ProxyNameGeneratorStrategy();
	private String proxyClassName;
	private Class<?> targetClass;
	private List<MethodInfo> methods = new ArrayList<MethodInfo>();
	private List<MethodInfo> constructors = new ArrayList<MethodInfo>();
	private ClassMetadataWriter cmw;
	private MethodMetadataWriter staticInit;
	// 静态初始中的try catch
	private Block staticInitTryCatchBlock;
	private HashEntry[] hashCodes;
	private boolean isInterface;

	
	private static List<MethodInfo> objectClassMethods = new ArrayList<MethodInfo>();
	
	static {
		try {
			Class<?> objectClass = Object.class;
			List<Method> list = new ArrayList<Method>();
			list.add(objectClass.getDeclaredMethod("hashCode"));
			list.add(objectClass.getDeclaredMethod("toString"));
			list.add(objectClass.getDeclaredMethod("equals", Object.class));
			list.add(objectClass.getDeclaredMethod("clone"));
			for(int i = 0; i < list.size(); i++)
				objectClassMethods.add(AsmUtils.getMethodInfo(list.get(i), i));
			objectClassMethods = Collections.unmodifiableList(objectClassMethods);

			// gc
			list.clear();
			list = null;
		} catch (Exception e) {
			throw new Error(e);
		}
	}

	public static final int PRIVATE_STATIC_FINAL = Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL;
	public static final int PUBLIC_STATIC = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
	public static final int PRIVATE_FINAL = Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL;
	public static final int PUBLIC_VARARGS = Opcodes.ACC_PUBLIC | Opcodes.ACC_VARARGS;

	public static final Type STRING = Type.getType(String.class);
	public static final Type THREAD_LOCAL = Type.getType(ThreadLocal.class);
	public static final Type THROWABLE = Type.getType(Throwable.class);
	public static final Type SIGNATURE = Type.getType(Signature.class);
	
	public static final Type METHOD = Type.getType(Method.class);
	public static final Type METHOD_INTERCEPTOR = Type.getType(MethodInterceptor.class);
	public static final Type OBJECT_ARRAY = Type.getType(Object[].class);
	public static final Type OBJECT = Type.getType(Object.class);
	public static final Type CLASS = Type.getType(Class.class);
	public static final Type PROXY_INVOKER = Type.getType(ProxyInvoker.class);
	public static final Type RUNTIME_EXCEPTION = Type.getType(RuntimeException.class);
	public static final Type ERROR = Type.getType(Error.class);
	
	
	// constructor signature
	public static final Signature EMPTY_CONSTRUCTOR = new Signature("<init>", "()V");
	public static final Signature STATIC_CONSTRUCTOR = new Signature("<clinit>", "()V");
	
	public static final Signature GET_INTERCEPTOR = new Signature("getInterceptor", "(Ljava/lang/Object;)V");
	public static final Signature SET_INTERCEPTOR = new Signature("setInterceptor", "(Lorg/fantasy/bean/proxy/MethodInterceptor;)V");
	public static final Signature GET_INDEX = new Signature("getIndex", "(Lorg/fantasy/bean/proxy/asm/Signature;)I");
	public static final Signature INVOKE0 = new Signature("invoke0", "(I[Ljava/lang/Object;)Ljava/lang/Object;");
	public static final Signature INVOKE = new Signature("invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;");
	public static final Signature METHOD_INVOKE = new Signature("invoke", "(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
	public static final Signature GET_SIGNATURE = new Signature("getSignature", "(Ljava/lang/reflect/Member;)Lorg/fantasy/bean/proxy/asm/Signature;");

	public static final Signature INTERCEPT = new Signature("intercept", "(Lorg/fantasy/bean/proxy/ProxyInvoker;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;");
//	public static final Signature THROWABLE_CONSTRUCTOR = new Signature("<init>", "(Ljava/lang/Throwable;)V");
	public static final Signature FOR_NAME = new Signature("forName", "(Ljava/lang/String;)Ljava/lang/Class;");
	public static final Signature GET_DECLARED_METHOD = new Signature("getDeclaredMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
	public static final Signature GET_DECLARING_CLASS = new Signature("getDeclaringClass", "()Ljava/lang/Class;");
	public static final Signature IS_INTERFACE = new Signature("isInterface", "()Z");
	
	


	
	public static final Signature TO_STRING = new Signature("toString", "()Ljava/lang/String;");
	public static final Signature HASH_CODE = new Signature("hashCode", "()I");
	public static final Signature EQUALS = new Signature("equals", "(Ljava/lang/Object;)Z");
	
	public ProxyClassGenerator(Class<?> targetClass) {
		if((targetClass.getModifiers() & Modifier.FINAL)!= 0)
			throw new IllegalArgumentException(targetClass.getName() + " is final class");
		this.targetClass = targetClass;
		this.proxyClassName = generator.getClassName(targetClass.getName());
		introspect();
		this.cmw = new ClassMetadataWriter(Opcodes.ASM5, ClassWriter.COMPUTE_FRAMES);
		isInterface = targetClass.isInterface();
	}
	
	private void introspect() {
		List<Method> allMethods = new ArrayList<Method>();
		ReflectionUtils.getAllMethods(targetClass, allMethods);
		Set<Signature> unique = new HashSet<Signature>();
		for(int i = 0; i < allMethods.size(); i++) {
			Method method = allMethods.get(i);
			int modifier = method.getModifiers();
			if((modifier & PRIVATE_STATIC_FINAL) != 0)
				continue;
			Signature sig = AsmUtils.getSignature(method);
			if(unique.contains(sig))
				continue;
			unique.add(sig);
			MethodInfo mi = new MethodInfo(
					i, 
					modifier, 
					sig, 
					AsmUtils.getTypes(method.getParameterTypes()), 
					AsmUtils.getTypes(method.getExceptionTypes())
			);
			methods.add(mi);
		}
		Collections.sort(methods, new Comparator<MethodInfo>() {
			public int compare(MethodInfo o1, MethodInfo o2) {
				return o1.getSignature().compareTo(o2.getSignature());
			}
		});
		
		int len = methods.size() + objectClassMethods.size();
		hashCodes = new HashEntry[len];
		int i = 0;
		List<MethodInfo> list = methods;
		do {
			for(Iterator<MethodInfo> iterator = list.iterator();iterator.hasNext();) {
				MethodInfo mi = iterator.next();
				Signature sig = mi.getSignature();
				hashCodes[i++] = new HashEntry(sig.toString().hashCode(), sig.toString());
			}
			list = objectClassMethods;
		} while(i < len);

		Arrays.sort(hashCodes, new Comparator<HashEntry>() {
			public int compare(HashEntry o1, HashEntry o2) {
				int h1 = o1.hashCode;
				int h2 = o2.hashCode;
				if(h1 == h2)
					return o1.desc.compareTo(o2.desc);
				return h1 < h2 ? -1 : 1;
			}
		});
		// gc
		unique.clear();
		unique = null;
		allMethods.clear();
		allMethods = null;
		if(!isInterface) {
			Constructor<?>[] cons = targetClass.getDeclaredConstructors();
			for(Constructor<?> constructor : cons)
				constructors.add(AsmUtils.getMethodInfo(constructor));
		}
	}
	
	private void writeDefaultConstructor() {
		MethodMetadataWriter mmw = null;
		MethodInfo mi = new MethodInfo(Opcodes.ACC_PUBLIC, EMPTY_CONSTRUCTOR, null, null);
		// 构造方法
		mmw = cmw.visitMethod(mi);
		mmw.visitCode();
		mmw.loadThis();
		mmw.invokeConstructor(OBJECT, EMPTY_CONSTRUCTOR);
		mmw.loadThis();
		mmw.invokeStatic(cmw.getClassType(), GET_INTERCEPTOR);
		mmw.returnValue();
		mmw.visitMaxs(0, 0);
		mmw.visitEnd();
		mmw = null;
		mi = null;
	}
	
	public void writeConstructor0() {
		MethodMetadataWriter mmw = null;
		for(Iterator<MethodInfo> iterator = constructors.iterator();iterator.hasNext();) {
			MethodInfo mi = iterator.next();
			// 构造方法
			mmw = cmw.visitMethod(mi);
			mmw.visitCode();
			mmw.loadThis();
			mmw.loadArgs();
			mmw.invokeConstructor(cmw.getClassInfo().getSuperType(), mi.getSignature());
			mmw.loadThis();
			mmw.invokeStatic(cmw.getClassType(), GET_INTERCEPTOR);
			mmw.returnValue();
			mmw.visitMaxs(0, 0);
			mmw.visitEnd();
		}
		mmw = null;
	}
	
	public void writeConstructor() {
		if(isInterface)
			writeDefaultConstructor();
		else
			writeConstructor0();
	}
	
	
	public void writeClassName() {
		Type superType = null;
		Type classType = Type.getType(targetClass);
		List<Type> interfaces = new ArrayList<Type>();
		interfaces.add(PROXY_INVOKER);
		if(targetClass.isInterface()) {
			interfaces.add(classType);
			superType = OBJECT;
		} else {
			superType = classType;
		}
		cmw.visitClass(
				Opcodes.V1_7,
				Opcodes.ACC_PUBLIC, 
				proxyClassName, 
				superType,
				interfaces.toArray(new Type[interfaces.size()]), 
				Constant.SOURCE_FILE
		);
	}

	public void writeDeclaredFields() {
		FieldVisitor fv = null;
		fv = cmw.visitField(PRIVATE_STATIC_FINAL, "$$_THREADLOCAL_$$", THREAD_LOCAL, "Ljava/lang/ThreadLocal<Lorg/fantasy/bean/proxy/MethodInterceptor;>;", null);
		fv.visitEnd();
		fv = cmw.visitField(Opcodes.ACC_PRIVATE, "interceptor", METHOD_INTERCEPTOR, null);
		fv.visitEnd();
		fv = cmw.visitField(PRIVATE_STATIC_FINAL, "EMPTY_ARGS", OBJECT_ARRAY, null);
		fv.visitEnd();
		fv = cmw.visitField(Opcodes.ACC_PRIVATE, "bound", Type.BOOLEAN_TYPE, null);
		fv.visitEnd();
		fv = null;
	}
	
	
	public void writeStaticInit() {
		staticInit = cmw.visitMethod(Opcodes.ACC_STATIC, STATIC_CONSTRUCTOR, null, null);
		staticInit.visitCode();
		staticInitTryCatchBlock = new Block(staticInit);
		staticInit.visitTryCatchBlock(staticInitTryCatchBlock.getStart(), staticInitTryCatchBlock.getEnd(), staticInitTryCatchBlock.getHandler(), "java/lang/Throwable");
		staticInit.newInstance(THREAD_LOCAL);
		staticInit.dup();
		staticInit.invokeConstructor(THREAD_LOCAL, EMPTY_CONSTRUCTOR);
		staticInit.putField("$$_THREADLOCAL_$$");
		staticInit.push(0);
		staticInit.newArray();
		staticInit.putField("EMPTY_ARGS");
		staticInit.visitLabel(staticInitTryCatchBlock.getStart());
		Type type = null;
		if(isInterface) {
			Type[] interfaces = cmw.getClassInfo().getInterfaceTypes();
			assert interfaces.length == 2;
			for(Type interfaceType : interfaces) {
				if(!interfaceType.equals(PROXY_INVOKER)) {
					type = interfaceType;
				}
			}
		} else {
			type = cmw.getClassInfo().getSuperType();
		}
		staticInit.push(type.getClassName());
		staticInit.invokeStatic(CLASS, FOR_NAME);
//		Local classLocal = staticInit.newLocal(CLASS);
//		staticInit.storeLocal(classLocal);
//		staticInit.loadLocal(classLocal);
		staticInit.visitVarInsn(Opcodes.ASTORE, 0);
		// 未写完
	}
	
	private byte[] generateByteCode() {
		writeClassName();
		writeDeclaredFields();
		writeStaticInit();
		writeConstructor();
		writeSetInterceptor();
		writeGetInterceptor();
		writeMethods();
		if(!isInterface) {
			writeGetIndex();
			writeInvoke0();
		}
		writeInvoke();
		byte[] byteCodes = cmw.toByteArray();
//		FileOutputStream fos;
//		try {
//			fos = new FileOutputStream("E:\\new_ws\\butterfly\\target\\classes\\org\\fantasy\\bean\\proxy\\ProxyTest.class");
//			fos.write(byteCodes);
//	        fos.close();
//		} catch (Exception e) {
//
//		}
		return byteCodes;
	}

	
	private void setField(MethodInfo mi, String methodField, String methodProxyField) {
		Signature sig = mi.getSignature();
		staticInit.visitVarInsn(Opcodes.ALOAD, 0);
		staticInit.push(sig.getName());		
		int argsLength = mi.getArgumentTypes().length;			
		staticInit.push(argsLength);
		staticInit.newArray(CLASS);
		for(int i = 0; i < argsLength; i++) {
			staticInit.dup();
			staticInit.push(i);
			Type type = mi.getArgumentTypes()[i];
			if(AsmUtils.isPrimitive(type)) {
				char desc = type.getDescriptor().charAt(0);
				staticInit.visitFieldInsn(Opcodes.GETSTATIC, AsmUtils.getInternalName(desc), "TYPE", "Ljava/lang/Class;");
			} else {
				staticInit.visitLdcInsn(type);
			}
			staticInit.aastore();
		}
		staticInit.invokeVirtual(CLASS, GET_DECLARED_METHOD);
		staticInit.putField(methodField);
//		staticInit.visitVarInsn(Opcodes.ALOAD, 0);
//		staticInit.push(sig.getDesc());
//		staticInit.push(sig.getName());
//		staticInit.invokeStatic(METHOD_PROXY, CREATE);
//		staticInit.putField(methodProxyField);
	}
	
	private void declareField(String methodField, String methodProxyField) {
		// 写字段
		cmw.visitField(PRIVATE_STATIC_FINAL, methodField, METHOD, null);
//		cmw.visitField(PRIVATE_STATIC_FINAL, methodProxyField, METHOD_PROXY, null);
	}
	
	
	private void declareMethod(MethodInfo mi) {
		MethodMetadataWriter mmw = null;
		Signature sig = mi.getSignature();
		Signature proxySig = new Signature(generator.getMethodProxyName(sig.getName()), sig.getDesc());
		String methodField = generator.getFieldName(sig.getName(), mi.getPos());
		String methodProxyField = generator.getFieldProxyName(sig.getName(), mi.getPos());
		declareField(methodField, methodProxyField);

		// method proxy
		if(!isInterface) {
			mmw = cmw.visitMethod(Opcodes.ACC_FINAL, proxySig, mi.getExceptionTypes(), mi);
			mmw.visitCode();
			mmw.loadThis();
			mmw.loadArgs();
			mmw.invokeSuper(sig);
			mmw.returnValue();
			mmw.visitMaxs(0, 0);
			mmw.visitEnd();
			mmw = null;
		}
		// method
		int access = (mi.getAccess() | Opcodes.ACC_FINAL) & ~Opcodes.ACC_NATIVE;
		if(isInterface)
			access &= ~Opcodes.ACC_ABSTRACT;
		mmw = cmw.visitMethod(access, sig, mi.getExceptionTypes(), mi);
		mmw.visitCode();
		Block tryCatchBlock = new Block(mmw);
		mmw.tryCacthException(tryCatchBlock, THROWABLE);
		mmw.loadThis();
		mmw.getField("bound");
		Label boundLabel = mmw.createLabel();
		mmw.jump(Opcodes.IFNE, boundLabel);
		mmw.loadThis();
		mmw.invokeStatic(cmw.getClassType(), GET_INTERCEPTOR);
		mmw.mark(boundLabel);
		mmw.loadThis();
		mmw.getField("interceptor");
		Local interceptorLocal = mmw.newLocal(METHOD_INTERCEPTOR);
		mmw.storeLocal(interceptorLocal);
		mmw.loadLocal(interceptorLocal);
		Label ifNotNullLabel = mmw.createLabel();
		mmw.jump(Opcodes.IFNULL, ifNotNullLabel);
		tryCatchBlock.start();
		mmw.loadLocal(interceptorLocal);
		mmw.loadThis();
		mmw.getField(methodField);
		if (sig.getArgumentTypes().length == 0) {
			mmw.getField("EMPTY_ARGS");
		} else {
			mmw.createArgsArray();
		}
//		mmw.getField(methodProxyField);
		mmw.invokeInterface(METHOD_INTERCEPTOR, INTERCEPT);
		if (sig.getReturnType().equals(Type.VOID_TYPE)) {
			mmw.pop();
		} else {
			// 处理基本类型
			Type type = sig.getReturnType();
			if (AsmUtils.isPrimitive(type)) {
				String className = type.getClassName();
				char desc = AsmUtils.PRIMITIVE_DESC.get(className);
				String internalName = AsmUtils.getInternalName(desc);
				mmw.checkCast(internalName);
				mmw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, internalName, className + "Value", "()" + desc, false);
				// mmw.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean");
				// mmw.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
				// "java/lang/Boolean", "booleanValue", "()Z", false);
			} else {
				mmw.checkCast(type);
			}
		}
		tryCatchBlock.end();
		mmw.returnValue();

		tryCatchBlock.handler();
		Local throwableLocal = mmw.newLocal(THROWABLE);
		mmw.storeLocal(throwableLocal);
		mmw.newInstance(RUNTIME_EXCEPTION);
		mmw.dup();
		mmw.loadLocal(throwableLocal);
		mmw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V", false);
		mmw.aThrow();
		mmw.mark(ifNotNullLabel);
		if(!isInterface) {
			mmw.loadThis();
			mmw.loadArgs();
			mmw.invokeConstructor(cmw.getClassInfo().getSuperType(), sig);
			mmw.returnValue();
		} else {
			mmw.newInstance(ERROR);
			mmw.dup();
			mmw.invokeConstructor(ERROR, EMPTY_CONSTRUCTOR);
			mmw.aThrow();
		}
		mmw.visitMaxs(0, 0);
		mmw.visitEnd();
		mmw = null;
		setField(mi, methodField, methodProxyField);
	}

	public void writeMethods() {
		for(Iterator<MethodInfo> iterator = methods.iterator();iterator.hasNext();) {
			MethodInfo mi = iterator.next();
			declareMethod(mi);
		}
		
		staticInit.visitLdcInsn(Type.getType("Ljava/lang/Object;"));
		staticInit.visitVarInsn(Opcodes.ASTORE, 0);
		
		for(Iterator<MethodInfo> iterator = objectClassMethods.iterator();iterator.hasNext();) {
			MethodInfo mi = iterator.next();
			declareMethod(mi);
		}

		staticInit.visitLabel(staticInitTryCatchBlock.getEnd());
		Label l3 = new Label();
		staticInit.jump(Opcodes.GOTO, l3);
		staticInit.visitLabel(staticInitTryCatchBlock.getHandler());
		staticInit.visitVarInsn(Opcodes.ASTORE, 0);
		staticInit.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
		staticInit.dup();
		staticInit.visitVarInsn(Opcodes.ALOAD, 0);
		staticInit.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V", false);
		staticInit.aThrow();
		staticInit.visitLabel(l3);
		staticInit.visitInsn(Opcodes.RETURN);
		staticInit.visitMaxs(0, 0);
		staticInit.visitEnd();
		// gc
		staticInit = null;
		staticInitTryCatchBlock = null;
	}

	public void writeSetInterceptor() {
		MethodMetadataWriter mmw = null;
		MethodInfo mi = new MethodInfo(PRIVATE_STATIC_FINAL, new Signature("setInterceptor", "(Lorg/fantasy/bean/proxy/Callback;)V"), Type.getArgumentTypes("(Lorg/fantasy/bean/proxy/Callback;)V"), null);
		mmw = cmw.visitMethod(PRIVATE_STATIC_FINAL, SET_INTERCEPTOR, null, mi);
		mmw.visitCode();
		mmw.getField("$$_THREADLOCAL_$$");
		mmw.loadArgs(0);
		mmw.invokeVirtual(THREAD_LOCAL, new Signature("set", "(Ljava/lang/Object;)V"));
		mmw.returnValue();
		mmw.visitMaxs(0, 0);
		mmw.visitEnd();
		mmw = null;
		mi = null;
	}

	public void writeGetInterceptor() {
		MethodMetadataWriter mmw = null;
		MethodInfo mi = new MethodInfo(PRIVATE_STATIC_FINAL, GET_INTERCEPTOR, Type.getArgumentTypes("(Ljava/lang/Object;)V"), null);
		mmw = cmw.visitMethod(PRIVATE_STATIC_FINAL, new Signature("getInterceptor", "(Ljava/lang/Object;)V"), null, mi);
		mmw.visitCode();
		mmw.loadArgs(0);
		mmw.instanceOf(cmw.getClassType());
		Label instanceOfLabel = mmw.createLabel();
		mmw.jump(Opcodes.IFEQ, instanceOfLabel);
		mmw.loadArgs(0);
		mmw.checkCast(cmw.getClassType());
		Local args0Local = mmw.newLocal(cmw.getClassType());
		mmw.storeLocal(args0Local);
		mmw.loadLocal(args0Local);
		mmw.getField("bound");
		Label boundLabel = mmw.createLabel();
		mmw.jump(Opcodes.IFEQ, boundLabel);
		mmw.returnValue();
		mmw.mark(boundLabel);
		mmw.getField("$$_THREADLOCAL_$$");
		mmw.invokeVirtual(THREAD_LOCAL, new Signature("get", "()Ljava/lang/Object;"));
		mmw.checkCast(METHOD_INTERCEPTOR);
		Local objLocal = mmw.newLocal(METHOD_INTERCEPTOR);
		mmw.storeLocal(objLocal);
		mmw.loadLocal(objLocal);
		mmw.jump(Opcodes.IFNULL, instanceOfLabel);
		mmw.loadLocal(args0Local);
		Type interceptorAdapterType = Type.getType(MethodInterceptorAdapter.class);
		mmw.newInstance(interceptorAdapterType);
		mmw.dup();
		mmw.loadLocal(objLocal);
		mmw.invokeConstructor(interceptorAdapterType, new Signature("<init>", "(Lorg/fantasy/bean/proxy/MethodInterceptor;)V"));
		mmw.putField("interceptor");
		mmw.loadLocal(args0Local);
		mmw.push(1);
		mmw.putField("bound");
		mmw.returnValue();
		mmw.mark(instanceOfLabel);
		mmw.returnValue();
		mmw.visitMaxs(0, 0);
		mmw.visitEnd();
		mmw = null;
		mi = null;
	}

	public void writeGetIndex() {
		MethodMetadataWriter mmw = null;
		MethodInfo mi = new MethodInfo(PRIVATE_FINAL, GET_INDEX, new Type[]{SIGNATURE}, null);
		mmw = cmw.visitMethod(mi);
		mmw.visitCode();
		mmw.visitVarInsn(Opcodes.ALOAD, 1);
//		mmw.loadArgs(0);
		mmw.invokeVirtual(SIGNATURE, TO_STRING);
		
		final Local toStringLocal = mmw.newLocal(STRING);
		mmw.storeLocal(toStringLocal);
		mmw.loadLocal(toStringLocal);
//		mmw.visitVarInsn(Opcodes.ASTORE, 2);
//		mmw.visitVarInsn(Opcodes.ALOAD, 2);
		
//		mmw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
		mmw.invokeVirtual(STRING, HASH_CODE);

		final int len = hashCodes.length;
		int[] keys = new int[len];
//		final Label[] labels = new Label[len];
		for(int i = 0; i < len; i++)
			keys[i] = hashCodes[i].hashCode;

		final MethodMetadataWriter _mmw = mmw;
		mmw.switchProcess(keys, new SwitchProcessor() {
			public void processDefault(Label switchLabel) {
				_mmw.mark(switchLabel);
				_mmw.push(-1);
				_mmw.returnValue();
			}
			public void processCase(int index, Label label) {
//				_mmw.visitVarInsn(Opcodes.ALOAD, 2);
				_mmw.loadLocal(toStringLocal);
				_mmw.push(hashCodes[index].desc);
				_mmw.invokeVirtual(STRING, EQUALS);
//				Label next = null;
//				if(index == len - 1) {
//					next = switchLabel;
//				} else {
//					next = labels[index + 1];
//				}
				_mmw.jump(Opcodes.IFEQ, label);
				_mmw.push(index + 1);
				_mmw.returnValue();
			}
		});
		mmw.visitMaxs();
		mmw.visitEnd();
		mi = null;
		mmw = null;
	}

	public void writeInvoke0() {
		MethodMetadataWriter mmw = null;
		MethodInfo mi = new MethodInfo(PRIVATE_FINAL, INVOKE0, new Type[]{Type.INT_TYPE, OBJECT_ARRAY}, null);
		mmw = cmw.visitMethod(mi);
		mmw.visitCode();
		mmw.loadArgs(0);
		int len = hashCodes.length;
//		Label[] labels = new Label[len];
//		for(int i = 0; i < len; i++)
//			labels[i] = mmw.createLabel();
//		Label switchLabel = mmw.createLabel();
//		mmw.visitTableSwitchInsn(1, len, switchLabel, labels);
		final MethodMetadataWriter _mmw = mmw;
		mmw.tableSwitch(len, new SwitchProcessor() {
			public void processDefault(Label switchLabel) {
				_mmw.mark(switchLabel);
				_mmw.aconstNull();
				_mmw.returnValue();
			}
			
			public void processCase(int index, Label label) {
				_mmw.mark(label);
				_mmw.loadThis();
				HashEntry entry = hashCodes[index];
				String desc = entry.getDesc();
				Type[] argsTypes = Type.getArgumentTypes(desc);
				for(int j = 0; j < argsTypes.length; j++) {
					_mmw.loadArgs(1);
					_mmw.aaload(j);
					Type type = argsTypes[j];
					if(AsmUtils.isPrimitive(type)) {
						char descriptor = type.getDescriptor().charAt(0);
						String wrapperClassInternalName = AsmUtils.getInternalName(descriptor);
						String className = type.getClassName();
						_mmw.visitTypeInsn(Opcodes.CHECKCAST, wrapperClassInternalName);
						_mmw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, wrapperClassInternalName, className + "Value", "()" + descriptor, Opcodes.INVOKEVIRTUAL == Opcodes.INVOKEINTERFACE);
					} else {
						_mmw.checkCast(argsTypes[j]);
					}
					
				}
				_mmw.invokeVirtual(
						cmw.getClassType(), 
						new Signature(generator.getMethodProxyName(entry.getName()), desc)
				);
				Type returnType = Type.getReturnType(desc);
				if(returnType.equals(Type.VOID_TYPE)) {
					_mmw.aconstNull();
				} else if(AsmUtils.isPrimitive(returnType)) {
					char c = AsmUtils.PRIMITIVE_DESC.get(returnType.getClassName());
					String internalName = AsmUtils.getInternalName(c);
					Type wrappedType = Type.getType("L" + internalName + ";");
					_mmw.invokeStatic(wrappedType, new Signature("valueOf", "(" + c + ")" + wrappedType.getDescriptor()));
				} 
				_mmw.returnValue();
			}
		});

		mmw.visitMaxs();
		mmw.visitEnd();
		mmw = null;
		mi = null;
	}

	public String getClassName() {
		return proxyClassName;
	}

	public static ClassFile generateByteCode(Class<?> theClass) {
		ProxyClassGenerator classGenerator = new ProxyClassGenerator(theClass);
		return new ClassFile(classGenerator.generateByteCode(), classGenerator.getClassName());
	}
	
	
	static class HashEntry {
		int hashCode;
		String desc;
		
		public HashEntry(int hashCode, String desc) {
			this.hashCode = hashCode;
			this.desc = desc;
		}


		public String getDesc() {
			char[] array = desc.toCharArray();
			int len = array.length;
			for(int i = 0; i < len; i++) {
				char c = array[i];
				if(c == '(')
					return new String(array, i, len - i);
			}
			throw new Error("Invalid descriptor");
		}
		
		public String getName() {
			char[] array = desc.toCharArray();
			for(int i = 0; i < array.length; i++) {
				char c = array[i];
				if(c == '(')
					return new String(array, 0, i);
			}
			throw new Error("Invalid descriptor");
		}
	}


	public void writeInvoke() {
		MethodMetadataWriter mmw = null;
		MethodInfo mi = new MethodInfo(PUBLIC_VARARGS, INVOKE, new Type[]{OBJECT, METHOD, OBJECT_ARRAY}, new Type[]{THROWABLE});
		mmw = cmw.visitMethod(mi);
		mmw.visitCode();
//		mmw.loadArgs(1);
//		mmw.invokeVirtual(METHOD, GET_DECLARING_CLASS);
//		mmw.invokeVirtual(CLASS, IS_INTERFACE);
//		Label ifLabel = mmw.createLabel();
//		mmw.jump(Opcodes.IFEQ, ifLabel);
//		mmw.loadArgs(0);
//		mmw.jump(Opcodes.IFNULL, ifLabel);
		if(isInterface) {
			mmw.loadArgs(1);
			mmw.loadArgs(0);
			Type[] argsTypes = mi.getArgumentTypes();
			for(int i = 2; i < argsTypes.length; i++)
				mmw.loadArgs(i);
			mmw.invokeVirtual(METHOD, METHOD_INVOKE);
			mmw.returnValue();
		} else {
//			mmw.mark(ifLabel);
			mmw.loadArgs(1);
			mmw.invokeStatic(Type.getType(AsmUtils.class), GET_SIGNATURE);
			Local sigLocal = mmw.newLocal(SIGNATURE);
			mmw.storeLocal(sigLocal);
			mmw.loadThis();
			mmw.loadThis();
			mmw.loadLocal(sigLocal);
			mmw.invokeConstructor(cmw.getClassType(), GET_INDEX);
			/***********************/
			Type[] argsTypes = mi.getArgumentTypes();
			/***********************/
			for(int i = 2; i < argsTypes.length; i++)
				mmw.loadArgs(i);
			mmw.invokeConstructor(cmw.getClassType(), INVOKE0);
			mmw.returnValue();
		}
		mmw.visitMaxs(0, 0);
		mmw.visitEnd();
		mmw = null;
		mi = null;
	}
}
