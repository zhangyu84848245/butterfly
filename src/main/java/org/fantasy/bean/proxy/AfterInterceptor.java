package org.fantasy.bean.proxy;

import java.lang.reflect.Method;

public interface AfterInterceptor {

	public void afterReturning(Object target, Object returnValue, Method method, Object ... args) throws Throwable;

}
