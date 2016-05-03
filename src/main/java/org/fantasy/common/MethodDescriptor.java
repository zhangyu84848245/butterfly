package org.fantasy.common;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class MethodDescriptor implements Serializable {

	private static final long serialVersionUID = 5570274084614713453L;
	
	private transient int access;
	private String name;
	private String[] parameterTypes;
	private transient String returnType;
	private transient String[] exceptionTypes;
	private String declaringClass;
	
	/**
	 * 默认构造方法序列化使用
	 */
	public MethodDescriptor() {
		
	}
	
	public MethodDescriptor(String name, String declaringClass) {
		this.name = name;
		this.declaringClass = declaringClass;
	}
	
	

	public MethodDescriptor(int access, String name, String declaringClass, String[] parameterTypes, String returnType, String[] exceptionTypes) {
		this.access = access;
		this.name = name;
		this.parameterTypes = parameterTypes;
		this.exceptionTypes = exceptionTypes;
		this.returnType = returnType;
		this.declaringClass = declaringClass;
	}

	public MethodDescriptor(Method method) {
		this.access = method.getModifiers();
		this.name = method.getName();
		Class<?>[] parameterTypes = method.getParameterTypes();
		String[] pTypes = new String[parameterTypes.length]; 
		for(int i = 0; i < parameterTypes.length; i++)
			pTypes[i] = parameterTypes[i].getName();
		Class<?>[] exceptionTypes = method.getExceptionTypes();
		String[] eTypes = new String[exceptionTypes.length];
		for(int i = 0; i < exceptionTypes.length; i++)
			eTypes[i] = exceptionTypes[i].getName();
		this.declaringClass = method.getDeclaringClass().getName();
		this.returnType = method.getReturnType().getName();
	}
	
	public void setDeclaringClass(String declaringClass) {
		this.declaringClass = declaringClass;
	}

	public String getName() {
		return name;
	}
	
	public String[] getParameterTypes() {
		return parameterTypes;
	}	

	public String getReturnType() {
		return returnType;
	}

	public String[] getExceptionTypes() {
		return exceptionTypes;
	}

	public String getDeclaringClass() {
		return declaringClass;
	}

	public int getAccess() {
		return access;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return super.equals(obj);
	}


	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getModifier(access)).append(" ").append(returnType).append(" ").append(name).append("(");
		for(String type : parameterTypes)
			sb.append(type).append(",");
		sb.deleteCharAt(sb.length() - 1);
		sb.append(")");
		if(exceptionTypes.length > 0) {
			sb.append(" throws ");
			for(String exception : exceptionTypes)
				sb.append(exception).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
	
	private String getModifier(int access) {
		switch(access) {
			case Modifier.PUBLIC: 
				return "public";
			case Modifier.PROTECTED:
				return "protected";
			case Modifier.PRIVATE:
				return "private";
			default:
				return "";
		}
	}

}
