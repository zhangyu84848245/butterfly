package org.fantasy.net.io.reader;

import java.io.IOException;
import java.util.Date;

import org.fantasy.net.io.unsafe.ObjectInput;

public class DateReader extends AbstractReader<Date> {

	public Date read(ObjectInput oin) throws IOException, ClassNotFoundException {
		return new Date(oin.readLong());
	}

}
