package org.fantasy.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.fantasy.util.ClassUtils;


public abstract class AbstractResourceLoader implements ResourceLoader {

	private static final Logger LOG = Logger.getLogger(AbstractResourceLoader.class);
//	private ClassLoader classLoader;
	
	public AbstractResourceLoader() {
//		this.classLoader = ClassUtils.getClassLoader();
//		this(ClassUtils.getClassLoader());
	}
	
//	public AbstractResourceLoader(ClassLoader classLoader) {
//		this.classLoader = classLoader;
//	}

	public ClassLoader getClassLoader() {
//		return classLoader == null ? ClassUtils.getClassLoader() : classLoader;
		return ClassUtils.getClassLoader();
	}
	
//	public void setClassLoader(ClassLoader classLoader) {
//		this.classLoader = classLoader;
//	}

//	public abstract <K, V> Map<K, V> load();

	public InputStream getInstream(String path) {
		ClassLoader cl = getClassLoader();
		return cl.getResourceAsStream(path);
	}
	
	public URL getResource(String location) {
		ClassLoader cl = getClassLoader();
		return cl.getResource(location);
	}

	public List<URL> getResources(String location) {
		ClassLoader cl = getClassLoader();
		List<URL> urls = new ArrayList<URL>();
		try {
			Enumeration<URL> enums = cl.getResources(location);
			while(enums.hasMoreElements()) {
				URL url = enums.nextElement();
				urls.add(url);
			}
		} catch (IOException e) {
			LOG.error("Can't open url, please check you path.");
			e.printStackTrace();
		}
		return urls;
	}

}
