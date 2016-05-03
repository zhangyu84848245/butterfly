package org.fantasy.net.client;

import org.fantasy.net.AbstractFuture;
import org.fantasy.net.proto.HeartbeatResponse;

public class HeartbeatFuture extends AbstractFuture<HeartbeatResponse> {

	public HeartbeatFuture(long id) {
		super(id);
	}
}
