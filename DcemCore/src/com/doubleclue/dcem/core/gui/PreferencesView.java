package com.doubleclue.dcem.core.gui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.core.utils.DcemUtils;

@Named("preferencesView")
@SessionScoped
public class PreferencesView extends DcemView {

	private static final Logger logger = LogManager.getLogger(PreferencesView.class);

	@Inject
	ViewNavigator viewNavigator;

	@Inject
	AutoViewBean autoViewBean;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	ConfigLogic configLogic;
	
	
	@Inject
	AuditingLogic auditingLogic;

	FacesContext context;

	private static final long serialVersionUID = 3733919546663290317L;

	List<ViewVariable> viewVariables = null;
	private transient HtmlPanelGrid panelGrid;

	ModulePreferences modulePreferencesClone;

	ModulePreferences modulePreferencesPrevious;

	Object actionObject;

	String locale;

	public PreferencesView() {

	}

	public String getId() {
		return subject.getModuleId() + "." + viewNavigator.getActiveView().getSubject().getName();
	}

	@PostConstruct
	public void init() {
		// DcemModule dcemModule = viewNavigator.getActiveModule();
		// subject = semApplication.getSubjectByName(dcemModule.getId(), "Preferences");
	}

	public HtmlPanelGrid getPanelGrid() {
		ModulePreferences modulePreferences = viewNavigator.getActiveModule().getModulePreferences();
		if (modulePreferences == null) {
			return null;
		}
		try {
			modulePreferencesClone = (ModulePreferences) modulePreferences.clone();
			modulePreferencesPrevious = (ModulePreferences) modulePreferences.clone();
		} catch (CloneNotSupportedException e1) {
			logger.warn(e1);
			return null;
		}
		populatePanelGrid();
		return panelGrid;
	}

	private ResourceBundle getResourceBundle() {
		String resourceName = viewNavigator.getActiveModule().getResourceName();
		return JsfUtils.getBundle(resourceName);
	}

	private void getViewVariables(Object object) {
		actionObject = object;
		Class<?> klass = object.getClass();
		Field[] fields = klass.getDeclaredFields();
		viewVariables = new ArrayList<ViewVariable>();
		String viewName = viewNavigator.getActiveView().getSubject().getName();
		ResourceBundle resourceBundle = getResourceBundle();
		for (Field field : fields) {
			ViewVariable viewVariable = DcemUtils.convertFieldToViewVariable(field, resourceBundle, viewName, object);
			if (viewVariable == null) {
				continue;
			}
			viewVariables.add(viewVariable);
		}
	}

	void populatePanelGrid() {
		panelGrid = new HtmlPanelGrid();
		getViewVariables(modulePreferencesClone);
		try {
			panelGrid = DcemUtils.populateTable(viewVariables, panelGrid, modulePreferencesClone, "preferencesView.actionObject", true, null);
		} catch (Exception e) {
			logger.warn("Cannot populate the variables tables", e);
		}
		return;
	}

	public void setPanelGrid(HtmlPanelGrid panelGrid) {
		this.panelGrid = panelGrid;
	}

	public List<ViewVariable> getViewVariables() {
		if (viewVariables == null) {
			reload();
		}
		return viewVariables;
	}

	public void reload() {
		DcemModule dcemModule = viewNavigator.getActiveModule();
		subject = dcemApplication.getSubjectByName(dcemModule.getId(), "Preferences");
		panelGrid = null;
	}

	public void setViewVariables(List<ViewVariable> viewColumns) {
		this.viewVariables = viewColumns;
	}

	public String actionSave() {
		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
		Object selectedObject = modulePreferencesClone;
		if (selectedObject != null) {
			Set<ConstraintViolation<Object>> violations = validator.validate(selectedObject);
			if (violations.isEmpty() == false) {
				JsfUtils.violations(violations, viewNavigator.getActiveView().getSubject().getName(), viewNavigator.getActiveModule().getResourceName(), viewVariables);
				return null;
			}
		}

		try {
			viewNavigator.getActiveModule().preferencesValidation(modulePreferencesClone);
		} catch (DcemException exp) {
			JsfUtils.addWarningMessage(DcemConstants.CORE_RESOURCE, "preferencesView.save.warning", exp.getLocalizedMessage());
		} catch (Exception exp) {
			logger.error("Couldn't save preferencese", exp);
			JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "preferencesView.save.warning", exp.toString());
		}
		try {
			// save in DB
			configLogic.setModulePreferencesInCluster(viewNavigator.getActiveModule().getId(), modulePreferencesPrevious, modulePreferencesClone);
			String changeInfo;
			try {
				changeInfo = DcemUtils.compareObjects(modulePreferencesPrevious, modulePreferencesClone);
				DcemAction action = new DcemAction (viewNavigator.getActiveModule().getId(), "Preferences", DcemConstants.ACTION_SAVE);
				auditingLogic.addAudit(action, changeInfo);
			} catch (DcemException e) {
				logger.warn("Couldn't compare operator", e);
			}
		} catch (Exception exp) {
			JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "preferencesView.save.error", exp.getMessage());
			return null;
		}
		JsfUtils.addInformationMessage(DcemConstants.CORE_RESOURCE, "preferencesView.save.ok");

		return null;
	}

	public boolean hasPreferences() {
		DcemModule dcemModule = viewNavigator.getActiveModule();
		return dcemModule.getModulePreferences() != null;
	}

	public boolean isPermissionSave() {
		return operatorSessionBean.isPermission(new DcemAction (subject, DcemConstants.ACTION_SAVE));
	}

	public Object getActionObject() {
		return actionObject;
	}

	public void setActionObject(Object actionObject) {
		this.actionObject = actionObject;
	}
}
