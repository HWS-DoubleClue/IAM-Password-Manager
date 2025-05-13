package com.doubleclue.dcem.core.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.Visibility;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuElement;
import org.primefaces.model.menu.MenuModel;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.utils.KaraUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Named("viewNavigator")
@SessionScoped
public class ViewNavigator implements Serializable {

	
	public static Logger logger = LogManager.getLogger(ViewNavigator.class);

	final String COLUMN_TOGGLER = "CT-";

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	AutoViewBean autoViewBean;

	DcemModule activeModule = null;

	DcemView activeView = null;

	@Inject
	AdminModule adminModule;

	// private UIToolbar menuBar;

	private MenuModel menuModel = null;

	@PostConstruct
	public void init() {
		activeModule = applicationBean.getDefaultModule();
		activeView = activeModule.getDefaultView();
	}
	
	public void preRenderView() {
		HttpServletRequest  httpServletRequest =  (HttpServletRequest) JsfUtils.getExternalContext().getRequest();
		String gotoView = httpServletRequest.getParameter(DcemConstants.GOTO_VIEW);
		if (gotoView != null) {
			setActiveView(gotoView);
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1042847880204878575L;

	public boolean isActiveView(String name) {
		if (activeView != null && activeView.getSubject().getName().equals(name)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isModuleActive() {
		return (activeModule != null);
	}

	public String getViewPath() {
		if (activeView != null) {
			return activeView.getSubject().getPath();
		} else {
			// TODO
			return null;
		}
	}

	public String getUrlLink() {
		try {
			return createUrlLink(activeView, activeView.getShareUrlParameters());
		} catch (Exception e) {
			logger.error("",  e);
			return "error";
		}
	}

	public String createUrlLink(DcemView dcemView, Map<String, String> parameterMap) throws Exception {
		String url = applicationBean.getDcemManagementUrl(null) + "/" + DcemConstants.PRE_LOGIN_PAGE + DcemConstants.URL_VIEW
				+ dcemView.getSubject().getModuleId() + DcemConstants.MODULE_VIEW_SPLITTER + dcemView.getSubject().getViewName();
		if (parameterMap != null) {
			url = url + DcemConstants.URL_PARAMS + KaraUtils.mapToUrlParamString(parameterMap);
		}
		return url;
	}

	public void actionRedirectionToHome() {
		setActiveView(AdminModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + "welcomeView");
	}

	public String getActiveModulePath() {
		if (activeModule == null) {
			return "welcome.xhtml";
		}
		return "mgt/modules/" + activeModule.getId() + "/index.xhtml";
	}

	public boolean isActiveModule(String id) {
		if (activeModule != null && activeModule.getId().equals(id)) {
			return true;
		}
		return false;
	}

	public DcemModule getActiveModule() {
		return activeModule;
	}

	public void setActiveModule(DcemModule activeModule) {
		this.activeModule = activeModule;
	}

	public void setActiveDialog(AutoViewAction autoviewAction) {
		if (activeView == null) {
			return;
		} else {
			try {
				activeView.triggerAction(autoviewAction);
			} catch (Exception e) {
				JsfUtils.addErrorMessage(e.toString());
			}
		}
	}

	public String getDialogTitle() {
		if (activeView == null || activeView.getActiveDialog() == null) {
			return null;
		}
		return activeView.getActiveDialog().getTitle();
	}

	// public List<String> getIncludeDialogs() {
	// if (activeView == null) {
	// return new ArrayList<String>(0);
	// }
	// return activeView.getSubject().getIncludeDialogs();
	// }

	public DcemView getActiveView() {
		return activeView;
	}

	public StreamedContent getDownloadFile() {
		try {
			return activeView.excelExportFile();
		} catch (DcemException e) {
			logger.error(e.getMessage());
			return null;
		}
	}

	public List<AutoViewAction> getViewActions() {
		if (activeView == null) {
			return new ArrayList<AutoViewAction>(0);
		}
		return activeView.getViewActions();
	}

	// public boolean showDialog(String id) {
	// if (activeView == null) {
	// return false;
	// }
	// return activeView.isShowDialog(id);
	// }
	public void setActiveView(String moduleViewName) {
		String moduleId;
		String viewName;
		if (moduleViewName == null) {
			return;
		}
		int ind = moduleViewName.indexOf(DcemConstants.MODULE_VIEW_SPLITTER);
		if (ind == -1) {
			return;
		}
		moduleId = moduleViewName.substring(0, ind);
		viewName = moduleViewName.substring(ind + 1);
		if (activeView != null && activeView.getSubject().getViewName().equals(viewName) == false) {
			activeView.leavingView();
		}
		boolean found = false;
		if (menuModel != null) {
			for (MenuElement element : menuModel.getElements()) {
				if (element.getId().equals(moduleId)) {
					found = true;
					break;
				}
			}
			if (found == false) {
				logger.warn("No permission Module Name " + moduleId + " user:" + operatorSessionBean.getDcemUser().getLoginId());
				return;
			}
		}
		DcemModule module = applicationBean.getModule(moduleId);
		if (module == null) {
			logger.warn("Invalid Module Name " + moduleId + " user:" + operatorSessionBean.getDcemUser().getLoginId());
			return;
		}
		try {
			activeView = CdiUtils.getReference(viewName);
		} catch (Exception e) {
			logger.warn("Invalid View Name " + viewName, e);
			return;
		}
		activeModule = module;
		activeView.closeDialog();
		autoViewBean.switchView();
		List<Boolean> togglerList = new ArrayList<>();
		String value = operatorSessionBean.getUserSettings().get(COLUMN_TOGGLER + activeView.getClassName());
		if (value != null) {
			ObjectMapper objectMapper = new ObjectMapper();
			TypeReference<List<Boolean>> typeRef = new TypeReference<List<Boolean>>() {
			};
			try {
				togglerList = objectMapper.readValue(value, typeRef);
				for (int i = 0; i < activeView.getDisplayViewVariables().size(); i++) {
					ViewVariable viewVariable = activeView.getDisplayViewVariables().get(i);
					viewVariable.setVisible(togglerList.get(i));
				}
			} catch (Exception e) {
			}
		}
		activeView.reload();
		PrimeFaces.current().ajax().update("viewPart");
		PrimeFaces.current().executeScript("localStorage.setItem('mgtActiveView', '" + moduleId + DcemConstants.MODULE_VIEW_SPLITTER + viewName + "')");
		JsfUtils.refreshCurrentPage();
	}

	public void actionCloseDialog() {
		if (activeView != null) {
			activeView.closeDialog();
		}
	}

	public boolean isEditAction() {
		if (activeView != null) {
			DcemDialog dcemDialog = activeView.getActiveDialog();
			if (dcemDialog != null) {
				return dcemDialog.getAutoViewAction().getDcemAction().getAction().equals(DcemConstants.ACTION_EDIT);
			}
		}
		return false;
	}

	public boolean isAddAction() {
		if (activeView != null) {
			DcemDialog dcemDialog = activeView.getActiveDialog();
			if (dcemDialog != null) {
				return dcemDialog.getAutoViewAction().getDcemAction().getAction().equals(DcemConstants.ACTION_ADD);
			}
		}
		return false;
	}

	public void reload() {
		if (activeView != null) {
			activeView.reload();
		}
	}

	public String getConfirmTextHeader() {
		if (activeView == null || activeView.getActiveDialog() == null) {
			return null;
		}
		return activeView.getActiveDialog().getConfirmTextHeader();
	}

	public String getConfirmText() {
		if (activeView == null || activeView.getActiveDialog() == null) {
			return null;
		}
		return activeView.getActiveDialog().getConfirmText();
	}

	/**
	 * @return
	 */
	public MenuModel getMenuModel() {
		if (menuModel != null) {
			return menuModel;
		}
		menuModel = new DefaultMenuModel();
		DefaultMenuItem menuItem;
		for (DcemModule dcemModule : applicationBean.getSortedEnabledModules()) {
//			if (dcemModule.getId().equals("dm")) {
//				System.out.println("ViewNavigator.getMenuModel()");
//			}
			if (operatorSessionBean.isModulePermission(dcemModule.getId()) == false) {
				continue; // ignore if role has no module View or Manage Action.
			}
			if (dcemModule.isMasterOnly() && TenantIdResolver.isCurrentTenantMaster() == false) {
				continue;
			}
			SortedSet<SubjectAbs> subjects = applicationBean.getModuleSubjects(dcemModule);
			if (subjects != null) {
				DefaultSubMenu subMenu = DefaultSubMenu.builder().label(dcemModule.getName()).build();
				subMenu.setId(dcemModule.getId());
				boolean viewRights = operatorSessionBean
						.isPermission(new DcemAction(dcemModule.getId(), DcemConstants.EMPTY_SUBJECT_NAME, DcemConstants.ACTION_MANAGE))
						|| operatorSessionBean.isPermission(new DcemAction(dcemModule.getId(), DcemConstants.EMPTY_SUBJECT_NAME, DcemConstants.ACTION_VIEW));
				for (SubjectAbs subject : subjects) {
					if (subject.isHiddenMenu() == true) {
						continue;
					}
//					if (subject.getName().contains("DmStorage")) {
//						System.out.println("ViewNavigator.getMenuModel()");
//					}
					if (viewRights == false && operatorSessionBean.isPermission(subject.getDcemActions()) == false) {
						if (subject.forceView(operatorSessionBean.getDcemUser()) == false) {
							continue; // ignore if role has Actions for this subject
						}
					}

					menuItem = DefaultMenuItem.builder().value(subject.getDisplayName()).build();
					menuItem.setIcon(subject.getIconName());
					menuItem.setCommand(
							"#{viewNavigator.setActiveView('" + dcemModule.getId() + DcemConstants.MODULE_VIEW_SPLITTER + subject.getViewName() + "')}");
					// Create an ID on your Item menu:
					menuItem.setAjax(true);
					menuItem.setUpdate("viewPart");
					subMenu.getElements().add(menuItem);
				}
				if (subMenu.getElements().isEmpty() == false) {
					subMenu.setExpanded(false);
					menuModel.getElements().add(subMenu);
				}
			}
		}
		return menuModel;
	}

	public boolean isMessages() {
		return JsfUtils.isMessages();
	}

	public void setMenuModel(MenuModel menuModel) {
		this.menuModel = menuModel;
	}

	public String getActionIcon(AutoViewAction autoviewAction) {
		if (autoviewAction == null) {
			return null;
		}
		if (autoviewAction.getRawAction() == null) {
			return null;
		}
		return autoviewAction.getRawAction().getIcon();
	}

	public boolean isPredefinedFilters() {
		return activeView.isPredefinedFilters();
	}

	public List<SelectItem> getPredefinedFilterItems() {
		return activeView.getPredefinedFilterItems();
	}

	public int getPredefinedFilterId() {
		return activeView.getPredefinedFilterId();
	}

	public void setPredefinedFilterId(int id) {
		activeView.setPredefinedFilterId(id);
		return;
	}

	public void editPredefinedFilter() {
		activeView.editPredefinedFilter();
		return;
	}

	public String getPredefinedFilterTitle() {
		return activeView.getPredefinedFilterTitle();
	}

	public void onToggle(ToggleEvent event) {
		activeView.getDisplayViewVariables().get((Integer) event.getData()).setVisible(event.getVisibility() == Visibility.VISIBLE);
		activeView.getDisplayViewVariables().get(getPredefinedFilterId());
		List<Boolean> togglerList = new ArrayList<>();
		for (int i = 0; i < activeView.getDisplayViewVariables().size(); i++) {
			ViewVariable variable = activeView.getDisplayViewVariables().get(i);
			togglerList.add(variable.isVisible());
		}
		try {
			ObjectMapper mapper = new ObjectMapper();
			operatorSessionBean.setLocalStorageUserSetting(COLUMN_TOGGLER + activeView.getClassName(), mapper.writeValueAsString(togglerList));
		} catch (Exception e) {
		}
	}

	public void removeToggle() {
		try {
			operatorSessionBean.removeLocalStorageUserSetting(COLUMN_TOGGLER + activeView.getClassName());
		} catch (Exception e) {
		}
	}

	public LinkedList<SelectItem> getSupportedLanguages() {
		LinkedList<SelectItem> supportedLocales = new LinkedList<SelectItem>();
		for (SupportedLanguage supportedLocale : SupportedLanguage.values()) {
			if (supportedLocale.getLocale() != null) {
				supportedLocales.add(new SelectItem(supportedLocale.name(), supportedLocale.name()));
			}
		}
		return supportedLocales;
	}

	public LinkedList<SelectItem> getAllLanguages() {
		LinkedList<SelectItem> allLocales = new LinkedList<SelectItem>();
		TreeSet<String> languageSet = new TreeSet<String>();
		for (Locale locale : Locale.getAvailableLocales()) {
			languageSet.add(locale.getDisplayLanguage());
		}
		Iterator<String> languages = languageSet.iterator();
		String lang;
		while (languages.hasNext()) {
			lang = languages.next();
			if (lang.isEmpty()) {
				continue;
			}
			allLocales.add(new SelectItem(lang, lang));
		}
		return allLocales;
	}

	public String getTopComposition() {
		if (this.activeView.getTopComposition() != null) {
			return this.activeView.getTopComposition();
		}
		return "";
	}

	public boolean isGroupViewActive() {
		if (getActiveView().getSubject().getName().equals(DcemConstants.GROUP_VIEW)) {
			return true;
		}
		return false;
	}

}
