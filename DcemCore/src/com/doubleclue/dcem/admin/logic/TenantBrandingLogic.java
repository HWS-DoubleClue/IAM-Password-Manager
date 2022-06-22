//#excludeif COMMUNITY_EDITION
package com.doubleclue.dcem.admin.logic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemConfiguration;
import com.doubleclue.dcem.core.entities.TenantBrandingEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.tasks.ReloadClassInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
@Named("tenantBrandingLogic")
public class TenantBrandingLogic implements ReloadClassInterface {

	@Inject
	ConfigLogic configLogic;
	
	@Inject
	AdminModule adminModule;

	private Logger logger = LogManager.getLogger(TenantBrandingLogic.class);

	public TenantBrandingEntity getTenantBrandingEntity() {
		TenantBrandingEntity brandingEntity;
		try {
			DcemConfiguration dcemConfiguration = configLogic.getDcemConfiguration(AdminModule.MODULE_ID, DcemConstants.CONFIG_KEY_TENANT_BRANDING);
			if (dcemConfiguration == null) {
				brandingEntity = new TenantBrandingEntity();
			} else {
				brandingEntity = new ObjectMapper().readValue(dcemConfiguration.getValue(), TenantBrandingEntity.class);
			}
		} catch (Exception e) {
			logger.warn(e);
			brandingEntity = new TenantBrandingEntity();
		}
		return brandingEntity;
	}
	
	public void setTenantBrandingEntity(TenantBrandingEntity tenantBrandingEntity) throws DcemException {
		byte[] value;
		try {
			value = new ObjectMapper().writeValueAsBytes(tenantBrandingEntity);
		} catch (JsonProcessingException e) {
			logger.warn(e);
			throw new DcemException(DcemErrorCodes.SERIALIZATION_ERROR, TenantBrandingEntity.class.getName());
		}
		DcemConfiguration dcemConfiguration = configLogic.getDcemConfiguration(AdminModule.MODULE_ID, DcemConstants.CONFIG_KEY_TENANT_BRANDING);
		if (dcemConfiguration == null) {
			dcemConfiguration = new DcemConfiguration(AdminModule.MODULE_ID, DcemConstants.CONFIG_KEY_TENANT_BRANDING, value);
		} else {
			dcemConfiguration.setValue(value);
		}
		configLogic.setDcemConfiguration(dcemConfiguration);
	}
	@Override
	public void reload() throws DcemException {
		TenantBrandingEntity tenantBrandingEntity = getTenantBrandingEntity();
		adminModule.getAdminTenantData().setTenantBrandingEntity(tenantBrandingEntity);		
	}

}
