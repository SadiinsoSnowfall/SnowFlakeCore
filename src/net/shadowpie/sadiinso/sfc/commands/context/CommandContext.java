package net.shadowpie.sadiinso.sfc.commands.context;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.utils.JDALogger;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler;
import net.shadowpie.sadiinso.sfc.utils.JdaUtils;
import net.shadowpie.sadiinso.sfc.utils.SFUtils;
import net.shadowpie.sadiinso.sfc.utils.SStringBuilder;
import org.slf4j.Logger;

import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;

public abstract class CommandContext {
	
	protected static final Logger logger = JDALogger.getLog("Command Context");
	
	/**
	 * Index for retrieving values from the command pipeline read buffer
	 */
	public static final int PIPELINE = -42;
	
	//#####
	//FLAGS
	//#####
	
	/**
	 * Indicate that the command pipeline execution should stop after the current command
	 */
	public static final byte FLAG_BREAK_PIPELINE = 1;
	
	/**
	 * Indicate that the command pipeline execution should stop after a warning / error is emitted
	 */
	public static final byte FLAG_BREAK_PIPELINE_ON_WARN = 2;
	
	//#############
	//INSTANCE VARS
	//#############
	
	/**
	 * Current command arguments
	 */
	private CommandContextFrame cframe;
	
	/**
	 * Command pipeline
	 */
	private CommandContextFrame[] pipeline;
	
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
	
	protected CommandContext(LinkedList<CommandContextFrame> frames, boolean useMention) {
		this.useMention = useMention;
		this.cframe = frames.pollFirst();
		
		if(!frames.isEmpty()) {
			pipeline = frames.toArray(CommandContextFrame[]::new);
			currentPipelineIndex = 0;
			pipelineOutBuffer = new SStringBuilder();
			pipelineInBuffer = new SStringBuilder();
		}
	}
	
	//################
	//FLAGS OPERATIONS
	//################
	
	public void setFlag(byte flag) {
		flags |= flag;
	}
	
	public boolean hasFlag(byte flag) {
		return ((flags & flag) > 0);
	}
	
	//###################
	//PIPELINE OPERATIONS
	//###################
	
	/**
	 * Indicate that the command pipeline execution should stop after a warning / error is emitted
	 */
	@SuppressWarnings("unused")
	public void breakPipelineOnWarn() {
		setFlag(FLAG_BREAK_PIPELINE_ON_WARN);
	}
	
	/**
	 * Stop the command pipeline from executing further
	 */
	@SuppressWarnings("unused")
	public void breakPipeline() {
		setFlag(FLAG_BREAK_PIPELINE);
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
		
		cframe = pipeline[currentPipelineIndex++];
		if(currentPipelineIndex >= pipeline.length) {
			pipeline = null;
		}
		
		// swap and reset buffers
		swapPipelineBuffers();
		
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
		return ((pipelineInBuffer != null) && !pipelineInBuffer.isEmpty());
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
	 * Write the given message to the pipeline or pass it to the {@link CommandContext#info(CharSequence)} method if the
	 * current context don't have a pipeline
	 * @param msg The message to pass
	 */
	public void wtpOrInfo(CharSequence msg) {
		if(hasPipeline()) {
			writeToPipe(msg);
		} else {
			info(msg);
		}
	}
	
	/**
	 * Swap the command pipeline in and out buffers
	 */
	private void swapPipelineBuffers() {
		// swap buffers
		SStringBuilder tmp = pipelineInBuffer;
		pipelineInBuffer = pipelineOutBuffer;
		pipelineOutBuffer = tmp;
		
		// empty out buffer
		pipelineOutBuffer.setLength(0);
	}
	
	/**
	 * Return the number of arguments passed to the command
	 * @param includePipe Whether or not to include the pipeline buffer in the result
	 * @return The command arguments count
	 */
	@SuppressWarnings("unused")
	public int argc(boolean includePipe) {
		return (includePipe && hasPipeContents() ? cframe.crs.length + 1 : cframe.crs.length);
	}
	
	/**
	 * Return the contents of the pipeline buffer or the argument at the given index if the pipeline
	 * buffer is empty
	 * @param index The argument index
	 */
	@SuppressWarnings("unused")
	public String pipeOrArg(int index)  {
		return (hasPipeContents() ? getPipeContents() : arg(index));
	}
	
	/**
	 * Merge the pipeline buffer contents and the argument list (the pipeline content will be added at
	 * the end of the argument list)
	 * @return Whether the operation happened (if the pipeline buffer is not empty) or not
	 * @see CommandContext#mergePipeAndArgs(boolean)
	 */
	@SuppressWarnings("unused")
	public boolean mergePipeAndArgs() {
		return mergePipeAndArgs(false);
	}
	
	/**
	 * Merge the pipeline buffer contents and the argument list
	 * @param before Whether to put the pipeline contents before or after the argument list
	 * @return Whether the operation happened (if the pipeline buffer is not empty) or not
	 */
	@SuppressWarnings("unused")
	public boolean mergePipeAndArgs(boolean before) {
		if(hasPipeContents()) {
			final int len = cframe.args.length;
			final String[] tmp = new String[len + 1];
			
			if(cframe.crs != null) {
				for (int i = 0; i < cframe.crs.length; i++) {
					++cframe.crs[i];
				}
			}
			
			if(before) {
				System.arraycopy(cframe.args, 0, tmp, 1, len);
				tmp[0] = pipelineInBuffer.toString();
			} else {
				System.arraycopy(cframe.args, 0, tmp, 0, len);
				tmp[len] = pipelineInBuffer.toString();
			}
			
			cframe.args = tmp;
			return true;
		} else {
			return false;
		}
	}
	
	//####################
	//ARGUMENTS OPERATIONS
	//####################
	
	/**
	 * Return the current first command argument (AKA prefix)
	 */
	public String prefix() {
		return (cframe.args.length > 0 ? cframe.args[0] : null);
	}
	
	/**
	 * Rebuild the context without the first command prefix
	 */
	public CommandContext pullPrefix() {
		if(cframe.args.length > 1) {
			cframe.args = Arrays.copyOfRange(cframe.args, 1, cframe.args.length);
			
			// shift and optimize the carriage return index array
			if(cframe.crs != null) {
				int toSkip = 0;
				for (int t = 0; t < cframe.crs.length; t++) {
					if(--cframe.crs[t] <= 0) {
						++toSkip;
					}
				}
				
				if(toSkip >= cframe.crs.length) {
					cframe.crs = null;
				} else if(toSkip > 0) {
					cframe.crs = Arrays.copyOfRange(cframe.crs, toSkip, cframe.crs.length);
				}
			}
		} else {
			cframe.args = new String[0];
		}
		
		return this;
	}
	
	/**
	 * Return the arguments passed to the command
	 */
	public String[] args() {
		return cframe.args;
	}
	
	/**
	 * Split the arguments at each newline
	 */
	@SuppressWarnings("unused")
	public String[][] splitLines() {
		if(cframe.crs == null) {
			return new String[][] { cframe.args };
		}
		
		String[][] split = new String[cframe.crs.length + 1][];
		
		int prev = 0;
		int index = 0;
		for(int t = 0; t < cframe.crs.length; t++) {
			split[index] = Arrays.copyOfRange(cframe.args, prev, cframe.crs[t]);
			prev = cframe.crs[t];
			++index;
		}
		
		split[index] = Arrays.copyOfRange(cframe.args, prev, cframe.args.length);
		
		return split;
	}
	
	/**
	 * Return the argument at the given index
	 * @param index The argument index or null if the index is out of bonds
	 */
	public String arg(int index) {
		return ((index < cframe.args.length) ? cframe.args[index] : null);
	}
	
	/**
	 * Return the number of arguments passed to the command
	 * @return The command arguments count
	 */
	public int argc() {
		return cframe.args.length;
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
			return Integer.parseInt(cframe.args[index]);
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
			return Long.parseLong(cframe.args[index]);
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
			return Double.parseDouble(cframe.args[index]);
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
			return Float.parseFloat(cframe.args[index]);
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
			return Boolean.parseBoolean(cframe.args[index]);
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
		return packArgs(0, cframe.args.length);
	}
	
	/**
	 * Pack all the arguments in one string, add the specified string between each argument
	 * @param between The {@link CharSequence} to add between two arguments
	 */
	@SuppressWarnings("unused")
	public String packArgs(CharSequence between) {
		return packArgs(0, cframe.args.length, between);
	}
	
	/**
	 * Pack all the arguments in one string
	 * @param start The first argument to pack
	 */
	@SuppressWarnings("unused")
	public String packArgs(int start) {
		return packArgs(start, cframe.args.length);
	}
	
	/**
	 * Pack all the arguments in one string, add the specified string between each argument
	 * @param start The first argument to pack
	 * @param between The {@link CharSequence} to add between two arguments
	 */
	@SuppressWarnings("unused")
	public String packArgs(int start, CharSequence between) {
		return packArgs(start, cframe.args.length, between);
	}
	
	/**
	 * Pack all the arguments in one string
	 * @param start The first argument to pack
	 * @param end The last argument to pack
	 */
	@SuppressWarnings("unused")
	public String packArgs(int start, int end) {
		StringBuilder builder = new StringBuilder(32);
		end = Math.min(cframe.args.length, end);
		
		for(int t = Math.max(0, start); t < end; t++) {
			builder.append(cframe.args[t]);
		}
		
		return builder.toString();
	}
	
	/**
	 * Pack all the arguments in one string, add the specified string between each argument
	 * @param start The first argument to pack
	 * @param end The last argument to pack
	 * @param between The {@link CharSequence} to add between two arguments
	 */
	@SuppressWarnings("unused")
	public String packArgs(int start, int end, CharSequence between) {
		StringBuilder builder = new StringBuilder(32);
		end = Math.min(cframe.args.length - 1, end);
		
		for(int t = start; t < end; t++) {
			builder.append(cframe.args[t]);
			builder.append(between);
		}
		
		builder.append(cframe.args[end]); // add the final argument
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
	public abstract void reply(CharSequence str);
	
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
	public void info(CharSequence message) {
		replyAsEmbed(message, ConfigHandler.color_info());
	}
	
	/**
	 * Send a message in the context channel as an embed with the specified warn color
	 * @param message The message to send
	 */
	@SuppressWarnings("unused")
	public void warn(CharSequence message) {
		if(hasFlag(FLAG_BREAK_PIPELINE_ON_WARN)) {
			setFlag(FLAG_BREAK_PIPELINE);
		}
		
		replyAsEmbed(message, ConfigHandler.color_warn());
	}
	
	/**
	 * Send a message in the context channel as an embed with the specified error color
	 * @param message The message to send
	 */
	@SuppressWarnings("unused")
	public void error(CharSequence message) {
		if(hasFlag(FLAG_BREAK_PIPELINE_ON_WARN)) {
			setFlag(FLAG_BREAK_PIPELINE);
		}
		
		replyAsEmbed(message, ConfigHandler.color_error());
	}
	
	/**
	 * Send a message in the context channel as an embed with the specified theme color
	 * @param message The message to send
	 */
	@SuppressWarnings("unused")
	public void replyAsEmbed(CharSequence message) {
		replyAsEmbed(message, ConfigHandler.color_theme());
	}
	
	/**
	 * Send a message in the context channel as an embed with the specified color
	 * @param message The message to send
	 */
	@SuppressWarnings("unused")
	public abstract void replyAsEmbed(CharSequence message, Color color);
	
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
	public abstract void sendFile(File file, CharSequence name);
	
	/**
	 * Send a file to the channel
	 * @param file The file to send
	 * @param name The file name
	 */
	@SuppressWarnings("unused")
	public abstract void sendFile(byte[] file, CharSequence name);
	
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
	public abstract void sendImage(RenderedImage img, CharSequence name);
	
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
		return "args=" + Arrays.toString(cframe.args) + ";pipeline=" + SFUtils.deepToString(pipeline) + ";pIndex=" + currentPipelineIndex;
	}
	
}
