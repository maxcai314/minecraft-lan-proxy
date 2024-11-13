package ax.xz.max.minecraft;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public record LanService(InetAddress address, int port, String motd) {
	public String getPingResponse() {
		return "[MOTD]" + motd + "[/MOTD][AD]" + port + "[/AD]";
	}

	public ByteBuffer getPayload() {
		return ByteBuffer.wrap(getPingResponse().getBytes(StandardCharsets.UTF_8));
	}

	public SocketAddress socketAddress() {
		return new InetSocketAddress(address, port);
	}

	public LanService withPort(int port) {
		return new LanService(address, port, motd);
	}
}
