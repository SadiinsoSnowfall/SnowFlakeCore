package net.shadowpie.sadiinso.sfc.commands.handlers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;

import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.utils.JDALogger;
import net.shadowpie.sadiinso.sfc.commands.Commands;
import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;
import net.shadowpie.sadiinso.sfc.commands.declaration.ASFCommand;
import net.shadowpie.sadiinso.sfc.permissions.OriginPerms;
import net.shadowpie.sadiinso.sfc.sfc.SFC;

/**
 * Legacy command handler
 */
public class ASFCommandHandler extends AbstractCommandHandler {

	private static final Logger logger = JDALogger.getLog("CommandHandler");
	
	private final Method method;
	private final Object invokeTarget; // equals null for static commands
	private final Class<?>[] types;
	private final Object[] params; // cache parameters array
	
	public ASFCommandHandler(ASFCommand infos, Method command, Object target, String[] perms) {
		super(infos.name(), infos.alias(), infos.usage(), infos.description(), OriginPerms.compute(infos.allowFrom()), perms);
		
		method = command;
		invokeTarget = target;
		types = method.getParameterTypes();
		params = new Object[types.length];
	}

	@Override
	public int execute(CommandContext ctx) {
		/* build parameters array */
		for(int t = 0; t < params.length; t++) {
			if(types[t] == CommandContext.class)		params[t] = ctx;
			else if(types[t] == String.class) 			params[t] = ctx.rawMessage();
			else if(types[t] == String[].class) 		params[t] = ctx.args(); 
			else if(types[t] == long.class)				params[t] = ctx.getMessage().getAuthor().getIdLong();
			else if(types[t] == MessageChannel.class)	params[t] = ctx.getMessage().getChannel();
			else if(types[t] == Guild.class)			params[t] = ctx.getMessage().getGuild();
			else if(types[t] == JDA.class) 				params[t] = SFC.getJDA();
			else if(types[t] == Message.class)			params[t] = ctx.getMessage();
		}
		
		/* invoke method with parameters */
		try {
			method.invoke(invokeTarget, params);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.error("Error while executing command \"" + super.name + "\"", e);
			return Commands.COMMAND_ERROR;
		}
		
		return Commands.COMMAND_SUCCESS;
	}

}
