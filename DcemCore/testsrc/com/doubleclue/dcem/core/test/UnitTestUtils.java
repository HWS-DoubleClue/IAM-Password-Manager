package com.doubleclue.dcem.core.test;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.jpa.DatabaseTypes;
import com.doubleclue.dcem.core.jpa.DatabaseUtils;
import com.doubleclue.dcem.core.utils.StreamUtils;


/**
 * 
 * @author 
 * 
 */
public class UnitTestUtils {

	private static final Logger logger = LogManager.getLogger(UnitTestUtils.class);

	private static final int MAX_SIZE = 1024 * 1024; // 1 MB

	public static void setSSMSHomeIfnotSet() {
		if (System.getProperty("DCEM_HOME") == null) {
			File semHome = new File("../dcem/testdata/config/");
			if (!semHome.exists()) {
				semHome = new File("../../dcem/testdata/config/");
			}
			System.setProperty("DCEM_HOME", semHome.getAbsolutePath());
		}
	}
	
	public static void setSemInstallIfnotSet() {
		if (System.getProperty("SEM_INSTALL") == null) {
			File ssmsInstall = new File("../dcem/testdata/");
			if (!ssmsInstall.exists()) {
				ssmsInstall = new File("../../dcem/testdata/");
			}
			System.setProperty("SEM_INSTALL", ssmsInstall.getAbsolutePath());
		}
	}
	
	/**
	 * 
	 * @param outPath
	 *            , defines the path where the file shall be written to, must
	 *            not be null
	 * @param fileName
	 *            , defines the file name, must not be null
	 * @param binaryData
	 *            , data to be written, must not be null
	 * @throws IOException
	 */
	public static void writeBinaryToFile(File file, byte[] binaryData) throws IOException {
		assert (binaryData != null);
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
			fos.write(binaryData);
			fos.flush();
		} finally {
			StreamUtils.close(fos);
		}
	}

	/**
	 * read file contents into byte array
	 * 
	 * @param inputFile
	 *            file that is up to {@link UnitTestUtils#MAX_SIZE} big
	 * @return
	 * @throws IOException
	 */
	public static byte[] readBinaryFromFile(File inputFile) throws IOException {
		assert (inputFile != null);
		if (inputFile.length() > UnitTestUtils.MAX_SIZE) {
			throw new RuntimeException("this method handles only files up to " + UnitTestUtils.MAX_SIZE + " bytes");
		}
		FileInputStream fis = null;
		DataInputStream dis = null;
		byte[] result = new byte[(int) inputFile.length()];
		try {
			fis = new FileInputStream(inputFile);
			dis = new DataInputStream(fis);
			dis.readFully(result);
		} finally {
			StreamUtils.close(dis);
			StreamUtils.close(fis);
		}
		return result;
	}

	/**
	 * return extension of a filename (without dot), if the file has no
	 * extension, the empty string is returned
	 * 
	 * @param file
	 * @return
	 */
	public static String getSuffix(File file) {
		String filename = file.getName();
		int dotPos = filename.lastIndexOf('.');
		if (dotPos == -1) {
			return "";
		}
		return filename.substring(dotPos + 1);
	}

	public static String readFileIntoString(File file) throws IOException {

		assert (file != null);

		StringBuilder contents = new StringBuilder();
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;

			while ((text = reader.readLine()) != null) {
				contents.append(text).append(System.getProperty("line.separator"));
			}
			return contents.toString();
		} catch (IOException e) {
			throw (e);
		} finally {
			StreamUtils.close(reader);
		}
	}

	
	/**
	 * create temporary directory
	 * 
	 * @return
	 * @throws IOException
	 */
	public static File createTempDirectory(String id) throws IOException {
		String prefix = id == null ? "" : "-" + id;

		File tempDir = null;
		while ((tempDir == null) || (tempDir.exists())) {
			tempDir = new File(System.getProperty("java.io.tmpdir"), "ssms-tmp" + prefix + System.nanoTime());
		}
		if (!tempDir.mkdir()) {
			throw new IOException("could not create " + tempDir.getAbsolutePath());
		}
		tempDir.deleteOnExit();
		return tempDir;
	}

	public static File createTempDirectory() throws IOException {
		return createTempDirectory(null);
	}

	private static void deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					deleteDirectory(file);
				} else {
					file.delete();
				}
			}
		}
		path.delete();
	}

	public static void cleanTempDirectories(final String id) {
		File tempBaseDir = new File(System.getProperty("java.io.tmpdir"));
		File[] tempBaseDirContents = tempBaseDir.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory() && pathname.getName().startsWith("ssms-tmp-" + id);
			}
		});
		for (File ssmsTmpDir : tempBaseDirContents) {
			deleteDirectory(ssmsTmpDir);
		}
	}

	

	public static String loadValue(Properties prop, String key) {
		String temp = prop.getProperty(key);
		if (temp == null || temp.isEmpty()) {
			throw new RuntimeException("database properties error - " + key + " is undefined");
		}
		return temp;
	}

//	private static File getDatabaseConfigFile(DatabaseTypes dbType) throws DcemException {
//		File configFile;
//		if ((System.getenv("HUDSON_URL") != null) || (System.getenv("JENKINS_URL") != null)) {
//			// running in hudson
//			configFile = new File(LocalPaths.getDcemHomeDir(), "jenkins-" + dbType.name().toLowerCase()
//					+ "-ssms-config.properties");
//		} else {
//			configFile = new File(LocalPaths.getDcemHomeDir(), "junit-" + dbType.name().toLowerCase()
//					+ "-ssms-config.properties");
//		}
//		logger.debug("loading database configuration from " + configFile.getAbsolutePath());
//		return configFile;
//	}

	/**
	 * create database table for given database type and for given modules.
	 * 
	 * @param dbmstype type of database. Either sql, mssql or hsql. default is hsql
	 * @param modules List of module name that need to be
	 * @param useCreativeIterative If true, an experimental dependency handling is used
	 * @throws Exception
//	 */
//	public static void createDatabaseTable(final DatabaseTypes dbType, String moduleName)
//			throws Exception {
//		
//		setSSMSHomeIfnotSet();
//		setSSMSInstallIfnotSet();
//
//		Properties databaseProperties = getDatabaseProperties(dbType);
//
//		String dburl = loadValue(databaseProperties, DatabaseUtils.DATABASE_URL);
//		String dbUsername = loadValue(databaseProperties, DatabaseUtils.DATABASE_USER);
//		String dbPass = loadValue(databaseProperties, DatabaseUtils.DATABASE_PASS);
//		String databaseName = loadValue(databaseProperties, DatabaseUtils.DATABASE_NAME);
//
//		switch (dbType) {
//		case MYSQL:
//			try{
//				DatabaseUtils.registerDatabaseDriver(dbType);
//			} catch (Exception e) {
//				throw new Exception("Cannot register database driver", e);
//			}
//			
//			// create Mysql Database
//			if (recreateMysqlDatabase(dburl, dbUsername, dbPass, databaseName)) {
//
//				// change database URL to select already created database
//				dburl = dburl + "/" + databaseName;
//
//			} else {
//				throw new RuntimeException("Exception during creating database.");
//			}
//
//			break;
//
//		case MSSQL:
//			try{
//				DatabaseUtils.registerDatabaseDriver(dbType);
//			} catch (Exception e) {
//				throw new Exception("Cannot register database driver", e);
//			}
//
//			// create Mssql Database
//			if (recreateMssqlDatabase(dburl, dbUsername, dbPass, databaseName)) {
//
//				// change database URL to select already created database
//				dburl = dburl + ";databaseName=" + databaseName;
//
//			} else {
//				throw new RuntimeException("Exception during creating database.");
//			}
//			break;
//			
//		case ORACLE:
//			try{
//				DatabaseUtils.registerDatabaseDriver(dbType);
//			} catch (Exception e) {
//				throw new Exception("Cannot register database driver", e);
//			}
//			
//			dburl = dburl + ":" + databaseName;
//			
//			break;
//		case HSQL:
//			// For HSQLDb database is automatically created. So we don't need to
//			// create it manually.
//			// Driver needs to be loaded otherwise log4jdbc-log4j2 wont't find it
//			Class.forName("org.hsqldb.jdbcDriver"); 
//			break;
//
//		default:
//			throw new RuntimeException("Invalid database type specified.");
//		}
//
//		Connection con = null;
//		try {
//			con = DriverManager.getConnection(dburl, dbUsername, dbPass);
//
//			AbstractDBCreator dbCreator = AbstractDBCreator.retrieveInstance(moduleName);
//			dbCreator.createIterative(dbType, con);
//
//		} catch (InstantiationException e) {
//			throw new RuntimeException(e);
//		} catch (IllegalAccessException e) {
//			throw new RuntimeException(e);
//		} catch (ClassNotFoundException e) {
//			throw new RuntimeException(e);
//		} finally {
//			sqlClose(con);
//		}
//	}

	public static boolean recreateDatabase(Properties prop, DatabaseTypes dbType) throws SQLException {

		if (prop == null) {
			throw new IllegalArgumentException("Propertie, containing databace connection informations, is not set.");
		}

		if (dbType == null) {
			throw new IllegalArgumentException("Database type, is not set.");
		}

		String dburl = prop.getProperty(DatabaseUtils.DATABASE_URL);
		if (dburl == null || dburl.trim().isEmpty()) {
			throw new IllegalArgumentException("Database url is null or empty.");
		}

		String dbUsername = prop.getProperty(DatabaseUtils.DATABASE_USER);
		if (dbUsername == null || dbUsername.trim().isEmpty()) {
			throw new IllegalArgumentException("Database user is null or empty.");
		}

		String dbPass = prop.getProperty(DatabaseUtils.DATABASE_PASS);
		if (dbPass == null || dbPass.trim().isEmpty()) {
			throw new IllegalArgumentException("Database password is null or empty.");
		}

		String databaseName = prop.getProperty(DatabaseUtils.DATABASE_NAME);
		if (databaseName == null || databaseName.trim().isEmpty()) {
			throw new IllegalArgumentException("Database name is null or empty.");
		}

		switch (dbType) {
		case MYSQL:
			return recreateMysqlDatabase(dburl, dbUsername, dbPass, databaseName);

		case MSSQL:
			return recreateMssqlDatabase(dburl, dbUsername, dbPass, databaseName);
		default:
			break;
		}

		throw new IllegalArgumentException("Unknown dbType: " + dbType);
	}

	private static boolean recreateMysqlDatabase(String dburl, String dbUsername, String dbPass, String databaseName)
			throws SQLException {
		Connection con = null;
		Statement stmt = null;
		try {
			con = DriverManager.getConnection(dburl, dbUsername, dbPass);
			System.out.println("dbUrl\t" + dburl + ", dbUsername=" + dbUsername + ", dbPass " + dbPass);
			stmt = con.createStatement();
			stmt.execute("DROP DATABASE IF EXISTS " + databaseName);
			stmt.execute("FLUSH TABLES");
			stmt.execute("CREATE DATABASE " + databaseName);
			stmt.execute("FLUSH PRIVILEGES");
			return true;
		} catch (SQLException e) {

			throw new SQLException("Cannot execute Statement. errorCode=" + e.getErrorCode() + ", sqlState="
					+ e.getSQLState(), e);
		} finally {
			sqlClose(stmt);
			sqlClose(con);
		}
	}

	private static boolean recreateMssqlDatabase(String dburl, String dbUsername, String dbPass, String databaseName)
			throws SQLException {

		Connection con = null;
		Statement stmt = null;
		try {
			con = DriverManager.getConnection(dburl, dbUsername, dbPass);
			stmt = con.createStatement();
			stmt.execute("IF db_id('" + databaseName + "') IS NULL CREATE DATABASE " + databaseName
					+ " collate Latin1_General_CI_AS");
			stmt.execute("ALTER DATABASE " + databaseName + " SET READ_COMMITTED_SNAPSHOT ON");
		} catch (SQLException e) {
			throw new SQLException("Cannot execute Statement. errorCode=" + e.getErrorCode() + ", sqlState="
					+ e.getSQLState(), e);
		} finally {
			sqlClose(stmt);
			sqlClose(con);
		}

		ResultSet rs = null;
		ResultSet rs1 = null;
		try {
			// Open Connection on given database.
			dburl = dburl + ";databaseName=" + databaseName;
			con = DriverManager.getConnection(dburl, dbUsername, dbPass);
			stmt = con.createStatement();

			// Delete all foreign keys before deleting tables
			rs = stmt
					.executeQuery("SELECT  OBJECT_NAME(fk.parent_object_id) AS fk_table_name, fk.name as fk_name FROM sys.foreign_keys fk JOIN	sys.tables tbl ON tbl.OBJECT_ID = fk.referenced_object_id");
			while (rs.next()) {
				stmt = con.createStatement();
				stmt.execute("ALTER TABLE " + rs.getString(1) + " DROP CONSTRAINT " + rs.getString(2));
			}

			// Delete all Database tables
			stmt = con.createStatement();
			rs1 = stmt.executeQuery("SELECT [TABLE_NAME] FROM [INFORMATION_SCHEMA].[TABLES]");
			String tableName;
			while (rs1.next()) {
				tableName = rs1.getString(1);
				stmt = con.createStatement();
				stmt.execute("IF EXISTS (SELECT [TABLE_NAME] FROM [INFORMATION_SCHEMA].[TABLES] WHERE [TABLE_NAME] = '"
						+ tableName + "') DROP TABLE " + tableName);
			}
			return true;
		} catch (SQLException e) {
			throw new SQLException("Cannot execute Statement. errorCode=" + e.getErrorCode() + ", sqlState="
					+ e.getSQLState(), e);
		} finally {
			sqlClose(rs);
			sqlClose(rs1);
			sqlClose(stmt);
			sqlClose(con);
		}
	}
	
//	public static void uninstallAllDatabaseTables(DatabaseTypes dbType, String moduleName) {
//		
//		setSSMSHomeIfnotSet();
//		setSSMSInstallIfnotSet();
//
//		Connection con = null;
//		try {
//			Properties databaseProperties = getDatabaseProperties(dbType);
//
//			String dburl = loadValue(databaseProperties, DatabaseUtils.DATABASE_URL);
//			String dbUsername = loadValue(databaseProperties, DatabaseUtils.DATABASE_USER);
//			String dbPass = loadValue(databaseProperties, DatabaseUtils.DATABASE_PASS);
//			String databaseName = loadValue(databaseProperties, DatabaseUtils.DATABASE_NAME);
//
//			switch (dbType) {
//			case MYSQL:
//				DatabaseUtils.registerDatabaseDriver(dbType);
//				dburl = dburl + "/" + databaseName;
//				break;
//				
//			case MSSQL:
//				DatabaseUtils.registerDatabaseDriver(dbType);
//				dburl = dburl + ";databaseName=" + databaseName;
//				break;
//				
//			case ORACLE:
//				DatabaseUtils.registerDatabaseDriver(dbType);
//				dburl = dburl + ":" + databaseName;
//				break;
//				
//			case HSQL:
//				break;
//				
//			default :
//				throw new RuntimeException("Invalid database type specified.");
//			}	
//			
//			con = DriverManager.getConnection(dburl, dbUsername, dbPass);
//
//			AbstractDBCreator dbCreator = AbstractDBCreator.retrieveInstance(moduleName);
//			dbCreator.uninstallAllDatabaseTables(dbType, con);
//			
//		} catch (InstantiationException e) {
//			throw new RuntimeException(e);
//		} catch (IllegalAccessException e) {
//			throw new RuntimeException(e);
//		} catch (ClassNotFoundException e) {
//			throw new RuntimeException(e);
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		} catch (SQLException e) {
//			throw new RuntimeException(e);
//		} catch (Exception e) {
//			throw new RuntimeException(e);
//		} finally {
//			sqlClose(con);
//		}
//		
//	}
	
	// Copied from Java 1.7
    private static class EmptyIterator<E> implements Iterator<E> {
        static final EmptyIterator<Object> EMPTY_ITERATOR
            = new EmptyIterator<Object>();

        public boolean hasNext() { return false; }
        public E next() { throw new NoSuchElementException(); }
        public void remove() { throw new IllegalStateException(); }
    }

	// Copied from Java 1.7
    public static <T> Iterator<T> emptyIterator() {
        return (Iterator<T>) EmptyIterator.EMPTY_ITERATOR;
    }
    
    private static void sqlClose(Connection con) {
		try {
			if (con != null && !con.isClosed()) {
				con.close();
			}
		} catch (SQLException e) {
			logger.info(
					"Cannot close sqlHelper.connection it will be ignored. errorCode="
							+ e.getErrorCode() + ", sqlState="
							+ e.getSQLState(), e);
		}
	}
    public static void sqlClose(final PreparedStatement statement) {
        try {
            if ((statement != null) && (!statement.isClosed())) {
            	sqlClose(statement.getResultSet());
                statement.close();
            }
        } catch (SQLException e) {
        	logger.error("Statement not closed. errorCode="+e.getErrorCode()+", sqlState="+e.getSQLState(), e);
        }
    }
    
    public static void sqlClose(final Statement statement) {
        try {
            if ((statement != null) && (!statement.isClosed())) {
            	sqlClose(statement.getResultSet());
                statement.close();
            }
        } catch (SQLException e) {
        	logger.error("Statement not closed. errorCode="+e.getErrorCode()+", sqlState="+e.getSQLState(), e);
        }
    }
    
    public static void sqlClose(final ResultSet rs) {
        try {
            if ((rs != null) && (!rs.isClosed())) {
                rs.close();
            }
        } catch (SQLException e) {
        	logger.error("ResultSet not closed. errorCode="+e.getErrorCode()+", sqlState="+e.getSQLState(), e);
        }
    }
    
	
}
