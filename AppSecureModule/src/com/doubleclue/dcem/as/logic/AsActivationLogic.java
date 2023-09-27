package com.doubleclue.dcem.as.logic;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.SendByEnum;
import com.doubleclue.dcem.as.entities.ActivationCodeEntity;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.DbResourceBundle;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.subjects.AsActivationSubject;
import com.doubleclue.dcem.system.send.MessageBird;
import com.doubleclue.dcem.system.send.SendEmail;
import com.doubleclue.utils.ActivationParameters;
import com.doubleclue.utils.RandomUtils;
import com.doubleclue.utils.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@ApplicationScoped
@Named("asActivtionLogic")
public class AsActivationLogic {

	@Inject
	EntityManager em;

	@Inject
	UserLogic userLogic;

	@Inject
	AsVersionLogic versionLogic;

	@Inject
	AsDeviceLogic deviceLogic;

	@Inject
	AsModule asModule;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	TemplateLogic templateLogic;

	@Inject
	MessageBird messageBird;

	@Inject
	DomainLogic domainLogic;

	@Inject
	AdminModule adminModule;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	AsActivationSubject asActivationSubject;

	@Inject
	DcemApplicationBean dcemApplicationBean;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.doubleclue.dcem.as.logic.AsActicatioLogicApi#addUpdateActivationCode(
	 * com.doubleclue.dcem.as.entities.AsActivationCode,
	 * com.doubleclue.dcem.core.entities.DcemAction,
	 * com.doubleclue.dcem.admin.logic.SendByEnum, boolean)
	 */

	@DcemTransactional
	public ActivationParameters addUpdateActivationCode(ActivationCodeEntity asActivationCode, DcemAction dcemAction, SendByEnum sendBy, boolean withAuditing)
			throws DcemException {
		return addUpdateActivationCode(asActivationCode, dcemAction, sendBy, withAuditing, null);
	}

	@DcemTransactional
	public ActivationParameters addUpdateActivationCode(ActivationCodeEntity asActivationCode, DcemAction dcemAction, SendByEnum sendBy, boolean withAuditing,
			DcemTemplate dcemTemplate) throws DcemException {
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			asActivationCode.setId(null);
			asActivationCode.setCreatedOn(LocalDateTime.now());
			if (asActivationCode.getActivationCode() == null || asActivationCode.getActivationCode().isEmpty()) {
				asActivationCode.setActivationCode(generateActivationCode());
			}
			em.persist(asActivationCode);
		} else {
			em.merge(asActivationCode);
		}
		if (withAuditing) {
			auditingLogic.addAudit(dcemAction, asActivationCode.getUser().getLoginId());
		}
		String userFullQualifiedId = asModule.getUserFullQualifiedId(asActivationCode.getUser());
		ActivationParameters activationParameters = new ActivationParameters(userFullQualifiedId, asActivationCode.getActivationCode(),
				asActivationCode.getValidTill().toEpochSecond(ZoneOffset.UTC));
		sendActivationCode(sendBy, asActivationCode, dcemTemplate, activationParameters);
		return activationParameters;
	}

	private void sendActivationCode(SendByEnum sendBy, ActivationCodeEntity asActivationCode, DcemTemplate dcemTemplate,
			ActivationParameters activationParameters) throws DcemException {

		if (sendBy != null) {
			switch (sendBy) {
			case EMAIL:
			case PRIVAT_EMAIL:
				sendActivationByEmail(asActivationCode, dcemTemplate, activationParameters, sendBy);
				break;
			case SMS:
				sendActivationBySms(asActivationCode);
				break;
			default:

				break;
			}
		}
	}

	@DcemTransactional
	public ActivationCodeEntity createActivationCode(DcemUser dcemUser, LocalDateTime validTill, SendByEnum sendBy, String info) throws DcemException {
		return createActivationCode(dcemUser, validTill, sendBy, info, null);
	}

	@DcemTransactional
	public ActivationCodeEntity createActivationCode(DcemUser dcemUser, LocalDateTime validTill, SendByEnum sendBy, String info, DcemTemplate dcemTemplate)
			throws DcemException {
		ActivationCodeEntity activationCode = new ActivationCodeEntity();
		activationCode.setUser(dcemUser);
		activationCode.setInfo(info);
		if (validTill == null) {
			validTill = LocalDateTime.now();
			validTill.plusHours(getPreferences().getActivationCodeDefaultValidTill());
		}
		activationCode.setValidTill(validTill);
		addUpdateActivationCode(activationCode, new DcemAction(DcemConstants.AS_MODULE_ID, null, DcemConstants.ACTION_ADD), sendBy, false, dcemTemplate);
		return activationCode;
	}

	private AsPreferences getPreferences() {
		try {
			return asModule.getPreferences();
		} catch (NullPointerException e) { // could not load preferences (happens with newly created tenants)
			return new AsPreferences();
		}
	}

	public void sendPasswordBySms(DcemUser dcemUser, String password, String smsTextResource) throws DcemException {
		String mobile = dcemUser.getMobile();
		if (mobile == null || mobile.isEmpty()) {
			throw new DcemException(DcemErrorCodes.SMS_USER_HAS_NO_MOBILE, null);
		}
		Map<String, String> map = new HashMap<>();
		map.put(AsConstants.PASSWORD_BY_SMS, password);
		map.put(AsConstants.EMAIL_ACTIVATION_USER_DOMAIN_KEY, asModule.getUserFullQualifiedId(dcemUser));
		if (smsTextResource == null) {
			DbResourceBundle dbResourceBundle = DbResourceBundle.getDbResourceBundle(dcemUser.getLanguage().getLocale());
			smsTextResource = dbResourceBundle.getString(AsConstants.PASSWORD_BY_SMS_BUNDLE_KEY);
		}
		String body = StringUtils.substituteTemplate(smsTextResource, map);
		List<String> telephoneNumbers = new LinkedList<>();
		telephoneNumbers.add(mobile);
		messageBird.sendSmsMessage(telephoneNumbers, body);
	}

	private void sendActivationBySms(ActivationCodeEntity asActivationCode) throws DcemException {
		DcemUser dcemUser = asActivationCode.getUser();
		String mobile = dcemUser.getMobile();
		if (mobile == null || mobile.isEmpty()) {
			throw new DcemException(DcemErrorCodes.SMS_USER_HAS_NO_MOBILE, null);
		}
		DbResourceBundle dbResourceBundle = DbResourceBundle.getDbResourceBundle(dcemUser.getLanguage().getLocale());
		Map<String, String> map = new HashMap<>();
		map.put(AsConstants.EMAIL_ACTIVATION_CODE_KEY, asActivationCode.getActivationCode());
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, dcemUser.getLanguage().getLocale());
		df.setTimeZone(adminModule.getTimezone());
		map.put(AsConstants.EMAIL_ACTIVATION_VALID_TILL_KEY, df.format(asActivationCode.getValidTill()));
		map.put(AsConstants.EMAIL_ACTIVATION_USER_KEY, dcemUser.getDisplayNameOrLoginId());
		map.put(AsConstants.EMAIL_ACTIVATION_USER_DOMAIN_KEY, asModule.getUserFullQualifiedId(dcemUser));
		String body = StringUtils.substituteTemplate(dbResourceBundle.getString(AsConstants.SMS_ACTIVATION_BUNDLE_KEY), map);
		List<String> telephoneNumbers = new LinkedList<>();
		telephoneNumbers.add(mobile);
		messageBird.sendSmsMessage(telephoneNumbers, body);
	}

	/**
	 * @param dcemUser
	 * @param asActivationCode
	 * @param sendBy 
	 * @throws DcemException
	 */
	private void sendActivationByEmail(ActivationCodeEntity asActivationCode, DcemTemplate dcemTemplate, ActivationParameters activationParameters,
			SendByEnum sendBy) throws DcemException {

		DcemUser dcemUser = asActivationCode.getUser();
		DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.LONG).withZone(userLogic.getTimeZone(dcemUser).toZoneId())
				.withLocale(dcemUser.getLanguage().getLocale());
		if (dcemTemplate == null) {
			dcemTemplate = templateLogic.getTemplateByNameLanguage(DcemConstants.EMAIL_ACTIVATION_BODY_TEMPLATE, dcemUser.getLanguage());
			if (dcemTemplate == null) {
				throw new DcemException(DcemErrorCodes.NO_TEMPLATE_FOUND, "Missing template: " + DcemConstants.EMAIL_ACTIVATION_BODY_TEMPLATE);
			}
		}
		String emailAddress;
		if (sendBy == SendByEnum.EMAIL) {
			emailAddress = dcemUser.getEmail();
		} else {
			emailAddress = dcemUser.getPrivateEmail();
		}
		if (emailAddress == null || emailAddress.isEmpty() == true) {
			throw new DcemException(DcemErrorCodes.USER_HAS_INVALID_EMAIL, dcemUser.getDisplayName());
		}
		DbResourceBundle dbResourceBundle = DbResourceBundle.getDbResourceBundle(dcemUser.getLanguage().getLocale());
		String subject = dbResourceBundle.getString(AsConstants.EMAIL_ACTIVATION_SUBJECT_BUNDLE_KEY);
		Map<String, String> map = new HashMap<>();
		map.put(AsConstants.EMAIL_ACTIVATION_TENANT_NAME, TenantIdResolver.getCurrentTenantName());
		map.put(AsConstants.EMAIL_ACTIVATION_TENANT_URL, dcemApplicationBean.getDcemManagementUrl(null));
		map.put(AsConstants.EMAIL_ACTIVATION_TENANT_LOGIN_ID, dcemUser.getLoginId());
		map.put(AsConstants.EMAIL_ACTIVATION_CODE_KEY, asActivationCode.getActivationCode());

		map.put(AsConstants.EMAIL_ACTIVATION_VALID_TILL_KEY, userLogic.getZonedTime(asActivationCode.getValidTill(), dcemUser).format(dtf));
		map.put(AsConstants.EMAIL_ACTIVATION_USER_KEY, dcemUser.getDisplayNameOrLoginId());
		map.put(AsConstants.EMAIL_ACTIVATION_USER_DOMAIN_KEY, activationParameters.getUsername());

		ObjectMapper objectMapper = new ObjectMapper();

		byte[] pngImage = null;
		try {
			pngImage = DcemUtils.createQRCode(objectMapper.writeValueAsString(activationParameters), 200, 200);
		} catch (Throwable e) {
			new DcemException(DcemErrorCodes.CANNOT_CREATE_QRCODE, "Couldn't Create QrCode", e);
		}
		map.put(AsConstants.EMAIL_ACTIVATION_IMAGE, "<img src=\"cid:activation\">");
		String body = StringUtils.substituteTemplate(dcemTemplate.getContent(), map);
		SendEmail.sendMessage(emailAddress, body, subject, pngImage);
	}

	@DcemTransactional
	public String requestActivationCode(DcemUser user) {
		ActivationCodeEntity asActivationCode = new ActivationCodeEntity();
		asActivationCode.setUser(user);
		int till = getPreferences().getRequestActivationCodeValidTill();
		asActivationCode.setValidTill(LocalDateTime.now().plusHours(till));
		asActivationCode.setActivationCode(generateActivationCode());
		asActivationCode.setCreatedOn(LocalDateTime.now());
		em.persist(asActivationCode);
		return asActivationCode.getActivationCode();
	}

	/**
	 * @param userLoginId
	 * @param activationCode
	 * @return
	 */
	public ActivationCodeEntity validateActivationCode(DcemUser dcemUser, String activationCode) {

		TypedQuery<ActivationCodeEntity> query = em.createNamedQuery(ActivationCodeEntity.VALID_CODES, ActivationCodeEntity.class);
		query.setParameter(1, dcemUser);
		query.setParameter(2, LocalDateTime.now());
		List<ActivationCodeEntity> list = query.getResultList();
		for (ActivationCodeEntity code : list) {
			if (code.getActivationCode().equals(activationCode)) {
				return code;
			}
		}
		return null;
	}

	@DcemTransactional
	public void deleteExpiredActivationCodes() {
		Query query = em.createNamedQuery(ActivationCodeEntity.DELETE_EXPIRED);
		query.setParameter(1, LocalDateTime.now().plusDays(-1));
		query.executeUpdate();
	}

	private String generateActivationCode() {
		int length = getPreferences().getActivationCodeLength();
		return getPreferences().numericActivationCode ? RandomUtils.generateRandomNumberString(length)
				: RandomUtils.generateRandomAlphaUppercaseNumericString(length);
	}

	public void deleteUserActivation(DcemUser dcemUser) {
		Query query = em.createNamedQuery(ActivationCodeEntity.DELETE_USER_ACTIVATION);
		query.setParameter(1, dcemUser);
		query.executeUpdate();
	}

	@DcemTransactional
	public void deleteActivationCode(int activationCodeId) throws DcemException {
		ActivationCodeEntity activationCodeEntity = em.find(ActivationCodeEntity.class, activationCodeId);
		if (activationCodeEntity == null) {
			throw new DcemException(DcemErrorCodes.INVALID_ACTIVATION_CODE_ID, Integer.toString(activationCodeId));
		}
		em.remove(activationCodeEntity);
		auditingLogic.addAudit(new DcemAction(asActivationSubject, DcemConstants.ACTION_DELETE), activationCodeEntity.getUser().toString());
	}
}
