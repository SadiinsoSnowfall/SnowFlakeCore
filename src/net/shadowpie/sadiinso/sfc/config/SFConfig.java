package net.shadowpie.sadiinso.sfc.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.shadowpie.sadiinso.sfc.utils.SFUtils;
import org.apache.commons.io.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SFConfig {

	private static ObjectNode root;
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
			String json = FileUtils.readFileToString(new File(configPath), "utf-8");
			root = (ObjectNode) SFUtils.parseJSON(json);
		} catch (Exception e) {
			root = SFUtils.mapper.createObjectNode();
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
		if(sfConfig.needrw) {
			return false;
		}
		
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
			SFUtils.IndentedJSONWriter.writeValue(w, root);
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
		return new Config(label, (ObjectNode) root.get(label));
	}
	
	public static class Config {
		private final String label;
		private ObjectNode json;
		private boolean needrw;
		
		private Config(String label, ObjectNode json) {
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
		
		private void createIfNeeded(String name) {
			if(json == null) {
				json = SFUtils.mapper.createObjectNode();
				root.set(label, json);
			}
		}
		
		public boolean setField(String name, String defaultValue) {
			createIfNeeded(name);
			JsonNode node = json.get(name);
			
			if ((node == null) || !node.isTextual()) {
				json.put(name, defaultValue);
				return (SFConfig.needrw = needrw = true);
			} else {
				return true;
			}
		}
		
		public boolean setField(String name, Boolean defaultValue) {
			createIfNeeded(name);
			JsonNode node = json.get(name);
			
			if ((node == null) || !node.isBoolean()) {
				json.put(name, defaultValue);
				return (SFConfig.needrw = needrw = true);
			} else {
				return true;
			}
		}
		
		public boolean setField(String name, Integer defaultValue) {
			createIfNeeded(name);
			JsonNode node = json.get(name);
			
			if ((node == null) || !node.isInt()) {
				json.put(name, defaultValue);
				return (SFConfig.needrw = needrw = true);
			} else {
				return true;
			}
		}
		
		public boolean setField(String name, Long defaultValue) {
			createIfNeeded(name);
			JsonNode node = json.get(name);
			
			if ((node == null) || !node.isLong()) {
				json.put(name, defaultValue);
				return (SFConfig.needrw = needrw = true);
			} else {
				return true;
			}
		}
		
		public boolean setField(String name, Double defaultValue) {
			createIfNeeded(name);
			JsonNode node = json.get(name);
			
			if ((node == null) || !node.isDouble()) {
				json.put(name, defaultValue);
				return (SFConfig.needrw = needrw = true);
			} else {
				return true;
			}
		}
		
		public String getString(String key) {
			return json.get(key).asText();
		}
		
		public String getString(String key, String fallback) {
			if (json.has(key)) {
				return json.get(key).asText();
			} else {
				return fallback;
			}
		}
		
		public boolean getBool(String key) {
			return json.get(key).asBoolean();
		}
		
		public boolean getBool(String key, boolean fallback) {
			if (json.has(key)) {
				return json.get(key).asBoolean();
			} else {
				return fallback;
			}
		}
		
		public int getInt(String key) {
			return json.get(key).asInt();
		}
		
		public int getInt(String key, int fallback) {
			if (json.has(key)) {
				return json.get(key).asInt();
			} else {
				return fallback;
			}
		}
		
		public long getLong(String key) {
			return json.get(key).asLong();
		}
		
		public long getLong(String key, long fallback) {
			if (json.has(key)) {
				return json.get(key).asLong();
			} else {
				return fallback;
			}
		}
		
		public double getDouble(String key) {
			return json.get(key).asDouble();
		}
		
		public double getDouble(String key, double fallback) {
			if (json.has(key)) {
				return json.get(key).asDouble();
			} else {
				return fallback;
			}
		}
		
	}
	
}
