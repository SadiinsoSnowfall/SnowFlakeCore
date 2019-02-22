package net.shadowpie.sadiinso.snowflakecore.config;

import java.awt.Color;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.json.ordered.JSONException;
import org.json.ordered.OrderedJSONObject;

public class ConfigHandler {

	private static OrderedJSONObject root;
	private static boolean needrw;
	private static String path;
	
	public static Config sfConfig;
	
	/*
	 * Frequently used entries
	 */
	private static String bot_token;
	private static String bot_tag;
	private static String bot_version;
	private static long owner_id_long;
	private static String owner_id;
	private static boolean use_mention;
	private static boolean enable_commands;
	private static Color color_theme;
	private static Color color_info;
	private static Color color_warn;
	private static Color color_error;
	
	public static String bot_token() {
		return bot_token;
	}
	
	public static String bot_tag() {
		return bot_tag;
	}
	
	public static String bot_version() {
		return bot_version;
	}
	
	/**
	 * Return the bot owner Discord ID as a long
	 */
	public static long owner_lid() {
		return owner_id_long;
	}
	
	/**
	 * Return the bot owner Discord ID as a String
	 */
	public static String owner_sid() {
		return owner_id;
	}
	
	public static boolean use_mention() {
		return use_mention;
	}
	
	public static boolean enable_commands() {
		return enable_commands;
	}
	
	public static Color color_theme() {
		return color_theme;
	}
	
	public static Color color_info() {
		return color_info;
	}
	
	public static Color color_warn() {
		return color_warn;
	}
	
	public static Color color_error() {
		return color_error;
	}
	
	/**
	 * Init the config handler and parse the configuration file
	 * @param configPath Path to the configuration file
	 * @return Whether the configuraion parsing was successfull
	 */
	public static boolean init(String configPath) {
		path = configPath;
		needrw = false;
		
		try {
			root = new OrderedJSONObject(FileUtils.readFileToString(new File(configPath), "utf-8"));
		} catch (Exception e) {
			root = new OrderedJSONObject();
			needrw = true;
		}
		
		sfConfig = queryConfig("SnowFlakeCore");
		sfConfig.setField("bot_token", "");
		sfConfig.setField("bot_tag", "");
		sfConfig.setField("bot_version", "1.0");
		sfConfig.setField("owner_id", "");
		sfConfig.setField("use_mention", true);
		sfConfig.setField("enable_commands", true);
		sfConfig.setField("color_theme", "#0bdf0b");
		sfConfig.setField("color_info", "#0bdf0b");
		sfConfig.setField("color_warn", "#ffb000");
		sfConfig.setField("color_error", "#e50000");
		
		// check if config is valid
		if(sfConfig.needrw)
			return false;
		
		// set global entries
		bot_token 		= sfConfig.getString("bot_token");
		bot_tag 		= sfConfig.getString("bot_tag");
		bot_version 	= sfConfig.getString("bot_version");
		owner_id_long 	= Long.parseLong(sfConfig.getString("owner_id"));
		owner_id		= sfConfig.getString("owner_id");
		use_mention 	= sfConfig.getBool("use_mention");
		enable_commands = sfConfig.getBool("enable_commands");
		color_theme 	= Color.decode(sfConfig.getString("color_theme"));
		color_info 		= Color.decode(sfConfig.getString("color_info"));
		color_warn 		= Color.decode(sfConfig.getString("color_warn"));
		color_error 	= Color.decode(sfConfig.getString("color_error"));
		
		assert(!bot_token.isEmpty());
		
		return true;
	}
	
	public static boolean rewrite() {
		try (FileWriter w = new FileWriter(new File(path))) {
			root.write(w, 4, 0);
		} catch (IOException e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Return true if the configuration file need to be rewrited
	 */
	public static boolean needRewrite() {
		return needrw;
	}
	
	/**
	 * Return the configuration handle for the specified label
	 */
	public static Config queryConfig(String label) {
		OrderedJSONObject config = null;
		
		try {
			config = root.getJSONObject(label);
		} catch (JSONException ignored) {}
		
		return new Config(label, config);
	}
	
	public static class Config {
		private final String label;
		private OrderedJSONObject json;
		private boolean needrw;
		
		private Config(String label, OrderedJSONObject json) {
			this.label = label;
			this.json = json;
			this.needrw = false;
		}
		
		public boolean exists() {
			return (json != null);
		}
		
		public String getLabel() {
			return label;
		}
		
		public boolean needRewrite() {
			return needrw;
		}
		
		private boolean setField(String name, Object defaultValue, Class<?> cast) {
			if(json == null) {
				json = new OrderedJSONObject();
				root.put(label, json);
			}
			
			Object var = null;
			
			try {
				var = json.get(name);
			} catch(JSONException e) {
				json.put(name, defaultValue);
				return (ConfigHandler.needrw = needrw = true);
			}
			
			if(!cast.isInstance(var)) {
				json.put(name, defaultValue);
				return (ConfigHandler.needrw = needrw = true);
			}
			
			return false;
		}
		
		public boolean setField(String name, String defaultValue) {
			return setField(name, defaultValue, defaultValue.getClass());
		}
		
		public boolean setField(String name, Boolean defaultValue) {
			return setField(name, defaultValue, defaultValue.getClass());
		}
		
		public boolean setField(String name, Integer defaultValue) {
			return setField(name, defaultValue, defaultValue.getClass());
		}
		
		public boolean setField(String name, Long defaultValue) {
			return setField(name, defaultValue, defaultValue.getClass());
		}
		
		public boolean setField(String name, Double defaultValue) {
			return setField(name, defaultValue, defaultValue.getClass());
		}
		
		public String getString(String key) {
			return json.getString(key);
		}
		
		public String getString(String key, String fallback) {
			try {
				return json.getString(key);
			} catch(JSONException e) {
				return fallback;
			}
		}
		
		public boolean getBool(String key) {
			return json.getBoolean(key);
		}
		
		public boolean getBool(String key, boolean fallback) {
			try {
				return json.getBoolean(key);
			} catch(JSONException e) {
				return fallback;
			}
		}
		
		public int getInt(String key) {
			return json.getInt(key);
		}
		
		public int getInt(String key, int fallback) {
			try {
				return json.getInt(key);
			} catch(JSONException e) {
				return fallback;
			}
		}
		
		public long getLong(String key) {
			return json.getLong(key);
		}
		
		public long getLong(String key, long fallback) {
			try {
				return json.getLong(key);
			} catch(JSONException e) {
				return fallback;
			}
		}
		
		public double getDouble(String key) {
			return json.getDouble(key);
		}
		
		public double getDouble(String key, double fallback) {
			try {
				return json.getDouble(key);
			} catch(JSONException e) {
				return fallback;
			}
		}
		
	}
	
}
