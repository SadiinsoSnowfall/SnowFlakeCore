package net.shadowpie.sadiinso.snowflakecore.commands.handlers;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import net.shadowpie.sadiinso.snowflakecore.commands.Commands;
import net.shadowpie.sadiinso.snowflakecore.commands.context.CommandContext;
import net.shadowpie.sadiinso.snowflakecore.commands.declaration.ASFCommand;
import net.shadowpie.sadiinso.snowflakecore.permissions.OriginPerms;

public class ASFCommandLambdaHandler extends AbstractCommandHandler {

	@FunctionalInterface
	private interface CommandCallSite {
		public void execute(CommandContext ctx);
	}
	
	public static ASFCommandLambdaHandler createHandler(ASFCommand infos, Method m, String[] perms) throws Throwable {
		Lookup lookup = MethodHandles.lookup();
		MethodHandle mh = lookup.unreflect(m);
		CommandCallSite command = (CommandCallSite) LambdaMetafactory.metafactory(lookup, "execute", MethodType.methodType(CommandCallSite.class), mh.type(), mh, mh.type()).getTarget().invokeExact();
		return new ASFCommandLambdaHandler(infos, command, perms);
	}
	
	private final CommandCallSite command;
	
	public ASFCommandLambdaHandler(ASFCommand infos, CommandCallSite command, String[] perms) {
		super(infos.name(), infos.alias(), infos.usage(), infos.description(), OriginPerms.compute(infos.allowFrom()), perms);
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
