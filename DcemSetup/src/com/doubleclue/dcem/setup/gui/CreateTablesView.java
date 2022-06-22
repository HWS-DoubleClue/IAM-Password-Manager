package com.doubleclue.dcem.setup.gui;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.setup.MainSetup;
import com.doubleclue.dcem.setup.logic.DbLogic;
import com.doubleclue.dcem.setup.logic.DbState;
import com.doubleclue.utils.RandomUtils;

@SuppressWarnings("serial")
@Named("createTablesView")
@SessionScoped
public class CreateTablesView extends DcemView {

	@Inject
	DbLogic dbLogic;

	@Inject
	DbView dbView;

	String adminName;
	String adminPassword;

	DatabaseConfig dbConfig;

	String superAdminPassword;

	String resetSuperAdminPassword;

	String mgtHostName;

	String serverUrl;

	@PostConstruct
	protected void init() {
		try {
			dbConfig = LocalConfigProvider.readConfig().getDatabase();
		} catch (DcemException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		adminName = dbConfig.getAdminName();
		adminPassword = dbConfig.getAdminPassword();
		resetSuperAdminPassword = adminPassword;
		try {
			mgtHostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			mgtHostName = DcemUtils.getComputerName();
		}
	}

	public void actionResetAdminPassword() {
		try {
			dbLogic.resetAdminPassword(LocalConfigProvider.readConfig(), resetSuperAdminPassword);
		} catch (DcemException e) {
			logger.warn("Couldn't reset password", e);
			JsfUtils.addErrorMessage(e.toString());
		} catch (Exception e) {
			logger.warn("Couldn't reset password", e);
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	public void actionCreateTables() throws DcemException {
		URL url;
		try {
			url = new URL(serverUrl);
			if (url.getProtocol().equalsIgnoreCase(DcemConstants.HTTPS_PROTOCOL) == false) {
				throw new Exception();
			}
		} catch (Exception exp) {
			JsfUtils.addErrorMessage("Invalid Server URL. URL must start with 'https://'");
			return;
		}
		serverUrl = serverUrl.toLowerCase();
		try {
			dbConfig = LocalConfigProvider.readConfig().getDatabase();
			dbLogic.setupCreateTables(LocalConfigProvider.getLocalConfig(), adminName, adminPassword);
			JsfUtils.addFacesInformationMessage("Tables created successfully");
			dbView.setDbConfig(dbConfig);
		} catch (Exception e) {
			logger.warn("Couldn't create the tables", e);
			JsfUtils.addErrorMessage(e.toString());
			return;
		}

		String clusterId = RandomUtils.generateRandomAlphaNumericString(16);

		try {
			dbLogic.initializeDb(LocalConfigProvider.getLocalConfig(), clusterId, superAdminPassword, mgtHostName, MainSetup.getWebPort(), url);
			JsfUtils.addFacesInformationMessage("Database initialization successfully.");
			dbLogic.setDbState(DbState.OK);
			PrimeFaces.current().executeScript("PF('readyMsg').show();");
			return;
		} catch (Exception exp) {
			logger.warn("Database initilization failed.", exp);
			JsfUtils.addErrorMessage(exp.toString());
			return;
		}
	}

	public String getConfigPath() {
		try {
			return LocalPaths.getConfigurationFile().getAbsolutePath();
		} catch (DcemException e) {
			return "ERROR: couldn't retreive configuration file name";
		}
	}

	public boolean isTablesRequired() {

		return (dbLogic.getDbState() == DbState.Create_Tables_Required);
	}

	public String getSuperAdminPassword() {
		return superAdminPassword;
	}

	public void setSuperAdminPassword(String superAdminPassword) {
		this.superAdminPassword = superAdminPassword;
	}

	public String getAdminName() {
		return adminName;
	}

	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}

	public String getAdminPassword() {
		return adminPassword;
	}

	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}

	public boolean isAppClose() {
		return (dbLogic.getDbState() == DbState.OK);
	}

	public String closeApplication() {
		JsfUtils.addErrorMessage("DoubleClue Setup will close in some seconds!");
		logger.info("Closing Setup Application..");
		Thread thread = new Thread (new Runnable() {
			        public void run(){
			        	try {
							Thread.sleep(4000);
						} catch (InterruptedException e) {
							logger.info("Couldn't close.", e);
						}
			        	System.exit(0);
			        }
			    });
		thread.start();
		return "setupClose";
	}
	
	public String getDcemUrl() {
		return getServerUrl() + ":8443/dcem/mgt";
	}

	public String getResetSuperAdminPassword() {
		return resetSuperAdminPassword;
	}

	public void setResetSuperAdminPassword(String resetSuperAdminPassword) {
		this.resetSuperAdminPassword = resetSuperAdminPassword;
	}

	public String getServerUrl() {
		if (serverUrl == null) {
			URL url = JsfUtils.getHostUrl();
			if (url != null) {
				serverUrl = "https://" + JsfUtils.getHostUrl().getHost();
			} else {
				try {
					serverUrl = "https://" + InetAddress.getLocalHost().getHostName();
				} catch (UnknownHostException e) {
					serverUrl = "https://your-host.com";
				}
			}
		}
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

}
