package com.doubleclue.dcem.core.logic;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.TenantBrandingLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.entities.DbVersion;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantBrandingEntity;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jpa.DatabaseTypes;
import com.doubleclue.dcem.core.jpa.DatabaseUtils;
import com.doubleclue.dcem.core.jpa.DatabaseUtils.UrlDriverName;
import com.doubleclue.dcem.core.jpa.DbFactoryProducer;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.JdbcUtils;
import com.doubleclue.dcem.core.jpa.ScriptRunner;
import com.doubleclue.dcem.core.licence.LicenceKeyContent;
import com.doubleclue.dcem.core.licence.LicenceLogicInterface;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.utils.FileContent;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.ProductVersion;
import com.doubleclue.utils.ResourceFinder;
import com.doubleclue.utils.StringUtils;

@ApplicationScoped
@Named("createTenant")
public class CreateTenant {

	private static Logger logger = LogManager.getLogger(CreateTenant.class);

	@Inject
	EntityManager em;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	RoleLogic roleLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	ActionLogic actionLogic;

	@Inject
	TemplateLogic templateLogic;

	@Inject
	TextResourceLogic textResourceLogic;

	@Inject
	GroupLogic groupLogic;

	@Inject
	LicenceLogicInterface licenceLogic;

	@Inject
	AdminModule adminModule;

	@Inject
	TenantBrandingLogic tenantBrandingLogic;

	@DcemTransactional
	public void recoverSuperAdminAccess(String superAdminPassword) throws DcemException {
		DcemRole superAdminRole = new DcemRole(DcemConstants.SYSTEM_ROLE_SUPERADMIN, true, 10);
		superAdminRole = roleLogic.addRole(superAdminRole);
		DcemUser superAdmin = userLogic.getDistinctUser(DcemConstants.SUPER_ADMIN_OPERATOR);
		if (superAdmin == null) {
			superAdmin = new DcemUser(DcemConstants.SUPER_ADMIN_OPERATOR, "SuperAdmin@dummy.com", DcemConstants.SUPER_ADMIN_OPERATOR_DISPLAY, superAdminRole);
		}
		superAdmin.setInitialPassword(superAdminPassword);
		userLogic.addOrUpdateUserWoAuditing(superAdmin);
		userLogic.enableUserWoAuditing(superAdmin);
		roleLogic.addActionsToRole(superAdminRole, actionLogic.getAllDcemActions("Privilege"));
		groupLogic.removeMemberFromAllGroups(superAdmin);
	}

	public void createTenant(TenantEntity tenantEntity, String superAdminPassword, String superAdminPhone, String superAdminEmail,
			SupportedLanguage supportedLanguage, String loginId, String displayName, TimeZone timeZone) throws SQLException, DcemException, IOException {
		DatabaseConfig databaseConfig = LocalConfigProvider.getLocalConfig().getDatabase();
		createSchema(tenantEntity.getSchema(), databaseConfig.getAdminName(), databaseConfig.getAdminPassword());
		createTables(DbFactoryProducer.getDbType(), tenantEntity.getSchema(), databaseConfig.getAdminName(), databaseConfig.getAdminPassword(), false);
		initializeDbTenant(tenantEntity, superAdminPassword, superAdminPhone, superAdminEmail, supportedLanguage, loginId, displayName, timeZone);
	}

	public void initializeDbTenant(TenantEntity tenantEntity, String superAdminPassword, String superAdminPhone, String superAdminEmail,
			SupportedLanguage supportedLanguage, String loginId, String displayName, TimeZone timeZone) throws DcemException {
		/*
		 * Create the Roles
		 */
		logger.debug("Adding Roles");
		DcemRole superAdminRole = new DcemRole(DcemConstants.SYSTEM_ROLE_SUPERADMIN, true, 10);
		roleLogic.addRole(superAdminRole);

		DcemRole role = new DcemRole(DcemConstants.SYSTEM_ROLE_ADMIN, true, 8);
		roleLogic.addRole(role);
		role = new DcemRole(DcemConstants.SYSTEM_ROLE_HELPDESK, true, 6);
		roleLogic.addRole(role);
		role = new DcemRole(DcemConstants.SYSTEM_ROLE_VIEWER, true, 4);
		roleLogic.addRole(role);
		DcemRole roleUser = new DcemRole(DcemConstants.SYSTEM_ROLE_USER, true, 0);
		roleLogic.addRole(roleUser);

		LicenceKeyContent licenceKeyContent = licenceLogic.createTrialLicence(DcemConstants.LICENCE_TRIAL_EXPIRY_DAYS, "Trial Licence");
		licenceLogic.addLicenceToDb(licenceKeyContent);
		adminModule.initializeTenant(tenantEntity);
		/*
		 * 
		 * Cerate the SuperAdmin
		 * 
		 */
		logger.debug("Adding SuperAdmin");
		DcemUser userOperator = new DcemUser(DcemConstants.SUPER_ADMIN_OPERATOR, "SuperAdmin@dummy.com", DcemConstants.SUPER_ADMIN_OPERATOR_DISPLAY,
				superAdminRole);
		userOperator.setEmail(superAdminEmail);
		userOperator.setLanguage(supportedLanguage);
		userOperator.setInitialPassword(superAdminPassword);
		userOperator.setMobileNumber(superAdminPhone);
		userLogic.addOrUpdateUserWoAuditing(userOperator);
		/*
		 * 
		 * Cerate the trial Tenant user
		 * 
		 */
		if (loginId != null) {
			DcemUser dcemUser = new DcemUser(loginId, superAdminEmail, loginId, superAdminRole);
			dcemUser.setLanguage(supportedLanguage);
			dcemUser.setInitialPassword(superAdminPassword);
			dcemUser.setEmail(superAdminEmail);
			dcemUser.setMobileNumber(superAdminPhone);
			dcemUser.setDisplayName(displayName);
			userLogic.addOrUpdateUserWoAuditing(dcemUser);
		}
		/*
		 * Create REST-API Action and role
		 */
		DcemAction action = new DcemAction(AdminModule.MODULE_ID, DcemConstants.SUBJECT_REST_API, DcemConstants.ACTION_MANAGE);
		actionLogic.addDcemAction(action);

		DcemRole serviceRole = new DcemRole(DcemConstants.SYSTEM_ROLE_REST_SERVICE, true, 8);
		Set<DcemAction> listActions = new HashSet<>();
		listActions.add(action);
		serviceRole.updateActions(listActions);
		roleLogic.addRole(serviceRole);

		DcemUser restApiUser = new DcemUser(DcemConstants.REST_API_OPERATOR, null, DcemConstants.REST_API_OPERATOR, serviceRole);
		restApiUser.setLanguage(supportedLanguage);
		restApiUser.setInitialPassword(superAdminPassword);
		userLogic.addOrUpdateUserWoAuditing(restApiUser);

		DcemUser radiusUser = new DcemUser(DcemConstants.RADIUS_OPERATOR_NAME, null, DcemConstants.RADIUS_OPERATOR_NAME, roleUser);
		radiusUser.setLanguage(supportedLanguage);
		radiusUser.setInitialPassword(superAdminPassword);
		radiusUser.setSaveit(superAdminPassword);
		userLogic.addOrUpdateUserWoAuditing(radiusUser);

		logger.debug("Adding Actions");
		actionLogic.createDbActions(tenantEntity);
		logger.debug("Adding Templates");
		createDefaultTemplates();
		try {
			logger.debug("Adding TextResources");
			textResourceLogic.createDefaultTextResources(true);
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Couldn't create default text resources", e);
		}
		logger.debug("initializeTenant complete");

		for (DcemModule dcemModule : applicationBean.getSortedModules()) {
			dcemModule.initializeDb(userOperator);
		}
		if (timeZone != null) {
			TenantBrandingEntity brandingEntity = new TenantBrandingEntity();
			brandingEntity.setTimezone(timeZone.getID());
			tenantBrandingLogic.setTenantBrandingEntity(brandingEntity);
		}
	}

	public void createSchema(String schemaName, String schemaAdmin, String schemaPassword) throws SQLException {
		Connection conn = null;
		UrlDriverName urlDriverName = DatabaseUtils.getUrlAndDriverName(LocalConfigProvider.getLocalConfig().getDatabase());
		conn = DriverManager.getConnection(urlDriverName.url, schemaAdmin, schemaPassword);
		Statement statement = conn.createStatement();
		DatabaseTypes databaseType = DatabaseTypes.valueOf(LocalConfigProvider.getLocalConfig().getDatabase().getDatabaseType());
		String createDb = databaseType.getCreateSchema();
		Map<String, String> map = new HashMap<>();
		map.put("db.name", schemaName);
		statement.executeUpdate(StringUtils.substituteTemplate(createDb, map));
		conn.close();
	}

	/**
	 * @param dbConfig
	 * @param schemaAdmin
	 * @param schemaPassword
	 * @param clusterName
	 * @param superAdminPassword
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 * @throws Exception
	 */
	public void createTables(DatabaseTypes dbType, String schemaName, String createTablesAdmin, String createTablesPassword, boolean masterTenant)
			throws DcemException, SQLException, IOException {
		// UrlDriverName urlDriverName =
		// DatabaseUtils.getUrlAndDriverName(LocalConfigProvider.getLocalConfig().getDatabase());

		// Connection conn = DriverManager.getConnection(urlDriverName.url, createTablesAdmin, createTablesPassword);

		Connection conn = JdbcUtils.getJdbcConnection(LocalConfigProvider.getLocalConfig().getDatabase(), createTablesAdmin, createTablesPassword);

		Statement stmt = null;
		conn.setAutoCommit(false);
		try {
			ScriptRunner scriptRunner = new ScriptRunner(conn);
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();

			stmt = conn.createStatement();
			if (dbType.getSchemaSwitch() != null) {
				stmt.execute(dbType.getSchemaSwitch() + schemaName);
			}

			for (DcemModule module : applicationBean.getSortedModules()) {
				if (module.isHasDbTables() == false) {
					continue;
				}
				if (module.isMasterOnly() && masterTenant == false) {
					continue;
				}
				String fileName = "com/doubleclue/dcem/db/" + dbType.name() + "/dcem." + module.getId() + "Tables.sql";
				URL url = null;
				try {
					url = classloader.getResource(fileName);
				} catch (Exception e) {
					throw new DcemException(DcemErrorCodes.CANNOT_CREATE_TABLES, "for Module " + module.getId());
				}
				if (url == null) {
					throw new DcemException(DcemErrorCodes.CANNOT_CREATE_TABLES, "for Module " + module.getId() + " Invalid URL: " + fileName);
				}
				logger.debug("Executing : " + url);
				InputStream inputstream = url.openStream();
				StringReader stringReader = new StringReader(KaraUtils.readInputStreamText(inputstream));
				scriptRunner.setStopOnError(true);
				scriptRunner.runScript(stringReader, dbType, 0, 0);
				DbVersion dbVersion = new DbVersion();
				dbVersion.setModuleId(module.getId());
				dbVersion.setVersion(module.getDbVersion());
				dbVersion.setVersionStr(Integer.toString(module.getDbVersion()));
				JdbcUtils.insertVersion(conn, dbVersion);
			}
			ProductVersion productVersion = applicationBean.getProductVersion();
			DbVersion dbVersion = new DbVersion();
			dbVersion.setModuleId("DCEM");
			dbVersion.setVersion(productVersion.getVersionInt());
			dbVersion.setVersionStr(productVersion.getVersionStr());
			JdbcUtils.insertVersion(conn, dbVersion);
			conn.commit();
		} finally {
			if (stmt != null) {
				stmt.close();
			}
			conn.close();
		}
		return;
	}

	private void createDefaultTemplates() throws DcemException {
		/**
		 * adding default Templates
		 * 
		 */
		List<FileContent> templates;
		try {
			templates = ResourceFinder.find(CreateTenant.class, DcemConstants.TEMPLATE_RESOURCES, DcemConstants.TEMPLATE_TYPE);
		} catch (Exception exp) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Couldn't find templates", exp);
		}

		for (FileContent template : templates) {
			SupportedLanguage supportedLanguage = null;
			String fileName = template.getName().substring(0, template.getName().length() - DcemConstants.TEMPLATE_TYPE.length());
			String locale = fileName.substring(fileName.length() - 2);
			if (fileName.charAt(fileName.length() - 3) != '_') {
				logger.info("Invalid Tempalte name format. " + fileName);
				continue;
			}
			fileName = fileName.substring(0, fileName.length() - 3);
			supportedLanguage = DcemUtils.getSuppotedLanguage(locale); // default is always english
			DcemTemplate dcemTemplate = new DcemTemplate();
			dcemTemplate.setName(fileName);
			if (supportedLanguage == SupportedLanguage.English) {
				dcemTemplate.setDefaultTemplate(true);
			}
			dcemTemplate.setLanguage(supportedLanguage);
			dcemTemplate.setContent(StringUtils.getStringFromUtf8(template.getContent()));
			templateLogic.addOrUpdateTemplate(dcemTemplate, new DcemAction(AdminModule.MODULE_ID, null, DcemConstants.ACTION_ADD), false);
		}
	}

	// public List<TenantEntity> getOperatorsByLdap(LdapEntity ldap) {
	// TypedQuery<DcemOperator> query = em.createNamedQuery(DcemOperator.GET_BY_LDAP, DcemOperator.class);
	// query.setParameter(1, ldap);
	// return query.getResultList();
	// }
}
