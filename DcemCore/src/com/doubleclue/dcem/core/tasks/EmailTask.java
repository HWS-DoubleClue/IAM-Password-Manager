package com.doubleclue.dcem.core.tasks;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.MissingFormatArgumentException;
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
import com.doubleclue.dcem.system.send.SendEmail;

import freemarker.template.Template;

public class EmailTask extends CoreTask {

    private static final Logger logger = LogManager.getLogger(EmailTask.class);

    List<DcemUser> users;
    Map<String, Object> map;
    String templateName;
    String subject;
    byte[] attachment;
    List<String> subjectParameter;
    
    public EmailTask(List<DcemUser> users, Map<String, Object> map, String templateName, String subjectResource, byte[] attachment) {
        super(EmailTask.class.getSimpleName(), null);
        this.users = users;
        this.map = map;
        this.templateName = templateName;
        this.subject = subjectResource;
        this.attachment = attachment;
        this.subjectParameter = new ArrayList<>();
    }

    public EmailTask(List<DcemUser> users, Map<String, Object> map, String templateName, String subjectResource, byte[] attachment, List<String> subjectParameter) {
        super(EmailTask.class.getSimpleName(), null);
        this.users = users;
        this.map = map;
        this.templateName = templateName;
        this.subject = subjectResource;
        this.attachment = attachment;
        this.subjectParameter = subjectParameter;
    }
    
    @Override
    public void runTask() {
        logger.debug("EmailTask started");
        if (subjectParameter == null) {
            subjectParameter = new ArrayList<>();
        }
        long start = System.currentTimeMillis();
        DcemApplicationBean applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
        TemplateLogic templateLogic = CdiUtils.getReference(TemplateLogic.class);
        HashMap<SupportedLanguage, Set<String>> mapSortEmailsByLanguage = new HashMap<SupportedLanguage, Set<String>>();
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
        DcemTemplate dcemTemplateEmail = null;
        for (SupportedLanguage language : mapSortEmailsByLanguage.keySet()) {
            try {
                DbResourceBundle dbResourceBundle = DbResourceBundle.getDbResourceBundle(language.getLocale());
                dcemTemplateEmail = templateLogic.getTemplateByNameLanguage(templateName, language);
                if (dcemTemplateEmail == null) {
                    logger.error("Couldn't send Email with template. Tempalte name not found for: " + templateName);
                    continue;
                }
                StringWriter stringWriter = new StringWriter();
                Template tempalte = applicationBean.getTemplateFromConfig(dcemTemplateEmail);
                tempalte.process(map, stringWriter);

                String subjectString = String.format(dbResourceBundle.getString(subject), subjectParameter.toArray());
                SendEmail.sendMessage(new ArrayList<String>(mapSortEmailsByLanguage.get(language)), stringWriter.toString(), subjectString, attachment);
            } catch (MissingFormatArgumentException e) {
                logger.error("E-Mail Task FAILED. Subject expected more arguments", e);
                continue;
            } catch (Exception e) {
                logger.error("E-Mail Task FAILED", e);
                continue;
            }
            // String body = StringUtils.substituteTemplate(dcemTemplateEmail.getContent(), map);
        }
        logger.debug("EmailTask ends: " + (System.currentTimeMillis() - start));
    }

}
