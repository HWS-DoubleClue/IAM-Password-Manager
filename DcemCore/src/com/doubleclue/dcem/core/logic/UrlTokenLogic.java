package com.doubleclue.dcem.core.logic;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.UrlTokenEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.system.send.SendEmail;
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

	// private static final Logger logger = LogManager.getLogger(UrlTokenLogic.class);

	@DcemTransactional
	public UrlTokenEntity addUrlTokenToDb(String url, UrlTokenType urlTokenUsage, int validMinutes, String urlToken, String objectIdentifier) throws DcemException {
		UrlTokenEntity entity = new UrlTokenEntity();
		if (urlToken == null) {
			urlToken = java.util.UUID.randomUUID().toString();
		}
		Date expiryDate;
		if (validMinutes == 0) {
			expiryDate = new Date(System.currentTimeMillis() + 86400000);
		} else {
			expiryDate = new Date(System.currentTimeMillis() + validMinutes * 60000);
		}
		entity.setUrlToken(urlToken);
		entity.setExpiryDate(expiryDate);
		entity.setObjectIdentifier(objectIdentifier);
		entity.setUrlTokenType(urlTokenUsage);
		em.persist(entity);
		return entity;
	}

	@DcemTransactional
	public String verifyUrlToken(String urlToken, String type) throws DcemException {
		UrlTokenEntity entity = getUrlToken(urlToken);
		if (entity == null) {
			throw new DcemException(DcemErrorCodes.URL_TOKEN_INVALID, null);
		}
		if (entity.getExpiryDate().getTime() < new Date().getTime()) {
			em.remove(entity);
			throw new DcemException(DcemErrorCodes.URT_TOKEN_OUT_OF_DATE, null);
		}
		if (!entity.getUrlTokenType().toString().equals(type)) {
			throw new DcemException(DcemErrorCodes.URL_TOKEN_INVALID, null);
		}
		em.remove(entity);
		return entity.getObjectIdentifier();
	}

	private UrlTokenEntity getUrlToken(String urlToken) {
		return em.find(UrlTokenEntity.class, urlToken);
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
