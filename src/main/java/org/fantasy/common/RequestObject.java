package org.fantasy.common;

import org.fantasy.common.MethodDescriptor;

public class RequestObject {

	private MethodDescriptor methodDescriptor;
	private Object[] arguments;

	public RequestObject(MethodDescriptor methodDescriptor, Object[] arguments) {
		this.methodDescriptor = methodDescriptor;
		this.arguments = arguments;
	}

	public MethodDescriptor getMethodDescriptor() {
		return methodDescriptor;
	}

	public Object[] getArguments() {
		return arguments;
	}
	
}
