package org.fantasy.bean;

public class ConsumerBean extends AbstractGenericBean {

	public ConsumerBean(String id, String beanClassName) {
		super(beanClassName, id);
	}

	public String getRefClassName() {
		return null;
	}

	public boolean isValid() {
		return true;
	}

}
