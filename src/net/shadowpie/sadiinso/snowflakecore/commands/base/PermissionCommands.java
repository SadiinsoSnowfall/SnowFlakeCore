package net.shadowpie.sadiinso.snowflakecore.commands.base;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.shadowpie.sadiinso.snowflakecore.commands.Commands;
import net.shadowpie.sadiinso.snowflakecore.commands.context.CommandContext;
import net.shadowpie.sadiinso.snowflakecore.commands.declaration.ASFCommand;
import net.shadowpie.sadiinso.snowflakecore.commands.declaration.ASFCommandHelper;
import net.shadowpie.sadiinso.snowflakecore.config.ConfigHandler;
import net.shadowpie.sadiinso.snowflakecore.permissions.Permissions;
import net.shadowpie.sadiinso.snowflakecore.utils.JdaUtils;

public class PermissionCommands {

	@ASFCommandHelper()
	public static void init() {
		Commands.registerCommandGroup("perms", "Gestion des permissions", "server");
	}

	/**
	 * Helper function to reduce code length
	 * 
	 * @param ctx
	 * @return
	 */
	private static Object getTarget(CommandContext ctx, int paramNb) {
		Guild guild = ctx.getGuild();
		if (ctx.argc() < paramNb) {
			ctx.warn("Certains paramètres sont manquants");
			return null;
		}

		Object target = null;

		long uid = ctx.getAsLong(0, -1);
		if (uid == -1) { // check for name

			List<Member> tmp = guild.getMembersByEffectiveName(ctx.arg(0), false);
			if (tmp.size() > 1) {
				ctx.warn("L'identifiant \"" + ctx.arg(0) + "\" correspond à plusieurs utilisateurs");
				return null;
			}

			if (tmp.size() == 1) {
				target = tmp.get(0);
			} else { // check for role
				List<Role> tmp2 = guild.getRolesByName(ctx.arg(0), false);
				if (tmp2.size() > 1) {
					ctx.warn("L'identifiant \"" + ctx.arg(0) + "\" correspond à plusieurs rôles");
					return null;
				}

				if (tmp2.size() == 1) {
					target = tmp2.get(0);
				} else {
					ctx.warn("Impossible de trouver \"" + ctx.arg(0) + "\"");
					return null;
				}
			}

		} else { // check for id
			target = guild.getMemberById(uid);

			if (target == null) {
				target = guild.getRoleById(uid);

				if (target == null) {
					ctx.error("Identifiant invalide : \"" + ctx.arg(0) + "\"");
					return null;
				}
			}
		}

		return target;
	}

	@ASFCommand(name = "list", usage = "<user|role>", description = "Affiche la liste des permissions de l'utilisateur", allowFrom = "server", parentGroup = "perms")
	public static void onPermList(CommandContext ctx) {
		Object target = getTarget(ctx, 1);
		if (target == null)
			return;

		Guild guild = ctx.getGuild();

		EmbedBuilder builder = JdaUtils.getEmbedBuilder();
		StringBuilder sb = new StringBuilder(64);

		// construct feedback
		if (target instanceof Member) {
			Member member = (Member) target;
			builder.setTitle("Permissions de l'utilisateur \"" + member.getEffectiveName() + "\"");
			builder.setThumbnail(member.getUser().getAvatarUrl());
			builder.setFooter("User ID : " + member.getUser().getId(), null);

			if(member.getUser().getIdLong() == ConfigHandler.owner_lid()) {
				builder.addField(":cat::bread:", "", false);
			} else if (member.isOwner()) {
				builder.addField("Permissions spécifiques : ", "*", false);
			} else {
				Map<String, List<String>> perms = Permissions.getAll(guild.getIdLong(), member.getUser().getIdLong());
				List<String> usrPerms = perms.remove(Permissions.permsListUsrKey);

				// the permissions module ensure all list contains at least one element
				for (Entry<String, List<String>> entry : perms.entrySet()) {
					for (String str : entry.getValue()) {
						sb.append(str);
						sb.append('\n');
					}

					builder.addField("Héritées du rôle \"" + entry.getKey() + "\" :", sb.toString(), true);
					sb.setLength(0); // reset string builder
				}

				// append user-specific permissions
				if (usrPerms != null && usrPerms.size() > 0) {
					for (String str : usrPerms) {
						sb.append(str);
						sb.append('\n');
					}

					builder.addField("Permissions spécifiques : ", sb.toString(), false);
				}
			}

		} else { // assume role
			Role role = (Role) target;
			List<String> perms = Permissions.getAllFromRole(guild.getIdLong(), role.getIdLong());

			if (perms.size() > 0) {
				for (String str : perms) {
					sb.append(str);
					sb.append('\n');
				}
			} else {
				sb.append("Ce rôle n'octroie aucune permissions.");
			}

			builder.addField("Permissions du rôle \"" + role.getName() + "\"", sb.toString(), false);
			builder.setFooter("Role ID : " + role.getId(), null);
		}

		// send feedback
		ctx.reply(builder);
	}

	@ASFCommand(name = "grant", usage = "<user|role> <perm> [<perm2>...]", description = "Ajoute des permissions à l'utilisateur", allowFrom = "server", parentGroup = "perms", permissions = "perms.grant")
	public static void onPermGrant(CommandContext ctx) {
		Object target = getTarget(ctx, 2);
		if (target == null)
			return;

		int nb = 0, res;
		boolean isMember = (target instanceof Member);
		long gid = ctx.getGuild().getIdLong();
		long targetID = (isMember ? ((Member) target).getUser().getIdLong() : ((Role) target).getIdLong());

		for (int t = 1; t < ctx.argc(); t++) {
			res = (isMember ? Permissions.grant(gid, targetID, ctx.arg(t)) : Permissions.grantToRole(gid, targetID, ctx.arg(t)));
			switch (res) {
				case 0:
					++nb;
					break;
				case 2:
					ctx.error("Une erreur est survenue");
					return;
				case 3:
					ctx.error("\"" + ctx.arg(t) + "\" ne définit pas une permission");
					return;
				case 4:
					ctx.error("La permission \"" + ctx.arg(t) + "\" n'existe pas");
					return;
			}
		}

		if (nb > 1)
			ctx.info("Opération effectuée, " + nb + " permissions ont été mises à jour");
		else if (nb == 1)
			ctx.info("Opération effectuée, une permission mise à jour");
		else
			ctx.info("Operétion effectuée, aucune permission mise à jour");
	}

	@ASFCommand(name = "revoke", usage = "<user|role> <perm> [<perm2>...]", description = "Retire des permissions à l'utilisateur", allowFrom = "server", parentGroup = "perms", permissions = "perms.revoke")
	public static void onPermRemove(CommandContext ctx) {
		Object target = getTarget(ctx, 2);
		if (target == null)
			return;

		int nb = 0, res;
		boolean isMember = (target instanceof Member);
		long gid = ctx.getGuild().getIdLong();
		long targetID = (isMember ? ((Member) target).getUser().getIdLong() : ((Role) target).getIdLong());

		for (int t = 1; t < ctx.argc(); t++) {
			res = (isMember ? Permissions.revoke(gid, targetID, ctx.arg(t)) : Permissions.revokeFromRole(gid, targetID, ctx.arg(t)));
			switch (res) {
				case 0:
					++nb;
					break;
				case 2:
					ctx.error("Une erreur est survenue");
					return;
				case 3:
					ctx.error("\"" + ctx.arg(t) + "\" ne définit pas une permission");
					return;
				case 4:
					ctx.error("La permission \"" + ctx.arg(t) + "\" n'existe pas");
					return;
			}
		}

		if (nb > 1)
			ctx.info("Opération effectuée, " + nb + " permissions ont été mises à jour");
		else if (nb == 1)
			ctx.info("Opération effectuée, une permission mise à jour");
		else
			ctx.info("Operétion effectuée, aucune permission mise à jour");
	}

	@ASFCommand(name = "test", usage = "<user|role> <perm>", description = "Vérifie si l'utilisateur dispose de la permission donnée", allowFrom = "server", parentGroup = "perms")
	public static void onPermTest(CommandContext ctx) {
		Object target = getTarget(ctx, 2);
		if (target == null)
			return;

		Guild guild = ctx.getGuild();
		String perm = ctx.arg(1);

		if (target instanceof Member) {
			Member member = (Member) target;
			boolean has = Permissions.hasPerm(guild.getIdLong(), member.getUser().getIdLong(), perm);

			if (has)
				ctx.info("L'utilisateur \"" + member.getEffectiveName() + "\" dispose de la permission \"" + perm + "\"");
			else
				ctx.info("L'utilisateur \"" + member.getEffectiveName() + "\" ne dispose pas de la permission \"" + perm + "\"");

		} else { // assume role
			Role role = (Role) target;
			boolean has = Permissions.hasRolePerm(guild.getIdLong(), role.getIdLong(), perm);

			if (has)
				ctx.info("Le role \"" + role.getName() + "\" octroie la permission \"" + perm + "\"");
			else
				ctx.info("Le role \"" + role.getName() + "\" n'octroie pas la permission \"" + perm + "\"");
		}
	}

	@ASFCommand(name = "yolo", allowFrom = "server", permissions = "yolo")
	public static void yolo(CommandContext ctx) {
		ctx.reply("ok");
	}

}
