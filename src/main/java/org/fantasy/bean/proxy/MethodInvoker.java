package org.fantasy.bean.proxy;

import java.lang.reflect.InvocationTargetException;

import org.fantasy.bean.proxy.asm.Signature;

public interface MethodInvoker {
	
	public Object invoke(int index, Object target, Object[] args) throws InvocationTargetException;

	public int getIndex(Signature signature);

}
