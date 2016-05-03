package org.fantasy.bean;

import java.util.ArrayList;
import java.util.List;

import org.fantasy.common.MethodDescriptor;

public abstract class AbstractGenericBean implements GenericBean {

	private String beanClassName;
	private String id;
	private List<MethodDescriptor> methodList = new ArrayList<MethodDescriptor>();

	public AbstractGenericBean(String beanClassName, String id) {
		this.beanClassName = beanClassName;
		this.id = id;
	}

	public void setMethodList(List<MethodDescriptor> methodList) {
		this.methodList = methodList;
	}

	public String getBeanClassName() {
		return beanClassName;
	}

	public void destory() {
		this.beanClassName = null;
		this.methodList.clear();
	}

	public List<MethodDescriptor> getMethodList() {
		return methodList;
	}

	public String getId() {
		return id;
	}

}
