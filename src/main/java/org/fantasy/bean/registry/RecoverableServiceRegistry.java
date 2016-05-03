package org.fantasy.bean.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.fantasy.service.AbstractService;

/**
 * 暂时没实现
 * @author fantasy
 *
 */
public abstract class RecoverableServiceRegistry extends AbstractService implements ServiceRegistry {
			
//	private Configuration conf;
	private final ConcurrentMap<RegistryKey, List<RegistryValue>> backupMap = new ConcurrentHashMap<RegistryKey, List<RegistryValue>>();

	public RecoverableServiceRegistry(/** Configuration conf */) {
//		this.conf = conf;
		
	}

	public void addRegistry(RegistryKey key, RegistryValue value) {
		if(key == null)
			throw new IllegalArgumentException("Registry key is null");
		List<RegistryValue> list = backupMap.get(key);
		if(list == null)
			list = new ArrayList<RegistryValue>();
		
		if(value != null)
			list.add(value);
		
		backupMap.put(key, list);
	}
	
//	public boolean removeRegistry
	
}
