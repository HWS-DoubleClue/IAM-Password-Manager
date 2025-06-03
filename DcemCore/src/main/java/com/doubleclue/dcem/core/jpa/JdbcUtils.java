package com.doubleclue.dcem.core.jpa;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.largeobject.LargeObject;
import org.postgresql.largeobject.LargeObjectManager;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.ClusterConfig;
import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.entities.DbVersion;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DatabaseUtils.UrlDriverName;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.utils.KaraUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JdbcUtils {

	private final static Logger logger = LogManager.getLogger(JdbcUtils.class);

	public static Connection getJdbcConnectionWithSchema(DatabaseConfig databaseConfig, String adminName, String adminPassword) throws Exception {
		Connection conn = getJdbcConnection(databaseConfig, adminName, adminPassword);
//		if (databaseConfig.getDatabaseType().equals(DatabaseTypes.DERBY.name()) == false) {
			DatabaseTypes databaseType = DatabaseTypes.valueOf(databaseConfig.getDatabaseType());
			conn.createStatement().execute(databaseType.getSchemaSwitch() + databaseConfig.getDatabaseName());
//		}
		return conn;
	}

	public static Connection getJdbcConnection(DatabaseConfig databaseConfig, String adminName, String adminPassword) throws Exception {
		Connection conn = null;
		UrlDriverName urlDriverName = DatabaseUtils.getUrlAndDriverName(databaseConfig);
		while (true) {
//			if (databaseConfig.getDatabaseType().equals(DatabaseTypes.DERBY.name())) {
//				Properties prop = System.getProperties();
//				try {
//					File file = LocalPaths.getDerbyDirectory();
//					prop.setProperty("derby.system.home", file.getAbsolutePath());
//					File fileSchema = new File(file, DatabaseConfig.DEFAULT_DATABASE_NAME);
//					if (fileSchema.exists() == false) {
//						urlDriverName.url += ";create=true";
//					}
//
//				} catch (DcemException exp) {
//					logger.error(exp);
//				}
//				try {
//					conn = DriverManager.getConnection(urlDriverName.url);
//				} catch (Exception e) {
//					logger.error("JDBC Connection failed URL : " + urlDriverName.url, e);
//					throw e;
//				}
//			} else {
				if (adminName == null) {
					adminName = databaseConfig.getAdminName();
					adminPassword = databaseConfig.getAdminPassword();
				}
				try {
					conn = DriverManager.getConnection(urlDriverName.url, adminName, adminPassword);
				} catch (Exception e) {
					logger.error("JDBC Connection failed URL : " + urlDriverName.url, e);
					throw e;
				}
		//	}
			break;
		}
		return conn;
	}

	public static Connection waitForJdbcConnection(DatabaseConfig databaseConfig, String adminName, String adminPassword) throws SQLException {
		Connection conn = null;
		DatabaseTypes databaseType = DatabaseTypes.valueOf(databaseConfig.getDatabaseType());
		UrlDriverName urlDriverName = DatabaseUtils.getUrlAndDriverName(databaseConfig);
		while (true) {
			try {
//				if (databaseType == DatabaseTypes.DERBY) {
//					Properties prop = System.getProperties();
//					try {
//						File file = LocalPaths.getDerbyDirectory();
//						prop.setProperty("derby.system.home", file.getAbsolutePath());
//						File fileSchema = new File(file, DatabaseConfig.DEFAULT_DATABASE_NAME);
//						if (fileSchema.exists() == false) {
//							urlDriverName.url += ";create=true";
//						}
//
//					} catch (DcemException exp) {
//						logger.error(exp);
//					}
//					conn = DriverManager.getConnection(urlDriverName.url);
//				} else {
					if (adminName == null) {
						adminName = databaseConfig.getAdminName();
						adminPassword = databaseConfig.getAdminPassword();
					}
					conn = DriverManager.getConnection(urlDriverName.url, adminName, adminPassword);
//				}
				if (databaseType.getSchemaSwitch() != null) {
					conn.createStatement().execute(databaseType.getSchemaSwitch() + databaseConfig.getDatabaseName());
				}
				break;
			} catch (SQLException exp) {
				logger.error("Failed to connect to database, will try again in one minute time. ", exp);
				try {
					Thread.sleep(60 * 1000);
				} catch (InterruptedException e) {
					logger.error("Wait failed", e);
				}
			}

		}
		return conn;
	}

	public static void verifyDbKey(Connection conn, DatabaseConfig databaseConfig) throws DcemException {
		byte[] data = null;
		try {
			data = getConfigData(conn, SystemModule.MODULE_ID, DcemConstants.CONFIG_KEY_DB_VERIFICATION);
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Couldn't verify DB-KEy", e);
		}
		if (Arrays.equals(data, ConfigLogic.DB_VERIFICATION) == false) {
			throw new DcemException(DcemErrorCodes.INVALID_DB_ENCRYPTION_KEY, "Invalid key");
		}
	}

	public static DatabaseMetaData getMetaData(Connection conn) {

		try {
			return conn.getMetaData();
		} catch (Exception e) {
			logger.warn("Couldn't get DatabaseMetaData", e);
		}
		return null;

	}

	public static ClusterConfig getClusterConfigbyJdbc(Connection conn)
			throws SQLException, JsonParseException, JsonMappingException, IOException, DcemException {

		Statement statement = conn.createStatement();
		ResultSet rs = statement.executeQuery(
				"SELECT cc.dc_value FROM core_config cc WHERE cc.moduleId='system' and cc.dc_key='" + DcemConstants.CONFIG_KEY_CLUSTER_CONFIG + "'");
		byte[] data;
		if (rs.next() == false) {
			return null;
		}
		data = rs.getBytes(1);
		statement.close();
		data = DbEncryption.decryptSeed(data);
		return new ObjectMapper().readValue(data, ClusterConfig.class);

	}

	public static byte[] getConfigData(Connection conn, String moduleId, String key) throws SQLException, DcemException {
		PreparedStatement statement = conn.prepareStatement("SELECT cc.dc_value FROM core_config cc WHERE cc.moduleId=? and cc.dc_key=?");
		statement.setString(1, moduleId);
		statement.setString(2, key);

		ResultSet rs = statement.executeQuery();
		byte[] data;
		if (rs.next() == false) {
			return null;
		}
		data = rs.getBytes(1);
		statement.close();
		return DbEncryption.decryptSeed(data);
	}

	public static void insertVersion(Connection conn, DbVersion dbVersion) throws SQLException {
		PreparedStatement statement = conn.prepareStatement("INSERT INTO sys_dbversion (moduleId, dbversion, versionStr) VALUES (?, ?, ?)");
		statement.setString(1, dbVersion.getModuleId());
		statement.setInt(2, dbVersion.getVersion());
		statement.setString(3, dbVersion.getVersionStr());
		statement.executeUpdate();
		statement.close();
		return;
	}

	public static void updateVersion(Connection conn, DbVersion dbVersion) throws SQLException {
		DbVersion dbVersion2 = getDbVersion(conn, dbVersion.getModuleId());
		if (dbVersion2 == null) {
			insertVersion (conn, dbVersion);
			return;
		}
		PreparedStatement statement = conn.prepareStatement("UPDATE sys_dbversion SET dbversion = ?, versionStr=? where moduleId=?");
		statement.setInt(1, dbVersion.getVersion());
		statement.setString(2, dbVersion.getVersionStr());
		statement.setString(3, dbVersion.getModuleId());
		statement.executeUpdate();
		statement.close();
		return;
	}

	// TODO Not Yet Tested
	public static DbVersion getDbVersion(Connection conn, String moduleId) throws SQLException {
		PreparedStatement statement = conn.prepareStatement("SELECT dbv.dbversion, versionstr FROM sys_dbversion dbv where dbv.moduleId = ?");
		statement.setString(1, moduleId);
		ResultSet rs = statement.executeQuery();
		if (rs.next() == false) {
			return null;
		}
		DbVersion dbVersion = new DbVersion();
		dbVersion.setModuleId(moduleId);
		dbVersion.setVersion(rs.getInt(1));
		dbVersion.setVersionStr(rs.getString(2));
		statement.close();
		return dbVersion;
	}

	// TODO Not Yet Tested
	public static Map<String, DbVersion> getAllDbVersion(Connection conn) throws SQLException {
		PreparedStatement statement = conn.prepareStatement("SELECT * FROM sys_dbversion");
		ResultSet rs = statement.executeQuery();
		Map<String, DbVersion> map = new HashMap<>();
		while (rs.next()) {
			DbVersion dbVersion = new DbVersion();
			dbVersion.setModuleId(rs.getString(1));
			dbVersion.setVersion(rs.getInt(2));
			dbVersion.setVersionStr(rs.getString(3));
			map.put(dbVersion.getModuleId(), dbVersion);
		}
		statement.close();
		return map;
	}

	// TODO Not Yet Tested
	// public static void addMasterTeneant(Connection conn, String masterSchema) throws SQLException {
	// PreparedStatement statement = conn
	// .prepareStatement("INSERT INTO sys_tenant (dc_schema, dc_enable, dc_fullname, dc_master, dc_name) VALUES (?, ?, ?, ?, ?)");
	// statement.setString(1, masterSchema);
	// statement.setBoolean(2, true);
	// statement.setString(3, "Master Tenant");
	// statement.setBoolean(4, true);
	// statement.setString(5, "master");
	// statement.executeUpdate();
	// statement.close();
	// return;
	// }

	public static TenantEntity getMasterTeneant(Connection conn) throws SQLException {
		PreparedStatement statement = conn.prepareStatement("SELECT * FROM sys_tenant tn where tn.dc_master = ?");
		statement.setBoolean(1, true);
		ResultSet rs = statement.executeQuery();
		TenantEntity tenantEntity = null;
		if (rs.next() == true) {
			tenantEntity = new TenantEntity(rs.getInt(1), rs.getString(2), rs.getBoolean(3), rs.getString(4), rs.getBoolean(5), rs.getString(6));
		}
		return tenantEntity;
	}

	public static List<TenantEntity> getTeneants(Connection conn) throws SQLException {
		PreparedStatement statement = conn.prepareStatement("SELECT * FROM sys_tenant ORDER BY dc_id ASC");
		ResultSet rs = statement.executeQuery();
		TenantEntity tenantEntity;
		List<TenantEntity> list = new LinkedList<>();
		while (rs.next()) {
			tenantEntity = new TenantEntity(rs.getInt("dc_id"), rs.getString("dc_schema"), rs.getBoolean("dc_disabled"), rs.getString("dc_fullname"),
					rs.getBoolean("dc_master"), rs.getString("dc_name"));
			list.add(tenantEntity);
		}
		return list;
	}

	public static void closeConnection(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {

			}
		}
	}

	public static Integer getNodeId(Connection conn, String clusterName) throws SQLException, DcemException {
		PreparedStatement statement = conn.prepareStatement("SELECT cc.dc_id FROM sys_node cc WHERE cc.dc_name=?");
		statement.setString(1, clusterName);
		ResultSet rs = statement.executeQuery();
		int type;
		if (rs.next() == false) {
			return null;
		}
		type = rs.getInt(1);
		statement.close();
		return type;
	}

	public static int updateNodeName(Connection conn, String currentNodeName, String newNodeName) throws SQLException, DcemException {
		PreparedStatement statement = conn.prepareStatement("UPDATE sys_node SET dc_name = ? WHERE dc_name = ?");
		statement.setString(1, newNodeName);
		statement.setString(2, currentNodeName);
		int count = statement.executeUpdate();
		statement.close();
		return count;
	}

	/**
	 * @param databaseConfig
	 * @param backupdirectory
	 * @return
	 * @throws SQLException
	 */
	public static long backUpEmbeddedDatabase(DatabaseConfig databaseConfig, String backupdirectory) throws Exception {
		long start = System.currentTimeMillis();
		Connection conn = null;
		try {
			conn = getJdbcConnection(databaseConfig, null, null);
			CallableStatement cs = conn.prepareCall("CALL SYSCS_UTIL.SYSCS_BACKUP_DATABASE(?)");
			cs.setString(1, backupdirectory);
			cs.execute();
			cs.close();
		} finally {
			closeConnection(conn);
		}
		return (System.currentTimeMillis() - start);
	}

	public static void switchDb(Connection conn, DatabaseTypes dbType, String dbName) throws SQLException {
		Statement stmt = conn.createStatement();
		if (dbType.getSchemaSwitch() != null) {
			stmt.execute(dbType.getSchemaSwitch() + dbName);
		}
	}

	public static int writeCloudSafeContent(int id, InputStream inputStream, boolean newEntry, long length) throws DcemException {
		Connection connection = null;
		try {
			DatabaseConfig databaseConfig = LocalConfigProvider.getLocalConfig().getDatabase();
			connection = getJdbcConnection(databaseConfig, null, null);
			DatabaseTypes dbType = DatabaseTypes.valueOf(databaseConfig.getDatabaseType());
			switchDb(connection, dbType, TenantIdResolver.getCurrentTenant().getSchema());
			connection.setAutoCommit(false);
			int contentIndex = 1;
			PreparedStatement stmt;
			int count;

			if (newEntry == true && cloudSafeContentExist(connection, id) == false) {
				stmt = connection.prepareStatement("insert into as_cloudsafecontent (cloudDataEntity_dc_id, content) values (?, ?)");
				stmt.setInt(1, id);
				contentIndex = 2;
			} else {
				stmt = connection.prepareStatement("UPDATE as_cloudsafecontent  SET content = ? WHERE cloudDataEntity_dc_id = ?");
				stmt.setInt(2, id);
			}
			if (databaseConfig.getDatabaseType().equals(DatabaseTypes.POSTGRE.name()) == false) {
				Blob blob = connection.createBlob();
				OutputStream outputStream = blob.setBinaryStream(1);
				count = KaraUtils.copyStream(inputStream, outputStream, DcemConstants.MAX_CIPHER_BUFFER);
				stmt.setBlob(contentIndex, blob);
				inputStream.close();
				outputStream.close();

			} else {
				LargeObjectManager lobj = ((org.postgresql.PGConnection) connection).getLargeObjectAPI();
				long oid = lobj.createLO(LargeObjectManager.WRITE);
				// Open the large object for writing
				LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
				// Copy the data from the file to the large object
				byte buf[] = new byte[1024 * 32];
				int s = 0;
				while ((s = inputStream.read(buf, 0, buf.length)) > 0) {
					obj.write(buf, 0, s);
				}
				// Close the large object
				obj.close();
				stmt.setLong(contentIndex, oid);
				// stmt.setBinaryStream(contentIndex, inputStream, length);
			}

			count = stmt.executeUpdate();
			connection.commit();
			try {
				stmt.close();
			} catch (SQLException e) {
			}
			return count;
		} catch (SQLIntegrityConstraintViolationException exp) {
			logger.info("Duplicate entry: " + id);
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_DUPLICAE_ENTRY, "Duplicate entry " + id, exp);
		} catch (Exception e) {
			logger.warn("Could not write to database", e);
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_WRITE_ERROR, "Couldn't save CloudSafe Content for " + id, e);
		} finally {
			closeConnection(connection);
		}
	}

	public static boolean cloudSafeContentExist(Connection connection, int id) throws DcemException, SQLException {
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement("select cloudDataEntity_dc_id  from as_cloudsafecontent  WHERE cloudDataEntity_dc_id = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			return rs.next();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}

	}

	public static File readCloudSafeContent(int id) throws DcemException {
		Connection connection = null;
		InputStream inputStream;
		File tempFile = null;
		try {
			DatabaseConfig databaseConfig = LocalConfigProvider.getLocalConfig().getDatabase();
			connection = getJdbcConnection(databaseConfig, null, null);
			DatabaseTypes dbType = DatabaseTypes.valueOf(databaseConfig.getDatabaseType());
			switchDb(connection, dbType, TenantIdResolver.getCurrentTenant().getSchema());
			connection.setAutoCommit(false);
			PreparedStatement stmt;
			// Get the Large Object Manager to perform operations with

			stmt = connection.prepareStatement("SELECT cs.content FROM as_cloudsafecontent cs WHERE clouddataentity_dc_id = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			boolean hasNext = rs.next();
			if (hasNext) {
				if (databaseConfig.getDatabaseType().equals(DatabaseTypes.POSTGRE.name()) == true) {
					LargeObjectManager largeObjectManager = ((org.postgresql.PGConnection) connection).getLargeObjectAPI();
					long oid = rs.getLong(1);
					LargeObject obj = largeObjectManager.open(oid, LargeObjectManager.READ);
					inputStream = obj.getInputStream();
				} else {
					inputStream = rs.getBinaryStream(1);
				}

				FileOutputStream fileOutputStream = null;
				tempFile = File.createTempFile("dcem-", "-cloudSafe");
				fileOutputStream = new FileOutputStream(tempFile);
				KaraUtils.copyStream(inputStream, fileOutputStream, 1024 * 64);
				fileOutputStream.close();
			} else {
				throw new DcemException(DcemErrorCodes.CLOUD_SAFE_NOT_FOUND, "Couldn't find CloudSafe Content for " + id);
			}
			try {
				stmt.close();
				rs.close();
			} catch (SQLException e) {
			}
			connection.commit();
			return tempFile;
		} catch (DcemException e) {
			throw e;
		} catch (Exception e) {
			logger.warn("Could not write to database", e);
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_READ_ERROR, "Couldn't read CloudSafe Content for " + id, e);
		} finally {
			closeConnection(connection);
		}
	}

	public static ArrayList<String> getListCloudSafeContent(Connection connection) throws Exception {
		PreparedStatement stmt = connection.prepareStatement("select cloudDataEntity_dc_id from as_cloudsafecontent");
		ResultSet resultSet = stmt.executeQuery();
		ArrayList<String> list = new ArrayList<>();
		while (resultSet.next()) {
			list.add(resultSet.getString(1));
		}
		stmt.close();
		return list;
	}

	public void deleteTenantDatabase(TenantEntity tenantEntity) throws Exception {
		Connection connection = null;
		PreparedStatement stmt = null;
		try {
			DatabaseConfig databaseConfig = LocalConfigProvider.getLocalConfig().getDatabase();
			connection = getJdbcConnection(databaseConfig, null, null);
			stmt = connection.prepareStatement("DROP DATABASE " + tenantEntity.getSchema());
			stmt.executeUpdate();
		} catch (Exception e) {
			throw e;
		} finally {
			closeConnection(connection);
		}
	}
}
