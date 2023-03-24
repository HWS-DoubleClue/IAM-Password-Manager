package com.doubleclue.dcem.admin.logic;

import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;

import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantBrandingEntity;
import com.doubleclue.dcem.core.licence.LicenceKeyContent;
import com.doubleclue.dcem.core.logic.DbResourceBundle;
import com.doubleclue.dcem.core.logic.DomainApi;
import com.doubleclue.dcem.core.logic.module.ModuleTenantData;
import com.hazelcast.flakeidgen.FlakeIdGenerator;

public class AdminTenantData extends ModuleTenantData {

	private Map<String, DbResourceBundle> bundleCache = new Hashtable<String, DbResourceBundle>();
	private LinkedHashMap<String, DomainApi> domains = new LinkedHashMap<>();
	private TenantBrandingEntity tenantBrandingEntity;
	private FlakeIdGenerator reportIdGenerator;
	private DcemGroup rootGroup = null;
	private String [] disabledModules;
	private String [] enabledPluginModules;
	private LicenceKeyContent licenceKeyContent;
	private DcemUser superAdmin;
	
	public Map<String, DbResourceBundle> getBundleCache() {
		return bundleCache;
	}

	public void setBundleCache(Map<String, DbResourceBundle> bundleCache) {
		this.bundleCache = bundleCache;
	}

	public LinkedHashMap<String, DomainApi> getDomains() {
		return domains;
	}

	public void setDomains(LinkedHashMap<String, DomainApi> domains) {
		this.domains = domains;
	}

	public TenantBrandingEntity getTenantBrandingEntity() {
		return tenantBrandingEntity;
	}

	public void setTenantBrandingEntity(TenantBrandingEntity tenantBrandingEntity) {
		this.tenantBrandingEntity = tenantBrandingEntity;
	}

	public FlakeIdGenerator getReportIdGenerator() {
		return reportIdGenerator;
	}

	public void setReportIdGenerator(FlakeIdGenerator reportIdGenerator) {
		this.reportIdGenerator = reportIdGenerator;
	}

	public DcemGroup getRootGroup() {
		return rootGroup;
	}

	public void setRootGroup(DcemGroup rootGroup) {
		this.rootGroup = rootGroup;
	}

	public LicenceKeyContent getLicenceKeyContent() {
		return licenceKeyContent;
	}

	public void setLicenceKeyContent(LicenceKeyContent licenceKeyContent) {
		this.licenceKeyContent = licenceKeyContent;
	}

	public String[] getDisabledModules() {
		return disabledModules;
	}

	public void setDisabledModules(String[] disabledModules) {
		this.disabledModules = disabledModules;
	}

	public DcemUser getSuperAdmin() {
		return superAdmin;
	}

	public void setSuperAdmin(DcemUser superAdmin) {
		this.superAdmin = superAdmin;
	}

	public String[] getEnabledPluginModules() {
		return enabledPluginModules;
	}

	public void setEnabledPluginModules(String[] enabledPluginModules) {
		this.enabledPluginModules = enabledPluginModules;
	}

	
}
