package com.doubleclue.dcem.setup;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URI;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.sql.Connection;
import java.util.Locale;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.commons.lang3.SystemUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.scan.StandardJarScanner;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.doubleclue.dcem.core.DcemJarScanFilter;
import com.doubleclue.dcem.core.config.LocalConfig;
import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.JdbcUtils;
import com.doubleclue.dcem.core.logging.DcemLogLevel;
import com.doubleclue.dcem.core.logging.LogUtils;
import com.doubleclue.dcem.core.utils.SecureServerUtils;

public class MainSetup {

	public static LocalConfig localConfig;

	final static String SETUP_KEYSTORE_NAME = "setup.p12";
	final static String SETUP_KEYSTORE_PASSWORD = "a1b2c3d4";
	final static String SETUP_KEYSTORE_ALIAS = "setup";
	final static String SETUP_DEFAULT_PORT = "8443";
	final static String CONFIG_SETUP_PORT = "DcemSetupPort";

	static String webPort;
	static String javaVersion;

	static Logger logger;

	public static String getWebPort() {
		return webPort;
	}

	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		javaVersion = System.getProperty("java.version");
		System.out.println("Java Version: " + javaVersion);
		Security.addProvider(new BouncyCastleProvider());

		Locale.setDefault(Locale.ENGLISH);
		Locale.setDefault(Locale.Category.DISPLAY, Locale.ENGLISH);
		try {
			LocalPaths.getDcemHomeDir();
		} catch (DcemException e) {
			System.err.println("ERROR: Couldn't set the Application Home Directory, Please set 'DCEM_HOME' in enviorment or as System-Parameter");
			System.exit(-1);
		}
		boolean fromEclipse = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;
		String debug = System.getProperty("debugLog");
		DcemLogLevel dcemLogLevel = DcemLogLevel.INFO;
		if (debug != null && debug.equals("true")) {
			dcemLogLevel = DcemLogLevel.DEBUG;
			DcemApplicationBean.debugMode = true;
		}
		LogUtils.initLog4j(null, null, dcemLogLevel, fromEclipse);
		logger = LogManager.getLogger(MainSetup.class);
		logger.info("DCEM_HOME: " + LocalPaths.getDcemHomeDir());

		System.out.println("Install Directory: " + LocalPaths.getDcemInstallDir());
		if (SystemUtils.IS_OS_WINDOWS) {
			System.setProperty("javax.net.ssl.trustStoreType", "Windows-ROOT");
		}

		LocalConfig config;
		try {
			config = LocalConfigProvider.readConfig();
		} catch (Exception e) {
			config = new LocalConfig();
			config.setDefaults();
			try {
				LocalConfigProvider.writeConfig(config);

			} catch (Exception e2) {
				fatalExit(null, -1, "Couldn't write the new created configuration file.", e2);
			}
			logger.info("New local configuration file created");
		}
		System.setProperty("org.jboss.weld.xml.disableValidating", "true");
		localConfig = config;
		Tomcat tomcat = new Tomcat();
		tomcat.setBaseDir(LocalPaths.getDcemHomeFile().getAbsolutePath());
		String webappDirLocation = "SetupContent";
		StandardContext context = (StandardContext) tomcat.addWebapp("/setup", new File(webappDirLocation).getAbsolutePath());

		tomcat.getHost().setAppBase(".");
		tomcat.setSilent(false);

		// Add AprLifecycleListener
		StandardServer server = (StandardServer) tomcat.getServer();
		AprLifecycleListener listener = new AprLifecycleListener();
		server.addLifecycleListener(listener);

		FilterDef filter1definition = new FilterDef();
		filter1definition.setFilterName(SetupWebFilter.class.getSimpleName());
		filter1definition.setFilterClass(SetupWebFilter.class.getName());
		context.addFilterDef(filter1definition);
		FilterMap filter1mapping = new FilterMap();
		filter1mapping.setFilterName(SetupWebFilter.class.getSimpleName());
		filter1mapping.addURLPattern("/*");
		context.addFilterMap(filter1mapping);

		// File additionWebInfClasses = new File("bin");
		WebResourceRoot resources = new StandardRoot(context);
		// resources.addPreResources(
		// new DirResourceSet(resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));
		resources.setCachingAllowed(false);

		context.setResources(resources);

		StandardJarScanner jarScanner = (StandardJarScanner) context.getJarScanner();
		jarScanner.setScanBootstrapClassPath(true);
		jarScanner.setScanClassPath(true);
		jarScanner.setJarScanFilter(new DcemJarScanFilter());

		context.setAddWebinfClassesResources(true);
		context.setDelegate(true);
		Connector connector = null;
		try {
			connector = createSslConnector();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-4);
		}
		logger.info("URL : https://" + InetAddress.getLocalHost().getHostName() + ":" + connector.getPort() + "/setup");

		tomcat.getService().addConnector(connector);
		tomcat.setConnector(connector);

		tomcat.start();

		logger.info("Setup started in " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
		String url = "https://" + InetAddress.getLocalHost().getHostName() + ":" + connector.getPort() + "/setup?start=yes";
		System.out.println("URL : " + url);
		try {
			startBrowser(url);
			System.out.println("Default Browser started...");
		} catch (Throwable e) {
			System.out.println("Coundn't start default browser! Please start your browser and enter following URL:");
			System.out.println();
			System.out.println(">>> " + url);
			System.out.println();
			try {
				StringSelection selec = new StringSelection(url);
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				clipboard.setContents(selec, selec);
				System.out.println("URL is copied to clipboard");
			} catch (Throwable t) {
				logger.debug("Could put url in clipboards", t);
			}
			logger.debug("Coundn't start default browser", e);
		}
		tomcat.getServer().await();

	}

	static Connector createSslConnector() throws Exception {

		File file = new File(LocalPaths.getCertsDirectory(), SETUP_KEYSTORE_NAME);
		if (file.exists() == false) {
			String password = "a1b2c3d4";
			KeyStore keyStore = SecureServerUtils.createKeyStore(1024, "cn=localhost", (X509Certificate[]) null, (String) null, password.toCharArray(), null,
					SETUP_KEYSTORE_ALIAS, null);
			keyStore.store(new FileOutputStream(file), password.toCharArray());
		}

		webPort = System.getProperty(CONFIG_SETUP_PORT);
		if (webPort == null || webPort.isEmpty()) {
			webPort = SETUP_DEFAULT_PORT;
		}
		int port = Integer.valueOf(webPort);
		if (isLocalPortInUse(port)) {
			throw new Exception("Port is already in use. Port=" + port);
		}
		Connector connector = new Connector();
		connector.setPort(port);
		// connector.setProtocol(Http11NioProtocol.class.getName());
		connector.setScheme("https");
		connector.setProperty("keyAlias", SETUP_KEYSTORE_ALIAS);
		connector.setProperty("keystorePass", SETUP_KEYSTORE_PASSWORD);
		connector.setProperty("keystoreFile", file.getAbsolutePath());
		connector.setProperty("clientAuth", "false");
		connector.setProperty("connectionTimeout", "8000");
		connector.setProperty("sslProtocol", "TLS");
		connector.setProperty("SSLEnabled", String.valueOf(true));
		return connector;
	}

	static void startBrowser(String urlStr) throws Throwable {
		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			desktop.browse(new URI(urlStr));
		} else {
			if (SystemUtils.IS_OS_WINDOWS) {
				Runtime rt = Runtime.getRuntime();
				rt.exec("rundll32 url.dll,FileProtocolHandler " + urlStr);
			}
			if (SystemUtils.IS_OS_LINUX) {
				Runtime rt = Runtime.getRuntime();
				String[] browsers = { "epiphany", "firefox", "mozilla", "konqueror", "netscape", "opera", "links", "lynx" };

				StringBuffer cmd = new StringBuffer();
				for (int i = 0; i < browsers.length; i++)
					cmd.append((i == 0 ? "" : " || ") + browsers[i] + " \"" + urlStr + "\" ");

				rt.exec(new String[] { "sh", "-c", cmd.toString() });
			}
		}
	}

	private static void fatalExit(Connection conn, int exitCode, String errorMessage, Exception exp) {

		System.err.println(errorMessage);
		if (exitCode < 0) {
			logger.fatal(errorMessage, exp);
			System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.err.println();
			System.err.println("DCEM Setup stopped with FATAL ERROR! " + errorMessage);
			System.err.println();
			System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			logger.fatal("DCEM Stopped with a FATAL ERROR!", exp);
		} else {
			System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			System.err.println();
			System.err.println(errorMessage);
			System.err.println();
			System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		if (conn != null) {
			JdbcUtils.closeConnection(conn);
		}
		System.exit(exitCode);
	}

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