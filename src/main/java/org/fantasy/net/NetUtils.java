package org.fantasy.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.fantasy.net.proto.PacketHeader;
import org.fantasy.util.Constant;
import org.fantasy.util.MemoryUtils;

public class NetUtils {

	private NetUtils() {
	}

	public static String getLocalHostName() throws UnknownHostException {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			String host = e.getMessage();
			if(host != null) {
				int colon = host.indexOf(':');
				if (colon > 0) {
					return host.substring(0, colon);
				}
			}
			throw e;
		}
	}

	public static String getLocalAddress() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostAddress();
	}
	
	/**
	 * read request header
	 * @param channel
	 * @return
	 * @throws IOException
	 */
	public static PacketHeader readRequestHeader(ReadableByteChannel channel) throws IOException {
		ByteBuffer header = ByteBuffer.allocateDirect(12);
		try {
			int readBytes = channelRead(channel, header);
			if(readBytes != header.capacity()) {
				throw new IOException("Read request header Error!");
			}
			header.flip();
			return PacketHeader.newBuilder().dataLength(header.getInt()).id(header.getLong()).build();
		} catch(ChannelException e) {
			return null;
		} finally {
			MemoryUtils.freeDirectBuffer(header);
		}
	}
	
	
	public static int channelRead(ReadableByteChannel channel, ByteBuffer buffer) throws IOException {
		int readBytes = (buffer.remaining() <= Constant.MAX_BUFFER_SIZE) ? channel.read(buffer) : channelIO(channel, buffer);
		if(readBytes < 0) {
			throw new ChannelException("Unable to read data fom stream, likely end of stream!");
		}
		return readBytes;
	}
	// GatheringByteChannel
	public static int channelIO(ReadableByteChannel channel, ByteBuffer buffer) throws IOException {
		int originalLimit = buffer.limit();
		int initialRemaining = buffer.remaining();
		int ret = 0;
		while(buffer.remaining() > 0) {
			try {
				int bufferSize = Math.min(buffer.remaining(), Constant.MAX_BUFFER_SIZE);
				buffer.limit(buffer.position() + bufferSize);
				ret = channel.read(buffer);
				if(ret < bufferSize) {
					break;
				}
			} finally {
				buffer.limit(originalLimit);
			}
		}
		int nBytes = initialRemaining - buffer.remaining();
		return (nBytes > 0) ? nBytes : ret;
	}

}
