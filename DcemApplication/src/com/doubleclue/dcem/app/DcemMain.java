package com.doubleclue.dcem.app;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.KeyStore;
import java.security.Security;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.commons.lang3.SystemUtils;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.descriptor.web.ErrorPage;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.hibernate.exception.GenericJDBCException;

import com.doubleclue.dcem.admin.servlet.LoginWebFilter;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.DcemJarScanFilter;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ClusterConfig;
import com.doubleclue.dcem.core.config.ConnectionService;
import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.config.KeyStorePurpose;
import com.doubleclue.dcem.core.config.LocalConfig;
import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.config.TuningMaxValues;
import com.doubleclue.dcem.core.entities.DbVersion;
import com.doubleclue.dcem.core.entities.DcemNode;
import com.doubleclue.dcem.core.entities.KeyStoreEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.DbEncryption;
import com.doubleclue.dcem.core.jpa.DbFactoryProducer;
import com.doubleclue.dcem.core.jpa.JdbcUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logging.DcemLogLevel;
import com.doubleclue.dcem.core.logging.LogUtils;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.TenantLogic;
import com.doubleclue.dcem.core.servlets.DcemFilter;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.system.logic.KeyStoreLogic;
import com.doubleclue.dcem.system.logic.NodeLogic;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.ProductVersion;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DcemMain {

	public static LocalConfig localConfig;
	private static Logger logger;

	static String javaVersion;

	public static void main(String[] args) {
		javaVersion = System.getProperty("java.version");
		

		System.out.println("Java Version: " + javaVersion);
		System.out.println("Java Installation: " + System.getProperty("java.home"));
		long start = System.currentTimeMillis();
		System.setProperty("java.awt.headless", "true");
		Security.addProvider(new BouncyCastleProvider());
		if (javaVersion.startsWith("1")) {
			KaraUtils.removeCryptographyRestrictions();
		}

		try {
			LocalPaths.getDcemHomeDir();
		} catch (DcemException e) {
			System.err.println("ERROR: Couldn't set the Application Home Directory, Please set 'DCEM_HOME' in enviorment or as System-Parameter");
			System.exit(-1);
		}
		if (SystemUtils.IS_OS_WINDOWS) {
			System.setProperty("javax.net.ssl.trustStoreType", "Windows-ROOT");
		}
		Locale.setDefault(Locale.ENGLISH);
		Locale.setDefault(Locale.Category.DISPLAY, Locale.ENGLISH); 
		TimeZone defaultTimeZone = TimeZone.getTimeZone(DcemConstants.SYSTEM_DEFAULT_ZONE);
		TimeZone.setDefault(defaultTimeZone);
		
		boolean debugMode = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;
		DcemLogLevel dcemLogLevel = DcemLogLevel.INFO;
		if (debugMode) {
			dcemLogLevel = DcemLogLevel.DEBUG;
		}
		LogUtils.initLog4j(null, null, dcemLogLevel, debugMode);
		logger = LogManager.getLogger(DcemMain.class);

		DcemApplicationBean.debugMode = debugMode;
		try {
			JLogger.setup(LocalPaths.getDcemLogDir().getAbsolutePath() + File.separatorChar + "tomcat.%g.log", 1024 * 1024, 4);
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}

		try {
			logger.info("DCEM_HOME directory: " + LocalPaths.getDcemHomeDir().getAbsolutePath());
			logger.info("DCEM Installation directory: " + LocalPaths.getDcemInstallDir().getAbsolutePath());
		} catch (DcemException e2) {
			logger.fatal("Couldn't get teh home directory", e2);
			System.exit(-1);
		}

		if (javaVersion.startsWith("1.8") == false) {
			fatalExit(null, "!!!  Sorry INVALID JAVA Version. !!! Please use version 1.8.xx", null);
		}
		// logger.info("Java User Home directory: " +
		// System.getProperty("user.home"));

		System.setProperty("org.jboss.weld.xml.disableValidating", "true");
		// System.setProperty("com.sun.xml.ws.fault.SOAPFaultBuilder.disableCaptureStackTrace",
		// "false");

		try {
			localConfig = LocalConfigProvider.readConfig();
			if (localConfig == null) {
				fatalExit(null, "Local configuration not found. Please create localconfiguration at " + LocalPaths.getConfigurationFile().getAbsolutePath(),
						null);
			}
		} catch (DcemException e2) {
			fatalExit(null, "Error at reading local configuration file: " + LocalPaths.getDcemHomeFile().getAbsolutePath(), e2);
		}

		TuningMaxValues.createDefaults();
		DatabaseConfig databaseConfig = localConfig.getDatabase();
		try {
			DbEncryption.createDbCiphers(databaseConfig);
		} catch (DcemException e1) {
			fatalExit(null, "Error at creating DB Ciphers", null);
		}
		Connection conn = null;
		try {
			conn = JdbcUtils.waitForJdbcConnection(databaseConfig, null, null);
		} catch (SQLException exp) {
			fatalExit(conn, "Couldn't establish Database connection", exp);
		}
		try {
			JdbcUtils.verifyDbKey(conn, localConfig.getDatabase());
		} catch (Exception exp) {
			fatalExit(conn, "Invalid database Encryption key!", exp);
		}

		DatabaseMetaData databaseMetaData = JdbcUtils.getMetaData(conn);

		/**
		 * Get the node Name
		 */
		String nodeName = localConfig.getNodeName();
		if (nodeName == null) {
			nodeName = DcemUtils.getComputerName();
			if (nodeName == null) {
				fatalExit(conn, "No Node Name found! You must set the Node Name in configuration.xml or enviroment as either COMPUTERNAME or HOSTNAME", null);
			}
			localConfig.setNodeName(nodeName);
			try {
				LocalConfigProvider.writeConfig(localConfig);
			} catch (DcemException e) {
				e.printStackTrace();
			}
		}
		ClusterConfig clusterConfig = null;
		byte[] data = null;

		try {
			data = JdbcUtils.getConfigData(conn, SystemModule.MODULE_ID, DcemConstants.CONFIG_KEY_CLUSTER_CONFIG);
			clusterConfig = new ObjectMapper().readValue(data, ClusterConfig.class);
			if (JdbcUtils.getNodeId(conn, nodeName) == null) {
				fatalExit(conn, "No DB-Node configuration found for Node: " + nodeName, null);
			}
			clusterConfig.addMissingConnectionServices();
		} catch (UnrecognizedPropertyException exp) {
			logger.warn("ATTENTION NEW Cluster Configuration. As the old configuration is not compatible.");
			ObjectNode node = null;
			try {
				node = new ObjectMapper().readValue(new String(data, DcemConstants.CHARSET_UTF8), ObjectNode.class);
			} catch (Exception e) {
				fatalExit(conn, "Failed to retrieve Cluster and Node configuration from database", exp);
			}
			clusterConfig = new ClusterConfig();
			clusterConfig.setDefault();
			clusterConfig.setName(node.get("name").textValue());
			clusterConfig.setPassword(node.get("password").textValue());
			clusterConfig.setGivenName(node.get("givenName").textValue());
			clusterConfig.setWebAppName(node.get("webAppName").textValue());
			clusterConfig.setToSave(true);
			// clusterConfig.setDcemHostDomainName(node.get("dcemHostDomainName").textValue());
		} catch (Exception exp) {
			fatalExit(conn, "Failed to retrieve Cluster and Node configuration from database", exp);
		}
		clusterConfig.setToSave(true);
		DbVersion dbVersion = null;
		DbVersion dbVersionSystemModule = null;
		try {
			dbVersion = JdbcUtils.getDbVersion(conn, DcemConstants.DCEM_MODULE_ID);
			dbVersionSystemModule = JdbcUtils.getDbVersion(conn, SystemModule.MODULE_ID);
			if (dbVersion == null) {
				throw new Exception("ProductDbVerion DCEM not found");
			}
		} catch (Exception exp) {
			fatalExit(conn, "Failed to retrieve DCEM Version from database", exp);
		}
		ProductVersion productVersion = null;
		try {
			productVersion = KaraUtils.getProductVersion(DcemMain.class);
		} catch (Exception exp) {
			fatalExit(conn, "Could read Product-Version", exp);
		}
		boolean ignoreDbVersion = false;
		String param = System.getProperty(DcemConstants.PARAM_IGNORE_DB_VERSION);
		if (param != null && param.equalsIgnoreCase("true")) {
			ignoreDbVersion = true;
		}
		//
		if (productVersion.getVersionInt() > dbVersion.getVersion()) {
			if (dbVersionSystemModule.getVersion() == SystemModule.DATABASE_VERSION) {
				logger.warn("NEW PRODUCT VERSION -  WITHOUT DB-MIGRATION : " + productVersion.getVersionStr() + "(" + productVersion.getVersionInt()
						+ "), Current DB-Version: " + dbVersion.getVersionStr() + "(" + dbVersion.getVersion() + ")");
				try {
					dbVersion.setVersion(productVersion.getVersionInt());
					dbVersion.setVersionStr(productVersion.getVersionStr());
					JdbcUtils.updateVersion(conn, dbVersion);
				} catch (SQLException exp) {
					fatalExit(conn, "Could write new Version", exp);
				}

			} else {
				logger.error("This Product-Version: " + productVersion.getVersionStr() + "(" + productVersion.getVersionInt() + "), Current DB-Version: "
						+ dbVersion.getVersionStr() + "(" + dbVersion.getVersion() + ")");
				if (ignoreDbVersion == false) {
					logger.debug(
							"\nDATABASE MIGRATION FAILED. Tip: You may set the Java System Variable '-DignoreDbVersion=true' to skip this error, in case you already migrate DB manually. Be carefull and have a look at the logs!\n");
					fatalExit(conn, "\nDATABASE MIGRATION REQUIRED. Please exeecute the 'runSetup' to migrate the database.", null);
				} else {
					logger.warn("-DignoreDbVersion=true is set");
				}
			}
		}
		if (productVersion.getVersionInt() < dbVersion.getVersion()) {
			logger.error("This Product-Version: " + productVersion.getVersionStr() + " (" + productVersion.getVersionInt() + "), Current DB-Version: "
					+ dbVersion.getVersionStr() + "(" + dbVersion.getVersion() + ")");
			logger.info(
					"\nDATABASE MIGRATION FAILED. Tip: You may set the Java System Variable '-DignoreDbVersion=true' to skip this error, in case you already migrate DB manually. Be carefull and have a look at the logs!\n");
			if (ignoreDbVersion == false) {
				fatalExit(conn, "This Version cannot run with a newer database version!", null);
			}
		}

		JdbcUtils.closeConnection(conn);
		conn = null;
		logger.info("Product-Version " + productVersion.getVersionStr());
		/*
		 * 
		 * Starting the cluster
		 * 
		 */
		DcemCluster dcemCluster = DcemCluster.getInstance();

		try {
			dcemCluster.startCluster(clusterConfig, nodeName);
		} catch (Exception exp) {
			fatalExit(null, "Couldn't start the Cluster", exp);
		}
		TenantIdResolver.setMasterTenant(TenantLogic.getMasterTenant(localConfig));

		/**
		 * 
		 * Loading the Module Plugins
		 */
		Method method = null;
		try {
			URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
			method.setAccessible(true);
			File pluginsDirectory = LocalPaths.getPluginsDirectory();
			logger.info("Plugins directory: " + pluginsDirectory.getAbsolutePath());
			File filesList[] = pluginsDirectory.listFiles();
			if (filesList.length == 0) {
				logger.info("No Plugins found.");
			} else {
				for (File pluginFile : filesList) {
					if (pluginFile.getName().endsWith(".jar")) {
						URL url = pluginFile.toURI().toURL();
						method.invoke(classLoader, url);
						logger.info("DCEM Plugin added: " + pluginFile.getName());
					}
				}
			}
		} catch (Exception e1) {
			logger.error("Couldn't load Plugins ", e1);
		}

		/*
		 * Initialising the Database connection
		 */
		DbFactoryProducer dbFactoryProducer = DbFactoryProducer.getInstance();

		try {
			dbFactoryProducer.createEmp(localConfig);
		} catch (Exception exp) {
			fatalExit(null, "Couldn't initialise Database ! ", exp);
		}

		dbFactoryProducer.setDatabaseMetaData(databaseMetaData);

		// ClusterConfig clusterConfig = null;
		DcemNode dcemNode = null;
		EntityManager entityManager = null;
		try {
			entityManager = dbFactoryProducer.produceUnmanagedEntitymanager();
			ConfigLogic configLogic = new ConfigLogic();
			configLogic.setEntityManager(entityManager);
			if (configLogic.verifyDbKey() == false) {
				fatalExit(null, "Wrong DB-Encryption Key.", null);
			}

			NodeLogic nodeLogic = new NodeLogic();
			nodeLogic.setEntityManager(entityManager);
			dcemNode = nodeLogic.getNodeByName(dcemCluster.getNodeName());
			if (dcemNode == null) {
				fatalExit(null, "No Node configuration found for: " + dcemCluster.getNodeName(), null);
			}
			dcemCluster.setDcemNode(dcemNode);

		} catch (PersistenceException exp) {
			if (exp.getCause() != null && exp.getCause() instanceof GenericJDBCException) {
				fatalExit(null, "Connection to Database failed.", exp);
			} else {
				fatalExit(null, "Database connection Exception.", exp);
			}

		} catch (Exception exp) {
			fatalExit(null, "Couldn't read Cluster configuration", exp);
		}
		System.setProperty("jvmRoute", dcemNode.getName());

		/*
		 * 
		 * Starting Tomcat
		 * 
		 */
		Tomcat tomcat = new Tomcat();
		tomcat.setBaseDir(".");

		String webappDirLocation = "WebContent";
		StandardContext standardContext = null;
		try {
			String webAppName = clusterConfig.getWebAppName();
			if (webAppName.charAt(0) != '/') {
				webAppName = '/' + webAppName;
			}
			standardContext = (StandardContext) tomcat.addWebapp(webAppName, new File(webappDirLocation).getAbsolutePath());
		} catch (Exception exp) {
			fatalExit(null, "Couldn't start tomcat web app: " + clusterConfig.getWebAppName() + ", from directory: " + webappDirLocation, exp);
		}
		tomcat.getHost().setAppBase(".");
		tomcat.setSilent(true);
		standardContext.setCookies(true);

		try {
			Context rootContext = (StandardContext) tomcat.addWebapp("", new File(webappDirLocation + "/custom").getAbsolutePath());
			ErrorPage errorPage = new ErrorPage();
			// errorPage.setErrorCode(HttpServletResponse.SC_NOT_FOUND);
			errorPage.setLocation("/toUserPortal.html");
			rootContext.addErrorPage(errorPage);
			StandardJarScanner jarScanner = (StandardJarScanner) rootContext.getJarScanner();
			jarScanner.setScanAllFiles(false);
			jarScanner.setScanAllDirectories(false);
			jarScanner.setScanBootstrapClassPath(false);
			jarScanner.setScanClassPath(false);
			jarScanner.setJarScanFilter(new DcemJarScanFilter());
		} catch (Exception exp) {
			fatalExit(null, "Couldn't start tomcat ROOT web app: ", exp);
		}

		// context.getServletContext().setAttribute(Globals.ALT_DD_ATTR,
		// "/WebContent/custom/web.xml");

		// Add AprLifecycleListener
		StandardServer server = (StandardServer) tomcat.getServer();
		AprLifecycleListener listener = new AprLifecycleListener();
		server.addLifecycleListener(listener);

		File additionWebInfClasses = new File("bin");
		WebResourceRoot resources = new StandardRoot(standardContext);
		resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));
		standardContext.setResources(resources);
		StandardJarScanner jarScanner = (StandardJarScanner) standardContext.getJarScanner();
		jarScanner.setScanBootstrapClassPath(true);
		jarScanner.setScanClassPath(true);
		// if this is set to true the websockets are deployed successfully
		jarScanner.setScanAllDirectories(true);
		jarScanner.setScanAllFiles(true);
		jarScanner.setJarScanFilter(new DcemJarScanFilter());
		standardContext.setAddWebinfClassesResources(true);
		standardContext.setDelegate(true);
		standardContext.getResources().setCacheObjectMaxSize(5000);
		standardContext.getResources().setCacheMaxSize(100000);

		try {
			List<Integer> portsInUseExternally = new ArrayList<Integer>();
			for (ConnectionService connectionService : clusterConfig.getConnectionServices()) {

				boolean hasSameAs = connectionService.getSameAsConnectionServiceType() != null;
				ConnectionService connectionServiceParent = (hasSameAs) ? clusterConfig.getConnectionService(connectionService.getSameAsConnectionServiceType())
						: connectionService;
				boolean enabled = connectionServiceParent.isEnabled();

				switch (connectionService.getConnectionServicesType()) {
				case MANAGEMENT:
					addFilter(standardContext, LoginWebFilter.class.getSimpleName(), LoginWebFilter.class.getName(), DcemConstants.WEB_MGT_CONTEXT + "/*", enabled);
					break;
				case SAML:
					addFilter(standardContext, DcemConstants.SAML_SERVLET_FILTER_NAME, DcemConstants.SAML_SERVLET_FILTER_CLASS,
							new String[] { DcemConstants.SAML_SERVLET_PATH + "/*" }, enabled);
					break;
				case OPENN_ID_OAUTH:
					addFilter(standardContext, DcemConstants.OAUTH_SERVLET_FILTER_NAME, DcemConstants.OAUTH_SERVLET_FILTER_CLASS,
							new String[] { DcemConstants.OAUTH_SERVLET_PATH + "/*" }, enabled);
					break;
				case USER_PORTAL:
					addFilter(standardContext, DcemConstants.USERPORTAL_SERVLET_FILTER_NAME, DcemConstants.USERPORTAL_SERVLET_FILTER_CLASS,
							DcemConstants.USERPORTAL_SERVLET_FILTER, enabled);
					addFilter(standardContext, DcemConstants.TESTMODULE_SERVLET_FILTER_NAME, DcemConstants.TESTMODULE_SERVLET_FILTER_CLASS,
							DcemConstants.TESTMODULE_SERVLET_FILTER, enabled);
					break;
				default:
					break;
				}

				if (enabled) {
					if (hasSameAs == false) {
						try {
							Connector connector = createConnector(entityManager, clusterConfig, connectionService, dcemNode);
							tomcat.getService().addConnector(connector);
							tomcat.setConnector(connector);
						} catch (Exception e) {
							logger.error("Error while adding connector for " + connectionService.getConnectionServicesType().displayName, e);
							portsInUseExternally.add(connectionService.port);
						}
					}
					switch (connectionService.getConnectionServicesType()) {
					case HEALTH_CHECK:
						addServlet(standardContext, DcemConstants.HEALTHCHECK_SERVLET_NAME, DcemConstants.HEALTHCHECK_SERVLET_CLASS,
								DcemConstants.HEALTHCHECK_SERVLET_PATH);
						break;
					case OPENN_ID_OAUTH:
						addServlet(standardContext, DcemConstants.OAUTH_SERVLET_NAME, DcemConstants.OAUTH_SERVLET_CLASS,
								new String[] { DcemConstants.OAUTH_SERVLET_PATH, DcemConstants.OAUTH_SERVLET_PATH + "/.well-known/oauth-authorization-server",
										DcemConstants.OAUTH_SERVLET_PATH + "/.well-known/openid-configuration", DcemConstants.OAUTH_SERVLET_PATH + "/userinfo",
										DcemConstants.OAUTH_SERVLET_PATH + "/jwks" });
						break;
					case SAML:
						addServlet(standardContext, DcemConstants.SAML_SERVLET_NAME, DcemConstants.SAML_SERVLET_CLASS,
								new String[] { DcemConstants.SAML_SERVLET_PATH, DcemConstants.SAML_SERVLET_PATH + "/idp_metadata.xml" });
						break;
					case USER_PORTAL:
						addServlet(standardContext, DcemConstants.USERPORTAL_SERVLET_NAME, DcemConstants.USERPORTAL_SERVLET_CLASS,
								DcemConstants.USERPORTAL_SERVLET_PATH);
						addServlet(standardContext, DcemConstants.WEBDAV_SERVLET_NAME, DcemConstants.WEBDAV_SERVLET_CLASS, DcemConstants.WEBDAV_SERVLET_PATH);
						addServlet(standardContext, DcemConstants.TEST_SP_SERVLET_NAME, DcemConstants.TEST_SP_SERVLET_CLASS, DcemConstants.TEST_SP_SERVLET_PATH);
						addServlet(standardContext, DcemConstants.LICENCE_SERVLET_NAME, DcemConstants.LICENCE_SERVLET_CLASS, DcemConstants.LICENCE_SERVLET_PATH);
						break;

					default:
						break;
					}
				}
			}
			ConfigLogic.portsInUse = portsInUseExternally;
		} catch (Exception exp) {
			fatalExit(conn, "Something went wrong during Servlet initialization.", exp);
		}

		try {
			Connector connector = createRedirectConnector(clusterConfig.getRedirectPort80());
			tomcat.getService().addConnector(connector);
			// tomcat.setConnector(connector);
		} catch (Exception exp) {
			logger.info("Redirection Port 80: " + exp.getMessage());
		}

		dbFactoryProducer.disposeUnmanagedEntityManager(entityManager);

		try {
			DcemApplicationBean.setStandardContext(standardContext);
			tomcat.getEngine().setJvmRoute(dcemNode.getName());
			tomcat.start();

		} catch (LifecycleException exp) {
			fatalExit(conn, "Couldn't start tomcat", exp);
		}
		String msg = "DCEM Application started in " + ((System.currentTimeMillis() - start) / 1000) + " seconds.";
		System.out.println(msg);
		logger.info(msg);
		tomcat.getServer().await();
	}

	private static void addServlet(StandardContext context, String servletName, String servletClass, String servletPath) {
		addServlet(context, servletName, servletClass, new String[] { servletPath });
	}

	private static void addServlet(StandardContext context, String servletName, String servletClass, String[] servletPaths) {
		try {
			Class.forName(servletClass);
			Tomcat.addServlet(context, servletName, servletClass);
			for (String path : servletPaths) {
				context.addServletMappingDecoded(path, servletName);
			}
		} catch (ClassNotFoundException e) {
			logger.info("Servlet class " + servletClass + " does not exist; will not register Servlet in Tomcat.");
		}
	}

	private static void addFilter(StandardContext context, String filterName, String filterClass, String mappingUrl, boolean enabled) {
		addFilter(context, filterName, filterClass, new String[] { mappingUrl }, enabled);
	}

	private static void addFilter(StandardContext context, String filterName, String filterClass, String[] mappingUrls, boolean enabled) {
		try {

			Class<?> klass = Class.forName(filterClass);
			DcemFilter filter = (DcemFilter) klass.newInstance();
			filter.setEnabled(enabled);

			FilterDef filterDef = new FilterDef();
			filterDef.setFilterName(filterName);
			filterDef.setFilterClass(filterClass);
			filterDef.setFilter(filter);
			context.addFilterDef(filterDef);

			FilterMap filterMap = new FilterMap();
			filterMap.setFilterName(filterName);
			for (String url : mappingUrls) {
				filterMap.addURLPattern(url);
			}
			context.addFilterMap(filterMap);

		} catch (ClassNotFoundException e) {
			logger.debug("Servlet Filter class " + filterClass + " does not exist; will not register Filter in Tomcat.");
		} catch (Exception e) {
			logger.debug("Error while setting property in Filter class.", e);
		}
	}

	static Connector createRedirectConnector(String redirectPortStr) throws Exception {
		if (redirectPortStr == null || redirectPortStr.isEmpty()) {
			throw new Exception("No Redirection configured");
		}
		int redirectPort = Integer.parseInt(redirectPortStr);
		int port = 80;
		if (isLocalPortInUse(port)) {
			throw new Exception("Port is already in use. Port=" + port);
		}
		logger.info("Creating redirectConnector for RedirectPort: " + redirectPort);
		Connector connector = new Connector("HTTP/1.1");
		connector.setPort(port);
		connector.setRedirectPort(redirectPort);
		return connector;
	}

	static Connector createConnector(EntityManager entityManager, ClusterConfig clusterConfig, ConnectionService connectorService, DcemNode dcemNode)
			throws Exception {
		int port = connectorService.getPort();
		if (isLocalPortInUse(port)) {
			throw new Exception("Port is already in use. Port=" + port + " Connection Type: " + connectorService.name);
		}
		logger.info("Creating Connector for Port: " + connectorService.getPortText());
		Connector connector = new Connector(Http11NioProtocol.class.getName());
		connector.setPort(port);
		// connector.setProtocol(Http11NioProtocol.class.getName());
		connector.setProperty("maxThreads",
				Integer.toString(TuningMaxValues.getMaxValueOf(TuningMaxValues.MGT_CONN_MAX_THREAD, clusterConfig.getScaleFactor())));
		connector.setProperty("minSpareThreads",
				Integer.toString(TuningMaxValues.getMaxValueOf(TuningMaxValues.MGT_CONN_MIN_THREAD, clusterConfig.getScaleFactor())));
		connector.setProperty("compression", "off");
		connector.setProperty("compressableMimeType", "application/json");
		connector.setProperty("maxHttpHeaderSize", "32768");
		if (connectorService.isSecure()) {
			sslConfiguration(connector, KeyStorePurpose.getKeyStorePurpose(connectorService.getConnectionServicesType()), entityManager, dcemNode);
		}
		return connector;
	}

	static private void sslConfiguration(Connector connector, KeyStorePurpose purpose, EntityManager entityManager, DcemNode dcemNode) throws Exception {
		KeyStoreLogic keyStoreLogic = new KeyStoreLogic();
		keyStoreLogic.setEntityManager(entityManager);

		KeyStoreEntity keyStoreEntity = keyStoreLogic.getKeyStoreByPurposeNode(purpose, dcemNode);
		if (keyStoreEntity == null) {
			throw new Exception("KeyStore not found in database. Purpose: " + purpose.name());
		}

		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		try {
			ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(keyStoreEntity.getKeyStore());
			keyStore.load(arrayInputStream, keyStoreEntity.getPassword().toCharArray());
			arrayInputStream.close();
			// SecureServerUtils.getCertificateRefactorAlias(keyStore, purpose,
			// keyStoreEntity.getPassword());
		} catch (Exception e) {
			logger.warn("Cannot load Keystore, please check file format and password", e);
			throw e;
		}

		File ksFile = new File(LocalPaths.getCertsDirectory(), purpose.name() + ".p12");
		FileOutputStream fos = new FileOutputStream(ksFile);
		fos.write(SecureServerUtils.serializeKeyStore(keyStore, keyStoreEntity.getPassword()));
		fos.close();
		connector.setSecure(true);
		connector.setScheme("https");
		Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
		protocol.setSSLEnabled(true);
		protocol.setKeystoreFile(ksFile.getAbsolutePath());
		// protocol.setKeyAlias(purpose.name());
		// protocol.setKeyPass(keyStoreEntity.getPassword());
		protocol.setKeystorePass(keyStoreEntity.getPassword());
		protocol.setCiphers(DcemConstants.SUPPORTED_CIPHERS);
		protocol.setSslEnabledProtocols(DcemConstants.SSL_PROTOCOLS);
	}

	/**
	 * @param tomcat
	 */
	static void stopTomcat(Tomcat tomcat) {
		if (tomcat == null || tomcat.getServer() == null) {
			return;
		}
		if (tomcat.getServer().getState() != LifecycleState.DESTROYED) {
			if (tomcat.getServer().getState() != LifecycleState.STOPPED) {
				try {
					tomcat.stop();
				} catch (LifecycleException e) {
					logger.error("Stop tomcat error.", e);
				}
			}
			try {
				tomcat.destroy();
			} catch (LifecycleException e) {
				logger.error("Destroy tomcat error.", e);
			}
		}
	}

	private static void fatalExit(Connection conn, String errorMessage, Exception exp) {
		System.err.println(errorMessage);
		logger.fatal(errorMessage, exp);
		if (conn != null) {
			JdbcUtils.closeConnection(conn);
		}
		System.err.println("DCEM stopped with FATAL ERROR!");
		logger.fatal("DCEM Stopped with a FATAL ERROR!", exp);
		System.exit(-1);
	}

	// private static Connection connectDb(LocalConfig localConfig) throws
	// SQLException {
	// DatabaseConfig dbConfig = localConfig.getDatabase();
	// return DriverManager.getConnection(dbConfig.getJdbcUrl(),
	// dbConfig.getAdminName(), dbConfig.getAdminPassword());
	// }
	//
	private static boolean isLocalPortInUse(int port) {
		try {
			// ServerSocket try to open a LOCAL port
			new ServerSocket(port).close();
			// local port can be opened, it's available
			return false;
		} catch (IOException e) {
			// local port cannot be opened, it's in use
			return true;
		}
	}

}