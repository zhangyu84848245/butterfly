package org.fantasy.net;

import org.fantasy.net.RpcFactoryProvider.Endpoint;

public interface RpcFactory {

	public <T> T create(String bindAddress, int port, Endpoint endpoint);
}
