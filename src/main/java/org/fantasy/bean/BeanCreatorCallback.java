package org.fantasy.bean;


public interface BeanCreatorCallback {

	GenericBean create(String beanId, String beanClassName, String refClassName);

	String getAnnotationName();
}
