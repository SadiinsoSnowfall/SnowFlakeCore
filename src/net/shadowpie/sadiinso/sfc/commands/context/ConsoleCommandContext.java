package net.shadowpie.sadiinso.sfc.commands.context;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.MessageEmbed.Field;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.JDALogger;

public class ConsoleCommandContext extends CommandContext {

	private static final Logger logger = JDALogger.getLog("Console_Commands");
	
	public static CommandContext getContext(String message) {
		return new ConsoleCommandContext(message.trim());
	}
	
	private ConsoleCommandContext(String msg) {
		super(msg);
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
	public String getUserAsMention() {
		return StringUtils.EMPTY;
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
	public void reply(String str) {
		System.out.println(str);
	}
	
	@Override
	public void replyAsEmbed(String message, Color color) {
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
		logger.warn("ConsoleCommandContext@sendFile: Unsupported operation");
	}
	
	@Override
	public void sendFile(File file, String message) {
		logger.warn("ConsoleCommandContext@sendFile: Unsupported operation");
	}
	
	@Override
	public void sendFile(byte[] file, String message) {
		logger.warn("ConsoleCommandContext@sendFile: Unsupported operation");
	}

	@Override
	public void sendImage(RenderedImage img, String message) {
		logger.warn("ConsoleCommandContext@sendFile: Unsupported operation");
	}

	@Override
	public void sendImage(RenderedImage img) {
		logger.warn("ConsoleCommandContext@sendImage: Unsupported operation");
	}
	
}
