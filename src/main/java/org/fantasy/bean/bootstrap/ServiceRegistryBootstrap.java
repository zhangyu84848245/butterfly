package org.fantasy.bean.bootstrap;



import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.fantasy.bean.factory.BeanRegistryAware;
import org.fantasy.bean.factory.ServiceBeanRegistry;
import org.fantasy.bean.registry.RegistryCallback;
import org.fantasy.bean.registry.ServiceRegistry;
import org.fantasy.bean.registry.ServiceRegistryFactory;
import org.fantasy.conf.Configuration;
import org.fantasy.context.BeanFactoryContext;
import org.fantasy.service.AbstractService;
import org.fantasy.service.STATE;
import org.fantasy.service.ServiceStateChangeEvent;
import org.fantasy.service.ServiceStateChangeListener;

public class ServiceRegistryBootstrap extends AbstractService implements BeanRegistryAware {
	
	public static final String SERVICE_REGISTRY_KEY = "service.registry.type";

	private ServiceRegistryFactory registryFactory;
	private ServiceRegistry serviceRegistry;
	private BeanFactoryContext beanFactoryContext;
	private ServiceBeanRegistry beanRegistry;
	private List<RegistryCallback> callbacks = new ArrayList<RegistryCallback>();
	
	public ServiceRegistryBootstrap(Configuration conf) {
		this();
		setConfig(conf);
	}

	public ServiceRegistryBootstrap() {
		super();
		support.addStateChangeListener(new ServiceStateChangeListener() {
			public void stateChange(ServiceStateChangeEvent event) {
				STATE state = event.getState();
				if(state == STATE.INITIALIZED) {
					
				} else if(state == STATE.STARTED) {
					started();
				}
			}
		});
	}
	
	private void beforeExecute() {
		
	}
	
	private void started() {
		synchronized(callbacks) {
			try {
				beforeExecute();
				for(Iterator<RegistryCallback> iterator = callbacks.iterator();iterator.hasNext();) {
					RegistryCallback callback = iterator.next();
					callback.execute(beanFactoryContext);
				}
			} finally {
				afterExecute();
			}
		}
		
	}
	
	
	public void afterExecute() {
		
	}

	public void serviceInit() {
		this.registryFactory = new ServiceRegistryFactory(getConf());
		this.serviceRegistry = registryFactory.get(getConf().get(SERVICE_REGISTRY_KEY));
		this.beanFactoryContext = new BeanFactoryContext(serviceRegistry, getBeanRegistry(), getConf());
		super.serviceInit();
	}

	public void serviceStart() {
		super.serviceStart();
	}

	public void serviceStop() {
		super.serviceStop();
	}

	public void setBeanRegistry(ServiceBeanRegistry beanFactory) {
		this.beanRegistry = beanFactory;
	}

	public ServiceBeanRegistry getBeanRegistry() {
		return beanRegistry;
	}

	public void addRegistryCallback(RegistryCallback callback) {
		synchronized(callbacks) {
			callbacks.add(callback);
		}
	}
}
