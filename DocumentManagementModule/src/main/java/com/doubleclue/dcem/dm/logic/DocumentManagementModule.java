package com.doubleclue.dcem.dm.logic;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.solr.client.solrj.impl.Http2SolrClient;

import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.as.logic.AsTenantData;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.dm.preferences.DmPreferences;
import com.doubleclue.dcem.system.logic.SystemPreferences;

@ApplicationScoped
public class DocumentManagementModule extends DcemModule {

	public static final String MODULE_ID = "dm";
	public static final String RESOURCE_NAME = "com.doubleclue.dcem.dm.resources.Messages";

	private static final long serialVersionUID = 1L;

	@Inject
	DocumentLogic documentLogic;
	
	@Inject
	DmWorkflowLogic workflowLogic;
	
	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	DmSolrLogic solrLogic;

	@Override
	public void init() throws DcemException {

	}

	public String getName() {
		return "DocumentManagement";
	}

	@Override
	public String receiveMail(String subjectName, String identifier, File emlFile) throws Exception {
		return documentLogic.addEmailDocument(subjectName, identifier, emlFile);
	}
	
	public void start() throws DcemException {
	//	System.out.println("DocumentManagementModule.start() " + TenantIdResolver.getCurrentTenantName());
	}

	@DcemTransactional
	@Override
	public void initializeTenant(TenantEntity tenantEntity) throws DcemException {
		DmTenantData tenantData = new DmTenantData();
		super.initializeTenant(tenantEntity, tenantData);
		try {
			solrLogic.initializeSolr(tenantEntity);
		} catch (Exception e) {
			reportingLogic.addWelcomeViewAlert(DcemConstants.ALERT_CATEGORY_DCEM, DcemErrorCodes.SOLR_NO_CONNECTION, "Solr : " + e.getMessage(), AlertSeverity.ERROR,
					false, e.getMessage());
		}
	}
	
	@Override
	public void runNightlyTask() {
//		System.out.println("DocumentManagementModule.runNightlyTask()");
		workflowLogic.nightlyTask();
	}

	@Override
	public String getId() {
		return MODULE_ID;
	}

	public int getRank() {
		return 999;
	}

	@Override
	public DcemView getDefaultView() {
		return null;
	}

	public ModulePreferences getDefaultPreferences() {
		return new DmPreferences();
	}

	public DmPreferences getPreferences() {
		return (DmPreferences) super.getModulePreferences();
	}
	
	public DmPreferences getMasterPreferences() {
		return (DmPreferences) getModuleTenantData(TenantIdResolver.getMasterTenant()).getModulePreferences();
	}
	
	public DmTenantData getDmTenantData() {
		return (DmTenantData) getModuleTenantData();
	}

	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}

}
