package com.doubleclue.dcem.core.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.utils.RandomUtils;

/**
 * 
 * @author Galea
 *
 */
@SuppressWarnings("serial")
public class ClusterConfig implements Cloneable, Serializable {

	@XmlTransient
	transient boolean toSave = false;

	@NotNull
	@Size(min = 4, max = 32)
	String name;

	@NotNull
	@Size(min = 4, max = 32)
	String givenName;

	@NotNull
	@Size(min = 4, max = 128)
	String dcemHostDomainName;

	@NotNull
	@Size(min = 4, max = 32)
	String password;

	@Min(1)
	@Max(100)
	int scaleFactor = 20;

	@NotNull
	@Size(min = 2, max = 32)
	private String webAppName = "dcem";
	
	private CloudSafeStorageType cloudSafeStorageType = CloudSafeStorageType.Database;
	
	private String nasDirectory;
	
	private String awsS3AccesskeyId;
	private String awsS3SecretAccessKey;
	private String awsS3Url;
	
	private String redirectPort80 = null;

	List<ConnectionService> connectionServices;

	public ClusterConfig() {
	}

//	public ClusterConfig(String name, String password, int scaleFactor, String webAppName) {
//		super();
//		this.name = name;
//		this.password = password;
//		this.scaleFactor = scaleFactor;
//		this.webAppName = webAppName;
//	}

	public void setDefault() {
		password = RandomUtils.generateRandomAlphaNumericString(16);
		scaleFactor = DcemConstants.DEFAULT_SCALE_FACTOR;
		webAppName = DcemConstants.DEFAULT_WEB_NAME;
		ConnectionServicesType[] types = ConnectionServicesType.values();
		connectionServices = new ArrayList<>(types.length);
		for (ConnectionServicesType type : types) {
			if (type == ConnectionServicesType.USER_PORTAL || type == ConnectionServicesType.AZURE_CALLBACK) {
				continue;
			}
			connectionServices.add(new ConnectionService(type));
		}
	}

	public void addMissingConnectionServices() {
		List<ConnectionServicesType> types = new LinkedList<ConnectionServicesType>(Arrays.asList(ConnectionServicesType.values()));
		for (ConnectionService service : connectionServices) {
			types.remove(service.getConnectionServicesType());
		}
		for (ConnectionServicesType type : types) {
			if (type == ConnectionServicesType.USER_PORTAL || type == ConnectionServicesType.AZURE_CALLBACK) {
				continue;
			}
			connectionServices.add(new ConnectionService(type));
		}
	}

	public ConnectionService getConnectionService(ConnectionServicesType connectionServicesType) {
		for (ConnectionService connectionService : connectionServices) {
			if (connectionServicesType == connectionService.getConnectionServicesType()) {
				
				if (connectionService.getSameAsConnectionServiceType() != null) {
					return getConnectionService(connectionService.getSameAsConnectionServiceType());
				}
				return connectionService;
			}
		}
		ConnectionService connectionService = new ConnectionService(connectionServicesType);
		connectionServices.add(connectionService);
		return connectionService;
	}

	public int getScaleFactor() {
		return scaleFactor;
	}

	public void setScaleFactor(int scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	public String getWebAppName() {
		return webAppName;
	}

	public void setWebAppName(String webAppName) {
		this.webAppName = webAppName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getDcemHostDomainName() {
		return dcemHostDomainName;
	}

	public void setDcemHostDomainName(String dcemHostDomainName) {
		this.dcemHostDomainName = dcemHostDomainName;
	}

	public List<ConnectionService> getConnectionServices() {
		return connectionServices;
	}

	public void setConnectionServices(List<ConnectionService> connectionServices) {
		this.connectionServices = new ArrayList<>();
		for (ConnectionService connectionService :  connectionServices) {
			if (connectionService.connectionServicesType == ConnectionServicesType.USER_PORTAL || connectionService.connectionServicesType == ConnectionServicesType.AZURE_CALLBACK) {
				continue;
			}
			this.connectionServices.add(connectionService);
		}
	}

	@XmlTransient
	public boolean isToSave() {
		return toSave;
	}

	public void setToSave(boolean toSave) {
		this.toSave = toSave;
	}

	@Override
	public String toString() {
		return "ClusterConfig [givenName=" + givenName + ", dcemHostDomainName=" + dcemHostDomainName + ", scaleFactor=" + scaleFactor
				+ ", webAppName=" + webAppName + ", connectionServices=" + connectionServices + "]";
	}

	public String getRedirectPort80() {
		return redirectPort80;
	}

	public void setRedirectPort80(String redirectPort80) {
		this.redirectPort80 = redirectPort80;
	}

	public CloudSafeStorageType getCloudSafeStorageType() {
		return cloudSafeStorageType;
	}

	public void setCloudSafeStorageType(CloudSafeStorageType cloudSafeStorageType) {
		this.cloudSafeStorageType = cloudSafeStorageType;
	}

	public String getNasDirectory() {
		return nasDirectory;
	}

	public void setNasDirectory(String nasDirectory) {
		this.nasDirectory = nasDirectory;
	}

	public String getAwsS3AccesskeyId() {
		return awsS3AccesskeyId;
	}

	public void setAwsS3AccesskeyId(String awsS3AccesskeyId) {
		this.awsS3AccesskeyId = awsS3AccesskeyId;
	}

	public String getAwsS3SecretAccessKey() {
		return awsS3SecretAccessKey;
	}

	public void setAwsS3SecretAccessKey(String awsS3secretAccessKey) {
		this.awsS3SecretAccessKey = awsS3secretAccessKey;
	}

	public String getAwsS3Url() {
		return awsS3Url;
	}

	public void setAwsS3Url(String awsS3Url) {
		this.awsS3Url = awsS3Url;
	}
	
	
	
}
