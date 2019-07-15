package net.shadowpie.sadiinso.sfc.commands;

import net.dv8tion.jda.core.utils.JDALogger;
import net.shadowpie.sadiinso.sfc.commands.context.CommandContext;
import net.shadowpie.sadiinso.sfc.commands.context.ContextOrigin;
import net.shadowpie.sadiinso.sfc.commands.declaration.SFCommand;
import net.shadowpie.sadiinso.sfc.commands.declaration.SFCommandHelper;
import net.shadowpie.sadiinso.sfc.commands.handlers.ASFCommandHandler;
import net.shadowpie.sadiinso.sfc.commands.handlers.AbstractCommandHandler;
import net.shadowpie.sadiinso.sfc.commands.handlers.GroupedCommandHandler;
import net.shadowpie.sadiinso.sfc.config.ASFConfig;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler.Config;
import net.shadowpie.sadiinso.sfc.permissions.OriginPerms;
import net.shadowpie.sadiinso.sfc.permissions.Permissions;
import net.shadowpie.sadiinso.sfc.utils.SFUtils;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public final class Commands {

	public static final int COMMAND_SUCCESS = 0;
	public static final int COMMAND_NOT_FOUND = 1;
	public static final int COMMAND_ERROR = 2;
	public static final int COMMAND_PERM_ERROR = 3;
	
	public static final String err_cmd_not_found = "Command not found";
	public static final String err_no_private = "This command cannot be executed in a private channel";
	public static final String err_no_server = "This command cannot be executed on a server";
	public static final String err_no_console = "This command cannot be executed on the console";
	public static final String err_no_perm = "You need to have permission \"%perm\" to execute this command";

	// Suppresses default constructor, ensuring non-instantiability.
	private Commands() {}

	private static final Map<String, AbstractCommandHandler> commands = new HashMap<>();
	private static final Map<String, AbstractCommandHandler> aliases = new HashMap<>();
	private static final Logger logger = JDALogger.getLog("Commands");

	private static int finalCommandsNumber = 0;

	private static Set<Class<?>> commandsToAdd = new HashSet<>();

	public static Map<String, AbstractCommandHandler> getMap() {
		return commands;
	}

	/**
	 * Return the size of the command map (ie the number of loaded commands).
	 */
	public static int size() {
		return finalCommandsNumber;
	}

	/**
	 * Execute a command pipeline
	 * <p>
	 * This method can return the following values :
	 * <ul>
	 * <li>{@link Commands#COMMAND_SUCCESS} if the command is executed
	 * successfully.</li>
	 * <li>{@link Commands#COMMAND_NOT_FOUND} if the specified command is not
	 * defined in the command map.</li>
	 * <li>{@link Commands#COMMAND_ERROR} if an error occurs during the command
	 * execution.</li>
	 * <li>{@link Commands#COMMAND_PERM_ERROR} if the user does not have the
	 * permissions required to execute the command.</li>
	 * </ul>
	 * 
	 * @param ctx The command context to execute
	 * @return The command end status
	 */
	public static int execute(CommandContext ctx) {
		int status;
		do {
			status = executeInternal(ctx);
			
			if(status != COMMAND_SUCCESS || ctx.hasFlag(CommandContext.FLAG_BREAK_PIPELINE)) {
				break;
			}
		} while (ctx.advancePipeline());
		
		return status;
	}
	
	/**
	 * Execute the given command
	 * @param ctx The command
	 * @return The command end status
	 */
	private static int executeInternal(CommandContext ctx) {
		String prefix = ctx.prefix().toLowerCase();
		AbstractCommandHandler handler = aliases.get(prefix);
		
		if (handler == null) {
			handler = commands.get(prefix);
		}

		if (handler == null) {
			return COMMAND_NOT_FOUND;
		}

		ContextOrigin origin = ctx.getOrigin();
		byte perms = handler.basePerms;

		// check base permissions
		if (OriginPerms.has(perms, OriginPerms.PERM_OWNER_ONLY) && (ctx.getAuthorIdLong() != ConfigHandler.owner_lid()) && (origin != ContextOrigin.CONSOLE)) {
			return COMMAND_NOT_FOUND;
		}

		// verify origin permissions
		if (origin == ContextOrigin.CONSOLE) { // emulated context (console)
			if (!OriginPerms.has(perms, OriginPerms.PERM_CONSOLE)) {
				ctx.warn(err_no_console);
				return COMMAND_PERM_ERROR;
			}
		} else if (origin == ContextOrigin.PRIVATE) { // pm context
			if (!OriginPerms.has(perms, OriginPerms.PERM_PRIVATE)) {
				ctx.warn(err_no_private);
				return COMMAND_PERM_ERROR;
			}
		} else if (origin == ContextOrigin.SERVER) { // server context
			if (!OriginPerms.has(perms, OriginPerms.PERM_SERVER)) {
				ctx.warn(err_no_server);
				return COMMAND_PERM_ERROR;
			}
		}

		int code = handler.execute(ctx.pullPrefix());
		if(code == COMMAND_ERROR) {
			ctx.notifyFailure();
		}
		
		return code;
	}

	/**
	 * Find a command by path
	 * 
	 * @param path The command path (exemple : "help.miaou")
	 * @return The command or null if not found
	 */
	public static AbstractCommandHandler findCommand(String path) {
		return findCommand(path.split("\\."));
	}

	/**
	 * Find a command by path
	 * 
	 * @param path The command path (exemple : {help, miaou})
	 * @return The command or null if not found
	 */
	public static AbstractCommandHandler findCommand(String[] path) {
		if (path.length == 0)
			return null;

		if (path.length == 1) {
			AbstractCommandHandler alias = aliases.get(path[0]);
			return ((alias == null) ? commands.get(path[0]) : alias);
		}

		AbstractCommandHandler handler = commands.get(path[0]);
		if (handler instanceof GroupedCommandHandler)
			return findCommand((GroupedCommandHandler) handler, path, 1);
		else
			return null;
	}

	private static AbstractCommandHandler findCommand(GroupedCommandHandler handler, String[] path, int cur) {
		if (cur >= path.length)
			return null;

		AbstractCommandHandler sub = handler.subCommands.get(path[cur]);
		if (sub == null)
			return null;

		if (cur == (path.length - 1))
			return sub;

		if (sub instanceof GroupedCommandHandler)
			return findCommand((GroupedCommandHandler) sub, path, cur + 1);
		else
			return null;
	}

	/**
	 * Find a CommandGroup by path
	 * 
	 * @param path The path to search
	 * @return The CommandGroup or null if not found
	 */
	private static GroupedCommandHandler findCommandGroup(String path) {
		AbstractCommandHandler handler = findCommand(path);

		if (handler instanceof GroupedCommandHandler)
			return (GroupedCommandHandler) handler;
		else
			return null;
	}
	
	/**
	 * Register a command group
	 * <br>
	 * This group will have the defaults origin permissions settings "private/server"
	 * 
	 * @param path        The group path
	 * @param description The group description
	 * @see #registerCommandGroup(String, String, String) registerCommandGroup(String path, String description, String allowFrom)
	 */
	@SuppressWarnings("unused")
	public static void registerCommandGroup(String path, String description) {
		registerCommandGroup(path, description, "private/server");
	}
	
	/**
	 * Register a command group
	 * 
	 * @param path        The group path
	 * @param description The group description
	 * @param allowFrom   The group origin permissions
	 */
	public static void registerCommandGroup(String path, String description, String allowFrom) {
		String[] split = SFUtils.splitLastIndexOf(path, ".", true);
		GroupedCommandHandler group = new GroupedCommandHandler(split[split.length - 1], description, allowFrom);

		if (split.length > 1) {
			GroupedCommandHandler parent = findCommandGroup(split[0]);

			if (parent == null) {
				logger.error("CommandGroup declaration error", new RuntimeException("Unknown parent group \"" + split[0] + "\""));
				return;
			}

			parent.addCommandHandler(group);
		} else {
			commands.put(split[0], group);
		}
	}

	/**
	 * Add all commands declared in the specified class in the internal command map.
	 * <p>
	 * Specified class can contains the specified commands declaration pattern :
	 * <ul>
	 * <li>Annotation declaration, using SFCommand annotation on static
	 * methods.</li>
	 * </ul>
	 * 
	 * @param clazz Class to scan for commands
	 */
	public static void addCommands(Class<?> clazz) {
		if (commandsToAdd == null) {
			logger.error("You can't add commands once the bot is started");
			return;
		}

		commandsToAdd.add(clazz);
	}

	/**
	 * Init the commands contained in the classes passed to
	 * {@link Commands#addCommands(Class)}
	 */
	public static void init() {
		for (Class<?> clazz : commandsToAdd)
			addCommandsInternal(clazz);

		commandsToAdd = null;
	}

	private static void addCommandsInternal(Class<?> clazz) {
		Method[] mhs = clazz.getDeclaredMethods();

		Arrays.stream(mhs).filter(m -> (m.isAnnotationPresent(ASFConfig.class) && Modifier.isStatic(m.getModifiers()))).forEach(m -> {
			String label = m.getAnnotation(ASFConfig.class).label();
			Config cfg = ConfigHandler.queryConfig(label);

			try {
				m.invoke(null, cfg);
			} catch (Exception e) {
				logger.error("Config helper execution at \"" + clazz.getSimpleName() + "\"", e);
				return;
			}

			if (cfg.needRewrite()) {
				logger.warn("The configuration \"" + label + "\" need to be reloaded, the bot will shutdown.");
			}
		});

		Arrays.stream(mhs).filter(m -> (m.isAnnotationPresent(SFCommandHelper.class) && Modifier.isStatic(m.getModifiers()))).forEach(m -> {
			try {
				m.invoke(null, (Object[]) null);
			} catch (Exception e) {
				logger.error("Command helper execution error at \"" + m.getName() + "@" + clazz.getName() + "\"", e);
			}
		});

		Arrays.stream(mhs).filter(m -> (m.isAnnotationPresent(SFCommand.class) && Modifier.isStatic(m.getModifiers())))
				.forEach(Commands::addSFCommandInternal);
	}

	/**
	 * Called by {@link Commands#addCommands(Class)}, defined here to increase code
	 * readability. Add a command to the internal command map.
	 * <p>
	 * If the command is static and only take a CommandContext as an argument, the
	 * command call will be optimized with a lambda call
	 * <p>
	 * 
	 * @param m method that has SFCommand annotation.
	 */
	private static void addSFCommandInternal(Method m) {
		SFCommand a = m.getAnnotation(SFCommand.class);
		AbstractCommandHandler handler = null;

		// parse and register permissions
		String[] perms = null;
		if (!a.permissions().isEmpty()) {
			perms = a.permissions().split(",\\s*");
			for (int t = 0; t < perms.length; t++) {
				switch (Permissions.register(perms[t])) {
					case 2:
						logger.error("Skipping command \"" + a.name() + "\" as an error occured during permissions registration");
						return;

					case 3:
						logger.error("Skipping command \"" + a.name() + "\" as it define malformed permissions paths");
						return;
				}
			}
		}

		// check if the command call can be optimized as a lambda call
		if ((m.getParameterCount() == 1) && (m.getParameterTypes()[0] == CommandContext.class)) {
			try {
				handler = ASFCommandHandler.createHandler(a, m, perms);
			} catch (Throwable t) {
				logger.error("Error while creating the handler for the command \"" + a.name() + "@" + m.getClass().getName() + "\"", t);
			}
		} else {
			// invalid command declaration
			logger.warn("Ignoring command \"" + a.name() + "@" + m.getClass().getName() + "\" because its declaration is not valid");
		}

		if (!a.parentGroup().isEmpty()) {
			GroupedCommandHandler group = findCommandGroup(a.parentGroup());

			if (group == null) {
				logger.error("Command declaration error", new RuntimeException("Unknown parent group \"" + a.parentGroup() + "\""));
				return;
			}

			group.addCommandHandler(handler);
		} else {
			commands.put(a.name().toLowerCase(), handler);
		}

		// add alias
		if (!a.alias().isEmpty()) {
			aliases.put(a.alias().toLowerCase(), handler);
		}

		++finalCommandsNumber;
	}

	/**
	 * Verify that all the commands are valid
	 */
	public static void checkCommands() {
		for (AbstractCommandHandler handler : commands.values())
			checkCommandInternal(handler);
	}

	/**
	 * Recursive function that check if a command is valid
	 * 
	 * @param handler the command to check
	 */
	private static void checkCommandInternal(AbstractCommandHandler handler) {
		if (handler instanceof GroupedCommandHandler) {
			for (AbstractCommandHandler subHandler : ((GroupedCommandHandler) handler).subCommands.values())
				checkCommandInternal(subHandler);
		} else {
			if (!OriginPerms.isAccessible(handler.basePerms))
				logger.warn("The command \"" + handler.name + "\" is not accessible, add more perms");
		}
	}

}
