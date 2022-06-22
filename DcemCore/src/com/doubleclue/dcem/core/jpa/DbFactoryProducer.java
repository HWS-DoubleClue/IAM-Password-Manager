package com.doubleclue.dcem.core.jpa;

import java.io.IOException;
import java.net.URL;
import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.cfg.Environment;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.config.DbPoolConfig;
import com.doubleclue.dcem.core.config.LocalConfig;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DatabaseUtils.UrlDriverName;

/**
 * Produces an entity manager from the given config file.
 * 
 * @author Emanuel Galea
 * 
 */
public class DbFactoryProducer {

	public static final String HIBERNATE_C3P0_MAX_IDLE_TIME_EXCESS_CONNECTIONS = "hibernate.c3p0.maxIdleTimeExcessConnections";
	public static final String HIBERNATE_C3P0_PREFERRED_TEST_QUERY = "hibernate.c3p0.preferredTestQuery";
	public static final String HIBERNATE_C3P0_MAX_STATEMENTS = "hibernate.c3p0.max_statements";
	public static final String HIBERNATE_C3P0_ACQUIRE_RETRY_DELAY = "hibernate.c3p0.acquireRetryDelay";
	public static final String HIBERNATE_C3P0_NUM_HELPER_THREADS = "hibernate.c3p0.numHelperThreads";
	public static final String HIBERNATE_C3P0_TIMEOUT = "hibernate.c3p0.timeout";
	public static final String HIBERNATE_C3P0_CHECKOUT_TIMEOUT = "hibernate.c3p0.checkoutTimeout";
	public static final String HIBERNATE_C3P0_IDLE_TEST_PERIOD = "hibernate.c3p0.idle_test_period";
	public static final String HIBERNATE_C3P0_ACQUIRE_INCREMENT = "hibernate.c3p0.acquire_increment";
	public static final String HIBERNATE_C3P0_MAX_SIZE = "hibernate.c3p0.max_size";
	public static final String HIBERNATE_C3P0_MIN_SIZE = "hibernate.c3p0.min_size";

	public static final String HIBERNATE_C3P0_ACQUIRE_RETRY_ATTEMPTS = "hibernate.c3p0.acquireRetryAttempts";

	public static final String DATABASE_LOAD_MAX = "database.load.max";

	private static final Logger logger = LogManager.getLogger(DbFactoryProducer.class);

	final static String PU_NAME = "dcem.system";
	final static String PU_NAME_TENANT = "dcem.tenenat";

	static DbFactoryProducer dbFactoryProducer = new DbFactoryProducer();

	static public DbFactoryProducer getInstance() {
		return dbFactoryProducer;
	}

	DatabaseMetaData databaseMetaData;

	// private boolean dbStatistics;

	// @Inject
	// LocalConfigProvider localConfigProvider;

	private static DatabaseTypes dbType;

	static private EntityManagerFactory emfMaster = null;

	static private EntityManagerFactory emf = null;

	// private EntityManagerFactory systemEmf = null;

	// @PostConstruct
	// public void init() {
	// }

	/**
	 * @return
	 * @throws DcemException
	 */
	static Map<String, Object> getDbSettings(DatabaseConfig databaseConfig, DbPoolConfig dbPoolConfig, boolean with2ndLayerCache) throws DcemException {

		Map<String, Object> settings = new HashMap<String, Object>();
		settings.put("javax.persistence.lock.timeout", "1000");
		settings.put("hibernate.jdbc.batch_size", "200");

		if (databaseConfig != null && databaseConfig.isDatabaseConfigured()) {
			dbType = DatabaseTypes.valueOf(databaseConfig.getDatabaseType());
			if (dbType != DatabaseTypes.DERBY) {
				settings.put("javax.persistence.jdbc.user", databaseConfig.getAdminName());
				settings.put("javax.persistence.jdbc.password", databaseConfig.getAdminPassword());
			}
			UrlDriverName urlDriverName = DatabaseUtils.getUrlAndDriverName(databaseConfig);
			settings.put("javax.persistence.jdbc.driver", urlDriverName.driverName);
			if (dbType == DatabaseTypes.MSSQL) {
				settings.put(HIBERNATE_C3P0_MAX_STATEMENTS, 0);
			} else {
				settings.put(HIBERNATE_C3P0_MAX_STATEMENTS, Integer.toString(dbPoolConfig.getMaxStatements()));
			}
			settings.put("javax.persistence.jdbc.url", urlDriverName.url);
			if (dbType != DatabaseTypes.DERBY) {
				String schemaName = databaseConfig.getSchemaName().trim();
				if (dbType == DatabaseTypes.MSSQL && schemaName.length() > 0) {
					settings.put(Environment.DEFAULT_SCHEMA, databaseConfig.getDatabaseName() + "." + schemaName);
				} else {
					settings.put(Environment.DEFAULT_SCHEMA, databaseConfig.getDatabaseName());
				}
			}
			
			/**
			 * hibernate settings
			 */
			settings.put(HIBERNATE_C3P0_ACQUIRE_INCREMENT, Integer.toString(dbPoolConfig.getAcquireIncrement()));
			settings.put(HIBERNATE_C3P0_IDLE_TEST_PERIOD, Integer.toString(dbPoolConfig.getDatabasePoolCheckConnectionInterval()));

			settings.put(HIBERNATE_C3P0_MIN_SIZE, Integer.toString(dbPoolConfig.getDatabasePoolMinimum()));

			settings.put(HIBERNATE_C3P0_MAX_SIZE, Integer.toString(dbPoolConfig.getDatabasePoolMaximum()));

			settings.put(HIBERNATE_C3P0_CHECKOUT_TIMEOUT, Integer.toString(dbPoolConfig.getCheckoutTimeout())); //
			// milliseconds
			settings.put(HIBERNATE_C3P0_TIMEOUT, Integer.toString(dbPoolConfig.getDatabasePoolTimeout()));

			settings.put(HIBERNATE_C3P0_NUM_HELPER_THREADS, Integer.toString(dbPoolConfig.getNumHelperThreads()));

			settings.put(HIBERNATE_C3P0_ACQUIRE_RETRY_ATTEMPTS, Integer.toString(dbPoolConfig.getAcquireRetryAttempts()));
			settings.put(HIBERNATE_C3P0_ACQUIRE_RETRY_DELAY, Integer.toString(dbPoolConfig.getAcquireRetryDelay()));
			settings.put(HIBERNATE_C3P0_MAX_IDLE_TIME_EXCESS_CONNECTIONS, Integer.toString(dbPoolConfig.getMaxIdleTimeExcessConnections()));

			/**
			 * DBMS specific
			 */
			try {
				dbType = DatabaseTypes.valueOf(databaseConfig.getDatabaseType());
			} catch (IllegalArgumentException exp) {
				throw new DcemException(DcemErrorCodes.INIT_DATABASE, "Database Wrong DB Type", exp);
			}

			settings.put("hibernate.dialect", dbType.getHibernateDialect());
			settings.put(HIBERNATE_C3P0_PREFERRED_TEST_QUERY, dbType.getTestQuery());

			/**
			 * 2nd Layer Cache specific
			 */
			if (with2ndLayerCache == true) {
//				settings.put("hibernate.cache.region.factory_class", "org.hibernate.cache.jcache.JCacheRegionFactory");
				settings.put("hibernate.javax.cache.provider", "com.hazelcast.cache.HazelcastCachingProvider");
				// settings.put("hibernate.cache.region.factory_class",
				// "com.hazelcast.hibernate.HazelcastCacheRegionFactory");
				settings.put("hibernate.cache.region.factory_class", "com.hazelcast.hibernate.HazelcastLocalCacheRegionFactory");
				settings.put("hibernate.cache.use_second_level_cache", "true");
				settings.put("hibernate.cache.use_query_cache", "true");
				settings.put("hibernate.cache.region_prefix", "");
				settings.put("hibernate.cache.hazelcast.instance_name", DcemConstants.CLUSTER_INTANCE_NAME);
//				settings.put("hibernate.cache.region.factory_class", "org.hibernate.cache.jcache.JCacheRegionFactory");
			} else {
				settings.put("hibernate.cache.use_second_level_cache", "false");
			}
			

		} else {
			logger.error("!!! DATABASE IS NOT YET CONFIGURED !!!");
			throw new DcemException(DcemErrorCodes.INIT_DATABASE, "Database connection is not configured");
		}
		return settings;
	}
	
	public void createEmp(LocalConfig localConfig) throws DcemException {
		createEmp (localConfig, true);
	}

	/**
	 * @param dbSettings
	 * @throws DcemException
	 */
	public EntityManagerFactory createEmp(LocalConfig localConfig, boolean with2ndLayerCache) throws DcemException {

		if (emf != null) {
			try {
				emf.close();
			} catch (Exception e) {
				// TODO: handle exception
			}
			emf = null;
		}

		Map<String, Object> dbSettings = getDbSettings(localConfig.getDatabase(), localConfig.getDbPoolConfig(), with2ndLayerCache);

		try {
			// DbEncryption.createDbCiphers(localConfig.getDatabase());
			logger.info("Starting Database initializing");

			List<Class<?>> entitties = null;
			dbSettings.put("hibernate.c3p0.dataSourceName", PU_NAME);
			try {
				entitties = getPersistenceClasses();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			dbSettings.put(org.hibernate.jpa.AvailableSettings.LOADED_CLASSES, entitties);
			PersistenceProvider provider = new HibernatePersistenceProvider();
			// emfMaster = provider.createEntityManagerFactory(PU_NAME, dbSettings);
			if (dbType != DatabaseTypes.DERBY) {
				dbSettings.remove(Environment.DEFAULT_SCHEMA);
				dbSettings.put(Environment.MULTI_TENANT, MultiTenancyStrategy.SCHEMA.name());
				dbSettings.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, MultiTenantConnectionProviderImpl.class.getName());
				dbSettings.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, TenantIdResolver.class.getName());
			}
			emf = provider.createEntityManagerFactory(PU_NAME, dbSettings);
			logger.info("EntityManagerFactory for tenant created");
			return emf;
		} catch (Throwable e) {
			logger.error("!!! DATABASE FAILED System!!!", e);
			throw new DcemException(DcemErrorCodes.INIT_DATABASE, "Database connection is not configured");
		}

	}

	public EntityManagerFactory getEntityManagerFactory() {
		return emf;
	}

	/**
	 * this function should only be used by methods that must use an entity
	 * manager and are not called during a request. For Request(scoped) usage
	 * just usage normal injection!
	 * 
	 * currently the only place where this is used is for application startup
	 * functions
	 * 
	 * @return
	 */
	public EntityManager produceUnmanagedEntitymanager() {
		return emf.createEntityManager();
	}

	public void disposeUnmanagedEntityManager(EntityManager em) {
		try {
			if (em != null && em.isOpen()) {
				if (em.getTransaction() != null && em.getTransaction().isActive()) {
					logger.warn("Transaction is still active");
				}
				em.close();
			}
		} catch (Exception exp) {
			if (logger.isDebugEnabled()) {
				logger.debug("Destroying entity manager", exp);
			}
		}

	}

	/**
	 * parse all META-INF/persistance.xml's and add all entities to the
	 * persistence context
	 * 
	 * @param puConfiguration
	 * @throws IOException
	 */
	public static List<Class<?>> getPersistenceClasses() throws IOException {

		HashSet<String> classesMap = new HashSet<String>();

		Enumeration<URL> persistenceXMLres = Thread.currentThread().getContextClassLoader().getResources("META-INF/persistence.xml");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			while (persistenceXMLres.hasMoreElements()) {
				URL persistenceXMLURL = persistenceXMLres.nextElement();
				DocumentBuilder builder = factory.newDocumentBuilder();
				Document persistenceDocument = builder.parse(persistenceXMLURL.openStream());
				XPath xPath = XPathFactory.newInstance().newXPath();
				NodeList result = (NodeList) xPath.evaluate("/persistence/persistence-unit/class/text()", persistenceDocument, XPathConstants.NODESET);

				for (int i = 0; i < result.getLength(); i++) {
					String className = result.item(i).getNodeValue();
					classesMap.add(className);
				}

			}
			ArrayList<Class<?>> list = new ArrayList<Class<?>>(classesMap.size());
			Iterator<String> iterator = classesMap.iterator();
			Class<?> cls = null;
			;
			while (iterator.hasNext()) {
				String className = iterator.next();
				cls = Class.forName(className);
				list.add(cls);
			}

			return list;
		} catch (Exception exp) {
			logger.error("Please check Installation, couldn't read the persistence.xml of modules", exp);
			throw new IOException(exp);
		}
	}

	public static DatabaseTypes getDbType() {
		return dbType;
	}

	public DatabaseMetaData getDatabaseMetaData() {
		return databaseMetaData;
	}

	public void setDatabaseMetaData(DatabaseMetaData databaseMetaData) {
		this.databaseMetaData = databaseMetaData;
	}

}
