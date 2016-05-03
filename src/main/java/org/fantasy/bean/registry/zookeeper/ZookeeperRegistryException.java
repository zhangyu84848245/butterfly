package org.fantasy.bean.registry.zookeeper;

public class ZookeeperRegistryException extends RuntimeException {

	private static final long serialVersionUID = 2521981652882404955L;

	public ZookeeperRegistryException(Throwable ex) {
		super(ex);
	}

	public ZookeeperRegistryException(String message, Throwable ex) {
		super(message, ex);
	}

}
