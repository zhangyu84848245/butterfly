package org.fantasy.bean.bootstrap.server;
import org.fantasy.bean.annotation.Provider;
import org.fantasy.bean.bootstrap.ConfigurableResourceBootstrap;
import org.fantasy.util.Constant;


public class ServerBootstrap extends ConfigurableResourceBootstrap  {

	public ServerBootstrap() {
		this(Constant.DEFAULT_CONFIG_LOCATION);
	}

	public ServerBootstrap(final String configLocation) {
		super(configLocation);
		setAnnotationClass(Provider.class);
	}

}
