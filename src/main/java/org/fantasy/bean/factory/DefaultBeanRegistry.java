package org.fantasy.bean.factory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.fantasy.bean.AbstractGenericBean;
import org.fantasy.bean.BeanException;
import org.fantasy.bean.GenericBean;
import org.fantasy.conf.Configuration;

public class DefaultBeanRegistry implements ServiceBeanRegistry {
	private Configuration confg;
	private final Map<String, GenericBean> serviceBeanMap = new ConcurrentHashMap<String, GenericBean>();
	private final List<String> beanNames = new CopyOnWriteArrayList<String>();
	private Class<?> annotationClass;
	
	public DefaultBeanRegistry(Configuration conf, Class<?> annotationClass) {
		this.confg = conf;
		this.annotationClass = annotationClass;
	}

	public void registerBean(String beanName, GenericBean bean) throws BeanException {
//		AbstractGenericBean theBean = (AbstractGenericBean)bean;
		bean.isValid();
		serviceBeanMap.put(beanName, bean);
		beanNames.add(beanName);
	}

	public void removeBean(String beanName) {
		serviceBeanMap.remove(beanName);
		beanNames.remove(beanName);
	}

	public GenericBean getBean(String beanName) {
		GenericBean serviceBean = serviceBeanMap.get(beanName);
		if(serviceBean == null) {
			throw new BeanException("No serviceBean found in serviceBeanMap.");
		}
		return serviceBean;
	}

	public boolean containsBean(String beanName) {
		return serviceBeanMap.containsKey(beanName) && beanNames.contains(beanName);
	}

	public String[] getBeanNames() {
		return beanNames.toArray(new String[beanNames.size()]);
	}

	public int getBeanCount() {
		return serviceBeanMap.size();
	}

	public void destoryBeanRegistry() {
		for(Iterator<Map.Entry<String, GenericBean>> iterator = serviceBeanMap.entrySet().iterator();iterator.hasNext();) {
			Map.Entry<String, GenericBean> entry = iterator.next();
			GenericBean bean = entry.getValue();
			bean.destory();
		}
		serviceBeanMap.clear();
		beanNames.clear();
	}

	public Class<?> getAnnotationClass() {
		return annotationClass;
	}

}
