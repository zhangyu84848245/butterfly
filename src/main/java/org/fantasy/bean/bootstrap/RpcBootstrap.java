package org.fantasy.bean.bootstrap;

import org.fantasy.bean.factory.BeanInstanceFactory;
import org.fantasy.conf.Configuration;
import org.fantasy.net.RpcFactory;
import org.fantasy.net.RpcFactoryProvider;
import org.fantasy.net.RpcFactoryProvider.Endpoint;
import org.fantasy.net.server.RpcServer;
import org.fantasy.service.AbstractService;
import org.fantasy.service.Service;
import org.fantasy.util.Constant;

public class RpcBootstrap extends AbstractService {

	private RpcFactory rpcFactory;
	private RpcServer server;
	private String bindAddress;
	private int port;
	
	private Endpoint endpoint;
	private BeanInstanceFactory beanFactory;
	
	public RpcBootstrap() {
		super();
	}
	
	public RpcBootstrap(Configuration conf, BeanInstanceFactory beanFactory) {
		this();
		setConfig(conf);
		this.beanFactory = beanFactory;
	}

	public void serviceInit() {
		this.rpcFactory = RpcFactoryProvider.createFactory(getConf(), endpoint);
		this.bindAddress = getConf().getBindAddress();
		this.port = getConf().getInt(Constant.BIND_PORT_KEY);
		this.server = rpcFactory.create(bindAddress, port, endpoint);
		server.registerBeanFactory(beanFactory);
		super.serviceInit();
	}

	public void serviceStart() {
		server.start();
		super.serviceStart();
	}

	public void serviceStop() {
		server.stop();
		super.serviceStop();
	}

	public void setEndpoint(Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	public Service getService() {
		return server;
	}

	public BeanInstanceFactory getBeanFactory() {
		return beanFactory;
	}

}
