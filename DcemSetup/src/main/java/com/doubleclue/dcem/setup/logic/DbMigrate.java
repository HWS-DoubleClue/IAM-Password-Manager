package com.doubleclue.dcem.setup.logic;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.tool.schema.TargetType;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.config.LocalConfig;
import com.doubleclue.dcem.core.entities.DbVersion;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jpa.DatabaseTypes;
import com.doubleclue.dcem.core.jpa.DbFactoryProducer;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.EntityManagerProducer;
import com.doubleclue.dcem.core.jpa.JdbcUtils;
import com.doubleclue.dcem.core.jpa.ScriptRunner;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.ActionLogic;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.logic.TenantLogic;
import com.doubleclue.dcem.core.logic.TextResourceLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.system.logic.SystemModule;

import com.doubleclue.utils.FileContent;
import com.doubleclue.utils.ProductVersion;
import com.doubleclue.utils.ResourceFinder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

@ApplicationScoped
public class DbMigrate {

	@Inject
	DbLogic dbLogic;

	@Inject
	TemplateLogic templateLogic;

	@Inject
	TextResourceLogic textResourceLogic;

	@Inject
	TaskExecutor taskExecutor;

	@Inject
	ActionLogic actionLogic;

	@Inject
	RoleLogic roleLogic;

//	@Inject
//	UpAppHubLogic upAppHubLogic;
	
	@Inject
	UserLogic userLogic;

	private static final Logger logger = LogManager.getLogger(DbMigrate.class);

	public void startMigration(LocalConfig localConfig, String schemaAdmin, String schemaPassword) throws Exception {
		DatabaseConfig databaseConfig = localConfig.getDatabase();
		DatabaseTypes dbType = DatabaseTypes.valueOf(databaseConfig.getDatabaseType());
		Connection conn = JdbcUtils.getJdbcConnectionWithSchema(databaseConfig, schemaAdmin, schemaPassword);

		TenantEntity tenantMaster = TenantLogic.getMasterTenant(localConfig);
		JdbcUtils.switchDb(conn, dbType, databaseConfig.getDatabaseName());
		List<TenantEntity> tenants = JdbcUtils.getTeneants(conn);
		TenantIdResolver.setMasterTenant(tenantMaster);
		tenants.add(0, tenantMaster);

		Map<String, List<ModuleMigrationVersion>> migrationModuleMap = new HashMap<String, List<ModuleMigrationVersion>>();

		for (TenantEntity tenantEntity : tenants) {
			JdbcUtils.switchDb(conn, dbType, tenantEntity.getSchema());
			logger.info("Start DB Migration Scripts for: " + tenantEntity.getName());
			List<ModuleMigrationVersion> migrationModules = getMigrationModules(conn);
			migrationModuleMap.put(tenantEntity.getSchema(), migrationModules);
			migrateScriptTenant(conn, localConfig, tenantEntity, migrationModules);
			logger.info("Migration Scripts succesful for: " + tenantEntity.getName());
			/*
			 *  Auto Update
			 */
			CreateDbUpdateScripts.createMigrationScripts(databaseConfig, TargetType.DATABASE, tenantEntity.getSchema());
			
		}

		EntityManagerProducer emp = null;
		DbFactoryProducer dbFactoryProducer;
		EntityManagerFactory emf = null;
		try {
			dbFactoryProducer = DbFactoryProducer.getInstance();
			emf = dbFactoryProducer.createEmp(localConfig, false);
			emp = CdiUtils.getReference(EntityManagerProducer.class);
			emp.init();
		} catch (Exception e) {
			logger.error("Error while setting up Entity Manager Producer during migration: " + e.getMessage(), e);
			throw new DcemException(DcemErrorCodes.CANNOT_MIGRATE_MODULE, "Cannot create database driver." + e.toString(), e);
		}

		for (TenantEntity tenantEntity : tenants) {
			logger.info("Start DB Migration for: " + tenantEntity.getName());
			List<ModuleMigrationVersion> migrationModules = migrationModuleMap.get(tenantEntity.getSchema());
			Future<Exception> future = taskExecutor.submit(new DbMigrateTenantTask(tenantEntity, migrationModules));
			try {
				Exception exp = future.get();
				if (exp != null) {
					throw exp;
				}
			} catch (Exception e) {
				String msg = "Error on initialization Tenant: " + tenantEntity.getName() + " Cause: " + e.toString();
				logger.fatal(msg, e);
			}
			logger.info("Migration successful for: " + tenantEntity.getName());
		}

		if (emp != null) {
			emp.close();
		}
		if (emf != null) {
			emf.close();
		}
	}

	private void migrateScriptTenant(Connection conn, LocalConfig localConfig, TenantEntity tenantEntity, List<ModuleMigrationVersion> migrationModules)
			throws SQLException, DcemException {
		DatabaseConfig databaseConfig = localConfig.getDatabase();
		try {
			executeMigrationScripts(conn, databaseConfig, migrationModules, tenantEntity);
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.CANNOT_MIGRATE_MODULE, "Execution migration script failed. " + e.toString(), e);
		}
		return;
	}

	private List<ModuleMigrationVersion> getMigrationModules(Connection conn) {
		List<ModuleMigrationVersion> migrationModules = null;
		try {
			migrationModules = dbLogic.checkMigration(conn);
		} catch (Exception e1) {
			logger.warn("checkMigration", e1);
		}
		return migrationModules;
	}

	private void executeMigrationScripts(Connection conn, DatabaseConfig databaseConfig, List<ModuleMigrationVersion> migrationModules, TenantEntity tenantEntity)
			throws SQLException, DcemException {
		DatabaseTypes dbType = DatabaseTypes.valueOf(databaseConfig.getDatabaseType());
		ScriptRunner scriptRunner = new ScriptRunner(conn);
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();

		URL url = null;
		int updateTo;
		conn.setAutoCommit(false);
		for (ModuleMigrationVersion moduleMigration : migrationModules) {
			if (moduleMigration.isMasterOnly() && tenantEntity.isMaster() == false && (moduleMigration.getId() != SystemModule.MODULE_ID)) {
				// ignore tenant for this Module
				continue;
			}
			if (moduleMigration.getId().equals(DcemConstants.DCEM_MODULE_ID) == false) {
				updateTo = Integer.parseInt(moduleMigration.updateToVersion);
				for (int i = Integer.parseInt(moduleMigration.currentVersion); i < updateTo; i++) {
					String fileName = String.format(SetupConstants.MIGRATION_FILE_FORMAT, dbType.name(), i, i + 1, moduleMigration.getId());
					try {
						url = classloader.getResource(fileName);
					} catch (Exception e) {
						throw new DcemException(DcemErrorCodes.CANNOT_MIGRATE_MODULE, "for Module " + moduleMigration.getId());
					}
					if (url == null) {
						logger.info("No Migration Script found for " + fileName);
						continue;
				//		throw new DcemException(DcemErrorCodes.CANNOT_MIGRATE_MODULE, "for Module " + moduleMigration.getId() + " Invalid File: " + fileName);
					}
					logger.info("Executing SQL Script : " + url);
					StringReader stringReader;
					try {
						stringReader = new StringReader(Resources.toString(url, Charsets.UTF_8));
					} catch (IOException e) {
						throw new DcemException(DcemErrorCodes.CANNOT_MIGRATE_MODULE, "for Module " + moduleMigration.getId() + " Invalid URL: " + fileName);
					}
					scriptRunner.setStopOnError(true);
					scriptRunner.runScript(stringReader, dbType, Integer.parseInt(moduleMigration.getCurrentVersion()), updateTo);
				}
			} else {
				try {
					ProductVersion productVersion = new ProductVersion("", moduleMigration.updateToVersion);
					updateTo = productVersion.getVersionInt();
				} catch (Exception e) {
					throw new DcemException(DcemErrorCodes.CANNOT_MIGRATE_MODULE, "Invalid DCEM Version");
				}
			}
			DbVersion dbVersion = new DbVersion();
			dbVersion.setModuleId(moduleMigration.getId());
			dbVersion.setVersion(updateTo);
			dbVersion.setVersionStr(moduleMigration.updateToVersion);
			JdbcUtils.updateVersion(conn, dbVersion);
		}
		conn.commit();
	}

	@DcemTransactional
	public void updateTemplates() throws Exception {
		List<DcemTemplate> templates = templateLogic.getActiveTemplates();
		List<FileContent> templateFiles = ResourceFinder.find(AdminModule.class, SetupConstants.TEMPLATE_RESOURCES, SetupConstants.TEMPLATE_TYPE);
		SupportedLanguage supportedLanguage;
		logger.info("  Updating Templates for Tenant: " + TenantIdResolver.getCurrentTenantName());
		for (FileContent fileContent : templateFiles) {
			String fileName = fileContent.getName().substring(0, fileContent.getName().length() - SetupConstants.TEMPLATE_TYPE.length());
			String locale = fileName.substring(fileName.length() - 2);
			if (fileName.charAt(fileName.length() - 3) != '_') {
				logger.warn("Invalid Tempalte name format. " + fileName);
				continue;
			}
			fileName = fileName.substring(0, fileName.length() - 3);
			supportedLanguage = DcemUtils.getSuppotedLanguage(locale); // default is always english
			if (templateLogic.isNewTemplate(templates, fileName, supportedLanguage)) {
				DcemTemplate dcemTemplate = new DcemTemplate();
				dcemTemplate.setName(fileName);
				if (supportedLanguage == SupportedLanguage.English) {
					dcemTemplate.setDefaultTemplate(true);
				}
				dcemTemplate.setLanguage(supportedLanguage);
				dcemTemplate.setContent(new String(fileContent.getContent(), DcemConstants.CHARSET_UTF8));
				templateLogic.addOrUpdateTemplate(dcemTemplate, new DcemAction(AdminModule.MODULE_ID, null, DcemConstants.ACTION_ADD), false);
			}
		}
	}

	@DcemTransactional
	public void updateMyApplications() throws Exception {
//		List<FileContent> myApplicationsFiles = ResourceFinder.find(DbLogic.class, SetupConstants.MYAPPLICATIONS_RESOURCES, SetupConstants.MYAPPLICATIONS_TYPE);
//		for (FileContent fileContent : myApplicationsFiles) {
//			try {
//				String fileContents = new String(fileContent.getContent(), DcemConstants.CHARSET_UTF8);
//				ObjectMapper mapper = new ObjectMapper();
//				MyApplication myApplication = mapper.readValue(fileContents, MyApplication.class);
//				if (upAppHubLogic.getApplicationByName(myApplication.getName()) == null) {
//					upAppHubLogic.updateApplication(new ApplicationHubEntity(myApplication));
//				}
//			} catch (Exception exp) {
//				logger.warn("Couldn't upload myapplication file [" + fileContent.getName() + "] to DB", exp);
//				throw exp;
//			}
//		}
	}

	

	public void migrateTo_2_5_0() {

		DcemAction reportingViewAction = new DcemAction("admin", "Reporting", "view");
		actionLogic.addDcemAction(reportingViewAction);
		reportingViewAction = actionLogic.getDcemAction(reportingViewAction);

		DcemAction reportingManageAction = new DcemAction("admin", "Reporting", "manage");
		actionLogic.addDcemAction(reportingManageAction);
		reportingManageAction = actionLogic.getDcemAction(reportingManageAction);

		List<DcemAction> newActions = new ArrayList<DcemAction>();
		newActions.add(reportingViewAction);
		newActions.add(reportingManageAction);

		DcemRole superAdminRole = roleLogic.getDcemRole("SuperAdmin");
		roleLogic.addActionsToRole(superAdminRole, newActions);

		DcemRole adminRole = roleLogic.getDcemRole("Admin");
		roleLogic.addActionsToRole(adminRole, newActions);
	}
}