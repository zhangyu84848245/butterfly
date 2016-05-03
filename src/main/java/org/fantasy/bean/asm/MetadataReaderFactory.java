package org.fantasy.bean.asm;

import java.io.File;
import java.io.IOException;



public interface MetadataReaderFactory {

	MetadataReader getMetadataReader(File file) throws IOException;

}
