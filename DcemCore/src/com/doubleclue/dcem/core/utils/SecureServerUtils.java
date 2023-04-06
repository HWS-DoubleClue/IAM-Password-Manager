package com.doubleclue.dcem.core.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.io.CipherInputStream;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.IPAddress;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.KeyStorePurpose;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.RandomUtils;
import com.doubleclue.utils.SecureUtils;

public class SecureServerUtils {

	// static { Application should add Bouncy Castle once
	// Security.addProvider(new BouncyCastleProvider());
	// }

	static final String KEY_ALGORITHM = "AES";
	static final String CHIPER_TYPE = "AES/CBC/PKCS5Padding";

	static final int SALT_LENGTH = 8;
	public final static int MAX_CIPHER_BUFFER = 1024 * 64;

//	private static final byte[] salt = { (byte) 0x43, (byte) 0x76, (byte) 0x95, (byte) 0xc7, (byte) 0x5b, (byte) 0xd7, (byte) 0x45, (byte) 0x17 };

	public static final byte[] ENCRYPTION_ALGORITHM_IV = { (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55,
			(byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00 };

	public static final byte[] ENCRYPTION_ALGORITHM_IV_32 = { (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55,
			(byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00,
			(byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x55,
			(byte) 0x00, (byte) 0x55, (byte) 0x00 };

	private static final AlgorithmParameterSpec algorithmParameterSpec = new IvParameterSpec(ENCRYPTION_ALGORITHM_IV);

	public static final String DEFAULT_SUPPORTED_CIPHERS = "TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384, TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384, TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA, "
			+ "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA, TLS_DHE_RSA_WITH_AES_256_CBC_SHA, TLS_RSA_WITH_AES_256_CBC_SHA";

	// private static final String SIGNATURE_ALGORITHM_SPONGY = "SHA256WithRSAEncryption";

	private static final String SIGNATURE_ALGORITHM = "SHA256WithRSA";

	static byte[] commonKey = { (byte) 0xA7, (byte) 0xF3, (byte) 0x46, (byte) 0xCF, (byte) 0xF3, (byte) 0x17, (byte) 0x54, (byte) 0x3c, (byte) 0x63,
			(byte) 0xC3, (byte) 0x32, (byte) 0x4E, (byte) 0x01, (byte) 0xF0, (byte) 0x55, (byte) 0x67 };

	// private static final int ROOT_KEYSIZE = 2048;

	private static final String KEY_GENERATION_ALGORITHM = "RSA";

	private static SecretKey macKeySha1 = new SecretKeySpec(commonKey, "HmacSHA1");

	// private static final boolean REGENERATE_FRESH_CA_CERTIFICATE = false;

	public static PrivateKey loadPrivateKey(byte[] value) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(new PKCS8EncodedKeySpec(value));
	}

	public static PublicKey loadPublicKey(byte[] value) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePublic(new X509EncodedKeySpec(value));
	}

	/**
	 * Create a random 2048 bit RSA key pair with the given length
	 * 
	 * @throws NoSuchProviderException
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyPair generateKeyPair(int keySize) throws NoSuchAlgorithmException, NoSuchProviderException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(KEY_GENERATION_ALGORITHM);
		generator.initialize(keySize, new SecureRandom());
		return generator.generateKeyPair();
	}

	/**
	 * @param cn
	 * @param keyPair
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 * @throws IOException
	 * @throws CertificateException
	 * @throws OperatorCreationException
	 */
	// public static byte[] generatePKCS10(String cn, KeyPair keyPair) throws NoSuchAlgorithmException,
	// InvalidKeyException, SignatureException, IOException, CertificateException {
	// X500Principal x500Principal = new X500Principal("cn=" + cn);

	// PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(x500Principal,
	// keyPair.getPublic());
	// JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM);
	// ContentSigner signer = csBuilder.build(keyPair.getPrivate());
	// PKCS10CertificationRequest csr = p10Builder.build(signer);
	// return csr.getEncoded();
	// return null;
	// }

	public static byte[] getSha1(String value) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md = MessageDigest.getInstance("SHA-1");
		md.update(value.getBytes("UTF-8"));
		return md.digest();
	}

	// private String convertCertificateToPEM(X509Certificate signedCertificate)
	// throws IOException {
	// StringWriter signedCertificatePEMDataStringWriter = new StringWriter();
	// PemWriter pemWriter = new
	// PemWriter(signedCertificatePEMDataStringWriter);
	// pemWriter.
	// pemWriter.writeObject(signedCertificate);
	// pemWriter.close();
	// log.info("PEM data:");
	// log.info("" + signedCertificatePEMDataStringWriter.toString());
	// return signedCertificatePEMDataStringWriter.toString();
	// }

	private static X509Certificate signCertificate(X509v3CertificateBuilder certificateBuilder, PrivateKey signedWithPrivateKey)
			throws OperatorCreationException, CertificateException {

		ContentSigner signer = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).setProvider(BouncyCastleProvider.PROVIDER_NAME).build(signedWithPrivateKey);
		return new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate(certificateBuilder.build(signer));
	}

	/**
	 * 
	 * Create a certificate to use by a Certificate Authority, signed by a self
	 * signed certificate.
	 * 
	 */
	public static X509Certificate createCertificate(PublicKey publicKey, PrivateKey privateKey, String issuerName, String subjectName, BigInteger serialNumber,
			String subjectAlternativeNameIp, Date notAfter) throws Exception {
		//
		// signers name
		//
		X500Name x500IssuerName = new X500Name(issuerName);
		//
		// subjects name - the same as we are self signed.
		//
		X500Name x500SubjectName = new X500Name(subjectName);
		//
		// serial
		//
		if (serialNumber == null) {
			serialNumber = BigInteger.valueOf(RandomUtils.getRandomLong());
		}
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, -65); // 65 minutes Tolerance;
		Date notBefore = calendar.getTime();

		if (notAfter == null) {
			calendar.add(Calendar.YEAR, 100);
			notAfter = calendar.getTime();
		}
		//
		// create the certificate - version 3
		//
		X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(x500IssuerName, serialNumber, notBefore, notAfter, x500SubjectName, publicKey);
		// builder.addExtension(Extension.subjectKeyIdentifier, false,
		// SubjectPublicKeyInfo.getInstance(publicKey.getEncoded()));
		builder.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));

		KeyUsage usage = new KeyUsage(
				KeyUsage.keyCertSign | KeyUsage.digitalSignature | KeyUsage.keyEncipherment | KeyUsage.dataEncipherment | KeyUsage.cRLSign);
		builder.addExtension(Extension.keyUsage, false, usage);

		ASN1EncodableVector purposes = new ASN1EncodableVector();
		purposes.add(KeyPurposeId.id_kp_serverAuth);
		purposes.add(KeyPurposeId.id_kp_clientAuth);
		purposes.add(KeyPurposeId.anyExtendedKeyUsage);

		List<ASN1Encodable> subjectAlternativeNames = null;

		if (subjectAlternativeNameIp != null) {
			if (IPAddress.isValidIPv6WithNetmask(subjectAlternativeNameIp) || IPAddress.isValidIPv6(subjectAlternativeNameIp)
					|| IPAddress.isValidIPv4WithNetmask(subjectAlternativeNameIp) || IPAddress.isValidIPv4(subjectAlternativeNameIp)) {
				subjectAlternativeNames = new ArrayList<ASN1Encodable>(1);
				subjectAlternativeNames.add(new GeneralName(GeneralName.iPAddress, subjectAlternativeNameIp));
			}

		}
		if (subjectAlternativeNames != null) {
			DERSequence subjectAlternativeNamesExtension = new DERSequence(subjectAlternativeNames.toArray(new ASN1Encodable[subjectAlternativeNames.size()]));
			builder.addExtension(Extension.subjectAlternativeName, false, subjectAlternativeNamesExtension);
		}

		builder.addExtension(Extension.extendedKeyUsage, false, new DERSequence(purposes));

		X509Certificate certificate = signCertificate(builder, privateKey);
		return certificate;
	}

	// private static SubjectKeyIdentifier createSubjectKeyIdentifier(Key key)
	// throws IOException {
	// ASN1InputStream is = null;
	// try {
	//
	// SubjectPublicKeyInfo info =
	// SubjectPublicKeyInfo.getInstance(key.getEncoded());
	// return new BcX509ExtensionUtils().createSubjectKeyIdentifier(info);
	// } finally {
	// is.close();
	// }
	// }

	// public static byte[] sign(PrivateKey privateKey, String text) throws
	// Exception {
	// Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, "BC");
	// signature.initSign(privateKey);
	// signature.update(text.getBytes("UTF-8"));
	// return signature.sign();
	// }

	public static byte[] sign(PrivateKey privateKey, byte[] data) throws Exception {
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(privateKey);
		signature.update(data);
		return signature.sign();
	}

	public static byte[] sign(PrivateKey privateKey, int tempalteId, String data, String actionId, String data2) throws Exception {
		Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
		signature.initSign(privateKey);
		signature.update(KaraUtils.intToByteArray(tempalteId));
		if (data != null) {
			signature.update(data.getBytes(StandardCharsets.UTF_8));
		}
		if (actionId != null) {
			signature.update(actionId.getBytes(StandardCharsets.UTF_8));
		}
		if (data2 != null) {
			signature.update(data2.getBytes(StandardCharsets.UTF_8));
		}
		return signature.sign();
	}

	public static PKCS10CertificationRequest createCsr(KeyPair keyPair, String princialName) throws OperatorCreationException {
		PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(new X500Principal(princialName), keyPair.getPublic());
		JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM);
		ContentSigner signer = csBuilder.build(keyPair.getPrivate());
		return p10Builder.build(signer);
	}

	// private static byte[] hexify(String string) {
	// return DatatypeConverter.parseHexBinary(string);
	// }

	// public static byte[] generatePKCS10(String cn, KeyPair keyPair) throws
	// NoSuchAlgorithmException,
	// InvalidKeyException, SignatureException, IOException,
	// CertificateException, OperatorCreationException {
	// X500Principal x500Principal = new X500Principal("cn=" + cn);
	//
	// PKCS10CertificationRequestBuilder p10Builder = new
	// JcaPKCS10CertificationRequestBuilder(x500Principal,
	// keyPair.getPublic());
	// JcaContentSignerBuilder csBuilder = new
	// JcaContentSignerBuilder(SIGNATURE_ALGORITHM);
	// ContentSigner signer = csBuilder.build(keyPair.getPrivate());
	// PKCS10CertificationRequest csr = p10Builder.build(signer);
	// return csr.getEncoded();
	// }
	//
	// public static byte[] sign(PrivateKey privateKey, String text) throws
	// Exception {
	// Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM, "BC");
	// signature.initSign(privateKey);
	// signature.update(text.getBytes("UTF-8"));
	// return signature.sign();
	// }

	/**
	 * @param keyLength
	 * @param cn
	 * @param certChain
	 * @param ipAddress
	 * @param password
	 * @param caPrivatKey
	 * @param alias
	 * @return
	 * @throws Exception
	 */
	public static KeyStore createKeyStore(int keyLength, String cn, Certificate[] inheritCertChain, String ipAddress, char[] password, PrivateKey caPrivatKey,
			String alias, Date notAfter) throws Exception {
		KeyPair keyPair = SecureUtils.generateKeyPair(keyLength);

		X509Certificate issuerCert = null;
		String issuerCn;

		Certificate[] certChain;

		if (caPrivatKey != null) {
			if ((inheritCertChain == null || inheritCertChain.length == 0)) {
				throw new Exception("The issuer Certificate is missing");
			}
			issuerCert = (X509Certificate) inheritCertChain[inheritCertChain.length - 1];
			issuerCn = issuerCert.getSubjectX500Principal().getName();
		} else {
			// Self sign certificate
			caPrivatKey = keyPair.getPrivate(); // selfsign
			issuerCn = cn;
		}

		X509Certificate cert = createCertificate(keyPair.getPublic(), caPrivatKey, issuerCn, cn, BigInteger.valueOf(RandomUtils.getRandomLong()), ipAddress,
				notAfter);
		if (inheritCertChain == null) {
			certChain = new Certificate[1];
			certChain[0] = cert;
		} else {
			certChain = new Certificate[inheritCertChain.length + 1];
			int i = 0;
			certChain[i++] = cert;
			for (; i < certChain.length; i++) {
				certChain[i] = inheritCertChain[i - 1];
			}
		}

		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		keyStore.load(null, null);
		// KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		// keyGen.init(128);
		// Key key = keyGen.generateKey();
		keyStore.setKeyEntry(alias, keyPair.getPrivate(), password, certChain);
		return keyStore;
	}

	// static KeyStore createKeyStore(String name, PrivateKey pv,
	// X509Certificate[] certChain, String password)
	// throws Exception {
	// KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
	// keyStore.load(null);
	// String alias = certChain[certChain.length -
	// 1].getSubjectX500Principal().getName();
	// keyStore.setKeyEntry(SETUP_KEYSTORE_ALIAS, pv, password.toCharArray(),
	// certChain);
	//
	// File file = new File(LocalPaths.getCertsDirectory(), name);
	// keyStore.store(new FileOutputStream(file), password.toCharArray());
	// return;
	// }

	// public static boolean verifySignature(byte[] serverPublicKey, String
	// oneTimePassword) throws
	// NoSuchAlgorithmException, NoSuchProviderException,
	// InvalidKeySpecException, Exception {
	// return verifySignature (loadPublicKey(serverPublicKey), oneTimePassword);
	// }

	public static boolean removeCryptographyRestrictions() throws Exception {
		if (isRestrictedCryptography() == false) {
			return false;
		}

		/*
		 * Do the following, but with reflection to bypass access checks:
		 *
		 * JceSecurity.isRestricted = false;
		 * JceSecurity.defaultPolicy.perms.clear();
		 * JceSecurity.defaultPolicy.add(CryptoAllPermission.INSTANCE);
		 */
		final Class<?> jceSecurity = Class.forName("javax.crypto.JceSecurity");
		final Class<?> cryptoPermissions = Class.forName("javax.crypto.CryptoPermissions");
		final Class<?> cryptoAllPermission = Class.forName("javax.crypto.CryptoAllPermission");

		final Field isRestrictedField = jceSecurity.getDeclaredField("isRestricted");
		isRestrictedField.setAccessible(true);
		final Field modifiersField = Field.class.getDeclaredField("modifiers");
		modifiersField.setAccessible(true);
		modifiersField.setInt(isRestrictedField, isRestrictedField.getModifiers() & ~Modifier.FINAL);
		isRestrictedField.set(null, false);

		final Field defaultPolicyField = jceSecurity.getDeclaredField("defaultPolicy");
		defaultPolicyField.setAccessible(true);
		final PermissionCollection defaultPolicy = (PermissionCollection) defaultPolicyField.get(null);

		final Field perms = cryptoPermissions.getDeclaredField("perms");
		perms.setAccessible(true);
		((Map<?, ?>) perms.get(defaultPolicy)).clear();

		final Field instance = cryptoAllPermission.getDeclaredField("INSTANCE");
		instance.setAccessible(true);
		defaultPolicy.add((Permission) instance.get(null));
		return true;

	}

	private static boolean isRestrictedCryptography() {
		// This simply matches the Oracle JRE, but not OpenJDK.
		return "Java(TM) SE Runtime Environment".equals(System.getProperty("java.runtime.name"));
	}

	public static byte[] serializeKeyStore(KeyStore keyStore, String password)
			throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		keyStore.store(outputStream, password.toCharArray());
		byte[] value = outputStream.toByteArray();
		outputStream.close();
		return value;
	}

	/**
	 * @param keyStoreContent
	 * @param password
	 * @param alias
	 * @return
	 * @throws Exception
	 */
	public static byte[] convertPk12ToPem(byte[] keyStoreContent, String password, String alias) throws Exception {
		KeyStore keyStore = KeyStore.getInstance("PKCS12");
		ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(keyStoreContent);
		keyStore.load(arrayInputStream, password.toCharArray());
		arrayInputStream.close();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PemWriter pemWriter = new PemWriter(new OutputStreamWriter(bos));

		Certificate[] chain = keyStore.getCertificateChain(alias);

		for (Certificate cert : chain) {
			pemWriter.append("\n### ");
			pemWriter.append(((X509Certificate) cert).getSubjectDN().getName());
			pemWriter.append("\n");
			pemWriter.append("### Issuer: ");
			pemWriter.append(((X509Certificate) cert).getIssuerDN().getName());
			pemWriter.append("\n");
			pemWriter.writeObject(new PemObject("CERTIFICATE", cert.getEncoded()));
		}
		pemWriter.close();
		return bos.toByteArray();
	}
	
	
	public static ByteArrayOutputStream convertChainToPem(X509Certificate [] chain) throws Exception {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PemWriter pemWriter = new PemWriter(new OutputStreamWriter(bos));
		
		for (Certificate cert : chain) {
			pemWriter.append("\n### Subject: ");
			pemWriter.append(((X509Certificate) cert).getSubjectDN().getName());
			pemWriter.append("\n");
			pemWriter.append("### Issuer: ");
			pemWriter.append(((X509Certificate) cert).getIssuerDN().getName());
			pemWriter.append("\n");
			pemWriter.writeObject(new PemObject("CERTIFICATE", cert.getEncoded()));
		}
		pemWriter.close();
		return bos;
	}

	/**
	 * @param keyStoreContent
	 * @param password
	 * @param alias
	 * @return
	 * @throws Exception
	 */
	public static KeyStore convertPemToTrustStore(byte[] content) throws Exception {
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, null);
		ByteArrayInputStream bis = new ByteArrayInputStream(content);
		PEMParser pemParser = new PEMParser(new InputStreamReader(bis));

		while (true) {
			X509CertificateHolder certificateHolder = (X509CertificateHolder) pemParser.readObject();
			if (certificateHolder != null) {
				X509Certificate cert = new JcaX509CertificateConverter().setProvider(BouncyCastleProvider.PROVIDER_NAME).getCertificate(certificateHolder);
				ks.setCertificateEntry(cert.getSubjectDN().getName(), (Certificate) cert);
			} else {
				pemParser.close();
				break;
			}

		}
		return ks;
	}
	
	

	// public byte[] createMacDigestCommon(byte[] data, int offset, int length)
	// throws InvalidKeyException, NoSuchAlgorithmException {
	// return createMacDigest(SecureUtilsApi.commonKey, data, offset, length);
	// }

	public static byte[] decryptData(byte[] key, byte[] encryptedData) throws Exception {
		Cipher decryptionCipher = Cipher.getInstance(SecureUtils.KEY_ALGORITHM);
		decryptionCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), algorithmParameterSpec);
		return decryptionCipher.doFinal(encryptedData);
	}

	public static byte[] encryptDataCommon(byte[] data) throws Exception {
		return encryptData(commonKey, data);
	}

	public static byte[] encryptDataCommonSalt(byte[] data) throws Exception {
		return encryptDataSalt(commonKey, data);
	}

	public static byte[] decryptDataCommon(byte[] data) throws Exception {
		return decryptData(commonKey, data);
	}

	public static byte[] decryptDataCommonSalt(byte[] data) throws Exception {
		return decryptDataSalt(commonKey, data);
	}

	public static byte[] encryptData(byte[] key, byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance(SecureUtils.KEY_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), algorithmParameterSpec);
		return cipher.doFinal(data);
	}

	public static byte[] encryptDataSalt(byte[] key, byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance(SecureUtils.KEY_ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), algorithmParameterSpec);
		cipher.update(RandomUtils.getRandom(SALT_LENGTH)); // SEED
		return cipher.doFinal(data);
	}

	public static byte[] decryptDataSalt(byte[] key, byte[] encryptedData) throws Exception {
		Cipher decryptionCipher = Cipher.getInstance(SecureUtils.KEY_ALGORITHM);
		decryptionCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), algorithmParameterSpec);
		byte[] decrpted = decryptionCipher.doFinal(encryptedData);
		byte[] result = new byte[decrpted.length - SALT_LENGTH];
		System.arraycopy(decrpted, SALT_LENGTH, result, 0, result.length);
		return result;
	}

	public byte[] verifyMacDigest(byte[] key, byte[] data, byte[] digest) throws NoSuchAlgorithmException, InvalidKeyException {
		SecretKey macKey = new SecretKeySpec(key, "HmacSHA256");
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(macKey);

		return mac.doFinal(data);
	}

	public static byte[] createMacDigest(byte[] key, byte[] data, int offset, int length, String algorithm)
			throws NoSuchAlgorithmException, InvalidKeyException {
		SecretKey macKey = new SecretKeySpec(key, algorithm);
		Mac mac = Mac.getInstance(algorithm);
		mac.init(macKey);
		mac.update(data, offset, length);
		return mac.doFinal();
	}

	public static byte[] createMacDigest(byte[] key, byte[] data, int offset, int length) throws NoSuchAlgorithmException, InvalidKeyException {
		return createMacDigest(key, data, offset, length, "HmacSHA256");
	}

	public static byte[] createMacSha1(byte[] data) throws NoSuchAlgorithmException, InvalidKeyException {
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(macKeySha1);
		mac.update(data);
		return mac.doFinal();
	}

	public static X509Certificate getCertificateRefactorAlias(KeyStore keyStore, KeyStorePurpose purpose, String password) throws Exception {
		X509Certificate certificate = (X509Certificate) keyStore.getCertificate(purpose.name());
		if (certificate == null) {
			Enumeration<String> es = keyStore.aliases();
			String alias = "";
			while (es.hasMoreElements()) {
				alias = (String) es.nextElement();
				// if alias refers to a private key break at that point
				// as we want to use that certificate
				if (keyStore.isKeyEntry(alias) == true) {
					Certificate[] certificates = keyStore.getCertificateChain(alias);
					Key privateKey = keyStore.getKey(alias, password.toCharArray());
					keyStore.setKeyEntry(purpose.name(), privateKey, password.toCharArray(), certificates);
					keyStore.deleteEntry(alias);
					return (X509Certificate) keyStore.getCertificate(purpose.name());
				}
			}
			throw new Exception("No Key-Entry found in Key-Store");
		}
		return certificate;
	}

	public static Cipher getCipherFromPasswordSalt(char[] password, boolean decryptMode, byte[] salt) throws DcemException {
		try {
			// Use a KeyFactory to derive the corresponding key from the passphrase:
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(password, salt, 10000, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
			
			Cipher cipher = Cipher.getInstance(DcemConstants.DB_KEY_ALG_MODE);
			if (decryptMode) {
				cipher.init(Cipher.DECRYPT_MODE, secret, algorithmParameterSpec);
			} else {
				cipher.init(Cipher.ENCRYPT_MODE, secret, algorithmParameterSpec);
			}
			return cipher;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.CANNOT_EN_DE_CRYPT_PASS, "", e);
		}
	}
	
	public static InputStream getBufferCipherInputStream (char[] password, boolean decryptMode, byte[] salt, InputStream inputStream, boolean gcm) throws DcemException {
		if (gcm) {
			GCMBlockCipher gcmBlockCipher = SecureServerUtils.getGcmBufferCipherFromPasswordSalt(password, decryptMode, salt);
			return new CipherInputStream(inputStream, gcmBlockCipher, MAX_CIPHER_BUFFER);
		} else {
			BufferedBlockCipher BufferedBlockCipher = SecureServerUtils.getBufferCipherFromPasswordSalt(password, decryptMode, salt);
			return new CipherInputStream(inputStream, BufferedBlockCipher, MAX_CIPHER_BUFFER);
		}
	}

	
	private static BufferedBlockCipher getBufferCipherFromPasswordSalt(char[] password, boolean decryptMode, byte[] salt) throws DcemException {
		try {
			// Use a KeyFactory to derive the corresponding key from the passphrase:
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(password, salt, 10000, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
									
			PaddedBufferedBlockCipher bufferedBlockCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
			CipherParameters ivAndKey = new ParametersWithIV(new KeyParameter(secret.getEncoded()), ENCRYPTION_ALGORITHM_IV);
			bufferedBlockCipher.init(decryptMode == false, ivAndKey);			
			return bufferedBlockCipher;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.CANNOT_EN_DE_CRYPT_PASS, "", e);
		}
	}
	
	private static GCMBlockCipher getGcmBufferCipherFromPasswordSalt(char[] password, boolean decryptMode, byte[] salt) throws DcemException {
		try {
			// Use a KeyFactory to derive the corresponding key from the passphrase:
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(password, salt, 100, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
			GCMBlockCipher gcmBlockCipher = new GCMBlockCipher (new AESEngine());
			CipherParameters parameters = new AEADParameters(new KeyParameter(secret.getEncoded()), 128, salt);
			gcmBlockCipher.init (decryptMode == false, parameters);
			return gcmBlockCipher;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.CANNOT_EN_DE_CRYPT_PASS, "", e);
		}
	}


	/**
	 * Class used to add the server's certificate to the KeyStore
	 * with your trusted certificates.
	 */
	public static X509Certificate[] getCertificates(String host, int port, String proxyHost, int proxyPort) throws Exception {

		InetSocketAddress proxyAddr = null;
		Socket underlying = null;
		if (port == -1) {
			port = 443;
		}
		if (proxyHost != null && proxyHost.isEmpty() == false) {
			proxyAddr = new InetSocketAddress(proxyHost, proxyPort);
			underlying = new Socket(new Proxy(Proxy.Type.HTTP, proxyAddr));
		}
		SSLContext context = SSLContext.getInstance("TLS");
		// TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
		// tmf.init(ks);
		// X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
		DcemTrustManager tm = new DcemTrustManager(true, true);
		context.init(null, new TrustManager[] { tm }, null);
		SSLSocketFactory factory = context.getSocketFactory();

		// System.out.println("Opening connection to " + host + ":" + port + (useProxy ? (" via proxy "+proxyHost+":"+proxyPort) : "") + " ...");
		SSLSocket socket;
		if (proxyAddr != null) {
			underlying.connect(new InetSocketAddress(host, port));
			socket = (SSLSocket) factory.createSocket(underlying, host, port, true);
		} else {
			socket = (SSLSocket) factory.createSocket(host, port);
		}
		socket.setSoTimeout(5000);
		try {
			socket.startHandshake();
			socket.close();
		} catch (SSLException e) {
			throw e;
		}
		return tm.getServerChainCertificates();
	}

	/**
	 * @param chain
	 * @param passphrase
	 */
	public static void storeCertificateChain(X509Certificate[] chain, char[] passphrase) throws Exception {
		if (chain == null) {
			return;
		}
		if (passphrase == null) {
			passphrase = "changeit".toCharArray();
		}
		File file = new File("jssecacerts");
		if (file.isFile() == false) {
			char SEP = File.separatorChar;
			File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
			file = new File(dir, "jssecacerts");
			if (file.isFile() == false) {
				file = new File(dir, "cacerts");
			}
		}
		InputStream in = new FileInputStream(file);
		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(in, passphrase);
		in.close();

		System.out.println("Server sent " + chain.length + " certificate(s):");
		for (int i = 0; i < chain.length; i++) {
			X509Certificate cert = chain[i];
			String cn = cert.getSubjectDN().getName();
			System.out.println(" " + (i + 1) + " Subject " + cn);
			System.out.println("   Issuer  " + cert.getIssuerDN());
			System.out.println();
			String alias = cn + "-" + (i + 1);
			ks.setCertificateEntry(alias, cert);
		}

		OutputStream out = new FileOutputStream("jssecacerts");
		ks.store(out, passphrase);
		out.close();
	}

}
