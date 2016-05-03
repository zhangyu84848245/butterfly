package org.fantasy.bean.registry.lb;

import java.util.List;

import org.fantasy.bean.registry.RegistryValue;
import org.fantasy.common.Filter;
import org.fantasy.common.NameAware;

public abstract class BaseLoadBalancer implements LoadBalancer, NameAware, Filter<String> {

	public static final int MIN_SERVER_BALANCE = 2;

	public boolean needsBalance(List<RegistryValue> values) {
		return values.size() >= MIN_SERVER_BALANCE;
	}

	public abstract ServerName assign(List<RegistryValue> values);

	
}
