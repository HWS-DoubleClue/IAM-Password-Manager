package com.doubleclue.dcem.test.gui;

import java.io.Serializable;
import java.net.URLEncoder;
import java.security.cert.CertificateEncodingException;
import java.util.UUID;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.joda.time.DateTime;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.NameIDPolicy;
import org.opensaml.saml.saml2.metadata.AssertionConsumerService;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.Organization;
import org.opensaml.saml.saml2.metadata.OrganizationDisplayName;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.UsageType;
import org.w3c.dom.Element;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.saml.logic.SamlModule;
import com.doubleclue.dcem.saml.logic.SamlSignatureSettings;
import com.doubleclue.dcem.saml.logic.SamlUtils;
import com.doubleclue.dcem.saml.logic.enums.NameIdFormatEnum;
import com.doubleclue.dcem.saml.preferences.SamlPreferences;

import net.shibboleth.utilities.java.support.xml.SerializeSupport;

@Named("samlTestServiceView")
@SessionScoped
public class SamlTestServiceView implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final String SP_RELAY_TOKEN = "test";
	public static final String SP_ENTITY_ID = "TEST_SP";
	private static final String SP_NAME_ID_FORMAT = NameIdFormatEnum.UNSPECIFIED.getFormat();
	private static String requestId;
	private AuthnRequest authnRequest;
	
	@Inject
	SamlModule samlModule;

	public void actionGoToIdp () throws Exception {
		SamlPreferences preferences = samlModule.getModulePreferences();
		
		// Create the authentication request and encode it
		String authnRequestUrl;
		try {
			AuthnRequest authnRequest = createAuthnRequest();
			String request = SamlUtils.getDeflatedBase64String(authnRequest);
			request = URLEncoder.encode(request, DcemConstants.CHARSET_UTF8);
			authnRequestUrl = preferences.getSsoDomain() + "/dcem/saml?SAMLRequest=" + request + "&RelayState=" + SP_RELAY_TOKEN;
			System.out.println(preferences.getSsoDomain());
		} catch (Exception e) {
			throw new Exception ("Creating of authentication Request failed, " + e.getLocalizedMessage());
		}
		
		// Send the authentication request
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
	    externalContext.redirect(authnRequestUrl);
	}
	
	public AuthnRequest createAuthnRequest() throws Exception {
		NameIDPolicy policy = SamlUtils.createXmlObject(NameIDPolicy.class);
		policy.setAllowCreate(true);
		policy.setFormat(NameIdFormatEnum.EMAIL.getFormat());

		requestId = "_" + UUID.randomUUID();

		authnRequest = SamlUtils.createXmlObject(AuthnRequest.class);
		authnRequest.setID(requestId);
		authnRequest.setAssertionConsumerServiceURL(getAcsLocation());
		authnRequest.setProtocolBinding(SAMLConstants.SAML2_POST_BINDING_URI);
		authnRequest.setIssueInstant(new DateTime());
		authnRequest.setDestination(samlModule.getModulePreferences().getSsoDomain() + "/dcem/saml");
		authnRequest.setVersion(SAMLVersion.VERSION_20);
		authnRequest.setIssuer(SamlUtils.createIssuer(SP_ENTITY_ID));
		authnRequest.setNameIDPolicy(policy);

		SamlUtils.signSamlObject(authnRequest, samlModule.getIdpCredential(), new SamlSignatureSettings()); // use IdP certificate
		return authnRequest;
	}
	
	public String getSpMetadataXml() throws CertificateEncodingException, MarshallingException {

		KeyDescriptor keyDescriptor = SamlUtils.createXmlObject(KeyDescriptor.class);
		keyDescriptor.setUse(UsageType.SIGNING);
		keyDescriptor.setKeyInfo(SamlUtils.createKeyInfo(samlModule.getIdpCredential())); // use same certificate as IdP

		NameIDFormat nameIdFormat = SamlUtils.createXmlObject(NameIDFormat.class);
		nameIdFormat.setFormat(SP_NAME_ID_FORMAT);

		AssertionConsumerService acs = SamlUtils.createXmlObject(AssertionConsumerService.class);
		acs.setBinding(SAMLConstants.SAML2_POST_BINDING_URI);
		acs.setIndex(1);
		acs.setIsDefault(true);
		acs.setLocation(getAcsLocation());

		SPSSODescriptor spSsoDescriptor = SamlUtils.createXmlObject(SPSSODescriptor.class);
		spSsoDescriptor.setAuthnRequestsSigned(true);
		spSsoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);
		spSsoDescriptor.getKeyDescriptors().add(keyDescriptor);
		spSsoDescriptor.getNameIDFormats().add(nameIdFormat);
		spSsoDescriptor.getAssertionConsumerServices().add(acs);

		OrganizationDisplayName orgDisplayName = SamlUtils.createXmlObject(OrganizationDisplayName.class);
		orgDisplayName.setValue(SP_ENTITY_ID);
		Organization organization = SamlUtils.createXmlObject(Organization.class);
		organization.getDisplayNames().add(orgDisplayName);

		EntityDescriptor entityDescriptor = SamlUtils.createXmlObject(EntityDescriptor.class);
		entityDescriptor.setID("_" + UUID.randomUUID());
		entityDescriptor.setEntityID(SP_ENTITY_ID);
		entityDescriptor.getRoleDescriptors().add(spSsoDescriptor);
		entityDescriptor.setOrganization(organization);

		Element element = SamlUtils.marshallXmlObject(entityDescriptor);
		return SerializeSupport.nodeToString(element);
	}

	public String getAcsLocation() {
		return samlModule.getModulePreferences().getSsoDomain() + DcemConstants.DEFAULT_WEB_NAME + DcemConstants.TEST_SP_SERVLET_PATH;
	}

	public AuthnRequest getAuthnRequest() {
		return authnRequest;
	}

	public void setAuthnRequest(AuthnRequest authnRequest) {
		this.authnRequest = authnRequest;
	}

	public String getRequestId() {
		return requestId;
	}
}
