package com.doubleclue.oauth.openid;

import java.util.Locale;

import com.doubleclue.oauth.oauth2.OAuthServerMetadata;
import com.doubleclue.oauth.oauth2.enums.OAuthServerMetadataSetting;
import com.doubleclue.oauth.openid.enums.OpenIdClaim;

import io.jsonwebtoken.SignatureAlgorithm;

public class OpenIdConfiguration extends OAuthServerMetadata {

	public OpenIdConfiguration(String json) {
		super(json);
	}

	public OpenIdConfiguration(OAuthServerMetadata metadata) {
		settingMap.putAll(metadata.getSettingMap());
	}

	public void setUserInfoEndpoint(String endpoint, SignatureAlgorithm[] signingAlgs, String[] encryptionAlgs, String[] encryptionEncValues) {
		setSetting(OAuthServerMetadataSetting.USERINFO_ENDPOINT, endpoint);
		setSigningAlgs(OAuthServerMetadataSetting.USERINFO_SIGNING_ALG_VALUES_SUPPORTED, signingAlgs);
		setSetting(OAuthServerMetadataSetting.USERINFO_ENCRYPTION_ALG_VALUES_SUPPORTED, encryptionAlgs);
		setSetting(OAuthServerMetadataSetting.USERINFO_ENCRYPTION_ENC_VALUES_SUPPORTED, encryptionEncValues);
	}

	public String getUserInfoEndpoint() {
		return getSetting(OAuthServerMetadataSetting.USERINFO_ENDPOINT);
	}

	public SignatureAlgorithm[] getUserInfoSigningAlgs() {
		return getSigningAlgs(OAuthServerMetadataSetting.USERINFO_SIGNING_ALG_VALUES_SUPPORTED);
	}

	public String[] getUserInfoEncryptionAlgs() {
		return getSetting(OAuthServerMetadataSetting.USERINFO_ENCRYPTION_ALG_VALUES_SUPPORTED);
	}

	public String[] getUserInfoEncryptionEncValues() {
		return getSetting(OAuthServerMetadataSetting.USERINFO_ENCRYPTION_ENC_VALUES_SUPPORTED);
	}

	public void setIdTokenConfig(SignatureAlgorithm[] signingAlgs, String[] encryptionAlgs, String[] encryptionEncValues) {
		setSigningAlgs(OAuthServerMetadataSetting.ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED, signingAlgs);
		setSetting(OAuthServerMetadataSetting.ID_TOKEN_ENCRYPTION_ALG_VALUES_SUPPORTED, encryptionAlgs);
		setSetting(OAuthServerMetadataSetting.ID_TOKEN_ENCRYPTION_ENC_VALUES_SUPPORTED, encryptionEncValues);
	}

	public SignatureAlgorithm[] getIdTokenSigningAlgs() {
		return getSigningAlgs(OAuthServerMetadataSetting.ID_TOKEN_SIGNING_ALG_VALUES_SUPPORTED);
	}

	public String[] getIdTokenEncryptionAlgs() {
		return getSetting(OAuthServerMetadataSetting.ID_TOKEN_ENCRYPTION_ALG_VALUES_SUPPORTED);
	}

	public String[] getIdTokenEncryptionEncValues() {
		return getSetting(OAuthServerMetadataSetting.ID_TOKEN_ENCRYPTION_ENC_VALUES_SUPPORTED);
	}

	public void setDisplayValues(String[] displayValues) {
		setSetting(OAuthServerMetadataSetting.DISPLAY_VALUES_SUPPORTED, displayValues);
	}

	public String getDisplayValues() {
		return getSetting(OAuthServerMetadataSetting.DISPLAY_VALUES_SUPPORTED);
	}

	public void setClaimConfig(String[] types, OpenIdClaim[] claims, Locale[] locales, boolean parameterSupported) {
		if (claims != null) {
			String[] claimStrings = new String[claims.length];
			for (int i = 0; i < claims.length; i++) {
				claimStrings[i] = claims[i].toString();
			}
			setSetting(OAuthServerMetadataSetting.CLAIMS_SUPPORTED, claimStrings);
		}
		if (locales != null) {
			String[] localeStrings = new String[locales.length];
			for (int i = 0; i < locales.length; i++) {
				localeStrings[i] = locales[i].getLanguage();
			}
			setSetting(OAuthServerMetadataSetting.CLAIMS_LOCALES_SUPPORTED, localeStrings);
		}
		setSetting(OAuthServerMetadataSetting.CLAIM_TYPES_SUPPORTED, types);
		setSetting(OAuthServerMetadataSetting.CLAIMS_PARAMETER_SUPPORTED, parameterSupported);
	}

	public String[] getSupportedClaimTypes() {
		return getSetting(OAuthServerMetadataSetting.CLAIM_TYPES_SUPPORTED);
	}

	public OpenIdClaim[] getSupportedClaims() {
		String[] claimStrings = getSetting(OAuthServerMetadataSetting.CLAIMS_SUPPORTED);
		if (claimStrings != null) {
			OpenIdClaim[] claims = new OpenIdClaim[claimStrings.length];
			for (int i = 0; i < claimStrings.length; i++) {
				claims[i] = OpenIdClaim.fromString(claimStrings[i]);
			}
			return claims;
		}
		return null;
	}

	public Locale[] getSupportedClaimsLocales() {
		String[] localeStrings = getSetting(OAuthServerMetadataSetting.CLAIMS_LOCALES_SUPPORTED);
		if (localeStrings != null) {
			Locale[] locales = new Locale[localeStrings.length];
			for (int i = 0; i < localeStrings.length; i++) {
				locales[i] = new Locale(localeStrings[i]);
			}
			return locales;
		}
		return null;
	}

	public boolean isClaimsParameterSupported() {
		return getBooleanSetting(OAuthServerMetadataSetting.CLAIMS_PARAMETER_SUPPORTED);
	}

	public void setRequestConfig(boolean parameterSupported, boolean uriSupported, boolean requireRequestUriRegistration) {
		setSetting(OAuthServerMetadataSetting.REQUEST_PARAMETER_SUPPORTED, parameterSupported);
		setSetting(OAuthServerMetadataSetting.REQUEST_URI_PARAMETER_SUPPORTED, uriSupported);
		setSetting(OAuthServerMetadataSetting.REQUIRE_REQUEST_URI_REGISTRATION, requireRequestUriRegistration);
	}

	public boolean isRequestParameterSupported() {
		return getBooleanSetting(OAuthServerMetadataSetting.REQUEST_PARAMETER_SUPPORTED);
	}

	public boolean isRequestUriParameterSupported() {
		return getBooleanSetting(OAuthServerMetadataSetting.REQUEST_URI_PARAMETER_SUPPORTED);
	}

	public boolean isRequireRequestUriRegistration() {
		return getBooleanSetting(OAuthServerMetadataSetting.REQUIRE_REQUEST_URI_REGISTRATION);
	}
}
