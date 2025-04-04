package com.doubleclue.dcem.saml.logic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.joda.time.DateTime;
import org.opensaml.core.config.ConfigurationService;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.XMLObjectBuilder;
import org.opensaml.core.xml.XMLObjectBuilderFactory;
import org.opensaml.core.xml.config.XMLObjectProviderRegistry;
import org.opensaml.core.xml.io.MarshallerFactory;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.core.xml.io.UnmarshallerFactory;
import org.opensaml.core.xml.io.UnmarshallingException;
import org.opensaml.core.xml.schema.XSString;
import org.opensaml.core.xml.schema.impl.XSStringBuilder;
import org.opensaml.saml.common.SAMLObjectContentReference;
import org.opensaml.saml.common.SAMLVersion;
import org.opensaml.saml.common.SignableSAMLObject;
import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeStatement;
import org.opensaml.saml.saml2.core.AttributeValue;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.Issuer;
import org.opensaml.saml.saml2.core.LogoutResponse;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.OneTimeUse;
import org.opensaml.saml.saml2.core.ProxyRestriction;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import org.opensaml.saml.saml2.core.StatusCode;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.NameIDFormat;
import org.opensaml.saml.saml2.metadata.SingleLogoutService;
import org.opensaml.saml.saml2.metadata.SingleSignOnService;
import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.UsageType;
import org.opensaml.security.x509.BasicX509Credential;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.ContentReference;
import org.opensaml.xmlsec.signature.support.SignatureException;
import org.opensaml.xmlsec.signature.support.Signer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.saml.logic.enums.NameIdFormatEnum;

import net.shibboleth.utilities.java.support.xml.SerializeSupport;

public class SamlUtils {

	private static XMLObjectBuilderFactory builderFactory;
	private static MarshallerFactory marshallerFactory;
	private static UnmarshallerFactory unmarshallerFactory;
	private static final Map<Class<?>, XMLObjectBuilder<XMLObject>> XMLObjectBuilderCache = new ConcurrentHashMap<Class<?>, XMLObjectBuilder<XMLObject>>();
	private static final Map<Class<?>, QName> elementNameCache = new ConcurrentHashMap<Class<?>, QName>();

	// Private Methods

	private static XMLObjectProviderRegistry getXmlObjectRegistry() {

		return ConfigurationService.get(XMLObjectProviderRegistry.class);
	}

	private static XMLObjectBuilderFactory getXmlObjectBuilderFactory() {

		if (builderFactory == null) {
			builderFactory = getXmlObjectRegistry().getBuilderFactory();
		}
		return builderFactory;
	}

	private static MarshallerFactory getMarshallerFactory() {

		if (marshallerFactory == null) {
			marshallerFactory = getXmlObjectRegistry().getMarshallerFactory();
		}
		return marshallerFactory;
	}

	private static UnmarshallerFactory getUnmarshallerFactory() {

		if (unmarshallerFactory == null) {
			unmarshallerFactory = getXmlObjectRegistry().getUnmarshallerFactory();
		}
		return unmarshallerFactory;
	}

	@SuppressWarnings("unchecked")
	private static <T extends XMLObject> XMLObjectBuilder<T> getXMLObjectBuilder(Class<T> type) {

		if (!XMLObjectBuilderCache.containsKey(type)) {

			QName elementName = getElementQName(type);

			XMLObjectBuilder<T> builder = (XMLObjectBuilder<T>) getXmlObjectBuilderFactory().getBuilder(elementName);
			if (builder == null) {
				throw new IllegalStateException("Cannot create builder.");
			}

			XMLObjectBuilderCache.put(type, (XMLObjectBuilder<XMLObject>) builder);
		}

		return (XMLObjectBuilder<T>) XMLObjectBuilderCache.get(type);
	}

	private static <T extends XMLObject> QName getElementQName(Class<T> type) {

		if (!elementNameCache.containsKey(type)) {

			try {
				Field typeField;
				try {
					typeField = type.getDeclaredField("DEFAULT_ELEMENT_NAME");
				} catch (NoSuchFieldException e) {
					try {
						typeField = type.getDeclaredField("ELEMENT_NAME");
					} catch (NoSuchFieldException e2) {
						typeField = type.getDeclaredField("TYPE_NAME");
					}
				}

				QName objectQName = (QName) typeField.get(null);
				elementNameCache.put(type, objectQName);

			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return elementNameCache.get(type);
	}

	private static void validateDateRange(DateTime notBefore, DateTime notOnOrAfter) throws IllegalStateException {

		if (notBefore != null && notOnOrAfter != null && notBefore.isAfter(notOnOrAfter)) {
			throw new IllegalStateException("The value of notBefore may not be after the value of notAfter.");
		}
	}

	private static byte[] getXmlByteArray(XMLObject xmlObject) throws MarshallingException {
		try {
			return getXmlString(xmlObject).getBytes(DcemConstants.CHARSET_UTF8);
		} catch (UnsupportedEncodingException e) {
			return null; // should never happen
		}
	}

	private static XMLObject getXmlObjectFromByteArray(byte[] xmlBytes)
			throws ParserConfigurationException, SAXException, IOException, UnmarshallingException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document requestDoc = builder.parse(new ByteArrayInputStream(xmlBytes));
		Element requestElement = requestDoc.getDocumentElement();

		return SamlUtils.unmarshallElement(requestElement);
	}

	// Public Methods

	public static String getXmlString(XMLObject xmlObject) throws MarshallingException {
		Element element = marshallXmlObject(xmlObject);
		return SerializeSupport.nodeToString(element);
	}

	public static byte[] decodeBase64String(String base64String) {
		base64String = base64String.replaceAll("\n", "");
		base64String = base64String.replaceAll("\r", "");
		return Base64.getDecoder().decode(base64String);
	}

	public static <T extends XMLObject> T createXmlObject(Class<T> type) {

		XMLObjectBuilder<T> builder = getXMLObjectBuilder(type);
		return builder.buildObject(getElementQName(type));
	}

	public static <T extends XMLObject> XSString createXSString(Class<T> type) {

		XSStringBuilder stringBuilder = (XSStringBuilder) getXMLObjectBuilder(XSString.class);
		return stringBuilder.buildObject(getElementQName(type), XSString.TYPE_NAME);
	}

	public static Element marshallXmlObject(XMLObject xmlObject) throws MarshallingException {

		return getMarshallerFactory().getMarshaller(xmlObject).marshall(xmlObject);
	}

	public static XMLObject unmarshallElement(Element element) throws UnmarshallingException {

		return getUnmarshallerFactory().getUnmarshaller(element).unmarshall(element);
	}

	public static String getBase64String(XMLObject xmlObject) throws MarshallingException {
		byte[] xmlBytes = getXmlByteArray(xmlObject);
		return Base64.getEncoder().encodeToString(xmlBytes);
	}

	public static String getDeflatedBase64String(XMLObject xmlObject) throws MarshallingException, IOException {

		byte[] xmlBytes = getXmlByteArray(xmlObject);

		ByteArrayOutputStream xmlDeflatedByteStream = new ByteArrayOutputStream();
		Deflater deflater = new Deflater(Deflater.DEFLATED, true);
		DeflaterOutputStream deflaterStream = new DeflaterOutputStream(xmlDeflatedByteStream, deflater);
		deflaterStream.write(xmlBytes);
		deflaterStream.finish();

		return Base64.getEncoder().encodeToString(xmlDeflatedByteStream.toByteArray());
	}

	public static XMLObject getXmlObjectFromBase64String(String base64String)
			throws ParserConfigurationException, SAXException, IOException, UnmarshallingException {

		byte[] xmlBytes = decodeBase64String(base64String);
		return getXmlObjectFromByteArray(xmlBytes);
	}

	public static XMLObject getXmlObjectFromDeflatedBase64String(String base64String)
			throws DataFormatException, ParserConfigurationException, SAXException, IOException, UnmarshallingException {

		byte[] xmlDeflatedBytes = decodeBase64String(base64String);

		byte[] inflaterBuffer = new byte[1024];
		Inflater inflater = new Inflater(true);
		inflater.setInput(xmlDeflatedBytes);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(xmlDeflatedBytes.length);

		while (!(inflater.finished() || inflater.needsInput())) {
			int count = inflater.inflate(inflaterBuffer);
			outputStream.write(inflaterBuffer, 0, count);
		}

		outputStream.close();
		byte[] xmlBytes = outputStream.toByteArray();
		inflater.end();
		new String(xmlBytes);
		return getXmlObjectFromByteArray(xmlBytes);
	}

	public static void signSamlObject(SignableSAMLObject samlObject, Credential credential, SamlSignatureSettings signatureSettings)
			throws MarshallingException, SignatureException, CertificateEncodingException {
		Signature signature = createSignature(credential, signatureSettings);
		samlObject.setSignature(signature);
		for (ContentReference contentReference : signature.getContentReferences()) {
			((SAMLObjectContentReference)contentReference).setDigestAlgorithm(signatureSettings.getDigestAlg().getFormat());
		}
		marshallXmlObject(samlObject);
		Signer.signObject(signature);
	}

	public static Certificate getX509CertificateFromKeyInfo(KeyInfo keyInfo) throws CertificateException {

		String certificateString = getX509CertificateStringFromKeyInfo(keyInfo);
		return getX509CertificateFromBase64String(certificateString);
	}

	public static String getX509CertificateStringFromKeyInfo(KeyInfo keyInfo) {

		List<X509Data> x509Datas = keyInfo.getX509Datas();
		if (x509Datas != null && x509Datas.size() > 0) {
			X509Data x509Data = x509Datas.get(0);
			List<X509Certificate> x509Certificates = x509Data.getX509Certificates();
			if (x509Certificates != null && x509Certificates.size() > 0) {
				X509Certificate x509Certificate = x509Certificates.get(0);
				return x509Certificate.getValue();
			}
		}

		return null;
	}

	public static Certificate getX509CertificateFromBase64String(String certificateString) throws CertificateException {

		if (certificateString != null && !certificateString.isEmpty()) {
			byte[] decoded = decodeBase64String(certificateString);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return cf.generateCertificate(new ByteArrayInputStream(decoded));
		} else {
			return null;
		}
	}

	public static Assertion createAssertion(String issuer, Subject subject, Conditions conditions, AuthnStatement authnStatement,
			AttributeStatement attributeStatement, Credential credential, SamlSignatureSettings signatureSettings)
			throws CertificateEncodingException, MarshallingException, SignatureException {

		Assertion assertion = createXmlObject(Assertion.class);
		assertion.setID("_" + UUID.randomUUID());
		assertion.setVersion(SAMLVersion.VERSION_20);
		assertion.setIssueInstant(new DateTime());
		assertion.setIssuer(createIssuer(issuer));
		assertion.setSubject(subject);
		assertion.setConditions(conditions);
		assertion.getAuthnStatements().add(authnStatement);
		assertion.getAttributeStatements().add(attributeStatement);

		if (credential != null) {
			signSamlObject(assertion, credential, signatureSettings);
		}

		return assertion;
	}

	public static Subject createSubject(String name, NameIdFormatEnum nameIdFormat, DateTime notOnOrAfter, String recipient, String requestId) {

		Subject subject = createXmlObject(Subject.class);
		subject.setNameID(createNameId(name, nameIdFormat));

		SubjectConfirmationData subjectConfirmationData = createXmlObject(SubjectConfirmationData.class);
		subjectConfirmationData.setRecipient(recipient);
		subjectConfirmationData.setNotOnOrAfter(notOnOrAfter);
		subjectConfirmationData.setInResponseTo(requestId);

		SubjectConfirmation subjectConfirmation = createXmlObject(SubjectConfirmation.class);
		subjectConfirmation.setMethod(SamlConstants.METHOD_BEARER);
		subjectConfirmation.setSubjectConfirmationData(subjectConfirmationData);

		subject.getSubjectConfirmations().add(subjectConfirmation);

		return subject;
	}

	public static NameID createNameId(String name, NameIdFormatEnum format) {

		NameID nameId = createXmlObject(NameID.class);
		nameId.setValue(name);
		nameId.setFormat(format.getFormat());
		return nameId;
	}

	public static Conditions createConditions(String[] audienceURIs, DateTime notBefore, DateTime notOnOrAfter, boolean oneTimeUse, int proxyCount) {

		validateDateRange(notBefore, notOnOrAfter);

		Conditions conditions = createXmlObject(Conditions.class);
		conditions.setNotBefore(notBefore);
		conditions.setNotOnOrAfter(notOnOrAfter);

		if (audienceURIs != null && audienceURIs.length > 0) {

			AudienceRestriction audienceRestriction = createXmlObject(AudienceRestriction.class);

			for (String audienceURI : audienceURIs) {
				Audience audience = createXmlObject(Audience.class);
				audience.setAudienceURI(audienceURI);
				audienceRestriction.getAudiences().add(audience);
			}

			conditions.getAudienceRestrictions().add(audienceRestriction);
		}

		if (oneTimeUse) {
			conditions.getConditions().add(createXmlObject(OneTimeUse.class));
		}

		if (proxyCount > -1) {
			ProxyRestriction proxyRestriction = createXmlObject(ProxyRestriction.class);
			proxyRestriction.setProxyCount(proxyCount);
			conditions.getConditions().add(proxyRestriction);
		}

		return conditions;
	}

	public static AuthnStatement createAuthnStatement(DateTime authInstant, String sessionIndex, String authnContextClassRefValue) {

		AuthnStatement authnStatement = createXmlObject(AuthnStatement.class);
		authnStatement.setAuthnInstant(authInstant);
		authnStatement.setSessionIndex(sessionIndex);

		if (authnContextClassRefValue != null) {

			AuthnContextClassRef authnContextClassRef = createXmlObject(AuthnContextClassRef.class);
			authnContextClassRef.setAuthnContextClassRef(authnContextClassRefValue);

			AuthnContext authnContext = createXmlObject(AuthnContext.class);
			authnContext.setAuthnContextClassRef(authnContextClassRef);

			authnStatement.setAuthnContext(authnContext);
		}

		return authnStatement;
	}

	public static AttributeStatement createAttributeStatement(Map<String, String[]> attributeValues) {

		AttributeStatement attributeStatement = createXmlObject(AttributeStatement.class);

		for (Map.Entry<String, String[]> entry : attributeValues.entrySet()) {
			Attribute attribute = createAttribute(entry.getKey(), entry.getValue());
			attributeStatement.getAttributes().add(attribute);
		}

		return attributeStatement;
	}

	public static Attribute createAttribute(String name, String[] values) {

		Attribute attribute = createXmlObject(Attribute.class);
		attribute.setName(name);
		attribute.setNameFormat(SamlConstants.ATTRIBUTE_FORMAT_BASIC);

		for (String value : values) {
			XSString attributeValue = createXSString(AttributeValue.class);
			attributeValue.setValue(value);
			attribute.getAttributeValues().add(attributeValue);
		}

		return attribute;
	}

	public static Status createStatus(String code) {

		StatusCode statusCode = createXmlObject(StatusCode.class);
		statusCode.setValue(code);

		Status status = createXmlObject(Status.class);
		status.setStatusCode(statusCode);
		return status;
	}

	public static Signature createSignature(Credential credential, SamlSignatureSettings signatureSettings) throws CertificateEncodingException {
		Signature signature = createXmlObject(Signature.class);
		signature.setSigningCredential(credential);
		signature.setSignatureAlgorithm(signatureSettings.getSignAlg().getFormat());
		signature.setCanonicalizationAlgorithm(signatureSettings.getC14nAlg().getFormat());
		signature.setKeyInfo(createKeyInfo(credential));
		return signature;
	}

	public static KeyInfo createKeyInfo(Credential credential) throws CertificateEncodingException {

		if (credential != null) {

			if (credential.getClass() == BasicX509Credential.class) {

				java.security.cert.X509Certificate certificate = ((BasicX509Credential) credential).getEntityCertificate();
				String certificateString = getBase64StringFromX509Certificate(certificate);

				X509Certificate certificateObject = createXmlObject(X509Certificate.class);
				certificateObject.setValue(certificateString);

				X509Data data = createXmlObject(X509Data.class);
				data.getX509Certificates().add(certificateObject);

				KeyInfo keyInfo = createXmlObject(KeyInfo.class);
				keyInfo.getX509Datas().add(data);

				return keyInfo;
			}
		}
		return null;
	}

	public static String getBase64StringFromX509Certificate(java.security.cert.X509Certificate certificate) throws CertificateEncodingException {
		return Base64.getEncoder().encodeToString(certificate.getEncoded()).replaceAll("(.{64})", "$1\n");
	}

	public static Issuer createIssuer(String value) {

		Issuer issuer = createXmlObject(Issuer.class);
		issuer.setValue(value);
		issuer.setFormat(SamlConstants.NAMEID_FORMAT_ENTITY);
		return issuer;
	}

	public static Response createResponse(String requestId, String issuer, String destination, String statusCode, Assertion assertion) {

		Response response = createXmlObject(Response.class);
		response.setID("_" + UUID.randomUUID());
		response.setInResponseTo(requestId);
		response.getAssertions().add(assertion);
		response.setIssueInstant(new DateTime());
		response.setDestination(destination);

		if (issuer != null) {
			response.setIssuer(createIssuer(issuer));
		}

		if (statusCode != null) {
			response.setStatus(createStatus(statusCode));
		}

		return response;
	}

	public static LogoutResponse createLogoutResponse(String requestId, String issuer, String destination, String statusCode, Credential credential,
			SamlSignatureSettings signatureSettings) throws CertificateEncodingException {

		LogoutResponse logoutResponse = createXmlObject(LogoutResponse.class);
		logoutResponse.setID("_" + UUID.randomUUID());
		logoutResponse.setInResponseTo(requestId);
		logoutResponse.setIssueInstant(new DateTime());
		logoutResponse.setDestination(destination);
		logoutResponse.setSignature(createSignature(credential, signatureSettings));

		if (issuer != null) {
			logoutResponse.setIssuer(createIssuer(issuer));
		}

		if (statusCode != null) {
			logoutResponse.setStatus(createStatus(statusCode));
		}

		return logoutResponse;
	}

	public static SingleSignOnService createSsoService(String location, String binding) {

		SingleSignOnService ssoService = createXmlObject(SingleSignOnService.class);
		ssoService.setLocation(location);
		ssoService.setBinding(binding);
		return ssoService;
	}

	public static SingleLogoutService createSloService(String location, String binding) {

		SingleLogoutService sloService = createXmlObject(SingleLogoutService.class);
		sloService.setLocation(location);
		sloService.setBinding(binding);
		return sloService;
	}

	public static EntityDescriptor createIdpMetadata(String entityId, String ssoServiceEndpoint, boolean wantsRequestsSigned,
			NameIdFormatEnum[] nameIdFormats, Credential credential) throws CertificateEncodingException {

		List<NameIDFormat> nameIdFormatObjects = new ArrayList<NameIDFormat>();
		for (NameIdFormatEnum format : nameIdFormats) {
			NameIDFormat nameIDFormatObject = createXmlObject(NameIDFormat.class);
			nameIDFormatObject.setFormat(format.getFormat());
			nameIdFormatObjects.add(nameIDFormatObject);
		}

		KeyDescriptor keyDescriptor = createXmlObject(KeyDescriptor.class);
		keyDescriptor.setUse(UsageType.SIGNING);
		keyDescriptor.setKeyInfo(createKeyInfo(credential));

		IDPSSODescriptor idpSsoDescriptor = createXmlObject(IDPSSODescriptor.class);
		idpSsoDescriptor.setWantAuthnRequestsSigned(wantsRequestsSigned);
		idpSsoDescriptor.addSupportedProtocol(SAMLConstants.SAML20P_NS);
		idpSsoDescriptor.getSingleSignOnServices().add(createSsoService(ssoServiceEndpoint, SAMLConstants.SAML2_REDIRECT_BINDING_URI));
		idpSsoDescriptor.getSingleSignOnServices().add(createSsoService(ssoServiceEndpoint, SAMLConstants.SAML2_POST_BINDING_URI));
		idpSsoDescriptor.getSingleLogoutServices().add(createSloService(ssoServiceEndpoint, SAMLConstants.SAML2_REDIRECT_BINDING_URI));
		idpSsoDescriptor.getSingleLogoutServices().add(createSloService(ssoServiceEndpoint, SAMLConstants.SAML2_POST_BINDING_URI));
		idpSsoDescriptor.getNameIDFormats().addAll(nameIdFormatObjects);
		idpSsoDescriptor.getKeyDescriptors().add(keyDescriptor);

		EntityDescriptor entityDescriptor = createXmlObject(EntityDescriptor.class);
		entityDescriptor.setEntityID(entityId);
		entityDescriptor.getRoleDescriptors().add(idpSsoDescriptor);

		return entityDescriptor;
	}
}
