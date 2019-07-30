package net.shadowpie.sadiinso.sfc.commands.base;

import net.dv8tion.jda.core.entities.Guild;
import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;
import net.shadowpie.sadiinso.sfc.commands.context.ContextOrigin;
import net.shadowpie.sadiinso.sfc.commands.declaration.SFCommand;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler;
import net.shadowpie.sadiinso.sfc.listeners.eventwaiter.ButtonMenu;
import net.shadowpie.sadiinso.sfc.sfc.SFC;
import net.shadowpie.sadiinso.sfc.utils.JdaUtils;
import net.shadowpie.sadiinso.sfc.utils.SFUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BaseCommands {

	@SFCommand(name = "stop", description = "stop the bot", allowFrom = "all/ownerOnly")
	public static void stop(CommandContext ctx) {
		if (ctx.getOrigin() == ContextOrigin.CONSOLE) {
			ctx.warn("Shutting down the bot...");
			SFC.shutdown();
			return;
		}

		ButtonMenu.create().addChoice(JdaUtils.EMOJI_ACCEPT, r -> {
			ctx.warn("Shutting down the bot...");
			SFC.shutdown();
		}).addChoice(JdaUtils.EMOJI_DENY, r -> {
			ctx.warn("Shutdown canceled");
		}).setTimeout(30, TimeUnit.SECONDS).build().display(ctx.getChannel(), "Shutting down", "Please confirm this operation", ConfigHandler.color_warn());
	}

	@SFCommand(name = "serverList", description = "print the list of all servers the bot is connected to", allowFrom = "all/ownerOnly")
	public static void onServerList(CommandContext ctx) {
		StringBuilder sb = new StringBuilder(16);
		List<Guild> guilds = SFC.getJDA().getGuilds();
		List<String> strs = new ArrayList<>(guilds.size());

		for (Guild guild : guilds) {
			sb.setLength(0);
			sb.append(guild.getId());
			sb.append(" \"");
			sb.append(guild.getName());
			sb.append("\"");
			strs.add(sb.toString());
		}

		if (ctx.getOrigin() == ContextOrigin.CONSOLE)
			ctx.reply(SFUtils.snapFormat(strs, 10, 2));
		else
			ctx.reply(SFUtils.monospace(SFUtils.snapFormat(strs, 10, 2)));
	}
	
	@SFCommand(
			name = "echo",
			usage = "<value>",
			description = "Write the given value to the command pipeline"
	)
	public static void onEcho(CommandContext ctx) {
		if(ctx.argc() > 0) {
			ctx.wtpOrInfo(ctx.packArgs(" "));
		} else {
			if(ctx.hasPipeContents()) {
				if(ctx.hasPipeline()) {
					ctx.getPipeOut().append(ctx.getPipeIn());
				} else {
					ctx.info(ctx.getPipeContents());
				}
			} else {
				ctx.warn("No parameters");
				ctx.breakPipeline();
			}
		}
	}

}