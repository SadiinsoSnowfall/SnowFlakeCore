package net.shadowpie.sadiinso.snowflakecore.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.GuildBanEvent;
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent;
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.JDALogger;
import net.shadowpie.sadiinso.snowflakecore.commands.Commands;
import net.shadowpie.sadiinso.snowflakecore.commands.context.CommandContext;
import net.shadowpie.sadiinso.snowflakecore.commands.context.DiscordCommandContext;
import net.shadowpie.sadiinso.snowflakecore.config.ConfigHandler;
import net.shadowpie.sadiinso.snowflakecore.listeners.filter.AbstractFilter;

public class SFCListener extends ListenerAdapter {

	private static final Logger logger = JDALogger.getLog("SFCListener");

	private static final List<AbstractFilter> privateFilters = new ArrayList<>();
	private static final List<AbstractFilter> guildFilters = new ArrayList<>();

	private static final Map<EventType, List<CustomEventHandler<?>>> customHandlers = new EnumMap<>(EventType.class);

	/**
	 * Add a filter to received private messages
	 * 
	 * @param filter The filter to apply
	 */
	public static void addPrivateMessageFilter(AbstractFilter filter) {
		privateFilters.add(filter);
	}

	/**
	 * Add a filter to received guild messages
	 * 
	 * @param filter The filter to apply
	 */
	public static void addGuildMessageFilter(AbstractFilter filter) {
		guildFilters.add(filter);
	}

	/**
	 * Add a custom handler to the specified event
	 * 
	 * @param type    The event type to handler
	 * @param handler The handler to add
	 */
	public static <T extends Event> void addEventHandler(EventType type, CustomEventHandler<T> handler) {
		if (type.clazz != handler.clazz) {
			logger.error("Cannot bind handler, required class for EventType " + type + " is " + type.clazz + " got " + handler.clazz, new ClassCastException());
			return;
		}

		List<CustomEventHandler<?>> handlers = customHandlers.get(type);

		if (handlers == null) {
			handlers = new LinkedList<CustomEventHandler<?>>();
			customHandlers.put(type, handlers);
		}

		handlers.add(handler);
	}

	/**
	 * Custom getter with auto cast
	 */
	@SuppressWarnings("unchecked")
	private static <T extends Event> List<CustomEventHandler<T>> getHandlers(EventType type) {
		List<CustomEventHandler<T>> res = (List<CustomEventHandler<T>>) (Object) customHandlers.get(type);
		return (res == null ? Collections.EMPTY_LIST : res);
	}

	public SFCListener() {
		super();
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
		List<CustomEventHandler<PrivateMessageReceivedEvent>> handlers = getHandlers(EventType.PRIVATE_MSG_RECEIVED);
		for (CustomEventHandler<PrivateMessageReceivedEvent> handler : handlers)
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
		List<CustomEventHandler<GuildMessageReceivedEvent>> handlers = getHandlers(EventType.GUILD_MSG_RECEIVED);
		for (CustomEventHandler<GuildMessageReceivedEvent> handler : handlers)
			handler.handle(event);
	}

	@Override
	public void onGuildMessageUpdate(GuildMessageUpdateEvent event) {

		// execute handlers
		List<CustomEventHandler<GuildMessageUpdateEvent>> handlers = getHandlers(EventType.GUILD_MSG_UPDATE);
		for (CustomEventHandler<GuildMessageUpdateEvent> handler : handlers)
			handler.handle(event);
	}

	@Override
	public void onGuildMessageDelete(GuildMessageDeleteEvent event) {
		// execute handlers
		List<CustomEventHandler<GuildMessageDeleteEvent>> handlers = getHandlers(EventType.GUILD_MSG_DELETE);
		for (CustomEventHandler<GuildMessageDeleteEvent> handler : handlers)
			handler.handle(event);
	}

	// #############
	// GUILD MEMBERS
	// #############

	@Override
	public void onGuildMemberJoin(GuildMemberJoinEvent event) {
		// execute handlers
		List<CustomEventHandler<GuildMemberJoinEvent>> handlers = getHandlers(EventType.GUILD_MEMBER_JOIN);
		for (CustomEventHandler<GuildMemberJoinEvent> handler : handlers)
			handler.handle(event);
	}

	@Override
	public void onGuildMemberLeave(GuildMemberLeaveEvent event) {
		// execute handlers
		List<CustomEventHandler<GuildMemberLeaveEvent>> handlers = getHandlers(EventType.GUILD_MEMBER_LEAVE);
		for (CustomEventHandler<GuildMemberLeaveEvent> handler : handlers)
			handler.handle(event);
	}

	@Override
	public void onGuildMemberNickChange(GuildMemberNickChangeEvent event) {
		// execute handlers
		List<CustomEventHandler<GuildMemberNickChangeEvent>> handlers = getHandlers(EventType.GUILD_MEMBER_NICK_CHANGE);
		for (CustomEventHandler<GuildMemberNickChangeEvent> handler : handlers)
			handler.handle(event);
	}

	@Override
	public void onGuildBan(GuildBanEvent event) {
		// execute handlers
		List<CustomEventHandler<GuildBanEvent>> handlers = getHandlers(EventType.GUILD_BAN);
		for (CustomEventHandler<GuildBanEvent> handler : handlers)
			handler.handle(event);
	}

	@Override
	public void onGuildUnban(GuildUnbanEvent event) {
		// execute handlers
		List<CustomEventHandler<GuildUnbanEvent>> handlers = getHandlers(EventType.GUILD_UNBAN);
		for (CustomEventHandler<GuildUnbanEvent> handler : handlers)
			handler.handle(event);
	}

}
