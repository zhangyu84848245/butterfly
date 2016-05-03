package org.fantasy.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class DefaultResourceLoader extends AbstractResourceLoader {

	private static final Logger LOG = Logger.getLogger(AbstractResourceLoader.class);
	
	public DefaultResourceLoader() {
		super();
	}
	
//	public DefaultResourceLoader(ClassLoader classLoader) {
//		super(classLoader);
//	}
	
	public URI toURI(URL url) throws URISyntaxException {
		String str = url.toString();
		return new URI(str);
	}
	
	public Map<String, String> load(String location) {
		Map<String, String> map = new HashMap<String, String>();
		Properties prop = new Properties();
		List<URL> urls = getResources(location);
		try {
			for(Iterator<URL> iterator = urls.iterator();iterator.hasNext();) {
				URL url = iterator.next();
				InputStream in = url.openStream();
				prop.load(in);
			}
		} catch (IOException e) {
			LOG.error("Can't open config resource, please check your path.");
			e.printStackTrace();
		}
		for(Iterator<Map.Entry<Object, Object>> iterator = prop.entrySet().iterator();iterator.hasNext();) {
			Map.Entry<Object, Object> entry = iterator.next();
			String key = (String)entry.getKey();
			String value = (String)entry.getValue();
			map.put(key, value);
		}
//		return Collections.unmodifiableMap(map);
		return map;
	}
	
	public File getFileByURL(URL url) throws URISyntaxException {
		String path = url.toString();
		URI uri = new URI(path);
		File rootDir = new File(uri.getSchemeSpecificPart());
		if(!rootDir.exists() || !rootDir.isDirectory() || !rootDir.canRead()) {
			return null;
		}
		return rootDir;
	}

	public File[] files(File rootDir) {
		if(!rootDir.exists() || !rootDir.isDirectory() || !rootDir.canRead()) {
			return new File[0];
		}
		return rootDir.listFiles();
	}
}
