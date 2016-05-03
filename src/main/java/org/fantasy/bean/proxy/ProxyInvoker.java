package org.fantasy.bean.proxy;

import java.lang.reflect.Method;

/**
 * 代理的回调类
 * @author fantasy
 */
public interface ProxyInvoker {

	public Object invoke(Object target, Method method, Object ... args) throws Throwable;

}
