package net.shadowpie.sadiinso.snowflakecore.webapi.base;

import org.json.JSONArray;
import org.json.JSONObject;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;
import net.shadowpie.sadiinso.snowflakecore.config.ConfigHandler;
import net.shadowpie.sadiinso.snowflakecore.snowflakecore.SFC;
import net.shadowpie.sadiinso.snowflakecore.utils.JdaUtils;
import net.shadowpie.sadiinso.snowflakecore.webapi.ASFWebEndpoint;
import net.shadowpie.sadiinso.snowflakecore.webapi.WebEndpoints;

public class BaseEndpoints {
	
	/**
	 * API endpoint to send a private message to a user
	 * @param data The json-encoded parameter {"id": "<user_id>", "msg": "<message_to_send>"}
	 * @return "user_not_found" if the user is not visible by the bot,
	 * 			"error" if the bot can't send a message to the user,
	 * 			"success" if everything went fine
	 */
	@ASFWebEndpoint(cmd = "sfc-pmsg")
	public static JSONObject onSFCPrivateMessage(JSONObject data) {
		User user = SFC.getJDA().retrieveUserById(data.getString("id")).complete();
		
		if(user == null)
			return WebEndpoints.simpleReply("user_not_found");
		
		try {
			JdaUtils.sendPrivate(user, data.getString("msg"));
		} catch(Exception e) {
			return WebEndpoints.simpleReply("error");
		}
		
		return WebEndpoints.simpleReply("success");
	}
	
	/**
	 * API endpoint to check if an user is visible by the bot
	 * @param data The json-encoded parameter {"id": "<user_id>"}
	 * @return "true" if the user is visible, else "false"
	 */
	@ASFWebEndpoint(cmd = "sfc-user-visible")
	public static JSONObject onSFCUserVisible(JSONObject data) {
		User user = SFC.getJDA().retrieveUserById(data.getString("id")).complete();
		
		if(user == null)
			return WebEndpoints.simpleReply("false");
		else
			return WebEndpoints.simpleReply("true");
	}
	
	/**
	 * Return the list of the discord serves owned by the given user
	 * @param data The json-encoded parameter {"id": "<user_id>"}
	 */
	@ASFWebEndpoint(cmd = "sfc-get-owned-guilds")
	public static JSONObject onGetOwnedServs(JSONObject data) {
		JSONObject reply = new JSONObject();
		JSONArray arr = new JSONArray();
		
		String userid = data.getString("id");
		
		// owner has access to all guilds
		if(userid.equals(ConfigHandler.owner_sid())) {
			for(Guild g : SFC.getJDA().getGuilds())
				arr.put(JdaUtils.guildToJSON(g));
			
		} else {
			JdaUtils.getUser(userid).getMutualGuilds()
									.stream()
									.filter(g -> g.getOwner().getUser().getId().equals(userid))
									.forEach(g -> arr.put(JdaUtils.guildToJSON(g)));
		}
		
		reply.put("res", arr);
		return reply;
	}
	
}
