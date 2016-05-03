package org.fantasy.net.io.unsafe;

import java.io.DataInput;
import java.io.IOException;

public interface ObjectInput extends DataInput, IndexAware {

	public Object readObject() throws ClassNotFoundException, IOException;
	
	public int available() throws IOException;
	
	public void close() throws IOException;
	
}
