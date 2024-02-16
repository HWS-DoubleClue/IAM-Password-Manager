package com.doubleclue.dcem.core.tasks;

import java.io.Serializable;

import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;

/**
 * 
 * This class is used from Cluster
 * @author Emanuel
 *
 */
public class SavePreferencesTask implements Runnable, Serializable {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SavePreferencesTask (String tenantName, String moduleId, ModulePreferences modulePreferencesPrevious, ModulePreferences modulePreferencesNew) {
		super();
		this.moduleId = moduleId;
		this.modulePreferencesPrevious = modulePreferencesPrevious;
		this.modulePreferencesNew = modulePreferencesNew;
		this.tenantName = tenantName;		
	}

	String tenantName;
	String moduleId;
	ModulePreferences modulePreferencesPrevious;
	ModulePreferences modulePreferencesNew;

    @Override
    public void run()  {
    	WeldRequestContext requestContext = null;
		try {
			requestContext = WeldContextUtils.activateRequestContext();
			DcemApplicationBean applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
			TenantEntity tenantEntity = applicationBean.getTenant(tenantName);
			TenantIdResolver.setCurrentTenant(tenantEntity);
			DcemModule module = applicationBean.getModule(moduleId);
			module.savePreferences(modulePreferencesPrevious, modulePreferencesNew);
			
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}    
        return;
    }
}
