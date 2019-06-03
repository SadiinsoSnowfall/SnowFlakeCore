package net.shadowpie.sadiinso.sfc.commands.context;

import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.List;

import net.dv8tion.jda.core.entities.*;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler;
import net.shadowpie.sadiinso.sfc.sfc.SFC;
import net.shadowpie.sadiinso.sfc.utils.JdaUtils;
import net.shadowpie.sadiinso.sfc.utils.Utils;

public class DiscordCommandContext extends CommandContext {
	
	// command Message entity
	private final Message message;

	public static CommandContext getContext(Message message) {
		String content = message.getContentRaw();
		if (content.length() < 2)
			return null;
		
		boolean useTag = (content.startsWith(ConfigHandler.bot_tag()));
		boolean useMention = (!useTag && ConfigHandler.use_mention() && content.startsWith(SFC.selfMention()));

		if (!useTag && !useMention)
			return null;

		return new DiscordCommandContext(message, useMention);
	}
	
	private DiscordCommandContext(Message msg, boolean useMention) {
		super(msg.getContentRaw(), useMention);
		this.message = msg;
	}
	
	@Override
	public ContextOrigin getOrigin() {
		return ((message.getChannel() instanceof TextChannel) ? ContextOrigin.SERVER : ContextOrigin.PRIVATE);
	}
	
	@Override
	public Guild getGuild() {
		MessageChannel chan = message.getChannel();
		if(chan instanceof TextChannel)
			return ((TextChannel) chan).getGuild();
		else
			return null;
	}
	
	@Override
	public Message getMessage() {
		return message;
	}
	
	@Override
	public MessageChannel getChannel() {
		return message.getChannel();
	}
	
	@Override
	public String getAuthorId() {
		return message.getAuthor().getId();
	}
	
	@Override
	public long getAuthorIdLong() {
		return message.getAuthor().getIdLong();
	}
	
	@Override
	public User getAuthor() {
		return message.getAuthor();
	}
	
	@Override
	public String getUserAsMention() {
		return message.getAuthor().getAsMention();
	}

	@Override
	public Member getAsMember(int index) {
		Guild guild = this.getGuild();
		if(guild == null)
			return null;
		
		try {
			Member member = guild.getMemberById(args[index]);
			if(member != null) {
				return member;
			}
		} catch (Exception ignored) {}
		
		List<Member> matchs = guild.getMembersByName(args[index], true);
		return (matchs.isEmpty() ? null : matchs.get(0));
	}
	
	@Override
	public Role getAsRole(int index) {
		Guild guild = this.getGuild();
		if(guild == null)
			return null;
		
		try {
			Role role = guild.getRoleById(args[index]);
			if(role != null) {
				return role;
			}
		} catch (Exception ignored) {}
		
		List<Role> matchs = guild.getRolesByName(args[index], true);
		return (matchs.isEmpty() ? null : matchs.get(0));
	}
	
	@Override
	public TextChannel getAsTextChannel(int index) {
		Guild guild = this.getGuild();
		if(guild == null)
			return null;
		
		try {
			TextChannel channel = guild.getTextChannelById(args[index]);
			if(channel != null) {
				return channel;
			}
		} catch (Exception ignored) {}
		
		List<TextChannel> matchs = guild.getTextChannelsByName(args[index], true);
		return (matchs.isEmpty() ? null : matchs.get(0));
	}
	
	@Override
	public VoiceChannel getAsVoiceChannel(int index) {
		Guild guild = this.getGuild();
		if(guild == null)
			return null;
		
		try {
			VoiceChannel channel = guild.getVoiceChannelById(args[index]);
			if(channel != null) {
				return channel;
			}
		} catch (Exception ignored) {}
		
		List<VoiceChannel> matchs = guild.getVoiceChannelsByName(args[index], true);
		return (matchs.isEmpty() ? null : matchs.get(0));
	}
	
	@Override
	public Category getAsCategory(int index) {
		Guild guild = this.getGuild();
		if(guild == null)
			return null;
		
		try {
			Category category = guild.getCategoryById(args[index]);
			if(category != null) {
				return category;
			}
		} catch (Exception ignored) {}
		
		List<Category> matchs = guild.getCategoriesByName(args[index], true);
		return (matchs.isEmpty() ? null : matchs.get(0));
	}
	
	//#######################
	// quick reply & reaction
	//#######################
	
	@Override
	public void reply(String str) {
		message.getChannel().sendMessage(str).queue();
	}
	
	@Override
	public void replyAsEmbed(String msg, Color color) {
		JdaUtils.sendAsEmbed(message.getChannel(), msg, color);
	}
	
	@Override
	public void reply(MessageEmbed embed) {
		message.getChannel().sendMessage(embed).queue();
	}
	
	@Override
	public void react(String unicode) {
		message.getChannel().addReactionById(message.getId(), unicode).queue();
	}
	
	@Override
	public void sendFile(File file) {
		message.getChannel().sendFile(file).queue();
	}
	
	@Override
	public void sendFile(File file, String msg) {
		message.getChannel().sendFile(file, msg).queue();
	}
	
	@Override
	public void sendFile(byte[] file, String msg) {
		message.getChannel().sendFile(file, msg).queue();
	}
	
	@Override
	public void sendImage(RenderedImage img) {
		sendImage(img, "unknow.png");
	}

	@Override
	public void sendImage(RenderedImage img, String msg) {
		byte[] converted;
		
		try {
			converted = Utils.imgToByteArray(img, "png");
		} catch(Exception e) {
			logger.error("Error while converting an image", e);
			return;
		}
		
		message.getChannel().sendFile(converted, msg).queue();
	}

	@Override
	public void notifySuccess() {
		react(JdaUtils.EMOJI_ACCEPT);
	}

	@Override
	public void notifyFailure() {
		react(JdaUtils.EMOJI_DENY);
	}
	
}
