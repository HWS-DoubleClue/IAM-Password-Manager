package com.doubleclue.dcem.admin.logic;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.gui.WelcomeView.SelectedFormat;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuthMethodsActivityDto;
import com.doubleclue.dcem.core.logic.DbResourceBundle;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.system.send.EmailAttachment;
import com.doubleclue.dcem.system.send.SendEmail;
import com.doubleclue.utils.StringUtils;

@ApplicationScoped
@Named("reportingLogic")
public class DcemReportingLogic {

	@Inject
	EntityManager em;

	@Inject
	AdminModule adminModule;

	@Inject
	GroupLogic groupLogic;

	@Inject
	TemplateLogic templateLogic;

	public final static String CACHE_NAME = "Reporting";
	private static Logger logger = LogManager.getLogger(DcemReportingLogic.class);

	@DcemTransactional
	public void addReporting(DcemReporting dcemReporting) {
		if (adminModule.getPreferences().isReportErrorsOnly() && dcemReporting.getErrorCode() == null) {
			return;
		}
		dcemReporting.setLocalDateTime(LocalDateTime.now());
		dcemReporting.setId(adminModule.getTenantData().getReportIdGenerator().newId());
		if (dcemReporting.getSource() == null) {
			dcemReporting.setSource(AuthApplication.SECURE_APP.name());
		}
		em.persist(dcemReporting);
	}

	public Long getLastId() {
		TypedQuery<Long> query = em.createQuery("SELECT rp.id FROM AsReporting rp ORDER BY rp.id DESC", Long.class);
		query.setMaxResults(1);
		try {
			return query.getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
	}

	public Long getAllAuthenticationEvents(LocalDateTime resultFrom, LocalDateTime resultTo) {
		TypedQuery<Long> query = em.createNamedQuery(DcemReporting.GET_ALL_REPORTS_COUNT, Long.class);
		query.setParameter(1, resultFrom);
		query.setParameter(2, resultTo);
		query.setParameter(3, ReportAction.Authenticate_pwd);
		query.setParameter(4, ReportAction.Authenticate_fido);
		try {
			return query.getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
	}

	public List<AuthMethodsActivityDto> getAllAuthMethodsEvents(LocalDateTime resultFrom, LocalDateTime resultTo) {
		TypedQuery<AuthMethodsActivityDto> query = em.createNamedQuery(DcemReporting.GET_ALL_AUTH_METHODS_COUNT, AuthMethodsActivityDto.class);
		query.setParameter(1, resultFrom);
		query.setParameter(2, resultTo);
		query.setParameter(3, ReportAction.Authenticate_pwd);
		query.setParameter(4, ReportAction.Authenticate_fido);
		try {
			return query.getResultList();
		} catch (NoResultException exp) {
			return null;
		}
	}

	public Long getFailedAuthenticationEvents(LocalDateTime resultFrom, LocalDateTime resultTo) {
		TypedQuery<Long> query = em.createNamedQuery(DcemReporting.GET_REPORTS_COUNT, Long.class);
		query.setParameter(1, resultFrom);
		query.setParameter(2, resultTo);
		query.setParameter(3, ReportAction.Authenticate);
		try {
			return query.getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
	}

	public void deleteUserReports(DcemUser dcemUser) {
		Query query = em.createNamedQuery(DcemReporting.DELETE_USER_REPORTS);
		query.setParameter(1, dcemUser);
		query.executeUpdate();
	}

	public List<DcemReporting> getDashboardAlertMessages() {
		TypedQuery<DcemReporting> query = em.createNamedQuery(DcemReporting.GET_ALL_DASHBOARD_REPORTS, DcemReporting.class);
		return query.getResultList();
	}

	@DcemTransactional
	public void insertAlertMessage(DcemReporting entityToSave) {
		if ((adminModule.getPreferences().isReportErrorsOnly() && entityToSave.getErrorCode() == null) == false) {
			try {
				entityToSave.setId(adminModule.getTenantData().getReportIdGenerator().newId());
				entityToSave.setShowOnDashboard(true);
				em.persist(entityToSave);
			} catch (Exception e) {
				logger.debug("Failed to insert Alert Message. ", e);
			}
		}
	}

	@DcemTransactional
	public void closeAlertMessage(Long entityId) {
		Query query = em.createNamedQuery(DcemReporting.CLOSE_DASHBOARD_REPORT);
		query.setParameter(1, entityId);
		query.executeUpdate();
	}

	public Map<String, Map<String, DcemReporting>> getWelcomeViewAlerts() {
		Map<String, Map<String, DcemReporting>> welcomeViewAlerts = new HashMap<>();
		for (DcemReporting entity : getDashboardAlertMessages()) {
			if (welcomeViewAlerts.get(entity.getSource()) == null) {
				HashMap<String, DcemReporting> map = new HashMap<String, DcemReporting>();
				map.put(entity.getSource(), entity);
				welcomeViewAlerts.put(entity.getSource(), map);
			} else {
				welcomeViewAlerts.get(entity.getSource()).put(entity.getId().toString(), entity);
			}
		}
		return welcomeViewAlerts;
	}

	@DcemTransactional
	public void addWelcomeViewAlert(String category, DcemErrorCodes errorCode, String title, AlertSeverity severity, boolean checkExists,
			Object... messageParams) {
		try {
			MessageFormat fmt = new MessageFormat(JsfUtils.getStringSafely(DcemConstants.CORE_RESOURCE, "DcemErrorCodes." + errorCode));
			String message = fmt.format(messageParams);
			addWelcomeViewAlert(category, errorCode, title, message, severity, checkExists);
		} catch (Exception e) {
			addWelcomeViewAlert(category, errorCode, title, null, severity, checkExists);
		}
	}

	@DcemTransactional
	private void addWelcomeViewAlert(String category, DcemErrorCodes errorCode, String title, String message, AlertSeverity severity, boolean checkExists) {

		if (severity == null) {
			severity = AlertSeverity.WARNING;
		}
		if (title != null && title.isEmpty() == false) {
			message = title + " : " + message;
		}

		DcemReporting alertMessage = new DcemReporting();
		alertMessage.setSource(category);
		alertMessage.setInfo(message);
		alertMessage.setSeverity(severity);
		alertMessage.setErrorCode(errorCode.toString());
		alertMessage.setLocalDateTime(LocalDateTime.now());

		if ((checkExists && welcomeViewAlertExists(alertMessage)) == false) {
			addWelcomeViewAlert(category, alertMessage);
		}
	}

	@DcemTransactional
	public void addWelcomeViewAlert(String category, DcemReporting entityToSave) {
		try {
			insertAlertMessage(entityToSave);
			sendAlertEmailNotification(entityToSave.getAlertDisplayString());
		} catch (Exception e) {
			logger.debug("Unexpected error while adding WelcomeViewAlert", e);
		}
		switch (entityToSave.getSeverity()) {
		case FAILURE:
		case OK:
			logger.info(entityToSave.toString());
			break;
		case ERROR:
			logger.error(entityToSave.toString());
			break;
		case WARNING:
			logger.warn(entityToSave.toString());
			break;
		}
	}

	@DcemTransactional
	public void sendAlertEmailNotification(String alertMessage) {
		try {
			HashMap<SupportedLanguage, List<String>> map = new HashMap<SupportedLanguage, List<String>>();
			String notificationGroupName = adminModule.getPreferences().getAlertsNotificationGroup();
			if (notificationGroupName != null && notificationGroupName.isEmpty() == false) {
				DcemGroup selectedGroup = groupLogic.getGroup(notificationGroupName);
				if (selectedGroup != null) {
					for (DcemUser recipient : selectedGroup.getMembers()) {
						String email = recipient.getEmail();
						if (email != null && email.isEmpty() == false) {
							SupportedLanguage language = recipient.getLanguage();
							if (map.containsKey(language) == false) {
								map.put(language, new ArrayList<String>());
							}
							map.get(language).add(email);
						}
					}
				}
			}
			for (Entry<SupportedLanguage, List<String>> entry : map.entrySet()) {
				Map<String, String> templateMap = new HashMap<>();
				templateMap.put(DcemConstants.EMAIL_ALERTS_ALERT_MESSAGE, alertMessage);
				DbResourceBundle bundle = DbResourceBundle.getDbResourceBundle(entry.getKey().getLocale());
				DcemTemplate bodyTemplate = templateLogic.getTemplateByNameLanguage(DcemConstants.EMAIL_ALERTS_BODY_TEMPLATE, entry.getKey());
				if (bodyTemplate == null) {
					logger.warn("Missing template for " + DcemConstants.EMAIL_ALERTS_BODY_TEMPLATE + ". Could not send email to " + entry.getValue());
				} else {
					String body = StringUtils.substituteTemplate(bodyTemplate.getContent(), templateMap);
					for (String entryValue : entry.getValue()) {
						SendEmail.sendMessage(entryValue, body, bundle.getString(DcemConstants.EMAIL_ALERTS_SUBJECT_BUNDLE_KEY), (EmailAttachment) null);
					}

				}
			}
		} catch (Exception e) {
			logger.debug("Error while sending alert email notification", e);
		}
	}

	public boolean welcomeViewAlertExists(DcemReporting entityToSave) {
		try {
			Query query = em.createNamedQuery(DcemReporting.GET_DASHBOARD_REPORT);
			query.setParameter(1, entityToSave.getSource());
			query.setParameter(2, entityToSave.getSeverity());
			query.setParameter(3, entityToSave.getErrorCode());
			query.setParameter(4, entityToSave.getInfo());
			return query.getResultList().isEmpty() == false;
		} catch (Exception e) {
			logger.debug("Error while checking if alert exists", e);
		}
		return false;
	}

	public HashMap<LocalDateTime, Long> getUserActivityData(LocalDateTime localDateTime, SelectedFormat dateFormat, boolean validLogins) {
		HashMap<LocalDateTime, Long> result = new HashMap<>();
		LocalDateTime localDateTimeStart;
		LocalDateTime localDateTimeEnd;
		switch (dateFormat) {
		case YEAR:
			for (int month = 1; month <= 12; month++) {
				localDateTimeStart = localDateTime.toLocalDate().withMonth(month).withDayOfMonth(1).atTime(LocalTime.MIN);
				localDateTimeEnd = localDateTime.toLocalDate().withMonth(month).with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
				result.put(localDateTimeStart, getUserActivityCount(localDateTimeStart, localDateTimeEnd, validLogins));
			}
			break;
		case MONTH:
			int lastDayOfMonth = localDateTime.toLocalDate().lengthOfMonth();
			for (int day = 1; day <= lastDayOfMonth; day++) {
				localDateTimeStart = localDateTime.toLocalDate().withDayOfMonth(day).atTime(LocalTime.MIN);
				localDateTimeEnd = localDateTime.toLocalDate().withDayOfMonth(day).atTime(LocalTime.MAX);
				result.put(localDateTimeStart, getUserActivityCount(localDateTimeStart, localDateTimeEnd, validLogins));
			}
			break;
		default:  // DAY
			for (int hour = 0; hour <= 23; hour++) {
				localDateTimeStart = localDateTime.toLocalDate().atTime(LocalTime.of(hour, 0));
				localDateTimeEnd = localDateTime.toLocalDate().atTime(LocalTime.of(hour, 59, 59));
				result.put(localDateTimeStart, getUserActivityCount(localDateTimeStart, localDateTimeEnd, validLogins));
			}
			break;
		}
		return result;
	}

	public Long getUserActivityCount(LocalDateTime dateFrom, LocalDateTime dateTo, boolean validLogin) {
		if (validLogin) {
			return getAllAuthenticationEvents(dateFrom, dateTo);
		} else {
			return getFailedAuthenticationEvents(dateFrom, dateTo);
		}
	}

	public HashMap<Integer, Long> getAuthMethodActivityData(LocalDateTime startDateTime, SelectedFormat format) {
		HashMap<Integer, Long> result = new HashMap<Integer, Long>();
		LocalDateTime localDateTimeStart;
		LocalDateTime localDateTimeEnd;
		switch (format) {
		case MONTH:
			localDateTimeStart = startDateTime.toLocalDate().with(TemporalAdjusters.firstDayOfMonth()).atTime(LocalTime.MIN);
			localDateTimeEnd = startDateTime.toLocalDate().with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
			break;
		case YEAR:
			localDateTimeStart = startDateTime.toLocalDate().with(TemporalAdjusters.firstDayOfYear()).atTime(LocalTime.MIN);
			localDateTimeEnd = startDateTime.toLocalDate().with(TemporalAdjusters.lastDayOfYear()).atTime(LocalTime.MAX);
			break;
		default:
			localDateTimeStart = startDateTime.toLocalDate().atTime(LocalTime.MIN);
			localDateTimeEnd = startDateTime.toLocalDate().atTime(LocalTime.MAX);
			break;
		}
		for (AuthMethodsActivityDto authMethodCount : getAllAuthMethodsEvents(localDateTimeStart, localDateTimeEnd)) {
			result.put(authMethodCount.getId().ordinal(), authMethodCount.getCount());
		}
		return result;
	}
}
