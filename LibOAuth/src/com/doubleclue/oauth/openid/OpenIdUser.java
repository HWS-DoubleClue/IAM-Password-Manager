package com.doubleclue.oauth.openid;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONObject;

import com.doubleclue.oauth.openid.enums.OpenIdClaim;
import com.doubleclue.oauth.openid.enums.OpenIdScope;
import com.doubleclue.oauth.utils.JwtUtils;
import com.doubleclue.oauth.utils.OAuthUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class OpenIdUser {

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private final Map<OpenIdClaim, Object> claimMap = new HashMap<>();
	private final Map<String, Object> customClaimMap = new HashMap<>();

	public OpenIdUser() {
	}

	public OpenIdUser(String jsonString) {
		JSONObject obj = new JSONObject(jsonString);
		for (String key : obj.keySet()) {
			OpenIdClaim claim = OpenIdClaim.fromString(key);
			if (claim != null) {
				if (claim == OpenIdClaim.ADDRESS) {
					setClaim(claim, new OpenIdAddress(obj.getJSONObject(key)));
				} else {
					setClaim(claim, obj.get(key));
				}
			} else {
				setCustomClaim(key, obj.get(key));
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public OpenIdUser(Jwt jwt) {
		if (jwt != null) {
			Claims body = (Claims) jwt.getBody();
			for (String key : body.keySet()) {
				OpenIdClaim claim = OpenIdClaim.fromString(key);
				if (claim != null) {
					if (claim == OpenIdClaim.ADDRESS) {
						setClaim(claim, new OpenIdAddress((String) body.get(key)));
					} else {
						setClaim(claim, body.get(key));
					}
				} else {
					setCustomClaim(key, body.get(key));
				}
			}
		}
	}

	public void setClaim(OpenIdClaim key, Object value) {
		if (value != null) {
			claimMap.put(key, key.getExpectedClass().cast(value));
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T getClaim(OpenIdClaim key) {
		return (T) claimMap.get(key);
	}

	public boolean getBooleanClaim(OpenIdClaim key) {
		Boolean param = getClaim(key);
		return param != null ? param : false;
	}

	public void setCustomClaim(String key, Object value) {
		if (!isNullOrEmpty(key) && value != null) {
			customClaimMap.put(key, value);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T getCustomClaim(String key) {
		return (T) customClaimMap.get(key);
	}

	private static boolean isNullOrEmpty(String s) {
		return s == null || s.isEmpty();
	}

	public String getJson() {
		return OAuthUtils.getJsonFromMaps(new Map[] { claimMap, customClaimMap });
	}

	public Map<String, Object> getRequestedClaims(OpenIdScope[] scopes, OpenIdClaimsRequest claimsRequest) {

		Map<String, Object> requestedClaimMap = new HashMap<>();

		// Add claims according to scopes
		if (scopes != null) {
			List<OpenIdClaim> claims = new ArrayList<>();
			for (OpenIdScope scope : scopes) {
				claims.addAll(Arrays.asList(scope.getReqestedClaims()));
			}
			for (OpenIdClaim claim : claims) {
				requestedClaimMap.put(claim.getValue(), getClaim(claim));
			}
		}

		// Add claims according to Claims Request Parameter
		if (claimsRequest != null) {
			Map<OpenIdClaim, OpenIdClaimsRequestInfo> claimMap = claimsRequest.getClaimMap();
			for (OpenIdClaim claim : claimMap.keySet()) {
				OpenIdClaimsRequestInfo info = claimMap.get(claim);
				if (info == null) {
					requestedClaimMap.put(claim.getValue(), getClaim(claim));
				} else {
					Object value;
					if (!isNullOrEmpty(info.getValue())) {
						value = info.getValue();
					} else if (info.getValues() != null) {
						value = info.getValues();
					} else {
						value = getClaim(claim);
					}
					requestedClaimMap.put(claim.getValue(), value);
				}
			}
			Map<String, OpenIdClaimsRequestInfo> requetCustomClaimMap = claimsRequest.getCustomClaimMap();
			for (String claim : requetCustomClaimMap.keySet()) {
				OpenIdClaimsRequestInfo info = requetCustomClaimMap.get(claim);
				if (info == null) {
					requestedClaimMap.put(claim, getCustomClaim(claim));
				} else {
					Object value;
					if (!isNullOrEmpty(info.getValue())) {
						value = info.getValue();
					} else if (info.getValues() != null) {
						value = info.getValues();
					} else {
						value = getCustomClaim(claim);
					}
					requestedClaimMap.put(claim, value);
				}
			}
			for (String claim : customClaimMap.keySet()) {
				requestedClaimMap.put(claim, getCustomClaim(claim));
			}
		}

		return requestedClaimMap;
	}

	public String getJson(OpenIdScope[] scopes, OpenIdClaimsRequest claimsRequest) {

		Map<String, Object> requestedClaimMap = getRequestedClaims(scopes, claimsRequest);

		// Always add the Subject claim
		requestedClaimMap.put(OpenIdClaim.SUBJECT.getValue(), getClaim(OpenIdClaim.SUBJECT));

		return OAuthUtils.getJsonFromMaps(new Map[] { requestedClaimMap });
	}

	public String getJwtString(OpenIdScope[] scopes, OpenIdClaimsRequest claimsRequest, SignatureAlgorithm signatureAlgorithm, String apiSecret, String issuer,
			String id, long expiresInMillis, String audience, String nonce, String accessToken, String authCode, LocalDateTime authTime) {

		Map<String, Object> jwtClaims = getRequestedClaims(scopes, claimsRequest);
		jwtClaims.put(OpenIdClaim.ACR.toString(), getAcr());

		// Hybrid Flow
		if (!isNullOrEmpty(nonce)) {
			setNonce(nonce);
			jwtClaims.put(OpenIdClaim.NONCE.toString(), getNonce());
			if (!isNullOrEmpty(accessToken)) {
				setAccessTokenHash(signatureAlgorithm, accessToken);
				jwtClaims.put(OpenIdClaim.ACCESS_TOKEN_HASH.toString(), getAccessTokenHash());
			}
			if (!isNullOrEmpty(authCode)) {
				setAuthCodeHash(signatureAlgorithm, authCode);
				jwtClaims.put(OpenIdClaim.AUTH_CODE_HASH.toString(), getAuthCodeHash());
			}
		}

		if (authTime != null) {
			setAuthTime(authTime);
			jwtClaims.put(OpenIdClaim.AUTH_TIME.toString(), getAuthTime());
		}

		Key signingKey = JwtUtils.getSigningKeyFromSecret(apiSecret, signatureAlgorithm);
		Date now = new Date();
		Date expiry = new Date(now.getTime() + expiresInMillis);
		return Jwts.builder().setClaims(jwtClaims).setSubject(getSubject()).setId(id).setIssuedAt(now).setExpiration(expiry).setIssuer(issuer)
				.setAudience(audience).signWith(signingKey).compact();
	}

	public void setSubject(String subject) {
		setClaim(OpenIdClaim.SUBJECT, subject);
	}

	public String getSubject() {
		return getClaim(OpenIdClaim.SUBJECT);
	}

	public void setName(String fullName, String givenName, String familyName, String middleName, String nickname, String preferredUsername) {
		setClaim(OpenIdClaim.FULL_NAME, fullName);
		setClaim(OpenIdClaim.GIVEN_NAME, givenName);
		setClaim(OpenIdClaim.FAMILY_NAME, familyName);
		setClaim(OpenIdClaim.MIDDLE_NAME, middleName);
		setClaim(OpenIdClaim.NICKNAME, nickname);
		setClaim(OpenIdClaim.PREFERRED_USERNAME, preferredUsername);
	}

	public String getFullName() {
		return getClaim(OpenIdClaim.FULL_NAME);
	}

	public String getGivenName() {
		return getClaim(OpenIdClaim.GIVEN_NAME);
	}

	public String getFamilyName() {
		return getClaim(OpenIdClaim.FAMILY_NAME);
	}

	public String getMiddleName() {
		return getClaim(OpenIdClaim.MIDDLE_NAME);
	}

	public String getNickname() {
		return getClaim(OpenIdClaim.NICKNAME);
	}

	public String getPreferredUsername() {
		return getClaim(OpenIdClaim.PREFERRED_USERNAME);
	}

	public void setProfile(String profileUrl, String pictureUrl) {
		setClaim(OpenIdClaim.PROFILE_URL, profileUrl);
		setClaim(OpenIdClaim.PICTURE_URL, pictureUrl);
	}

	public String getProfileUrl() {
		return getClaim(OpenIdClaim.PROFILE_URL);
	}

	public String getPictureUrl() {
		return getClaim(OpenIdClaim.PICTURE_URL);
	}

	public void setWebsite(String website) {
		setClaim(OpenIdClaim.WEBSITE, website);
	}

	public String getWebsite() {
		return getClaim(OpenIdClaim.WEBSITE);
	}

	public void setEmail(String email, boolean verified) {
		setClaim(OpenIdClaim.EMAIL, email);
		setClaim(OpenIdClaim.EMAIL_VERIFIED, verified);
	}

	public String getEmail() {
		return getClaim(OpenIdClaim.EMAIL);
	}

	public boolean isEmailVerified() {
		return getBooleanClaim(OpenIdClaim.EMAIL_VERIFIED);
	}

	public void setGender(String gender) {
		setClaim(OpenIdClaim.GENDER, gender);
	}

	public String getGender() {
		return getClaim(OpenIdClaim.GENDER);
	}

	public void setDoB(Date dob) {
		setClaim(OpenIdClaim.DOB, dateFormat.format(dob));
	}

	public Date getDoB() {
		try {
			return dateFormat.parse(getClaim(OpenIdClaim.DOB));
		} catch (Exception e) {
			return null;
		}
	}

	public void setTimeZone(TimeZone timeZone) {
		setClaim(OpenIdClaim.TIME_ZONE, timeZone.getDisplayName());
	}

	public TimeZone getTimeZone() {
		return TimeZone.getTimeZone((String) getClaim(OpenIdClaim.TIME_ZONE));
	}

	public void setLocale(Locale locale) {
		setClaim(OpenIdClaim.LOCALE, locale.getLanguage());
	}

	public Locale getLocale() {
		return new Locale(getClaim(OpenIdClaim.LOCALE));
	}

	public void setPhoneNumber(String phoneNumber, boolean verified) {
		setClaim(OpenIdClaim.PHONE_NUMBER, phoneNumber);
		setClaim(OpenIdClaim.PHONE_NUMBER_VERIFIED, verified);
	}

	public String getPhoneNumber() {
		return getClaim(OpenIdClaim.PHONE_NUMBER);
	}

	public boolean isPhoneNumberVerified() {
		return getBooleanClaim(OpenIdClaim.PHONE_NUMBER_VERIFIED);
	}

	public void setAddress(OpenIdAddress address) {
		setClaim(OpenIdClaim.ADDRESS, address.getJson());
	}

	public OpenIdAddress getAddress() {
		String json = getClaim(OpenIdClaim.ADDRESS);
		return new OpenIdAddress(json);
	}

	public void setUpdatedAt(Date updatedAt) {
		setClaim(OpenIdClaim.UPDATED_AT, updatedAt.getTime() / 1000);
	}

	public Date getUpdatedAt() {
		try {
			Long updatedAt = getClaim(OpenIdClaim.UPDATED_AT);
			if (updatedAt != null) {
				return new Date(updatedAt * 1000);
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public void setNonce(String nonce) {
		setClaim(OpenIdClaim.NONCE, nonce);
	}

	public String getNonce() {
		return getClaim(OpenIdClaim.NONCE);
	}

	public void setAccessTokenHash(SignatureAlgorithm signatureAlgorithm, String accessToken) {
		setHashClaim(OpenIdClaim.ACCESS_TOKEN_HASH, signatureAlgorithm, accessToken);
	}

	public String getAccessTokenHash() {
		return getClaim(OpenIdClaim.ACCESS_TOKEN_HASH);
	}

	public void setAuthCodeHash(SignatureAlgorithm signatureAlgorithm, String authCode) {
		setHashClaim(OpenIdClaim.AUTH_CODE_HASH, signatureAlgorithm, authCode);
	}

	public String getAuthCodeHash() {
		return getClaim(OpenIdClaim.AUTH_CODE_HASH);
	}

	public void setAuthTime(LocalDateTime authTime) {
		if (authTime != null) {
			setClaim(OpenIdClaim.AUTH_TIME, authTime.toEpochSecond(ZoneOffset.UTC));	
		}
	}

	public Long getAuthTime() {
		return getClaim(OpenIdClaim.AUTH_TIME);
	}

	public Date getAuthTimeAsDate() {
		try {
			Long authTime = getAuthTime();
			if (authTime != null) {
				return new Date(authTime * 1000);
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	public void setAcr(String acr) {
		setClaim(OpenIdClaim.ACR, acr);
	}

	public String getAcr() {
		return getClaim(OpenIdClaim.ACR);
	}

	private void setHashClaim(OpenIdClaim claim, SignatureAlgorithm signatureAlgorithm, String text) {
		if (!isNullOrEmpty(text)) {
			try {
				String digestAlg = getDigestAlgorithm(signatureAlgorithm);
				byte[] hash;
				if (digestAlg != null) {
					MessageDigest digest = MessageDigest.getInstance(digestAlg);
					hash = digest.digest(text.getBytes(StandardCharsets.US_ASCII));
				} else {
					hash = text.getBytes(StandardCharsets.US_ASCII);
				}
				byte[] halfHash = new byte[hash.length / 2];
				System.arraycopy(hash, 0, halfHash, 0, halfHash.length);
				String encoded = Base64.getUrlEncoder().encodeToString(halfHash);
				encoded = encoded.replaceAll("=", "");
				setClaim(claim, encoded);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
		}
	}

	private String getDigestAlgorithm(SignatureAlgorithm signatureAlgorithm) {
		switch (signatureAlgorithm) {
		case HS256:
		case RS256:
		case ES256:
		case PS256:
			return "SHA-256";
		case HS384:
		case RS384:
		case ES384:
		case PS384:
			return "SHA-384";
		case HS512:
		case RS512:
		case ES512:
		case PS512:
			return "SHA-512";
		default:
			return null;
		}
	}
}
