package net.shadowpie.sadiinso.sfc.commands.handlers;

import net.shadowpie.sadiinso.sfc.commands.Commands;
import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;
import net.shadowpie.sadiinso.sfc.commands.context.ContextOrigin;
import net.shadowpie.sadiinso.sfc.commands.declaration.SFCommand;
import net.shadowpie.sadiinso.sfc.config.SFConfig;
import net.shadowpie.sadiinso.sfc.permissions.Permissions;
import net.shadowpie.sadiinso.sfc.sfc.SFC;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

public class ASFCommandHandler extends AbstractCommandHandler {

	@FunctionalInterface
	private interface CommandCallSite {
		void execute(CommandContext ctx);
	}
	
	public static ASFCommandHandler createHandler(SFCommand inf, Method m, String[] perms) throws Throwable {
		Lookup lookup = MethodHandles.lookup();
		MethodHandle mh = lookup.unreflect(m);
		CommandCallSite command = (CommandCallSite) LambdaMetafactory.metafactory(lookup, "execute", MethodType.methodType(CommandCallSite.class), mh.type(), mh, mh.type()).getTarget().invokeExact();
		return new ASFCommandHandler(inf, command, perms);
	}
	
	private final CommandCallSite command;
	
	public ASFCommandHandler(@NotNull SFCommand inf, CommandCallSite command, String[] perms) {
		super(inf.name(), inf.alias(), inf.usage(), inf.description(), inf.allowFrom(), perms);
		this.command = command;
	}
	
	@Override
	public int execute(@NotNull CommandContext ctx) {
		// verify permissions
		long uid = ctx.getAuthorIdLong();
		long ownerid = SFConfig.owner_lid();
		long selfid = SFC.getSelfUserIdLong();
		boolean isAdmin = ((uid == ownerid) || (uid == selfid));
		
		if ((ctx.getOrigin() == ContextOrigin.SERVER) && (perms != null) && !isAdmin) {
			for (int t = 0; t < perms.length; t++) {
				if (!Permissions.hasPerm(ctx.getGuild().getIdLong(), ctx.getAuthorIdLong(), perms[t])) {
					ctx.warn(Commands.err_no_perm.replace("%perm", perms[t]));
					return Commands.COMMAND_PERM_ERROR;
				}
			}
		}
		
		try {
			command.execute(ctx);
		} catch(Exception e) {
			e.printStackTrace();
			return Commands.COMMAND_ERROR;
		}
		
		return Commands.COMMAND_SUCCESS;
	}

}
