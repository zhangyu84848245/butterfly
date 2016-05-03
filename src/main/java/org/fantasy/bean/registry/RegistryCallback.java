package org.fantasy.bean.registry;

import org.fantasy.context.BeanFactoryContext;

public interface RegistryCallback {

//	public void initialize();
	public void execute(BeanFactoryContext context);

}
