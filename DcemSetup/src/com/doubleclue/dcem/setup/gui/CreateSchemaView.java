package com.doubleclue.dcem.setup.gui;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.setup.logic.DbLogic;
import com.doubleclue.dcem.setup.logic.DbState;

@SuppressWarnings("serial")
@Named("createSchemaView")
@SessionScoped
public class CreateSchemaView extends DcemView {



	@Inject
	DbLogic dbLogic;
	
	@Inject
	DbView dbView;
	
	DatabaseConfig dbConfig;
	
	String adminName;
	String adminPassword;
	
	boolean schemaExists;

	@PostConstruct
	protected void init()  {
		try {
			dbConfig = LocalConfigProvider.readConfig().getDatabase();
		} catch (DcemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		adminName = dbConfig.getAdminName();
		adminPassword = dbConfig.getAdminPassword();
	}
	
	
	public void actionCreateSchema() {
		
		try {
			dbConfig = LocalConfigProvider.readConfig().getDatabase();
			dbLogic.createSchema(dbConfig, adminName, adminPassword);
			dbView.setDbState(DbState.Create_Tables_Required);
			JsfUtils.addFacesInformationMessage("Database Schema created successfully. Please proceed with the next step.");
			dbLogic.setDbState(DbState.Create_Tables_Required);
			return;
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getMessage());
			return;
		}

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

	public boolean isSchemaRequired() {
		return (dbLogic.getDbState() == DbState.Create_Schema_Required );
	}

	
}
