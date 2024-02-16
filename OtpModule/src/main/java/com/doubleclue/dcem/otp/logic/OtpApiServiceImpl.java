package com.doubleclue.dcem.otp.logic;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.as.comm.AppServices;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.JpaSelectProducer;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.logic.module.AsApiOtpToken;
import com.doubleclue.dcem.core.logic.module.AsApiOtpType;
import com.doubleclue.dcem.core.logic.module.OtpModuleApi;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.otp.entities.OtpTokenEntity;
import com.doubleclue.dcem.subjects.AsActivationSubject;

@Named("otpApiServiceImpl")
@ApplicationScoped
public class OtpApiServiceImpl implements OtpModuleApi {

	private static Logger logger = LogManager.getLogger(OtpApiServiceImpl.class);

	@Inject
	AppServices appServices;

	@Inject
	OperatorSessionBean sessionBean;

	@Inject
	EntityManager entityManager;

	@Inject
	UserLogic userLogic;

	@Inject
	JpaLogic jpaLogic;

	@Inject
	AsActivationSubject activationSubject;

	@Inject
	AsModule asModule;

	@Inject
	AdminModule adminModule;

	@Override
	@DcemTransactional
	public void modifyOtpToken(AsApiOtpToken asApiOtpToken, String passcode) throws DcemException {
		OtpTokenEntity entity = otpLogic.getOtpTokenBySerailNo(asApiOtpToken.getSerialNumber());
		DcemUser dcemUser;
		if (entity == null) {
			throw new DcemException(DcemErrorCodes.INVALID_OTP_SERIAL_NO, asApiOtpToken.getSerialNumber());
		}
		entity.setDisabled(asApiOtpToken.isDisabled());
		entity.setInfo(asApiOtpToken.getInfo());
		if (asApiOtpToken.getAssignedTo() == null) {
			entity.setUser(null);
		} else {
			dcemUser = userLogic.getUser(asApiOtpToken.getAssignedTo());
			if (dcemUser == null) {
				throw new DcemException(DcemErrorCodes.INVALID_USERID, asApiOtpToken.getAssignedTo());
			}
			if (entity.getUser() != null) {
				if (entity.getUser() != dcemUser) {
					throw new DcemException(DcemErrorCodes.TOKEN_BELONGS_TO_SOMEONE_ELSE, asApiOtpToken.getAssignedTo());
				} else if (asApiOtpToken.getOtpId() == 0) {
					throw new DcemException(DcemErrorCodes.TOKEN_ALREADY_ASSIGNED, asApiOtpToken.getAssignedTo());
				}
			} else {
				if (passcode == null) {
					throw new DcemException(DcemErrorCodes.INVALID_OTP, asApiOtpToken.getAssignedTo());
				}
				int inPasscode = 0;
				try {
					inPasscode = Integer.parseInt(passcode);
				} catch (Exception e) {
					throw new DcemException(DcemErrorCodes.PASSCODE_NOT_NUMERIC, null);
				}
				if (generatePassCode(entity.getSecretKey(), entity.getOtpType(), inPasscode, passcode.length()) == false) {
					throw new DcemException(DcemErrorCodes.INVALID_OTP, asApiOtpToken.getAssignedTo());
				}
			}
			entity.setUser(dcemUser);
		}

	}

	@Override
	public Response queryOtpTokens(List<ApiFilterItem> filters, Integer offset, Integer maxResults, SecurityContext securityContext) {

		try {
			List<AsApiOtpToken> tokens = queryOtpTokenEntities(filters, offset, maxResults, false);
			return Response.ok().entity(tokens).build();
		} catch (DcemException exp) {
			logger.info(exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
	}

	@Inject
	OtpLogic otpLogic;

	@Inject
	OtpModule otpModule;

	public static final String TOTP_ALGORITHM_HMAC_SHA1 = "HmacSHA1";
	public static final String TOTP_ALGORITHM_HMAC_SHA256 = "HmacSHA256";

	@Override
	public String verifyOtpPasscode(DcemUser user, String passcode) throws DcemException {
		List<OtpTokenEntity> tokens = otpLogic.getUserEnabledTokens(user);
		if (tokens.size() == 0) {
			// otpLogic.writeOtpReport(user, null, true,
			// DcemErrorCodes.USER_HAS_NO_OTP_TOKENS.name());
			throw new DcemException(DcemErrorCodes.USER_HAS_NO_OTP_TOKENS, null);
		}
		int inPasscode = 0;
		try {
			inPasscode = Integer.parseInt(passcode);
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.PASSCODE_NOT_NUMERIC, null);
		}
		try {
			OtpTokenEntity foundToken = null;
			for (OtpTokenEntity otpTokenEntity : tokens) {

				if (generatePassCode(otpTokenEntity.getSecretKey(), otpTokenEntity.getOtpType(), inPasscode, passcode.length()) == true) {
					foundToken = otpTokenEntity;
					break;
				}
			}
			if (foundToken == null) {
				throw new DcemException(DcemErrorCodes.INVALID_OTP, null);
			}
			logger.debug("Hardware OTP successfull. " + user.getLoginId() + ", " + foundToken.getSerialNumber());
			return foundToken.getSerialNumber();
		} catch (DcemException exp) {
			logger.info("Hardware OTP Failed. " + user.getLoginId());
			throw exp;
		}
	}

	/**
	 * @param key
	 * @param otpTypes
	 * @param passcode
	 * @return
	 * @throws DcemException
	 */
	boolean generatePassCode(byte[] key, OtpTypes otpTypes, int inPasscode, int length) throws DcemException {

		long passcodeValidFor = 0;
		int passwordLength = 0;
		int modDivisor = 0;
		String algorithm = null;

		switch (otpTypes) {
		case TIME_6_SHA1_30:
			passcodeValidFor = 30 * 1000;
			passwordLength = 6;
			modDivisor = 1_000_000;
			algorithm = TOTP_ALGORITHM_HMAC_SHA1;
			break;
		case TIME_6_SHA1_60:
			passcodeValidFor = 60 * 1000;
			passwordLength = 6;
			modDivisor = 1_000_000;
			algorithm = TOTP_ALGORITHM_HMAC_SHA1;
			break;
		case TIME_6_SHA2_30:
			passcodeValidFor = 30 * 1000;
			passwordLength = 6;
			modDivisor = 1_000_000;
			algorithm = TOTP_ALGORITHM_HMAC_SHA256;
			break;
		case TIME_6_SHA2_60:
			passcodeValidFor = 60 * 1000;
			passwordLength = 6;
			modDivisor = 1_000_000;
			algorithm = TOTP_ALGORITHM_HMAC_SHA256;
			break;
		case TIME_8_SHA1_30:
			passcodeValidFor = 30 * 1000;
			passwordLength = 8;
			modDivisor = 1_000_000;
			algorithm = TOTP_ALGORITHM_HMAC_SHA1;
			break;
		case TIME_8_SHA1_60:
			passcodeValidFor = 60 * 1000;
			passwordLength = 8;
			modDivisor = 1_000_000;
			algorithm = TOTP_ALGORITHM_HMAC_SHA1;
			break;
		case TIME_8_SHA2_30:
			passcodeValidFor = 30 * 1000;
			passwordLength = 8;
			modDivisor = 1_000_000;
			algorithm = TOTP_ALGORITHM_HMAC_SHA256;
			break;
		case TIME_8_SHA2_60:
			passcodeValidFor = 60 * 1000;
			passwordLength = 8;
			modDivisor = 1_000_000;
			algorithm = TOTP_ALGORITHM_HMAC_SHA256;
			break;
		default:
			break;
		}
		if (length != passwordLength) {
			return false;
		}

		Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));
		long counter = calendar.getTimeInMillis() / passcodeValidFor;

		int window = otpModule.getPreferences().getDelayWindow();
		ByteBuffer buffer;
		for (int i = window; i >= 0; --i) {
			buffer = ByteBuffer.allocate(8);
			buffer.putLong(0, counter - i);
			int candidate = generate(key, buffer, algorithm, modDivisor);
			if (candidate == inPasscode) {
				return true;
			}
		}
		counter++;
		for (int ind = 0; ind < window; ind++) {
			buffer = ByteBuffer.allocate(8);
			buffer.putLong(0, counter + ind);
			int candidate = generate(key, buffer, algorithm, modDivisor);
			if (candidate == inPasscode) {
				return true;
			}
		}
		return false;
	}

	int generate(byte[] key, ByteBuffer buffer, String algorithm, int modDivisor) throws DcemException {
		byte[] hmac;
		try {
			hmac = SecureServerUtils.createMacDigest(key, buffer.array(), 0, buffer.array().length, algorithm);
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.INVALID_OTP, "cannot create hmac", e);
		}

		final int offset = hmac[hmac.length - 1] & 0x0f;
		for (int i = 0; i < 4; i++) {
			// Note that we're re-using the first four bytes of the buffer here; we just
			// ignore the latter four from
			// here on out.
			buffer.put(i, hmac[i + offset]);
		}
		int hotp = buffer.getInt(0) & 0x7fffffff;
		return hotp % modDivisor;
	}

	@Override
	public List<AsApiOtpToken> queryOtpTokenEntities(List<ApiFilterItem> filters, Integer offset, Integer maxResults, boolean includeSecretKey)
			throws DcemException {
		JpaSelectProducer<OtpTokenEntity> jpaSelectProducer = new JpaSelectProducer<OtpTokenEntity>(entityManager, OtpTokenEntity.class);
		int firstResult = 0;
		if (offset != null) {
			firstResult = offset.intValue();
		}
		int page = DcemConstants.MAX_DB_RESULTS;
		if (maxResults != null && maxResults.intValue() < page) {
			page = maxResults.intValue();
		}
		List<OtpTokenEntity> otpTokenEntities = jpaSelectProducer.selectCriteriaQueryFilters(filters, firstResult, page, null);
		List<AsApiOtpToken> tokens = new LinkedList<>();
		for (OtpTokenEntity entity : otpTokenEntities) {
			AsApiOtpToken asApiOtpToken = new AsApiOtpToken();
			try {
				asApiOtpToken.setOtpId(entity.getId());
				asApiOtpToken.setAssignedTo((entity.getUser() == null) ? null : entity.getUser().getLoginId());
				asApiOtpToken.setDisabled(entity.isDisabled());
				asApiOtpToken.setInfo(entity.getInfo());
				asApiOtpToken.setSerialNumber(entity.getSerialNumber());
				asApiOtpToken.setOtpType(AsApiOtpType.fromValue(entity.getOtpType().name()));
				if (includeSecretKey) {
					asApiOtpToken.setSecretKey(entity.getSecretKey());
				}
			} catch (Exception e) {
				throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString(), e);
			}
			tokens.add(asApiOtpToken);
		}
		return tokens;
	}

	@Override
	public int getDelayWindow() {
		return otpModule.getPreferences().getDelayWindow();
	}
}
