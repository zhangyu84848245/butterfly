package org.fantasy.bean.bootstrap.client;

import org.fantasy.bean.annotation.Consumer;
import org.fantasy.bean.bootstrap.ConfigurableResourceBootstrap;
import org.fantasy.util.Constant;

public class ClientBootstrap extends ConfigurableResourceBootstrap {

	public ClientBootstrap() {
		this(Constant.DEFAULT_CONFIG_LOCATION);
	}

	public ClientBootstrap(String configLocation) {
		super(configLocation);
		setAnnotationClass(Consumer.class);
	}

}
