package com.doubleclue.dcem.setup.gui;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.event.FlowEvent;

import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.setup.logic.DbState;

@SuppressWarnings("serial")
@Named("setupWizard")
@SessionScoped
public class SetupWizard implements Serializable {

	@Inject
	DbView dbView;

	DcemView activeView = null;

	WizardState state = WizardState.dbConfig;

	@PostConstruct
	public void init() {
		activeView = dbView;
	}

	public String onFlowProcess(FlowEvent event) {
		state = WizardState.valueOf(event.getOldStep());

		switch (state) {
		case createSchema:
			if (event.getNewStep().equals(WizardState.createTables.name())) {
				if (dbView.getDbState() != DbState.Create_Tables_Required) {
					JsfUtils.addErrorMessage("Please create the Schema, before continue with next step.");
					state = WizardState.createSchema;
					break;
				}
			}
			state = WizardState.valueOf(event.getNewStep());

			break;
		case dbConfig:
			DbState dbState = dbView.getDbState();
			switch (dbState) {
			case Init:
				JsfUtils.addErrorMessage("Please 'Save', before continue with next step.");
				state = WizardState.dbConfig;
				break;

			case No_Connection:
			case Exception:
				JsfUtils.addErrorMessage("Please establish a connection to database, before continue with next step.");
				state = WizardState.dbConfig;
				break;
			case Create_Schema_Required:
				state = WizardState.valueOf(event.getNewStep());
				break;
			case Create_Tables_Required:
				state = WizardState.createTables;
				break;
			case OK:
				JsfUtils.addFacesInformationMessage("Setup is ready. Close setup application and start DCEM.");
				state = WizardState.dbConfig;
				break;
			case Migration_Required:
				state = WizardState.dbMigration;
				break;
			default:
				state = WizardState.valueOf(event.getNewStep());
				break;

			}
			break;
		default:
			state = WizardState.valueOf(event.getNewStep());

		}
		return state.name();
	}


	public boolean isActiveView(String name) {
		if (activeView != null && activeView.getSubject().getName().equals(name)) {
			return true;
		} else {
			return false;
		}
	}

	public String getViewPath() {
		if (activeView != null) {
			return activeView.getSubject().getPath();

		} else {
			// TODO
			return null;
		}

	}

	public void setActiveDialog(AutoViewAction autoviewAction) {
		if (activeView == null) {
			return;
		} else {
			activeView.triggerAction(autoviewAction);
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

	public void setActiveView(String viewName) {
		activeView = CdiUtils.getReference(viewName);
		activeView.closeDialog();
		activeView.setDirty(true);
		activeView.reload();
		JsfUtils.refreshCurrentPage();
	}

	public void actionCloseDialog() {
		if (activeView != null) {
			activeView.closeDialog();
		}
	}

	public String logoff() {
		ExternalContext extCon = FacesContext.getCurrentInstance().getExternalContext();
		HttpSession session = (HttpSession) extCon.getSession(true);
		session.invalidate();
		return "logoff";
	}

	public String getConfirmText() {
		if (activeView == null || activeView.getActiveDialog() == null) {
			return null;
		}
		return activeView.getActiveDialog().getConfirmText();
	}

	public String getActionIcon(AutoViewAction autoviewAction) {
		if (autoviewAction == null) {
			return null;
		}
		return autoviewAction.getRawAction().getIcon();
	}

}
