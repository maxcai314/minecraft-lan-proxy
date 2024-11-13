package ax.xz.max.minecraft;

import java.io.IOException;
import java.net.InetAddress;

public class RunServer {
	public static void main(String[] args) throws InterruptedException, IOException {
		var host = InetAddress.getByName("synergyserver.net");
		var service = new LanService(host, 25565, "Funny Redstone Server");

		try (var server = new LanProxyServer(service, 1234)) {
			server.start();

			Thread.sleep(Long.MAX_VALUE);
		}
	}
}