package com.doubleclue.dcem.saml.logic.enums;

public enum NameIdFormatEnum {

	// DO NOT CHANGE THE ORDER OF THESE
	UNSPECIFIED("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified", "Unspecified"),
	EMAIL("urn:oasis:names:tc:SAML:1.1:nameid-format:emailAddress", "Email"),
	X509SUBJECT("urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName", "X.509 Subject"),
	WINDOWS_DOMAIN("urn:oasis:names:tc:SAML:1.1:nameid-format:WindowsDomainQualifiedName", "Windows Domain"),
	KERBEROS_PRINCIPAL("urn:oasis:names:tc:SAML:2.0:nameid-format:kerberos", "Kerberos Principal"),
	ENTITY("urn:oasis:names:tc:SAML:2.0:nameid-format:entity", "Entity"),
	PERSISTENT("urn:oasis:names:tc:SAML:2.0:nameid-format:persistent", "Persistent"),
	TRANSIENT("urn:oasis:names:tc:SAML:2.0:nameid-format:transient", "Transient");

	private final String format;
	private final String displayName;

	private NameIdFormatEnum(String format, String displayName) {
		this.format = format;
		this.displayName = displayName;
	}

	public static NameIdFormatEnum getFromString(String format) throws IllegalArgumentException {
		for (NameIdFormatEnum id : values()) {
			if (id.format.equals(format)) {
				return id;
			}
		}
		throw new IllegalArgumentException("Format " + format + " unknown");
	}

	public String getFormat() {
		return format;
	}
	
	public String getDisplayName() {
		return displayName;
	}
}