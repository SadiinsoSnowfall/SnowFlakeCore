package net.shadowpie.sadiinso.sfc.listeners;

import net.dv8tion.jda.api.events.GenericEvent;

public interface CustomEventHandler<T extends GenericEvent> {
	
	/**
	 * Handle the event
	 * @param event The event
	 */
	void handle(T event);
	
}
