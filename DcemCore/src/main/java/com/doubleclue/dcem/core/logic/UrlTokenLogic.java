package com.doubleclue.dcem.core.logic;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.UrlTokenEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.system.send.SendEmail;
import com.doubleclue.utils.RandomUtils;
import com.doubleclue.utils.StringUtils;

@ApplicationScoped
@Named("urlTokenLogic")
public class UrlTokenLogic {

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	TemplateLogic templateLogic;

	private static final Logger logger = LogManager.getLogger(UrlTokenLogic.class);

	static final String MAIL_TOKEN_FORMAT_HASH = "%d.%s.%s.%s";
	static final String MAIL_TOKEN_FORMAT = "{{%02x%s}}";

	@DcemTransactional
	public UrlTokenEntity addUrlTokenToDb(UrlTokenType urlTokenUsage, int validMinutes, String urlToken, String objectIdentifier) throws DcemException {
		UrlTokenEntity entity = new UrlTokenEntity();
		if (urlToken == null) {
			urlToken = java.util.UUID.randomUUID().toString();
		}
		LocalDateTime expiryDate;
		if (validMinutes == 0) {
			expiryDate = LocalDateTime.now().plusDays(1000);
		} else {
			expiryDate = LocalDateTime.now().plusMinutes(validMinutes);
		}
		entity.setUrlToken(urlToken);
		entity.setExpiryDate(expiryDate);
		entity.setObjectIdentifier(objectIdentifier);
		entity.setUrlTokenType(urlTokenUsage);
		em.persist(entity);
		return entity;
	}

	@DcemTransactional
	public String addMailUrlToken(String moduleId, LocalDateTime expiryDate, String objectIdentifier) throws DcemException {
		UrlTokenEntity entity = new UrlTokenEntity();
		String urlToken = RandomUtils.generateRandomAlphaLowercaseNumericString(12);
		entity.setUrlToken(urlToken);
		entity.setExpiryDate(expiryDate);
		entity.setObjectIdentifier(objectIdentifier);
		entity.setUrlTokenType(UrlTokenType.EmailToken);
		em.persist(entity);
		return createEmailToken(entity, moduleId);
	}

	private String createEmailToken(UrlTokenEntity entity, String moduleId) {
		int tenentId = 0;
		if (TenantIdResolver.isCurrentTenantMaster() == false) {
			tenentId = TenantIdResolver.getCurrentTenant().getId().intValue();
		}
		String tokenPart = String.format(MAIL_TOKEN_FORMAT_HASH, tenentId, moduleId, entity.getObjectIdentifier(),
				entity.getUrlToken());
		byte hash = (byte) (tokenPart.hashCode() & 0xFF);
		return String.format(MAIL_TOKEN_FORMAT, hash, tokenPart);
	}

	public UrlTokenEntity verifyUrlToken(String urlToken, String type) throws DcemException {
		UrlTokenEntity entity = em.find(UrlTokenEntity.class, urlToken);
		if (entity == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("URL Token not found: " + urlToken + " Type: " + type);
			}
			throw new DcemException(DcemErrorCodes.URL_TOKEN_INVALID, null);
		}
		if (entity.getExpiryDate().isBefore(LocalDateTime.now())) {
			throw new DcemException(DcemErrorCodes.URT_TOKEN_OUT_OF_DATE, null);
		}
		if (entity.getUrlTokenType().toString().equals(type) == false) {
			throw new DcemException(DcemErrorCodes.URL_TOKEN_INVALID, null);
		}
		return entity;
	}
	
	
	@DcemTransactional
	public void deleteUrlToken(UrlTokenEntity entity) {
		UrlTokenEntity entity2 = em.find(UrlTokenEntity.class, entity.getUrlToken());
		if (entity2 != null) {
			em.remove(entity2);
		}
	}

	public void sendUrlTokenByEmail(DcemUser dcemUser, String url, UrlTokenEntity urlTokenEntity) throws DcemException {
		DcemTemplate bodyTemplate = templateLogic.getTemplateByNameLanguage(getDcemTemplateName(urlTokenEntity.getUrlTokenType()), dcemUser.getLanguage());
		if (bodyTemplate == null) {
			throw new DcemException(DcemErrorCodes.NO_TEMPLATE_FOUND, "Missing template: " + getDcemTemplateName(urlTokenEntity.getUrlTokenType()));
		}
		templateLogic.setTemplateInUse(bodyTemplate);
		if (dcemUser.getEmail() == null || dcemUser.getEmail().isEmpty()) {
			throw new DcemException(DcemErrorCodes.USER_HAS_INVALID_EMAIL, null);
		}
		DbResourceBundle dbResourceBundle = DbResourceBundle.getDbResourceBundle(dcemUser.getLanguage().getLocale());
		String subject;
		try {
			subject = dbResourceBundle.getString(getDcemTemplateNameSubject(urlTokenEntity.getUrlTokenType()));
		} catch (Exception e) {
			subject = "DoubleClue Verify EMail";
		}

		String link = url + urlTokenEntity.getUrlToken() + "&type=" + urlTokenEntity.getUrlTokenType().toString();
		Map<String, String> map = new HashMap<>();
		map.put(DcemConstants.EMAIL_FORGOT_PASSWORD_KEY, link);
		map.put(DcemConstants.EMAIL_LICENCE_USER_KEY, dcemUser.getLoginId());

		String body = StringUtils.substituteTemplate(bodyTemplate.getContent(), map);
		if (dcemUser.getEmail() != null && dcemUser.getEmail().isEmpty() == false) {
			SendEmail.sendMessage(dcemUser.getEmail(), body, subject);
		}
		if (dcemUser.getPrivateEmail() != null && dcemUser.getPrivateEmail().isEmpty() == false) {
			SendEmail.sendMessage(dcemUser.getPrivateEmail(), body, subject);
		}
	}

	private String getDcemTemplateName(UrlTokenType urlTokenType) {
		String templateName;
		switch (urlTokenType) {
		case VerifyEmail:
			templateName = DcemConstants.EMAIL_VERIFY_EMAIL_BODY_TEMPLATE;
			break;
		case ResetPassword:
			templateName = DcemConstants.EMAIL_RESET_PASSWORD_BODY_TEMPLATE;
			break;
		default:
			templateName = "";
			break;
		}
		return templateName;
	}

	private String getDcemTemplateNameSubject(UrlTokenType urlTokenType) {
		String subjectName;
		switch (urlTokenType) {
		case VerifyEmail:
			subjectName = DcemConstants.EMAIL_VERIFY_EMAIL_SUBJECT_BUNDLE_KEY;
			break;
		case ResetPassword:
			subjectName = DcemConstants.EMAIL_PASSWORD_RESET_SUBJECT_BUNDLE_KEY;
			break;
		default:
			subjectName = "";
			break;
		}
		return subjectName;
	}

	@DcemTransactional
	public int deleteUserUrlTokens(String objectIdentifier) {
		Query query = em.createNamedQuery(UrlTokenEntity.DELETE_USER);
		query.setParameter(1, objectIdentifier);
		return query.executeUpdate();
	}

	
}
