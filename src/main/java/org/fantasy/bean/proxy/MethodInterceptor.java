package org.fantasy.bean.proxy;

import java.lang.reflect.Method;


public interface MethodInterceptor extends TargetObjectAware {

	public Object intercept(ProxyInvoker invoker, Method method, Object ... args) throws Throwable;

}
