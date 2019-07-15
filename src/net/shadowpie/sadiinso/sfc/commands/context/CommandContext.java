package net.shadowpie.sadiinso.sfc.commands.context;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.utils.JDALogger;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler;
import net.shadowpie.sadiinso.sfc.utils.JdaUtils;
import net.shadowpie.sadiinso.sfc.utils.SFUtils;
import net.shadowpie.sadiinso.sfc.utils.SStringBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class CommandContext {
	
	/**
	 * Index for retrieving values from the command pipeline read buffer
	 */
	public static final int PIPELINE = -1;
	
	/**
	 * Indicate that the command pipeline execution should stop after the current command
	 */
	public static final byte FLAG_BREAK_PIPELINE = 1;
	
	protected static final Logger logger = JDALogger.getLog("Command Context");
	
	/**
	 * Current command arguments
	 */
	protected String[] args;
	
	/**
	 * Command pipeline
	 */
	private String[][] pipeline;
	
	/**
	 * Current command pipeline index
	 */
	private int currentPipelineIndex;
	
	/**
	 * Command pipeline read buffer (input)
	 */
	private SStringBuilder pipelineInBuffer;
	
	/**
	 * Command pipeline write buffer (output)
	 */
	private SStringBuilder pipelineOutBuffer;
	
	/**
	 * Indicate whether or not the user tagged the bot to execute this command
	 */
	private final boolean useMention;
	
	/**
	 * The current flags set during this command execution
	 */
	private byte flags = 0;
	
	//############
	//CONSTRUCTORS
	//############
	
	protected CommandContext(String content, boolean useMention) {
		this.useMention = useMention;
		
		List<String[]> pipe = new ArrayList<>();
		args = CommandContextUtils.extractArguments(CommandContextUtils.resolveMentions(content, useMention), pipe);
		initPipeline(pipe);
	}
	
	protected CommandContext(String content) {
		this.useMention = false;
		
		List<String[]> pipe = new ArrayList<>();
		args = CommandContextUtils.extractArguments(content.toCharArray(), pipe);
		initPipeline(pipe);
	}
	
	/**
	 * initialize the command pipeline if needed
	 * @param pipe The command pipeline contents
	 */
	private void initPipeline(List<String[]> pipe) {
		if(!pipe.isEmpty()) {
			pipeline = pipe.toArray(String[][]::new);
			currentPipelineIndex = pipeline.length;
			pipelineOutBuffer = new SStringBuilder();
			pipelineInBuffer = new SStringBuilder();
		}
	}
	
	//################
	//FLAGS OPERATIONS
	//################
	
	public boolean hasFlag(int flag) {
		return ((flags & flag) > 0);
	}
	
	//###################
	//PIPELINE OPERATIONS
	//###################
	
	/**
	 * Stop the command pipeline from executing further
	 */
	public void breakPipeline() {
		flags |= FLAG_BREAK_PIPELINE;
	}
	
	/**
	 * Return whether of not the command pipe to another command
	 */
	@SuppressWarnings("unused")
	public boolean hasPipeline() {
		return (pipeline != null);
	}
	
	/**
	 * shift the current args to the next piped command
	 * @return Whether of not the command pipe to another command
	 */
	@SuppressWarnings("unused")
	public boolean advancePipeline() {
		if(!hasPipeline()) {
			return false;
		}
		
		args = pipeline[--currentPipelineIndex];
		if(currentPipelineIndex <= 0) {
			pipeline = null;
		}
		
		// swap and reset buffers
		swapPipelineBuffers();
		pipelineOutBuffer.setLength(0);
		
		// reset flags
		flags = 0;
		
		return true;
	}
	
	/**
	 * Return the command pipeline read (in) buffer
	 * @return A {@link StringBuilder} representing the pipeline read buffer
	 */
	@SuppressWarnings("unused")
	public SStringBuilder getPipeIn() {
		return pipelineInBuffer;
	}
	
	/**
	 * Return whether or not the command pipeline read buffer contains something
	 */
	public boolean hasPipeContents() {
		return ((pipelineInBuffer == null) ? false : pipelineInBuffer.length() > 0);
	}
	
	/**
	 * Return the content of the command pipeline read buffer
	 * @return A String representing the contents of the command pipeline read buffer
	 */
	@SuppressWarnings("unused")
	public String getPipeContents() {
		return ((pipelineInBuffer == null) ? null : pipelineInBuffer.toString());
	}
	
	/**
	 * Return the command pipeline write (out) buffer
	 * @return A {@link StringBuilder} representing the pipeline write buffer
	 */
	@SuppressWarnings("unused")
	public SStringBuilder getPipeOut() {
		return pipelineOutBuffer;
	}
	
	/**
	 * Append {@code chars} to the command pipeline write buffer
	 * @param chars The {@link CharSequence} to append
	 */
	@SuppressWarnings("unused")
	public void writeToPipe(CharSequence chars) {
		if(hasPipeline()) {
			pipelineOutBuffer.append(chars);
		}
	}
	
	/**
	 * Append {@code obj} to the command pipeline write buffer
	 * @param obj The {@link Object} to append
	 */
	@SuppressWarnings("unused")
	public void writeToPipe(Object obj) {
		if(hasPipeline()) {
			pipelineOutBuffer.append(obj);
		}
	}
	
	/**
	 * Reset the commmand pipeline write buffer
	 */
	@SuppressWarnings("unused")
	public void pipeReset() {
		if(hasPipeline()) {
			pipelineOutBuffer.setLength(0);
		}
	}
	
	/**
	 * Swap the command pipeline in and out buffers
	 */
	private void swapPipelineBuffers() {
		SStringBuilder tmp = pipelineInBuffer;
		pipelineInBuffer = pipelineOutBuffer;
		pipelineOutBuffer = tmp;
	}
	
	//####################
	//ARGUMENTS OPERATIONS
	//####################
	
	/**
	 * Return the current first command argument (AKA prefix)
	 */
	public String prefix() {
		return (args.length > 0 ? args[0] : StringUtils.EMPTY);
	}
	
	/**
	 * Rebuild the context without the first command prefix
	 */
	public CommandContext pullPrefix() {
		args = (args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[0]);
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
	 * @param index The argument index or null if the index is out of bonds
	 */
	public String arg(int index) {
		if(index >= args.length)
			return null;
		
		return args[index];
	}
	
	/**
	 * Return the number of arguments passed to the command
	 * @return The command arguments count
	 */
	public int argc() {
		return args.length;
	}
	
	/**
	 * Return the given argument parsed as an 32 bits signed integer
	 * @param index The argument index
	 */
	@SuppressWarnings("unused")
	public int getAsInt(int index) throws NumberFormatException {
		if(index == PIPELINE) {
			return Integer.parseInt(pipelineInBuffer, 0, pipelineInBuffer.length(), 10);
		} else {
			return Integer.parseInt(args[index]);
		}
	}
	
	/**
	 * Return the given argument parsed as an 32 bits signed integer without throwing errors
	 * In case of errors, return the fallback value
	 * @param index The argument index
	 */
	@SuppressWarnings("unused")
	public int getAsInt(int index, int fallback) {
		try {
			return getAsInt(index);
		} catch(Exception ignored) {}
		
		return fallback;
	}
	
	/**
	 * Return the given argument parsed as a 64 bits signed integer
	 * @param index The argument index
	 */
	@SuppressWarnings("unused")
	public long getAsLong(int index) throws NumberFormatException {
		if(index == PIPELINE) {
			return Long.parseLong(pipelineInBuffer, 0, pipelineInBuffer.length(), 10);
		} else {
			return Long.parseLong(args[index]);
		}
	}
	
	/**
	 * Return the given argument parsed as a 64 bits signed integer without throwing errors
	 * In case of errors, return the fallback value
	 * @param index The argument index
	 */
	@SuppressWarnings("unused")
	public long getAsLong(int index, long fallback) {
		try {
			return getAsLong(index);
		} catch(Exception ignored) {}
		
		return fallback;
	}
	
	/**
	 * Return the given argument parsed as a double
	 * @param index The argument index
	 */
	@SuppressWarnings("unused")
	public double getAsDouble(int index) throws NumberFormatException {
		if(index == PIPELINE) {
			return Double.parseDouble(pipelineInBuffer.toString());
		} else {
			return Double.parseDouble(args[index]);
		}
	}
	
	/**
	 * Return the given argument parsed as a double without throwing errors
	 * In case of errors, return the fallback value
	 * @param index The argument index
	 */
	@SuppressWarnings("unused")
	public double getAsDouble(int index, double fallback) {
		try {
			return getAsDouble(index);
		} catch(Exception ignored) {}
		
		return fallback;
	}
	
	/**
	 * Return the given argument parsed as a float
	 * @param index The argument index
	 */
	@SuppressWarnings("unused")
	public float getAsFloat(int index) throws NumberFormatException {
		if(index == PIPELINE) {
			return Float.parseFloat(pipelineInBuffer.toString());
		} else {
			return Float.parseFloat(args[index]);
		}
	}
	
	/**
	 * Return the given argument parsed as a float without throwing errors
	 * In case of errors, return the fallback value
	 * @param index The argument index
	 */
	@SuppressWarnings("unused")
	public float getAsFloat(int index, float fallback) {
		try {
			return getAsFloat(index);
		} catch(Exception ignored) {}
		
		return fallback;
	}
	
	/**
	 * Return the given argument parsed as a boolean
	 * @param index The argument index
	 */
	@SuppressWarnings("unused")
	public boolean getAsBoolean(int index) {
		if(index == PIPELINE) {
			return Boolean.parseBoolean(pipelineInBuffer.toString());
		} else {
			return Boolean.parseBoolean(args[index]);
		}
	}
	
	/**
	 * Return the given argument parsed as a user
	 * <br/>
	 * Attention, this function is not searching users by name, only IDs
	 * @param index The argument index
	 * @return The user or null if an error occured
	 */
	@SuppressWarnings("unused")
	public abstract User getAsUser(int index);
	
	/**
	 * Return the given argument parsed as a member
	 * <br/>
	 * Attention, this function will also search members by name, if two members have the same name, the first one will be returned
	 * <br/>
	 * Attention, use this function only if you are sure the message was sent in a guild
	 * @param index The argument index
	 * @return The member or null if an error occured or if the command was not sent in a server
	 */
	@SuppressWarnings("unused")
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
	@SuppressWarnings("unused")
	public abstract Role getAsRole(int index);
	
	/**
	 * Return the given argument parsed as a textchannel
	 * <br/>
	 * Attention, this function will also search channels by name, if two channels have the same name, the first one will be returned
	 * <br/>
	 * Attention, use this function only if you are sure the message was sent in a guild
	 * @param index The argument index
	 * @return The channel or null if an error occured or if the command was not sent in a server
	 */
	@SuppressWarnings("unused")
	public abstract TextChannel getAsTextChannel(int index);
	
	/**
	 * Return the given argument parsed as a voicechannel
	 * <br/>
	 * Attention, this function will also search channels by name, if two channels have the same name, the first one will be returned
	 * <br/>
	 * Attention, use this function only if you are sure the message was sent in a guild
	 * @param index The argument index
	 * @return The channel or null if an error occured or if the command was not sent in a server
	 */
	@SuppressWarnings("unused")
	public abstract VoiceChannel getAsVoiceChannel(int index);
	
	/**
	 * Return the given argument parsed as a Category
	 * <br/>
	 * Attention, this function will also search category by name, if two categories have the same name, the first one will be returned
	 * <br/>
	 * Attention, use this function only if you are sure the message was sent in a guild
	 * @param index The argument index
	 * @return The Category or null if an error occured or if the command was not sent in a server
	 */
	@SuppressWarnings("unused")
	public abstract Category getAsCategory(int index);
	
	/**
	 * Pack all the arguments in one string
	 */
	@SuppressWarnings("unused")
	public String packArgs() {
		StringBuilder builder = new StringBuilder(32);
		for(int t = 0; t < args.length; t++) {
			builder.append(args[t]);
		}
		
		return builder.toString();
	}
	
	/**
	 * Pack all the arguments in one string, add the specified string between each argument
	 * @param between The string to add between two arguments
	 */
	@SuppressWarnings("unused")
	public String packArgs(String between) {
		StringBuilder builder = new StringBuilder(32);
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
	@SuppressWarnings("unused")
	public String packArgs(int start) {
		StringBuilder builder = new StringBuilder(32);
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
	@SuppressWarnings("unused")
	public String packArgs(int start, String between) {
		StringBuilder builder = new StringBuilder(32);
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
	@SuppressWarnings("unused")
	public String packArgs(int start, int end) {
		StringBuilder builder = new StringBuilder(32);
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
	@SuppressWarnings("unused")
	public String packArgs(int start, int end, String between) {
		end = Math.min(args.length - 2, end);
		StringBuilder builder = new StringBuilder(32);
		
		for(int t = start; t <= end; t++) {
			builder.append(args[t]);
			builder.append(between);
		}
		
		builder.append(args[end]); // add the final argument
		return builder.toString();
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
	@SuppressWarnings("unused")
	public boolean usedMention() {
		return useMention;
	}
	
	/**
	 * Return the message channel that the command was sent in.
	 */
	@SuppressWarnings("unused")
	public abstract MessageChannel getChannel();
	
	/**
	 * Return the id of the user as a string
	 */
	@SuppressWarnings("unused")
	public abstract String getAuthorId();
	
	/**
	 * Return the id of the user as a long
	 */
	@SuppressWarnings("unused")
	public abstract long getAuthorIdLong();
	
	/**
	 * Return the message author
	 */
	@SuppressWarnings("unused")
	public abstract User getAuthor();
	
	/**
	 * Return the message author as mention
	 */
	@SuppressWarnings("unused")
	public abstract String getAuthorAsMention();
	
	//#######################
	// quick reply & reaction
	//#######################
	
	/**
	 * Send a message in the channel that the command was sent in.
	 * @param str The message to send
	 */
	@SuppressWarnings("unused")
	public abstract void reply(String str);
	
	/**
	 * Send a message in the context channel
	 * @param embed The message to send
	 */
	@SuppressWarnings("unused")
	public abstract void reply(MessageEmbed embed);
	
	/**
	 * Send a message in the context channel as an embed with the specified info color
	 * @param message The message to send
	 */
	@SuppressWarnings("unused")
	public void info(String message) {
		replyAsEmbed(message, ConfigHandler.color_info());
	}
	
	/**
	 * Send a message in the context channel as an embed with the specified warn color
	 * @param message The message to send
	 */
	@SuppressWarnings("unused")
	public void warn(String message) {
		replyAsEmbed(message, ConfigHandler.color_warn());
	}
	
	/**
	 * Send a message in the context channel as an embed with the specified error color
	 * @param message The message to send
	 */
	@SuppressWarnings("unused")
	public void error(String message) {
		replyAsEmbed(message, ConfigHandler.color_error());
	}
	
	/**
	 * Send a message in the context channel as an embed with the specified theme color
	 * @param message The message to send
	 */
	@SuppressWarnings("unused")
	public void replyAsEmbed(String message) {
		replyAsEmbed(message, ConfigHandler.color_theme());
	}
	
	/**
	 * Send a message in the context channel as an embed with the specified color
	 * @param message The message to send
	 */
	@SuppressWarnings("unused")
	public abstract void replyAsEmbed(String message, Color color);
	
	/**
	 * Send a message in the channel that the command was sent in.
	 * @param builder The message to send
	 */
	@SuppressWarnings("unused")
	public void reply(EmbedBuilder builder) {
		reply(builder.build());
	}
	
	/**
	 * Add a reaction to the message that contain the command.
	 * @param unicode The emote in unicode format
	 */
	@SuppressWarnings("unused")
	public abstract void react(String unicode);
	
	/**
	 * Send a file to the channel
	 * @param file The file to send
	 */
	@SuppressWarnings("unused")
	public abstract void sendFile(File file);
	
	/**
	 * Send a file to the channel
	 * @param file The file to send
	 * @param name The file name
	 */
	@SuppressWarnings("unused")
	public abstract void sendFile(File file, String name);
	
	/**
	 * Send a file to the channel
	 * @param file The file to send
	 * @param name The file name
	 */
	@SuppressWarnings("unused")
	public abstract void sendFile(byte[] file, String name);
	
	/**
	 * Send an image to the channel
	 * @param img The image to send
	 */
	@SuppressWarnings("unused")
	public abstract void sendImage(RenderedImage img);
	
	/**
	 * Send an image with a message to the channel
	 * @param img The image to send
	 * @param name The image name
	 */
	@SuppressWarnings("unused")
	public abstract void sendImage(RenderedImage img, String name);
	
	/**
	 * Add {@link JdaUtils#EMOJI_ACCEPT} as a reaction to the command message
	 */
	@SuppressWarnings("unused")
	public abstract void notifySuccess();
	
	/**
	 * Add {@link JdaUtils#EMOJI_DENY} as a reaction to the command message
	 */
	@SuppressWarnings("unused")
	public abstract void notifyFailure();
	
	/**
	 * Return the representation of the arguments passed to the command as a String
	 */
	@Override
	public String toString() {
		return "args=" + Arrays.toString(args) + ";pipeline=" + SFUtils.deepToString(pipeline) + ";pIndex=" + currentPipelineIndex;
	}
	
}
