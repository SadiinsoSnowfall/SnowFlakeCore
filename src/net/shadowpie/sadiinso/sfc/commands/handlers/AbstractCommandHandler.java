package net.shadowpie.sadiinso.sfc.commands.handlers;

import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;

public abstract class AbstractCommandHandler {

	public final String name;
	public final String alias;
	public final String computedUsage;
	public final String description;
	public final String[] perms;
	public final byte basePerms;
	
	public AbstractCommandHandler(String name, String alias, String usage, String description, byte basePerms, String[] perms) {
		this.name = name;
		this.alias = ("".equals(alias) ? null : alias);
		this.basePerms = basePerms;
		this.description = description;
		this.perms = perms;
		
		if((usage != null) && !usage.isEmpty()) {
			if(!usage.startsWith(name))
				this.computedUsage = name + " " + usage;
			else
				this.computedUsage = usage;
		} else {
			this.computedUsage = null;
		}
	}
	
	public abstract int execute(CommandContext ctx);
	
	
}
