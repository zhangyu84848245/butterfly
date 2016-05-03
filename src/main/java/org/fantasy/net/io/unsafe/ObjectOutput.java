package org.fantasy.net.io.unsafe;

import java.io.IOException;

public interface ObjectOutput extends MarkableDataOutput {

	public void writeObject(Object obj) throws IOException;

	public void flush() throws IOException;

	public void close() throws IOException;

}
