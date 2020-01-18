package net.shadowpie.sadiinso.sfc.commands.base;

import net.dv8tion.jda.api.EmbedBuilder;
import net.shadowpie.sadiinso.sfc.commands.Commands;
import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;
import net.shadowpie.sadiinso.sfc.commands.declaration.SFCommand;
import net.shadowpie.sadiinso.sfc.commands.handlers.AbstractCommandHandler;
import net.shadowpie.sadiinso.sfc.commands.handlers.GroupedCommandHandler;
import net.shadowpie.sadiinso.sfc.permissions.OriginPerms;
import net.shadowpie.sadiinso.sfc.utils.JdaUtils;
import net.shadowpie.sadiinso.sfc.utils.SFUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class HelpCommand {

	public static final String STR_CMD_NOT_FOUND = "Commande inconnue : ";
	
	@SFCommand(
			name="help",
			usage = "[command]",
			description = "Affiche la liste des commandes"
	)
	public static void onHelp(CommandContext ctx) {
		showHelp(ctx, JdaUtils.isBotOwner(ctx.getAuthor()));
	}
	
	public static void showHelp(CommandContext ctx, boolean owner) {
		if(ctx.argc() > 0) {
			String path = ctx.packArgs(".").toLowerCase();
			AbstractCommandHandler handler = Commands.findCommand(path);
			
			if(handler == null) {
				ctx.warn(STR_CMD_NOT_FOUND + path);
				return;
			}
			
			if (OriginPerms.has(handler.originPerms, OriginPerms.OWNER_ONLY) && !owner) { // skip owner_only commands if needed
				ctx.warn(STR_CMD_NOT_FOUND + path);
				return;
			}
			
			if(handler instanceof GroupedCommandHandler) {
				List<String> commands = new LinkedList<>();
				List<String> groups = new LinkedList<>();
				
				for(AbstractCommandHandler sub : ((GroupedCommandHandler) handler).subCommands.values()) {
					if (!OriginPerms.has(handler.originPerms, OriginPerms.OWNER_ONLY) || owner) { // skip owner_only commands if needed
						if (sub instanceof GroupedCommandHandler) {
							groups.add(sub.name);
						} else {
							commands.add(sub.name);
						}
					}
				}
				
				EmbedBuilder embed = JdaUtils.getEmbedBuilder();
				embed.addField("Groupe \"" + path + "\"", SFUtils.monospace(handler.description), false);
				
				
				if(commands.size() > 0) {
					Collections.sort(commands);
					embed.addField("Commandes :", SFUtils.monospace(SFUtils.snapFormat(commands, 8, 2)), true);
				}
				
				if(groups.size() > 0) {
					Collections.sort(groups);
					embed.addField("Groupes :", SFUtils.monospace(SFUtils.snapFormat(groups, 8, 2)), true);
				}
				
				ctx.reply(embed);
				
			} else { // single command
				EmbedBuilder embed;
				String alias = "";
				
				if(handler.alias != null)
					alias = " alias \"" + handler.alias + "\"";
				
				if(handler.computedUsage != null) {
					embed = JdaUtils.getEmbedBuilder("Commande \"" + path + "\"" + alias);
					embed.addField(handler.description, SFUtils.monospace(handler.computedUsage), false);
				} else {
					embed = JdaUtils.getEmbedBuilder();
					embed.addField("Commande \"" + path + "\"" + alias, handler.description, false);
				}
				
				ctx.reply(embed);
			}
			
			return;
		}
		
		EmbedBuilder embed = JdaUtils.getEmbedBuilder("Liste des commandes");
		List<String> commands = new LinkedList<>();
		List<String> groups = new LinkedList<>();

		for (AbstractCommandHandler handler : Commands.getMap().values()) {
			if (!OriginPerms.has(handler.originPerms, OriginPerms.OWNER_ONLY) || owner) { // skip owner_only commands if needed
				if (handler instanceof GroupedCommandHandler) {
					groups.add(handler.name);
				} else {
					commands.add(handler.name);
				}
			}
		}
		
		if(commands.size() > 0) {
			Collections.sort(commands);
			embed.addField("Commandes :", SFUtils.monospace(SFUtils.snapFormat(commands, 8, 2)), true);
		}
		
		if(groups.size() > 0) {
			Collections.sort(groups);
			embed.addField("Groupes :", SFUtils.monospace(SFUtils.snapFormat(groups, 8, 2)), true);
		}
		
		ctx.reply(embed);
	}
	
}
