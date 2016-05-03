package org.fantasy.net.io.reader;

import java.io.IOException;
import java.util.Date;

import org.fantasy.net.io.unsafe.ObjectOutput;
import org.fantasy.net.io.writer.AbstractWriter;
import org.fantasy.util.Constant;

public class DateWriter extends AbstractWriter<Date> {

	public void write(Date object, ObjectOutput oout) throws IOException {
		oout.writeByte(Constant.TYPE_DATE);
		oout.writeLong(object.getTime());
	}

	
}
