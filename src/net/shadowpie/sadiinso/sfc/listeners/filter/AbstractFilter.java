package net.shadowpie.sadiinso.sfc.listeners.filter;

import net.dv8tion.jda.core.entities.Message;

public interface AbstractFilter {

	/**
	 * Used to filter the received messages
	 * @param event The received message
	 * @return false to drop the message else true
	 */
	public boolean applyFilter(Message event);
	
}
