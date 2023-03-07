package com.doubleclue.dcem.setup.gui;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.hibernate.tool.schema.TargetType;
import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.config.LocalConfig;
import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.DatabaseTypes;
import com.doubleclue.dcem.core.jpa.DatabaseUtils;
import com.doubleclue.dcem.core.jpa.JdbcUtils;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.setup.logic.CreateDbUpdateScripts;
import com.doubleclue.dcem.setup.logic.DbLogic;
import com.doubleclue.dcem.setup.logic.DbState;

@SuppressWarnings("serial")
@Named("dbView")
@SessionScoped
public class DbView extends DcemView {

	LocalConfig config;

	@Inject
	DbLogic dbLogic;

	@Inject
	CreateDbUpdateScripts createDbUpdateScripts;

	DbState dbState = DbState.Init;

	DatabaseConfig dbConfig;

	String nodeName;

	@PostConstruct
	protected void init() {
		try {
			config = LocalConfigProvider.readConfig();
			dbConfig = config.getDatabase();
		} catch (Exception e) {
			logger.error("Couldn't read configuration file");
			return;
		}
		dbConfig = config.getDatabase();
	}

	public String getLocalConfigurationFile() {
		try {
			return LocalPaths.getConfigurationFile().getAbsolutePath();
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.toString());
			return null;
		}
	}

	/**
	 * 
	 */
	public void actionSave() {

		if (withSchema() == false) {
			dbConfig.setSchemaName("");
		}
		config.setDatabase(dbConfig);
		try {
			testDbConnection(config.getNodeName(), nodeName);
			config.setNodeName(nodeName);
			LocalConfigProvider.writeConfig(config);
			if ((dbState == DbState.OK || dbState == DbState.Create_Tables_Required) && dbConfig.getDatabaseType().equals(DatabaseTypes.DERBY.name()) == true) {
				PrimeFaces.current().executeScript("PF('embeddedDbMsg').show();");
			}
		} catch (Exception exp) {
			logger.warn(exp.getMessage(), exp);
			JsfUtils.addErrorMessage(exp.getMessage());
		}
		return;
	}
	
	public void actionSynchroniuzeDb () {
		try {
			File outputFolder = createDbUpdateScripts.createMigrationScripts(dbConfig, TargetType.SCRIPT);
			StringBuffer stringBuffer = new StringBuffer();
			for (File file : outputFolder.listFiles()) {
				if (file.length() > 0) {
					stringBuffer.append(file.getName());
					stringBuffer.append(", ");
				}
			}
			if (stringBuffer.length() == 0) {
				JsfUtils.addInfoMessage("There is no need for migration");
			} else {
				JsfUtils.addWarnMessage("The database-schema is not UpToDate!!!");
				JsfUtils.addWarnMessage("See SQL Update Scripts at: "  + outputFolder.getAbsolutePath() );
				JsfUtils.addWarnMessage(stringBuffer.toString());
				JsfUtils.addInfoMessage("Click on 'Execute Migration Scripts' for migration.");
			}
			
			
			
		} catch (Exception exp) {
			logger.warn(exp.getMessage(), exp);
			JsfUtils.addErrorMessage("Database migration scripts FAILED: " + exp.toString());
		}
	}
	
	public void actionAutoMigrateeDb () {
		try {
			createDbUpdateScripts.createMigrationScripts(dbConfig, TargetType.DATABASE);
			JsfUtils.addInfoMessage("The database migrated succesfully: ");
		} catch (Exception exp) {
			JsfUtils.addErrorMessage("Database migration FAILED: " + exp.toString());
			logger.warn(exp.getMessage(), exp);
		}
	}

	public void actionCreateUrl() {
		try {
			String jdbcUrl = DatabaseUtils.createDatabaseUrl(dbConfig);
			dbConfig.setJdbcUrl(jdbcUrl);
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getMessage());
		}

	}

	public String getEmbeddedDir() throws DcemException {
		File file = LocalPaths.getDerbyDirectory();
		File fileSchema = new File(file, DatabaseConfig.DEFAULT_DATABASE_NAME);
		return fileSchema.getAbsolutePath();
	}

	public void dbChange() {
		DatabaseTypes dbType = DatabaseTypes.valueOf(dbConfig.getDatabaseType());
		dbConfig.setPort(dbType.getDefaultPort());
		actionCreateUrl();
	}

	public List<SelectItem> getDatabaseTypes() {
		List<SelectItem> selectItems = new LinkedList<SelectItem>();
		for (DatabaseTypes dbType : DatabaseTypes.values()) {
			if (/* dbType == DatabaseTypes.DERBY || */ dbType == DatabaseTypes.ORACLE) {
				continue;
			}
			selectItems.add(new SelectItem(dbType.name(), dbType.getDisplayName()));
		}
		return selectItems;
	}

	public LocalConfig getConfig() {
		return config;
	}

	public DatabaseConfig getDbConfig() {
		return dbConfig;
	}

	public DbState getDbState() {
		return dbState;
	}

	public void setDbState(DbState dbState) {
		this.dbState = dbState;
	}

	public boolean withSchema() {
		return dbConfig.getDatabaseType().equals(DatabaseTypes.MSSQL.name());
	}

	public boolean isEmbedded() {
		return (dbConfig.getDatabaseType().equals(DatabaseTypes.DERBY.name()));
	}

	public void setDbConfig(DatabaseConfig dbConfig) {
		this.dbConfig = dbConfig;
	}

	public boolean isMigration() {
		return dbState == DbState.Migration_Required;
	}

	public boolean isStateOk() {
		return dbState == DbState.OK;
	}

	public boolean isCloseApplication() {
		return dbState == DbState.OK;
	}

	/**
	 * 
	 */
	private void testDbConnection(String currentNodeName, String newNodeName) {

		try {
			if (withSchema() == false) {
				dbConfig.setSchemaName("");
			}
			config.setDatabase(dbConfig);
			List<String> list = dbLogic.testDbConnection(dbConfig, currentNodeName, newNodeName);
			dbState = dbLogic.getDbState();
			for (String msg : list) {
				if (dbState == DbState.Exception) {
					JsfUtils.addErrorMessage(msg);
				} else {
					JsfUtils.addFacesInformationMessage(msg);
				}
			}
			if (dbState == DbState.Exception) {
				return;
			}

			switch (dbState) {

			case Create_Schema_Required:

				break;
			case Create_Tables_Required:

				break;
			case No_Connection:
				JsfUtils.addErrorMessage("Couldn't connect to Database");
				break;
			case Migration_Required:
				JsfUtils.addErrorMessage("Migration required");
				break;
			case OK:
				break;
			default:
				break;
			}

			return;
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			JsfUtils.addErrorMessage(e.getMessage());
			return;
		}

	}

	public String getNodeName() {
		nodeName = config.getNodeName();
		if (nodeName == null) {
			nodeName = DcemUtils.getComputerName();
		}
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

}
