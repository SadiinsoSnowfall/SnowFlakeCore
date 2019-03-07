package net.shadowpie.sadiinso.sfc.commands.context;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;

import gnu.trove.list.array.TIntArrayList;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.JDALogger;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler;
import net.shadowpie.sadiinso.sfc.sfc.SFC;
import net.shadowpie.sadiinso.sfc.utils.SStringBuilder;

public abstract class CommandContext {

	protected static final Logger logger = JDALogger.getLog("Command Context");
	
	// command arguments
	protected String[] args;
	protected int[] argsPos;
	protected int currentArg = 0;
	
	// command raw message
	protected String resolvedMessage;
	
	// command raw message after prefix pulling
	protected String resolvedMessagePP;
	
	// true if the user used the mention to trigger the bot
	protected final boolean useMention;

	protected CommandContext(String content, boolean useMention) {
		this.useMention = useMention;
		char[] command = content.toCharArray();
		
		extractArguments(getMessageResolved(command)); // parse command
	}
	
	protected CommandContext(String content) {
		this.useMention = false;
		char[] command = content.toCharArray();
		
		extractArguments(command); // parse command
	}

	/**
	 * Return the current first command argument (AKA prefix)
	 */
	public String prefix() {
		return (args.length > 0 ? args[0] : "");
	}
	
	/**
	 * Rebuild the context without the first command prefix
	 */
	public CommandContext pullPrefix() {
		args = (args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0]);
		
		++currentArg;
		resolvedMessagePP = (argsPos.length > currentArg ? resolvedMessage.substring(argsPos[currentArg]) : "");
		return this;
	}
	
	/**
	 * Return the arguments passed to the command
	 */
	public String[] args() {
		return args;
	}
	
	/**
	 * Return the argument at the given index
	 * Warning, may throw ArrayIndexOutOfBoundException
	 * @param index The argument index
	 */
	public String arg(int index) {
		if(index >= args.length)
			return null;
		
		return args[index];
	}
	
	/**
	 * Return the number of arguments passed to the command
	 * @return
	 */
	public int argc() {
		return args.length;
	}
	
	/**
	 * Return the given argument parsed as an 32 bits signed integer
	 * @param index The argument index
	 */
	public int getAsInt(int index) throws NumberFormatException {
		return Integer.parseInt(args[index]);
	}
	
	/**
	 * Return the given argument parsed as an 32 bits signed integer without throwing errors
	 * In case of errors, return the fallback value
	 * @param index The argument index
	 */
	public int getAsInt(int index, int fallback) {
		try {
			return Integer.parseInt(args[index]);
		} catch(Exception ignored) {}
		
		return fallback;
	}
	
	/**
	 * Return the given argument parsed as an 64 bits signed integer
	 * @param index The argument index
	 */
	public long getAsLong(int index) throws NumberFormatException {
		return Long.parseLong(args[index]);
	}
	
	/**
	 * Return the given argument parsed as an 64 bits signed integer without throwing errors
	 * In case of errors, return the fallback value
	 * @param index The argument index
	 */
	public long getAsLong(int index, long fallback) {
		try {
			return Long.parseLong(args[index]);
		} catch(Exception ignored) {}
		
		return fallback;
	}
	
	/**
	 * Return the given argument parsed as a boolean
	 * @param index The argument index
	 */
	public boolean getAsBoolean(int index) {
		return Boolean.parseBoolean(args[index]);
	}
	
	/**
	 * Return the given argument parsed as a user
	 * <br/>
	 * Attention, this function is not searching users by name, only IDs
	 * @param index The argument index
	 * @return The user or null if an error occured
	 */
	public User getAsUser(int index) {
		try {
			return SFC.getJDA().retrieveUserById(args[index]).complete();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Return the given argument parsed as a member
	 * <br/>
	 * Attention, this function will also search members by name, if two members have the same name, the first one will be returned
	 * <br/>
	 * Attention, use this function only if you are sure the message was sent in a guild
	 * @param index The argument index
	 * @return The member or null if an error occured or if the command was not sent in a server
	 */
	public abstract Member getAsMember(int index);
	
	/**
	 * Return the given argument parsed as a role
	 * <br/>
	 * Attention, this function will also search roles by name, if two roles have the same name, the first one will be returned
	 * <br/>
	 * Attention, use this function only if you are sure the message was sent in a guild
	 * @param index The argument index
	 * @return The role or null if an error occured or if the command was not sent in a server
	 */
	public abstract Role getAsRole(int index);
	
	/**
	 * Return the given argument parsed as a textchannel
	 * <br/>
	 * Attention, this function will also search channels by name, if two roles channels the same name, the first one will be returned
	 * <br/>
	 * Attention, use this function only if you are sure the message was sent in a guild
	 * @param index The argument index
	 * @return The channel or null if an error occured or if the command was not sent in a server
	 */
	public abstract TextChannel getAsTextChannel(int index);
	
	/**
	 * Return the given argument parsed as a voicechannel
	 * <br/>
	 * Attention, this function will also search channels by name, if two roles channels the same name, the first one will be returned
	 * <br/>
	 * Attention, use this function only if you are sure the message was sent in a guild
	 * @param index The argument index
	 * @return The channel or null if an error occured or if the command was not sent in a server
	 */
	public abstract VoiceChannel getAsVoiceChannel(int index);
	
	/**
	 * Pack all the arguments in one string
	 */
	public String packArgs() {
		StringBuilder builder = new StringBuilder(resolvedMessagePP.length());
		for(int t = 0; t < args.length; t++)
			builder.append(args[t]);
		
		return builder.toString();
	}
	
	/**
	 * Pack all the arguments in one string, add the specified string between each argument
	 * @param between The string to add between two arguments
	 */
	public String packArgs(String between) {
		StringBuilder builder = new StringBuilder(resolvedMessagePP.length() + (between.length() * args.length));
		for(int t = 0; t < args.length - 1; t++) {
			builder.append(args[t]);
			builder.append(between);
		}
		
		builder.append(args[args.length - 1]); // add the final argument
		return builder.toString();
	}
	
	/**
	 * Pack all the arguments in one string
	 * @param start The first argument to pack
	 */
	public String packArgs(int start) {
		StringBuilder builder = new StringBuilder(resolvedMessagePP.length());
		start = Math.max(0, start);
		
		for(int t = start; t < args.length; t++)
			builder.append(args[t]);
		
		return builder.toString();
	}
	
	/**
	 * Pack all the arguments in one string, add the specified string between each argument
	 * @param start The first argument to pack
	 * @param between The string to add between two arguments
	 */
	public String packArgs(int start, String between) {
		StringBuilder builder = new StringBuilder(resolvedMessagePP.length() + (between.length() * (args.length - start)));
		start = Math.max(0, start);
		
		for(int t = start; t < args.length - 1; t++) {
			builder.append(args[t]);
			builder.append(between);
		}
		
		builder.append(args[args.length - 1]); // add the final argument
		return builder.toString();
	}
	
	/**
	 * Pack all the arguments in one string
	 * @param start The first argument to pack
	 * @param end The last argument to pack
	 */
	public String packArgs(int start, int end) {
		StringBuilder builder = new StringBuilder(resolvedMessagePP.length());
		end = Math.min(args.length - 2, end);
		start = Math.max(0, start);
		
		for(int t = start; t <= end; t++)
			builder.append(args[t]);
		
		return builder.toString();
	}
	
	/**
	 * Pack all the arguments in one string, add the specified string between each argument
	 * @param start The first argument to pack
	 * @param end The last argument to pack
	 * @param between The string to add between two arguments
	 */
	public String packArgs(int start, int end, String between) {
		end = Math.min(args.length - 2, end);
		StringBuilder builder = new StringBuilder(resolvedMessagePP.length() + (between.length() * (args.length - start)));
		
		for(int t = start; t <= end; t++) {
			builder.append(args[t]);
			builder.append(between);
		}
		
		builder.append(args[end]); // add the final argument
		return builder.toString();
	}

	/**
	 * Return the raw context message (with IDs resolved)
	 */
	public String rawMessage() {
		return resolvedMessagePP;
	}

	/**
	 * Return the guild from where the message was sent or null if it did not came from a guild
	 */
	public abstract Guild getGuild();
	
	/**
	 * Return the origin of the command
	 */
	public abstract ContextOrigin getOrigin();
	
	/**
	 * Return the JDA Entity.Message Object of this context
	 */
	public abstract Message getMessage();
	
	/**
	 * Return whether the user use the mention or the tag to trigger the bot
	 */
	public boolean usedMention() {
		return useMention;
	}
	
	/**
	 * Return the message channel that the command was sent in.
	 */
	public abstract MessageChannel getChannel();
	
	/**
	 * Return the id of the user as a string
	 */
	public abstract String getAuthorId();
	
	/**
	 * Return the id of the user as a long
	 */
	public abstract long getAuthorIdLong();
	
	/**
	 * Return the message author
	 */
	public abstract User getAuthor();
	
	/**
	 * Return the message author as mention
	 */
	public abstract String getUserAsMention();
	
	//#######################
	// quick reply & reaction
	//#######################
	
	/**
	 * Send a message in the channel that the command was sent in.
	 * @param str The message to send
	 */
	public abstract void reply(String str);
	
	/**
	 * Send a message in the context channel
	 * @param embed The message to send
	 */
	public abstract void reply(MessageEmbed embed);
	
	/**
	 * Send a message in the context channel as an embed with the specified theme color
	 * @param message The message to send
	 */
	public void replyAsEmbed(String message) {
		replyAsEmbed(message, ConfigHandler.color_theme());
	}
	
	/**
	 * Send a message in the context channel as an embed with the specified info color
	 * @param message The message to send
	 */
	public void info(String message) {
		replyAsEmbed(message, ConfigHandler.color_info());
	}
	
	/**
	 * Send a message in the context channel as an embed with the specified warn color
	 * @param message The message to send
	 */
	public void warn(String message) {
		replyAsEmbed(message, ConfigHandler.color_warn());
	}
	
	/**
	 * Send a message in the context channel as an embed with the specified error color
	 * @param message The message to send
	 */
	public void error(String message) {
		replyAsEmbed(message, ConfigHandler.color_error());
	}
	
	/**
	 * Send a message in the context channel as an embed with the specified color
	 * @param message The message to send
	 */
	public abstract void replyAsEmbed(String message, Color color);
	
	/**
	 * Send a message in the channel that the command was sent in.
	 * @param builder The message to send
	 */
	public void reply(EmbedBuilder builder) {
		reply(builder.build());
	}
	
	/**
	 * Add a reaction to the message that contain the command.
	 * @param unicode The emote in unicode format
	 */
	public abstract void react(String unicode);
	
	/**
	 * Send a file to the channel
	 * @param file The file to send
	 */
	public abstract void sendFile(File file);
	
	/**
	 * Send a file to the channel
	 * @param file The file to send
	 * @param name The file name
	 */
	public abstract void sendFile(File file, String name);
	
	/**
	 * Send a file to the channel
	 * @param file The file to send
	 * @param name The file name
	 */
	public abstract void sendFile(byte[] file, String name);
	
	/**
	 * Send an image to the channel
	 * @param img The image to send
	 */
	public abstract void sendImage(RenderedImage img);
	
	/**
	 * Send an image with a message to the channel
	 * @param img The image to send
	 * @param name The image name
	 */
	public abstract void sendImage(RenderedImage img, String name);
	
	//################
	// Utils & private
	//################
	
	/**
	 * Return the representation of the arguments passed to the command as a String
	 */
	@Override
	public String toString() {
		return Arrays.toString(args);
	}

	/**
	 * return message without caller (prefix or pre-mentions) and all mentions
	 * resolved
	 * 
	 * useMention : true if the command use the "mention system" to call the bot,
	 * false if it use the tag system
	 */
	protected char[] getMessageResolved(char[] msg) {
		int index = 0;

		// remove caller
		index += (useMention ? SFC.selfMention().length() : ConfigHandler.bot_tag().length());
		
		// remove post-caller whitespace
		while (Character.isWhitespace(msg[index]))
			++index;
		
		// resolve mentions (skip resolving if message size is less than 21 chars : no possible IDs left to resolve)
		if (msg.length - index > 20) {
			SStringBuilder builder = new SStringBuilder(msg.length - index); // ensure that the internal buffer will nether growth
			int lastPush = msg.length - 21;
			
			resolve: for (int t = index, len = msg.length - 20; t < len; t++) { // prevent "msg.length - 20" to be re-calculated each loop turn
				if ((msg[t] == '<') && !Character.isDigit(msg[t + 1])) {
					if (!Character.isDigit(msg[t + 2])) {
						if (msg[t + 21] != '>') {
							builder.push(msg[t]);
							continue resolve;
						}
						
						for (int u = t + 3, len2 = t + 21; u < len2; u++) {
							if (!Character.isDigit(msg[u])) {
								builder.push(msg[t]);
								continue resolve;
							}
						}

						builder.push(msg, t + 3, 18);// push digits
						t += 21;
					} else {
						if (msg[t + 20] != '>') {
							builder.push(msg[t]);
							continue resolve;
						}

						for (int u = t + 3, len2 = t + 20; u < len2; u++) {
							if (!Character.isDigit(msg[u])) {
								builder.push(msg[t]);
								continue resolve;
							}
						}

						builder.push(msg, t + 2, 18);// push digits
						t += 20;
					}
				} else {
					builder.push(msg[t]);
				}
				
				lastPush = t;
			}

			builder.push(msg, lastPush + 1);
			resolvedMessage = builder.toString();
			return builder.copy();
		} else {
			char[] tmp = Arrays.copyOfRange(msg, index, msg.length);
			resolvedMessage = new String(tmp);
			return tmp;
		}
	}

	protected void extractArguments(char[] cmd) {
		ArrayList<char[]> tmpArgs = new ArrayList<>();
		TIntArrayList tmpArgsPos = new TIntArrayList(); // position of the arguments in the resolved message
		SStringBuilder builder = new SStringBuilder();
		boolean inStr = false; // is current argument escaped
		boolean wParseState = false; // is current argument parsing started
		int t;
		
		/* separate command arguments */
		for (t = 0; t < cmd.length; t++) {
			if (cmd[t] == '\"') {
				if (!inStr && !wParseState)
					inStr = true;
				else if (inStr)
					inStr = false;
				else
					builder.push(cmd[t]);
			} else if (Character.isWhitespace(cmd[t]) && !inStr) {
				if (builder.empty()) // auto trim
					continue;

				tmpArgsPos.add(t - builder.length());
				tmpArgs.add(builder.copy());
				builder.reset();
				wParseState = false;
			} else {
				wParseState = true;
				builder.push(cmd[t]);
			}
		}

		tmpArgsPos.add(t - builder.length());

		/* reconstruct arguments */
		args = new String[tmpArgs.size() + 1];
		t = 0;
		for (char[] arg : tmpArgs)
			args[t++] = new String(arg);

		/* add last argument */
		if (inStr)
			builder.insert(0, "\""); // add " at the beginning if still in escaped string
		args[t] = builder.toString();

		argsPos = new int[tmpArgsPos.size()];
		for (t = 1; t < tmpArgsPos.size(); t++)
			argsPos[t] = tmpArgsPos.getQuick(t);
	}
}
