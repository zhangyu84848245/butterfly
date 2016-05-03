package org.fantasy.bean;

import org.fantasy.util.ClassUtils;
import org.fantasy.util.ReflectionUtils;

public class SimpleInstantiationStrategy implements InstantiationStrategy {

	public Object instantiate(GenericBean serviceBean) {
		Class<?> refClass = ClassUtils.forName(serviceBean.getRefClassName());
		return ReflectionUtils.newInstance(refClass);
	}

}
