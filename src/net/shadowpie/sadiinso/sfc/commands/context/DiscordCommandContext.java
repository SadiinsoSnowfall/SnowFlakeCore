package net.shadowpie.sadiinso.sfc.commands.context;

import net.dv8tion.jda.core.entities.*;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler;
import net.shadowpie.sadiinso.sfc.sfc.SFC;
import net.shadowpie.sadiinso.sfc.utils.JdaUtils;
import net.shadowpie.sadiinso.sfc.utils.SFUtils;
import net.shadowpie.sadiinso.sfc.utils.SStringBuilder;

import java.awt.*;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class DiscordCommandContext extends CommandContext {
	
	// command Message entity
	private final Message message;

	public static CommandContext getContext(Message message) {
		String content = message.getContentRaw();
		if (content.length() < 2) {
			return null;
		}
		
		boolean useTag = (content.startsWith(ConfigHandler.bot_tag()));
		boolean useMention = (!useTag && ConfigHandler.use_mention() && content.startsWith(SFC.selfMention()));

		if (!useTag && !useMention) {
			return null;
		}
		
		SStringBuilder resolved = CommandContextUtils.resolveMentions(content, useMention);
		if(resolved == null) {
			return null;
		}
		
		LinkedList<CommandContextFrame> frames = CommandContextUtils.extractFrames(resolved);
		if(frames == null) {
			System.out.println("frames == null");
			return null;
		}

		return new DiscordCommandContext(frames, message, useMention);
	}
	
	private DiscordCommandContext(LinkedList<CommandContextFrame> frames, Message msg, boolean useMention) {
		super(frames, useMention);
		this.message = msg;
	}
	
	@Override
	public ContextOrigin getOrigin() {
		return ((message.getChannel() instanceof TextChannel) ? ContextOrigin.SERVER : ContextOrigin.PRIVATE);
	}
	
	@Override
	public Guild getGuild() {
		return message.getGuild();
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
	public String getAuthorAsMention() {
		return message.getAuthor().getAsMention();
	}
	
	@Override
	public User getAsUser(int index) {
		try {
			return SFC.getJDA().retrieveUserById(getAsLong(index)).complete();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public Member getAsMember(int index) {
		Guild guild = this.getGuild();
		if(guild == null)
			return null;
		
		try {
			Member member = guild.getMemberById(getAsLong(index));
			if(member != null) {
				return member;
			}
		} catch (Exception ignored) {}
		
		List<Member> matchs = guild.getMembersByName(arg(index), true);
		return (matchs.isEmpty() ? null : matchs.get(0));
	}
	
	@Override
	public Role getAsRole(int index) {
		Guild guild = this.getGuild();
		if(guild == null)
			return null;
		
		try {
			Role role = guild.getRoleById(getAsLong(index));
			if(role != null) {
				return role;
			}
		} catch (Exception ignored) {}
		
		List<Role> matchs = guild.getRolesByName(arg(index), true);
		return (matchs.isEmpty() ? null : matchs.get(0));
	}
	
	@Override
	public TextChannel getAsTextChannel(int index) {
		Guild guild = this.getGuild();
		if(guild == null)
			return null;
		
		try {
			TextChannel channel = guild.getTextChannelById(getAsLong(index));
			if(channel != null) {
				return channel;
			}
		} catch (Exception ignored) {}
		
		List<TextChannel> matchs = guild.getTextChannelsByName(arg(index), true);
		return (matchs.isEmpty() ? null : matchs.get(0));
	}
	
	@Override
	public VoiceChannel getAsVoiceChannel(int index) {
		Guild guild = this.getGuild();
		if(guild == null)
			return null;
		
		try {
			VoiceChannel channel = guild.getVoiceChannelById(getAsLong(index));
			if(channel != null) {
				return channel;
			}
		} catch (Exception ignored) {}
		
		List<VoiceChannel> matchs = guild.getVoiceChannelsByName(arg(index), true);
		return (matchs.isEmpty() ? null : matchs.get(0));
	}
	
	@Override
	public Category getAsCategory(int index) {
		Guild guild = this.getGuild();
		if(guild == null)
			return null;
		
		try {
			Category category = guild.getCategoryById(getAsLong(index));
			if(category != null) {
				return category;
			}
		} catch (Exception ignored) {}
		
		List<Category> matchs = guild.getCategoriesByName(arg(index), true);
		return (matchs.isEmpty() ? null : matchs.get(0));
	}
	
	//#######################
	// quick reply & reaction
	//#######################
	
	@Override
	public void reply(CharSequence str) {
		message.getChannel().sendMessage(str).queue();
	}
	
	@Override
	public void replyAsEmbed(CharSequence msg, Color color) {
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
	public void sendFile(File file, CharSequence msg) {
		message.getChannel().sendFile(file, msg.toString()).queue();
	}
	
	@Override
	public void sendFile(byte[] file, CharSequence msg) {
		message.getChannel().sendFile(file, msg.toString()).queue();
	}
	
	@Override
	public void sendImage(RenderedImage img) {
		sendImage(img, "unknow.png");
	}

	@Override
	public void sendImage(RenderedImage img, CharSequence msg) {
		byte[] converted;
		
		try {
			converted = SFUtils.imgToByteArray(img, "png");
		} catch(Exception e) {
			logger.error("Error while converting an image", e);
			return;
		}
		
		message.getChannel().sendFile(converted, msg.toString()).queue();
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
