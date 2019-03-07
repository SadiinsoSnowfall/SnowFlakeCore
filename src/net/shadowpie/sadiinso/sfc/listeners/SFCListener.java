package net.shadowpie.sadiinso.sfc.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.priv.react.PrivateMessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.priv.react.PrivateMessageReactionRemoveEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.shadowpie.sadiinso.sfc.commands.Commands;
import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;
import net.shadowpie.sadiinso.sfc.commands.context.DiscordCommandContext;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler;
import net.shadowpie.sadiinso.sfc.listeners.eventwaiter.EventWaiter;
import net.shadowpie.sadiinso.sfc.listeners.filter.AbstractFilter;

public class SFCListener extends ListenerAdapter {

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
		List<CustomEventHandler<?>> handlers = customHandlers.computeIfAbsent(clazz, e -> new LinkedList<CustomEventHandler<?>>());
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
	
	// #############
	// GENERIC EVENT
	// #############
	
	@Override
	public void onGenericEvent(Event event) {
		// notify event waiter
		EventWaiter.onEvent(event);
		
		// execute handlers
		for (CustomEventHandler<Event> handler : getHandlers(Event.class))
			handler.handle(event);
	}
	
	@Override
	public void onMessageReactionAdd(MessageReactionAddEvent event) {
		// execute handlers
		for (CustomEventHandler<MessageReactionAddEvent> handler : getHandlers(MessageReactionAddEvent.class))
			handler.handle(event);
	}
	
	@Override
	public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
		// execute handlers
		for (CustomEventHandler<MessageReactionRemoveEvent> handler : getHandlers(MessageReactionRemoveEvent.class))
			handler.handle(event);
	}
	
	@Override
	public void onGenericMessageReaction(GenericMessageReactionEvent event) {
		// execute handlers
		for (CustomEventHandler<GenericMessageReactionEvent> handler : getHandlers(GenericMessageReactionEvent.class))
			handler.handle(event);
	}

	// ################
	// PRIVATE MESSAGES
	// ################

	@Override
	public void onPrivateMessageReceived(PrivateMessageReceivedEvent event) {
		// drop empty message and bot messages */
		if (event.getMessage().getContentRaw().isEmpty() || event.getAuthor().isBot())
			return;

		// apply filters
		for (AbstractFilter filter : privateFilters)
			if (!filter.applyFilter(event.getMessage()))
				return;

		if (ConfigHandler.enable_commands()) {

			// build command context (null if the message is not a command)
			CommandContext ctx = DiscordCommandContext.getContext(event.getMessage());

			// execute the command
			if (ctx != null)
				Commands.execute(ctx);
		}

		// execute handlers
		for (CustomEventHandler<PrivateMessageReceivedEvent> handler : getHandlers(PrivateMessageReceivedEvent.class))
			handler.handle(event);
	}
	
	@Override
	public void onPrivateMessageReactionAdd(PrivateMessageReactionAddEvent event) {
		// execute handlers
		for (CustomEventHandler<PrivateMessageReactionAddEvent> handler : getHandlers(PrivateMessageReactionAddEvent.class))
				handler.handle(event);
	}
	
	@Override
	public void onPrivateMessageReactionRemove(PrivateMessageReactionRemoveEvent event) {
		// execute handlers
		for (CustomEventHandler<PrivateMessageReactionRemoveEvent> handler : getHandlers(PrivateMessageReactionRemoveEvent.class))
			handler.handle(event);
	}

	// ##############
	// GUILD MESSAGES
	// ##############

	@Override
	public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
		// drop empty messages
		if (event.getMessage().getContentRaw().isEmpty())
			return;

		// apply filters
		for (AbstractFilter filter : guildFilters)
			if (!filter.applyFilter(event.getMessage()))
				return;

		// execute command
		if (ConfigHandler.enable_commands()) {

			// build command context (null if the message is not a command)
			CommandContext ctx = DiscordCommandContext.getContext(event.getMessage());

			// execute the command
			if (ctx != null)
				Commands.execute(ctx);
		}

		// execute handlers
		for (CustomEventHandler<GuildMessageReceivedEvent> handler : getHandlers(GuildMessageReceivedEvent.class))
			handler.handle(event);
	}

	@Override
	public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {

		// execute handlers
		for (CustomEventHandler<GuildMessageUpdateEvent> handler : getHandlers(GuildMessageUpdateEvent.class))
			handler.handle(event);
	}

	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
		// execute handlers
		for (CustomEventHandler<GuildMessageDeleteEvent> handler : getHandlers(GuildMessageDeleteEvent.class))
			handler.handle(event);
	}

	// #############
	// GUILD MEMBERS
	// #############

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		// execute handlers
		for (CustomEventHandler<GuildMemberJoinEvent> handler : getHandlers(GuildMemberJoinEvent.class))
			handler.handle(event);
	}

	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		// execute handlers
		for (CustomEventHandler<GuildMemberLeaveEvent> handler : getHandlers(GuildMemberLeaveEvent.class))
			handler.handle(event);
	}

	@Override
	public void onGuildMemberNickChange(GuildMemberNickChangeEvent event) {
		// execute handlers
		for (CustomEventHandler<GuildMemberNickChangeEvent> handler : getHandlers(GuildMemberNickChangeEvent.class))
			handler.handle(event);
	}

	@Override
	public void onGuildBan(GuildBanEvent event) {
		// execute handlers
		for (CustomEventHandler<GuildBanEvent> handler : getHandlers(GuildBanEvent.class))
			handler.handle(event);
	}

	@Override
	public void onGuildUnban(GuildUnbanEvent event) {
		// execute handlers
		for (CustomEventHandler<GuildUnbanEvent> handler : getHandlers(GuildUnbanEvent.class))
			handler.handle(event);
	}
	
	@Override
	public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
		// execute handlers
		for (CustomEventHandler<GuildMessageReactionAddEvent> handler : getHandlers(GuildMessageReactionAddEvent.class))
				handler.handle(event);
	}
	
	@Override
	public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
		// execute handlers
		for (CustomEventHandler<GuildMessageReactionRemoveEvent> handler : getHandlers(GuildMessageReactionRemoveEvent.class))
				handler.handle(event);
	}

}
