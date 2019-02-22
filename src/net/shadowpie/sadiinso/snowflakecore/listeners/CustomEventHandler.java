package net.shadowpie.sadiinso.snowflakecore.listeners;

import java.lang.reflect.ParameterizedType;

import net.dv8tion.jda.core.events.Event;

public abstract class CustomEventHandler<T extends Event> {
	
	public final Class<T> clazz;
	
	@SuppressWarnings("unchecked")
	public CustomEventHandler() {
		this.clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}
	
	/**
	 * Handle the event
	 * @param event The event
	 */
	public abstract void handle(T event);
	
}
