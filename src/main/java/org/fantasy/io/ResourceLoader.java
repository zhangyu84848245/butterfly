package org.fantasy.io;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public interface ResourceLoader {

	public ClassLoader getClassLoader();
	
	public <K, V> Map<K, V> load(String location);
	
	public InputStream getInstream(String path);
	
	public URL getResource(String location);
	
	public List<URL> getResources(String location);
	
	public URI toURI(URL url) throws URISyntaxException;
	
	public File[] files(File rootDir);
	
	public File getFileByURL(URL url) throws URISyntaxException;

}
