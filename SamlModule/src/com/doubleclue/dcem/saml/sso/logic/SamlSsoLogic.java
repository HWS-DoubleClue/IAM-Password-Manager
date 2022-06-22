package com.doubleclue.dcem.saml.sso.logic;

import java.io.IOException;
import java.io.Serializable;
import java.security.cert.CertificateEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.xmlsec.signature.support.SignatureException;

import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.policy.AuthenticationLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthenticateResponse;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.ErrorDisplayBean;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.ClaimAttribute;
import com.doubleclue.dcem.core.logic.DbResourceBundle;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.saml.entities.SamlSpMetadataEntity;
import com.doubleclue.dcem.saml.logic.SamlConstants;
import com.doubleclue.dcem.saml.logic.SamlIdpSettings;
import com.doubleclue.dcem.saml.logic.SamlLogic;
import com.doubleclue.dcem.saml.logic.SamlModule;
import com.doubleclue.dcem.saml.logic.SamlUtils;
import com.doubleclue.dcem.saml.logic.enums.NameIdFormatEnum;
import com.doubleclue.dcem.saml.sso.gui.SamlReturnView;

@SuppressWarnings("serial")
@SessionScoped
@Named("samlSsoLogic")
public class SamlSsoLogic implements Serializable {

	private static final Logger logger = LogManager.getLogger(SamlSsoLogic.class);

	@Inject
	SamlLogic samlLogic;

	@Inject
	SamlModule samlModule;

	@Inject
	UserLogic userLogic;

	@Inject
	CloudSafeLogic cloudDataLogic;

	@Inject
	SamlReturnView returnView;

	@Inject
	ErrorDisplayBean errorDisplayBean;

	@Inject
	DomainLogic domainLogic;

	@Inject
	AuthenticationLogic authenticationLogic;

	private AuthnRequest authnRequest;
	private LogoutRequest logoutRequest;
	private String relayToken;
	private SamlSpMetadataEntity metadata;
	private DbResourceBundle dbResourceBundle;

	// IdP-Initiated SSO
	private String selectedSp;
	private Map<String, SamlSpMetadataEntity> metadataMap;

	@PostConstruct
	public void init() {
		JsfUtils.setSessionTimeout(samlModule.getModulePreferences().getSessionIdleTimeout() * 60);
	}

	public String getSessionError() {
		if (errorDisplayBean.isErrorOn() == true && errorDisplayBean.getMessage().equals(DcemConstants.EXPIRED_PAGE)) {
			JsfUtils.addMessage(FacesMessage.SEVERITY_INFO, getDbResourceBundle(), "sso.error.expired", null, null);
			errorDisplayBean.setErrorOn(false);
		}
		return null;
	}

	public DbResourceBundle getDbResourceBundle() {
		if (dbResourceBundle == null) {
			try {
				dbResourceBundle = DbResourceBundle.getDbResourceBundle(JsfUtils.getLocale());
			} catch (Exception e) {
				logger.error("SAML - could not get db resource bundle: " + e.getMessage());
			}
		}
		return dbResourceBundle;
	}

	public AuthnRequest getAuthnRequest() {
		return authnRequest;
	}

	public void setAuthnRequest(AuthnRequest authnRequest) {
		this.authnRequest = authnRequest;
	}

	public LogoutRequest getLogoutRequest() {
		return logoutRequest;
	}

	public void setLogoutRequest(LogoutRequest logoutRequest) {
		this.logoutRequest = logoutRequest;
	}

	public String getRelayToken() {
		return relayToken;
	}

	public void setRelayToken(String relayToken) {
		this.relayToken = relayToken;
	}

	public void setupResponse(AuthenticateResponse authResponse) throws CertificateEncodingException, MarshallingException, SignatureException, DcemException {
//		if (metadata == null) {
//		}
		String entityId = metadata.getEntityId();
		String acsLocation = metadata.getAcsLocation();
		Response response = createResponse(authResponse);
		String base64Response = SamlUtils.getBase64String(response);
		samlModule.addLogin(entityId);
		setupReturnPage(acsLocation, entityId, base64Response, getRelayToken(), metadata.getDisplayName());
		// logging
		if (metadata.getIdpSettings().isTraceRequests()) {
			logger.info("SAML - created response:\n" + SamlUtils.getXmlString(response));
		} else if (logger.isTraceEnabled()) {
			logger.trace("SAML - created response:\n" + base64Response);
		}
	}

	private Response createResponse(AuthenticateResponse authResponse) throws CertificateEncodingException, MarshallingException, SignatureException {
		long messageId = authResponse != null ? authResponse.getSecureMsgId() : (long) (Math.random() * 100000);
		SamlIdpSettings idpSettings = metadata.getIdpSettings();
		String idpEntityId = samlModule.getModulePreferences().getIdpEntityId();
		DcemUser user = authResponse.getDcemUser();
		String userPrincipalName = user.getUserPrincipalName();
		int ind = -1;
		if (userPrincipalName != null) {
			ind = userPrincipalName.indexOf('@');
		}
		if (idpSettings.isAddUserDomain() && ind != -1) {
			idpEntityId += "/" + userPrincipalName.substring(ind + 1);
		}

		NameIdFormatEnum nameIdFormat = metadata.getNameIdFormat();
		String spEntityId = metadata.getEntityId();
		String acsLocation = metadata.getAcsLocation();

		DateTime authnInstant = new DateTime();
		DateTime expiryDate = authnInstant.plusHours(24);
		String requestId = authnRequest != null ? authnRequest.getID() : null;
		String subjectName;
		if (metadata.isAzure()) {
			subjectName = user.getImmutableId();
		} else {
			switch (nameIdFormat) {
			case EMAIL:
				subjectName = user.getEmail();
				break;
			case PERSISTENT:
				subjectName = user.getAccountName();
				break;
			default:
				subjectName = user.getLoginId();
				break;
			}
		}
		// nanda2.test2.doubleclue.com subjectName = "qA2CRxZloEqe0Uld+RaQaQ==";
		// levent.test3.doubleclue.com subjectName = "P9QljXTS6kmxN9E8ZorMPQ==";
		Subject subject = SamlUtils.createSubject(subjectName, nameIdFormat, expiryDate, acsLocation, requestId);
		AuthnStatement authnStatement = SamlUtils.createAuthnStatement(new DateTime(), "" + messageId,
				SamlConstants.AUTHN_CONTEXT_CLASS_PASSWORD_PROTECTED_TRANSPORT);
		Conditions conditions = SamlUtils.createConditions(new String[] { spEntityId }, authnInstant, expiryDate, false, -1);
		AttributeStatement attributeStatement = createAttributeStatement(authResponse);
		Assertion assertion = SamlUtils.createAssertion(idpEntityId, subject, conditions, authnStatement, attributeStatement, samlModule.getIdpCredential(),
				metadata.getIdpSettings().getSignatureSettings());

		// Create Response.
		return SamlUtils.createResponse(requestId, idpEntityId, acsLocation, SamlConstants.STATUS_SUCCESS, assertion);
	}

	public String createLogoutResponse() throws CertificateEncodingException, MarshallingException, IOException {

		if (logoutRequest == null) {
			return null;
		}

		String requestId = logoutRequest.getID();
		String idpEntityId = samlModule.getModulePreferences().getIdpEntityId();
		String acsLocation = metadata.getAcsLocation();

		LogoutResponse response = SamlUtils.createLogoutResponse(requestId, idpEntityId, acsLocation, SamlConstants.STATUS_SUCCESS,
				samlModule.getIdpCredential(), metadata.getIdpSettings().getSignatureSettings());
		return metadata.isLogoutIsPost() ? SamlUtils.getBase64String(response) : SamlUtils.getDeflatedBase64String(response);
	}

	private AttributeStatement createAttributeStatement(AuthenticateResponse authResponse) {
		List<ClaimAttribute> requiredAttributes = metadata.getIdpSettings().getAttributes();
		if (requiredAttributes != null && requiredAttributes.size() > 0) {
			List<ClaimAttribute> claimValues = authenticationLogic.getClaimAttributeValues(requiredAttributes, authResponse.getDcemUser(),
					authResponse.getPolicyName(), null);
			Map<String, String[]> attributeValues = new HashMap<String, String[]>();
			for (ClaimAttribute claimAttribute : claimValues) {
				attributeValues.put(claimAttribute.getName(), new String[] { claimAttribute.getValue() });
			}
			AttributeStatement attributeStatement = SamlUtils.createAttributeStatement(attributeValues);
			return attributeStatement;
		}
		return null;
	}

	private void setupReturnPage(String acsLocation, String entityId, String base64Response, String relayToken, String displayName) {
		returnView.setAcsLocation(acsLocation);
		returnView.setBase64Response(base64Response);
		returnView.setEntityId(entityId);
		returnView.setRelayToken(relayToken);
		returnView.setDisplayName(displayName);
	}

	public void redirectToPage(String page) {
		try {
			FacesContext fc = FacesContext.getCurrentInstance();
			ExternalContext ec = fc.getExternalContext();
			if (page.equals(SamlConstants.JSF_PAGE_RETURN_TO_SP)) { // kill session
				HttpSession session = (HttpSession) ec.getSession(false);
				if (session != null) {
					session.setMaxInactiveInterval(4 * 60); // 4 minutes
				}
			}
			ec.redirect(page);
		} catch (IOException e) {
			logger.info("SAML - could not redirect to " + page, e);
		}
	}

	public SamlSpMetadataEntity getMetadata() {
		if (metadata == null && selectedSp != null && !selectedSp.isEmpty()) {
			onSpChanged();
		}

		return metadata;
	}

	public void setMetadata(SamlSpMetadataEntity metadata) {
		this.metadata = metadata;
		setSelectedSp(metadata != null ? metadata.getDisplayName() : null);
	}

	public String getDisplayName() {
		return (metadata == null) ? "?" : metadata.getDisplayName();
	}

	public void displayErrorCode(DcemException dcemException, String username) {
		DcemErrorCodes errorCode = dcemException.getErrorCode();
		if (errorCode == DcemErrorCodes.UNEXPECTED_ERROR) {
			Throwable cause = dcemException.getCause();
			if (cause != null && cause.getClass() == DcemException.class) {
				displayErrorCode((DcemException) cause, username);
			} else {
				logger.info("SAML - unknown error for user: " + username, dcemException);
				JsfUtils.addMessage(FacesMessage.SEVERITY_ERROR, getDbResourceBundle(), "sso.error.unknown", null, null);
			}
		} else {
			logger.info("SAML SSO Error: " + errorCode, dcemException);
			JsfUtils.addMessage(FacesMessage.SEVERITY_ERROR, getDbResourceBundle(), "sso.error.dcem." + errorCode, null, null);
		}
	}

	// Idp-Initiated SSO
	public Set<String> getSpList() {
		List<SamlSpMetadataEntity> spList = samlLogic.getAllSpMetadataEntities();
		metadataMap = new HashMap<String, SamlSpMetadataEntity>();
		for (SamlSpMetadataEntity entity : spList) {
			metadataMap.put(entity.getDisplayName(), entity);
		}
		return metadataMap.keySet();
	}

	public String getSelectedSp() {
		return selectedSp;
	}

	public void setSelectedSp(String selectedSp) {
		this.selectedSp = selectedSp;
	}

	public void onSpChanged() {
		if (authnRequest == null) {
			if (selectedSp != null && !selectedSp.isEmpty() && metadataMap.containsKey(selectedSp)) {
				setMetadata(metadataMap.get(selectedSp));
			} else {
				setMetadata(null);
			}
		}
	}
}
