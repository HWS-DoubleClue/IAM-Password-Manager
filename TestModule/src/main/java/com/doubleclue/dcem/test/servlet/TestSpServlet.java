package com.doubleclue.dcem.test.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.SignatureValidator;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.saml.logic.SamlConstants;
import com.doubleclue.dcem.saml.logic.SamlModule;
import com.doubleclue.dcem.saml.logic.SamlUtils;
import com.doubleclue.dcem.saml.logic.enums.NameIdFormatEnum;
import com.doubleclue.dcem.saml.preferences.SamlPreferences;
import com.doubleclue.dcem.test.gui.SamlTestServiceView;
import com.doubleclue.dcem.test.logic.SeleniumApplication;
import com.doubleclue.dcem.test.units.SeleniumSamlTest;

@SessionScoped
public class TestSpServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SP_NAME_ID_FORMAT = NameIdFormatEnum.UNSPECIFIED.getFormat();

	@Inject
	SamlModule samlModule;

	@Inject
	UserLogic userLogic;
	
	@Inject
	SeleniumApplication seleniumApplication;

	@Inject
	SamlTestServiceView samlTestServiceView;

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpStatus.SC_UNAUTHORIZED);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String requestId = samlTestServiceView.getRequestId();
		String errorMessage = null;
		try {
			if (requestId == null) {
				throw new Exception("Not expecting a SAML Response.");
			}
			SamlPreferences preferences = samlModule.getModulePreferences();

			// Check Relay Token
			String relayToken = request.getParameter(SamlConstants.PARAM_RELAY_STATE);
			if (!relayToken.equals(SamlTestServiceView.SP_RELAY_TOKEN)) {
				throw new Exception("Relay Token does not match.");
			}

			// Parse Response
			String base64SamlResponse = request.getParameter(SamlConstants.PARAM_SAML_RESPONSE);
			Response samlResponse = (Response) SamlUtils.getXmlObjectFromBase64String(base64SamlResponse);

			if (!samlResponse.getDestination().equals(samlTestServiceView.getAcsLocation())) {
				throw new Exception("Destination does not match ACS location.");
			} else if (samlResponse.getIssueInstant().isAfterNow()) {
				throw new Exception("The Issue Instant cannot be after the time of receiving the response.");
			} else if (!samlResponse.getInResponseTo().equals(requestId)) {
				throw new Exception("Request ID does not match.");
			}

			// Check Status
			Status status = samlResponse.getStatus();
			if (status == null) {
				throw new Exception("Status is missing.");
			} else if (!status.getStatusCode().getValue().equals(SamlConstants.STATUS_SUCCESS)) {
				throw new Exception("Status is unsuccessful.");
			}

			// Check Issuer
			Issuer issuer = samlResponse.getIssuer();
			if (issuer == null) {
				throw new Exception("Issuer is missing.");
			} else if (!issuer.getValue().equals(preferences.getIdpEntityId())) {
				throw new Exception("Issuer does not match IdP Entity Id.");
			}

			// Check Assertions (should only be one)
			if (samlResponse.getAssertions().size() == 0) {
				throw new Exception("No Assertions found.");
			} else {
				for (Assertion assertion : samlResponse.getAssertions()) {
					// Check Signature
					String validationErrorMessage = validateSignature(assertion);
					if (validationErrorMessage != null) {
						throw new Exception(validationErrorMessage);
					}

					// Check Conditions
					Conditions conditions = assertion.getConditions();
					if (conditions == null) {
						throw new Exception("Conditions are missing.");
					} else if (conditions.getNotBefore() == null) {
						throw new Exception("Conditions should have a NotBefore value.");
					} else if (conditions.getNotOnOrAfter() == null) {
						throw new Exception("Conditions should have a NotOnOrAfter value.");
					} else if (conditions.getNotBefore().isAfterNow() || conditions.getNotOnOrAfter().isBeforeNow()) {
						throw new Exception("Currently outside valid date range for this response.");
					} else if (conditions.getAudienceRestrictions().size() == 0) {
						throw new Exception("No Audience Restrictions found.");
					} else {
						// Check Audiences
						boolean spFound = false;
						for (AudienceRestriction ar : conditions.getAudienceRestrictions()) {
							for (Audience a : ar.getAudiences()) {
								if (a.getAudienceURI().equals(SamlTestServiceView.SP_ENTITY_ID)) {
									spFound = true;
									break;
								}
								if (spFound)
									break;
							}
						}
						if (!spFound) {
							throw new Exception("SP is not a valid audience for this response.");
						}
					}
					// Check AuthnStatements (should only be one)
					if (assertion.getAuthnStatements().size() == 0) {
						throw new Exception("No AuthnStatements found.");
					} else {
						for (AuthnStatement as : assertion.getAuthnStatements()) {
							if (as.getAuthnInstant().isAfterNow()) {
								throw new Exception("The AuthnInstant cannot be after the time of receiving the response.");
							} else {
								AuthnContext authnContext = as.getAuthnContext();
								if (authnContext == null) {
									throw new Exception("AuthnContext is missing.");
								} else {
									AuthnContextClassRef accf = authnContext.getAuthnContextClassRef();
									if (accf == null) {
										throw new Exception("AuthnContextClassRef is missing.");
									} /*else if (!accf.getAuthnContextClassRef().equals(SamlConstants.AUTHN_CONTEXT_CLASS_PASSWORD_PROTECTED_TRANSPORT)) {
										throw new Exception("AuthnContextClassRef is not PasswordProtectedTransport.");
										}*/
								}
							}
						}
					}
					// Check Subject
					Subject subject = assertion.getSubject();
					if (subject == null) {
						throw new Exception("Subject is missing.");
					} else {
						NameID nameId = subject.getNameID();
						if (nameId == null) {
							throw new Exception("NameID is missing.");
						} else if (!nameId.getFormat().equals(SP_NAME_ID_FORMAT)) {
							throw new Exception("NameID is not in the expected format.");
						} else {
							String userId = nameId.getValue();
							DcemUser user = userLogic.getUser(userId);
							if (user == null) {
								throw new Exception("User not found.");
							}
						}
					}

					// Check all Claims/Attributes
					DcemUser testUser = seleniumApplication.getSeleniumUser();
					for (AttributeStatement attributeStatement : assertion.getAttributeStatements()) {
						for (Attribute attribute : attributeStatement.getAttributes()) {
							List <String> attributeValueList = new ArrayList<String>();
							for (XMLObject attributeValue : attribute.getAttributeValues()) {
								attributeValueList.add(attributeValue.getDOM().getTextContent());
							}
							switch (attribute.getName()) {
							case "User Login ID":
								if (attributeValueList.get(0).equals(testUser.getLoginId()) == false) {
									throw new Exception("Returned " + attribute.getName() + " Attribute does not match.");
								}
								break;
							case "User Display Name":
								if (attributeValueList.get(0).equals(testUser.getDisplayName()) == false) {
									throw new Exception("Returned " + attribute.getName() + " Attribute does not match.");
								}
								break;
							case "User Account Name":
								if (attributeValueList.get(0).equals(testUser.getAccountName()) == false) {
									throw new Exception("Returned " + attribute.getName() + " Attribute does not match.");
								}
								break;
							case "User Email":
								if (attributeValueList.get(0).equals(testUser.getEmail()) == false) {
									throw new Exception("Returned " + attribute.getName() + " Attribute does not match.");
								}
								break;
							case "User Principal Name":
								if (attributeValueList.get(0).equals(testUser.getUserPrincipalName()) == false) {
									throw new Exception("Returned " + attribute.getName() + " Attribute does not match.");
								}
								break;
							case "User Telephone":
								if (attributeValueList.get(0).equals(testUser.getTelephoneNumber()) == false) {
									throw new Exception("Returned " + attribute.getName() + " Attribute does not match.");
								}
								break;
							case "User Mobile":
								if (attributeValueList.get(0).equals(testUser.getMobile()) == false) {
									throw new Exception("Returned " + attribute.getName() + " Attribute does not match.");
								}
								break;
							case "User Locale":
								if (attributeValueList.get(0).equals(testUser.getLanguage().getLocale().toString()) == false) {
									throw new Exception("Returned " + attribute.getName() + " Attribute does not match.");
								}
								break;
							case "User CloudSafe":
								// TODO AR Validation to be added later as soon as the test for adding content are implemented
								break;
							case "Groups":
								// TODO AR Validation to be added later as soon as the test for adding content are implemented
								break;
							case "Domain Attribute":
								// TODO AR Validation to be added later as soon as the test for adding content are implemented
								break;
							case "Policy Name":
								if (attributeValueList.get(0).equals(SeleniumSamlTest.getPolicyName()) == false) {
									throw new Exception("Returned " + attribute.getName() + " Attribute does not match.");
								}
								break;
							case "Static Text":
								if (attributeValueList.get(0).equals(SeleniumSamlTest.getAttributeStaticText()) == false) {
									throw new Exception("Returned " + attribute.getName() + " Attribute does not match.");
								}
								break;
							default:
								break;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			errorMessage = e.getMessage();
		}

		printResponse(response, errorMessage);
	}

	private void printResponse(HttpServletResponse response, String errorMessage) throws IOException {
		response.setContentType("text/html");
		PrintWriter writer = response.getWriter();
		writer.write("<html>");
		writer.write("<head>");
		writer.write("<script type=\"text/javascript\" src=\"/dcem/javax.faces.resource/jquery/jquery.js.xhtml?ln=primefaces&amp;v=6.0\"></script>");
		writer.write("</head>");
		writer.write("<body>");
		writer.write("<p id=\"pageName\">Test Service Provider Return Page</p>");
		if (errorMessage != null) {
			writer.write("<p id=\"errorMessage\"> Error: " + errorMessage + "</p>");
		}
		writer.write("</body>");
		writer.write("</html>");
		writer.close();
	}

	private String validateSignature(Assertion assertion) throws Exception {

		String errorMessage = null;
		Signature signatureElement = assertion.getSignature();

		KeyInfo keyInfo = signatureElement.getKeyInfo();
		if (keyInfo != null) {
			Certificate requestCert = SamlUtils.getX509CertificateFromKeyInfo(keyInfo);
			if (samlModule.getIdpCertificate().equals(requestCert)) { // SP is using the IdP certificate
				try {
					BasicX509Credential credential = new BasicX509Credential((X509Certificate) requestCert);
					SignatureValidator.validate(signatureElement, credential);
				} catch (SignatureException e) {
					errorMessage = "The embedded signature is invalid.";
				}
			} else {
				errorMessage = "The Certificate in the request does not match the one in the Metadata.";
			}
		} else {
			errorMessage = "No KeyInfo found";
		}

		return errorMessage;
	}
}
