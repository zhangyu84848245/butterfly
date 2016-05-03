package org.fantasy.net;

import java.io.IOException;
import java.lang.reflect.Constructor;

import org.fantasy.conf.Configurable;
import org.fantasy.conf.Configuration;
import org.fantasy.net.RpcFactoryProvider.Endpoint;
import org.fantasy.net.client.ConnectionPool;
import org.fantasy.net.client.NioConnection;
import org.fantasy.net.client.RpcClient;
import org.fantasy.net.client.RpcClientException;
import org.fantasy.util.ClassUtils;
import org.fantasy.util.StringUtils;

public class DefaultRpcFactory implements RpcFactory , Configurable {

	private Configuration conf;
	private ConnectionPool pool;
	
	private DefaultRpcFactory(/** Configuration conf */) {
//		this.conf = conf;
	}

	public <T> T create(String bindAddress, int port, Endpoint endpoint) {
		String name = null;
		Class<?> clazz = null;
		Object obj = null;
		try {
			name = conf.get(endpoint.getEndPointKey());
			if(StringUtils.isEmpty(name))
				name = endpoint.getEndPointClass();
			clazz = ClassUtils.forName(name);
			Constructor<?> constructor = clazz.getDeclaredConstructor(new Class[]{String.class, int.class});
			constructor.setAccessible(true);
			obj = constructor.newInstance(bindAddress, port);
		} catch (Exception e) {
			throw new RuntimeException("Exception in create endpoint.", e);
		}
		if(obj instanceof Configurable)
			((Configurable)obj).setConfig(conf);
		
		if(obj instanceof RpcClient) {
			try {
				NioConnection connection = getPool().next();
				RpcClient client = (RpcClient)obj;
				client.setConnection(connection);
			} catch (IOException e) {
				throw new RpcClientException(e);
			}
		}
		return (T)obj;
	}

	public void setConfig(Configuration conf) {
		this.conf = conf;
	}

	public Configuration getConf() {
		return conf;
	}
	
	public ConnectionPool getPool() throws IOException {
		if(pool == null)
			pool = new ConnectionPool(conf);
		return pool;
		
	}

}
