package org.fantasy.bean.proxy;

public class ProxyWrapper<T> {

	private Class<T> proxyClass;
	private T proxy;
	private T targetObject;
	
	public ProxyWrapper(Class<T> proxyClass, T proxy, T targetObject) {
		this.proxy = proxy;
		this.proxyClass = proxyClass;
		this.targetObject = targetObject;
	}
	
	public ProxyWrapper(Class<T> proxyClass, T proxy) {
		this(proxyClass, proxy, null);
	}

	public T getProxy() {
		return proxy;
	}

	public T getTargetObject() {
		return targetObject;
	}

	public void setTargetObject(T targetObject) {
		this.targetObject = targetObject;
	}

	public Class<T> getProxyClass() {
		return proxyClass;
	}
	
	
}
