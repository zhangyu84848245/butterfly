package org.fantasy.bean.annotation.scanner;

import org.fantasy.bean.BeanCreatorCallback;

public interface BeanScanner {

	public void doScan(/** Class<?> annotationClass */);
	
	public void setBeanCallback(BeanCreatorCallback beanCallback);

}
