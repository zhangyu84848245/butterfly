package org.fantasy.bean.registry.zookeeper;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.fantasy.bean.registry.RecoverableServiceRegistry;
import org.fantasy.bean.registry.RegistryKey;
import org.fantasy.bean.registry.RegistryValue;
import org.fantasy.common.RetryCounter;
import org.fantasy.common.RetryCounterFactory;
import org.fantasy.conf.Configuration;
import org.fantasy.net.io.Deserializer;
import org.fantasy.net.io.Serialization;
import org.fantasy.net.io.SerializationFactory;
import org.fantasy.net.io.Serializer;
import org.fantasy.net.server.Chooser;
import org.fantasy.util.Constant;
import org.fantasy.util.StringUtils;

public class ZookeeperServiceRegistry /** implements ServiceRegistry */ extends RecoverableServiceRegistry {

	private static final Logger LOG = Logger.getLogger(ZookeeperServiceRegistry.class);
	private Watcher watcher;
	private ZooKeeper zooKeeper;
	private RetryCounterFactory retryFactory;
	private Serialization<Serializable> serialization;

	private Configuration conf;
	private Chooser<String> chooser;
	
	public ZookeeperServiceRegistry(Watcher watcher, Configuration conf) throws IOException {
		super();
		this.watcher = watcher;
		String serverAddress = conf.get("service.registry.address");
		chooser = new ZkAddressChooser(serverAddress);
		this.zooKeeper = new ZooKeeper(chooser.next(), conf.getInt("zookeeper.sessionTimeout"), watcher);
		retryFactory = new RetryCounterFactory(conf.getInt("zookeeper.maxAttempts"), conf.getLong("zookeeper.sleepInterval"));
		this.serialization = SerializationFactory.get("jdk");
		this.conf = conf;
	}
	
	
	public void recursiveCreateNode(ZkPath zkPath) throws InterruptedException {
		if(zkPath == null)
			throw new NullPointerException("path is null");

		String path = zkPath.getKey();
		if(path.startsWith("/"))
			path = path.substring(1);

		List<String> list = StringUtils.tokenizeToList(path, "/");
		String builder = "";
		
		for(Iterator<String> iterator = list.iterator();iterator.hasNext();) {
			builder += "/" + iterator.next();
			RetryCounter counter = retryFactory.create();
			for(;;) {
				try {
					zooKeeper.create(builder, null, Ids.OPEN_ACL_UNSAFE, zkPath.getCreateMode());
					break;
				} catch (KeeperException e) {
					Code code = e.code();
					if(code == Code.NODEEXISTS) {
						break;
					} else if(code == Code.CONNECTIONLOSS || code == Code.SESSIONEXPIRED || code == Code.OPERATIONTIMEOUT) {
						if(counter.shouldRetry()) {
							counter.doRetry();
							continue;
						}
						throw new ZookeeperRegistryException(e);
					} else {
						throw new ZookeeperRegistryException(e);
					}
				}
			}
		}
	}

	public String create(RegistryKey key, RegistryValue value) throws Exception {
		ZkPath path = ((ZkPath)key);
		List<ACL> acls = ((ZkPath)key).getAcls();
		if(acls == null)
			acls = Ids.OPEN_ACL_UNSAFE;
		RetryCounter counter = retryFactory.create();
		Set<RegistryValue> dataSet = null;
		
		boolean exists = exists(key);
		if(exists) {
			if(value != null) {
				setData(key, value);
			}
			return key.getKey();
		} else {
			dataSet = new HashSet<RegistryValue>();
			if(value != null)
				dataSet.add(value);
		}
		String result = null;
		Serializer<Serializable> serializer = serialization.getSerializer();
		while(true) {
			try {
				result = zooKeeper.create(key.getKey(), serializer.serialize((HashSet<RegistryValue>)dataSet).array(), acls, path.getCreateMode());
				super.addRegistry(key, value);
				return result;
			} catch (KeeperException e) {
				switch(e.code()) {
					case NODEEXISTS:
						return null;
					case CONNECTIONLOSS:
					case SESSIONEXPIRED:
					case OPERATIONTIMEOUT:
						if(counter.shouldRetry())
							break;
					default:
						throw e;
				}
			} finally {
				serializer.close();
				serializer = null;
			}
			counter.doRetry();
		}
	}
	
	/**
	 * 重连
	 */
	public void reconnect() {
		LOG.info("Closing zookeeper,  Ready to reconnect");
		RetryCounter counter = retryFactory.create();
		for(;;) {
			try {
				zooKeeper.close();
				zooKeeper = new ZooKeeper(chooser.next(), conf.getInt("zookeeper.sessionTimeout"), watcher);
				break;
			} catch (Exception e) {
				if(!counter.shouldRetry())
					throw new ZookeeperRegistryException("Unable to connect to zookeeper in max attempts", e);
			}
			counter.doRetry();
		}
		// 初始化
	}


	public Set<RegistryValue> getData(RegistryKey key) throws Exception {
		Stat stat = zooKeeper.exists(key.getKey(), watcher);
		if(stat != null) {
			RetryCounter counter = retryFactory.create();
			Deserializer<Serializable> deserializer = serialization.getDeserializer();
			for(;;) {
				try {
					byte[] data = zooKeeper.getData(key.getKey(), watcher, stat);
					return (HashSet<RegistryValue>)deserializer.deserialize(ByteBuffer.wrap(data));
				} catch (KeeperException e) {
					switch(e.code()) {
						case CONNECTIONLOSS:
						case SESSIONEXPIRED:
						case OPERATIONTIMEOUT:
							if(counter.shouldRetry())
								break;
							throw e;
						default:
							throw e;
					}
				} finally {
					deserializer.close();
					deserializer = null;
				}
				counter.doRetry();
			}
		}
		return null;
	}

	public boolean exists(RegistryKey key) throws Exception {
		Stat stat = null;
		RetryCounter counter = retryFactory.create();
		for(;;) {
			try {
				stat = zooKeeper.exists(key.getKey(), watcher);
				return stat != null ? true : false;
			} catch (KeeperException e) {
				switch(e.code()) {
					case CONNECTIONLOSS:
					case SESSIONEXPIRED:
					case OPERATIONTIMEOUT:
						if(counter.shouldRetry())
							break;
						throw e;
					default:
						throw e;
				}
			}
			counter.doRetry();
		}
	}

	/**
	 * setData 要保证key一定存在
	 */
	public void setData(RegistryKey key, RegistryValue value) throws Exception  {
		HashSet<RegistryValue> dataSet = (HashSet<RegistryValue>)getData(key);
		if(dataSet == null)
			dataSet = new HashSet<RegistryValue>();
		dataSet.add(value);
		RetryCounter counter = retryFactory.create();
		Serializer<Serializable> serializer = serialization.getSerializer();
		for(;;) {
			try {
				zooKeeper.setData(key.getKey(), serializer.serialize(dataSet).array(), -1);
				break;
			} catch (KeeperException e) {
				switch(e.code()) {
					case CONNECTIONLOSS:
					case SESSIONEXPIRED:
					case OPERATIONTIMEOUT:
						if(counter.shouldRetry())
							break;
						throw e;
					default:
						throw e;
				}
			} finally {
				serializer.close();
				serializer = null;
			}
			counter.doRetry();
		}
	}

	public String getName() {
		return "zookeeper";
	}

	public boolean accept(String name) {
		if(StringUtils.isEmpty(name))
			return false;
		return name.equals(getName());
	}

	public void delete(RegistryKey key) throws Exception {
		int version = ((ZkPath)key).getVersion();
		RetryCounter counter = retryFactory.create();
		for(;;) {
			try {
				zooKeeper.delete(key.getKey(), version);
				break;
			} catch (KeeperException e) {
				switch(e.code()) {
					case NONODE:
						LOG.info("Node " + key.getKey() + " already deleted");
						return;
					case CONNECTIONLOSS:
					case SESSIONEXPIRED:
					case OPERATIONTIMEOUT:
						if(counter.shouldRetry())
							break;
						throw e;
					default:
						throw e;
				}
			}
			counter.doRetry();
		}
	}
	
	List<String> getChildren(String path) {
		RetryCounter counter = retryFactory.create();
		for(;;) {
			try {
				return zooKeeper.getChildren(path, watcher);
			} catch (KeeperException e) {
				switch(e.code()) {
					case CONNECTIONLOSS:
					case SESSIONEXPIRED:
					case OPERATIONTIMEOUT:
						LOG.error(e.code());
						if(counter.shouldRetry())
							break;
						throw new ZookeeperRegistryException(e);
					default:
						throw new ZookeeperRegistryException(e);
				}
			} catch (InterruptedException e) {
				throw new ZookeeperRegistryException(e);
			}
			counter.doRetry();
		}
	}

	public void close() {
		try {
			deleteChild(Constant.ZOOKEEPER_REGISTRY_ROOT);
		} catch (Exception e1) {}
		
		try {
			zooKeeper.close();
		} catch (Exception e) {
			LOG.error("Closing zookeeper error", e);
			// ignore
		}
	}
	
	private void deleteChild(String path) throws Exception {
		List<String> children = getChildren(path);
		if(children.size() == 0) {
			delete(new ZkPath(path));
		} else {
			for(Iterator<String> iterator = children.iterator();iterator.hasNext();) {
				deleteChild(path + Constant.SLASH + iterator.next());
			}
			if(path.equals(Constant.ZOOKEEPER_REGISTRY_ROOT))
				return;
			delete(new ZkPath(path));
		}
	}

	/**
	 * setData 要保证key一定存在
	 */
	public boolean deleteData(RegistryKey key, RegistryValue value) throws Exception {
		boolean result = false;
		HashSet<RegistryValue> dataSet = (HashSet<RegistryValue>)getData(key);
		if(dataSet == null)
			return false;
		result = dataSet.remove(value);
		if(!result)
			return result;
		setData(key, dataSet);
		return true;
	}

	public void setData(RegistryKey key, Set<RegistryValue> dataSet) throws Exception {
		RetryCounter counter = retryFactory.create();
		Serializer<Serializable> serializer = serialization.getSerializer();
		for(;;) {
			try {
				zooKeeper.setData(key.getKey(), serializer.serialize((HashSet<RegistryValue>)dataSet).array(), -1);
				break;
			} catch (KeeperException e) {
				switch(e.code()) {
					case CONNECTIONLOSS:
					case SESSIONEXPIRED:
					case OPERATIONTIMEOUT:
						if(counter.shouldRetry())
							break;
						throw e;
					default:
						throw e;
				}
			} finally {
				serializer.close();
				serializer = null;
			}
			counter.doRetry();
		}
	}

}
