package com.doubleclue.dcem.core.tasks;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.licence.LicenceLogic;
import com.doubleclue.dcem.core.logic.ActionLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;

public class CallInittializeTenant implements Callable<Hashtable<String, Exception>> {

	private static final Logger logger = LogManager.getLogger(CallInittializeTenant.class);

	TenantEntity tenantEntity;
	List<DcemModule> modules;
	public CallInittializeTenant(TenantEntity tenantEntity, List<DcemModule> modules) {
		this.tenantEntity = tenantEntity;
		this.modules = modules;
	}

	@Override
	public Hashtable<String, Exception> call() {
		TenantIdResolver.setCurrentTenant(tenantEntity);
		WeldRequestContext requestContext = null;
		try {
			Hashtable<String, Exception> exceptionTable = new Hashtable<>();
 			Thread.currentThread().setName(this.getClass().getSimpleName());
			requestContext = WeldContextUtils.activateRequestContext();
			
			for (DcemModule dcemModule : modules) {
				logger.info("Initializing " + tenantEntity.getName() + " Module: " + dcemModule.getName());
				if(dcemModule.isMasterOnly() == true && tenantEntity.isMaster() == false) {
					continue;
				}
				try {
					dcemModule.initializeTenant(tenantEntity);
				} catch (DcemException exp) {
					logger.fatal("Tenant Initialization failed by module: " + dcemModule.getName(), exp);
					exceptionTable.put(dcemModule.getName(), exp);
//					return exp;
				} catch (Exception exp) {
					logger.fatal("Tenant Initialization failed by module: " + dcemModule.getName(), exp);
					exceptionTable.put(dcemModule.getName(), exp);
//					return exp;
				}
			}
	 		LicenceLogic licenceLogic = CdiUtils.getReference(LicenceLogic.class);
			try {
				licenceLogic.reload(null);
			} catch (DcemException exp) {
				logger.warn("Loading licence failed during tenant initialisation: " + exp.getMessage());
				exceptionTable.put(AdminModule.MODULE_ID + "-LICENSE", exp);
				return exceptionTable;
			}
			try {
				ActionLogic actionLogic = CdiUtils.getReference(ActionLogic.class);
				actionLogic.createDbActions(tenantEntity);
			} catch (Exception exp) {
				logger.warn("Couldn't add module actions: ", exp);
				exceptionTable.put(AdminModule.MODULE_ID + "-CreateAction", exp);
				return exceptionTable;
			}
			return exceptionTable;
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
	}

}
