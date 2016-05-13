package org.fantasy.context;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.fantasy.bean.BeanException;
import org.fantasy.bean.GenericBean;
import org.fantasy.bean.factory.BeanInstanceFactory;
import org.fantasy.bean.factory.ServiceBeanRegistry;
import org.fantasy.bean.registry.RegistryValue;
import org.fantasy.bean.registry.ServiceRegistry;
import org.fantasy.bean.registry.lb.LoadBalancer;
import org.fantasy.bean.registry.lb.ServerName;
import org.fantasy.bean.registry.zookeeper.ZkData;
import org.fantasy.bean.registry.zookeeper.ZkPath;
import org.fantasy.bean.registry.zookeeper.ZookeeperRegistryException;
import org.fantasy.common.FactoryProvider;
import org.fantasy.conf.Configuration;
import org.fantasy.net.NetUtils;
import org.fantasy.net.RpcFactory;
import org.fantasy.net.RpcFactoryProvider;
import org.fantasy.net.RpcFactoryProvider.Endpoint;
import org.fantasy.net.client.RpcClient;
import org.fantasy.util.Constant;
import org.fantasy.util.ReflectionUtils;



public class BeanFactoryContext implements Closeable {

	private static final Logger LOG = Logger.getLogger(BeanFactoryContext.class);
	private ServiceRegistry serviceRegistry;
	private ServiceBeanRegistry beanRegistry;
	private Configuration conf;
	private String address;
	private int port;
	private FactoryProvider<LoadBalancer> lbProvider;
	private RpcFactory rpcFactory;
	private BeanInstanceFactory beanFactory;
	private ConcurrentMap<RpcClient, String> clientMap = new ConcurrentHashMap<RpcClient, String>();

	public BeanFactoryContext(ServiceRegistry serviceRegistry, ServiceBeanRegistry beanRegistry, Configuration conf) {
		this.serviceRegistry = serviceRegistry;
		this.beanRegistry = beanRegistry;
		this.beanFactory = (BeanInstanceFactory)beanRegistry;
		this.conf = conf;
		this.address = conf.getBindAddress();
		this.port = conf.getInt(Constant.BIND_PORT_KEY);
	}
	
//	public void register() {
//		String[] beanNames = beanRegistry.getBeanNames();
//		for(String name : beanNames) {
//			GenericBean bean = beanRegistry.getBean(name);
//			StringBuilder key = new StringBuilder();
//			key.append(Constant.ZOOKEEPER_REGISTRY_ROOT).append(Constant.SLASH).append(bean.getBeanClassName());
//			ZkPath path = new ZkPath(key.toString(), CreateMode.PERSISTENT);
//			ZkData data = new ZkData(address, port, bean.getMethodList());
//			try {
//				/**  /fantasy/butterfly/interface name  */
//				serviceRegistry.create(path, null);
//				/**  /fantasy/butterfly/interface name/implement name  */
//				key.append(Constant.SLASH).append(bean.getId());
//				path = new ZkPath(key.toString(), CreateMode.EPHEMERAL);
//				serviceRegistry.create(path, data);
//			} catch (Exception e) {
//				throw new ZookeeperRegistryException(e);
//			}
//		}
////		 debug
////		((ZookeeperRegistryWatcher)serviceRegistry).close();
//	}
	
	public void register() {
		String[] beanNames = beanRegistry.getBeanNames();
		for(String name : beanNames) {
			GenericBean bean = beanRegistry.getBean(name);
			StringBuilder key = new StringBuilder();
			/**  /fantasy/butterfly/interfaceName/implementName  */
			key.append(Constant.ZOOKEEPER_REGISTRY_ROOT).append(Constant.SLASH).append(bean.getBeanClassName());
			ZkPath path = new ZkPath(key.toString(), CreateMode.PERSISTENT);
			ZkData data = new ZkData(address, port, bean.getMethodList());
			try {
				serviceRegistry.create(path, null);
				key.append(Constant.SLASH).append(bean.getId());
				path = new ZkPath(key.toString(), CreateMode.PERSISTENT);
				serviceRegistry.create(path, null);
				key.append(Constant.SLASH).append(data.toString());
				path = new ZkPath(key.toString(), CreateMode.EPHEMERAL);
				serviceRegistry.create(path, null);
			} catch (Exception e) {
				throw new ZookeeperRegistryException(e);
			}
		}
	}
	
	private String getPath(GenericBean bean) {
		StringBuilder key = new StringBuilder();
		key.append(Constant.ZOOKEEPER_REGISTRY_ROOT).append(Constant.SLASH).append(bean.getBeanClassName());
		key.append(Constant.SLASH).append(bean.getId());
		return key.toString();
	}
	
	private FactoryProvider<LoadBalancer> getLBProvider() {
		if(lbProvider == null) {
			lbProvider = new FactoryProvider<LoadBalancer>(conf, LoadBalancer.class);
		}
		return lbProvider;
	}
	
	private LoadBalancer getLoadBalancer(String key) {
		LoadBalancer lb = getLBProvider().get(key);
		if(lb == null)
			lb = getLBProvider().get("random");
		return lb;
	}
//	public void subscribe(/**  Endpoint endpoint  */) {
//		String[] beanNames = beanRegistry.getBeanNames();
//		LoadBalancer lb = getLoadBalancer(conf.get(Constant.LOADBALANCE_TYPE_KEY, "random"));
//		for(String beanName : beanNames) {
//			GenericBean bean = beanRegistry.getBean(beanName);
//			String key = getPath(bean);
//			ZkPath path = new ZkPath(key);
//			try {
//				Set<RegistryValue> values = serviceRegistry.getData(path);
//				if(values == null || values.size() == 0) {
//					LOG.error("No server available");
//					return;
//				}
//				List<RegistryValue> list = new ArrayList<RegistryValue>();
//				list.addAll(values);
//				// help gc
//				values.clear();
//				values = null;
//				ServerName server = lb.assign(list);
//				RpcClient client = createRpcClient(server, beanName);
//				registerBeanInstance(beanName, client);
//				clientMap.put(client, beanName);
//			} catch (Exception e) {
//				throw new ZookeeperRegistryException(e);
//			}
//		}
//	}

	public void subscribe(/**  Endpoint endpoint  */) {
		String[] beanNames = beanRegistry.getBeanNames();
		LoadBalancer lb = getLoadBalancer(conf.get(Constant.LOADBALANCE_TYPE_KEY, "random"));
		for(String beanName : beanNames) {
			GenericBean bean = beanRegistry.getBean(beanName);
			String key = getPath(bean);
			ZkPath path = new ZkPath(key);
			try {
//				Set<RegistryValue> values = serviceRegistry.getData(path);
				List<String> children = serviceRegistry.getChildren(path);
				if(children == null || children.size() == 0) {
					LOG.error("No server available");
					return;
				}
				List<RegistryValue> list = new ArrayList<RegistryValue>();
				for(Iterator<String> iterator = children.iterator();iterator.hasNext();) {
					String child = iterator.next();
					list.add(new ZkData(child));
				}
//				list.addAll(values);
				// help gc
				children.clear();
				children = null;
				ServerName server = lb.assign(list);
				RpcClient client = createRpcClient(server, beanName);
				registerBeanInstance(beanName, client);
				clientMap.put(client, beanName);
			} catch (Exception e) {
				throw new ZookeeperRegistryException(e);
			}
		}
	}
	
	private RpcClient createRpcClient(ServerName server, String beanId) {
		RpcFactory factory = getRpcFactory(Endpoint.CLIENT);
		RpcClient client = factory.create(server.getAddress(), server.getPort(), Endpoint.CLIENT);
		client.setBeanContext(this);
		client.start();
		return client;
	}

	public RpcFactory getRpcFactory(Endpoint endpoint) {
		if(rpcFactory == null)
			rpcFactory = RpcFactoryProvider.createFactory(conf, endpoint);
		return rpcFactory;
	}
	
	
	private void registerBeanInstance(String beanName, RpcClient client) {
		Object beanInstance = beanFactory.getBeanInstance(beanName);
		if(beanInstance == null)
			throw new BeanException("No such bean instance");
		Object object = null;
		do {
			object = beanInstance;
			beanInstance = ReflectionUtils.getFieldValue(beanInstance, "interceptor");
		} while(beanInstance != null);
		beanInstance = null;
		Field field = ReflectionUtils.getField(object.getClass(), "client");
		field.setAccessible(true);
		try {
			field.set(object, client);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
	
	public void reconnect(RpcClient oldClient) {
		String address = oldClient.getBindAddress();
		int port = oldClient.getPort();
		String beanId = clientMap.remove(oldClient);
		oldClient.stop();
		GenericBean bean = beanRegistry.getBean(beanId);
		ZkData data = new ZkData(address, port, bean.getMethodList());
		String key = getPath(bean);
		ZkPath path = new ZkPath(key + Constant.SLASH + data.toString());
		List<RegistryValue> list = new ArrayList<RegistryValue>();
		try {
			serviceRegistry.delete(path);
			path = new ZkPath(key);
			List<String> values = serviceRegistry.getChildren(path);
			if(values == null || values.size() == 0) {
				LOG.error("No server available");
				return;
			}
			for(Iterator<String> iterator = values.iterator();iterator.hasNext();) {
				String child = iterator.next();
				list.add(new ZkData(child));
			}
			// help gc
			values.clear();
			values = null;
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw new ZookeeperRegistryException(e);
		}

		LoadBalancer lb = getLoadBalancer(conf.get(Constant.LOADBALANCE_TYPE_KEY));
		ServerName server = lb.assign(list);
		RpcClient client = createRpcClient(server, beanId);
		registerBeanInstance(beanId, client);
		clientMap.put(client, beanId);
	}

//	public void reconnect(RpcClient oldClient) {
//		String address = oldClient.getBindAddress();
//		int port = oldClient.getPort();
//		String beanId = clientMap.remove(oldClient);
//		oldClient.stop();
//		GenericBean bean = beanRegistry.getBean(beanId);
//		ZkPath path = new ZkPath(getPath(bean), CreateMode.EPHEMERAL);
//		ZkData data = new ZkData(address, port, bean.getMethodList());
//		List<RegistryValue> list = new ArrayList<RegistryValue>();
//		try {
//			serviceRegistry.deleteData(path, data);
//			Set<RegistryValue> values = serviceRegistry.getData(path);
//			if(values == null || values.size() == 0) {
//				LOG.error("No server available");
//				return;
//			}
//			list.addAll(values);
//			// help gc
//			values.clear();
//			values = null;
//		} catch (Exception e) {
//			LOG.error(e.getMessage());
//			throw new ZookeeperRegistryException(e);
//		}
//
//		LoadBalancer lb = getLoadBalancer(conf.get(Constant.LOADBALANCE_TYPE_KEY));
//		ServerName server = lb.assign(list);
//		RpcClient client = createRpcClient(server, beanId);
//		registerBeanInstance(beanId, client);
//		clientMap.put(client, beanId);
//	}

	public void close() throws IOException {
		Set<RpcClient> clients = clientMap.keySet();
		for(Iterator<RpcClient> iterator = clients.iterator();iterator.hasNext();) {
			RpcClient client = iterator.next();
			client.getIoHandler().getHeartbeatHandler().stop();
			client.stop();
		}
		clientMap.clear();
		if(rpcFactory != null) {
			rpcFactory.close();
			rpcFactory = null;
		}
		
		if(lbProvider != null) {
			lbProvider.close();
			lbProvider = null;
		}
	}

}