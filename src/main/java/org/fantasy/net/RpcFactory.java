package org.fantasy.net;

import java.io.Closeable;

import org.fantasy.net.RpcFactoryProvider.Endpoint;

public interface RpcFactory extends Closeable {

	public <T> T create(String bindAddress, int port, Endpoint endpoint);
}
