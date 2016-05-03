package org.fantasy.bean.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fantasy.bean.InstantiationStrategy;
import org.fantasy.bean.GenericBean;
import org.fantasy.bean.annotation.Provider;
import org.fantasy.bean.proxy.ProxyWrapper;
import org.fantasy.conf.Configuration;
import org.fantasy.util.ClassUtils;
import org.fantasy.util.ReflectionUtils;
import org.fantasy.util.StringUtils;

public class DefaultInstanceBeanFactory extends DefaultBeanRegistry implements BeanInstanceFactory {
	
	private final Map<String, Object> beanInstances = new ConcurrentHashMap<String, Object>();
	private final Map<String, Boolean> createdBeans = new ConcurrentHashMap<String, Boolean>();
//	private boolean userProxy = true;
	private InstantiationStrategy instantiationStrategy;
//	private final Map<String, String> name2IdMap = new ConcurrentHashMap<String, String>();

	public DefaultInstanceBeanFactory(Configuration conf, Class<?> annotationClass) {
		super(conf, annotationClass);
		this.instantiationStrategy = (InstantiationStrategy)ReflectionUtils.newInstance(ClassUtils.forName(conf.get("bean.instantiation.strategy", "org.fantasy.bean.ProxyInstantiationStrategy")), null, conf, annotationClass);
	}

	public Object getBeanInstance(String beanName) {
		if(beanInstances.containsKey(beanName)/** && createdBeans.containsKey(beanName) */) {
			ProxyWrapper<Object> wrapper = (ProxyWrapper<Object>)beanInstances.get(beanName);
			return wrapper.getProxy();
		}
		return null;
	}

	public void addBeanInstance(String beanName, Object instance) {
		beanInstances.put(beanName, instance);
	}

	public void destoryBeanInstance() {
		beanInstances.clear();
		createdBeans.clear();
	}

	public InstantiationStrategy getInstantiationStrategy() {
		return instantiationStrategy;
	}

	public void destory() {
		destoryBeanInstance();
		destoryBeanRegistry();
	}

	public void instantiateBeans() {
		String[] beanNames = getBeanNames();
		for(String beanName : beanNames) {
			if(!beanInstances.containsKey(beanName)) {
				final GenericBean serviceBean = getBean(beanName);
				if(serviceBean.isValid()) {
					String id = serviceBean.getId();
					createBeanInstance(id, new ObjectFactory<Object>() {
						public Object createObject() {
							return getInstantiationStrategy().instantiate(serviceBean);
						}
					});
				}
			}
			
		}
	}
	
	
	private Object createBeanInstance(String beanName, ObjectFactory<Object> objectFactory) {
		synchronized(beanInstances) {
			Object beanInstance = beanInstances.get(beanName);
			if(beanInstance == null) {
				beforeInstance(beanName);
				try {
					beanInstance = objectFactory.createObject();
				} finally {
					afterInstance(beanName);
				}
				addBeanInstance(beanName, beanInstance);
			}
			return beanInstance;
		}
	}
	
	public void beforeInstance(String beanName) {
		
	}
	
	public void afterInstance(String beanName) {
		createdBeans.put(beanName, true);
	}

}
