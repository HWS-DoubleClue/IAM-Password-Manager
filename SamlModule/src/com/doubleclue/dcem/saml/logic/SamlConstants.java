package com.doubleclue.dcem.saml.logic;

public class SamlConstants {

	public static final String DIALOG_SP_METADATA = "/modules/saml/spMetadataDialog.xhtml";
	public static final String DIALOG_IDP_METADATA = "/modules/saml/downloadIdpMetadataDialog.xhtml";

	public static final String ACTION_DOWNLOAD_IDP_METADATA = "downloadIdpMetadata";
	public static final String ICON_DOWNLOAD_IDP_METADATA = "fa fa-download";

	public static final String FILENAME_IDP_METADATA = "idp_metadata.xml";
	public static final String FILENAME_IDP_CERTIFICATE = "idp_certificate.pem";

	public static final String PATH_JSF_PAGES = "saml";
	public static final String JSF_PAGE_LOGIN = "login.xhtml";
	public static final String JSF_PAGE_RETURN_TO_SP = "return.xhtml";
	public static final String JSF_PAGE_ERROR = "error_.xhtml";

	public static final String QUERY_STRING_IDP_INITIATED = "idpinit";

	public static final String RESOURCE_DIR_PRESETS = "com/doubleclue/dcem/saml/presets";
	public static final String RESOURCE_TYPE_PRESETS = ".xml";

	// SAML Keywords not in org.opensaml.saml.common.xml.SAMLConstants

	public static final String METHOD_BEARER = "urn:oasis:names:tc:SAML:2.0:cm:bearer";
	public static final String AUTHN_CONTEXT_CLASS_PASSWORD_PROTECTED_TRANSPORT = "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";
	public static final String ATTRIBUTE_FORMAT_BASIC = "urn:oasis:names:tc:SAML:2.0:attrname-format:basic";
	public static final String STATUS_SUCCESS = "urn:oasis:names:tc:SAML:2.0:status:Success";
	public static final String NAMEID_FORMAT_ENTITY = "urn:oasis:names:tc:SAML:2.0:nameid-format:entity";

	public static final String PARAM_SAML_REQUEST = "SAMLRequest";
	public static final String PARAM_SAML_RESPONSE = "SAMLResponse";
	public static final String PARAM_SAML_ARTIFACT = "SAMLart";
	public static final String PARAM_RELAY_STATE = "RelayState";
	public static final String PARAM_SIGNATURE = "Signature";
	public static final String PARAM_SIG_ALG = "SigAlg";
}
