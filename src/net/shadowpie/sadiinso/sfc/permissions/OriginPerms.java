package net.shadowpie.sadiinso.sfc.permissions;

import net.dv8tion.jda.core.utils.JDALogger;
import net.shadowpie.sadiinso.sfc.commands.context.ContextOrigin;
import org.slf4j.Logger;

public final class OriginPerms {
	
	private OriginPerms() {}
	
	private static final Logger logger = JDALogger.getLog("Command");
	
	public static final byte PERM_CONSOLE = 1 << 0;
	public static final byte PERM_PRIVATE = 1 << 1;
	public static final byte PERM_SERVER  = 1 << 2;
	public static final byte PERM_OWNER_ONLY = 1 << 4;
	public static final byte PERM_ALL = PERM_CONSOLE | PERM_PRIVATE | PERM_SERVER;
	
	public static byte compute(String config) {
		String[] split = config.split("/");
		byte val = 0;
		
		for(String str : split) {
			if(str.isEmpty()) continue;
			str = str.toLowerCase();
			switch(str) {
				case "console":
					val |= PERM_CONSOLE;
					break;
					
				case "private":
					val |= PERM_PRIVATE;
					break;
				
				case "server":
					val |= PERM_SERVER;
					break;
				
				case "all":
					val |= PERM_ALL;
					break;
					
				case "owneronly":
					val |= PERM_OWNER_ONLY;
					break;
					
				default:
					logger.warn("Ignoring unknown permission property : \"" + str + "\"");
			}
		}
		
		return val;
	}
	
	/**
	 * ContextOrigin to OriginPermission
	 * @return
	 */
	public static byte CO2OP(ContextOrigin co) {
		switch(co) {
			case CONSOLE:
				return PERM_CONSOLE;
			
			case PRIVATE:
				return PERM_PRIVATE;
				
			case SERVER:
				return PERM_SERVER;
				
			default: // unknown perm
				return 0;
		}
	}
	
	/**
	 * Return true if the command is accessible by at least one of the ContextOrigin
	 * @param perms The command permissions container
	 */
	public static boolean isAccessible(byte perms) {
		return (((perms & PERM_CONSOLE) > 0) || ((perms & PERM_PRIVATE) > 0) || ((perms & PERM_SERVER) > 0));
	}
	
	/**
	 * Return true if the permission container contains the specified permission
	 * @param perms The permission container
	 * @param property The permission to test
	 */
	public static boolean has(byte perms, byte property) {
		return ((perms & property) > 0);
	}
	
	/**
	 * Return true if the permission container contains the specified permission
	 * @param perms The permission container
	 * @param oc The permission to test
	 */
	public static boolean has(byte perms, ContextOrigin oc) {
		return has(perms, CO2OP(oc));
	}
	
}
