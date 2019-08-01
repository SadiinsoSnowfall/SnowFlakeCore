package net.shadowpie.sadiinso.sfc.webapi;

import net.dv8tion.jda.core.utils.JDALogger;
import net.shadowpie.sadiinso.sfc.webapi.WebEndpointHandler.WebEventCaller;
import org.json.JSONObject;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WebEndpoints {

	public static String error_not_found;
	public static String error_exec;
	public static String error_unknown;
	public static String error_malformed_json;

	private static Logger logger;
	private static Map<String, WebEventCaller> handlers;

	public static int size() {
		return (handlers == null ? 0 : handlers.size());
	}

	static void init() {
		error_not_found = "{\"error\":\"handler_not_found\"}";
		error_exec = "{\"error\":\"error_during_request_execution\"}";
		error_unknown = "{\"error\":\"unknown_error\"}";
		error_malformed_json = "{\"error\":\"malformed_json\"}";
		
		logger = JDALogger.getLog("WebEndpoints");
	}
	
	public static String execute(String cmd, JSONObject data) {
		WebEventCaller handler = handlers.get(cmd);
		if (handler == null) {
			return error_not_found;
		}

		JSONObject answer;

		try {
			answer = handler.execute(data);
		} catch (Throwable t) {
			logger.error("An error occured in a webEventHandler", t);
			return error_exec;
		}

		return (answer == null ? "{}" : answer.toString());
	}

	/**
	 * Add all the web handlers declared in the given class A webHandler must have
	 * the ASFWebHandler annotation, be static and have a single JSONObject as
	 * parameters and return a JSONObject (can be null)
	 * 
	 * @param clazz The class to search in
	 */
	public static void addHandlers(Class<?> clazz) {
		if(handlers == null) {
			handlers = new HashMap<>();
		}
		
		Arrays.stream(clazz.getDeclaredMethods()).filter(WebEndpoints::isHandler).forEach(m -> {
			ASFWebEndpoint label = m.getAnnotation(ASFWebEndpoint.class);

			try {
				handlers.put(label.cmd(), WebEndpointHandler.createHandler(m));
			} catch (Throwable t) {
				logger.error("Error while creating web handler \"" + label.cmd() + "\"", t);
			}
		});
	}

	private static boolean isHandler(Method m) {
		if (!m.isAnnotationPresent(ASFWebEndpoint.class) || !Modifier.isStatic(m.getModifiers()) || !m.getReturnType().isAssignableFrom(JSONObject.class)) {
			return false;
		}

		Class<?>[] params = m.getParameterTypes();
		return (params.length == 1) && params[0].isAssignableFrom(JSONObject.class);
	}

	/**
	 * Return a JSONObject that contain a unique field "res" with the given message
	 * @param msg The message
	 */
	public static JSONObject simpleReply(String msg) {
		JSONObject reply = new JSONObject();
		reply.put("res", msg);
		return reply;
	}
}
