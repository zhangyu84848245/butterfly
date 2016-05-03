package org.fantasy.net.io.unsafe;

import java.io.DataOutput;
import java.io.IOException;

public interface MarkableDataOutput extends DataOutput, IndexAware {

	public void mark() throws IOException;

	public int markValue() throws IOException;

	public void reset() throws IOException;

	public void discardMark() throws IOException;

}
