package org.fantasy.bean.proxy;

public class ProxyConfig {

	private Class<?> proxyClass;
	private Object targetObject;
	private MethodInterceptor interceptor;
	private Object[] arguments;
	private boolean noTargetProxy;

	public ProxyConfig(Class<?> proxyClass, Object targetObject) {
		this(proxyClass, targetObject, null);
	}

	public ProxyConfig(Class<?> proxyClass) {
		this(proxyClass, null);
	}

	public ProxyConfig(Class<?> proxyClass, Object targetObject, Object[] arguments) {
		this(proxyClass, targetObject, new SimpleMethodInterceptor(targetObject), arguments);
	}

	public ProxyConfig(Class<?> proxyClass, Object targetObject, MethodInterceptor interceptor, Object[] arguments) {
		this(proxyClass, targetObject, interceptor, arguments, false);
	}
	
	public ProxyConfig(Class<?> proxyClass, Object targetObject, MethodInterceptor interceptor, Object[] arguments, boolean noTargetProxy) {
		this.proxyClass = proxyClass;
		this.targetObject = targetObject;
		if(interceptor == null)
			this.interceptor = new SimpleMethodInterceptor(targetObject);
		else
			this.interceptor = interceptor;
		this.arguments = arguments;
		this.noTargetProxy = noTargetProxy;
	}

	public Object getTargetObject() {
		return targetObject;
	}

	public void setTargetObject(Object targetObject) {
		this.targetObject = targetObject;
	}

	public Class<?> getProxyClass() {
		return proxyClass;
	}

	public MethodInterceptor getInterceptor() {
		return interceptor;
	}

	public void setInterceptor(MethodInterceptor interceptor) {
		this.interceptor = interceptor;
	}
	
	public boolean isInterface() {
		return proxyClass.isInterface();
	}

	public Object[] getArguments() {
		return arguments;
	}

	public boolean isNoTargetProxy() {
		return noTargetProxy;
	}

	
}
