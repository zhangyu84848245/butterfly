package org.fantasy.bean.bootstrap;

import org.fantasy.bean.BeanCreatorCallback;
import org.fantasy.bean.ConsumerBean;
import org.fantasy.bean.GenericBean;
import org.fantasy.bean.ProviderBean;
import org.fantasy.bean.annotation.Provider;
import org.fantasy.bean.annotation.scanner.BeanScanner;
import org.fantasy.bean.asm.AnnotationAttributes;
import org.fantasy.bean.factory.BeanInstanceFactory;
import org.fantasy.bean.factory.ServiceBeanRegistry;
import org.fantasy.bean.registry.RegistryCallback;
import org.fantasy.context.BeanFactoryContext;
import org.fantasy.net.RpcFactoryProvider.Endpoint;
import org.fantasy.service.STATE;
import org.fantasy.service.ServiceStateChangeEvent;
import org.fantasy.service.ServiceStateChangeListener;

public class ConfigurableBootstrap extends BeanFactoryBootstrap {

//	private Class<?> annotationClass;

	public ConfigurableBootstrap() {
		super();
		support.addStateChangeListener(new ServiceStateChangeListener() {
			private ServiceRegistryBootstrap registryBootstrap;
			public void stateChange(ServiceStateChangeEvent event) {
				STATE state = event.getState();
				if(state == STATE.INITIALIZED) {
					ServiceBeanRegistry beanFactory = ((BeanFactoryBootstrap)event.getService()).getBeanRegistry();
					registryBootstrap = new ServiceRegistryBootstrap(getConf());
					registryBootstrap.setBeanRegistry(beanFactory);
					final boolean isProvider = getAnnotationClass().equals(Provider.class);
					getBeanScanner().setBeanCallback(new BeanCreatorCallback() {
						public String getAnnotationName() {
							return getAnnotationClass().getName();
						}
						public GenericBean create(String beanId, String beanClassName, String refClassName) {
							if(isProvider)
								return new ProviderBean(refClassName, beanClassName, beanId);
							else
								return new ConsumerBean(beanId, beanClassName);
						}
					});
					if(isProvider) {
						RpcBootstrap rpcBootstrap = new RpcBootstrap(getConf(), (BeanInstanceFactory)beanFactory);
						rpcBootstrap.setEndpoint(Endpoint.SERVER);
						rpcBootstrap.start();
					} else {
						
					}
					registryBootstrap.addRegistryCallback(new RegistryCallback() {
						public void execute(BeanFactoryContext context) {
							if(isProvider)
								context.register();
							else
								context.subscribe(/**  Endpoint.CLIENT  */);
						}
					});
				} else if(state == STATE.STARTED) {
					registryBootstrap.start();
				}
			}
		});
	}

//	public void setAnnotationClass(Class<?> annotationClass) {
//		super.setAnnotationClass(annotationClass);
//		this.annotationClass = annotationClass;
//	}
}
