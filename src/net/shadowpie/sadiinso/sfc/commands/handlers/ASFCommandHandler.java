package net.shadowpie.sadiinso.sfc.commands.handlers;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import net.shadowpie.sadiinso.sfc.commands.Commands;
import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;
import net.shadowpie.sadiinso.sfc.commands.declaration.SFCommand;
import net.shadowpie.sadiinso.sfc.permissions.OriginPerms;

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
	
	public ASFCommandHandler(SFCommand inf, CommandCallSite command, String[] perms) {
		super(inf.name(), inf.alias(), inf.usage(), inf.description(), OriginPerms.compute(inf.allowFrom()), perms);
		this.command = command;
	}
	
	@Override
	public int execute(CommandContext ctx) {
		try {
			command.execute(ctx);
		} catch(Exception e) {
			e.printStackTrace();
			return Commands.COMMAND_ERROR;
		}
		
		return Commands.COMMAND_SUCCESS;
	}

}
