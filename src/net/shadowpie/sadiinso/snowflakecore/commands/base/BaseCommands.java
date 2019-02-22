package net.shadowpie.sadiinso.snowflakecore.commands.base;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.shadowpie.sadiinso.snowflakecore.commands.context.CommandContext;
import net.shadowpie.sadiinso.snowflakecore.commands.declaration.ASFCommand;
import net.shadowpie.sadiinso.snowflakecore.snowflakecore.SFC;

public class BaseCommands {

	@ASFCommand(
		name = "stop",
		description = "stop the bot",
		allowFrom = "all/ownerOnly"
	)
	public static void stop(CommandContext ctx) {
		ctx.warn("Shutting down the bot...");
		SFC.shutdown();
	}
	
	@ASFCommand(
		name = "logall",
		description = "Log every messages the bot has access to at instant t",
		usage="logall [number]",
		allowFrom = "all/ownerOnly"
	)
	public static void logall(CommandContext ctx) {
		int tmp = 100;
		
		if(ctx.args().length > 0) {
			try {
				tmp = Integer.parseInt(ctx.args()[0]);
			} catch(Exception e) {
				ctx.reply("\"" + ctx.args()[0] + "\" is not an number");
			}
		}
		
		ctx.reply("logging the last " + tmp + " messages...");
		String dirName = "logs_" + System.currentTimeMillis();
		
		new File(dirName).mkdir();
		List<Message> msgs;
		
		for(PrivateChannel chan : SFC.getJDA().getPrivateChannels()) {
			try {
				msgs = chan.getHistory().retrievePast(100).complete();
			} catch(Exception e) {
				ctx.reply("Error while retrieving messages from private channel " + chan.getName() + "(" + chan.getId() + ")");
				continue;
			}
			
			if(msgs.size() <= 0)
				continue;
			
			try (PrintWriter w = new PrintWriter(new FileWriter(new File(dirName + "/" + chan.getId())))) {
				for(Message msg : msgs)
					w.println(msg.getAuthor().getName() + "(" + msg.getAuthor().getId() + ") > " + msg.getContentRaw());
			} catch(Exception e) {
				ctx.reply("Error while retrieving messages from private channel " + chan.getName() + "(" + chan.getId() + ")");
			}
		}
		
		for(Guild guild : SFC.getJDA().getGuilds()) {
			String gname = dirName + "/" + guild.getName() + "_" + guild.getId();
			new File(gname).mkdir();
			
			for(TextChannel chan : guild.getTextChannels()) {
				try {
					msgs = chan.getHistory().retrievePast(100).complete();
				} catch(Exception e) {
					ctx.reply("Error while retrieving messages from private channel " + chan.getName() + "(" + chan.getId() + ")");
					continue;
				}
				
				if(msgs.size() <= 0)
					continue;
				
				try (PrintWriter w = new PrintWriter(new FileWriter(new File(gname + "/" + chan.getName() + "(" + chan.getId() + ")")))) {
					for(Message msg : msgs)
						w.println(msg.getAuthor().getName() + "(" + msg.getAuthor().getId() + ") > " + msg.getContentRaw());
				} catch(Exception e) {
					ctx.reply("Error while retrieving messages from guild channel " + chan.getName() + "(" + chan.getId() + ")");
				}
			}
		}
		
		ctx.reply("logging finished");
	}

}