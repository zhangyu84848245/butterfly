package org.fantasy.bean.bootstrap.server;
import org.fantasy.bean.annotation.Provider;
import org.fantasy.bean.bootstrap.ConfigurableResourceBootstrap;
import org.fantasy.util.Constant;


public class ServerBootstrap extends ConfigurableResourceBootstrap  {

	private volatile Thread shutdownHook;
	public ServerBootstrap() {
		this(Constant.DEFAULT_CONFIG_LOCATION);
	}

	public ServerBootstrap(final String configLocation) {
		super(configLocation);
		setAnnotationClass(Provider.class);
	}

	public void serviceInit() {
		shutdownHook = new Thread() {
			public void run() {
				ServerBootstrap.this.stop();
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		super.serviceInit();
	}

	public void serviceStop() {
		super.serviceStop();
		// stop zookeeper
		getRegistryBootstrap().stop();
		// stop rpc server
		getRpcBootstrap().stop();
	}

	
}
