package org.fantasy.conf;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.fantasy.io.DefaultResourceLoader;
import org.fantasy.io.ResourceLoader;
import org.fantasy.net.NetUtils;
import org.fantasy.util.Constant;
import org.fantasy.util.StringUtils;

public class Configuration implements Iterable<Map.Entry<String, String>> {

//	private static final Logger LOG = Logger.getLogger(Configuration.class);
	private boolean loadDefault = true;
	private Map<String, String> props;
	private ResourceLoader resourceLoader;
	private String configLocation;
	
	public Configuration() {
		this(Constant.DEFAULT_CONFIG_LOCATION);
	}
	private Configuration(String configLocation) {
		this(configLocation, new DefaultResourceLoader());
	}
	public Configuration(String configLocation, ResourceLoader resourceLoader) {
		this(configLocation, resourceLoader, true);
	}

	private Configuration(String configLocation, ResourceLoader resourceLoader, boolean loadDefault) {
		this.resourceLoader = resourceLoader;
		this.loadDefault = loadDefault;
		this.configLocation = configLocation;
		if(this.loadDefault) {
			this.props = resourceLoader.load(configLocation);
		}
	}

	public Map<String, String> getProps() {
		if(props == null) {
			props = resourceLoader.load(configLocation);
		}
		return props;
	}
	
	
	public String get(String key) {
		return get(key, null);
	}
	
	public String get(String key, String defaultValue) {
		if(StringUtils.isEmpty(key))
			return defaultValue;
		String value = getProps().get(key);
		if(StringUtils.isEmpty(value))
			return defaultValue;
		return value;
	}
	
	public long getLong(String key, long defaultValue) {
		String value = get(key);
		if(value == null)
			return defaultValue;
		return Long.decode(value);
	}
	
	public long getLong(String key) {
		return getLong(key, 0L);
	}
	
	public Integer getInt(String key, int defaultValue) {
		String result = get(key);
		if(result == null)
			return defaultValue;
		return Integer.decode(result);
	}
	
	public Integer getInt(String key) {
		return getInt(key, 0);
	}

	public Iterator<Entry<String, String>> iterator() {
		return getProps().entrySet().iterator();
	}
	
	
	public Map<String, String> getSocketOptions() {
		Map<String, String> soMap = new HashMap<String, String>();
		for(Iterator<Entry<String, String>> iterator = iterator();iterator.hasNext();) {
			Map.Entry<String, String> entry = iterator.next();
			String key = entry.getKey();
			if(key.startsWith("socket."))
				soMap.put(key, entry.getValue());
		}
		return soMap;
	}
	
	public String[] getArray(String key) {
		return getArray(key, ",");
	}
	
	
	private String[] getArray(String key, String separator) {
		String value = getProps().get(key);
		if(StringUtils.isEmpty(value)) {
			return null;
		}
		return value.split(separator);
	}

	public void destory() {
		if(props != null) {
			props.clear();
		}
	}
	
	private enum Singleton {
		INSTANCE;
		
		private Configuration conf;
		
		public synchronized Configuration getInstance() {
			if(conf == null)
				conf = new Configuration();
			return conf;
		}

	}
	
	public static Configuration getConf() {
		return Singleton.INSTANCE.getInstance();
	}
	
	
	public String getBindAddress() {
		String address = get(Constant.BIND_ADDRESS_KEY);;
		if(address == null) {
			try {
				address = NetUtils.getLocalAddress();
			} catch (UnknownHostException e) {
				address = "localhost";
			}
		}
		return address;
	}
}
