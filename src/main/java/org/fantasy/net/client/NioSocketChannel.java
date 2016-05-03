package org.fantasy.net.client;

import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

import org.fantasy.common.MethodDescriptor;
import org.fantasy.net.proto.RpcResponse;

public interface NioSocketChannel {

	
	public SocketChannel getChannel();
	
//	public Socket socket();
//	
//	public void configureBlocking(boolean block);
	
	public boolean doConnect(SocketAddress remoteAddress) throws Exception;
	
//	public boolean isActive();
	
	public void doClose();
	
//	public void doFinishConnect() throws Exception;
	
	public boolean isOpen();

	public RpcResponse sendRequest(MethodDescriptor md, Object[] args) throws Exception;
}
