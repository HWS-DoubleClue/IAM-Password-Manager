package com.doubleclue.dcem.otp.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.security.spec.AlgorithmParameterSpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.otp.entities.OtpTokenEntity;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.SecureUtils;

@Named("otpLogic")
@ApplicationScoped
public class OtpLogic {

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	OtpModule otpModule;

	public List<OtpTokenEntity> parseTokens(OtpTypes otpTypes, byte[] contents, String encryptionKey)
			throws DcemException {
		byte[] key = Base64.getDecoder().decode(encryptionKey);
		AlgorithmParameterSpec spec = new IvParameterSpec(SecureServerUtils.ENCRYPTION_ALGORITHM_IV);
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(SecureUtils.KEY_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), spec);
		} catch (Exception e1) {
			throw new DcemException(DcemErrorCodes.INVALID_OTP_TOKEN_FILE, "Cannot create cipher", e1);
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(new String(contents, DcemConstants.CHARSET_ISO_8859_1)));
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.INVALID_OTP_TOKEN_FILE, "Couldn't read the file", e);
		}
		String line;
		List<OtpTokenEntity> list = new ArrayList<>();
		String serialNo;
		byte[] secretKey;
		int ind;
		int lineCounter = 0;
		try {
			String signature = reader.readLine(); // not being checked for the moment
			while ((line = reader.readLine()) != null) {
				lineCounter++;
				ind = KaraUtils.nextWhiteSpace(line);
				if (ind == -1) {
					throw new DcemException(DcemErrorCodes.INVALID_OTP_TOKEN_FILE,
							"No serial number found in line " + lineCounter);
				}
				serialNo = line.substring(0, ind);
				try {
					byte[] seed = Base64.getDecoder().decode(line.substring(ind + 1));
					secretKey = cipher.doFinal(seed);
//					secretKey = StringUtils.hexStringToBinary(new String(seed, DcemConstants.CHARSET_UTF8));

					switch (otpTypes) {
					case TIME_6_SHA1_30:
					case TIME_8_SHA1_30:
					case TIME_6_SHA1_60:
					case TIME_8_SHA1_60:
						if (secretKey.length != 20) {
							throw new DcemException(DcemErrorCodes.INVALID_OTP_TOKEN_FILE,
									"Invalid key length " + lineCounter);
						}
						break;
					default:
						if (secretKey.length != 32) {
							throw new DcemException(DcemErrorCodes.INVALID_OTP_TOKEN_FILE,
									"Invalid key length " + lineCounter);
						}
						break;
					}
				} catch (Exception e) {
					throw new DcemException(DcemErrorCodes.INVALID_OTP_TOKEN_FILE,
							"Couldn't convert key. Line: " + lineCounter);
				}
				list.add(new OtpTokenEntity(otpTypes, serialNo, secretKey));
			}
		} catch (IOException e) {
			throw new DcemException(DcemErrorCodes.INVALID_OTP_TOKEN_FILE, "IO Exception", e);
		}
		return list;
	}

	/**
	 * @param serialNo
	 * @return
	 */
	public OtpTokenEntity getOtpTokenBySerailNo(String serialNo) {
		TypedQuery<OtpTokenEntity> query = em.createNamedQuery(OtpTokenEntity.GET_TOKEN_BY_SERIAL_NO,
				OtpTokenEntity.class);
		query.setParameter(1, serialNo);
		try {
			return query.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	@DcemTransactional
	public int addTokens(List<OtpTokenEntity> tokens, DcemAction dcemAction) {
		int counter = 0;
		for (OtpTokenEntity entity : tokens) {
			if (getOtpTokenBySerailNo(entity.getSerialNumber()) == null) {
				em.persist(entity);
				counter++;
			}
		}
		auditingLogic.addAudit(dcemAction,
				tokens.get(0).getSerialNumber() + '-' + tokens.get(tokens.size() - 1).getSerialNumber());
		return counter;
	}

	@DcemTransactional
	public void editToken(OtpTokenEntity otpTokenEntity, DcemAction dcemAction) {
		em.merge(otpTokenEntity);
		auditingLogic.addAudit(dcemAction, otpTokenEntity.getSerialNumber());
	}

	public List<OtpTokenEntity> getAllUserTokens(DcemUser dcemUser) {
		TypedQuery<OtpTokenEntity> query = em.createNamedQuery(OtpTokenEntity.GET_ALL_USER_TOKENS,
				OtpTokenEntity.class);
		query.setParameter(1, dcemUser);
		return query.getResultList();
	}
	
	public List<OtpTokenEntity> getUserEnabledTokens(DcemUser dcemUser) {
		TypedQuery<OtpTokenEntity> query = em.createNamedQuery(OtpTokenEntity.GET_USER_TOKENS,
				OtpTokenEntity.class);
		query.setParameter(1, dcemUser);
		return query.getResultList();
	}

	public List<OtpTokenEntity> getUserDisabledToken(DcemUser dcemUser) {
		TypedQuery<OtpTokenEntity> query = em.createNamedQuery(OtpTokenEntity.GET_DISABLED_USER_TOKENS,
				OtpTokenEntity.class);
		query.setParameter(1, dcemUser);
		return query.getResultList();
	}

	@DcemTransactional
	public void unassignUser(DcemUser dcemUser) {
		List<OtpTokenEntity> list = getAllUserTokens(dcemUser);
		for (OtpTokenEntity entity : list) {
			entity.setUser(null);
		}
	}
}
