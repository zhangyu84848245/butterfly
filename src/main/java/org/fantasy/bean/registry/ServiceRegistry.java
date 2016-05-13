package org.fantasy.bean.registry;

import java.io.Closeable;
import java.util.List;
import java.util.Set;

import org.fantasy.common.Filter;
import org.fantasy.common.NameAware;

public interface ServiceRegistry extends NameAware, Filter<String>, Closeable {

	public String create(RegistryKey key, RegistryValue value) throws Exception;

	public Set<RegistryValue> getData(RegistryKey key) throws Exception;

	public void setData(RegistryKey key, RegistryValue value) throws Exception;

	public boolean exists(RegistryKey key) throws Exception;
	
	public void delete(RegistryKey key) throws Exception;
	
	public boolean deleteData(RegistryKey key, RegistryValue value) throws Exception;
	
	public void setData(RegistryKey key, Set<RegistryValue> list) throws Exception;
 
	public List<String> getChildren(RegistryKey key);
}
