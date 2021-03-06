package mobi.omegacentauri.raspberryjammod.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import mobi.omegacentauri.raspberryjammod.RaspberryJamMod;
import mobi.omegacentauri.raspberryjammod.api.APIHandler;
import mobi.omegacentauri.raspberryjammod.api.APIHandlerClientOnly;
import mobi.omegacentauri.raspberryjammod.events.MCEventHandler;

public class WSServer extends WebSocketServer {
	private static boolean isLocal(InetAddress addr) {
		if (addr.isAnyLocalAddress() || addr.isLoopbackAddress()) {
			return true;
		}
		try {
			return null != NetworkInterface.getByInetAddress(addr);
		} catch (Exception e) {
			return false;
		}
	}

	Map<WebSocket, APIHandler> handlers;
	boolean controlServer;

	private MCEventHandler eventHandler;

	public WSServer(MCEventHandler eventHandler, int port, boolean clientSide) throws UnknownHostException {
		super(new InetSocketAddress(port));
		RaspberryJamMod.logger.info("Websocket server on " + port);
		controlServer = !clientSide;
		this.eventHandler = eventHandler;
		handlers = new HashMap<>();
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		RaspberryJamMod.logger.info("websocket closed for reason " + reason);
		APIHandler apiHandler = handlers.get(conn);
		if (apiHandler != null) {
			apiHandler.getWriter().close();
			handlers.remove(conn);
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		APIHandler apiHandler = handlers.get(conn);
		if (apiHandler != null) {
			apiHandler.process(message);
		}
	}

	@Override
	public void onOpen(final WebSocket conn, ClientHandshake handshake) {
		RaspberryJamMod.logger.info("websocket connect from " + conn.getRemoteSocketAddress().getHostName());
		if (!RaspberryJamMod.allowRemote && !isLocal(conn.getRemoteSocketAddress().getAddress())) {
			conn.closeConnection(1, "Remote connections disabled");
			return;
		}
		Writer writer = new Writer() {
			@Override
			public void close() throws IOException {
			}

			@Override
			public void flush() throws IOException {
			}

			@Override
			public void write(char[] data, int start, int len) throws IOException {
				conn.send(new String(data, start, len));
			}
		};
		PrintWriter pw = new PrintWriter(writer);
		try {
			APIHandler apiHandler = controlServer ? new APIHandler(eventHandler, pw)
					: new APIHandlerClientOnly(eventHandler, pw);

			handlers.put(conn, apiHandler);
		} catch (IOException e) {
		}
	}

	@Override
	public void stop() throws IOException, InterruptedException {
		super.stop();
	}
}
