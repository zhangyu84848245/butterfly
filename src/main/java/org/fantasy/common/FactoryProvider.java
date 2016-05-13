package org.fantasy.common;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.fantasy.common.Filter;
import org.fantasy.conf.Configurable;
import org.fantasy.conf.Configuration;
import org.fantasy.service.Service;
import org.fantasy.util.ClassUtils;
import org.fantasy.util.ReflectionUtils;
import org.fantasy.util.StringUtils;

public class FactoryProvider<T> implements Closeable {

	private List<T> factory = new ArrayList<T>();
	private ServiceLoader<T> serviceLoader;
	private Configuration conf;

	public FactoryProvider(Configuration conf, Class<T> serviceClass) {
		this.serviceLoader = ServiceLoader.load(serviceClass);
		this.conf = conf;
		for(T object : serviceLoader) {
			if(object instanceof Configurable) {
				((Configurable) object).setConfig(conf);
			}
			
			if(object instanceof Service) {
				((Service) object).start();
			}
			factory.add(object);
		}
	}

	public void add(String className) {
		if(StringUtils.isEmpty(className))
			return;
		add((Class<T>)ClassUtils.forName(className));
	}

	public void add(Class<T> serviceClass) {
		if(serviceClass == null)
			throw new IllegalArgumentException("Service class is null");
		T object = ReflectionUtils.newInstance(serviceClass, conf);
		if(object instanceof Service) {
			((Service) object).start();
		}
		factory.add(object);
	}

	public T get(String name) {
		synchronized(factory) {
			for(Iterator<T> iterator = factory.iterator();iterator.hasNext();) {
				T object = iterator.next();
				if(!(object instanceof Filter))
					continue;
				if(((Filter<String>)object).accept(name))
					return object;
			}
		}
		return null;
	}

	public void close() throws IOException {
		factory.clear();
		factory = null;
		serviceLoader = null;
	}

}
