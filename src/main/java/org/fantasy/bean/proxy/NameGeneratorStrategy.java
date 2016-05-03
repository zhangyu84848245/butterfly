package org.fantasy.bean.proxy;

/**
 * @author fantasy
 * 代理类名生成策略
 */
public interface NameGeneratorStrategy {
	
	String getClassName(String name);
	
	String getMethodProxyName(String name);
	
	String getFieldName(String name, int position);
	
	String getFieldProxyName(String name, int position);
}
