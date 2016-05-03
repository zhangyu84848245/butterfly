package org.fantasy.bean.registry.lb;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.fantasy.bean.registry.RegistryValue;

public class RandomLoadBalancer extends BaseLoadBalancer {

	private static final Random RANDOM = new Random();
	public static final Logger LOG = Logger.getLogger(RandomLoadBalancer.class);
	public RandomLoadBalancer() {
		
	}

	public ServerName assign(List<RegistryValue> values) {
		if(values == null || values.size() == 0)
			return null;
		RegistryValue value = null;
		if(!needsBalance(values)) {
			LOG.info("There is only one server");
			value = values.get(0);
		} else {
			int size = values.size();
			int serverIndex = RANDOM.nextInt(values.size());
			value = values.get(serverIndex % size);
		}
		return new ServerName(value.getAddress(), value.getPort());
	}

	public String getName() {
		return "random";
	}

	public boolean accept(String name) {
		return getName().equals(name);
	}

}
