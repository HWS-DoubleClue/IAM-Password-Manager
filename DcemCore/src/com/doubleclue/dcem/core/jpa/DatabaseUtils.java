package com.doubleclue.dcem.core.jpa;

import java.net.MalformedURLException;
import java.sql.SQLException;

import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;

/**
 * 
 * @author Emanuel Galea
 *
 */
public class DatabaseUtils {
	
	public static class UrlDriverName {
		public String url;
		public String driverName;
	}

	public static final String DATABASE_NAME = "database.name";
	public static final String DATABASE_USER = "database.operations.user";
	public static final String DATABASE_PASS = "database.operations.pass";
	public static final String DATABASE_URL = "database.operations.dbUrl";
	public static final String DATABASE_DRIVER = "database.operations.driver";
	public static final String DATABASE_TYPE = "database.operations.dbType";
	
	public static boolean logForJdbc = false;

	
	/**
	 * Used to generate Database URL from given database type.
	 * @throws DcemException 
	 */
	public static String createDatabaseUrl(DatabaseConfig databaseConfig)
			throws DcemException {

		DatabaseTypes dbType = DatabaseTypes.valueOf(databaseConfig.getDatabaseType());
		if (dbType == null) {
			throw new IllegalArgumentException("Database type must not be null or empty");
		}

		
		String dbUrl = null;

		switch (dbType) {
		case MYSQL:
		case MARIADB:
			dbUrl = dbType.getProtocol() + "://" + databaseConfig.getIpAddress() + ":" + databaseConfig.getPort(); 
			break;
		case MSSQL:
			dbUrl = dbType.getProtocol() + "://" + databaseConfig.getIpAddress() + ":" + databaseConfig.getPort();
			break;
		case DERBY: 
//			// jdbc:derby:DatabaseName
//			dbUrl = dbType.getProtocol() + "://" + databaseConfig.getIpAddress() + ":" + databaseConfig.getPort() +  ";";
			dbUrl = dbType.getProtocol() + ":" + DatabaseConfig.DEFAULT_DATABASE_NAME  + ";collation=TERRITORY_BASED:PRIMARY"; 
			break;
//		case ORACLE:
//			dbUrl = dbType.getProtocol() + ":@" + databaseConfig.getIpAddress() + ":" + databaseConfig.getPort();
//			break;
		case POSTGRE:
			dbUrl = dbType.getProtocol() + "://" + databaseConfig.getIpAddress() + ":" + databaseConfig.getPort();
			break;
		default:
			throw new DcemException(DcemErrorCodes.INVALID_DB_TYPE, "Invalid database type.");
		}
		return dbUrl;
	}

	/**
	 * @param databaseType
	 * @throws MalformedURLException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static void registerDatabaseDriver(DatabaseTypes databaseType) throws MalformedURLException,
			InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

		if (databaseType.getDriver() == null) {
			return; // nothing to register.
		}

		String driverName = databaseType.getDriver();
		if (isLog4Jdbc()) {
			driverName = setLog4JdbcProperties(driverName);
			Class.forName(driverName);
			return;
		} else if (/* databaseType == DatabaseTypes.ORACLE || */ databaseType == DatabaseTypes.MSSQL) {
			Class.forName(databaseType.getDriver());
			return;
		}

	}
	
	public static UrlDriverName getUrlAndDriverName (DatabaseConfig databaseConfig) {
		UrlDriverName urlDriverName = new UrlDriverName();
		String dbUrl = databaseConfig.getJdbcUrl();
		String driverName = DatabaseTypes.valueOf(databaseConfig.getDatabaseType()).getDriver();
		logForJdbc = false;
		if (DatabaseUtils.isLog4Jdbc()) {
			dbUrl = "jdbc:log4jdbc:" + dbUrl.substring(5);
			driverName = DatabaseUtils.setLog4JdbcProperties(driverName);
			logForJdbc = true;
		}
		urlDriverName.driverName = driverName;
		urlDriverName.url = dbUrl;
		return urlDriverName;
	}

	/**
	 * @return
	 */
	private static boolean isLog4Jdbc() {
		String prop = System.getProperty("log4jdbc");
		if (prop == null) {
			return false;
		}
		if (prop.equalsIgnoreCase("true")) {
			return true;
		}
		return false;
	}

	/**
	 * Set the system Properties for log4jdbc
	 * 
	 * @param driverName
	 * @return the log4jdbc driver name
	 */
	public static String setLog4JdbcProperties(String driverName) {
		System.setProperty("log4jdbc.drivers", driverName);
		System.setProperty("log4jdbc.auto.load.popular.drivers", "false");
		System.setProperty("log4jdbc.debug.stack.prefix", "^com\\.doubleclue\\.dcem.*");

		return "net.sf.log4jdbc.sql.jdbcapi.DriverSpy";
	}
	

}



