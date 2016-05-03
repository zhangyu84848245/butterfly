package org.fantasy.bean.factory;


public interface BeanInstanceFactory {

	public Object getBeanInstance(String beanName);

	public void addBeanInstance (String beanName, Object instance);

	public void destoryBeanInstance();

	public void instantiateBeans();
	
//	public void registerService(Service service);

}
