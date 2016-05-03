package org.fantasy.bean.annotation.scanner;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.fantasy.bean.asm.ClassFileMetadataReader;
import org.fantasy.bean.asm.MetadataReader;
import org.fantasy.common.Cache;

public class MetadataReaderCache implements Cache<File, MetadataReader> {
	public static final int DEFAULT_CACHE_LIMIT = 256;
	private volatile int cacheLimit = DEFAULT_CACHE_LIMIT;

	private Map<File, MetadataReader> cache = new LinkedHashMap<File, MetadataReader>() {
		private static final long serialVersionUID = 3304742727010916302L;
		protected boolean removeEldestEntry(Map.Entry<File,MetadataReader> eldest) {
			return cacheLimit < size();
		};
	};

	public MetadataReader get(File key) {
		if(containsKey(key)) {
			return cache.get(key);
		} else {
			MetadataReader metadataReader = null;
			try {
				metadataReader = new ClassFileMetadataReader(key);
				cache.put(key, metadataReader);
			} catch (IOException e) {
				throw new ServiceBeanException(e);
			}
		}
		return cache.get(key);
	}

	public MetadataReader put(File key, MetadataReader value) {
		return cache.put(key, value);
	}

	public boolean containsKey(File key) {
		return cache.containsKey(key);
	}

	public int getCacheLimit() {
		return cacheLimit;
	}

	public void setCacheLimit(int cacheLimit) {
		this.cacheLimit = cacheLimit;
	}

	public void clear() {
		cache.clear();
	}

}
