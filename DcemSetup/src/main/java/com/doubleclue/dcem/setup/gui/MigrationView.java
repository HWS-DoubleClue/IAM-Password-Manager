package com.doubleclue.dcem.setup.gui;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.config.LocalConfig;
import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.entities.DbVersion;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.setup.logic.DbLogic;
import com.doubleclue.dcem.setup.logic.DbMigrate;
import com.doubleclue.dcem.setup.logic.DbState;
import com.doubleclue.dcem.setup.logic.ModuleMigrationVersion;
import com.doubleclue.utils.ProductVersion;

@SuppressWarnings("serial")
@Named("migrationView")
@SessionScoped
public class MigrationView extends DcemView {

	@Inject
	DbLogic dbLogic;

	@Inject
	DbView dbView;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	DbMigrate dbMigrate;

	String adminName;
	String adminPassword;

	DatabaseConfig dbConfig;

	boolean migrationDone = false;

	List<ModuleMigrationVersion> migrationModules;

	@PostConstruct
	protected void init() {
		try {
			dbConfig = LocalConfigProvider.readConfig().getDatabase();
		} catch (DcemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		adminName = dbConfig.getAdminName();
		adminPassword = dbConfig.getAdminPassword();

	}

	/**
	 * @throws DcemException
	 */
	public void actionMigrate() throws DcemException {

		try {
			LocalConfig localConfig = LocalConfigProvider.readConfig();
			dbMigrate.startMigration(localConfig, adminName, adminPassword);
		} catch (Exception e) {
			logger.warn("Couldn't create the tables", e);
			JsfUtils.addErrorMessage(e.toString());
			return;
		}
		try {
			dbLogic.setDbState(DbState.OK);
			migrationDone = true;
			PrimeFaces.current().executeScript("PF('confirmBackup').hide();");
			JsfUtils.addInfoMessage("Database migration successfully.");
			return;
		} catch (Exception exp) {
			logger.warn("Database initilization failed.", exp);
			JsfUtils.addErrorMessage(exp.toString());
			return;
		}
	}

	public List<ModuleMigrationVersion> getMigrationModules() {
		try {
			return dbLogic.getMigrationModules(dbConfig);
		} catch (Exception e) {
			JsfUtils.addErrorMessage("Couldn't get Versions. " + e.toString());
			return null;
		}
	}

	public String getCurrentVersion() {
		try {
			dbConfig = LocalConfigProvider.readConfig().getDatabase();
			DbVersion dbVersion = dbLogic.getDbVersion(dbConfig, "DCEM");
			return dbVersion.getVersionStr();
		} catch (Exception exp) {
			JsfUtils.addErrorMessage("Couln't get the database version. Cause: " + exp.toString());
			return null;
		}

	}

	public String getUpdateVersion() {
		ProductVersion productVersion = applicationBean.getProductVersion();
		return productVersion.getVersionStr();
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

	public void closeApplication() {
		logger.info("Closing Setp Application..");
		System.exit(0);
	}

	public boolean isMigrationDone() {
		return migrationDone;
	}

	public void setMigrationDone(boolean migrationDone) {
		this.migrationDone = migrationDone;
	}

}
