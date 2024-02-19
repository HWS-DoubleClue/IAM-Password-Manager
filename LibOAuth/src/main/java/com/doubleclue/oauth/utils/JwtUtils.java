package com.doubleclue.oauth.utils;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtUtils {

	public static final String JWT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

	@SuppressWarnings("rawtypes")
	public static Jwt getJwtFromString(String jwtString, String secret) {
		if (jwtString != null && secret != null) {
			return Jwts.parser().setSigningKey(secret.getBytes(StandardCharsets.UTF_8)).parse(jwtString);
		}
		return null;
	}

	public static Key getSigningKeyFromSecret(String secret, SignatureAlgorithm signatureAlgorithm) {
		byte[] apiKeySecretBytes = secret.getBytes(StandardCharsets.UTF_8);
		return new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
	}
}
