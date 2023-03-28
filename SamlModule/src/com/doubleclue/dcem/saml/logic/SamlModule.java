package com.doubleclue.dcem.saml.logic;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.x509.BasicX509Credential;

import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.config.ConnectionService;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.config.KeyStorePurpose;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.KeyStoreEntity;
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
import com.doubleclue.dcem.saml.entities.SamlSpMetadataEntity;
import com.doubleclue.dcem.saml.preferences.SamlPreferences;
import com.doubleclue.dcem.system.logic.KeyStoreLogic;

@SuppressWarnings("serial")
@ApplicationScoped
@Named("samlModule")
public class SamlModule extends DcemModule {

	@Inject
	KeyStoreLogic keyStoreLogic;

	int dbVersion = 3;

	@Inject
	UserLogic userLogic;

	@Inject
	RoleLogic roleLogic;

	@Inject
	SamlLogic samlLogic;

	@Inject
	ConfigLogic configLogic;

	private static Logger logger = LogManager.getLogger(SamlModule.class);
	private DcemUser dcemUser;

	public final static String MODULE_ID = "saml";
	public final static String RESOURCE_NAME = "com.doubleclue.dcem.saml.resources.Messages";

	private Map<String, Integer> loginCounters = new HashMap<String, Integer>();

	// KEYSTORE
	private KeyStoreEntity idpKeyStoreEntity;
	private final static KeyStorePurpose ksPurpose = KeyStorePurpose.Saml_IdP_CA;
	private final static String ksAlias = ksPurpose.name();
	private Credential credential;

	@Override
	public void init() throws DcemException {
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
	public boolean isPluginModule() {
		return false;
	}

	@Override
	public ModulePreferences getDefaultPreferences() {
		return new SamlPreferences();
	}

	@Override
	public SamlPreferences getModulePreferences() {
		SamlPreferences samlPreferences = (SamlPreferences) super.getModulePreferences();
		if (samlPreferences != null) {
			String ssoDomain = samlPreferences.getSsoDomain();
			if (ssoDomain == null || ssoDomain.isEmpty()) {
				try {
					URL url = new URL(JsfUtils.getHttpServletRequest().getRequestURL().toString());
					ConnectionService service = null;
					if (TenantIdResolver.isCurrentTenantMaster()) {
						service = configLogic.getClusterConfig().getConnectionService(ConnectionServicesType.SAML);
					}
					if (service != null) {
						ssoDomain = (service.isSecure() ? "https" : "http") + "://" + url.getHost() + ":" + service.getPort();
					} else {
						ssoDomain = url.getProtocol() + "://" + url.getHost();
					}
					samlPreferences.setSsoDomain(ssoDomain);
				} catch (Exception e) {
					logger.error("Cannot create default SAML IdP Entity ID", e);
				}
			}
		}
		return samlPreferences;
	}

	@Override
	public String getName() {
		return "SAML";
	}

	@Override
	public String getId() {
		return MODULE_ID;
	}

	@Override
	public int getRank() {
		return 60;
	}

	@Override
	public void start() throws DcemException {
		super.start();
		try {
			getIdPKeyStoreEntity();
			InitializationService.initialize();
		} catch (InitializationException e) {
			logger.warn("Could not initialise OpenSaml library: " + e.getMessage());
		}
	}

	private KeyStoreEntity getIdPKeyStoreEntity() {

		if (idpKeyStoreEntity == null) {
			List<KeyStoreEntity> keyStores = keyStoreLogic.getKeyStoreByPurpose(ksPurpose);
			if (!keyStores.isEmpty()) {
				idpKeyStoreEntity = keyStores.get(0);
			} else {
				logger.error("No SAML IdP Keystore Entity found.");
			}
		}
		return idpKeyStoreEntity;
	}

	private KeyStore getIdpKeyStore() {

		KeyStoreEntity keyStoreEntity = getIdPKeyStoreEntity();
		if (keyStoreEntity != null) {
			try {
				KeyStore keyStore = KeyStore.getInstance("PKCS12");
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(keyStoreEntity.getKeyStore());
				keyStore.load(byteArrayInputStream, keyStoreEntity.getPassword().toCharArray());
				byteArrayInputStream.close();
				return keyStore;
			} catch (Exception e) {
				logger.error("Couldn't obtain SAML IdP KeyStore.", e);
			}
		}
		return null;
	}

	public Certificate getIdpCertificate() {

		KeyStore keyStore = getIdpKeyStore();
		if (keyStore != null) {
			try {
				return keyStore.getCertificate(ksAlias);
			} catch (KeyStoreException e) {
				logger.error("Couldn't obtain SAML IdP Certificate.", e);
			}
		}
		return null;
	}

	private PrivateKey getIdpPrivateKey() {

		KeyStoreEntity keyStoreEntity = getIdPKeyStoreEntity();
		if (keyStoreEntity != null) {
			KeyStore idpKeyStore = getIdpKeyStore();
			if (idpKeyStore != null) {
				try {
					return (PrivateKey) idpKeyStore.getKey(ksAlias, keyStoreEntity.getPassword().toCharArray());
				} catch (Exception e) {
					logger.error("Couldn't obtain SAML IdP Private Key.", e);
				}
			}
		}
		return null;
	}

	@Override
	public int getDbVersion() {
		return dbVersion;
	}

		
	public DcemUser getOperator() throws DcemException {
		if (dcemUser == null) {
			dcemUser = userLogic.getUser(DcemConstants.SAML_OPERATOR_NAME);
			if (dcemUser == null) {
				DcemRole role = roleLogic.getDcemRole(DcemConstants.SYSTEM_ROLE_ADMIN);
				dcemUser = new DcemUser(DcemConstants.SAML_OPERATOR_NAME, null, DcemConstants.SAML_OPERATOR_NAME, role);
				userLogic.addOrUpdateUserWoAuditing(dcemUser);
				}
		}
		return dcemUser;
	}

	public Credential getIdpCredential() {
		if (credential == null) {
			Certificate certificate = getIdpCertificate();
			PrivateKey privateKey = getIdpPrivateKey();
			credential = new BasicX509Credential((X509Certificate) certificate, privateKey);
		}
		return credential;
	}

	public List<String> getTextResourceKeys() {
		List<String> keys = new ArrayList<>();
		return keys;
	}

	public void addLogin(String spEntityId) {
		int count = loginCounters.containsKey(spEntityId) ? loginCounters.get(spEntityId) + 1 : 1;
		loginCounters.put(spEntityId, count);
	}

	@Override
	public Map<String, String> getStatisticValues() {

		Map<String, String> map = super.getStatisticValues();

		int total = 0;
		for (Map.Entry<String, Integer> entry : loginCounters.entrySet()) {
			int logins = entry.getValue();
			map.put("SAML Logins - " + entry.getKey(), Integer.toString(logins));
			total += logins;
		}

		map.put("SAML Logins - Total", Integer.toString(total));
		return map;
	}

	public void initializeTenant(TenantEntity tenantEntity) throws DcemException {
		SamlTenantData samlTenantData = (SamlTenantData) getModuleTenantData();
		if (samlTenantData == null) {
			SamlTenantData tenantData = new SamlTenantData();
			super.initializeTenant(tenantEntity, tenantData);
		}
	}

	@Override
	public List<PolicyAppEntity> getPolicyApplications() {
		// this could be called pior to tenant initialize
		try {
			initializeTenant(TenantIdResolver.getCurrentTenant());
		} catch (DcemException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		List<SamlSpMetadataEntity> spMetadataEntities = samlLogic.getAllSpMetadataEntities();
		List<PolicyAppEntity> policyAppEntities = new ArrayList<>(spMetadataEntities.size());
		policyAppEntities.add(new PolicyAppEntity(AuthApplication.SAML, 0, null));
		for (SamlSpMetadataEntity entity : spMetadataEntities) {
			policyAppEntities.add(new PolicyAppEntity(AuthApplication.SAML, entity.getId(), entity.getDisplayName()));
		}
		return policyAppEntities;
	}
}
