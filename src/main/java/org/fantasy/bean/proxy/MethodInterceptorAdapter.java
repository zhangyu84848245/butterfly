package org.fantasy.bean.proxy;

import java.lang.reflect.Method;

public class MethodInterceptorAdapter implements MethodInterceptor {

	private MethodInterceptor interceptor;

	public MethodInterceptorAdapter(MethodInterceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public Object intercept(ProxyInvoker invoker, Method method, Object ... args) throws Throwable {
		if(interceptor instanceof BeforeInterceptor)
			((BeforeInterceptor)interceptor).before(getTargetObject(), method, args);
		Object returnValue = interceptor.intercept(invoker, method, args);
		if(interceptor instanceof AfterInterceptor)
			((AfterInterceptor)interceptor).afterReturning(getTargetObject(), returnValue, method, args);
		return returnValue;
	}

	public Object getTargetObject() {
		return interceptor.getTargetObject();
	}

	public void setTargetObject(Object targetObject) {
		interceptor.setTargetObject(targetObject);
	}

}
