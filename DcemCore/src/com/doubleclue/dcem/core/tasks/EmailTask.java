package com.doubleclue.dcem.core.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.logic.DbResourceBundle;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.system.send.SendEmail;
import com.doubleclue.utils.StringUtils;

public class EmailTask extends CoreTask {

	private static final Logger logger = LogManager.getLogger(EmailTask.class);

	List<DcemUser> users;
	Map<String, String> map;
	String templateName;
	String subjectResource;
	byte[] attachment;

	public EmailTask(List<DcemUser> users, Map<String, String> map, String templateName, String subjectResource, byte[] attachment) {
		super(EmailTask.class.getSimpleName(), null);
		this.users = users;
		this.map = map;
		this.templateName = templateName;
		this.subjectResource = subjectResource;
		this.attachment = attachment;
	}
	
	

	@Override
	public void runTask() {
		logger.info("EmailTask started");
		long start = System.currentTimeMillis();
//		DcemApplicationBean applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
		TemplateLogic templateLogic = CdiUtils.getReference(TemplateLogic.class);
		HashMap<SupportedLanguage, List<String>> mapSortEmailsByLanguage= new HashMap<SupportedLanguage, List<String>>();		
		for (DcemUser dcemUser : users) {
			List<String> emails = mapSortEmailsByLanguage.get(dcemUser.getLanguage());
			if (emails == null) {
				emails = new ArrayList<String>();
				mapSortEmailsByLanguage.put(dcemUser.getLanguage(), emails);
			}
			if (dcemUser.getEmail() == null || dcemUser.getEmail().isEmpty() == true) {
				logger.warn("Could not send email to '" + dcemUser.getLoginId() + "'. User has no Email!");
				continue;
			}
			emails.add(dcemUser.getEmail());			
		}
		
		DcemTemplate dcemTemplateEmail= null;
		for (SupportedLanguage language : mapSortEmailsByLanguage.keySet()) {
			DbResourceBundle dbResourceBundle = DbResourceBundle.getDbResourceBundle(language.getLocale());
			String subject = dbResourceBundle.getString(subjectResource);
			dcemTemplateEmail =  templateLogic.getTemplateByNameLanguage(templateName, language);
			String body = StringUtils.substituteTemplate(dcemTemplateEmail.getContent(), map);
			try {
				SendEmail.sendMessage(mapSortEmailsByLanguage.get(language), body, subject, attachment);
			} catch (DcemException e) {
				logger.error(e);
			}
		}
		logger.info("EmailTask ends: " + (System.currentTimeMillis() - start));
	}

}
