package net.shadowpie.sadiinso.snowflakecore.listeners;

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

public enum EventType {

	PRIVATE_MSG_RECEIVED(PrivateMessageReceivedEvent.class),
	
	GUILD_MSG_RECEIVED(GuildMessageReceivedEvent.class),
	GUILD_MSG_UPDATE(GuildMessageUpdateEvent.class),
	GUILD_MSG_DELETE(GuildMessageDeleteEvent.class),
	GUILD_MEMBER_JOIN(GuildMemberJoinEvent.class),
	GUILD_MEMBER_LEAVE(GuildMemberLeaveEvent.class),
	GUILD_MEMBER_NICK_CHANGE(GuildMemberNickChangeEvent.class),
	GUILD_BAN(GuildBanEvent.class),
	GUILD_UNBAN(GuildUnbanEvent.class);
	
	public final Class<? extends Event> clazz;
	private EventType(Class<? extends Event> clazz) {
		this.clazz = clazz;
	}
	
}
