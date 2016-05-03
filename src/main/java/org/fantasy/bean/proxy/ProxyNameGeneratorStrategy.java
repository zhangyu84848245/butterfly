package org.fantasy.bean.proxy;

import java.util.concurrent.atomic.AtomicLong;

import org.fantasy.util.Constant;

public class ProxyNameGeneratorStrategy implements NameGeneratorStrategy {

	private static final AtomicLong NEXT = new AtomicLong(0);
	
	public String getClassName(String fullName) {
		String name = fullName.substring(fullName.lastIndexOf(".") + 1);
		return Constant.PROXY_PACKAGE_NAME + Constant.DOLLAR + name + NEXT.getAndIncrement();
	}

	public String getMethodProxyName(String name) {
		return name + Constant.PROXY_STR;
	}

	/**
	 * $$_methodName_METHOD_$$position
	 */
	public String getFieldName(String name, int position) {
		StringBuilder builder = new StringBuilder();
		builder.append(Constant.DOLLAR)
			.append(Constant.UNDERLINE)
			.append(name.toUpperCase())
			.append(Constant.UNDERLINE)
			.append(Constant.METHOD_STR )
			.append(Constant.UNDERLINE)
			.append(Constant.DOLLAR)
			.append(position);
		return  builder.toString();
	}

	/**
	 * $$_methodName_METHOD_PROXY_$$position
	 */
	public String getFieldProxyName(String name, int position) {
		StringBuilder builder = new StringBuilder();
		builder.append(Constant.DOLLAR)
			.append(Constant.UNDERLINE)
			.append(name.toUpperCase())
			.append(Constant.UNDERLINE)
			.append(Constant.METHOD_PROXY_STR)
			.append(Constant.UNDERLINE)
			.append(Constant.DOLLAR)
			.append(position);
		return builder.toString();
	}

}