package com.doubleclue.dcem.core.config;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.utils.SecureServerUtils;

public class LocalConfigProvider {

	private final static Logger logger = LogManager.getLogger(LocalConfigProvider.class);

	private volatile static LocalConfig localConfig;

	private static long timeStamp;

	static public LocalConfig getLocalConfig() {
		return localConfig;
	}

	static public LocalConfig setLocalConfig(LocalConfig localConfig2) {
		return localConfig = localConfig2;
	}

	// public String getLocalNodeId() {
	// return getLocalConfig().getNode().getNodeId();
	// }

	public static LocalConfig readConfig() throws DcemException {
		File configFile = LocalPaths.getConfigurationFile();

		if (configFile.exists()) {
			if (configFile.lastModified() == timeStamp) {
				return localConfig;
			}

			try {
				timeStamp = configFile.lastModified();
				JAXBContext jc = JAXBContext.newInstance(LocalConfig.class);
				Unmarshaller unmarshaller = jc.createUnmarshaller();
				localConfig = (LocalConfig) unmarshaller.unmarshal(configFile);
							

			} catch (JAXBException e) {
				throw new DcemException(DcemErrorCodes.INVALID_CONFIGURATION_FILE, "Invalid configuration file : " + configFile.getAbsolutePath(), e);
				// localConfig = new LocalConfig();
				// logger.warn("Exception reading configuration file - " + configFile.getAbsolutePath(), e);
			}
		} else {
			throw new DcemException(DcemErrorCodes.NO_CONFIG_FILE_FOUND, "No config file found at: " + configFile.getAbsolutePath());
			// config = new LocalConfig();
		}

		try {
			String encPassword = localConfig.database.getAdminPasswordEnc();
			if (encPassword != null && encPassword.isEmpty() == false) {
				byte[] enc = java.util.Base64.getDecoder().decode(localConfig.database.getAdminPasswordEnc());
				enc = SecureServerUtils.decryptDataCommon(enc);
				localConfig.database.setAdminPassword(new String(enc, DcemConstants.CHARSET_UTF8));
			}
		} catch (Exception e1) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Couldn't encrypt admin password");
		}
		return localConfig;
	}

	/**
	 * @param configObject
	 *            is the local configuration
	 * @throws ConfigurationUtilityException
	 * @throws JAXBException
	 */
	public synchronized static void writeConfig(LocalConfig configObject) throws DcemException {

		File configFile = null;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm");
		/*
		 * Encrypt the password
		 * 
		 */
		DatabaseConfig databaseConfig = configObject.getDatabase();

		try {
			String password = databaseConfig.getAdminPassword();
			if (password != null && password.isEmpty() == false) {
				byte[] enc = SecureServerUtils.encryptDataCommon(password.getBytes(DcemConstants.CHARSET_UTF8));
				databaseConfig.setAdminPasswordEnc(java.util.Base64.getEncoder().encodeToString(enc));
			}
		} catch (Exception e1) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Couldn't encrypte admin password", e1);
		}
		try {
			configFile = LocalPaths.getConfigurationFile();
			if (configFile.exists()) {
				try {
					boolean renamed = configFile.renameTo(new File(configFile.getAbsolutePath() + "." + dateFormat.format(new Date())));
					if (renamed == false) {
						throw new Exception("Couldn't rename configuration file");
					}
				} catch (Exception e) {
					logger.error("Couldn't rename configuration file");
				}
			}

			JAXBContext jc = JAXBContext.newInstance(LocalConfig.class);
			Marshaller marshaller = jc.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.marshal(configObject, configFile);

			localConfig = configObject;

		} catch (JAXBException e) {
			throw new DcemException(DcemErrorCodes.WRITE_CONFIG_FILE, "Error writing config file - " + configFile.getAbsolutePath(), e);
		}
	}
}
