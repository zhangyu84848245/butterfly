package org.fantasy.bean.annotation.scanner;

public class ServiceBeanException extends RuntimeException {

	private static final long serialVersionUID = 1149933038836352083L;

	public ServiceBeanException(String message) {
		super(message);
	}
	
	public ServiceBeanException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public ServiceBeanException(Throwable cause) {
		super(cause);
	}
}
