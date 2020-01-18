package net.shadowpie.sadiinso.sfc.commands.declaration;

import net.shadowpie.sadiinso.sfc.permissions.OriginPerms;
import org.apache.commons.lang3.StringUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SFCommand {
	
	/**
	 * The name of the command
	 * <br>
	 * SFC ignore the command case
	 * <br>
	 * A command name should contains only lowercases latin letters
	 */
	String name();
	
	/**
	 * The aliases of the command (séparated by commas)
	 * <br>
	 * SFC ignore the command case
	 * <br>
	 * A command name should contain only lowercases latin letters
	 */
	String alias() default StringUtils.EMPTY;
	
	/**
	 * Apply a filter to the command origin consisting of the followings strings :
	 * <ul>
	 * 		<li>private   : allow the command to be executed in private channels</li>
	 * 		<li>server    : allow the command to be executed in guild channels</li>
	 * 		<li>console   : allow the command to be executed in the console</li>
	 * 		<li>all       : allow the command to be executed everywhere</li>
	 * 		<li>ownerOnly : allow the command to be executed by the bot owner only</li>
	 * </ul>
	 * Set to "private/server" by default
	 */
	byte allowFrom() default OriginPerms.PRIVATE | OriginPerms.SERVER;
	
	/**
	 * Set the description of the command (display in the "help" base command)
	 */
	String description() default StringUtils.EMPTY;
	
	/**
	 * Set the usage hint of the command
	 * <br>
	 * exemple : <strong>&lt;required_arg&gt; [optional_arg]</strong>
	 */
	String usage() default StringUtils.EMPTY;
	
	/**
	 * Set the parent group of the command
	 */
	String parentGroup() default StringUtils.EMPTY;
	
	/**
	 * Set the permissions required in order to run this command (separated by commas)
	 * <br>
	 * A permission name should contain only lowercases latin letters.
	 * <br>
	 * Permissions paths are to be separated by dots 
	 * <br>
	 * exemple : <strong>permgroup.perm</strong>
	 */
	String permissions() default StringUtils.EMPTY;
}
