package net.shadowpie.sadiinso.sfc.utils;

import java.awt.Color;
import java.io.File;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.requests.restaction.MessageAction;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler;
import net.shadowpie.sadiinso.sfc.sfc.SFC;

/**
 * Utility class for all function related to JDA or Discord in general
 */
public class JdaUtils {
	
	public static final String EMOJI_ACCEPT = "✅";
	public static final String EMOJI_DENY = "❌";
	
	public static EmbedBuilder getEmbedBuilder() {
		return getEmbedBuilder(null, ConfigHandler.color_theme());
	}
	
	public static EmbedBuilder getEmbedBuilder(Color color) {
		return getEmbedBuilder(null, color);
	}
	
	public static EmbedBuilder getEmbedBuilder(String title) {
		return getEmbedBuilder(title, ConfigHandler.color_theme());
	}
	
	public static EmbedBuilder getEmbedBuilder(String title, Color color) {
		EmbedBuilder builder = new EmbedBuilder();

		if ((title != null) && !title.isEmpty())
			builder.setTitle(title);

		builder.setColor(color);
		return builder;
	}

	public static MessageEmbed sendAsEmbed(MessageChannel channel, String message) {
		return sendAsEmbed(channel, message, null, ConfigHandler.color_theme());
	}

	public static MessageEmbed sendAsEmbed(MessageChannel channel, String title, String sub) {
		return sendAsEmbed(channel, title, sub, ConfigHandler.color_theme());
	}

	public static MessageEmbed sendAsEmbed(MessageChannel channel, String message, Color color) {
		return sendAsEmbed(channel, message, null, color);
	}

	public static MessageEmbed sendAsEmbed(MessageChannel channel, String title, String sub, Color color) {
		return sendAsEmbed(channel, title, sub, null, color);
	}

	public static MessageEmbed sendAsEmbed(MessageChannel channel, String title, String sub, String footer, Color color) {
		EmbedBuilder builder = new EmbedBuilder();
		builder.setColor(color);

		if ((sub == null) || sub.isEmpty())
			builder.setDescription(title);
		else
			builder.addField(title, sub, false);

		builder.setFooter(footer, null);
		MessageEmbed msg = builder.build();
		
		if(channel != null)
			channel.sendMessage(msg).queue();
		
		return msg;
	}

	public static PrivateChannel getPrivateChannel(User user) {
		return user.openPrivateChannel().complete();
	}
	
	public static PrivateChannel getPrivateChannel(Member member) {
		return member.getUser().openPrivateChannel().complete();
	}
	
	public static PrivateChannel getPrivateChannel(long userid) {
		User user = getUser(userid);
		return (user == null ? null : user.openPrivateChannel().complete());
	}
	
	/**
	 * Send a private message to the given user
	 * @param user The user
	 * @param message The message to send
	 */
	public static void sendPrivate(User user, String message) {
		sendPrivate(user, message, false);
	}
	
	/**
	 * Send a private message to the given user
	 * @param user The user
	 * @param message The message to send
	 * @param instant Whether or not to perform the action immediately (if true, the ID of the message will be returned, else 0L)
	 */
	public static long sendPrivate(User user, String message, boolean instant) {
		MessageAction action = user.openPrivateChannel().complete().sendMessage(message);
		
		try {
			if(instant)
				return action.complete().getIdLong();
			else
				action.queue();
		} catch(Exception ignored) {}
		
		return 0L;
	}

	public static void sendPrivate(User user, MessageEmbed message) {
		sendPrivate(user, message, false);
	}
	
	/**
	 * Send a private message to the given user
	 * @param user The user
	 * @param message The message to send
	 * @param instant Whether or not to perform the action immediately
	 */
	public static long sendPrivate(User user, MessageEmbed message, boolean instant) {
		MessageAction action = user.openPrivateChannel().complete().sendMessage(message);
		
		try {
			if(instant)
				return action.complete().getIdLong();
			else
				action.queue();
		} catch(Exception ignored) {}
		
		return 0L;
	}
	
	/**
	 * Send a private message to the given user
	 * @param user The user
	 * @param message The message to send
	 * @param file The file to send
	 */
	public static void sendPrivate(User user, String message, File file) {
		sendPrivate(user, message, file, false);
	}
	
	/**
	 * Send a private message to the given user
	 * @param user The user
	 * @param message The message to send
	 * @param file The file to send
	 * @param instant Whether or not to perform the action immediately
	 */
	public static long sendPrivate(User user, String message, File file, boolean instant) {
		MessageAction action = user.openPrivateChannel().complete().sendFile(file, new MessageBuilder().append(message).build());
		
		try {
			if(instant)
				return action.complete().getIdLong();
			else
				action.queue();
		} catch(Exception ignored) {}
		
		return 0L;
	}
	
	/**
	 * Return the user associated with the given id
	 * @param id The user id
	 */
	public static User getUser(long id) {
		return SFC.getJDA().retrieveUserById(id).complete();
	}
	
	/**
	 * Return the user associated with the given id
	 * @param id The user id
	 */
	public static User getUser(String id) {
		return SFC.getJDA().retrieveUserById(id).complete();
	}
	
	/**
	 * Check if the given user is the bot owner
	 * @param user The user to test
	 * @return true if the user is the bot owner else false
	 */
	public static boolean isBotOwner(User user) {
		return (user.getId().equals(ConfigHandler.owner_sid()));
	}
	
	/**
	 * Check if the given member is the bot owner
	 * @param member The member to test
	 * @return true if the member is the bot owner else false
	 */
	public static boolean isBotOwner(Member member) {
		return (member.getUser().getId().equals(ConfigHandler.owner_sid()));
	}
	
	/**
	 * Return the member associated with the given id the the given guild
	 * @param serverid The guild id
	 * @param userid The user id
	 * @return The member or null if not found
	 */
	public static Member getMember(long serverid, long userid) {
		Guild guild = SFC.getJDA().getGuildById(serverid);
		if(guild == null)
			return null;
		
		return guild.getMemberById(userid);
	}
	
	/**
	 * Return the member associated with the given id the the given guild
	 * @param serverid The guild id
	 * @param userid The user id
	 * @return The member or null if not found
	 */
	public static Member getMember(String serverid, String userid) {
		Guild guild = SFC.getJDA().getGuildById(serverid);
		if(guild == null)
			return null;
		
		return guild.getMemberById(userid);
	}
	
	/**
	 * Return the role associated with the given id the the given guild
	 * @param serverid The guild id
	 * @param roleid The role id
	 * @return The role or null if not found
	 */
	public static Role getRole(long serverid, long roleid) {
		Guild guild = SFC.getJDA().getGuildById(serverid);
		if(guild == null)
			return null;
		
		return guild.getRoleById(roleid);
	}
	
	/**
	 * Return the role associated with the given id the the given guild
	 * @param serverid The guild id
	 * @param roleid The role id
	 * @return The role or null if not found
	 */
	public static Role getRole(String serverid, String roleid) {
		Guild guild = SFC.getJDA().getGuildById(serverid);
		if(guild == null)
			return null;
		
		return guild.getRoleById(roleid);
	}
	
	/**
	 * Return the invite code (name + '#' + discriminator) of the given member
	 * @param member The member
	 */
	public static String getInviteCode(Member member) {
		return getInviteCode(member.getUser());
	}
	
	/**
	 * Return the invite code (name + '#' + discriminator) of the given user
	 * @param user The user
	 */
	public static String getInviteCode(User user) {
		return user.getName() + '#' + user.getDiscriminator();
	}
	
	/**
	 * Get all the roles ids of the given member
	 * @param member The member
	 * @return A String array containing the roles ids
	 */
	public static String[] getAllRoleId(Member member) {
		return (member != null ? member.getRoles().stream().map(r -> r.getId()).toArray(String[]::new) : null);
	}
	
	/**
	 * Get all the roles ids of the given member
	 * @param member The member
	 * @return A long array containing the roles ids
	 */
	public static long[] getAllRoleIdLong(Member member) {
		return (member != null ? member.getRoles().stream().mapToLong(r -> r.getIdLong()).toArray() : null);
	}
	
}
