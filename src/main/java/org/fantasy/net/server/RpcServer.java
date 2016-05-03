package org.fantasy.net.server;

import java.io.IOException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.spi.SelectorProvider;

import org.fantasy.bean.BeanException;
import org.fantasy.bean.factory.BeanInstanceFactory;
import org.fantasy.net.ChannelException;
import org.fantasy.service.STATE;
import org.fantasy.service.ServiceStateChangeEvent;
import org.fantasy.service.ServiceStateChangeListener;
import org.apache.log4j.Logger;


public class RpcServer extends AbstractNioServerSocketChannel {
	
	private static final Logger LOG = Logger.getLogger(RpcServer.class);
	Listener listener;
//	private Reader reader;
	private Writer writer;
	private BeanInstanceFactory beanFactory;

	public static final SelectorProvider DEFAULT_SELECTOR_PROVIDER = SelectorProvider.provider();

	public RpcServer(String bindAddress, int port) {
		this(newSocket(DEFAULT_SELECTOR_PROVIDER), bindAddress, port);
	}

	private RpcServer(ServerSocketChannel ch, String bindAddress, int port) {
		super(ch, bindAddress, port);
		// 观察者模式
		support.addStateChangeListener(new ServiceStateChangeListener() {
			public void stateChange(ServiceStateChangeEvent event) {
				STATE state = event.getState();
				switch(state.getValue()) {
					case 1: {
						if(LOG.isDebugEnabled()) {
							LOG.debug("Initializing ...");
						}
						break;
					}
					default:
				}
			}
		});
	}
	
//	public void configureSocket() {
//		getSocketOption().setReuseAddress(true);
//	}

	private static ServerSocketChannel newSocket(SelectorProvider provider) {
		try {
			return provider.openServerSocketChannel();
		} catch (IOException e) {
			throw new ChannelException("Failed to open a server socket.", e);
		}
	}


	public void serviceInit() {
		try {
			this.writer = new Writer(getConf());
			this.listener = new Listener(
					RpcServer.DEFAULT_SELECTOR_PROVIDER, 
					bindAddress, 
					port, 
					this, 
					this.writer, 
					getConf()
			);
		} catch (IOException e) {
			throw new RuntimeException("Server init error!");
		}
		
	}

	public void serviceStart() {
		this.listener.start();
		this.writer.start();
	}

	public void serviceStop() {
		this.listener.interrupt();
		this.listener.close();
		this.writer.interrupt();
		this.writer.close();
	}
	
	public void registerBeanFactory(BeanInstanceFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
	
	public Object getBean(String beanName) {
		Object bean = beanFactory.getBeanInstance(beanName);
		if(bean == null)
			throw new BeanException("No such bean instance");
		return bean;
	}

}
