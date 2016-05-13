package org.fantasy.bean.bootstrap.client;

import org.fantasy.bean.annotation.Consumer;
import org.fantasy.bean.bootstrap.ConfigurableResourceBootstrap;
import org.fantasy.bean.bootstrap.server.ServerBootstrap;
import org.fantasy.util.Constant;

public class ClientBootstrap extends ConfigurableResourceBootstrap {

	private volatile Thread shutdownHook;
	public ClientBootstrap() {
		this(Constant.DEFAULT_CONFIG_LOCATION);
	}

	public ClientBootstrap(String configLocation) {
		super(configLocation);
		setAnnotationClass(Consumer.class);
	}

	public void serviceInit() {
		shutdownHook = new Thread() {
			public void run() {
				ClientBootstrap.this.stop();
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		super.serviceInit();
	}

	public void serviceStop() {
		super.serviceStop();
		// stop zookeeper
		getRegistryBootstrap().stop();
	}

	
}
