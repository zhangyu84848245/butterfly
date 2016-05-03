package org.fantasy.bean.proxy.asm;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.fantasy.util.Constant;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public abstract class AsmUtils {
	
	public static final Map<String, Character> PRIMITIVE_DESC = new HashMap<String, Character>();
    
    
	static {
		PRIMITIVE_DESC.put("byte", 'B');
		PRIMITIVE_DESC.put("char", 'C');
		PRIMITIVE_DESC.put("double", 'D');
		PRIMITIVE_DESC.put("float", 'F');
		PRIMITIVE_DESC.put("int", 'I');
		PRIMITIVE_DESC.put("long", 'J');
		PRIMITIVE_DESC.put("short", 'S');
		PRIMITIVE_DESC.put("boolean", 'Z');
	}

	
	private AsmUtils() {
	}
	
	public static String getInternalName(char desc) {
		switch(desc) {
			case 'B':
				return "java/lang/Byte";
			case 'C':
				return "java/lang/Character";
			case 'D':
				return "java/lang/Double";
			case 'F':
				return "java/lang/Float";
			case 'I':
				return "java/lang/Integer";
			case 'J':
				return "java/lang/Float";
			case 'S':
				return "java/lang/Short";
			case 'Z':
				return "java/lang/Boolean";
			default:
				throw new IllegalArgumentException("" + desc);
		}
	}

	
	
	public static Type getType(String className) {
		return Type.getType("L" + className.replace('.', '/') + ";");
	}

	public static String[] toInternalNames(Type[] types) {
		if(types == null)
			return null;
		String[] names = new String[types.length];
		for(int i = 0; i < types.length; i++)
			names[i] = types[i].getInternalName();
		return names;
	}

	public static Type[] getTypes(Class<?>[] classes) {
		if(classes == null)
			return null;
		Type[] types = new Type[classes.length];
		for(int i = 0; i < classes.length; i++)
			types[i] = Type.getType(classes[i]);
		return types;
	}

	public static boolean isInterface(int access) {
        return (Opcodes.ACC_INTERFACE & access) != 0;
    }

	public static boolean isArray(Type type) {
		return type.getSort() == Type.ARRAY;
	}

	public static MethodInfo getMethodInfo(Method method, int pos) {
		Signature sig = getSignature(method);
		int access = Opcodes.ACC_FINAL | ( method.getModifiers() & ~Opcodes.ACC_NATIVE & ~Opcodes.ACC_SYNCHRONIZED );
		return new MethodInfo(pos, access, sig, getTypes(method.getParameterTypes()), getTypes(method.getExceptionTypes()));
	}
	
	public static MethodInfo getMethodInfo(Constructor<?> constructor) {
		Signature sig = getSignature(constructor);
		return new MethodInfo(constructor.getModifiers(), sig, getTypes(constructor.getParameterTypes()), getTypes(constructor.getExceptionTypes()));
	}

	public static ClassInfo getClassInfo(Class<?> theClass) {
		Class<?> superClass = theClass.getSuperclass();
		Class<?>[] interfaces = theClass.getInterfaces();
		return new ClassInfo(Type.getType(theClass), Type.getType(superClass), theClass.getModifiers(), getTypes(interfaces));
	}

	public static Signature getSignature(Member member) {
		if(member instanceof Method) {
			return new Signature(member.getName(), Type.getMethodDescriptor((Method)member));
		} else if(member instanceof Constructor<?>) {
			Type[] parameterTypes = getTypes(((Constructor)member).getParameterTypes());
			return new Signature(Constant.CONSTRUCTOR_NAME, Type.getMethodDescriptor(Type.VOID_TYPE, parameterTypes));
		} else {
			return null;
		}
	}

	public static boolean isAbstract(int access) {
		return (Opcodes.ACC_ABSTRACT & access) != 0;
	}
	
	public static boolean isStatic(int access) {
		return (Opcodes.ACC_STATIC & access) != 0;
    }

	public static int ICONST(int value) {
        switch (value) {
	        case -1: 
	        	return Opcodes.ICONST_M1;
	        case 0: 
	        	return Opcodes.ICONST_0;
	        case 1: 
	        	return Opcodes.ICONST_1;
	        case 2: 
	        	return Opcodes.ICONST_2;
	        case 3: 
	        	return Opcodes.ICONST_3;
	        case 4: 
	        	return Opcodes.ICONST_4;
	        case 5: 
	        	return Opcodes.ICONST_5;
        }
        return -1; // error
    }
	
	
	public static boolean isPrimitive(Type type) {
		switch (type.getSort()) {
			case Type.ARRAY:
			case Type.OBJECT:
				return false;
			default:
				return true;
		}
	}
	
	
	public static int NEWARRAY(Type type) {
		switch (type.getSort()) {
			case Type.BYTE:
				return Opcodes.T_BYTE;
			case Type.CHAR:
				return Opcodes.T_CHAR;
			case Type.DOUBLE:
				return Opcodes.T_DOUBLE;
			case Type.FLOAT:
				return Opcodes.T_FLOAT;
			case Type.INT:
				return Opcodes.T_INT;
			case Type.LONG:
				return Opcodes.T_LONG;
			case Type.SHORT:
				return Opcodes.T_SHORT;
			case Type.BOOLEAN:
				return Opcodes.T_BOOLEAN;
			default:
				return -1; // error
		}
	}
}
