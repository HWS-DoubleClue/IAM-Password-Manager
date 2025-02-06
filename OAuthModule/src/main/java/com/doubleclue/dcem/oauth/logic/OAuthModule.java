package com.doubleclue.dcem.oauth.logic;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ConnectionService;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.oauth.entities.OAuthClientEntity;
import com.doubleclue.dcem.oauth.entities.OAuthTokenId;
import com.doubleclue.dcem.oauth.preferences.OAuthPreferences;
import com.doubleclue.oauth.openid.OpenIdAuthenticationRequest;
import com.hazelcast.core.IMap;

@SuppressWarnings("serial")
@ApplicationScoped
@Named("oauthModule")
public class OAuthModule extends DcemModule {

	@Inject
	UserLogic userLogic;

	@Inject
	RoleLogic roleLogic;

	@Inject
	OAuthLogic oauthLogic;

	@Inject
	ConfigLogic configLogic;

	private static Logger logger = LogManager.getLogger(OAuthModule.class);

	public final static String MODULE_ID = "oauth";
	public final static String RESOURCE_NAME = "com.doubleclue.dcem.oauth.resources.Messages";

	private DcemUser dcemUser;

	@Override
	public void init() throws DcemException {
		setDbVersion(1); // DCEM 2.3.1
	}
	
	@Override
	public boolean isPluginModule() {
		return false;
	}

	@Override
	public String getResourceName() {
		return RESOURCE_NAME;
	}

	@Override
	public DcemView getDefaultView() {
		return null;
	}

	@Override
	public ModulePreferences getDefaultPreferences() {
		return new OAuthPreferences();
	}

	@Override
	public String getName() {
		return "OpenID/OAuth";
	}

	@Override
	public String getId() {
		return MODULE_ID;
	}

	@Override
	public int getRank() {
		return 70;
	}

	@Override
	public OAuthPreferences getModulePreferences() {
		OAuthPreferences preferences = (OAuthPreferences) super.getModulePreferences();
		if (preferences != null) {
			String issuer = preferences.getIssuer();
			if (issuer == null || issuer.isEmpty() && JsfUtils.getFacesContext() != null) {
				try {
					URL url = new URL(JsfUtils.getHttpServletRequest().getRequestURL().toString());
					ConnectionService service = null;
					if (TenantIdResolver.isCurrentTenantMaster()) {
						service = configLogic.getClusterConfig().getConnectionService(ConnectionServicesType.OPENN_ID_OAUTH);
					}
					if (service != null) {
						issuer = (service.isSecure() ? "https" : "http") + "://" + url.getHost() + ":" + service.getPort();
					} else {
						issuer = url.getProtocol() + "://" + url.getHost();
					}
					preferences.setIssuer(issuer);
				} catch (Exception e) {
					logger.warn("Cannot create default OAuth Issuer", e);
				}
			}
		}
		return preferences;
	}

	@Override
	public OAuthTenantData getModuleTenantData() {
		return (OAuthTenantData) super.getModuleTenantData();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initializeTenant(TenantEntity tenantEntity) throws DcemException {
		OAuthTenantData oauthTenantData = (OAuthTenantData) getModuleTenantData();
		if (oauthTenantData == null) {
			oauthTenantData = new OAuthTenantData();
			super.initializeTenant(tenantEntity, oauthTenantData);
		}

		String tenantName = tenantEntity.getName();
		DcemCluster dcemCluster = DcemCluster.getInstance();
		oauthTenantData.setAuthCodes((IMap<String, OAuthAuthCodeInfo>) dcemCluster.getMap("oauthAuthCodes@" + tenantName));
		oauthTenantData.setAuthnRequests((IMap<OAuthTokenId, OpenIdAuthenticationRequest>) dcemCluster.getMap("oauthAuthnRequests@" + tenantName));
	}

	@Override
	public List<PolicyAppEntity> getPolicyApplications() {
		try {
			initializeTenant(TenantIdResolver.getCurrentTenant());
		} catch (DcemException e) {
			e.printStackTrace();
		}
		List<OAuthClientEntity> clientMetadataEntities = oauthLogic.getAllClientMetadataEntities();
		List<PolicyAppEntity> policyAppEntities = new ArrayList<>(clientMetadataEntities.size());
		policyAppEntities.add(new PolicyAppEntity(AuthApplication.OAUTH, 0, null));
		for (OAuthClientEntity entity : clientMetadataEntities) {
			policyAppEntities.add(new PolicyAppEntity(AuthApplication.OAUTH, entity.getId(), entity.getDisplayName()));
		}
		return policyAppEntities;
	}

//	public DcemUser getOperator() throws DcemException {
//		if (dcemUser == null) {
//			dcemUser = userLogic.getUser(DcemConstants.OAUTH_OPERATOR_NAME);
//			if (dcemUser == null) {
//				DcemRole role = roleLogic.getDcemRole(DcemConstants.SYSTEM_ROLE_ADMIN);
//				dcemUser = new DcemUser(DcemConstants.OAUTH_OPERATOR_NAME, null, DcemConstants.OAUTH_OPERATOR_NAME, role);
//				userLogic.addOrUpdateUserWoAuditing(dcemUser);
//			}
//		}
//		return dcemUser;
//	}
}
