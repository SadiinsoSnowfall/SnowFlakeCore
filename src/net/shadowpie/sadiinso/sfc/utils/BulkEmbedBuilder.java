package net.shadowpie.sadiinso.sfc.utils;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class BulkEmbedBuilder {

	public static final int MAX_CHARS_PER_EMBED = 1024;

	private final String title;
	private final Color color;
	private final String pageStr;

	private final List<String> lines = new LinkedList<>();
	private List<MessageEmbed> cache = null;

	public BulkEmbedBuilder(String title) {
		this(title, ConfigHandler.color_theme());
	}
	
	public BulkEmbedBuilder(String title, Color color) {
		this.title = title;
		this.color = color;
		this.pageStr = title + " (page ";
	}

	/**
	 * Add a line of text to the messages buffer.
	 * @param line The line of text to add
	 */
	public void addLine(String line) {
		lines.add(line.endsWith("\n") ? line : line + '\n');
	}
	
	/**
	 * Return the lines cache
	 * @return A list of string
	 */
	public List<String> getLines() {
		return lines;
	}

	/**
	 * Build the MessageEmbed cache
	 * 
	 * @return A list of {@link MessageEmbed}
	 */
	public List<MessageEmbed> build() {
		int page = 1;

		StringBuilder builder = new StringBuilder(MAX_CHARS_PER_EMBED);
		cache = new LinkedList<>();

		for (String line : lines) {
			if (builder.length() + line.length() < MAX_CHARS_PER_EMBED) {
				builder.append(line);
			} else {
				cache.add(JdaUtils.sendAsEmbed(null, pageStr + page + ") :", builder.toString(), color));
				builder.setLength(0);
				builder.append(line);
				++page;
			}
		}
		
		if(builder.length() > 0)
			if(page > 1)
				cache.add(JdaUtils.sendAsEmbed(null, pageStr + page + ") :", builder.toString(), color));
			else
				cache.add(JdaUtils.sendAsEmbed(null, title + " :", builder.toString(), color));

		return cache;
	}

	/**
	 * Build the MessageEmbed cache and send the messages to the specified
	 * channel.<br>
	 * This method will build the cache, do not call the {@link #build()} method
	 * beforehand.
	 * 
	 * @param channel The channel to send the messages in.
	 * @return A list of {@link MessageEmbed}
	 */
	public List<MessageEmbed> send(MessageChannel channel) {
		build();
		
		if (channel != null) {
			for (MessageEmbed embed : cache) {
				channel.sendMessage(embed).complete();
			}
		}
		
		return cache;
	}

}
