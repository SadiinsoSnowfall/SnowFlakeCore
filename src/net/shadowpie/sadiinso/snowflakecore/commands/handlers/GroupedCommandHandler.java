package net.shadowpie.sadiinso.snowflakecore.commands.handlers;

import java.util.HashMap;
import java.util.Map;

import net.shadowpie.sadiinso.snowflakecore.commands.Commands;
import net.shadowpie.sadiinso.snowflakecore.commands.context.CommandContext;
import net.shadowpie.sadiinso.snowflakecore.permissions.OriginPerms;

public class GroupedCommandHandler extends AbstractCommandHandler {

	public final Map<String, AbstractCommandHandler> subCommands = new HashMap<>();
	
	public GroupedCommandHandler(String name, String description, String allowFrom) {
		super(name, null, null, description, OriginPerms.compute(allowFrom), null);
	}
	
	public boolean addCommandHandler(AbstractCommandHandler handler) {
		return (subCommands.put(handler.name.toLowerCase(), handler) == null);
	}

	@Override
	public int execute(CommandContext ctx) {
		var handler = subCommands.get(ctx.prefix().toLowerCase());
		return (handler == null ? Commands.COMMAND_NOT_FOUND : handler.execute(ctx.pullPrefix()));
	}

}
