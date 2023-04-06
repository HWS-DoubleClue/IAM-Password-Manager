package com.doubleclue.dcem.core.logic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ClusterConfig;
import com.doubleclue.dcem.core.config.ConnectionService;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.config.KeyStorePurpose;
import com.doubleclue.dcem.core.entities.DcemConfiguration;
import com.doubleclue.dcem.core.entities.DcemNode;
import com.doubleclue.dcem.core.entities.KeyStoreEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.core.tasks.SavePreferencesTask;
import com.doubleclue.dcem.system.logic.KeyStoreLogic;
import com.doubleclue.dcem.system.logic.NodeLogic;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.IExecutorService;

/**
 * @author emanuel.galea
 *
 */
/**
 * @author emanuel.galea
 *
 */
@ApplicationScoped
@Named("configLogic")
public class ConfigLogic {

	private static final Logger logger = LogManager.getLogger(ConfigLogic.class);

	public static final byte[] DB_VERIFICATION = { (byte) 0x30, (byte) 0x32, (byte) 0x53, (byte) 0x40, (byte) 0x55, (byte) 0x50, (byte) 0x55, (byte) 0x73,
			(byte) 0x55, (byte) 0x20, (byte) 0xF5, (byte) 0x22, (byte) 0x55, (byte) 0x7E, (byte) 0xE4, (byte) 0x00 };

	@Inject
	EntityManager entityManager;

	@Inject
	KeyStoreLogic keyStoreLogic;

	@Inject
	NodeLogic nodeLogic;

	@Inject
	DcemReportingLogic reportingLogic;

	ClusterConfig clusterConfig;

	public static List<Integer> portsInUse = new ArrayList<Integer>();

	/**
	 * This method verifies that the DB-Key is OK. This is call at startup
	 * @return
	 * @throws DcemException
	 */
	public boolean verifyDbKey() throws Exception {
		DcemConfiguration semConfiguration = getDcemConfiguration(SystemModule.MODULE_ID, DcemConstants.CONFIG_KEY_DB_VERIFICATION);
		if (semConfiguration == null) {
			return false;
		}
		if (Arrays.equals(DB_VERIFICATION, semConfiguration.getValue())) {
			return true;
		}
		return false;
	}

	public ClusterConfig getClusterConfig() throws DcemException {
		if (clusterConfig != null) {
			return clusterConfig;
		}
		try {
			DcemConfiguration semConfiguration = getDcemConfiguration(SystemModule.MODULE_ID, DcemConstants.CONFIG_KEY_CLUSTER_CONFIG);
			if (semConfiguration == null) {
				return null;
			}
			this.clusterConfig = new ObjectMapper().readValue(semConfiguration.getValue(), ClusterConfig.class);
			return clusterConfig;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.NO_CONNECTION_CONFIG, "No Cluster Configuration found", e);
		}
	}

	public ModulePreferences getModulePreferences(String moduleId, Class<?> klass) throws DcemException {
		DcemConfiguration semConfiguration = null;
		try {
			semConfiguration = getDcemConfiguration(moduleId, DcemConstants.CONFIG_KEY_PREFERENCES);
			if (semConfiguration == null) {
				return null;
			}
			ByteArrayInputStream input;
			input = new ByteArrayInputStream(semConfiguration.getValue());

			JAXBContext jc = JAXBContext.newInstance(klass);
			Unmarshaller unmarshaller = jc.createUnmarshaller();
			return (ModulePreferences) unmarshaller.unmarshal(input);
		} catch (UnmarshalException exp) {
			System.out.println("ConfigLogic.getModulePreferences() " + new String (semConfiguration.getValue()));
			throw new DcemException(DcemErrorCodes.INVALID_PREFERENCES_FORMAT, "No Connection Configuration found", exp);
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.NO_CONNECTION_CONFIG, "No Connection Configuration found", e);
		}
	}

	@DcemTransactional
	public void setModulePreferences(String moduleId, ModulePreferences modulePreferences) throws DcemException {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			JAXBContext jc = JAXBContext.newInstance(modulePreferences.getClass());
			Marshaller marshaller = jc.createMarshaller();
			marshaller.marshal(modulePreferences, bos);

			DcemConfiguration semConfiguration = getDcemConfiguration(moduleId, DcemConstants.CONFIG_KEY_PREFERENCES);
			if (semConfiguration == null) {
				semConfiguration = new DcemConfiguration();
				semConfiguration.setModuleId(moduleId);
				semConfiguration.setKey(DcemConstants.CONFIG_KEY_PREFERENCES);
			}
			semConfiguration.setValue(bos.toByteArray());
			setDcemConfiguration(semConfiguration);
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.NO_CONNECTION_CONFIG, "No Connection Configuration found", e);
		}
	}

	/**
	 * @param moduleId
	 * @param key
	 * @return
	 * @throws DcemException
	 */
	public DcemConfiguration getDcemConfiguration(String moduleId, String key) throws DcemException {
		TypedQuery<DcemConfiguration> query = entityManager.createNamedQuery(DcemConfiguration.MODULE_KEY, DcemConfiguration.class);
		query.setParameter("moduleId", moduleId);
		query.setParameter("key", key);
		try {
			return query.getSingleResult();
		} catch (NoResultException exp) {
			logger.debug("Config not found. " + moduleId + ":" + key);
			return null;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.READ_CONFIG, "Error reading config" + moduleId + ":" + key, e);
		}
	}

	public List<DcemConfiguration> getDcemConfigurations(String key) throws DcemException {
		TypedQuery<DcemConfiguration> query = entityManager.createNamedQuery(DcemConfiguration.KEY_ONLY, DcemConfiguration.class);
		query.setParameter("key", key);
		try {
			List<DcemConfiguration> semConfigurations = query.getResultList();
			if (semConfigurations == null) {
				return null;
			}
			return semConfigurations;

		} catch (NoResultException exp) {
			logger.debug("Configs not found for key: " + key);
			return null;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.READ_CONFIG, "Error reading configs for key: " + key, e);
		}
	}

	public DcemConfiguration createClusterConfig(ClusterConfig clusterConfig) throws DcemException {

		/* 
		 * Check if KEystores are available 
		 */
		List<DcemNode> nodeList = nodeLogic.getNodes();
		for (ConnectionService connectionService : clusterConfig.getConnectionServices()) {
			if (connectionService.isEnabled() && connectionService.isSecure() && connectionService.getSameAsConnectionServiceType() == null) {
				List<KeyStoreEntity> list = keyStoreLogic
						.getKeyStoreByPurpose(KeyStorePurpose.getKeyStorePurpose(connectionService.getConnectionServicesType()));
				if (nodeList.size() > list.size()) {
					throw new DcemException(DcemErrorCodes.CREATE_KEYSTORE_FOR_NODES,
							"Please create KeyStores for " + connectionService.getConnectionServicesType().displayName);
				}
			}
		}

		byte[] value;
		try {
			value = new ObjectMapper().writeValueAsBytes(clusterConfig);
		} catch (JsonProcessingException e) {
			throw new DcemException(DcemErrorCodes.SERIALIZATION_ERROR, ClusterConfig.class.getName());
		}

		DcemConfiguration dcemConfiguration = getDcemConfiguration(SystemModule.MODULE_ID, DcemConstants.CONFIG_KEY_CLUSTER_CONFIG);
		if (dcemConfiguration == null) {
			dcemConfiguration = new DcemConfiguration(SystemModule.MODULE_ID, DcemConstants.CONFIG_KEY_CLUSTER_CONFIG, value);
			logger.debug("Create new DcemConfiguration " + dcemConfiguration.toString());
		} else {
			dcemConfiguration.setValue(value);
		}
		return dcemConfiguration;
	}

	@DcemTransactional
	public void setDcemConfiguration(String moduleId, String key, byte[] data) throws DcemException {
		DcemConfiguration semConfiguration = getDcemConfiguration(moduleId, key);
		if (semConfiguration == null) {
			semConfiguration = new DcemConfiguration(moduleId, key, data);
		} else {
			semConfiguration.setValue(data);
		}
		setDcemConfiguration(semConfiguration);
	}

	/**
	 * @param dcemConfiguration
	 * @throws DcemException
	 */
	@DcemTransactional
	public void setDcemConfiguration(DcemConfiguration dcemConfiguration) throws DcemException {

		try {
			if (dcemConfiguration.getId() != null) {
				logger.debug("updating DcemConfiguration " + dcemConfiguration.toString());
				dcemConfiguration = entityManager.merge(dcemConfiguration);
			} else {
				entityManager.persist(dcemConfiguration);
				logger.debug("persist DcemConfiguration " + dcemConfiguration.toString());
			}
			return;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.READ_CONFIG, "Error writing config" + dcemConfiguration.toString(), e);
		}
	}

	public DcemConfiguration getDbVerification() throws DcemException {
		DcemConfiguration dcemConfiguration = getDcemConfiguration(SystemModule.MODULE_ID, DcemConstants.CONFIG_KEY_DB_VERIFICATION);
		if (dcemConfiguration == null) {
			logger.info("creating DB-Key");
			dcemConfiguration = new DcemConfiguration(SystemModule.MODULE_ID, DcemConstants.CONFIG_KEY_DB_VERIFICATION, DB_VERIFICATION);
		} else {
			logger.info("Updating DB-Key !");
			dcemConfiguration.setValue(DB_VERIFICATION);
		}
		return dcemConfiguration;
	}

	/**
	 * This is used during Application start, as at the time we con't have CDI
	 * @param entityManager
	 */
	public void setEntityManager(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@DcemTransactional
	public void setModulePreferencesInCluster(String moduleId, ModulePreferences modulePreferencesPrevious, ModulePreferences modulePreferencesNew)
			throws DcemException {
		modulePreferencesNew.incrementVersion();
		setModulePreferences(moduleId, modulePreferencesNew);
		IExecutorService executorService = DcemCluster.getInstance().getExecutorService();
		SavePreferencesTask savePreferences = new SavePreferencesTask(TenantIdResolver.getCurrentTenantName(), moduleId, modulePreferencesPrevious,
				modulePreferencesNew);
		executorService.executeOnAllMembers(savePreferences);
	}

	public void reloadPortInUseAlerts() {
		ClusterConfig clusterConfig = DcemCluster.getInstance().getClusterConfig();
		for (ConnectionService connectionService : clusterConfig.getConnectionServices()) {
			ConnectionServicesType sameAs = connectionService.getSameAsConnectionServiceType();
			int port = sameAs == null ? connectionService.getPort() : clusterConfig.getConnectionService(sameAs).getPort();
			if (portsInUse.contains(port)) {
				try {
					reportingLogic.addWelcomeViewAlert(DcemConstants.ALERT_CATEGORY_DCEM, DcemErrorCodes.PORT_IN_USE, connectionService.getName(),
							AlertSeverity.ERROR, true, port);
				} catch (Exception e) {
					logger.debug(e);
				}
			}
		}
	}
}
