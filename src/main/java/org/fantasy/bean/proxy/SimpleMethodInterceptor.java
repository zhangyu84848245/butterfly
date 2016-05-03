package org.fantasy.bean.proxy;

import java.lang.reflect.Method;

public class SimpleMethodInterceptor implements MethodInterceptor {

	private Object targetObject;

	public SimpleMethodInterceptor() {
		
	}

	public SimpleMethodInterceptor(Object targetObject) {
		this.targetObject = targetObject;
	}
	
	public Object intercept(ProxyInvoker invoker, Method method, Object... args) throws Throwable {
		return invoker.invoke(getTargetObject(), method, args);
	}

	public Object getTargetObject() {
		return targetObject;
	}

	public void setTargetObject(Object targetObject) {
		this.targetObject = targetObject;
	}

}
