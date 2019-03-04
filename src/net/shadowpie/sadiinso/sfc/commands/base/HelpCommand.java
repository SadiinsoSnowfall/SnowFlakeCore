package net.shadowpie.sadiinso.sfc.commands.base;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import net.dv8tion.jda.core.EmbedBuilder;
import net.shadowpie.sadiinso.sfc.commands.Commands;
import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;
import net.shadowpie.sadiinso.sfc.commands.declaration.ASFCommand;
import net.shadowpie.sadiinso.sfc.commands.handlers.AbstractCommandHandler;
import net.shadowpie.sadiinso.sfc.commands.handlers.GroupedCommandHandler;
import net.shadowpie.sadiinso.sfc.permissions.OriginPerms;
import net.shadowpie.sadiinso.sfc.utils.JdaUtils;

public final class HelpCommand {

	public static String STR_CMD_NOT_FOUND = "Commande inconnue : %s";

	@ASFCommand(name = "ownerhelp", allowFrom = "all/ownerOnly", usage = "[command]", description = "Affiche la liste des commandes")
	public static void onOwnerHelp(CommandContext ctx) {
		showHelp(ctx, true);
	}
	
	@ASFCommand(name="help", usage = "[command]", description = "Affiche la liste des commandes")
	public static void onHelp(CommandContext ctx) {
		showHelp(ctx, false);
	}
	
	public static void showHelp(CommandContext ctx, boolean owner) {
		if(ctx.argc() > 0) {
			String path = ctx.packArgs(".");
			AbstractCommandHandler handler = Commands.findCommand(path);
			
			if(handler == null) {
				ctx.warn(STR_CMD_NOT_FOUND.replaceAll("%s", path));
				return;
			}
			
			if (OriginPerms.has(handler.basePerms, OriginPerms.PERM_OWNER_ONLY) && !owner) { // skip owner_only commands if needed
				ctx.warn(STR_CMD_NOT_FOUND.replaceAll("%s", path));
				return;
			}
			
			if(handler instanceof GroupedCommandHandler) {
				List<String> commands = new LinkedList<>();
				List<String> groups = new LinkedList<>();
				
				for(AbstractCommandHandler sub : ((GroupedCommandHandler) handler).subCommands.values()) {
					if (OriginPerms.has(handler.basePerms, OriginPerms.PERM_OWNER_ONLY) && !owner) // skip owner_only commands if needed
						continue;
					
					if (sub instanceof GroupedCommandHandler)
						groups.add(sub.name);
					else
						commands.add(sub.name);
				}
				
				EmbedBuilder embed = JdaUtils.getEmbedBuilder();
				embed.addField("Groupe \"" + path + "\"", monospace(handler.description), false);
				
				Collections.sort(commands);
				if(commands.size() > 0)embed.addField("Commandes :", monospace(snapFormat(commands, 7, 2)), true);
				
				Collections.sort(groups);
				if(groups.size() > 0)embed.addField("Groupes :", monospace(snapFormat(groups, 7, 2)), true);
				
				ctx.reply(embed);
				
			} else { // single command
				EmbedBuilder embed = null;
				String alias = "";
				
				if(handler.alias != null)
					alias = " alias \"" + handler.alias + "\"";
				
				if(handler.computedUsage != null) {
					embed = JdaUtils.getEmbedBuilder("Commande \"" + path + "\"" + alias);
					embed.addField(monospace(handler.computedUsage), handler.description, false);
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
			if (OriginPerms.has(handler.basePerms, OriginPerms.PERM_OWNER_ONLY) && !owner) // skip owner_only commands if needed
				continue;

			if (handler instanceof GroupedCommandHandler)
				groups.add(handler.name);
			else
				commands.add(handler.name);
		}
		
		Collections.sort(commands);
		if(commands.size() > 0) embed.addField("Commandes :", monospace(snapFormat(commands, 7, 2)), true);
		
		Collections.sort(groups);
		if(groups.size() > 0)embed.addField("Groupes :", monospace(snapFormat(groups, 7, 2)), true);
		
		ctx.reply(embed);
	}

	private static String monospace(String str) {
		StringBuilder sb = new StringBuilder(str.length() + 6);
		sb.append("```");
		sb.append(str);
		sb.append("```");
		return sb.toString();
	}
	
	private static String snapFormat(List<String> strs, int maxColSize, int spacing) {
		String[] arr = strs.toArray(new String[strs.size()]);
		
		// only one column
		if(strs.size() <= maxColSize) {
			int end = arr.length - 1;
			StringBuilder sb = new StringBuilder(strs.size() << 2);
			
			for(int t = 0; t < end; t++) {
				sb.append(arr[t]);
				sb.append('\n');
			}
			
			sb.append(arr[end]);
			return sb.toString();
		}
		
		// multiples columns
		int lastCol = arr.length % maxColSize;
		if(lastCol == 0)
			lastCol = maxColSize;
		
		// init padding
		int maxPad = 0;
		for(int t = 0; t < arr.length; t++)
			if(arr[t].length() > maxPad)
				maxPad = arr[t].length();
		char[] pad = new char[(maxPad + spacing) * 2];
		for(int t = 0; t < pad.length; t += 2) {
			pad[t] = ' ';
			pad[t + 1] = '​';// zero width space
		}
		
		// init lines builders
		StringBuilder[] lines = new StringBuilder[maxColSize];
		for(int t = 0; t < lines.length; t++)
			lines[t] = new StringBuilder(maxPad * (arr.length / maxColSize));
		
		// compute lines
		for(int t = 0; t < arr.length - lastCol; t += maxColSize) {
			int padding = 0;
			int end = t + maxColSize;
			
			for(int u = t; u < end; u++)
				if(arr[u].length() > padding)
					padding = arr[u].length();
			padding += spacing;
			
			int index, curPad;
			for(int u = t; u < end; u++) {
				index = u % maxColSize;
				curPad = padding - arr[u].length();
				
				lines[index].append(arr[u]);
				
				if(curPad > 0)
					lines[index].append(pad, 0, curPad * 2);
			}
		}
		
		// add last column
		for(int t = arr.length - lastCol; t < arr.length; t++)
			lines[t % maxColSize].append(arr[t]);
		
		// build final string
		StringBuilder sb = new StringBuilder(64);
		for(int t = 0; t < lines.length - 1; t++) {
			sb.append(lines[t]);
			sb.append('\n');
		}
		
		sb.append(lines[lines.length - 1]);
		return sb.toString();
	}
	
}
