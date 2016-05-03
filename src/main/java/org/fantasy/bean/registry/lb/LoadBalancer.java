package org.fantasy.bean.registry.lb;

import java.util.List;

import org.fantasy.bean.registry.RegistryValue;

public interface LoadBalancer {

	public boolean needsBalance(List<RegistryValue> values);
	
	public ServerName assign(List<RegistryValue> values);

}
