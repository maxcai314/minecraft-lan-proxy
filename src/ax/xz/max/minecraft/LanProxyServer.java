package ax.xz.max.minecraft;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LanProxyServer implements AutoCloseable {
	private final LanService service;
	private final ExecutorService executor;
	private final int port;

	public LanProxyServer(LanService service) {
		this.service = service;
		this.executor = Executors.newVirtualThreadPerTaskExecutor();
		this.port = 25565;
	}

	public LanProxyServer(LanService service, int port) {
		this.service = service;
		this.executor = Executors.newVirtualThreadPerTaskExecutor();
		this.port = port;
	}

	private static final SocketAddress MAGIC_ADDRESS = new InetSocketAddress("224.0.2.60", 4445);

	public void start() throws IOException {
		DatagramChannel datagramChannel;
		ServerSocketChannel serverSocketChannel;

		datagramChannel= DatagramChannel.open();
		try {
			serverSocketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(port));
		} catch (IOException e) {
			datagramChannel.close();
			throw e;
		}

		executor.execute(() -> advertiseLanServer(datagramChannel));
		executor.execute(() -> runProxy(serverSocketChannel));
	}

	private void advertiseLanServer(DatagramChannel datagramChannel) {
		ByteBuffer payload = service.withPort(port).getPayload();
		try (datagramChannel) {
			while (!Thread.interrupted()) {
				payload.clear();
				datagramChannel.send(payload, MAGIC_ADDRESS);

				Thread.sleep(1500);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void runProxy(ServerSocketChannel serverSocketChannel) {
		try (serverSocketChannel) {
			while (!Thread.interrupted()) {
				var client = serverSocketChannel.accept();
				executor.execute(() -> handleProxy(client));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void handleProxy(SocketChannel client) {
		try (client; var server = SocketChannel.open(service.socketAddress())) {
			var a = executor.submit(() -> forwardPackets(client, server));
			var b = executor.submit(() -> forwardPackets(server, client));

			a.get();
			b.get();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static void forwardPackets(ReadableByteChannel socketChannelIn, WritableByteChannel socketChannelOut) {
		try {
			ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4096); // 1 page

			while (!Thread.interrupted()) {
				inputBuffer.clear();
				int bytesReceived = socketChannelIn.read(inputBuffer); // gets data from In
				inputBuffer.flip();

				if (bytesReceived == -1) {
					throw new RuntimeException("Connection closed");
				}

				socketChannelOut.write(inputBuffer); // sends data to Out
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		executor.shutdownNow();
	}
}
