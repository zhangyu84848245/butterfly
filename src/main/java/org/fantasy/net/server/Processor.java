package org.fantasy.net.server;

import java.io.Serializable;

import org.fantasy.net.proto.RpcRequest;

public interface Processor {

	public void process(Serializable request);

}
