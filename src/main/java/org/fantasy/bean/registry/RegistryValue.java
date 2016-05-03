package org.fantasy.bean.registry;

import java.io.Serializable;
import java.util.List;

public interface RegistryValue extends Serializable {

	public Object getAttachment();

	public void setAttachment(Object attachment);

	public int getPort();

	public String getAddress();

	public List<String> getMethods();

}
