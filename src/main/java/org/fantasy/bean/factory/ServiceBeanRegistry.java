package org.fantasy.bean.factory;

import org.fantasy.bean.BeanException;
import org.fantasy.bean.GenericBean;

public interface ServiceBeanRegistry {
	
	
	public void registerBean(String beanName, GenericBean bean) throws BeanException;
	
	public void removeBean(String beanName);
	
	public GenericBean getBean(String beanName);
	
	public boolean containsBean(String beanName);
	
	public String[] getBeanNames();
	
	public int getBeanCount();

	public void destoryBeanRegistry();
	
//	public Collection<ServiceBean> getRegisteredBeans();
}
