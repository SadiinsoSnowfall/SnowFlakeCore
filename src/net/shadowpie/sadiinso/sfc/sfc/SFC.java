package net.shadowpie.sadiinso.sfc.sfc;

import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.exceptions.AccountTypeException;
import net.dv8tion.jda.internal.utils.JDALogger;
import net.shadowpie.sadiinso.sfc.commands.Commands;
import net.shadowpie.sadiinso.sfc.commands.base.BaseCommands;
import net.shadowpie.sadiinso.sfc.commands.base.HelpCommand;
import net.shadowpie.sadiinso.sfc.commands.base.PermissionCommands;
import net.shadowpie.sadiinso.sfc.config.SFConfig;
import net.shadowpie.sadiinso.sfc.db.DB;
import net.shadowpie.sadiinso.sfc.listeners.ConsoleListener;
import net.shadowpie.sadiinso.sfc.listeners.SFCListener;
import net.shadowpie.sadiinso.sfc.permissions.Permissions;
import net.shadowpie.sadiinso.sfc.webapi.WebAPI;
import org.slf4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SFC {

	public static final int ALL_OK = 0;
	public static final int STOP_CONFIG_REWRITE = 1;
	public static final int STOP_MODULE_ERROR = 2;
	
	// Suppresses default constructor, ensuring non-instantiability.
	private SFC() {}

	private static final Logger logger = JDALogger.getLog("SFC");

	private static final AtomicBoolean stopping = new AtomicBoolean(false);
	private static List<Runnable> shutdownHooks;
	
	private static JDA jda;
	private static SFCListener listener;
	private static String selfMention;
	
	/**
	 * Initialize all the components of the SnowFlakeCore and JDA libraries
	 */
	public static void init() {
		init("config.json");
	}

	/**
	 * Initialize all the components of the SnowFlakeCore and dependencies
	 * libraries, permit to specify the configuration file
	 * 
	 * @param configFile Path to the configuration file of SnowFlakeCore
	 */
	public static void init(String configFile) {
		logger.info("Initialing SnowFlakeCore library...");

		//###########
		//INIT CONFIG
		//###########
		logger.info("Loading configuration file...");
		try {
			if(!SFConfig.init(configFile))
				logger.warn("Writing configuration file, the bot need to be restarted");
		} catch(Exception e) {
			logger.warn("The \"SnowFlakeCore\" entry of the configuration file seems imcomplete or corrupted", e);
		}
		
		//#######
		//INIT DB
		//#######
		switch(DB.init()) {
			case STOP_CONFIG_REWRITE:
				logger.warn("Writing database configuration entry, the bot need to be restarted");
				break;
				
			case STOP_MODULE_ERROR:
				logger.error("Unable to init the database module");
				System.exit(STOP_MODULE_ERROR);
				break;
		}		
		
		//#####################
		//CHECK CONFIG PRE INIT
		//#####################
		if(SFConfig.needRewrite()) {
			SFConfig.rewrite();
			System.exit(STOP_CONFIG_REWRITE);
		}	
		
		//################
		//INIT PERMISSIONS
		//################
		try {
			if(Permissions.init() == STOP_MODULE_ERROR) {
				logger.error("An error occured during the permission module initialization");
				System.exit(STOP_MODULE_ERROR);
			}
		} catch(Exception e) {
			logger.error("An error occured while initialing the permissions module", e);
			System.exit(STOP_MODULE_ERROR);
		}
		
		//########
		//INIT JDA
		//########
		logger.info("Initialing JDA library...");
		try {
			jda = new JDABuilder(AccountType.BOT).setToken(SFConfig.bot_token()).build();
			jda.awaitReady();
		} catch (AccountTypeException e) {
			logger.warn("The given token is a client token, trying to launch as a selfbot...");
			try {
				jda = new JDABuilder(AccountType.CLIENT).setToken(SFConfig.bot_token()).build();
				jda.awaitReady();
			} catch (LoginException e1) {
				logger.error("The bot token is invalid");
				System.exit(-1);
			} catch (InterruptedException e1) {
				logger.error("Unable to join the discord servers");
				System.exit(-1);
			}
		} catch (LoginException e) {
			logger.error("The bot token is invalid");
			System.exit(-1);
		} catch (InterruptedException e) {
			logger.error("Unable to join the discord servers");
			System.exit(-1);
		}
		
		//##############
		//INIT LISTENERS
		//##############
		listener = new SFCListener();
		jda.addEventListener(listener);
		selfMention = jda.getSelfUser().getAsMention();
		
		if(SFConfig.sfConfig.getBool("enable_console", true)) {
			ConsoleListener.setup();
		} else {
			logger.info("Console input disabled");
		}

		//#############
		//INIT COMMANDS
		//#############
		if (SFConfig.enable_commands()) {
			logger.info("Initialing commands...");
			Commands.addCommands(BaseCommands.class);
			Commands.addCommands(HelpCommand.class);
			Commands.addCommands(PermissionCommands.class);
			
			Commands.init();
			logger.info("Verifying commands...");
			Commands.checkCommands();
			logger.info("Loaded " + Commands.size() + " commands !");
		} else {
			logger.info("ENABLE_COMMANDS option set to FALSE, starting the bot with 0 commands");
		}

		//######################
		//CHECK CONFIG POST INIT
		//######################
		if(SFConfig.needRewrite()) {
			SFConfig.rewrite();
			System.exit(STOP_CONFIG_REWRITE);
		}
		
		//##################
		//INIT SOCKET SERVER
		//##################
		WebAPI.init();
		
		logger.info("SFC loaded successfully !");
	}

	/**
	 * Return the String used to mention the bot
	 */
	public static String selfMention() {
		return selfMention;
	}
	
	/**
	 * Return the JDA library handler
	 */
	public static JDA getJDA() {
		return jda;
	}

	public static SFCListener getListener() {
		return listener;
	}
	
	/**
	 * Return the bot user object
	 */
	public static User getSelfUser() {
		return jda.getSelfUser();
	}
	
	/**
	 * Return the bot user id
	 */
	public static String getSelfUserId() {
		return jda.getSelfUser().getId();
	}
	
	/**
	 * Return the bot user id
	 */
	public static long getSelfUserIdLong() {
		return jda.getSelfUser().getIdLong();
	}
	
	/**
	 * Add code to be executed when the bot shutdown
	 * @param hook The code to be executed
	 */
	public static void addShutdownHook(Runnable hook) {
		if(shutdownHooks == null) {
			shutdownHooks = new LinkedList<>();
		}
		
		shutdownHooks.add(hook);
	}
	
	/**
	 * Stop the bot
	 */
	public static void shutdown() {
		if(!stopping.compareAndSet(false, true)) {
			return;
		}
		
		// stop the bot in a new thread to prevent race conditions
		new Thread(() -> {
			if(shutdownHooks != null) {
				logger.info("Executing shutdown hooks...");
				for(Runnable hook : shutdownHooks) {
					hook.run();
				}
			}
			
			logger.info("Shutting down SFC...");
			WebAPI.shutdown();
			DB.shutdown();
			ConsoleListener.shutdown();
			jda.shutdownNow();
			System.exit(0);
		}, "STOP").start();
	}

}
