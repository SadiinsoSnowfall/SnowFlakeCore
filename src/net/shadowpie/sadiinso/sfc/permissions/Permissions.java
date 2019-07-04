package net.shadowpie.sadiinso.sfc.permissions;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.utils.JDALogger;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler;
import net.shadowpie.sadiinso.sfc.db.DB;
import net.shadowpie.sadiinso.sfc.db.DBUtils;
import net.shadowpie.sadiinso.sfc.sfc.SFC;
import net.shadowpie.sadiinso.sfc.utils.JdaUtils;
import org.slf4j.Logger;

import java.sql.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Permissions {

	private static final Logger logger = JDALogger.getLog("Permissions");

	// only lowercase separated by dots or * (root perm)
	private static Pattern permPattern;

	public static final String permsListUsrKey = "#usr~";
	
	private static String remAllPerms;// remove all perms from user
	private static String remAllRolePerms;// remove all perms from role
	
	private static String getAllPermsName;// get all permissions names from user
	private static String getAllRolePermsName;// get all permissions names from role

	public static int init() {
		if (!DB.isInit()) {
			logger.warn("Database access is disabled, skipping permissions module");
			return SFC.ALL_OK;
		}
		
		permPattern = Pattern.compile("^(([a-z]+(\\.[a-z]+)*(\\.\\*)?)|\\*)$");

		remAllPerms = "DELETE FROM perms_users WHERE sid = ? AND uid = ?";
		remAllRolePerms = "DELETE FROM perms_roles WHERE sid = ? AND rid = ?";
		getAllPermsName = "SELECT name, isgroup FROM perms_def WHERE id IN (SELECT pid FROM perms_users WHERE sid = ? AND uid = ?) ORDER BY name";
		getAllRolePermsName = "SELECT name, isgroup FROM perms_def WHERE id IN (SELECT pid FROM perms_roles WHERE sid = ? AND rid = ?) ORDER BY name";
		
		// create table & procedure in DB
		/*String sql = Utils.getDataResourceAsString("db_init_permission.sql");
		if(sql == null) {
			logger.error("Unable to read the database configuration file for the permission module, skipping DB init. This may cause errors");
		} else {
			try (Connection conn = DB.getConn()) {	
				Statement stat = conn.createStatement();
				String[] updates = sql.split(";;");
				
				for(String update : updates)
					if(!update.isBlank())
						stat.executeUpdate(update);
				
			} catch (SQLException e) {
				logger.error("Error while initialing the database for the permission module", e);
				return SFC.STOP_MODULE_ERROR;
			}
		}*/
		
		// ensure the root perm is registered
		register("*");
		
		return SFC.ALL_OK;
	}

	/**
	 * Return whether or not the given string represent a permission path
	 * <p>
	 * A permission path is constitued exclusively of lowercase letters groups
	 * separated by dots and can end with ".*"
	 * </p>
	 */
	public static boolean isPermPath(String perm) {
		return permPattern.matcher(perm).find();
	}

	/**
	 * Return the master path of the given permission path
	 */
	private static String extractMasterPath(String perm) {
		return (perm.endsWith(".*") ? perm.substring(0, perm.length() - 2) : perm);
	}

	/**
	 * Grant the given permission to the given user
	 * 
	 * @param serverid
	 * @param userid
	 * @param perm
	 * @return
	 *         <ul>
	 *         <li>0 if everything went "ok"</li>
	 *         <li>1 if the user already have the perm</li>
	 *         <li>2 if an error occured</li>
	 *         <li>3 if the given string is not a permission path</li>
	 *         <li>4 if the permission do not exists</li>
	 *         <li>5 if the given user do not exists</li>
	 *         </ul>
	 */
	public static int grant(long serverid, long userid, String perm) {
		if(perm.equals("*")) {
			// first revoke all perms then only add the root perm
			revokeAll(serverid, userid);
		} else if (!isPermPath(perm)) {
			return 3;
		}

		// owner already have all the perms
		if (ConfigHandler.owner_lid() == userid) {
			return 1;
		}

		Member member = JdaUtils.getMember(serverid, userid);
		if(member == null) {
			return 5;
		}
		
		// server owner already have all the perms
		if (member.isOwner()) {
			return 1;
		}

		int res;
		try (Connection conn = DB.getConn()) {
			CallableStatement call = conn.prepareCall("{CALL grantPermToUser(?, ?, ?, ?)}");
			call.setLong(1, serverid);
			call.setLong(2, userid);
			call.setString(3, extractMasterPath(perm));
			call.registerOutParameter(4, Types.INTEGER);
			call.execute();
			res = call.getInt(4);
		} catch (Exception e) {
			logger.error("Error while setting permissions for user " + userid + "@" + serverid, e);
			res = 2;
		}
		
		return res;
	}

	/**
	 * Grant the given permission to the given role
	 * 
	 * @param serverid The server in which the role is
	 * @param roleid   The role
	 * @param perm     The permission path
	 * @return
	 *         <ul>
	 *         <li>0 if everything went "ok"</li>
	 *         <li>1 if the role already have the perm</li>
	 *         <li>2 if an error occured</li>
	 *         <li>3 if the given string is not a permission path</li>
	 *         <li>4 if the permission do not exists</li>
	 *         </ul>
	 */
	public static int grantToRole(long serverid, long roleid, String perm) {
		if(perm.equals("*")) {
			// first revoke all perms then only add the root perm
			revokeAllFromRole(serverid, roleid);
		} else if(!isPermPath(perm)) {
			return 3;
		}

		int res;
		try (Connection conn = DB.getConn()) {
			CallableStatement call = conn.prepareCall("{CALL grantPermToRole(?, ?, ?, ?)}");
			call.setLong(1, serverid);
			call.setLong(2, roleid);
			call.setString(3, extractMasterPath(perm));
			call.registerOutParameter(4, Types.INTEGER);
			call.execute();
			res = call.getInt(4);
		} catch (Exception e) {
			logger.error("Error while setting permissions for role " + roleid + "@" + serverid, e);
			res = 2;
		}
		
		return res;
	}

	/**
	 * Remove the given perm from the given user
	 * 
	 * @param serverid The server in which the user is
	 * @param userid   The user
	 * @param perm     The permission path
	 * @return
	 *         <ul>
	 *         <li>0 if everything went "ok"</li>
	 *         <li>1 if the user don't have the permission</li>
	 *         <li>2 if an error occured</li>
	 *         <li>3 if the given string is not a permission path</li>
	 *         <li>4 if the permission do not exists</li>
	 *         </ul>
	 */
	public static int revoke(long serverid, long userid, String perm) {
		if(perm.equals("*")) {
			return revokeAll(serverid, userid);
		}
		
		if (!isPermPath(perm)) {
			return 3;
		}

		// bot owner have all the perms
		if (ConfigHandler.owner_lid() == userid) {
			return 0;
		}

		// guild owners have all the perms on their servers
		Member member = JdaUtils.getMember(serverid, userid);
		if ((member != null) && member.isOwner()) {
			return 0;
		}

		int res;
		try (Connection conn = DB.getConn()) {
			CallableStatement call = conn.prepareCall("{CALL revokePermFromUser(?, ?, ?, ?)}");
			call.setLong(1, serverid);
			call.setLong(2, userid);
			call.setString(3, extractMasterPath(perm));
			call.registerOutParameter(4, Types.INTEGER);
			call.execute();
			res = call.getInt(4);
		} catch (Exception e) {
			logger.error("Error while revoking permissions from user " + userid + "@" + serverid, e);
			res = 2;
		}
		
		return res;
	}

	/**
	 * Remove the given perm from the given role
	 * 
	 * @param serverid The server in which the role is
	 * @param roleid   The role
	 * @param perm     The permission path
	 * @return
	 *         <ul>
	 *         <li>0 if everything went "ok"</li>
	 *         <li>2 if an error occured</li>
	 *         <li>3 if the given string is not a permission path</li>
	 *         <li>4 if the permission do not exists</li>
	 *         </ul>
	 */
	public static int revokeFromRole(long serverid, long roleid, String perm) {
		if(perm.equals("*")) {
			return revokeAllFromRole(serverid, roleid);
		}
		
		if (!isPermPath(perm)) {
			return 3;
		}

		int res;
		try (Connection conn = DB.getConn()) {
			CallableStatement call = conn.prepareCall("{CALL revokePermFromRole(?, ?, ?, ?)}");
			call.setLong(1, serverid);
			call.setLong(2, roleid);
			call.setString(3, extractMasterPath(perm));
			call.registerOutParameter(4, Types.INTEGER);
			call.execute();
			res = call.getInt(4);
		} catch (Exception e) {
			logger.error("Error while revoking permissions from user " + roleid + "@" + serverid, e);
			res = 2;
		}
		
		return res;
	}

	/**
	 * Register a new permission
	 * 
	 * @param perm The permission
	 * @throws SQLException
	 * @return
	 *         <ul>
	 *         <li>0 if everything went "ok"</li>
	 *         <li>2 if an error occured</li>
	 *         <li>3 if the given string is not a permission path</li>
	 *         </ul>
	 */
	public static int register(String perm) {
		if(!DB.isInit()) {
			return 0;
		}
		
		if (!isPermPath(perm)) {
			return 3;
		}
		
		try (Connection conn = DB.getConn()) {
			CallableStatement call = conn.prepareCall("{CALL registerPerm(?)}");
			call.setString(1, extractMasterPath(perm));
			call.execute();
		} catch (Exception e) {
			logger.error("Error while registering permission \"" + perm + "\"", e);
			return 2;
		}
		
		return 0;
	}
	
	/**
	 * Check if the given user have the given permission
	 * 
	 * @param serverid The server in which the user is located
	 * @param userid The user
	 * @param perm The permission path
	 * @return
	 */
	public static boolean hasPerm(long serverid, long userid, String perm) {
		// bot owner have all the perms
		if (ConfigHandler.owner_lid() == userid)
			return true;

		// guild owners have all the perms on their servers
		Member member = JdaUtils.getMember(serverid, userid);
		if ((member != null) && member.isOwner()) {
			return true;
		}
		
		if (!isPermPath(perm)) {
			return false;
		}
		
		boolean res;
		try (Connection conn = DB.getConn()) {
			CallableStatement call = conn.prepareCall("{CALL testFullUserPerm(?, ?, ?, ?, ?)}");
			call.setLong(1, serverid);
			call.setLong(2, userid);
			call.setString(3, DBUtils.toSQLSet(JdaUtils.getAllRoleIdLong(member)));
			call.setString(4, extractMasterPath(perm));
			call.registerOutParameter(5, Types.BOOLEAN);
			call.execute();
			res = call.getBoolean(5);
		} catch(Exception e) {
			logger.error("Error while retrieving permissions for user " + userid + "@" + serverid, e);
			res = false;
		}
		
		return res;
	}

	/**
	 * Check if the given role have the given permission
	 * 
	 * @param serverid The server in which the role is located
	 * @param roleid The role
	 * @param perm The permission path
	 */
	public static boolean hasRolePerm(long serverid, long roleid, String perm) {
		if (!isPermPath(perm))
			return false;

		boolean res;
		try (Connection conn = DB.getConn()) {
			CallableStatement call = conn.prepareCall("{CALL testRolePerm(?, ?, ?, ?)}");
			call.setLong(1, serverid);
			call.setLong(2, roleid);
			call.setString(3, perm);
			call.registerOutParameter(4, Types.BOOLEAN);
			call.execute();
			res = call.getBoolean(4);
		} catch(Exception e) {
			logger.error("Error while retrieving permissions for user " + roleid + "@" + serverid, e);
			res = false;
		}
		
		return res;
	}

	/**
	 * Remove all the permissions of the given user
	 * 
	 * @param serverid The server in which the user is
	 * @param userid   The user
	 * @return <ul>
	 *         <li>0 if everything went "ok"</li>
	 *         <li>2 if an error occured</li>
	 *         </ul>
	 */
	public static int revokeAll(long serverid, long userid) {
		// bot owner have all the perms
		if (ConfigHandler.owner_lid() == userid)
			return 0;

		// guild owners have all the perms on their servers
		Member member = JdaUtils.getMember(serverid, userid);
		if ((member != null) && member.isOwner())
			return 0;
		
		try (Connection conn = DB.getConn()) {
			PreparedStatement stat = conn.prepareStatement(remAllPerms);
			stat.setLong(1, serverid);
			stat.setLong(2, userid);
			stat.executeUpdate();
		} catch (Exception e) {
			logger.error("Error while removing permissions from user " + userid + "@" + serverid, e);
			return 2;
		}
		
		return 0;
	}
	
	/**
	 * Remove all the permissions of the given role
	 * 
	 * @param serverid The server in which the role is
	 * @param roleid   The role
	 * @return <ul>
	 *         <li>0 if everything went "ok"</li>
	 *         <li>2 if an error occured</li>
	 *         </ul>
	 */
	public static int revokeAllFromRole(long serverid, long roleid) {
		try (Connection conn = DB.getConn()) {
			PreparedStatement stat = conn.prepareStatement(remAllRolePerms);
			stat.setLong(1, serverid);
			stat.setLong(2, roleid);
			stat.executeUpdate();
		} catch (Exception e) {
			logger.error("Error while removing permissions from role " + roleid + "@" + serverid, e);
			return 2;
		}
		
		return 0;
	}
	
	/**
	 * Return all permissions the given user have access to
	 * @param serverid The server
	 * @param userid The user
	 * @return a HashMap object containing representing the perms granted to each roles (use {@link #permsListUsrKey} key to get user perms) or null if an error occured
	 */
	public static Map<String, List<String>> getAll(long serverid, long userid) {		
		Member member = JdaUtils.getMember(serverid, userid);
		if(member == null)
			return null;
		
		Map<String, List<String>> perms = new HashMap<>();
		
		try (Connection conn = DB.getConn()) {
			PreparedStatement stat = conn.prepareStatement(getAllRolePermsName);
			stat.setLong(1, serverid);
			
			for(Role role : member.getRoles()) {
				stat.setLong(2, role.getIdLong());
				stat.execute();
				
				ResultSet res = stat.getResultSet();
				List<String> list = new LinkedList<>();
				
				while(res.next())
					list.add(res.getBoolean(2) ? res.getString(1) + ".*" : res.getString(1));
				
				if(!list.isEmpty())
					perms.put(role.getName(), list);
			}
			
			stat = conn.prepareStatement(getAllPermsName);
			stat.setLong(1, serverid);
			stat.setLong(2, userid);
			stat.execute();
			
			ResultSet res = stat.getResultSet();
			List<String> list = new LinkedList<>();
			
			while(res.next())
				list.add(res.getBoolean(2) ? res.getString(1) + ".*" : res.getString(1));
			
			if(!list.isEmpty())
				perms.put(permsListUsrKey, list);
			
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return perms;
	}
	
	/**
	 * Return all the permissions granted by the given role
	 * @param serverid The server
	 * @param roleid The role
	 * @return A list of all the perms or null if an error occured
	 */
	public static List<String> getAllFromRole(long serverid, long roleid) {	
		Role role = JdaUtils.getRole(serverid, roleid);
		if(role == null)
			return null;
		
		List<String> perms = new LinkedList<>();
		
		try (Connection conn = DB.getConn()) {
			PreparedStatement stat = conn.prepareStatement(getAllRolePermsName);
			stat.setLong(1, serverid);
			stat.setLong(2, roleid);
			stat.execute();
			ResultSet res = stat.getResultSet();
			
			while(res.next())
				perms.add(res.getBoolean(2) ? res.getString(1) + ".*" : res.getString(1));
			
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
		return perms;
	}

}
