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

	public static final String error_not_found = "{\"error\":\"handler_not_found\"}";
	public static final String error_exec = "{\"error\":\"error_during_request_execution\"}";
	public static final String error_unknown = "{\"error\":\"unknown_error\"}";
	public static final String error_malformed_json = "{\"error\":\"malformed_json\"}";

	private static final Logger logger = JDALogger.getLog("WebEndpoints");

	public static Map<String, WebEventCaller> handlers = new HashMap<>();

	public static int size() {
		return handlers.size();
	}

	public static String execute(String cmd, JSONObject data) {
		WebEventCaller handler = handlers.get(cmd);

		if (handler == null)
			return error_not_found;

		JSONObject answer = null;

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
		Method[] mhs = clazz.getDeclaredMethods();

		Arrays.stream(mhs).filter(WebEndpoints::isHandler).forEach(m -> {
			ASFWebEndpoint label = m.getAnnotation(ASFWebEndpoint.class);

			try {
				handlers.put(label.cmd(), WebEndpointHandler.createHandler(m));
			} catch (Throwable t) {
				logger.error("Error while creating web handler \"" + label.cmd() + "\"", t);
			}
		});
	}

	private static boolean isHandler(Method m) {
		if (!m.isAnnotationPresent(ASFWebEndpoint.class) || !Modifier.isStatic(m.getModifiers()) || !m.getReturnType().isAssignableFrom(JSONObject.class))
			return false;

		Class<?>[] params = m.getParameterTypes();
		if ((params.length != 1) || !params[0].isAssignableFrom(JSONObject.class))
			return false;

		return true;
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
