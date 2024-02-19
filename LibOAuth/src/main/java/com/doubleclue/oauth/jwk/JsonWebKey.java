package com.doubleclue.oauth.jwk;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import com.doubleclue.oauth.jwk.enums.JwkOp;
import com.doubleclue.oauth.jwk.enums.JwkParam;
import com.doubleclue.oauth.jwk.enums.JwkType;
import com.doubleclue.oauth.jwk.enums.JwkUse;
import com.doubleclue.oauth.utils.OAuthUtils;

public class JsonWebKey {

	protected final Map<JwkParam, Object> paramMap = new HashMap<>();

	public JsonWebKey() {
	}

	public JsonWebKey(JSONObject obj) {
		for (String key : obj.keySet()) {
			Object value = obj.get(key);
			JwkParam param = JwkParam.fromString(key);
			if (param != null) {
				setParam(param, value);
			}
		}
	}

	public JsonWebKey(String kid, JwkType type, JwkOp[] ops, JwkUse use, String modulus, String exponent) {

		setParam(JwkParam.KEY_ID, kid);
		setParam(JwkParam.KEY_TYPE, type.toString());
		setParam(JwkParam.USE, use.toString());
		setParam(JwkParam.MODULUS, modulus);
		setParam(JwkParam.EXPONENT, exponent);

		String[] opStrings = new String[ops.length];
		for (int i = 0; i < ops.length; i++) {
			opStrings[i] = ops[i].toString();
		}
		setParam(JwkParam.KEY_OPS, opStrings);
	}

	public String getJson() {
		return OAuthUtils.getJsonFromMaps(new Map[] { paramMap });
	}

	public String getKeyId() {
		return getParam(JwkParam.KEY_ID);
	}

	public JwkType getKeyType() {
		return getParam(JwkParam.KEY_TYPE);
	}

	public JwkOp[] getKeyOps() {
		String[] opStrings = getParam(JwkParam.KEY_OPS);
		if (opStrings != null) {
			JwkOp[] ops = new JwkOp[opStrings.length];
			for (int i = 0; i < opStrings.length; i++) {
				ops[i] = JwkOp.fromString(opStrings[i]);
			}
			return ops;
		}
		return null;
	}

	public JwkUse getUse() {
		return JwkUse.fromString(getParam(JwkParam.USE));
	}

	public String getModulus() {
		return getParam(JwkParam.MODULUS);
	}

	public String getExponent() {
		return getParam(JwkParam.EXPONENT);
	}

	public void setX509CertificateUrl(String url) {
		setParam(JwkParam.X509_URL, url);
	}

	public String getX509CertificateUrl() {
		return getParam(JwkParam.X509_URL);
	}

	public void setX509CertificateChain(X509Certificate[] certs) {
		if (certs != null) {
			String[] certStrings = new String[certs.length];
			for (int i = 0; i < certs.length; i++) {
				try {
					certStrings[i] = getBase64StringFromX509Certificate(certs[i]);
				} catch (CertificateEncodingException e) {
					certStrings[i] = null;
				}
			}
			setParam(JwkParam.X509_CHAIN, certStrings);
		}
	}

	public X509Certificate[] getX509CertificateChain() {
		String[] certStrings = getParam(JwkParam.X509_CHAIN);
		if (certStrings != null) {
			X509Certificate[] certs = new X509Certificate[certStrings.length];
			for (int i = 0; i < certs.length; i++) {
				try {
					certs[i] = (X509Certificate) getX509CertificateFromBase64String(certStrings[i]);
				} catch (CertificateException e) {
					certs[i] = null;
				}
			}
			return certs;
		}
		return null;
	}

	public void setX509CertificateSha1Thumbprint(String thumbprint) {
		setParam(JwkParam.X509_SHA1_THUMBPRINT, thumbprint);
	}

	public String getX509CertificateSha1Thumbprint() {
		return getParam(JwkParam.X509_SHA1_THUMBPRINT);
	}

	public void setX509CertificateSha256Thumbprint(String thumbprint) {
		setParam(JwkParam.X509_SHA256_THUMBPRINT, thumbprint);
	}

	public String getX509CertificateSha256Thumbprint() {
		return getParam(JwkParam.X509_SHA256_THUMBPRINT);
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T getParam(JwkParam key) {
		return (T) paramMap.get(key);
	}

	public void setParam(JwkParam key, Object value) {
		if (value != null) {
			paramMap.put(key, key.getExpectedClass().cast(value));
		}
	}

	private X509Certificate getX509CertificateFromBase64String(String certificateString) throws CertificateException {
		if (certificateString != null && !certificateString.isEmpty()) {
			byte[] decoded = decodeBase64String(certificateString);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(decoded));
		} else {
			return null;
		}
	}

	private static String getBase64StringFromX509Certificate(X509Certificate certificate) throws CertificateEncodingException {
		return Base64.getEncoder().encodeToString(certificate.getEncoded()).replaceAll("(.{64})", "$1\n");
	}

	private byte[] decodeBase64String(String base64String) {
		return Base64.getDecoder().decode(base64String.replaceAll("\n", "").replaceAll("\r", ""));
	}
}
