package net.shadowpie.sadiinso.sfc.listeners;

import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.shadowpie.sadiinso.sfc.commands.Commands;
import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;
import net.shadowpie.sadiinso.sfc.commands.context.DiscordCommandContext;
import net.shadowpie.sadiinso.sfc.config.SFConfig;
import net.shadowpie.sadiinso.sfc.listeners.eventwaiter.EventWaiter;
import net.shadowpie.sadiinso.sfc.listeners.filter.AbstractFilter;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.util.*;

public class SFCListener extends ListenerAdapter {

	private static final Logger logger = JDALogger.getLog("SFCListener");
	
	private final List<AbstractFilter> privateFilters = new ArrayList<>();
	private final List<AbstractFilter> guildFilters = new ArrayList<>();

	private final Map<Class<? extends GenericEvent>, List<CustomEventHandler<? extends GenericEvent>>> customHandlers = new HashMap<>();

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
	public <T extends GenericEvent> void addEventHandler(Class<T> clazz, CustomEventHandler<T> handler) {
		List<CustomEventHandler<?>> handlers = customHandlers.computeIfAbsent(clazz, e -> new LinkedList<>());
		handlers.add(handler);
	}

	/**
	 * Custom getter with auto cast
	 */
	@SuppressWarnings("unchecked")
	private <T extends GenericEvent> List<CustomEventHandler<T>> getHandlers(Class<T> clazz) {
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
	public void onGenericEvent(@Nonnull GenericEvent event) {
		// notify event waiter
		EventWaiter.onEvent(event);
		
		// execute generic handlers
		for (CustomEventHandler<GenericEvent> handler : getHandlers(GenericEvent.class)) {
			handler.handle(event);
		}
		
		// execute specific handlers
		Class<? extends GenericEvent> clazz = event.getClass();
		while(clazz != Event.class) {
			for (CustomEventHandler handler : getHandlers(clazz)) {
				handler.handle(event);
			}
			
			clazz = (Class<? extends Event>) clazz.getSuperclass();
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
		
		if (SFConfig.enable_commands()) {

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
		if (SFConfig.enable_commands()) {
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
