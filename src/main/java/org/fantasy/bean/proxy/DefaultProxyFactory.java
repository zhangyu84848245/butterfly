package org.fantasy.bean.proxy;

import org.fantasy.common.Filter;
import org.fantasy.common.NameAware;


public class DefaultProxyFactory implements ProxyFactory, NameAware, Filter<String> {

	public <T> ProxyWrapper<T> createProxy(ProxyConfig config) throws ProxyConfigException {
		Class<T> proxyClass = (Class<T>)config.getProxyClass();
		if(proxyClass == null)
			throw new ProxyConfigException("Proxy class can't be null");
		if(config.isInterface()) {
			Object targetObject = config.getTargetObject();
			if(targetObject == null && !config.isNoTargetProxy())
				throw new ProxyConfigException("Interface proxy requires to create a proxy object");
		}
		return new ProxyWrapper<T>(proxyClass, (T)ProxyCreator.newProxyInstance(proxyClass, config.getInterceptor(), config.getArguments()), (T)config.getTargetObject());
	}

	public boolean accept(String name) {
		return getName().equals(name);
	}

	public String getName() {
		return "default";
	}

	
}
