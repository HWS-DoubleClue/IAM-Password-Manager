package com.doubleclue.dcem.core.gui;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.utils.DcemUtils;

@Named("autoDialog")
@SessionScoped
public class AutoDialogBean implements Serializable {

	private static final Logger logger = LogManager.getLogger(AutoDialogBean.class);

	@Inject
	ViewNavigator viewNavigator;

	@Inject
	AutoViewBean autoViewBean;

	FacesContext context;

	private static final long serialVersionUID = 3733919546663290317L;

	private transient HtmlPanelGrid panelGrid;

	// Class<?> klass;

	ArrayList<ViewVariable> viewVariables;

	public AutoDialogBean() {
	}

	private ResourceBundle getResourceBundle() {
		String resourceName = viewNavigator.getActiveModule().getResourceName();
		return JsfUtils.getBundle(resourceName);
	}

	private void getViewVariables(Object object) {
		Class<?> klass = object.getClass();
		Field[] fields = klass.getDeclaredFields();
		viewVariables = new ArrayList<ViewVariable>();
		String viewName = viewNavigator.getActiveView().getSubject().getName();
		ResourceBundle resourceBundle = getResourceBundle();
		for (Field field : fields) {
			// System.out.println("AutoDialogBean.getViewVariables() " + field.getName());
			ViewVariable viewVariable = DcemUtils.convertFieldToViewVariable(field, resourceBundle, viewName, object);
			if (viewVariable == null) {
				continue;
			}
			viewVariables.add(viewVariable);
		}
	}

	@PostConstruct
	public void init() {
	}

	public String getId() {
		return DcemConstants.AUTO_DIALOG_ID;
	}

	// public Class<?> getKlass() {
	// return klass;
	// }
	//
	// public void setKlass(Class<?> klass) {
	// this.klass = klass;
	// }

	public ArrayList<ViewVariable> getViewVariables() {
		return viewVariables;
	}

	public void setViewVariables(ArrayList<ViewVariable> viewVariables) {
		this.viewVariables = viewVariables;
	}

	public HtmlPanelGrid getPanelGrid() {
		if (panelGrid == null) {
			panelGrid = new HtmlPanelGrid();
			// populatePanelGrid();
		}
		return panelGrid;
	}

	public void populatePanelGrid() {

		String bind;
		Object object = getActionSubObject();
		if (object != null) {
			getViewVariables(object);
			bind = "autoDialog.actionSubObject";
		} else {
			object = getActionObject();
			getViewVariables(object);
			bind = "autoDialog.actionObject";
		}

		panelGrid = null;
		try {
			panelGrid = DcemUtils.populateTable(viewVariables, getPanelGrid(), object, bind, false, viewNavigator.getActiveView().getActiveDialog());
		} catch (Exception e) {
			logger.warn("Cannot populate the variables tables", e);
		}
		return;
	}

	public void setPanelGrid(HtmlPanelGrid panelGrid) {
		this.panelGrid = panelGrid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 */
	public Object getActionObject() {
		return viewNavigator.getActiveView().getActionObject();
	}

	public void setActionObject(Object actionObject) {
		viewNavigator.getActiveView().setActionObject(actionObject);
	}

	public Object getActionSubObject() {
		return viewNavigator.getActiveView().getActionSubObject();
	}

	public void setActionSubObject(Object actionSubObject) {
		viewNavigator.getActiveView().setActionSubObject(actionSubObject);
	}

	/**
	 * @return
	 */
	public void actionOk() {
		/*
		 * For some reason Validatin is not done in JSF, so we have to do it on oour
		 * own.
//		 */
		// Could be that there wa a bug in JSF. It looks as if validation is working.
//		Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
//		Object selectedObject = viewNavigator.getActiveView().getActionObject();
//		if (selectedObject != null) {
//			Set<ConstraintViolation<Object>> violations = validator.validate(selectedObject);
//			if (violations.isEmpty() == false) {
//				JsfUtils.violations(violations, viewNavigator.getActiveView().getSubject().getName(), viewNavigator.getActiveModule().getResourceName(), viewVariables);
//				return;
//			}
//		}

		DcemDialog dcemDialog = viewNavigator.getActiveView().getActiveDialog();
		if (dcemDialog == null) {
			JsfUtils.addErrorMessage("No open dialog found");
			return;
		}
		
		try {
			if (dcemDialog.actionOk() == false) {
				return; // don't close dialog
			}
		} catch (DcemException dcemExp) {
			logger.debug("OK Action Failed", dcemExp);
			switch (dcemExp.getErrorCode()) {
			case VALIDATION_CONSTRAIN_VIOLATION:
				Set<ConstraintViolation<?>> set = ((javax.validation.ConstraintViolationException) dcemExp.getCause()).getConstraintViolations();
				for (ConstraintViolation<?> entry : set) {
					String path = entry.getPropertyPath().toString();
					JsfUtils.addErrorMessage(Character.toUpperCase(path.charAt(0)) + path.substring(1) + ": " + entry.getMessage());
				}
				return;
			case CONSTRAIN_VIOLATION_DB:
				JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "db.constrain.at.insert", (Object[]) null);
				return;
			case CONSTRAIN_VIOLATION:
				JsfUtils.addErrorMessage("Verification Error: " + dcemExp.getMessage());
				return;
			case EXCEPTION:
				JsfUtils.addErrorMessage("Something went wrong. Cause: " + dcemExp.getMessage());
				return;
			default:
				JsfUtils.addErrorMessage(dcemExp.getLocalizedMessage());
				return;
			}

		} catch (Exception exp) {
			ConstraintViolationException constrainViolation = DcemUtils.getConstainViolation(exp);
			if (constrainViolation != null) {
				JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "db.constrain.at.insert", (Object[]) null);
			} else {
				JsfUtils.addErrorMessage(exp.toString());
				logger.warn("OK Action Failed", exp);
			}
			return;
		}

		// if (JsfUtils.getMaximumSeverity() < FacesMessage.SEVERITY_WARN.getOrdinal() )
		// {
		viewNavigator.getActiveView().closeDialog();
		JsfUtils.addFacesInformationMessage("successful", "mainMessages");
		return;
	}

	/**
	 * @return
	 */
	public String actionConfirm() {
		try {
			viewNavigator.getActiveView().getActiveDialog().actionConfirm();
		} catch (DcemException semExp) {
			if (semExp.getErrorCode() == DcemErrorCodes.CONSTRAIN_VIOLATION_DB) {
				JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "db.constrain.at.delete", (Object[]) null);
				return null;
			}
			JsfUtils.addErrorMessage(semExp.getErrorCode().name() + ":" + semExp.getMessage());
			logger.warn("Delete Node Failed", semExp);

		} catch (Exception exp) {
			if (exp.getCause() instanceof PersistenceException) {
				JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "db.constrain.at.delete", (Object[]) null);
			} else {
				JsfUtils.addErrorMessage(exp.toString());
			}
		}

		if (JsfUtils.getMaximumSeverity() <= 0) {
			viewNavigator.getActiveView().closeDialog();
			JsfUtils.addFacesInformationMessage(JsfUtils.getStringFromBundle("delete.successful"), "indexForm:viewMessage"); // TODO
																																// doesn't
																																// work.
		}
		return null;
	}

}
