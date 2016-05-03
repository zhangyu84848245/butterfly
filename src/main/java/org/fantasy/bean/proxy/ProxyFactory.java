package org.fantasy.bean.proxy;

public interface ProxyFactory {

	public <T> ProxyWrapper<T> createProxy(ProxyConfig config) throws ProxyConfigException;
	
}
