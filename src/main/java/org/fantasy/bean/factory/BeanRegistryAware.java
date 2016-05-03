package org.fantasy.bean.factory;

public interface BeanRegistryAware {
	
	public void setBeanRegistry(ServiceBeanRegistry beanFactory);
	
	public ServiceBeanRegistry getBeanRegistry();
	
}

