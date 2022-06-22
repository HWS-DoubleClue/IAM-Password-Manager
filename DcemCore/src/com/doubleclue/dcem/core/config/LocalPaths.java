package com.doubleclue.dcem.core.config;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;

/**
 * Provides a SettingsProducer
 * 
 * @author Emanuel Galea
 */
public class LocalPaths {

	public static final String ENV_DCEM_HOME = "DCEM_HOME";
	public static final String DERBY_HOME = "EmbeddedDB";
	public static final String CLOUDSAFE_HOME = "CloudSafeStorage";

	public static final String DERBY_SCHEMA = "dcem_db";
	public static final String ENV_DCEM_LOGS = "DCEM_LOGS";
	public static final String ENV_DCEM_INSTALL = "DCEM_INSTALL";
	public static final String CERTS = "certs";
	public static final String PLUGINS = "Plugins";
	public static final String ARCHIVE = "archive";
	public static final String DBDRIVERS = "dbdrivers";
	private static final String CONFIGURATION_FILE = "configuration.xml";
	private static final String SDK_CONFIG_FILE = "cachedSdkConfig.dcem";
	private static final String HAZELCAST_CLUSTER_CONFIG_FILE = "HazelcastClusterConfig.xml";
	private static final String HAZELCAST_CLUSTER_CONFIG_FILE_X = "x_HazelcastClusterConfig.xml";
	private static final String LOG4J_CONFIG_FILE = "log4j2.xml";

	public static final String LOGS = "logs";
	// private static final String CLUSTER_CONFIGURATION_FILE = "cluster.xml";

	public static final String RC_TRUSTSTORE = "truststore.jks";
	public static final char[] DEFAULT_PWD = new char[] { '1', '2', '3', '4', '5', '6' };

	// private static Logger logger;

	private static File dcemHomeDir;

	private static File dcemInstallDir;

	public static File getDcemHomeFile() {
		return dcemHomeDir;
	}

	public static File getDcemHomeDir() throws DcemException {
		if (dcemHomeDir == null) {
			dcemHomeDir = createDcemHomeDir();
		}
		return dcemHomeDir;
	}

	/**
	 * @return
	 * @throws DcemException
	 */
	private static File createDcemHomeDir() throws DcemException {
		File dcemHomeFile = null;

		/*
		 * first check for java property, so that we easily can overwrite this (also in
		 * unit tests, environment variables are hard to overwrite :) )
		 */
		if (System.getProperty(ENV_DCEM_HOME) != null) {
			Path path = Paths.get(System.getProperty(ENV_DCEM_HOME));
			dcemHomeFile = path.toFile();
			// logger.info("SEM_HOME specified by java global property >> (" + semHomeFile.getAbsolutePath() + ")");
		} else if (System.getenv(ENV_DCEM_HOME) != null) {
			System.out.println("DCEM_HOME environment found at: " + System.getenv(ENV_DCEM_HOME));
			Path path = Paths.get(System.getenv(ENV_DCEM_HOME));
			dcemHomeFile = path.toFile();
			System.out.println("DCEM_HOME file at: " + dcemHomeFile.getAbsolutePath());
			if (dcemHomeFile.exists() == false) {
				dcemHomeFile.mkdirs();
			}
		} else {
			dcemHomeFile = new File(System.getProperty("user.home"));
			dcemHomeFile = new File(dcemHomeFile, ENV_DCEM_HOME);
			if (dcemHomeFile.exists() == false) {
				dcemHomeFile.mkdirs();
			}

			System.setProperty(ENV_DCEM_HOME, dcemHomeFile.getAbsolutePath());
		}

		if (dcemHomeFile.exists() == false) {
			throw new RuntimeException("specified DCEM_HOME does not exist: " + dcemHomeFile.getAbsolutePath());
		}
		if (dcemHomeFile.isDirectory() == false) {
			throw new DcemException(DcemErrorCodes.CONFIGURE_HOME_DIRECTORY,
					"specified DCEM_HOME is not a directory: " + dcemHomeFile.getAbsolutePath());
		}

		if (System.getProperty(ENV_DCEM_LOGS) == null) {
			System.setProperty(ENV_DCEM_LOGS, new File(dcemHomeFile, LOGS).getAbsolutePath());
		}
		return dcemHomeFile;
	}

	public static File getDcemLogDir() {
		return new File(dcemHomeDir, LOGS);
	}

	/**
	 * @return
	 * @throws DcemException
	 */
	public static File getDcemInstallDir() throws DcemException {
		if (dcemInstallDir != null) {
			return dcemInstallDir;
		}
		URL pathUrl = LocalPaths.class.getProtectionDomain().getCodeSource().getLocation();
		File filePath;
		try {
			filePath = new File(pathUrl.toURI().getPath());
			if (filePath.isDirectory() == false) {
				String path = filePath.getPath();
				int ind = path.lastIndexOf(File.separator + "bin");
				if (ind != -1) {
					path = path.substring(0, ind);
					filePath = new File(path);
				} else {
					throw new DcemException(DcemErrorCodes.CONFIGURE_INSTALL_DIRECTORY, "Couldn't get the install path");
				}
			}
			dcemInstallDir = filePath;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Could get Installation path", e);
		}
		return dcemInstallDir;
	}

	public static File getCertsDirectory() throws DcemException {
		File certs = new File(LocalPaths.getDcemHomeDir(), CERTS);
		if (certs.exists() == false) {
			certs.mkdirs();
		}
		return certs;
	}

	public static File getPluginsDirectory() throws DcemException {
		File certs = new File(LocalPaths.getDcemInstallDir(), PLUGINS);
		if (certs.exists() == false) {
			certs.mkdirs();
		}
		return certs;
	}

	public static File getArchiveDirectory() throws DcemException {
		File archive = new File(LocalPaths.getDcemHomeDir(), ARCHIVE);
		if (archive.exists() == false) {
			archive.mkdirs();
		}
		return archive;
	}

	public static File getConfigurationFile() throws DcemException {
		return new File(LocalPaths.getDcemHomeDir(), CONFIGURATION_FILE);
	}

	public static File getCacheSdkConfigFile() throws DcemException {
		return new File(LocalPaths.getDcemHomeDir(), SDK_CONFIG_FILE);
	}

	public static File getClusterConfig() throws DcemException {
		return new File(LocalPaths.getDcemHomeDir(), HAZELCAST_CLUSTER_CONFIG_FILE);
	}

	public static File getLog4JConfig() throws DcemException {
		return new File(LocalPaths.getDcemHomeDir(), LOG4J_CONFIG_FILE);
	}

	public static File getClusterConfigX() throws DcemException {
		return new File(LocalPaths.getDcemHomeDir(), HAZELCAST_CLUSTER_CONFIG_FILE_X);
	}

	public static File getDerbyDirectory() throws DcemException {
		return new File(LocalPaths.getDcemHomeDir(), DERBY_HOME);
	}

	public static File getCloudSafeStorageDirectory() throws DcemException {
		return new File(LocalPaths.getDcemHomeDir(), CLOUDSAFE_HOME);
	}

}
