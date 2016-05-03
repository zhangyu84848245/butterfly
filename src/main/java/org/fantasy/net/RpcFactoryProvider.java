package org.fantasy.net;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;
import org.fantasy.conf.Configuration;
import org.fantasy.util.ClassUtils;
import org.fantasy.util.ReflectionUtils;
import org.fantasy.util.StringUtils;

public class RpcFactoryProvider {
	
	private static final Logger LOG = Logger.getLogger(RpcFactoryProvider.class);
	private RpcFactoryProvider() {
		
	}
	
	public static <F> F createFactory(Configuration conf, Endpoint endpoint) {
		if(conf == null) {
			throw new IllegalArgumentException("conf == null");
		}
		return (F)Singleton.INSTANCE.getInstance(conf, endpoint);
	}

	private enum Singleton {
		INSTANCE;
		private RpcFactory rpcFactory;
		public RpcFactory getInstance(Configuration conf, Endpoint endpoint) {
			if(rpcFactory == null) {
				String name = conf.get(endpoint.getFactoryKeyName());
				if(StringUtils.isEmpty(name)) {
					name = "org.fantasy.net.DefaultRpcFactory";
				}
				rpcFactory = (RpcFactory)ReflectionUtils.newInstance(ClassUtils.forName(name), conf);
//				{
//					name = "org.fantasy.rpc.DefaultRpcFactory";
//					Class<?> clazz = ClassUtils.forName(name);
//					Constructor cons;
//					try {
//						cons = clazz.getDeclaredConstructor(new Class[]{Configuration.class});
//						cons.setAccessible(true);
//						rpcFactory = (RpcFactory)cons.newInstance(conf);
//					} catch (Exception e) {
//						LOG.error("Create factory error.", e);
//						return null;
//					}
//				} else
//					rpcFactory = (RpcFactory)ReflectionUtils.newInstance(ClassUtils.forName(name), conf);
			}
			return rpcFactory;
		}
	}
	
	public static enum Endpoint implements NameFactory {
		SERVER("server"),
		CLIENT("client");
		
		private String name;
		private Endpoint(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}

		public String getEndPointKey() {
			StringBuilder builder = new StringBuilder();
			builder.append("rpc.").append(name).append(".class");
			return builder.toString();
		}

		public String getEndPointClass() {
			StringBuilder builder = new StringBuilder();
			builder.append("org.fantasy.net.").append(name).append(".Rpc");
			builder.append(name.substring(0, 1).toUpperCase());
			builder.append(name.substring(1));
			return builder.toString();
		}

		public String getFactoryKeyName() {
			StringBuilder builder = new StringBuilder();
			builder.append("rpc.").append(name).append(".factory.class");
			return builder.toString();
		}
		
	}
	
	interface NameFactory {
		public String getFactoryKeyName();
		public String getEndPointClass();
		public String getEndPointKey();
	}
}
