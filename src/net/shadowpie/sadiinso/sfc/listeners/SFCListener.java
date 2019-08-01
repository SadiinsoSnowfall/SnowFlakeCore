package net.shadowpie.sadiinso.sfc.listeners;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.JDALogger;
import net.shadowpie.sadiinso.sfc.commands.Commands;
import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;
import net.shadowpie.sadiinso.sfc.commands.context.DiscordCommandContext;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler;
import net.shadowpie.sadiinso.sfc.listeners.eventwaiter.EventWaiter;
import net.shadowpie.sadiinso.sfc.listeners.filter.AbstractFilter;
import org.slf4j.Logger;

import java.util.*;

public class SFCListener extends ListenerAdapter {

	private static final Logger logger = JDALogger.getLog("SFCListener");
	
	private final List<AbstractFilter> privateFilters = new ArrayList<>();
	private final List<AbstractFilter> guildFilters = new ArrayList<>();

	private final Map<Class<? extends Event>, List<CustomEventHandler<? extends Event>>> customHandlers = new HashMap<>();

	/**
	 * Add a filter to received private messages
	 * 
	 * @param filter The filter to apply
	 */
	public void addPrivateMessageFilter(AbstractFilter filter) {
		privateFilters.add(filter);
	}

	/**
	 * Add a filter to received guild messages
	 * 
	 * @param filter The filter to apply
	 */
	public void addGuildMessageFilter(AbstractFilter filter) {
		guildFilters.add(filter);
	}

	/**
	 * Add a custom handler to the specified event
	 * 
	 * @param clazz   The event type
	 * @param handler The handler to add
	 */
	public <T extends Event> void addEventHandler(Class<T> clazz, CustomEventHandler<T> handler) {
		List<CustomEventHandler<?>> handlers = customHandlers.computeIfAbsent(clazz, e -> new LinkedList<>());
		handlers.add(handler);
	}

	/**
	 * Custom getter with auto cast
	 */
	@SuppressWarnings("unchecked")
	private <T extends Event> List<CustomEventHandler<T>> getHandlers(Class<T> clazz) {
		List<CustomEventHandler<T>> res = (List<CustomEventHandler<T>>) (Object) customHandlers.get(clazz);
		return (res == null ? Collections.EMPTY_LIST : res);
	}
	
	/**
	 * Default constructor
	 */
	public SFCListener() {
		super();
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void onGenericEvent(Event event) {
		// notify event waiter
		EventWaiter.onEvent(event);
		
		// execute generic handlers
		for (CustomEventHandler<Event> handler : getHandlers(Event.class)) {
			handler.handle(event);
		}
		
		// handle custom events
		if(event.getClass() != Event.class) {
			for (CustomEventHandler handler : getHandlers(event.getClass())) {
				handler.handle(event);
			}
		}
	}

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		// drop empty message and bot messages */
		if (event.getMessage().getContentRaw().isEmpty() || event.getAuthor().isBot())
			return;
		
		// apply filters
		for (AbstractFilter filter : privateFilters) {
			if (!filter.applyFilter(event.getMessage())) {
				return;
			}
		}
		
		if (ConfigHandler.enable_commands()) {

			// build command context (null if the message is not a command)
			CommandContext ctx = null;
			try {
				 ctx = DiscordCommandContext.getContext(event.getMessage());
			} catch(Exception e) {
				logger.error("Error while building a CommandContext; user=" + event.getAuthor().getName() + " (" + event.getAuthor().getId() + "); msg=\"" + event.getMessage().getContentRaw() + "\"", e);
			}

			// execute the command
			if (ctx != null) {
				Commands.execute(ctx);
			}
		}
	}

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		// drop empty messages
		if (event.getMessage().getContentRaw().isEmpty()) {
			return;
		}

		// apply filters
		for (AbstractFilter filter : guildFilters) {
			if (!filter.applyFilter(event.getMessage())) {
				return;
			}
		}

		// execute command
		if (ConfigHandler.enable_commands()) {
			// build command context (null if the message is not a command)
			CommandContext ctx = null;
			try {
				ctx = DiscordCommandContext.getContext(event.getMessage());
			} catch(Exception e) {
				logger.error("Error while building a CommandContext; user=" + event.getAuthor().getName() + " (" + event.getAuthor().getId() + "); msg=\"" + event.getMessage().getContentRaw() + "\"", e);
			}

			// execute the command
			if (ctx != null) {
				Commands.execute(ctx);
			}
		}
	}
	
}
