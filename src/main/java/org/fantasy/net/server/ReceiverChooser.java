package org.fantasy.net.server;

import java.util.concurrent.atomic.AtomicInteger;

public class ReceiverChooser implements Chooser<Reader> {

	private Reader[] receivers;
	private AtomicInteger recvIndex = new AtomicInteger();
	
	public ReceiverChooser(Reader[] receivers) {
		this.receivers = receivers;
	}
	public Reader next() {
		// 注意低位要是1
		return receivers[recvIndex.getAndIncrement() & receivers.length - 1];
	}

}
