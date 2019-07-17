package net.shadowpie.sadiinso.sfc.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.dv8tion.jda.core.utils.JDALogger;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler;
import net.shadowpie.sadiinso.sfc.config.ConfigHandler.Config;
import net.shadowpie.sadiinso.sfc.sfc.SFC;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class DB {
	
	private static final Logger logger = JDALogger.getLog("DB");
	
	private static boolean init = false;
	private static HikariDataSource ds;
	
	public static int init() {
		Config cfg = ConfigHandler.queryConfig("database");
		
		// mysql
		cfg.setField("enabled", true);
		cfg.setField("flavour", "mysql");
		cfg.setField("host", "localhost");
		cfg.setField("database", "");
		cfg.setField("user", "");
		cfg.setField("pass", "");
		
		if(cfg.needRewrite())
			return SFC.STOP_CONFIG_REWRITE;
		
		// if DB connection is disabled
		if(!cfg.getBool("enabled"))
			return SFC.ALL_OK;
		
		logger.info("Connecting to the database...");
		
		String driverClass = null;
		String driverBase = null;
		
		switch(cfg.getString("flavour").toLowerCase()) {
			case "mysql":
				driverClass = "com.mysql.cj.jdbc.Driver";
				driverBase = "jdbc:mysql://";
				break;
				
			case "mariadb":
				driverClass = "org.mariadb.jdbc.Driver";
				driverBase = "jdbc:mariadb://";
				break;
			
			default:
				logger.error("Unavailable SGDB flavour");
				return SFC.STOP_MODULE_ERROR;
		}
		
		HikariConfig config = new HikariConfig();
		config.setDriverClassName(driverClass);
		config.setJdbcUrl(driverBase + cfg.getString("host") + "/" + cfg.getString("database") + "?serverTimezone=UTC");
		config.setUsername(cfg.getString("user"));
		config.setPassword(cfg.getString("pass"));
		config.addDataSourceProperty("useServerPrepStmts", "true");
		config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "25");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "1024");
        config.setPoolName("SFCPool");
        config.setConnectionInitSql("SET NAMES utf8mb4");
        ds = new HikariDataSource(config);
		
		init = true;
		return SFC.ALL_OK;
	}
	
	/**
	 * @return whether the DB connection is up or not
	 */
	public static boolean isInit() {
		return init;
	}
	
	/**
	 * Shutdown the DB connection
	 */
	public static void shutdown() {
		if(!init)
			return;
		
		init = false;
		ds.close();
	}
	
	/**
	 * Return a new database connection, don't forget to call close() on it once you are done
	 */
	public static Connection getConn() {
		if(!init)
			return null;
		
		try {
			return ds.getConnection();
		} catch (SQLException e) {
			logger.error("Error while retrieving a mysql connection", e);
			return null;
		}
	}
	
}
