package com.doubleclue.dcem.core.logic;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.tasks.ReloadClassInterface;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.utils.FileContent;
import com.doubleclue.utils.ResourceFinder;
import com.doubleclue.utils.StringUtils;

@ApplicationScoped
@Named("templateLogic")
public class TemplateLogic implements ReloadClassInterface {

	private static Logger logger = LogManager.getLogger(TemplateLogic.class);

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	EntityManager em;

	@DcemTransactional
	public void addOrUpdateTemplate(DcemTemplate template, DcemAction dcemAction, boolean withAudit) throws DcemException {
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD) || dcemAction.getAction().equals(DcemConstants.ACTION_COPY)) {
			template.setId(null);
			template.setVersion(1);
			template.setInUse(false);
			List<DcemTemplate> tempaltes = getTemplatesByName(template.getName(), template.getLanguage());
			for (DcemTemplate preTemplate : tempaltes) {
				preTemplate.setActive(false);
			}
			template.setActive(true);
			template.setTokens(getTokens(template.getContent()));
			template.setLastModified(LocalDateTime.now());
			em.persist(template);
		} else {
			if (template.isActive() == false) {
				throw new DcemException(DcemErrorCodes.CANNOT_CHANGE_TEMPLATE_IN_USE, "Can't change inactive Template");
			}
			if (template.isInUse()) {
				DcemTemplate newTemplate = new DcemTemplate();
				newTemplate.setContent(template.getContent());
				newTemplate.setDefaultTemplate(template.isDefaultTemplate());
				newTemplate.setLanguage(template.getLanguage());
				newTemplate.setName(template.getName());
				newTemplate.setId(null);
				newTemplate.setVersion(template.getVersion() + 1);
				newTemplate.setActive(true);
				newTemplate.setInUse(false);
				newTemplate.setTokens(getTokens(newTemplate.getContent()));
				template = getTemplate(template.getId()); // reload original from DB
				template.setActive(false);
				if (template.getName().equals(newTemplate.getName()) == false) {
					throw new DcemException(DcemErrorCodes.CANNOT_CHANGE_TEMPLATE_IN_USE, "Can't change tempalte name");
				}
				newTemplate.setLastModified(LocalDateTime.now());
				em.persist(newTemplate);
			} else {
				template = em.merge(template);
				template.setLastModified(LocalDateTime.now());
				template.setTokens(getTokens(template.getContent()));
				Exception exception = DcemUtils.reloadTaskNodes(TemplateLogic.class, TenantIdResolver.getCurrentTenantName(), template.getFullName());
				if (exception != null) {
					throw new DcemException(DcemErrorCodes.NODE_FAILED, "Can't uüdate all nodes");
				}
			}
		}
		if (withAudit) {
			auditingLogic.addAudit(dcemAction, template.toString());
		}
	}

	/**
	 * @param name
	 * @param locale
	 * @return
	 * @throws Exception
	 */
	public List<DcemTemplate> getActiveTemplates() throws Exception {
		TypedQuery<DcemTemplate> query = em.createNamedQuery(DcemTemplate.GET_TEMPLATES, DcemTemplate.class);
		return query.getResultList();
	}

	public DcemTemplate getDefaultTemplate(String name) {
		TypedQuery<DcemTemplate> query = em.createNamedQuery(DcemTemplate.GET_DEFAULT_TEMPLATE, DcemTemplate.class);
		query.setParameter(1, name);
		query.setMaxResults(1);
		try {
			return query.getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
	}

	@DcemTransactional
	public String getUpdateTemplateByName(Class<?> loadingClass, String[] names, SupportedLanguage[] languages, String scanPackages) {
		StringBuffer sb = new StringBuffer();
		for (String name : names) {
			for (SupportedLanguage language : languages) {
				DcemTemplate dcemTemplate = getTemplateByNameLanguage(name, language);
				if (dcemTemplate == null || dcemTemplate.getLanguage() != language) {
					try {
						String templateName = name + '_' + language.getLocale().getLanguage() + DcemConstants.TEMPLATE_TYPE;
						List<FileContent> templateFiles = ResourceFinder.find(loadingClass, scanPackages, templateName);
						if (templateFiles.isEmpty()) {
							sb.append("Couldn't add Tempalte: " + scanPackages + "/" + templateName);
							sb.append("\n");
							continue;
						}

						dcemTemplate = new DcemTemplate();
						dcemTemplate.setName(name);
						if (language == SupportedLanguage.English) {
							dcemTemplate.setDefaultTemplate(true);
						}
						dcemTemplate.setLanguage(language);
						dcemTemplate.setContent(StringUtils.getStringFromUtf8(templateFiles.get(0).getContent()));
						addOrUpdateTemplate(dcemTemplate, new DcemAction(AdminModule.MODULE_ID, null, DcemConstants.ACTION_ADD), false);
					} catch (Exception e) {
						sb.append("Couldn't add Tempalte " + name);
						sb.append("\n");
						logger.warn("Couldn't add Tempalte " + name, e);
					}
				}
			}
		}
		return sb.toString();
	}

	/**
	 * @param name
	 * @param locale
	 * @return
	 * @throws Exception
	 */
	public List<DcemTemplate> getTemplatesByName(String name, SupportedLanguage language) {
		TypedQuery<DcemTemplate> query = em.createNamedQuery(DcemTemplate.GET_TEMPLATES_BY_NAME_LOCALE, DcemTemplate.class);
		query.setParameter(1, name);
		query.setParameter(2, language);
		return query.getResultList();
	}

	public DcemTemplate getTemplateByNameLanguage(String name, SupportedLanguage language) {
		if (language == null) {
			return getDefaultTemplate(name);
		}
		List<DcemTemplate> list = getTemplatesByName(name, language);
		if (list.isEmpty()) {
			return getDefaultTemplate(name);
		}
		return list.get(0);
	}

	public List<SelectItem> getActiveTemplateSelection() throws Exception {
		List<DcemTemplate> templates = getActiveTemplates();
		LinkedList<SelectItem> selectItems = new LinkedList<>();
		TreeSet<String> set = new TreeSet<>();
		for (DcemTemplate template : templates) {
			set.add(template.getName());
		}
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			String name = iter.next();
			selectItems.add(new SelectItem(name, name));
		}
		return selectItems;
	}

	public List<String> getCompleteTemplateList(String name, int max) {
		TypedQuery<String> query = em.createNamedQuery(DcemTemplate.GET_FILTER_LIST, String.class);
		query.setParameter(1, name + "%");
		query.setMaxResults(max);
		try {
			return query.getResultList();
		} catch (Throwable exp) {
			logger.warn("Couldn't retriev Products.", exp);
			return null;
		}
	}

	/**
	 * @param content
	 * @return
	 */
	public LinkedList<String> getTokens(String content) {
		LinkedList<String> keys = new LinkedList<>();
		int ind = 0;
		int indEnd;
		while (true) {
			ind = content.indexOf("{{", ind);
			if (ind == -1) {
				break;
			} else {
				ind += 2;
				indEnd = content.indexOf("}}", ind);
				if (indEnd == -1) {
					break;
				}
				String token = content.substring(ind, indEnd);
				if (keys.contains(token) == false) {
					keys.add(token);
				}
			}
		}
		return keys;
	}

	public DcemTemplate getTemplate(int id) {
		return em.find(DcemTemplate.class, id);
	}

	@DcemTransactional
	public void setTemplateInUse(DcemTemplate template) {
		template.setInUse(true);
	}

	@Override
	public void reload(String templateName) throws DcemException {
		applicationBean.removeFreeMarkerTemplate(templateName);
	}

	@DcemTransactional
	public int updateAllTemplates() throws Exception {
		List<DcemTemplate> templates = getActiveTemplates();
		int count = updateTemplateModule (templates, AdminModule.class, DcemConstants.TEMPLATE_RESOURCES, DcemConstants.TEMPLATE_TYPE);
		for (DcemModule dcemModule : applicationBean.getSortedModules()) {
			if (dcemModule.isPluginModule()) {
				count = count + updateTemplateModule (templates, dcemModule.getClass(), DcemConstants.TEMPLATE_RESOURCES + "/" + dcemModule.getId(), DcemConstants.TEMPLATE_TYPE);
			}
		}
		return count;
	}
	
	private int updateTemplateModule (List<DcemTemplate> templates,  Class loadingClass, String pakageName, String templateType) throws Exception {
		List<FileContent> templateFiles = ResourceFinder.find(loadingClass, DcemConstants.TEMPLATE_RESOURCES, DcemConstants.TEMPLATE_TYPE);
		SupportedLanguage supportedLanguage;
		int count = 0;
		for (FileContent fileContent : templateFiles) {
			String fileName = fileContent.getName().substring(0, fileContent.getName().length() - DcemConstants.TEMPLATE_TYPE.length());
			String locale = fileName.substring(fileName.length() - 2);
			if (fileName.charAt(fileName.length() - 3) != '_') {
				logger.warn("Invalid Tempalte name format. " + fileName);
				continue;
			}
			fileName = fileName.substring(0, fileName.length() - 3);
			supportedLanguage = DcemUtils.getSuppotedLanguage(locale); // default is always english
			if (isNewTemplate(templates, fileName, supportedLanguage)) {
				DcemTemplate dcemTemplate = new DcemTemplate();
				dcemTemplate.setName(fileName);
				if (supportedLanguage == SupportedLanguage.English) {
					dcemTemplate.setDefaultTemplate(true);
				}
				dcemTemplate.setLanguage(supportedLanguage);
				dcemTemplate.setContent(new String(fileContent.getContent(), DcemConstants.CHARSET_UTF8));
				addOrUpdateTemplate(dcemTemplate, new DcemAction(AdminModule.MODULE_ID, null, DcemConstants.ACTION_ADD), false);
				count++;
			}
		}
		return count;
	}

	public boolean isNewTemplate(List<DcemTemplate> templates, String fileName, SupportedLanguage supportedLanguage) {
		boolean isNew = true;
		for (DcemTemplate template : templates) {
			if (template.getName().equals(fileName) && (template.getLanguage() == supportedLanguage)) {
				isNew = false;
				break;
			}
		}
		return isNew;
	}

}
