package net.shadowpie.sadiinso.snowflakecore.commands.base;

import net.dv8tion.jda.core.EmbedBuilder;
import net.shadowpie.sadiinso.snowflakecore.commands.Commands;
import net.shadowpie.sadiinso.snowflakecore.commands.context.CommandContext;
import net.shadowpie.sadiinso.snowflakecore.commands.declaration.ASFCommand;
import net.shadowpie.sadiinso.snowflakecore.commands.handlers.AbstractCommandHandler;
import net.shadowpie.sadiinso.snowflakecore.commands.handlers.GroupedCommandHandler;
import net.shadowpie.sadiinso.snowflakecore.permissions.OriginPerms;
import net.shadowpie.sadiinso.snowflakecore.utils.JdaUtils;

public final class HelpCommand {

	public static String STR_CMD_NOT_FOUND = "Commande inconnue : %s";
	
	// Suppresses default constructor, ensuring non-instantiability.
	private HelpCommand() {}

	@ASFCommand(name="help", usage = "[command]", description = "Affiche la liste des commandes")
	public static void help(CommandContext ctx) {
		if(ctx.argc() > 0) {
			String path = ctx.packArgs(".");
			AbstractCommandHandler handler = Commands.findCommand(path);
			
			if(handler == null) {
				ctx.warn(STR_CMD_NOT_FOUND.replaceAll("%s", path));
				return;
			}
			
			if (OriginPerms.has(handler.basePerms, OriginPerms.PERM_OWNER_ONLY)) { // skip owner_only commands
				ctx.warn(STR_CMD_NOT_FOUND.replaceAll("%s", path));
				return;
			}
			
			if(handler instanceof GroupedCommandHandler) {
				StringBuilder base = new StringBuilder(32);
				StringBuilder groups = new StringBuilder(32);
				
				for(AbstractCommandHandler sub : ((GroupedCommandHandler) handler).subCommands.values()) {
					if (OriginPerms.has(handler.basePerms, OriginPerms.PERM_OWNER_ONLY)) // skip owner_only commands
						continue;
					
					if (sub instanceof GroupedCommandHandler) {
						groups.append(sub.name);
						groups.append('\n');
					} else {
						base.append(sub.name);
						base.append('\n');
					}
				}
				
				EmbedBuilder embed = JdaUtils.getEmbedBuilder();
				embed.addField("Groupe \"" + path + "\"", handler.description, false);
				if(base.length() > 0) embed.addField("Commandes", base.toString(), true);
				if(groups.length() > 0) embed.addField("Groupes", groups.toString(), true);
				ctx.reply(embed);
				
			} else {
				EmbedBuilder embed = null;
				String alias = "";
				
				if(handler.alias != null)
					alias = " alias \"" + handler.alias + "\"";
				
				if(handler.computedUsage != null) {
					embed = JdaUtils.getEmbedBuilder("Commande \"" + path + "\"" + alias);
					embed.addField(handler.computedUsage, handler.description, false);
				} else {
					embed = JdaUtils.getEmbedBuilder();
					embed.addField("Commande \"" + path + "\"" + alias, handler.description, false);
				}
				ctx.reply(embed);
			}
			
			return;
		}
		
		StringBuilder base = new StringBuilder(64);
		StringBuilder groups = new StringBuilder(64);

		for (AbstractCommandHandler handler : Commands.getMap().values()) {
			if (OriginPerms.has(handler.basePerms, OriginPerms.PERM_OWNER_ONLY)) // skip owner_only commands
				continue;

			if (handler instanceof GroupedCommandHandler) {
				groups.append(handler.name);
				groups.append('\n');
			} else {
				base.append(handler.name);
				base.append('\n');
			}
		}

		EmbedBuilder embed = JdaUtils.getEmbedBuilder("Liste des commandes");
		if(base.length() > 0) embed.addField("Commandes", base.toString(), true);
		if(groups.length() > 0) embed.addField("Groupes", groups.toString(), true);
		ctx.reply(embed);
	}

	@ASFCommand(name = "ownerhelp", allowFrom = "all/ownerOnly", usage = "[command]", description = "Affiche la liste des commandes \"owneronly\"")
	public static void ownerHelp(CommandContext ctx) {
		StringBuilder base = new StringBuilder(64);
		StringBuilder groups = new StringBuilder(64);

		for (AbstractCommandHandler handler : Commands.getMap().values()) {
			if (!OriginPerms.has(handler.basePerms, OriginPerms.PERM_OWNER_ONLY)) // skip mainstreams commands
				continue;

			if (handler instanceof GroupedCommandHandler) {
				groups.append(handler.name);
				groups.append('\n');
			} else {
				base.append(handler.name);
				base.append('\n');
			}
		}

		EmbedBuilder builder = JdaUtils.getEmbedBuilder("Liste des commandes");
		if(base.length() > 0) builder.addField("Commandes", base.toString(), true);
		if(groups.length() > 0) builder.addField("Groupes", groups.toString(), true);
		
		ctx.reply(builder);
	}

}
