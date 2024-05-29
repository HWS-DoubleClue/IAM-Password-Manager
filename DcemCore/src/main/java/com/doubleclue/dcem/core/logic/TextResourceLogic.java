package com.doubleclue.dcem.core.logic;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.PropertyResourceBundle;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.TextMessage;
import com.doubleclue.dcem.core.entities.TextResourceBundle;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.utils.FileContent;
import com.doubleclue.utils.ResourceFinder;

@ApplicationScoped
@Named("textResourceLogic")
public class TextResourceLogic {

	private static final Logger logger = LogManager.getLogger(TextResourceLogic.class);

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	DcemApplicationBean applicationBean;

	public TextResourceLogic() {
	}

	public List<TextResourceBundle> getAllTextResourceBundles() {
		TypedQuery<TextResourceBundle> query = em.createNamedQuery(TextResourceBundle.QUERYNAME_FIND_BY_BASE_NAME, TextResourceBundle.class);
		query.setParameter(1, AdminModule.MODULE_ID);
		try {
			return query.getResultList();
		} catch (NoResultException exp) {
			return null;
		}
	}

	public TextResourceBundle getTextResourceBundle(String localeDisplayName) {
		TypedQuery<TextResourceBundle> query = em.createNamedQuery(TextResourceBundle.QUERYNAME_FIND_BY_LOCALE, TextResourceBundle.class);
		query.setParameter(1, AdminModule.MODULE_ID);
		query.setParameter(2, localeDisplayName);
		try {
			return query.getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
	}

	@DcemTransactional
	public TextResourceBundle addTextResourceBundle(String locale) {
		TextResourceBundle textResource = getTextResourceBundle(locale);
		if (textResource == null) {
			textResource = new TextResourceBundle(locale);
			em.persist(textResource);
		}
		return textResource;
	}

	@DcemTransactional
	public void addOrUpdate(TextMessage textMessage, DcemAction dcemAction, String baseName, String locale) {

		String auditInfo = null;
		auditingLogic.addAudit(dcemAction, textMessage);
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD) || dcemAction.getAction().equals(DcemConstants.ACTION_COPY)) {
			TextResourceBundle textResourceBundle = addTextResourceBundle(locale);
			textMessage.setTextResourceBundle(textResourceBundle);
			textMessage.setId(null);
			textMessage.setJpaVersion(0);
			em.persist(textMessage);
			auditInfo = textMessage.toString();
		} else {
			em.merge(textMessage);
		}
	}

	public TextMessage getResourceMessage(TextResourceBundle textResourceBundle, String key) {
		TypedQuery<TextMessage> query = em.createNamedQuery(TextMessage.QUERYNAME_FIND_KEY, TextMessage.class);
		query.setParameter(1, textResourceBundle);
		query.setParameter(2, key);
		try {
			return query.getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
	}

	public List<TextMessage> getResourceMessages(TextResourceBundle textResourceBundle, String key) {
		TypedQuery<TextMessage> query = em.createNamedQuery(TextMessage.QUERYNAME_FIND_KEY, TextMessage.class);
		query.setParameter(1, textResourceBundle);
		query.setParameter(2, key);
		try {
			return query.getResultList();
		} catch (NoResultException exp) {
			return null;
		}
	}

	public Properties loadResourceFromDB(Locale locale) {
		TextResourceBundle textResourceBundle = getTextResourceBundle(locale.getDisplayLanguage());
		Properties properties = new Properties();
		if (textResourceBundle != null) {
			List<TextMessage> resources = textResourceBundle.getMessages();
			for (TextMessage resource : resources) {
				properties.put(resource.getKey(), resource.getValue());
			}
		}
		return properties;
	}

	public TextMessage getEntityById(Integer id) {
		return em.find(TextMessage.class, id);
	}

	@DcemTransactional
	public void addTextResourceProperties(String baseName, Properties properties, Locale locale, boolean overwrite) throws DcemException {
		TextResourceBundle textResourceBundle = addTextResourceBundle(locale.getDisplayLanguage());
		Enumeration<Object> keys = properties.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = properties.getProperty(key);
			TextMessage textMessage = getResourceMessage(textResourceBundle, key);
			if (textMessage == null) {
				textMessage = new TextMessage();
				textMessage.setTextResourceBundle(textResourceBundle);
				textMessage.setKey(key);
				textMessage.setValue(value);
				em.persist(textMessage);
			} else {
				if (overwrite) {
					textMessage.setValue(value);
					em.merge(textMessage);
				}
			}
		}
		return;
	}

	@DcemTransactional
	public void createDefaultTextResources(boolean overwrite) throws Exception {
		List<FileContent> fileResources;
		Locale locale;
		for (DcemModule module : applicationBean.getSortedModules()) {
			try {
				fileResources = ResourceFinder.find(module.getClass(), DcemConstants.TEXT_RESOURCES_FOLDER, DcemConstants.TEXT_TYPE);
			} catch (IllegalArgumentException e) {
				logger.debug("No Text Resources found for " + module.getName());
				continue;
			}
			for (FileContent fileContent : fileResources) {
				String fileName = fileContent.getName().substring(0, fileContent.getName().length() - DcemConstants.TEXT_TYPE.length());
				String localeText = fileName.substring(fileName.length() - 2);
				if (fileName.charAt(fileName.length() - 3) != '_') {
					logger.warn("Invalid resource name format. " + fileContent.getName());
					continue;
				}
				fileName = fileName.substring(0, fileName.length() - 3);
				locale = new Locale(localeText);
				Properties properties = new Properties();
				ByteArrayInputStream bais = new ByteArrayInputStream(fileContent.getContent());
				properties.load(new InputStreamReader(bais, Charset.forName("UTF-8")));
				try {
					addTextResourceProperties(AdminModule.MODULE_ID, properties, locale, overwrite);
				} catch (Exception exp) {
					logger.error("Couldn't save Text-Resources for: " + fileContent.getName(), exp);
					throw exp;
				}
			}
		}
	}

	@DcemTransactional
	public void deleteTextResource(TextResourceBundle trb, String key) {
		Query query = em.createNamedQuery(TextMessage.QUERYNAME_DELETE_BY_KEY);
		query.setParameter(1, trb);
		query.setParameter(2, key);
		query.executeUpdate();
	}
}
