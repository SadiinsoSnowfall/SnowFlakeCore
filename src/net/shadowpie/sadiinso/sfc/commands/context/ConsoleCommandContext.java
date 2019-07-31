package net.shadowpie.sadiinso.sfc.commands.context;

import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.utils.JDALogger;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.LinkedList;

public class ConsoleCommandContext extends CommandContext {

	private static final Logger logger = JDALogger.getLog("Console_Commands");
	
	public static CommandContext getContext(String message) {
		LinkedList<CommandContextFrame> frames = CommandContextUtils.extractFrames(message.toCharArray());
		if(frames == null) {
			return null;
		}
		
		return new ConsoleCommandContext(frames);
	}
	
	private ConsoleCommandContext(LinkedList<CommandContextFrame> frames) {
		super(frames, false);
	}
	
	@Override
	public ContextOrigin getOrigin() {
		return ContextOrigin.CONSOLE;
	}
	
	@Override
	public Guild getGuild() {
		return null;
	}
	
	@Override
	public Message getMessage() {
		return null;
	}

	@Override
	public MessageChannel getChannel() {
		return null;
	}
	
	@Override
	public String getAuthorId() {
		return null;
	}
	
	@Override
	public long getAuthorIdLong() {
		return 0L;
	}
	
	@Override
	public User getAuthor() {
		return null;
	}
	
	@Override
	public String getAuthorAsMention() {
		return StringUtils.EMPTY;
	}
	
	@Override
	public User getAsUser(int index) {
		return null;
	}
	
	@Override
	public Member getAsMember(int index) {
		return null;
	}

	@Override
	public Role getAsRole(int index) {
		return null;
	}

	@Override
	public TextChannel getAsTextChannel(int index) {
		return null;
	}

	@Override
	public VoiceChannel getAsVoiceChannel(int index) {
		return null;
	}
	
	@Override
	public Category getAsCategory(int index) {
		return null;
	}
	
	@Override
	public void reply(CharSequence str) {
		System.out.println(str);
	}
	
	@Override
	public void replyAsEmbed(CharSequence message, Color color) {
		System.out.println(message);
	}

	@Override
	public void reply(MessageEmbed embed) {
		for(Field field : embed.getFields())
			System.out.println(field.getName() + " : " + field.getValue());
	}

	@Override
	public void react(String unicode) {
		System.out.println("reaction: " + unicode);
	}

	@Override
	public void sendFile(File file) {
		logger.warn("ConsoleCommandContext#sendFile: Unsupported operation");
	}
	
	@Override
	public void sendFile(File file, CharSequence message) {
		logger.warn("ConsoleCommandContext#sendFile: Unsupported operation");
	}
	
	@Override
	public void sendFile(byte[] file, CharSequence message) {
		logger.warn("ConsoleCommandContext#sendFile: Unsupported operation");
	}

	@Override
	public void sendImage(RenderedImage img, CharSequence message) {
		logger.warn("ConsoleCommandContext#sendFile: Unsupported operation");
	}

	@Override
	public void sendImage(RenderedImage img) {
		logger.warn("ConsoleCommandContext#sendImage: Unsupported operation");
	}

	@Override
	public void notifySuccess() {
		System.out.println("command executed successfully");
	}

	@Override
	public void notifyFailure() {
		System.out.println("command execution failed");
	}
	
}
