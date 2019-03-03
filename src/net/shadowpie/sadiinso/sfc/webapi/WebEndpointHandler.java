package net.shadowpie.sadiinso.sfc.webapi;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import org.json.JSONObject;

public class WebEndpointHandler {

	@FunctionalInterface
	public interface WebEventCaller {
		public JSONObject execute(JSONObject data);
	}
	
	public static WebEventCaller createHandler(Method m) throws Throwable {
		var lookup = MethodHandles.lookup();
		var mh = lookup.unreflect(m);
		var command = (WebEventCaller) LambdaMetafactory.metafactory(lookup, "execute", MethodType.methodType(WebEventCaller.class), mh.type(), mh, mh.type()).getTarget().invokeExact();
		return command;
	}
}
