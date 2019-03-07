package net.shadowpie.sadiinso.sfc.commands.base;

import java.util.concurrent.TimeUnit;

import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;
import net.shadowpie.sadiinso.sfc.commands.context.ContextOrigin;
import net.shadowpie.sadiinso.sfc.commands.declaration.ASFCommand;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler;
import net.shadowpie.sadiinso.sfc.listeners.eventwaiter.ButtonMenu;
import net.shadowpie.sadiinso.sfc.sfc.SFC;
import net.shadowpie.sadiinso.sfc.utils.JdaUtils;

public class BaseCommands {

	@ASFCommand(
		name = "stop",
		description = "stop the bot",
		allowFrom = "all/ownerOnly"
	)
	public static void stop(CommandContext ctx) {
		if(ctx.getOrigin() == ContextOrigin.CONSOLE) {
			ctx.warn("Shutting down the bot...");
			SFC.shutdown();
			return;
		}
		
		ButtonMenu.create("Shutting down", "Please confirm this operation").addChoice(JdaUtils.EMOJI_ACCEPT, r -> {
			ctx.warn("Shutting down the bot...");
			SFC.shutdown();
		}).addChoice(JdaUtils.EMOJI_DENY, r -> {
			ctx.warn("Shutdown canceled");
		}).setColor(ConfigHandler.color_warn()).setTimeout(30, TimeUnit.SECONDS).build().display(ctx.getChannel());
	}

}