package com.doubleclue.dcem.saml.logic;

import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.util.AttributeMap;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.ext.saml2alg.impl.DigestMethodImpl;
import org.opensaml.saml.ext.saml2alg.impl.SigningMethodImpl;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.AttributeConsumingService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.Extensions;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.RequestedAttribute;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.AttributeTypeEnum;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.ClaimAttribute;
import com.doubleclue.dcem.saml.entities.SamlSpMetadataEntity;
import com.doubleclue.dcem.saml.logic.enums.DigestAlgorithmEnum;
import com.doubleclue.dcem.saml.logic.enums.NameIdFormatEnum;
import com.doubleclue.dcem.saml.logic.enums.SamlErrorCodes;
import com.doubleclue.dcem.saml.logic.enums.SignatureAlgorithmEnum;
import com.doubleclue.dcem.saml.preferences.SamlPreferences;
import com.doubleclue.dcem.saml.tasks.UpdateSpMetadataCacheTask;
import com.doubleclue.dcem.system.logic.KeyStoreLogic;
import com.doubleclue.utils.FileContent;
import com.doubleclue.utils.ResourceFinder;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.hazelcast.core.MultiExecutionCallback;

import net.shibboleth.utilities.java.support.xml.SerializeSupport;

@ApplicationScoped
@Named("samlLogic")
public class SamlLogic implements MultiExecutionCallback {

	private static final Logger logger = LogManager.getLogger(SamlLogic.class);

	@Inject
	EntityManager em;

	@Inject
	SamlModule samlModule;

	@Inject
	KeyStoreLogic keyStoreLogic;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	PolicyLogic policyLogic;

	private Map<String, SamlSpMetadataEntity> presetMetadataMap = new HashMap<String, SamlSpMetadataEntity>();
	private static final QName userPropertyQName = new QName("UserProperty");

	// Private Methods

	private Element getXmlFromString(String xmlString) throws ParserConfigurationException, SAXException, IOException {

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		InputSource inputSource = new InputSource(new StringReader(xmlString));
		Document doc = dBuilder.parse(inputSource);

		return doc.getDocumentElement();
	}

	private boolean isValidURL(String url) {
		try {
			new URL(url).toURI();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// Public Methods

	@Override
	public void onComplete(Map<Member, Object> arg0) {
	}

	@Override
	public void onResponse(Member arg0, Object arg1) {
	}

	public String getIdpMetadataString() throws DcemException {

		try {

			SamlPreferences preferences = samlModule.getModulePreferences();

			String entityId = preferences.getIdpEntityId();
			if (entityId == null || entityId.isEmpty()) {
				throw new DcemException(DcemErrorCodes.MISSING_SAML_PREFERENCE, "No EntityID is defined in Preferences.");
			}

			String ssoDomain = preferences.getSsoDomain();
			if (ssoDomain == null || ssoDomain.isEmpty()) {
				throw new DcemException(DcemErrorCodes.MISSING_SAML_PREFERENCE, "No SSO Domain is defined in Preferences.");
			}
			String ssoServiceEndpoint = null;
			if (TenantIdResolver.isCurrentTenantMaster() == true) {
				ssoServiceEndpoint = ssoDomain + DcemConstants.DEFAULT_WEB_NAME + DcemConstants.SAML_SERVLET_PATH;
			} else {
				ssoServiceEndpoint = ssoDomain + DcemConstants.DEFAULT_WEB_NAME + DcemConstants.SAML_SERVLET_PATH + "?" + DcemConstants.URL_TENANT_PARAMETER
						+ "=" + TenantIdResolver.getCurrentTenantName();
			}

			NameIdFormatEnum[] nameIdFormats = { NameIdFormatEnum.UNSPECIFIED, NameIdFormatEnum.EMAIL };

			Credential credential = null;
			Certificate certificate = samlModule.getIdpCertificate();
			if (certificate != null) {
				credential = new BasicX509Credential((X509Certificate) certificate);
			}

			EntityDescriptor entityDescriptor = SamlUtils.createIdpMetadata(entityId, ssoServiceEndpoint, true, nameIdFormats, credential);
			Element element = SamlUtils.marshallXmlObject(entityDescriptor);
			return SerializeSupport.nodeToString(element);

		} catch (DcemException e) {
			throw e;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString(), e);
		}
	}

	public String getIdPCertificatePem() {
		try {
			Certificate certificate = samlModule.getIdpCertificate();
			String certificateString = SamlUtils.getBase64StringFromX509Certificate((X509Certificate) certificate);
			return "-----BEGIN CERTIFICATE-----\n" + certificateString + "\n-----END CERTIFICATE-----";
		} catch (CertificateEncodingException e) {
			return null;
		}
	}

	public Map<String, SamlSpMetadataEntity> getPresetMetadataMap() {
		// if (presetMetadataMap.size() == 0) {
		presetMetadataMap.clear();
		try {

			List<FileContent> files = ResourceFinder.find(SamlLogic.class, SamlConstants.RESOURCE_DIR_PRESETS, SamlConstants.RESOURCE_TYPE_PRESETS);
			for (FileContent file : files) {
				byte[] encoded = file.getContent();
				String xml = new String(encoded, DcemConstants.CHARSET_UTF8);
				SamlSpMetadataEntity entity = new SamlSpMetadataEntity();
				setUpEntityFromXml(xml, entity, false);
				presetMetadataMap.put(entity.getDisplayName(), entity);
			}
		} catch (Exception e) {
			logger.warn("SAML - Could not load preset configurations.", e);
		}
		// }

		return presetMetadataMap;
	}

	/**
	 * @param xml
	 * @param entity
	 * @param validateContents
	 * @throws DcemException
	 */
	public void setUpEntityFromXml(String xml, SamlSpMetadataEntity entity, boolean validateContents) throws DcemException {

		if (xml != null && !xml.isEmpty()) {

			Element metadataElement;
			try {
				metadataElement = getXmlFromString(xml);
			} catch (ParserConfigurationException | SAXException | IOException e) {
				throw new DcemException(DcemErrorCodes.INVALID_SAML_METADATA, e.getLocalizedMessage());
			}

			EntityDescriptor entityDescriptor;
			try {
				entityDescriptor = (EntityDescriptor) SamlUtils.unmarshallElement(metadataElement);
			} catch (UnmarshallingException e) {
				throw new DcemException(DcemErrorCodes.INVALID_SAML_METADATA, e.getLocalizedMessage());
			}

			String entityID = entityDescriptor.getEntityID();
			if (entityID != null && !entityID.isEmpty()) {
				entity.setEntityId(entityID);
			} else {
				if (validateContents) {
					throw new DcemException(DcemErrorCodes.INVALID_SAML_METADATA,
							JsfUtils.getStringSafely(samlModule.getResourceName(), "error.noEntityIdFound"));
				}
			}

			// Find SP SSO Descriptor
			SPSSODescriptor ssoDescriptor = entityDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
			if (ssoDescriptor == null) {
				throw new DcemException(DcemErrorCodes.INVALID_SAML_METADATA,
						JsfUtils.getStringSafely(samlModule.getResourceName(), "error.noSpSsoDescriptorFound"));
			}

			// Find Assertion Consumer Service Location
			List<AssertionConsumerService> assertionConsumerServices = ssoDescriptor.getAssertionConsumerServices();
			AssertionConsumerService httpPostAssertionConsumerService = null;
			if (assertionConsumerServices != null) {
				for (AssertionConsumerService assertionConsumerService : assertionConsumerServices) {
					if (assertionConsumerService.getBinding().equals(SAMLConstants.SAML2_POST_BINDING_URI)) {
						httpPostAssertionConsumerService = assertionConsumerService;
						break;
					}
				}
			}

			if (httpPostAssertionConsumerService != null && httpPostAssertionConsumerService.getLocation() != null) {
				entity.setAcsLocation(httpPostAssertionConsumerService.getLocation());
			} else {
				if (validateContents) {
					throw new DcemException(DcemErrorCodes.INVALID_SAML_METADATA, JsfUtils.getStringSafely(samlModule.getResourceName(), "error.noAcsFound"));
				}
			}

			// Find Single Logout Service
			List<SingleLogoutService> logoutServices = ssoDescriptor.getSingleLogoutServices();
			if (logoutServices != null && logoutServices.size() > 0) {
				SingleLogoutService logoutService = logoutServices.get(0);
				if (logoutService.getLocation() != null) {
					entity.setLogoutLocation(logoutService.getLocation());
					entity.setLogoutIsPost(logoutService.getBinding().equals(SAMLConstants.SAML2_POST_BINDING_URI));
				}
			}

			// Find whether the SP specifies whether requests are signed
			if (ssoDescriptor.isAuthnRequestsSignedXSBoolean() != null) {
				entity.setRequestsSigned(ssoDescriptor.isAuthnRequestsSigned());
			} else {
				entity.setRequestsSigned(true);
			}

			// Find Public Key for signed AuthnRequests (if any)
			List<KeyDescriptor> keyDescriptors = ssoDescriptor.getKeyDescriptors();
			if (keyDescriptors != null) {
				for (KeyDescriptor keyDescriptor : keyDescriptors) {
					if (keyDescriptor.getUse() == UsageType.SIGNING) {
						KeyInfo keyInfo = keyDescriptor.getKeyInfo();
						if (keyInfo != null) {
							try {
								entity.setCertificateString(SamlUtils.getX509CertificateStringFromKeyInfo(keyInfo));
							} catch (CertificateException e) {
								throw new DcemException(DcemErrorCodes.INVALID_SAML_METADATA, e.getLocalizedMessage());
							}
						}
						break;
					}
				}
			}

			// Find the NameID Format (if any) which has the loosest restriction
			NameIdFormatEnum loosestNameIdFormat = null;
			List<NameIDFormat> nameIdFormats = ssoDescriptor.getNameIDFormats();
			if (nameIdFormats != null) {
				for (NameIDFormat nameIdFormat : nameIdFormats) {
					String formatString = nameIdFormat.getFormat();
					NameIdFormatEnum format;
					try {
						format = NameIdFormatEnum.getFromString(formatString);
					} catch (IllegalArgumentException e) {
						format = NameIdFormatEnum.UNSPECIFIED;
					}
					if (loosestNameIdFormat == null || loosestNameIdFormat.ordinal() > format.ordinal()) {
						loosestNameIdFormat = format;
					}
				}
			}
			entity.setNameIdFormat(loosestNameIdFormat == null ? NameIdFormatEnum.UNSPECIFIED : loosestNameIdFormat);

			// **** IdP Settings ****
			SamlIdpSettings idpSettings = entity.getIdpSettings();

			// Find attributes (if any)
			List<ClaimAttribute> samlAttributeList = idpSettings.getAttributes();
			List<AttributeConsumingService> attributeConsumingServices = ssoDescriptor.getAttributeConsumingServices();
			for (AttributeConsumingService attributeConsumingService : attributeConsumingServices) {
				List<RequestedAttribute> requestedAttributes = attributeConsumingService.getRequestAttributes();
				for (RequestedAttribute requestedAttribute : requestedAttributes) {
					if (requestedAttribute.isRequired()) {
						String name = requestedAttribute.getName();
						if (getClaimAttributeByName(samlAttributeList, name) == null) { // TODO
							AttributeTypeEnum attributeTypeEnum = AttributeTypeEnum.USER_INPUT;
							try {
								AttributeMap attrMap = requestedAttribute.getUnknownAttributes();
								if (attrMap != null && attrMap.get(userPropertyQName) != null) {
									String prop = attrMap.get(userPropertyQName);
									attributeTypeEnum = AttributeTypeEnum.valueOf(prop);
								}
								samlAttributeList.add(new ClaimAttribute(name, attributeTypeEnum, null));
							} catch (Exception e) {
								logger.warn("SAML - error while reading custom attribute properties: " + e.toString());
							}
						}
					}
				}
			}
			idpSettings.setAttributes(samlAttributeList);

			// Find extensions (if any)
			Extensions extensions = entityDescriptor.getExtensions();
			if (extensions != null) {
				for (XMLObject extension : extensions.getOrderedChildren()) {
					if (extension instanceof DigestMethodImpl) {
						idpSettings.getSignatureSettings().setDigestAlg(DigestAlgorithmEnum.getFromString(((DigestMethodImpl) extension).getAlgorithm()));
					} else if (extension instanceof SigningMethodImpl) {
						idpSettings.getSignatureSettings().setSignAlg(SignatureAlgorithmEnum.getFromString(((SigningMethodImpl) extension).getAlgorithm()));
					}
				}
			}
			entity.setIdpSettings(idpSettings);
			// **** IdP Settings ****

			// Find display name if one is not entered
			String displayName = entity.getDisplayName();
			if (displayName == null || displayName.isEmpty()) {
				Organization org = entityDescriptor.getOrganization();
				if (org != null) {
					if (org.getDisplayNames().size() > 0) {
						entity.setDisplayName(org.getDisplayNames().get(0).getValue());
					} else if (org.getOrganizationNames().size() > 0) {
						entity.setDisplayName(org.getOrganizationNames().get(0).getValue());
					}
				}
			}
			entity.setMetadata(xml);
		} else {
			throw new DcemException(DcemErrorCodes.INVALID_SAML_METADATA, JsfUtils.getStringSafely(samlModule.getResourceName(), "error.noMetadataFound"));
		}
	}

	private ClaimAttribute getClaimAttributeByName(List<ClaimAttribute> samlAttributeList, String name) {
		for (ClaimAttribute attribute : samlAttributeList) {
			if (attribute.getName().equals(name)) {
				return attribute;
			}
		}
		return null;
	}

	@DcemTransactional
	public void addUpdateSpMetadata(DcemAction dcemAction, SamlSpMetadataEntity entity, boolean withAuditing) throws DcemException {
		try {
			String entityID = entity.getEntityId();
			if (entityID == null || entityID.isEmpty()) {
				throw new DcemException(DcemErrorCodes.INVALID_SAML_METADATA, JsfUtils.getStringSafely(samlModule.getResourceName(), "error.noEntityIdFound"));
			}

			String acsLocation = entity.getAcsLocation();
			if (acsLocation == null || acsLocation.isEmpty() || !isValidURL(acsLocation)) {
				throw new DcemException(DcemErrorCodes.INVALID_SAML_METADATA, JsfUtils.getStringSafely(samlModule.getResourceName(), "error.noAcsFound"));
			}

			String displayName = entity.getDisplayName();
			if (displayName == null || displayName.isEmpty()) {
				throw new DcemException(DcemErrorCodes.INVALID_SAML_METADATA,
						JsfUtils.getStringSafely(samlModule.getResourceName(), "error.noDisplayNameFound"));
			}
			// List<ClaimAttribute> attributes = entity.getAttributes();
			if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
				em.persist(entity);
			} else {
				em.merge(entity);
			}

			if (withAuditing) {
				auditingLogic.addAudit(dcemAction, entity.toString());
			}

			IExecutorService executorService = DcemCluster.getDcemCluster().getExecutorService();
			executorService.executeOnAllMembers(new UpdateSpMetadataCacheTask(entity.getEntityId(), TenantIdResolver.getCurrentTenantName()));

		} catch (DcemException e) {
			logger.debug("SAML - validation error while adding new SP Metadata: " + e.toString() + "\nEntity: " + entity.toString());
			throw e;
		} catch (Exception e) {
			logger.debug("SAML - unknown error while adding new SP Metadata: " + e.toString() + "\nEntity: " + entity.toString());
			throw new DcemException(DcemErrorCodes.INVALID_SAML_METADATA, e.toString(), e);
		}
	}

	@DcemTransactional
	public void report(String spEntityId, String description, ReportAction action, SamlErrorCodes errorCode) {
		SamlSpMetadataEntity metadata = getSpMetadataEntity(spEntityId);
		PolicyAppEntity policyApp = policyLogic.getDetachedPolicyApp(AuthApplication.SAML, metadata != null ? metadata.getId() : 0);
		String errorCodeString = errorCode != null ? errorCode.toString() : null;
		DcemReporting report = new DcemReporting(getAppName(policyApp), action, null, errorCodeString, null, description, AlertSeverity.OK);
		reportingLogic.addReporting(report);
	}

	private String getAppName(PolicyAppEntity appEntity) {
		return appEntity.getSubName() != null ? appEntity.getSubName() : appEntity.getAuthApplication().name();
	}

	public SamlSpMetadataEntity getSpMetadataEntity(String entityId) {
		SamlTenantData samlTenantData = (SamlTenantData) samlModule.getModuleTenantData();
		SamlSpMetadataEntity entity = samlTenantData.getMetadataMap().get(entityId);
		if (entity == null) {
			try {
				TypedQuery<SamlSpMetadataEntity> query = em.createNamedQuery(SamlSpMetadataEntity.GET_SP_METADATA_BY_ENTITY_ID, SamlSpMetadataEntity.class);
				query.setParameter(1, entityId);
				entity = query.getSingleResult();
				samlTenantData.getMetadataMap().put(entityId, entity);
			} catch (Exception e) {
				logger.debug("SAML - could not find SP Metadata with EntityID: " + entityId);
				entity = null;
			}
		}
		return entity;
	}

	@DcemTransactional
	public void deleteSpMetadataEntity(String entityId) throws Exception {
		SamlSpMetadataEntity samlSpMetadataEntity = getSpMetadataEntity(entityId);
		if (samlSpMetadataEntity != null) {
			if (em.contains(samlSpMetadataEntity) == false) {
				em.remove(em.merge(samlSpMetadataEntity));
			} else {
				em.remove(samlSpMetadataEntity);
			}
		}
	}

	/**
	 * @return
	 */
	public List<SamlSpMetadataEntity> getAllSpMetadataEntities() {
		SamlTenantData samlTenantData = (SamlTenantData) samlModule.getModuleTenantData();
		try {
			TypedQuery<SamlSpMetadataEntity> query = em.createNamedQuery(SamlSpMetadataEntity.GET_ALL_SP_METADATA, SamlSpMetadataEntity.class);
			List<SamlSpMetadataEntity> entities = query.getResultList();
			for (SamlSpMetadataEntity entity : entities) {
				String entityId = entity.getEntityId();
				if (!samlTenantData.getMetadataMap().containsKey(entityId)) {
					samlTenantData.getMetadataMap().put(entityId, entity);
				}
			}
			return entities;
		} catch (Exception e) {
			logger.error("SAML - could not find SP Metadata Entities", e);
			return null;
		}
	}

	public void invalidateMetadata(String entityId) {
		SamlTenantData samlTenantData = (SamlTenantData) samlModule.getModuleTenantData();
		samlTenantData.getMetadataMap().remove(entityId);
	}

	public boolean validateCertificateWithCA(X509Certificate certificate) {
		// TODO - need to test
		boolean isValid = false;
		try {
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init((KeyStore) null);
			X509Certificate[] certChain = new X509Certificate[] { certificate };
			for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
				if (trustManager instanceof X509TrustManager) {
					X509TrustManager x509TrustManager = (X509TrustManager) trustManager;
					try {
						x509TrustManager.checkServerTrusted(certChain, "RSA");
						isValid = true;
					} catch (CertificateException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (NoSuchAlgorithmException | KeyStoreException e) {
			e.printStackTrace();
		}

		return isValid;
	}

}
