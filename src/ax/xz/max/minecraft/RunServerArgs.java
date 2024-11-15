package ax.xz.max.minecraft;

import java.io.IOException;
import java.net.InetAddress;

public class RunServerArgs {
	public static void main(String[] args) throws InterruptedException, IOException {
		if (args.length != 3) {
			throw new IllegalArgumentException("Usage: <host_ip> <host_port> <motd>");
		}
				
		var host = InetAddress.getByName(args[0]);
		var service = new LanService(host, Integer.valueOf(args[1]), args[2]);

		try (var server = new LanProxyServer(service, 1234)) {
			server.start();

			Thread.sleep(Long.MAX_VALUE);
		}
	}
}
