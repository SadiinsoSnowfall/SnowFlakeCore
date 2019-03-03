package net.shadowpie.sadiinso.sfc.listeners;

import net.dv8tion.jda.core.events.Event;

public interface CustomEventHandler<T extends Event> {
	
	/**
	 * Handle the event
	 * @param event The event
	 */
	public abstract void handle(T event);
	
}
