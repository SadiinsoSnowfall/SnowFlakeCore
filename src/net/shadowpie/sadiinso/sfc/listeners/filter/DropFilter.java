package net.shadowpie.sadiinso.sfc.listeners.filter;

import net.dv8tion.jda.core.entities.Message;

/**
 * Filter that drop all messages
 */
public class DropFilter implements AbstractFilter {

	@Override
	public boolean applyFilter(Message event) {
		return false;
	}

}
