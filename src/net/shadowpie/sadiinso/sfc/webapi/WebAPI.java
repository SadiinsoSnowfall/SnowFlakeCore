package net.shadowpie.sadiinso.sfc.webapi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import net.dv8tion.jda.core.utils.JDALogger;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler.Config;
import net.shadowpie.sadiinso.sfc.webapi.base.BaseEndpoints;

public class WebAPI {

	private static final Logger logger = JDALogger.getLog("WebAPI");

	private static ServerSocket server;
	private static Thread serverThread;
	private static boolean init = false;

	public static void init() {
		Config cfg = ConfigHandler.queryConfig("socket_server");
		if (!cfg.exists())
			return;

		boolean enabled = cfg.getBool("enable", true);
		if(!enabled)
			return;
		
		logger.info("Initialing socket server...");
		String ip = cfg.getString("address", "localhost");
		int port = cfg.getInt("port", -1);

		logger.info("address=" + ip + " port=" + port);

		try {
			server = new ServerSocket(port, 10, InetAddress.getByName(ip));
		} catch (IOException e) {
			logger.error("Error while creating the socket server (port=" + port + ")", e);
			return;
		}

		// configure basics endpoints
		WebEndpoints.addHandlers(BaseEndpoints.class);

		serverThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!Thread.interrupted()) {
					try {
						Socket client = server.accept();
						SocketClientHandler.accept(client);
					} catch (IOException e) {
						logger.error("Socket error", e);
					}
				}

				try {
					server.close();
				} catch (IOException e) {
					logger.error("Error while closing the socketServer", e);
				}
			}
		}, "socket_server");

		serverThread.setDaemon(true);
		serverThread.start();
		init = true;
		logger.info("Loaded " + WebEndpoints.size() + " WebEndpoints !");
	}

	public static void shutdown() {
		if (!init)
			return;

		serverThread.interrupt();
	}

	static class SocketClientHandler implements Runnable {
		public static void accept(Socket socket) {
			new Thread(new SocketClientHandler(socket)).start();
		}

		private final Socket socket;

		public SocketClientHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			if (socket.isClosed())
				return;

			BufferedReader r = null;
			PrintWriter w = null;

			try {
				r = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				w = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
				String str = r.readLine();
				String answer = null;

				if (str != null) {
					try {
						JSONObject json = new JSONObject(str);
						answer = WebEndpoints.execute(json.getString("cmd"), json.getJSONObject("data"));
					} catch (JSONException ignored) {
						answer = WebEndpoints.error_malformed_json;
					}

					if (answer == null)
						answer = WebEndpoints.error_unknown;
				} else {
					answer = WebEndpoints.simpleReply("null_request").toString();
				}

				w.println(answer);
			} catch (IOException e) {
				logger.error("Error while getting the client input stream", e);
			}

			// close the writer
			w.flush();
			w.close();

			// close the reader
			try {
				r.close();
			} catch (IOException ignored) {
			}

			// close the socket if needed
			if (!socket.isClosed()) {
				try {
					socket.close();
				} catch (Exception e) {
					logger.error("Error while closing the client connection", e);
				}
			}
		}
	}

}
