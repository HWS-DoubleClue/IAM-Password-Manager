package com.doubleclue.dcem.setup.gui;

import java.io.File;
import java.sql.Connection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.cloudsafe.CloudSafeContentDb;
import com.doubleclue.dcem.as.logic.cloudsafe.CloudSafeContentI;
import com.doubleclue.dcem.as.logic.cloudsafe.CloudSafeContentNas;
import com.doubleclue.dcem.as.logic.cloudsafe.CloudSafeContentS3;
import com.doubleclue.dcem.core.config.CloudSafeStorageType;
import com.doubleclue.dcem.core.config.ClusterConfig;
import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.config.LocalConfig;
import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.entities.DcemConfiguration;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.DbEncryption;
import com.doubleclue.dcem.core.jpa.DbFactoryProducer;
import com.doubleclue.dcem.core.jpa.EntityManagerProducer;
import com.doubleclue.dcem.core.jpa.JdbcUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.TenantLogic;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.doubleclue.dcem.core.weld.WeldSessionContext;
import com.doubleclue.dcem.setup.logic.CallCloudSafeStorageCopy;
import com.doubleclue.dcem.setup.logic.DbLogic;

@SuppressWarnings("serial")
@Named("setupCloudSafe")
@SessionScoped
public class SetupCloudSafe extends DcemView {

	LocalConfig localConfig;

	EntityManagerProducer emp = null;
	WeldRequestContext requestContext = null;
	WeldSessionContext weldSessionContext = null;

	@Inject
	DbLogic dbLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	TenantLogic tenantLogic;

	@Inject
	ConfigLogic configLogic;

	@Inject
	TaskExecutor taskExecutor;

	DatabaseConfig dbConfig;

	String cloudStorageType;
	String currentCloudStorageType;

	String nodeName;
	String nasPath;

	String s3AccessKeyId;
	String s3SecretAccessKey;
	String s3Url;
	boolean copyFileContent;

	ClusterConfig clusterConfig;

	@PostConstruct
	protected void init() {
		try {
			localConfig = LocalConfigProvider.readConfig();
			dbConfig = localConfig.getDatabase();
			Connection connection;
			connection = JdbcUtils.getJdbcConnectionWithSchema(dbConfig, null, null);
			clusterConfig = JdbcUtils.getClusterConfigbyJdbc(connection);
			connection.close();
			currentCloudStorageType = clusterConfig.getCloudSafeStorageType().name();
			cloudStorageType = currentCloudStorageType;
			s3AccessKeyId = clusterConfig.getAwsS3AccesskeyId();
			s3SecretAccessKey = clusterConfig.getAwsS3SecretAccessKey();
			copyFileContent = true;
			s3Url = clusterConfig.getAwsS3Url();
			if (s3Url == null) {
				s3Url = CloudSafeContentS3.DIGITAL_OCEAN_SPACES_URL;
			}
		} catch (Exception e) {
			logger.error("Couldn't read Cluster configuration file", e);
			return;
		}
	}

	public List<SelectItem> getCloudStorageTypes() {
		List<SelectItem> selectItems = new LinkedList<SelectItem>();
		for (CloudSafeStorageType cloudStorageType : CloudSafeStorageType.values()) {
			selectItems.add(new SelectItem(cloudStorageType.name(), cloudStorageType.name()));
		}
		return selectItems;
	}

	/**
	 * @throws Exception 
	 * 
	 */
	public void actionSave() throws Exception {
		CloudSafeStorageType selectedType = CloudSafeStorageType.valueOf(cloudStorageType);
		CloudSafeStorageType currentType = clusterConfig.getCloudSafeStorageType();
		switch (currentType) {
		case NetworkAccessStorage:
			File file = new File(nasPath);
			if (file.exists() == false) {
				JsfUtils.addErrorMessage("Directory does not exists");
				return;
			}
			break;
		case Database:
			
			break;
		case AwsS3:
			if (selectedType == CloudSafeStorageType.AwsS3) {
				// validate
				getCloudStorage(selectedType);
			}
			break;
		}

		//// Starting copy
		try {
			startDatabase();
			if (selectedType == currentType) {
				if (selectedType == CloudSafeStorageType.NetworkAccessStorage) {
					JsfUtils.addInfoMessage(
							"No change in Storage Media. Note: If you changed the NAS Path, you would need to copy the files" + "manually to the new path.");
				}
				JsfUtils.addInfoMessage("There is nothing to do. Source and destination are the same.");
			} else {
				if (copyFileContent == true) {
					List<TenantEntity> tenants = tenantLogic.getAllTenants();
					tenants.add(0, TenantIdResolver.getMasterTenant());
					if (selectedType != currentType) {
						for (TenantEntity tenantEntity : tenants) {
							CloudSafeContentI source = getCloudStorage(currentType);
							CloudSafeContentI destination = getCloudStorage(selectedType);
							destination.initiateTenant(tenantEntity.getName());
							source.initiateTenant(tenantEntity.getName());
							Future<Exception> future = taskExecutor.submit(new CallCloudSafeStorageCopy(tenantEntity, source, destination));
							try {
								Exception exp = future.get();
								if (exp != null) {
									throw exp;
								}
							} catch (Exception e) {
								String msg = "Error on initialization Tenant: " + tenantEntity.getName() + " Cause: " + e.toString();
								logger.fatal(msg, e);
								throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Can't copy CloudSafeStorage for : " + tenantEntity.getName(), e);
							}
						}
						JsfUtils.addInfoMessage("Migration to " + selectedType.name() + " is succesfull");
					}
				}
			}

			requestContext = WeldContextUtils.activateRequestContext();
			TenantIdResolver.setMasterTenant();
			clusterConfig.setCloudSafeStorageType(selectedType);
			clusterConfig.setNasDirectory(nasPath);
			clusterConfig.setAwsS3AccesskeyId(s3AccessKeyId);
			clusterConfig.setAwsS3SecretAccessKey(s3SecretAccessKey);
			clusterConfig.setAwsS3Url(s3Url);
			DcemConfiguration dcemConfiguration = configLogic.createClusterConfig(clusterConfig);
			configLogic.setDcemConfiguration(dcemConfiguration);
			WeldContextUtils.deactivateRequestContext(requestContext);
			currentCloudStorageType = clusterConfig.getCloudSafeStorageType().name();
			cloudStorageType = currentCloudStorageType;

		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.toString());
			return;
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			return;
		} finally {
			closeDatabase();
		}

		return;
	}

	private CloudSafeContentI getCloudStorage(CloudSafeStorageType cloudSafeStorageType) throws Exception {
		CloudSafeContentI cloudSafeContentI = null;
		switch (cloudSafeStorageType) {
		case Database:
			cloudSafeContentI = new CloudSafeContentDb();
			break;
		case NetworkAccessStorage:
			File file = new File(nasPath);
			if (file.exists() == false) {
				file.mkdir();
			}
			cloudSafeContentI = new CloudSafeContentNas(file);
			break;
		case AwsS3:
			cloudSafeContentI = new CloudSafeContentS3(clusterConfig.getName(), s3Url, s3AccessKeyId, s3SecretAccessKey);
			break;
		default:
			break;
		}
		return cloudSafeContentI;
	}

	public String getCloudStorageType() {
		return cloudStorageType;
	}

	public void setCloudStorageType(String cloudStorageType) {
		this.cloudStorageType = cloudStorageType;
	}

	public String getCurrentCloudStorageType() {
		return currentCloudStorageType;
	}

	public void setCurrentCloudStorageType(String currentCloudStorageType) {
		this.currentCloudStorageType = currentCloudStorageType;
	}

	public String getNasPath() {
		if (nasPath == null || nasPath.isEmpty()) {
			try {
				nasPath = clusterConfig.getNasDirectory();
				if (nasPath == null || nasPath.isEmpty()) {
					File nasPathFile = LocalPaths.getCloudSafeStorageDirectory();
					if (nasPathFile.exists() == false) {
						nasPathFile.mkdir();
					}
					nasPath = nasPathFile.getAbsolutePath();
				}

			} catch (DcemException e) {

			}
		}
		return nasPath;
	}

	public void setNasPath(String nasPath) {
		this.nasPath = nasPath;
	}

	public boolean isNas() {
		return cloudStorageType.equals(CloudSafeStorageType.NetworkAccessStorage.name());
	}

	public boolean isAwsS3() {
		return cloudStorageType.equals(CloudSafeStorageType.AwsS3.name());
	}

	private void startDatabase() throws DcemException {

		// requestContext = WeldContextUtils.activateRequestContext();
		weldSessionContext = WeldContextUtils.activateSessionContext(null);
		DbFactoryProducer dbFactoryProducer = DbFactoryProducer.getInstance();
		dbFactoryProducer.createEmp(localConfig, false);
		DbEncryption.createDbCiphers(localConfig.getDatabase());
		TenantEntity masterTenant = new TenantEntity(null, localConfig.getDatabase().getDatabaseName(), false, null, true, "master");
		TenantIdResolver.setMasterTenant(masterTenant);
		emp = CdiUtils.getReference(EntityManagerProducer.class);
		emp.init();

	}

	private void closeDatabase() {
		if (emp != null) {
			emp.close();
		}
		// WeldContextUtils.deactivateRequestContext(requestContext);
		WeldContextUtils.deactivateSessionContext(weldSessionContext);
	}

	public String getS3AccessKeyId() {
		return s3AccessKeyId;
	}

	public void setS3AccessKeyId(String s3AccessKeyId) {
		this.s3AccessKeyId = s3AccessKeyId;
	}

	public String getS3SecretAccessKey() {
		return s3SecretAccessKey;
	}

	public void setS3SecretAccessKey(String s3SecretAccessKey) {
		this.s3SecretAccessKey = s3SecretAccessKey;
	}

	public String getS3Url() {
		return s3Url;
	}

	public void setS3Url(String s3Url) {
		this.s3Url = s3Url;
	}

	public boolean isCopyFileContent() {
		return copyFileContent;
	}

	public void setCopyFileContent(boolean copyFileContent) {
		this.copyFileContent = copyFileContent;
	}

}
