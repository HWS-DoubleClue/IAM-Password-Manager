package com.doubleclue.dcem.saml.logic;

import java.util.HashMap;
import java.util.Map;

import com.doubleclue.dcem.core.logic.module.ModuleTenantData;
import com.doubleclue.dcem.saml.entities.SamlSpMetadataEntity;

public class SamlTenantData extends ModuleTenantData {
	
	private Map<String, SamlSpMetadataEntity> metadataMap = new HashMap<String, SamlSpMetadataEntity>();

	public Map<String, SamlSpMetadataEntity> getMetadataMap() {
		return metadataMap;
	}

	public void setMetadataMap(Map<String, SamlSpMetadataEntity> metadataMap) {
		this.metadataMap = metadataMap;
	}

}
