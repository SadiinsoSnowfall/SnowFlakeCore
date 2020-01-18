package net.shadowpie.sadiinso.sfc.listeners.filter;

import net.dv8tion.jda.api.entities.Message;

public interface AbstractFilter {

	/**
	 * Used to filter the received messages
	 * @param event The received message
	 * @return false to drop the message else true
	 */
	boolean applyFilter(Message event);
	
}
