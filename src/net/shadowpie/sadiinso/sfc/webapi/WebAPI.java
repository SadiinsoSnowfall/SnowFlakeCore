package net.shadowpie.sadiinso.sfc.webapi;

import com.fasterxml.jackson.databind.node.ObjectNode;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.shadowpie.sadiinso.sfc.config.SFConfig;
import net.shadowpie.sadiinso.sfc.config.SFConfig.Config;
import net.shadowpie.sadiinso.sfc.utils.SFUtils;
import org.slf4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class WebAPI {
	
	private static Logger logger;
	
	private static ServerSocket server;
	private static Thread serverThread;
	private static boolean init = false;
	
	public static void init() {
		Config cfg = SFConfig.queryConfig("socket_server");
		if (!cfg.exists()) {
			return;
		}
		
		boolean enabled = cfg.getBool("enable", true);
		if(!enabled) {
			return;
		}
		
		logger = JDALogger.getLog("WebAPI");
		
		logger.info("Initialing socket server...");
		String ip = cfg.getString("address", "localhost");
		int port = cfg.getInt("port", -1);
		
		WebEndpoints.init();
		
		logger.info("address=" + ip + " port=" + port);
		
		try {
			server = new ServerSocket(port, 10, InetAddress.getByName(ip));
		} catch (IOException e) {
			logger.error("Error while creating the socket server (port=" + port + ")", e);
			return;
		}
		
		if(WebEndpoints.size() == 0) {
			logger.error("No endpoints registered, cancelling socket server starting...");
		} else {
			serverThread = new Thread(() -> {
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
			}, "socket_server");
			
			serverThread.setDaemon(true);
			serverThread.start();
			init = true;
			logger.info("Loaded " + WebEndpoints.size() + " WebEndpoints !");
		}
	}
	
	public static void shutdown() {
		if (!init) {
			return;
		}
		
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
			if (socket.isClosed()) {
				return;
			}
			
			BufferedReader r = null;
			PrintWriter w = null;
			
			try {
				r = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
				w = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
				String str = r.readLine();
				String answer;
				
				if (str != null) {
					ObjectNode json = (ObjectNode) SFUtils.parseJSON(str);
					if (json == null) {
						answer = WebEndpoints.error_malformed_json;
					} else {
						answer = WebEndpoints.execute(json.get("cmd").asText(), json.get("data"));
						if (answer == null) {
							answer = WebEndpoints.error_unknown;
						}
					}
				} else {
					answer = WebEndpoints.simpleReply("null_request").toString();
				}
				
				w.println(answer);
			} catch (IOException e) {
				logger.error("Error while getting the client input stream", e);
			}
			
			// close the writer
			if (w != null) {
				w.flush();
				w.close();
			}
			
			// close the reader
			if (r != null) {
				try {
					r.close();
				} catch (IOException ignored) {
				}
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
