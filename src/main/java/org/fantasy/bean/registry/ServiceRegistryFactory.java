package org.fantasy.bean.registry;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CopyOnWriteArrayList;

import org.fantasy.conf.Configurable;
import org.fantasy.conf.Configuration;
import org.fantasy.service.Service;
import org.fantasy.util.ClassUtils;
import org.fantasy.util.ReflectionUtils;
import org.fantasy.util.StringUtils;

public class ServiceRegistryFactory {

	private List<ServiceRegistry> factory = new CopyOnWriteArrayList<ServiceRegistry>();
	// SPI
	private ServiceLoader<ServiceRegistry> serviceLoader = ServiceLoader.load(ServiceRegistry.class) ;
	private Configuration conf;
	
	public ServiceRegistryFactory(Configuration conf) {
		this.conf = conf;
		for(ServiceRegistry serviceRegistry : serviceLoader) {
			if(serviceRegistry instanceof Configurable) {
				((Configurable) serviceRegistry).setConfig(conf);
			}
			
			if(serviceRegistry instanceof Service) {
				((Service) serviceRegistry).start();
			}
			factory.add(serviceRegistry);
		}
	}
	
	
	public void add(String className) {
		if(StringUtils.isEmpty(className))
			return;
		add((Class<ServiceRegistry>)ClassUtils.forName(className));
	}
	
	
	public void add(Class<ServiceRegistry> registryClass) {
		if(registryClass == null)
			throw new IllegalArgumentException("Registry class is null");
		ServiceRegistry serviceRegistry = ReflectionUtils.newInstance(registryClass, conf);
		if(serviceRegistry instanceof Service) {
			((Service) serviceRegistry).start();
		}
		factory.add(serviceRegistry);
	}
	
	
	public ServiceRegistry get(String name) {
		synchronized(factory) {
			for(Iterator<ServiceRegistry> iterator = factory.iterator();iterator.hasNext();) {
				ServiceRegistry serviceRegistry = iterator.next();
				if(serviceRegistry.accept(name))
					return serviceRegistry;
			}
		}
		return null;
	}
}
