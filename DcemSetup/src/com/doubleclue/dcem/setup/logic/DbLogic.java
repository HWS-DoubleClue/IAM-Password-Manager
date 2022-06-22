package com.doubleclue.dcem.setup.logic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ClusterConfig;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.config.KeyStorePurpose;
import com.doubleclue.dcem.core.config.LocalConfig;
import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.entities.DbVersion;
import com.doubleclue.dcem.core.entities.DcemConfiguration;
import com.doubleclue.dcem.core.entities.DcemNode;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jpa.DatabaseTypes;
import com.doubleclue.dcem.core.jpa.DbEncryption;
import com.doubleclue.dcem.core.jpa.DbFactoryProducer;
import com.doubleclue.dcem.core.jpa.EntityManagerProducer;
import com.doubleclue.dcem.core.jpa.JdbcUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.ActionLogic;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.CreateTenant;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.logic.TextResourceLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.doubleclue.dcem.core.weld.WeldSessionContext;
import com.doubleclue.dcem.system.logic.KeyStoreLogic;
import com.doubleclue.dcem.system.logic.NodeLogic;
import com.doubleclue.dcem.system.logic.NodeState;
import com.doubleclue.utils.ProductVersion;
import com.doubleclue.utils.RandomUtils;
import com.google.common.io.ByteStreams;

@ApplicationScoped
@Named("dbLogic")
public class DbLogic {

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	@Any
	Instance<DcemModule> modules;

	@Inject
	RoleLogic roleLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	ConfigLogic configLogic;

	@Inject
	ActionLogic actionLogic;

	@Inject
	KeyStoreLogic keyStoreLogic;

	@Inject
	NodeLogic nodeLogic;

	@Inject
	TemplateLogic templateLogic;

	@Inject
	TextResourceLogic textResourceLogic;

	@Inject
	CreateTenant createTenant;

	@Inject
	PolicyLogic policyLogic;

	@Inject
	GroupLogic groupLogic;

	List<DcemModule> sortedModules;

	private static final Logger logger = LogManager.getLogger(DbLogic.class);

	DbState dbState = null;

	@PostConstruct
	public void init() {
		sortedModules = applicationBean.getSortedModules();
	}

	public DbVersion getDbVersion(DatabaseConfig databaseConfig, String moduelId) throws Exception {

		Connection conn = null;
		try {
			conn = JdbcUtils.getJdbcConnectionWithSchema(databaseConfig, null, null);
			return JdbcUtils.getDbVersion(conn, moduelId);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	/**
	 * @param databaseConfig
	 * @return
	 */
	public List<String> testDbConnection(DatabaseConfig databaseConfig, String currentNodeName, String newNodeName) {

		List<String> list = new LinkedList<>();
		Connection conn;
		DatabaseTypes databaseType = DatabaseTypes.valueOf(databaseConfig.getDatabaseType());

		try {
			conn = JdbcUtils.getJdbcConnection(databaseConfig, null, null);
		} catch (SQLException e) {
			dbState = DbState.No_Connection;
			list.add("Connection failed. Reason: " + e.getMessage());
			if (databaseType == DatabaseTypes.POSTGRE && e.getMessage().startsWith("No suitable driver found")) {
				list.add("Please check! Most probably the URL have to end with a forward slash. '/'");
			}
			return list;
		}
		try {
			if (databaseConfig.getDatabaseType().equals(DatabaseTypes.DERBY.name()) == false) {
				list.add("Connection successful");
				Statement statement = conn.createStatement();
				ResultSet rs = null;
				switch (databaseType) {
				case MYSQL:
				case MARIADB:
					rs = statement
							.executeQuery("SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '" + databaseConfig.getDatabaseName() + "'");
					break;
				case MSSQL:
					rs = statement.executeQuery("SELECT name FROM master.sys.databases WHERE name = '" + databaseConfig.getDatabaseName() + "'");
					break;
				case POSTGRE:
					rs = statement
							.executeQuery("SELECT schema_name FROM information_schema.schemata WHERE schema_name = '" + databaseConfig.getDatabaseName() + "'");
					break;
				default:
					break;
				}
				if (rs == null) {
					dbState = DbState.Exception;
					list.add("Couldn't read database scheme.");
					return list;
				}
				if (rs.next() == false) {
					dbState = DbState.Create_Schema_Required;
					list.add("Database doesn't exist yet. Please proceed with the next step.");
					return list;
				} else {
					list.add("Database already exists.");
				}
			}

			// check if "dbversion" table is there
			if (isTablesAvailable(conn, databaseConfig)) {
				// String table = rsTables.getString("TABLE_NAME");
				// String scheme = rsTables.getString("TABLE_SCHEM");
				// String cat = rsTables.getString("TABLE_CAT");
				if (databaseConfig.getDatabaseEncryptionKey() == null || databaseConfig.getDatabaseEncryptionKey().isEmpty()) {
					list.add("Database tables exist, but you don't have the Database encryption key for it.");
					dbState = DbState.Exception;
					return list;
				}

				try {
					if (databaseConfig.getDatabaseType().equals(DatabaseTypes.DERBY.name()) == false) {
						conn.createStatement().execute(databaseType.getSchemaSwitch() + databaseConfig.getDatabaseName());
					}
					DbEncryption.createDbCiphers(databaseConfig);
					JdbcUtils.verifyDbKey(conn, databaseConfig);
				} catch (DcemException e) {
					if (e.getErrorCode() == DcemErrorCodes.UNEXPECTED_ERROR) {
						list.add("Database tables exist, but something went wrong! Reason: " + e.getCause().toString());
					} else {
						list.add("Database tables exists, but you have an invalid 'database-encryption-key' in DCEM_HOME\\configuration.xml!");
					}
					dbState = DbState.Exception;
					return list;
				}
				/*
				 * Check for migration
				 */
				if (checkMigration(conn).isEmpty()) {
					dbState = DbState.OK;
					if (currentNodeName.contentEquals(newNodeName) == false) {
						if (JdbcUtils.updateNodeName(conn, currentNodeName, newNodeName) != 1) {
							logger.error("Couldn't change the node name: " + currentNodeName);
						}
					}

				} else {
					list.add("Database exists, please proceed with DB Migration, next step.");
					dbState = DbState.Migration_Required;
				}

			} else {
				list.add("Database exists, please proceed with next step.");
				dbState = DbState.Create_Tables_Required;
			}

		} catch (Exception e) {
			dbState = DbState.Exception;
			list.add("Test failed. Cause: " + e.getMessage());
		} finally {
			JdbcUtils.closeConnection(conn);
		}
		return list;

	}

	public DbState getDbState() {
		return dbState;
	}

	public void setDbState(DbState dbState) {
		this.dbState = dbState;
	}

	public void createSchema(DatabaseConfig dbConfig, String schemaAdmin, String schemaPassword) throws SQLException {
		createTenant.createSchema(dbConfig.getDatabaseName(), schemaAdmin, schemaPassword);
	}

	/**
	 * @param dbConfig
	 * @param schemaAdmin
	 * @param schemaPassword
	 * @param clusterName
	 * @param superAdminPassword
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws Exception
	 */
	public void setupCreateTables(LocalConfig localConfig, String createTablesAdmin, String createTablesPassword)
			throws DcemException, SQLException, IOException {
		Connection conn = JdbcUtils.getJdbcConnection(localConfig.getDatabase(), createTablesAdmin, createTablesPassword);
		if (isTablesAvailable(conn, localConfig.getDatabase())) {
			conn.close();
			throw new DcemException(DcemErrorCodes.TABLE_EXISTS_ALREADY, "");
		}
		conn.close();
		DatabaseTypes dbType = DatabaseTypes.valueOf(localConfig.getDatabase().getDatabaseType());

		createTenant.createTables(dbType, localConfig.getDatabase().getDatabaseName(), createTablesAdmin, createTablesPassword, true);
		return;
	}

	public void resetAdminPassword(LocalConfig localConfig, String superAdminPassword) throws Exception {
		WeldRequestContext requestContext = null;
		WeldSessionContext weldSessionContext = null;
		EntityManagerProducer emp = null;
		Exception ex = null;
		try {
			requestContext = WeldContextUtils.activateRequestContext();
			weldSessionContext = WeldContextUtils.activateSessionContext(null);
			DbFactoryProducer dbFactoryProducer = DbFactoryProducer.getInstance();
			dbFactoryProducer.createEmp(localConfig, false);
			DbEncryption.createDbCiphers(localConfig.getDatabase());
			TenantEntity masterTenant = new TenantEntity(null, localConfig.getDatabase().getDatabaseName(), false, null, true, "master");
			TenantIdResolver.setMasterTenant(masterTenant);
			emp = CdiUtils.getReference(EntityManagerProducer.class);
			emp.init();
			DcemRole superAdminRole = new DcemRole(DcemConstants.SYSTEM_ROLE_SUPERADMIN, true, 10);
			superAdminRole = roleLogic.addRole(superAdminRole);
			DcemUser superAdmin = userLogic.getDistinctUser(DcemConstants.SUPER_ADMIN_OPERATOR);
			if (superAdmin == null) {
				superAdmin = new DcemUser(DcemConstants.SUPER_ADMIN_OPERATOR, "SuperAdmin@dummy.com", DcemConstants.SUPER_ADMIN_OPERATOR_DISPLAY,
						superAdminRole);
			}
			superAdmin.setInitialPassword(superAdminPassword);
			userLogic.addOrUpdateUserWoAuditing(superAdmin);
			userLogic.enableUserWoAuditing(superAdmin);
			policyLogic.setBackdoorToManagementPolicy();
			roleLogic.addActionsToRole(superAdminRole, actionLogic.getAllDcemActions("Privilege"));
			groupLogic.removeMemberFromAllGroups(superAdmin);
			JsfUtils.addInfoMessage("SuperAdmin password changed successfully. Please note that the Management Policy has been modified "
					+ "so that you can log in with the password only. You can change this in the policy settings once you have logged in successfully.");
		} finally {
			if (emp != null) {
				emp.close();
			}
			WeldContextUtils.deactivateRequestContext(requestContext);
			WeldContextUtils.deactivateSessionContext(weldSessionContext);
		}
	}

	/**
	 * @param localConfig
	 * @param clusterName
	 * @param superAdminPassword
	 * @throws Exception
	 */
	public void initializeDb(LocalConfig localConfig, String clusterId, String superAdminPassword, String mgtHost, String mgtPort, URL serverUrl)
			throws Exception {

		WeldRequestContext requestContext = null;
		WeldSessionContext weldSessionContext = null;
		try {

			requestContext = WeldContextUtils.activateRequestContext();
			weldSessionContext = WeldContextUtils.activateSessionContext(null);
			DcemCluster dcemCluster = DcemCluster.getInstance();
			/**
			 * Get the node Name
			 */
			String nodeName = localConfig.getNodeName();
			if (nodeName == null) {
				nodeName = DcemUtils.getComputerName();
				if (nodeName == null) {
					nodeName = "";
				}
			}
			logger.info("Creating Cluster-Config");
			ClusterConfig clusterConfig = new ClusterConfig();
			clusterConfig.setDefault();
			clusterConfig.setName(clusterId);
			clusterConfig.setGivenName(serverUrl.getHost());
			clusterConfig.setDcemHostDomainName(serverUrl.toString().toLowerCase());
			try {
				clusterConfig.getConnectionService(ConnectionServicesType.MANAGEMENT).setPort(Integer.parseInt(mgtPort));
			} catch (Exception e) {

			}
			clusterConfig.setPassword(RandomUtils.generateRandomAlphaLowercaseNumericString(16));
			// logger.info("starting Cluster");

			dcemCluster.startCluster(clusterConfig, nodeName);

			// logger.info("creating database factory");
			/*
			 * Create new Database Key
			 * 
			 */
			localConfig.getDatabase().setDatabaseConfigured(true);
			byte[] key = createDbKey();
			localConfig.getDatabase().setDatabaseEncryptionKey(Base64.getEncoder().encodeToString(key));
			LocalConfigProvider.writeConfig(localConfig);
			DbEncryption.createDbCiphers(key);
			DbFactoryProducer dbFactoryProducer = DbFactoryProducer.getInstance();
			dbFactoryProducer.createEmp(localConfig);
			EntityManagerProducer emp = CdiUtils.getReference(EntityManagerProducer.class);
			TenantEntity tenantEntity = new TenantEntity(null, localConfig.getDatabase().getDatabaseName(), false, null, true, "master");
			TenantIdResolver.setMasterTenant(tenantEntity);
			emp.init();
			DcemConfiguration dcemConfiguration = configLogic.getDbVerification();
			configLogic.setDcemConfiguration(dcemConfiguration); // write DB Verification to database

			/*
			 * Create Root Key-Store
			 * 
			 */
			logger.info("Creating keystores");
			String password = RandomUtils.generateRandomAlphaNumericString(16);
			KeyStore keyStore = SecureServerUtils.createKeyStore(DcemConstants.DEFAULT_KEY_PAIR_SIZE, "cn=" + clusterId, null, null, password.toCharArray(),
					null, KeyStorePurpose.ROOT_CA.name(), null);
			keyStoreLogic.addReplaceKeystore(keyStore, KeyStorePurpose.ROOT_CA, password, null, null);
			/*
			 * Create first Node
			 */
			logger.info("Creating node");
			DcemNode dcemNode = new DcemNode();
			dcemNode.setName(DcemUtils.getComputerName());
			// dcemNode.setNodeType(NodeTypeEnum.MGT_SVC);
			dcemNode.setState(NodeState.Off);
			nodeLogic.addDcemNode(dcemNode);

			/*
			 * Create Management Key-Store
			 * 
			 */
			logger.info("Creating MGT keystores");
			PrivateKey pvKey = (PrivateKey) keyStore.getKey(KeyStorePurpose.ROOT_CA.name(), password.toCharArray());
			String passwordMgt = RandomUtils.generateRandomAlphaNumericString(16);
			KeyStore keyStoreMgt = SecureServerUtils.createKeyStore(DcemConstants.DEFAULT_KEY_PAIR_SIZE, "cn=" + mgtHost,
					keyStore.getCertificateChain(KeyStorePurpose.ROOT_CA.name()), null, passwordMgt.toCharArray(), pvKey, KeyStorePurpose.Management_CA.name(),
					null);
			keyStoreLogic.addReplaceKeystore(keyStoreMgt, KeyStorePurpose.Management_CA, passwordMgt, dcemNode.getName(), null);

			/*
			 * Create WebSocket Key-Store
			 * 
			 */
			logger.info("Creating Web-Socket keystores");
			KeyStore keyStoreWs = SecureServerUtils.createKeyStore(DcemConstants.DEFAULT_KEY_PAIR_SIZE, "cn=" + mgtHost,
					keyStore.getCertificateChain(KeyStorePurpose.ROOT_CA.name()), null, passwordMgt.toCharArray(), pvKey,
					KeyStorePurpose.DeviceWebsockets_CA.name(), null);
			keyStoreLogic.addReplaceKeystore(keyStoreWs, KeyStorePurpose.DeviceWebsockets_CA, passwordMgt, dcemNode.getName(), null);

			/*
			 * Create SAML
			 * 
			 */
			logger.info("Creating SAML keystores");
			KeyStore keyStoreSaml = SecureServerUtils.createKeyStore(DcemConstants.DEFAULT_KEY_PAIR_SIZE, "cn=" + mgtHost, null, null,
					passwordMgt.toCharArray(), null, KeyStorePurpose.Saml_IdP_CA.name(), null);
			keyStoreLogic.addReplaceKeystore(keyStoreSaml, KeyStorePurpose.Saml_IdP_CA, passwordMgt, dcemNode.getName(), null);

			keyStoreSaml = SecureServerUtils.createKeyStore(DcemConstants.DEFAULT_KEY_PAIR_SIZE, "cn=" + mgtHost,
					keyStore.getCertificateChain(KeyStorePurpose.ROOT_CA.name()), null, passwordMgt.toCharArray(), pvKey,
					KeyStorePurpose.Saml_Connection_CA.name(), null);
			keyStoreLogic.addReplaceKeystore(keyStoreSaml, KeyStorePurpose.Saml_Connection_CA, passwordMgt, dcemNode.getName(), null);

			modulesAtInstall(keyStore, passwordMgt, dcemNode);
			/*
			 * 
			 * Cluster Configuration
			 * 
			 */
			DcemConfiguration dcemConfiguration2 = configLogic.createClusterConfig(clusterConfig);
			configLogic.setDcemConfiguration(dcemConfiguration2);

			// DcemCluster.getDcemCluster().setClusterConfig(clusterConfig);

			File file = LocalPaths.getClusterConfig();
			if (file.exists() == false) {
				InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(DcemConstants.CLUSTER_CONFIG_PATH);
				FileOutputStream outputStream = new FileOutputStream(LocalPaths.getClusterConfigX());
				ByteStreams.copy(inputStream, outputStream);
				inputStream.close();
				outputStream.close();
			}

			SupportedLanguage language;
			try {
				String currentLanguage = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale().getDisplayLanguage();
				Locale currentLocale = SupportedLanguage.toLocale(currentLanguage);
				language = SupportedLanguage.fromLocale(currentLocale);
			} catch (Exception e) {
				language = SupportedLanguage.English;
			}

			createTenant.initializeDbTenant(tenantEntity, superAdminPassword, null, null, language, null, null, null);
			logger.info("initilize DB READY");

		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
			WeldContextUtils.deactivateSessionContext(weldSessionContext);
		}

	}

	private void modulesAtInstall(KeyStore keyStore, String passwordMgt, DcemNode dcemNode) throws Exception {
		for (DcemModule module : sortedModules) {
			module.onInstall(keyStore, passwordMgt, dcemNode);
		}
	}

	/**
	 * Create a new dbKey and encrypt him with the codeKey
	 * 
	 * @return The encrypted dbKey
	 * @throws DatabaseDecryptionException
	 */
	public byte[] createDbKey() throws DcemException {

		// create new dbKey
		SecretKey dbKey;
		KeyGenerator kgen = null;
		try {
			kgen = KeyGenerator.getInstance(DcemConstants.DB_KEY_ALGORITHM);
		} catch (NoSuchAlgorithmException e) {
			throw new DcemException(DcemErrorCodes.DB_DECRYTION_ERROR, "No algorithm for db key.", e);
		}
		kgen.init(DcemConstants.DB_KEY_LENGTH);
		dbKey = kgen.generateKey();

		// create encryption Cipher
		Cipher dbKeyEncryptionCipher = null;
		try {
			dbKeyEncryptionCipher = Cipher.getInstance(DcemConstants.DB_KEY_ALG_MODE);
		} catch (NoSuchAlgorithmException e) {
			throw new DcemException(DcemErrorCodes.DB_DECRYTION_ERROR, "No algorithm for code key.", e);
		} catch (NoSuchPaddingException e) {
			throw new DcemException(DcemErrorCodes.DB_DECRYTION_ERROR, "Padding for codeKey is not available: " + DcemConstants.DB_KEY_PADDING, e);
		}

		try {
			dbKeyEncryptionCipher.init(Cipher.ENCRYPT_MODE, DbEncryption.generateKeyFromByteArray(DcemConstants.DB_KEY_ALGORITHM),
					DbEncryption.getAlgorithmParameterSpec());
		} catch (InvalidKeyException e) {
			throw new DcemException(DcemErrorCodes.DB_DECRYTION_ERROR, "Invalid code key.", e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new DcemException(DcemErrorCodes.DB_DECRYTION_ERROR, "Invalid code key algorithm.", e);
		}

		byte[] encryptedDbKey = null;
		try {
			encryptedDbKey = dbKeyEncryptionCipher.doFinal(dbKey.getEncoded());
		} catch (IllegalBlockSizeException e) {
			throw new DcemException(DcemErrorCodes.DB_DECRYTION_ERROR, "Illegal block size for dbKey.", e);
		} catch (BadPaddingException e) {
			throw new DcemException(DcemErrorCodes.DB_DECRYTION_ERROR, "Bad padding for dbKey.", e);
		}

		return encryptedDbKey;
	}

	private boolean isTablesAvailable(Connection conn, DatabaseConfig databaseConfig) throws SQLException {
		String[] types = { "TABLE", "VIEW" };
		String tables = "core_%";
		if (databaseConfig.getDatabaseType().equals(DatabaseTypes.DERBY.name()) == true) {
			tables = "CORE_%";
		}
		ResultSet rsTables;
		DatabaseTypes dbType = DatabaseTypes.valueOf(databaseConfig.getDatabaseType());
		if (dbType == DatabaseTypes.POSTGRE) {
			rsTables = conn.getMetaData().getTables(null, databaseConfig.getDatabaseName(), tables, types);
		} else {
			rsTables = conn.getMetaData().getTables(databaseConfig.getDatabaseName(), null, tables, types);
		}

		if (rsTables.next()) {
			// System.out.println(rsTables.getString("TABLE_NAME") + "(" + rsTables.getString("TABLE_TYPE") + ")");
			// while (rsTables.next()) {
			// System.out.println(rsTables.getString("TABLE_NAME") + "(" + rsTables.getString("TABLE_TYPE") + ")");
			// }
			return true;
		}
		return false;
	}

	public List<ModuleMigrationVersion> getMigrationModules(DatabaseConfig databaseConfig) throws Exception {
		Connection conn = null;
		try {
			conn = JdbcUtils.getJdbcConnectionWithSchema(databaseConfig, null, null);
			return checkMigration(conn);
		} finally {
			if (conn != null) {
				conn.close();
			}
		}
	}

	/**
	 * @param conn
	 * @param databaseConfig
	 * @return
	 * @throws Exception
	 */
	List<ModuleMigrationVersion> checkMigration(Connection conn) throws Exception {
		List<ModuleMigrationVersion> dbVersions = new ArrayList<>();
		DbVersion dbVersion;
		for (DcemModule module : sortedModules) {
			if (module.isHasDbTables() == false) {
				continue;
			}
			dbVersion = JdbcUtils.getDbVersion(conn, module.getId());
			if (dbVersion == null) {
				if (module.getDbVersion() > 0) {
					logger.info("DB-Migration necessary for " + module.getId() + ". Module Version is " + module.getDbVersion() + " db-version is null");
					dbVersions.add(
							new ModuleMigrationVersion(module.getId(), module.getName(), "0", Integer.toString(module.getDbVersion()), module.isMasterOnly()));
				}
				continue;
			}
			if (module.getDbVersion() == dbVersion.getVersion()) {
				// logger.info("No DB-Migration required for " + module.getId());
				continue;
			}
			if (module.getDbVersion() < dbVersion.getVersion()) {
				throw new Exception("DB-Migration cannot degrade DB-Version for Module:" + module.getId() + ". Module Version is " + module.getDbVersion()
						+ " db-version is " + dbVersion.getVersion());
			} else {
				logger.info("DB-Migration necessary for " + module.getId() + ". Module Version is " + module.getDbVersion() + " db-version is "
						+ dbVersion.getVersion());
				dbVersions.add(new ModuleMigrationVersion(module.getId(), module.getName(), dbVersion.getVersionStr(), Integer.toString(module.getDbVersion()),
						module.isMasterOnly()));
			}
		}
		dbVersion = JdbcUtils.getDbVersion(conn, DcemConstants.DCEM_MODULE_ID);
		ProductVersion productVersion = applicationBean.getProductVersion();
		if (productVersion.getVersionInt() > dbVersion.getVersion()) {
			dbVersions.add(0, new ModuleMigrationVersion(DcemConstants.DCEM_MODULE_ID, "DoubleClue Enterprise Management", dbVersion.getVersionStr(),
					productVersion.getVersionStr(), false));
		}
		return dbVersions;
	}

}
