package org.fantasy.bean.asm;

import java.util.Map;

public interface MethodMetadata {

	public int getAccess();
	
	public String getMethodName();
	
	public String getDeclaringClassName();
	
	public boolean isStatic();
	
	public boolean isFinal();
	
	public boolean isAnnotated(String annotationType);
	
	public Map<String, Object> getAnnotationAttributes(String annotationType);
	
	public String getMethodDescriptor();
	
	public String[] getExceptionTypes();
}
