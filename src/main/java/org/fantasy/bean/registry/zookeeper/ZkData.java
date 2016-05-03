package org.fantasy.bean.registry.zookeeper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.fantasy.bean.registry.RegistryValue;
import org.fantasy.common.MethodDescriptor;

public class ZkData implements RegistryValue, Serializable {

	private static final long serialVersionUID = -4548387610113871721L;

	private String address;
	private int port;
	private List<String> methods = new ArrayList<String>();
	private Object attachment;
	
	public ZkData(String address, int port, List<MethodDescriptor> methodList) {
		if(address == null)
			throw new IllegalArgumentException(address);
		this.address = address;
		this.port = port;
		if(methodList != null) {
			Iterator<MethodDescriptor> iterator = methodList.iterator();
			while(iterator.hasNext()) {
				MethodDescriptor md = iterator.next();
				methods.add(md.getName());
			}
		}
	}

	public int getPort() {
		return port;
	}

	public String getAddress() {
		return address;
	}

	
	public List<String> getMethods() {
		return methods;
	}

	public Object getAttachment() {
		return attachment;
	}

	public void setAttachment(Object attachment) {
		this.attachment = attachment;
	}
	
	public int hashCode() {
		return 
				address.hashCode() * 29 + 
				port * 31 + 
				methods.hashCode(); //+ 
//				(attachment != null ? attachment.hashCode() : 0);
	}

	public boolean equals(Object other) {
		if(this == other)
			return true;
		
		if(!(other instanceof ZkData))
			return false;
		
		ZkData that = (ZkData)other;
		return 
				that.getAddress().equals(address) &&
				that.getPort() == port &&
				that.getMethods().equals(methods); //&&
				//attachment == null ? that.getAttachment() == null : attachment.equals(that.getAttachment());
		
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(address).append(":").append(port).append(";").append(methods.toString());
		if(attachment != null)
			result.append(";").append(attachment.toString());
		return result.toString();
	}

	  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException{
		  out.writeUTF(address);
		  out.writeInt(port);
		  out.writeInt(methods.size());
		  for(Iterator<String> iterator = methods.iterator();iterator.hasNext();) {
			  String method = iterator.next();
			  out.writeUTF(method);
		  }
		  out.writeObject(attachment);
	  }
	  
	  
	  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
		  address = in.readUTF();
		  port = in.readInt();
		  int size = in.readInt();
		  methods = new ArrayList<String>();
		  for(int i = 0; i < size; i++) {
			  methods.add(in.readUTF());
		  }
		  attachment = in.readObject();
	  }
	
}