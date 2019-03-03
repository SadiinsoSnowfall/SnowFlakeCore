package net.shadowpie.sadiinso.sfc.commands.base;

import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;
import net.shadowpie.sadiinso.sfc.commands.declaration.ASFCommand;
import net.shadowpie.sadiinso.sfc.sfc.SFC;

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

}