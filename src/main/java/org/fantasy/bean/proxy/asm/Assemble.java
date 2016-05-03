package org.fantasy.bean.proxy.asm;

/**
 * 组装类的方法
 * @author fantasy
 *
 */
public interface Assemble {

	public void writeClassName();
	
	public void writeDeclaredFields();
	
	public void writeStaticInit();
	
	public void writeMethods();
	
	public void writeSetInterceptor();
	
	public void writeGetInterceptor();
	
	public void writeGetIndex();
	
	public void writeInvoke0();
	
	public void writeInvoke();

}
