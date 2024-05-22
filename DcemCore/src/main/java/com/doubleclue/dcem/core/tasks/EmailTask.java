package com.doubleclue.dcem.core.tasks;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.logic.DbResourceBundle;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.system.send.EmailAttachment;
import com.doubleclue.dcem.system.send.SendEmail;

import freemarker.template.Template;

public class EmailTask extends CoreTask {

	private static final Logger logger = LogManager.getLogger(EmailTask.class);

	List<DcemUser> users = null;
	Set<String> emailAdresses = null;
	Map<String, Object> map;
	String templateName;
	String subjectResource;
	List<EmailAttachment> attachments;
	SupportedLanguage language;

	public EmailTask(List<DcemUser> users, Map<String, Object> map, String templateName, String subjectResource, List<EmailAttachment> attachments) {
		super(EmailTask.class.getSimpleName(), null);
		this.users = users;
		this.map = map;
		this.templateName = templateName;
		this.subjectResource = subjectResource;
		this.attachments = attachments;
	}

	public EmailTask(Set<String> emailAdresses, SupportedLanguage language, Map<String, Object> map, String templateName, String subjectResource,
			List<EmailAttachment> attachments) {
		super(EmailTask.class.getSimpleName(), null);
		this.emailAdresses = emailAdresses;
		this.map = map;
		this.templateName = templateName;
		this.subjectResource = subjectResource;
		this.attachments = attachments;
		this.language = language;
	}

	@Override
	public void runTask() {
		logger.debug("EmailTask started");
		long start = System.currentTimeMillis();
		DcemApplicationBean applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
		TemplateLogic templateLogic = CdiUtils.getReference(TemplateLogic.class);
		HashMap<SupportedLanguage, Set<String>> mapSortEmailsByLanguage = new HashMap<SupportedLanguage, Set<String>>();
		if (users != null) {
			for (DcemUser dcemUser : users) {
				Set<String> emails = mapSortEmailsByLanguage.get(dcemUser.getLanguage());
				if (emails == null) {
					emails = new HashSet<String>();
					mapSortEmailsByLanguage.put(dcemUser.getLanguage(), emails);
				}
				if (dcemUser.getEmail() == null || dcemUser.getEmail().isEmpty() == true) {
					logger.info("Could not send email to '" + dcemUser.getLoginId() + "'. User has no Email!");
					continue;
				}
				emails.add(dcemUser.getEmail());
			}
		}
		if (emailAdresses != null && language != null) {
			mapSortEmailsByLanguage.put(language, emailAdresses);
		}

		DcemTemplate dcemTemplateEmail = null;
		for (SupportedLanguage language : mapSortEmailsByLanguage.keySet()) {
			try {
				DbResourceBundle dbResourceBundle = DbResourceBundle.getDbResourceBundle(language.getLocale());
				dcemTemplateEmail = templateLogic.getTemplateByNameLanguage(templateName, language);
				if (dcemTemplateEmail == null) {
					logger.error("Couldn't send Emanuel with template. Tempalte name not found for: " + templateName);
					continue;
				}
				StringWriter stringWriter = new StringWriter();
				Template tempalte = applicationBean.getTemplateFromConfig(dcemTemplateEmail);
				tempalte.process(map, stringWriter);
				SendEmail.sendMessage(new ArrayList<String>(mapSortEmailsByLanguage.get(language)), stringWriter.toString(),
						dbResourceBundle.getString(subjectResource), attachments);
			} catch (Exception e) {
				logger.error("E-Mail Task FAILED", e);
				continue;
			}
			// String body = StringUtils.substituteTemplate(dcemTemplateEmail.getContent(), map);
		}
		logger.debug("EmailTask ends: " + (System.currentTimeMillis() - start));
	}

}
