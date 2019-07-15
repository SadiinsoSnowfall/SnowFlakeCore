package net.shadowpie.sadiinso.sfc.commands.handlers;

import net.shadowpie.sadiinso.sfc.commands.Commands;
import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;
import net.shadowpie.sadiinso.sfc.permissions.OriginPerms;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class GroupedCommandHandler extends AbstractCommandHandler {

	public final Map<String, AbstractCommandHandler> subCommands = new HashMap<>();
	
	public GroupedCommandHandler(String name, String description, String allowFrom) {
		super(name, null, null, description, OriginPerms.compute(allowFrom), null);
	}
	
	public boolean addCommandHandler(AbstractCommandHandler handler) {
		return (subCommands.put(handler.name.toLowerCase(), handler) == null);
	}

	@Override
	public int execute(@NotNull CommandContext ctx) {
		var handler = subCommands.get(ctx.prefix().toLowerCase());
		return (handler == null ? Commands.COMMAND_NOT_FOUND : handler.execute(ctx.pullPrefix()));
	}

}
