package org.fantasy.common;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceConfigurationError;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.fantasy.util.ClassUtils;

public class SPILoader<T> implements Iterable<Entry<String, T>> {

	private static final Logger LOG = Logger.getLogger(SPILoader.class);
	private static final String PREFIX = "META-INF/services/";
	private Class<T> serviceClass;
	private ClassLoader classLoader;
	private String path;
	private Enumeration<URL> urls;
	private Map<String, T> providers = new LinkedHashMap<String, T>();
	private Iterator<Entry<String, T>> lazyIterator = new LazyIterator();
	
	private SPILoader(Class<T> serviceClass, ClassLoader cl) {
		this.serviceClass = serviceClass;
		this.classLoader = cl;
		this.path = PREFIX + serviceClass.getName();
		try {
			this.urls = cl.getResources(path);
		} catch (IOException e) {
			LOG.error("Configuration file's location should be " + path);
		}
		
	}
	
	public static <C> SPILoader<C> load(Class<C> clazz) {
		ClassLoader cl = ClassUtils.getClassLoader();
		return new SPILoader(clazz, cl);
	}

	public Iterator<Entry<String, T>> iterator() {

		return new Iterator<Entry<String, T>>() {

			private Iterator<Map.Entry<String, T>> cacheIterator = providers.entrySet().iterator();

			public boolean hasNext() {
				if(cacheIterator.hasNext())
					return true;
				return lazyIterator.hasNext();
			}

			public Entry<String, T> next() {
				if(cacheIterator.hasNext())
					return cacheIterator.next();
				return lazyIterator.next();
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}
	
	private Iterator<Map.Entry<String, String>> parse(URL url) {
		InputStream in = null;
		BufferedReader br = null;
		Map<String, String> map = new HashMap<String, String>();
		try {
			in = url.openStream();
			br = new BufferedReader(new InputStreamReader(in, "utf-8"));
			String line = null;
			while((line = br.readLine()) != null) {
				line = line.trim();
				if(line.startsWith("#"))
					continue;
				int n = line.length();
				char[] array = new char[n];
				String key = null, value = null;
				int start = 0;
				for(int i = 0; i < n; i++) {
					char c = line.charAt(i);
					if(c == ' ' || c == '\t')
						throw new ServiceConfigurationError("Illegal syntax");
					if(c == '=') {
						key = new String(array, 0, start);
						start = 0;
						continue;
					}
					array[start++] = c;
				}
				value = new String(array, 0, start);
				map.put(key, value);
			}
			return map.entrySet().iterator();
		} catch (IOException e) {
			throw new ServiceConfigurationError("Error reading configuration file.");
		} finally {
			try {
				if(in != null)
					in.close();
				if(br != null)
					br.close();
			} catch (IOException e) {
				
			}
		}
	}

	class LazyIterator implements Iterator<Entry<String, T>> {

		private Iterator<Map.Entry<String, String>> iterator;
		private Map.Entry<String, String> next;

		public boolean hasNext() {
			if(next != null)
				return true;
			while(iterator == null || !iterator.hasNext()) {
				if(!urls.hasMoreElements())
					return false;
				iterator = parse(urls.nextElement());
			}
			next = iterator.next();
			return true;
		}

		public Entry<String, T> next() {
			if(!hasNext())
				throw new NoSuchElementException();
			String key = next.getKey();
			String value = next.getValue();
			next = null;
			Class<?> clazz = ClassUtils.forName(value);
			T service = null;
			if(!serviceClass.isAssignableFrom(clazz))
				throw new ServiceConfigurationError(clazz.getName() + " is not a subtype of " + serviceClass.getName());
			try {
				service = serviceClass.cast(clazz.newInstance());
			} catch (Throwable ex) {
				throw new ServiceConfigurationError(clazz.getName() + " could not be instantiated", ex);
			}
			providers.put(key, service);
			return new ServiceEntry(key, service);
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	private class ServiceEntry implements Entry<String, T> {
		private String key;
		private T value;
		
		public ServiceEntry(String key, T value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public T getValue() {
			return value;
		}

		public T setValue(T value) {
			T old = this.value;
			this.value = value;
			return old;
		}

	}
	
}