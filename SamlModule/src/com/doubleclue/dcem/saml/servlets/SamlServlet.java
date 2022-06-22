package com.doubleclue.dcem.saml.servlets;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.opensaml.saml.saml2.core.AuthnRequest;
import org.opensaml.saml.saml2.core.LogoutRequest;
import org.opensaml.saml.saml2.core.RequestAbstractType;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;

import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.saml.entities.SamlSpMetadataEntity;
import com.doubleclue.dcem.saml.logic.SamlConstants;
import com.doubleclue.dcem.saml.logic.SamlLogic;
import com.doubleclue.dcem.saml.logic.SamlModule;
import com.doubleclue.dcem.saml.logic.SamlUtils;
import com.doubleclue.dcem.saml.logic.enums.SamlErrorCodes;
import com.doubleclue.dcem.saml.sso.gui.SamlErrorView;
import com.doubleclue.dcem.saml.sso.logic.SamlSsoLogic;

@SuppressWarnings("serial")
@SessionScoped
public class SamlServlet extends HttpServlet {

	private static final Logger logger = LogManager.getLogger(SamlServlet.class);

	@Inject
	SamlErrorView errorView;

	@Inject
	SamlLogic samlLogic;

	@Inject
	SamlModule samlModule;

	@Inject
	SamlSsoLogic ssoLogic;

	@Inject
	DcemApplicationBean applicationBean;

	private int samlPort;

	@Override
	public void init() throws ServletException {
		try {
			samlPort = DcemCluster.getInstance().getClusterConfig().getConnectionService(ConnectionServicesType.SAML).getPort();
		} catch (Exception e) {
			e.printStackTrace();
			throw new ServletException(e.getLocalizedMessage());
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("SAML - Received Request via GET: " + request.getQueryString());
		}
		if (request.getRequestURI().endsWith(SamlConstants.FILENAME_IDP_METADATA)) {
			sendIdPMetadata(response);
		} else {
			respondHttpRequest(true, request, response);
		}
	}

	private void sendIdPMetadata(HttpServletResponse resp) throws IOException {
		try {
			resp.setContentType("application/xml;charset=UTF-8");
			resp.getWriter().write(samlLogic.getIdpMetadataString());
		} catch (DcemException e) {
			resp.getWriter().write(e.getMessage());
		}
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("SAML - Received Request via POST: " + request.getParameterMap().toString());
		}
		respondHttpRequest(false, request, response);
	}

	private void respondHttpRequest(boolean isGet, HttpServletRequest request, HttpServletResponse response) throws IOException {
		if (request.getLocalPort() != samlPort) {
			redirectToErrorPage(response, HttpServletResponse.SC_UNAUTHORIZED, "Incorrect URL", null, SamlErrorCodes.INCORRECT_URL, null);
			logger.info("Received SAML-Request on wrong port. Port=" + request.getLocalPort());
			return;
		}
		TenantEntity tenantEntity = applicationBean.getTenantFromRequest(request);
		TenantIdResolver.setCurrentTenant(tenantEntity);

		try {
			request.getSession().setMaxInactiveInterval(samlModule.getModulePreferences().getSessionIdleTimeout() * 60);
			request.setCharacterEncoding(DcemConstants.CHARSET_UTF8);
			if (request.getParameter(SamlConstants.PARAM_SAML_REQUEST) != null || request.getParameter(SamlConstants.PARAM_SAML_ARTIFACT) != null) {
				respondSamlRequest(isGet, request, response);
			} else {
				redirectToLoginPage(request, response, null, null, null); // IdP-Initiated SSO
			}
		} catch (UnsupportedEncodingException e) {
			redirectToErrorPage(response, HttpStatus.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage(), e, SamlErrorCodes.UNSUPPORTED_ENCODING, null);
		}
	}

	private void redirectToLoginPage(HttpServletRequest request, HttpServletResponse response, String relayToken, AuthnRequest authnRequest,
			SamlSpMetadataEntity metadata) throws IOException {
		logger.trace("SAML - Proceeding to login screen.");

		request.getSession(true); // create session
		ssoLogic.setRelayToken(relayToken);
		ssoLogic.setAuthnRequest(authnRequest);
		ssoLogic.setMetadata(metadata);
		String path = SamlConstants.PATH_JSF_PAGES + "/" + DcemConstants.HTML_PAGE_PRE_LOGIN;
		
		String queryString = SamlConstants.QUERY_STRING_IDP_INITIATED + "=" + (metadata == null ? "true" : "false");
		response.sendRedirect(path + "?" + queryString);
	}

	private void redirectToLogoutPage(HttpServletResponse response, String relayToken, LogoutRequest logoutRequest, SamlSpMetadataEntity metadata)
			throws IOException {
		logger.trace("SAML - Proceeding to logout screen.");
		ssoLogic.setRelayToken(relayToken);
		ssoLogic.setLogoutRequest(logoutRequest);
		ssoLogic.setMetadata(metadata);
		response.sendRedirect(SamlConstants.PATH_JSF_PAGES + "/" + DcemConstants.LOGOFF_PAGE);
	}

	private void redirectToErrorPage(HttpServletResponse response, int httpStatusCode, String errorMessage, Exception e, SamlErrorCodes errorCode,
			String entityId) throws IOException {
		String fullErrorMessage = "SAML - Request validation failed: " + errorMessage;
		if (e != null) {
			logger.info(fullErrorMessage, e);
		} else {
			logger.info(fullErrorMessage);
		}
		samlLogic.report(entityId, errorMessage, ReportAction.SamlValidation, errorCode);
		errorView.setHttpStatusCode(httpStatusCode);
		errorView.setErrorCode(errorCode.getErrorCode());
		response.sendRedirect(SamlConstants.PATH_JSF_PAGES + "/" + SamlConstants.JSF_PAGE_ERROR);
	}

	private void respondSamlRequest(boolean isGet, HttpServletRequest request, HttpServletResponse response) throws IOException {

		String relayToken = request.getParameter(SamlConstants.PARAM_RELAY_STATE);
		String base64Request = request.getParameter(SamlConstants.PARAM_SAML_REQUEST);

		try {
			RequestAbstractType samlRequest = null;
			if (base64Request != null) {
				samlRequest = isGet ? (RequestAbstractType) SamlUtils.getXmlObjectFromDeflatedBase64String(base64Request)
						: (RequestAbstractType) SamlUtils.getXmlObjectFromBase64String(base64Request);
			} else if (request.getParameter(SamlConstants.PARAM_SAML_ARTIFACT) != null) {
				samlRequest = getAuthnRequestFromArtifactId(request.getParameter(SamlConstants.PARAM_SAML_ARTIFACT));
			}

			if (samlRequest != null) {

				String entityId = samlRequest.getIssuer().getValue();
				SamlSpMetadataEntity metadata = samlLogic.getSpMetadataEntity(entityId);

				if (metadata != null) {
					if (metadata.getIdpSettings().isTraceRequests()) {
						logger.info("SAML - received request:\n" + SamlUtils.getXmlString(samlRequest));
					}
					String signatureValidationError = metadata.isRequestsSigned() ? validateSignature(samlRequest, metadata, request) : null;
					if (signatureValidationError == null) {
						if (samlRequest instanceof AuthnRequest) {
							redirectToLoginPage(request, response, relayToken, (AuthnRequest) samlRequest, metadata);
						} else if (samlRequest instanceof LogoutRequest) {
							redirectToLogoutPage(response, relayToken, (LogoutRequest) samlRequest, metadata);
						} else {
							redirectToErrorPage(response, HttpStatus.SC_UNAUTHORIZED, signatureValidationError, null, SamlErrorCodes.UNKNOWN_REQUEST_TYPE,
									entityId);
						}
					} else {
						redirectToErrorPage(response, HttpStatus.SC_UNAUTHORIZED, signatureValidationError, null, SamlErrorCodes.INVALID_REQUEST, entityId);
					}
				} else {
					redirectToErrorPage(response, HttpStatus.SC_UNAUTHORIZED, "This IdP does not know about the SP called " + entityId, null,
							SamlErrorCodes.UNKNOWN_SP, entityId);
				}
			} else {

				redirectToErrorPage(response, HttpStatus.SC_BAD_REQUEST, "AuthnRequest could not be found.", null, SamlErrorCodes.REQUEST_MISSING, null);
			}
		} catch (Exception e) {
			redirectToErrorPage(response, HttpStatus.SC_BAD_REQUEST, e.toString(), e, SamlErrorCodes.REQUEST_PARSE_ERROR, null);
		}
	}

	private AuthnRequest getAuthnRequestFromArtifactId(String artifactId) {
		// TODO - Support for Artifact Binding
		return null;
	}

	private String getSignedQueryString(String samlRequest, String relayToken, String signatureAlgorithm) throws UnsupportedEncodingException {
		String query = SamlConstants.PARAM_SAML_REQUEST + "=" + URLEncoder.encode(samlRequest, DcemConstants.CHARSET_UTF8);
		if (relayToken != null) {
			query += "&" + SamlConstants.PARAM_RELAY_STATE + "=" + URLEncoder.encode(relayToken, DcemConstants.CHARSET_UTF8);
		}
		query += "&" + SamlConstants.PARAM_SIG_ALG + "=" + URLEncoder.encode(signatureAlgorithm, DcemConstants.CHARSET_UTF8);
		return query;
	}

	private String getJavaSecuritySignatureAlgorithm(String algoUrl) {
		String code = algoUrl.substring(algoUrl.indexOf('#') + 1);
		code = code.toUpperCase();
		String[] parts = code.split("-");
		if (parts.length == 2) {
			return parts[1] + "with" + parts[0];
		} else if (parts.length == 1) {
			return parts[0];
		} else {
			return null;
		}
	}

	private String urlEncodeToLowercase(String input) throws UnsupportedEncodingException {
		char[] temp = input.toCharArray();
		for (int i = 0; i < temp.length - 2; i++) {
			if (temp[i] == '%') {
				temp[i + 1] = Character.toLowerCase(temp[i + 1]);
				temp[i + 2] = Character.toLowerCase(temp[i + 2]);
			}
		}
		return new String(temp);
	}

	private String validateSignature(RequestAbstractType samlRequest, SamlSpMetadataEntity metadata, HttpServletRequest request) throws Exception {
		String errorMessage = null;
		Certificate metadataCert = metadata.getCertificate();
		Signature signatureElement = samlRequest.getSignature();
		if (signatureElement != null) {
			Certificate requestCert = null;
			KeyInfo keyInfo = signatureElement.getKeyInfo();
			if (keyInfo != null) {
				requestCert = SamlUtils.getX509CertificateFromKeyInfo(keyInfo);
			}
			Certificate signingCert = null;
			if (metadataCert != null && requestCert != null) {
				if (metadataCert.equals(requestCert)) {
					signingCert = metadataCert;
				} else {
					errorMessage = "The Certificate in the request does not match the one in the Metadata.";
				}
			} else if (metadataCert != null) {
				signingCert = metadataCert;
			} else if (requestCert != null) {
				if (samlLogic.validateCertificateWithCA((X509Certificate) signingCert)) {
					signingCert = requestCert;
				} else {
					logger.debug("SAML - Untrusted certificate: " + SamlUtils.getX509CertificateStringFromKeyInfo(keyInfo));
					errorMessage = "The embedded X.509 Certificate is untrusted.";
				}
			}

			if (signingCert != null) {
				try {
					BasicX509Credential credential = new BasicX509Credential((X509Certificate) signingCert);
					SignatureValidator.validate(signatureElement, credential);
				} catch (SignatureException e) {
					errorMessage = "The embedded signature is invalid.";
				}
			} else {
				errorMessage = "The signature cannot be validated because no Certificate is found in either the request or the Metadata.";
			}

		} else {

			String relayToken = request.getParameter(SamlConstants.PARAM_RELAY_STATE);
			String base64Request = request.getParameter(SamlConstants.PARAM_SAML_REQUEST);
			String signatureValue = request.getParameter(SamlConstants.PARAM_SIGNATURE);
			String signatureAlgorithm = request.getParameter(SamlConstants.PARAM_SIG_ALG);

			if (signatureValue == null || signatureValue.isEmpty() || signatureAlgorithm == null || signatureValue.isEmpty()) {
				errorMessage = "This request does not contain a Signature and/or Signature Algorithm.";
			} else {

				if (metadataCert == null) {
					errorMessage = "The signature cannot be validated because no Certificate was registered in the Metadata.";
				} else {

					String algorithm = getJavaSecuritySignatureAlgorithm(signatureAlgorithm);
					byte[] signatureBytes = SamlUtils.decodeBase64String(signatureValue);
					org.apache.xml.security.Init.init();
					java.security.Signature sig = java.security.Signature.getInstance(algorithm);
					sig.initVerify(metadataCert.getPublicKey());
					String query = getSignedQueryString(base64Request, relayToken, signatureAlgorithm);
					sig.update(query.getBytes(DcemConstants.CHARSET_UTF8));
					boolean signatureValid = sig.verify(signatureBytes);
					if (signatureValid == false) {
						// Now lets try with lowerCase
						query = urlEncodeToLowercase(query);
						sig.initVerify(metadataCert.getPublicKey());
						sig.update(query.getBytes(DcemConstants.CHARSET_UTF8));
						signatureValid = sig.verify(signatureBytes);
						if (signatureValid == false) {
							logger.warn("SAML - Invalid signature: " + signatureValue + ", query: " + query);
							errorMessage = "The query string signature is invalid.";
						}
					}
				}
			}
		}

		return errorMessage;
	}
}
