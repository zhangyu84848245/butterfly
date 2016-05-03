package org.fantasy.bean.registry.zookeeper;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.fantasy.bean.registry.RegistryKey;
import org.fantasy.bean.registry.RegistryValue;
import org.fantasy.bean.registry.ServiceRegistry;
import org.fantasy.conf.Configurable;
import org.fantasy.conf.Configuration;
import org.fantasy.service.AbstractService;
import org.fantasy.util.Constant;

public class ZookeeperRegistryWatcher extends AbstractService implements Watcher, ServiceRegistry {

	private static final Logger LOG = Logger.getLogger(ZookeeperRegistryWatcher.class);
	public String rootNode;
	private ZookeeperServiceRegistry zookeeper;
	private Configuration conf;
	
	public ZookeeperRegistryWatcher(/** Configuration conf */)/**  throws IOException */{
	}
	
	private void createRootNode() {
		ZkPath path = new ZkPath(rootNode, CreateMode.PERSISTENT);
		try {
			zookeeper.recursiveCreateNode(path);
		} catch (InterruptedException e) {
			throw new ZookeeperRegistryException(e);
		}
	}
	
	public void delete(RegistryKey key) throws Exception {
		zookeeper.delete(key);
	}

	public void serviceInit() {
		try {
			this.zookeeper = new ZookeeperServiceRegistry(this, conf);
			this.rootNode = Constant.ZOOKEEPER_REGISTRY_ROOT;
			createRootNode();
		} catch (IOException e) {
			LOG.error("Failed to startup zookeeper", e);
			throw new ZookeeperRegistryException(e);
		}
		super.serviceInit();
	}

	public void serviceStart() {
		super.serviceStart();
	}

	public void serviceStop() {

		super.serviceStop();
	}

	public void process(WatchedEvent event) {
		switch(event.getType()) {
			case None:
				handleEvent(event);
				break;
			case NodeCreated:
			case NodeDeleted: 
			case NodeDataChanged:
			case NodeChildrenChanged:
				break;
		}
	}

	private void handleEvent(WatchedEvent event) {
		switch(event.getState()) {
			case SyncConnected:
				break;
			case Expired:
			case Disconnected:
				try {
					zookeeper.reconnect();
				} catch (Exception e) {
					throw new ZookeeperRegistryException(e);
				} 
				break;
			case ConnectedReadOnly:
			case SaslAuthenticated:
			case AuthFailed:
				break;
			default:
				throw new IllegalStateException("Invalid state");
		}
	}

	public void setConfig(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return conf;
	}

	public String getName() {
		return zookeeper.getName();
	}

	public boolean accept(String name) {
		return zookeeper.accept(name);
	}

	public String create(RegistryKey key, RegistryValue value) throws Exception {
		return zookeeper.create(key, value);
	}

	public Set<RegistryValue> getData(RegistryKey key) throws Exception {
		return zookeeper.getData(key);
	}

	public void setData(RegistryKey key, RegistryValue value) throws Exception {
		zookeeper.setData(key, value);
	}

	public boolean exists(RegistryKey key) throws Exception {
		return zookeeper.exists(key);
	}

	public void close() {
		zookeeper.close();
	}

	public boolean deleteData(RegistryKey key, RegistryValue value) throws Exception {
		return zookeeper.deleteData(key, value);
	}

	public void setData(RegistryKey key, Set<RegistryValue> dataSet) throws Exception {
		zookeeper.setData(key, dataSet);
	}

}
