package net.shadowpie.sadiinso.sfc.permissions;

import net.dv8tion.jda.internal.utils.JDALogger;
import net.shadowpie.sadiinso.sfc.commands.context.ContextOrigin;
import org.slf4j.Logger;

public final class OriginPerms {
	
	private OriginPerms() {}
	
	private static final Logger logger = JDALogger.getLog("Command");
	
	public static final byte CONSOLE = 1;
	public static final byte PRIVATE = 2;
	public static final byte SERVER  = 4;
	public static final byte OWNER_ONLY = 8;
	public static final byte ALL = CONSOLE | PRIVATE | SERVER;
	
	/**
	 * ContextOrigin to OriginPermission
	 * @return
	 */
	public static byte CO2OP(ContextOrigin co) {
		switch(co) {
			case CONSOLE:
				return CONSOLE;
			
			case PRIVATE:
				return PRIVATE;
				
			case SERVER:
				return SERVER;
				
			default: // unknown perm
				return 0;
		}
	}
	
	/**
	 * Return true if the command is accessible by at least one of the ContextOrigin
	 * @param perms The command permissions container
	 */
	public static boolean isAccessible(byte perms) {
		return (((perms & CONSOLE) > 0) || ((perms & PRIVATE) > 0) || ((perms & SERVER) > 0));
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
