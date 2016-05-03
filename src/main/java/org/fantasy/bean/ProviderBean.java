package org.fantasy.bean;

import org.fantasy.util.ClassUtils;
import org.fantasy.util.StringUtils;

public class ProviderBean extends AbstractGenericBean {

	private String refClassName;

	public ProviderBean(String refClassName, String beanClassName, String id) {
		super(beanClassName, id);
		this.refClassName = refClassName;
	}

	public String getRefClassName() {
		return refClassName;
	}

	// 验证注解的类只能是接口
	public boolean isValid() {
		Class<?> beanClass = ClassUtils.forName(getBeanClassName());
		if(StringUtils.isEmpty(refClassName))
			throw new BeanException("Reference class name can't be null");
		Class<?> refClass = ClassUtils.forName(refClassName);
		if(beanClass.isInterface()) {
			if(!beanClass.isAssignableFrom(refClass))
				throw new BeanException(refClassName + "can't be represented by " + getBeanClassName());
		}
		return true;
	}

	public void destory() {
		super.destory();
		this.refClassName = null;
	}

}
