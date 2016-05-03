package org.fantasy.bean.proxy;

import java.lang.reflect.Method;

public interface BeforeInterceptor {

	public void before(Object target, Method method, Object ... args) throws Throwable;
}
