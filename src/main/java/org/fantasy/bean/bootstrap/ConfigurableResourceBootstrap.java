package org.fantasy.bean.bootstrap;

import org.fantasy.conf.Configuration;
import org.fantasy.io.DefaultResourceLoader;
import org.fantasy.io.ResourceLoader;

public class ConfigurableResourceBootstrap extends ConfigurableBootstrap {

	private String configLocation;
	private ResourceLoader resourceLoader = new DefaultResourceLoader();
	private Configuration conf;
	
	public ConfigurableResourceBootstrap(String configLocation) {
		super();
		this.configLocation = configLocation;
		this.conf = new Configuration(configLocation, resourceLoader);
		setConfig(conf);
	}

	public String getConfigLocation() {
		return configLocation;
	}

}
