package org.fantasy.bean.asm;

import java.io.File;
import java.io.IOException;

import org.fantasy.bean.annotation.scanner.MetadataReaderCache;
import org.fantasy.common.Cache;

public class CachedMetadataReaderFactory implements MetadataReaderFactory {

	private Cache<File, MetadataReader> cache = new MetadataReaderCache();
	
	public synchronized MetadataReader getMetadataReader(File file) throws IOException {
		return cache.get(file);
	}
	
	public synchronized void clear() {
		cache.clear();
	}
}
