package com.doubleclue.dcem.setup.logic;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.TextResourceLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;

public class DbMigrateTenantTask implements Callable<Exception> {

	private static final Logger logger = LogManager.getLogger(DbMigrateTenantTask.class);

	private final TenantEntity tenantEntity;
	private final List<ModuleMigrationVersion> migrationModules;

	public DbMigrateTenantTask(TenantEntity tenantEntity, List<ModuleMigrationVersion> migrationModules) {
		this.tenantEntity = tenantEntity;
		this.migrationModules = migrationModules;
	}

	@Override
	public Exception call() {
		TenantIdResolver.setCurrentTenant(tenantEntity);
		WeldRequestContext requestContext = null;
		try {
			Thread.currentThread().setName(this.getClass().getSimpleName());
			requestContext = WeldContextUtils.activateRequestContext();
			DbMigrate dbMigrate = CdiUtils.getReference(DbMigrate.class);
			dbMigrate.updateTemplates();
			dbMigrate.updateMyApplications();

			TextResourceLogic textResourceLogic = CdiUtils.getReference(TextResourceLogic.class);
			textResourceLogic.createDefaultTextResources(false);

			if (migrationModules != null) {
				for (ModuleMigrationVersion moduleMigrationVersion : migrationModules) {
					switch (moduleMigrationVersion.updateToVersion) {
					case "2.5.0":
					case "2.5.0-SNAPSHOT":
						dbMigrate.migrateTo_2_5_0();
						break;
					default:
						break;
					}
				}
			}

			return null;
		} catch (Exception e) {
			logger.error("Error while setting up Default Text Resources during migration: " + e.getMessage());
			return e;
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
	}
}
