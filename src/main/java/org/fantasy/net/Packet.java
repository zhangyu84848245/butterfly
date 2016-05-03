package org.fantasy.net;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Packet {

	private int length;
	private ByteBuffer data;
	private SocketChannel channel;

	
	public Packet(int length, ByteBuffer data) {
		this(length, data, null);
	}
	
	public Packet(int length, ByteBuffer data, SocketChannel channel) {
		this.length = length;
		this.data = data;
		this.channel = channel;
	}
	
	
	public Packet(int length) {
		this.length = length;
		this.data = ByteBuffer.allocateDirect(length);
	}

	public int getLength() {
		return length;
	}

	public ByteBuffer getData() {
		return data;
	}

	public boolean isDone() {
		return !data.hasRemaining();
	}

	public SocketChannel getChannel() {
		return channel;
	}

	public void setLength(int length) {
		this.length = length;
	}

}
