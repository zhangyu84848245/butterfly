package org.fantasy.bean;

import java.util.List;

import org.fantasy.common.MethodDescriptor;

public interface GenericBean {

	public String getBeanClassName();

	public String getRefClassName();
	
	public void setMethodList(List<MethodDescriptor> methodList);
	
	public void destory();
	
	public boolean isValid();

	public List<MethodDescriptor> getMethodList();
	
	public String getId();

}
