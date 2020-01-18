package net.shadowpie.sadiinso.sfc.commands.handlers;

import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCommandHandler {

	public final String name;
	public final String alias;
	public final String computedUsage;
	public final String description;
	public final String[] perms;
	public final byte originPerms;
	
	public AbstractCommandHandler(String name, String alias, String usage, String description, byte originPerms, String[] perms) {
		this.name = name;
		this.alias = (((alias == null) || alias.isEmpty()) ? null : alias);
		this.originPerms = originPerms;
		this.description = description;
		this.perms = perms;
		
		if ((usage == null) || usage.isEmpty()) {
			this.computedUsage = null;
		} else {
			if(!usage.startsWith(name)) {
				this.computedUsage = name + " " + usage;
			} else {
				this.computedUsage = usage;
			}
		}
	}
	
	public abstract int execute(@NotNull CommandContext ctx);
	
}
