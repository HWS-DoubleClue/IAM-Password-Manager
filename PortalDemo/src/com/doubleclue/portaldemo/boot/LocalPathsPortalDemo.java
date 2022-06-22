package com.doubleclue.portaldemo.boot;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;


/**
 * Provides a SettingsProducer
 * 
 * @author Emanuel Galea
 */
public class LocalPathsPortalDemo {

	public static final String ENV_DCEM_HOME = "DCEM_HOME";
	public static final String ENV_DCEM_LOGS = "DCEM_LOGS";
	public static final String ENV_DCEM_INSTALL = "DCEM_INSTALL";
	public static final String CERTS = "certs";
	public static final String LOGS = "logs";
	// private static final String CLUSTER_CONFIGURATION_FILE = "cluster.xml";

	public static final char[] DEFAULT_PWD = new char[] { '1', '2', '3', '4', '5', '6' };

	private static File dcemHomeDir;

	public static File getDcemHomeFile () {
		return dcemHomeDir;
	}

	public static File getDcemHomeDir() throws Exception {
		if (dcemHomeDir == null) {
			dcemHomeDir = createDcemHomeDir();
		}

		return dcemHomeDir;
	}

	
	
	
	/**
	 * @return
	 * @throws Exception
	 */
	private static File createDcemHomeDir() throws Exception {
		File dcemHomeFile = null;

		/*
		 * first check for java property, so that we easily can overwrite this
		 * (also in unit tests, environment variables are hard to overwrite :) )
		 */
		if (System.getProperty(ENV_DCEM_HOME) != null) {
			Path path = Paths.get(System.getProperty(ENV_DCEM_HOME));
			dcemHomeFile = path.toFile();
			// logger.info("DCEM_HOME specified by java global property >> (" + semHomeFile.getAbsolutePath() + ")");
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
			throw new Exception("specified DCEM_HOME is not a directory: " + dcemHomeFile.getAbsolutePath());
		}

		if (System.getProperty(ENV_DCEM_LOGS) == null) {
			System.setProperty(ENV_DCEM_LOGS, new File(dcemHomeFile, LOGS).getAbsolutePath());
		}
		LogManager.getLogger(LocalPathsPortalDemo.class);

		return dcemHomeFile;
	}
	
	public static File getDcemLogDir () {
		return new File(dcemHomeDir, LOGS);
	}
	
	public static File getCertsDirectory() throws Exception {
		File certs = new File(LocalPathsPortalDemo.getDcemHomeDir(), CERTS);
		if (certs.exists() == false) {
			certs.mkdirs();
		}
		return certs;
	}

	

}
