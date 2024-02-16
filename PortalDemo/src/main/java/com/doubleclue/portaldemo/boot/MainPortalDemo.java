package com.doubleclue.portaldemo.boot;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.StandardRoot;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.apache.tomcat.util.scan.StandardJarScanner;

import com.doubleclue.portaldemo.LoginPortalFilter;
import com.doubleclue.portaldemo.PortalDemoConfig;
import com.doubleclue.portaldemo.utils.ConfigUtil;
import com.doubleclue.portaldemo.utils.LogUtils;

public class MainPortalDemo {

	final static String PORTALDEMO_KEYSTORE_NAME = "portalDemo.p12";
	final static String PORTALDEM_KEYSTORE_PASSWORD = "a1b2c3d4";
	final static String PORTALDEM_KEYSTORE_ALIAS = "setup";
	final static String PORTALDEMO_DEFAULT_PORT = "8080";
	final static String CONFIG_PORTALDEMO_PORT = "PortalDemoPort";

	static String webPort;
	static String javaVersion;

	public static String getWebPort() {
		return webPort;
	}

	public static void main(String[] args) throws Exception {
		long start = System.currentTimeMillis();
		javaVersion = System.getProperty("java.version");
		System.out.println("Java Version: " + javaVersion);

		System.out.println("DCEM_HOME: " + LocalPathsPortalDemo.getDcemHomeDir());
		LogUtils.initLog4j(null, null, Level.DEBUG, false);
		final Logger logger = LogManager.getLogger(MainPortalDemo.class);
		logger.info("DCEM_HOME: " + LocalPathsPortalDemo.getDcemHomeDir());
		if (javaVersion.startsWith("9")) {
			logger.fatal("!!!  Sorry JAVA Version 9.x.x is not supported yet. !!! Please use version 1.8.xx");
			System.exit(-1);
		}

		System.setProperty("org.jboss.weld.xml.disableValidating", "true");

		Tomcat tomcat = new Tomcat();
		tomcat.setBaseDir(LocalPathsPortalDemo.getDcemHomeFile().getAbsolutePath());
		String webappDirLocation = "PortalDemoContent";
		StandardContext context = (StandardContext) tomcat.addWebapp("/portalDemo",
				new File(webappDirLocation).getAbsolutePath());

		tomcat.getHost().setAppBase(".");
		tomcat.setSilent(false);

		// Add AprLifecycleListener
		StandardServer server = (StandardServer) tomcat.getServer();
		AprLifecycleListener listener = new AprLifecycleListener();
		server.addLifecycleListener(listener);

		FilterDef filter1definition = new FilterDef();
		filter1definition.setFilterName(LoginPortalFilter.class.getSimpleName());
		filter1definition.setFilterClass(LoginPortalFilter.class.getName());
		context.addFilterDef(filter1definition);
		FilterMap filter1mapping = new FilterMap();
		filter1mapping.setFilterName(LoginPortalFilter.class.getSimpleName());
		filter1mapping.addURLPattern("/*");
		context.addFilterMap(filter1mapping);

//		File additionWebInfClasses = new File("bin");
		WebResourceRoot resources = new StandardRoot(context);
//		resources.addPreResources(
//				new DirResourceSet(resources, "/WEB-INF/classes", additionWebInfClasses.getAbsolutePath(), "/"));
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
			connector = createConnector();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			System.exit(-1);
		}
		logger.info("URL : http://" + InetAddress.getLocalHost().getHostName() + ":" + connector.getPort() + "/portalDemo");

		PortalDemoConfig config = ConfigUtil.getPortalDemoConfig();
				
		if (config.getCookieRoute() != null && config.getCookieRoute().isEmpty() == false) {
			tomcat.getEngine().setJvmRoute(config.getCookieRoute());
			System.setProperty("jvmRoute", config.getCookieRoute());
		}
		tomcat.getService().addConnector(connector);
		tomcat.setConnector(connector);		
		tomcat.start();

		logger.info("PortalDemo started in " + ((System.currentTimeMillis() - start) / 1000) + " seconds.");
		String url = "http://" + InetAddress.getLocalHost().getHostName() + ":" + connector.getPort() + "/portalDemo";
		System.out.println("URL : " + url);

		tomcat.getServer().await();

	}

	static Connector createConnector() throws Exception {
		webPort = System.getProperty(CONFIG_PORTALDEMO_PORT);
		if (webPort == null || webPort.isEmpty()) {
			webPort = PORTALDEMO_DEFAULT_PORT;
		}
		int port = Integer.valueOf(webPort);
		if (isLocalPortInUse(port)) {
			throw new Exception("Web Port is already in use. Port=" + port);
		}
		Connector connector = new Connector();
		connector.setPort(port);
		connector.setProperty("compression", "off");
		return connector;
	}

	// static Connector createSslConnector() throws Exception {
	//
	// File file = new File(LocalPathsPortalDemo.getCertsDirectory(),
	// PORTALDEMO_KEYSTORE_NAME);
	// if (file.exists() == false) {
	// String password = "a1b2c3d4";
	// KeyStore keyStore = SecureServerUtils.createKeyStore(1024, "cn=localhost",
	// (X509Certificate[]) null,
	// (String) null, password.toCharArray(), null, SETUP_KEYSTORE_ALIAS, null);
	// keyStore.store(new FileOutputStream(file), password.toCharArray());
	// }
	//
	// webPort = System.getProperty(CONFIG_SETUP_PORT);
	// if (webPort == null || webPort.isEmpty()) {
	// webPort = SETUP_DEFAULT_PORT;
	// }
	// int port = Integer.valueOf(webPort);
	// if (isLocalPortInUse(port)) {
	// throw new Exception("Port is already in use. Port=" + port);
	// }
	// Connector connector = new Connector();
	// connector.setPort(port);
	// // connector.setProtocol(Http11NioProtocol.class.getName());
	// connector.setScheme("https");
	// connector.setAttribute("keyAlias", SETUP_KEYSTORE_ALIAS);
	// connector.setAttribute("keystorePass", SETUP_KEYSTORE_PASSWORD);
	// connector.setAttribute("keystoreFile", file.getAbsolutePath());
	// connector.setAttribute("clientAuth", "false");
	// connector.setProperty("connectionTimeout", "8000");
	// connector.setAttribute("sslProtocol", "TLS");
	// connector.setAttribute("SSLEnabled", true);
	// return connector;
	// }

	// static void startBrowser(String urlStr) throws Exception {
	//
	// if (Desktop.isDesktopSupported()) {
	// Desktop desktop = Desktop.getDesktop();
	// desktop.browse(new URI(urlStr));
	//
	// } else {
	// if (SystemUtils.IS_OS_WINDOWS) {
	// Runtime rt = Runtime.getRuntime();
	// rt.exec("rundll32 url.dll,FileProtocolHandler " + urlStr);
	// }
	//
	// if (SystemUtils.IS_OS_LINUX) {
	// Runtime rt = Runtime.getRuntime();
	// String[] browsers = { "epiphany", "firefox", "mozilla", "konqueror",
	// "netscape", "opera", "links",
	// "lynx" };
	//
	// StringBuffer cmd = new StringBuffer();
	// for (int i = 0; i < browsers.length; i++)
	// cmd.append((i == 0 ? "" : " || ") + browsers[i] + " \"" + urlStr + "\" ");
	//
	// rt.exec(new String[] { "sh", "-c", cmd.toString() });
	// }
	// }
	// }

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