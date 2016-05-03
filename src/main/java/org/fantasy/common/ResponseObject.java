package org.fantasy.common;

public class ResponseObject {

	private Throwable exception;
	private Object result;
	
	public ResponseObject(Throwable exception, Object result) {
		this.exception = exception;
		this.result = result;
	}
	
	public boolean hasException() {
		return exception != null;
	}

	public Throwable getException() {
		return exception;
	}

	public Object getResult() {
		return result;
	}

}
