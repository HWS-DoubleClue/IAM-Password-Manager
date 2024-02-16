package com.doubleclue.dcem.system.logic;

import java.util.HashMap;
import java.util.Map;

import com.doubleclue.dcem.core.logic.module.ModuleTenantData;

public class SystemTenantData extends ModuleTenantData {
	
	Map<String, Integer> billingItemReadCache = new HashMap<String, Integer>();

	public Map<String, Integer> getBillingItemReadCache() {
		return billingItemReadCache;
	}

	public void setBillingItemReadCache(Map<String, Integer> billingItemReadCache) {
		this.billingItemReadCache = billingItemReadCache;
	}
}
