package org.fantasy.bean.bootstrap;

import java.util.concurrent.atomic.AtomicBoolean;

import org.fantasy.bean.annotation.Provider;
import org.fantasy.bean.annotation.scanner.BeanScanner;
import org.fantasy.bean.annotation.scanner.ClassPathScanner;
import org.fantasy.bean.factory.BeanInstanceFactory;
import org.fantasy.bean.factory.DefaultInstanceBeanFactory;
import org.fantasy.bean.factory.ServiceBeanRegistry;
import org.fantasy.bean.proxy.MethodInterceptor;
import org.fantasy.service.AbstractService;
import org.fantasy.service.ServiceStateChangeEvent;
import org.fantasy.service.ServiceStateChangeListener;

public class BeanFactoryBootstrap extends AbstractService {

	private BeanInstanceFactory beanFactory;
	private AtomicBoolean active = new AtomicBoolean(false);
	private BeanScanner beanScanner;
	private Class<?> annotationClass;

	public BeanFactoryBootstrap() {
		super();
	}

	public BeanInstanceFactory createBeanFactory() {
		if(beanFactory != null) {
			((DefaultInstanceBeanFactory)beanFactory).destory();
			beanFactory = null;
		}
		this.beanFactory = new DefaultInstanceBeanFactory(getConf(), annotationClass);
		return beanFactory;
	}


	public BeanInstanceFactory getBeanFactory() {
		if(this.beanFactory == null) {
			createBeanFactory();
		}
		return beanFactory;
	}
	
	public ServiceBeanRegistry getBeanRegistry() {
		return (ServiceBeanRegistry)getBeanFactory();
	}

	public void serviceInit() {
		if(active.compareAndSet(false, true)) {
			createBeanFactory();
			this.beanScanner = new ClassPathScanner(getConf(), (ServiceBeanRegistry)beanFactory);
		}
		super.serviceInit();
	}


	public void serviceStart() {
		if(active.get()) {
			beanScanner.doScan(/** annotationClass */);
			beanFactory.instantiateBeans();
		}
		super.serviceStart();
	}


	public void serviceStop() {
		((DefaultInstanceBeanFactory)beanFactory).destory();
		beanFactory = null;
		getConf().destory();
		super.serviceStop();
	}

	public void setAnnotationClass(Class<?> annotationClass) {
		this.annotationClass = annotationClass;
	}

	public Class<?> getAnnotationClass() {
		return annotationClass;
	}

	public BeanScanner getBeanScanner() {
		return beanScanner;
	}

}
