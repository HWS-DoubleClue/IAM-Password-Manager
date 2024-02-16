package com.doubleclue.dcem.admin.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.subjects.GroupSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewVariable;
import com.doubleclue.dcem.core.jpa.FilterOperator;
import com.doubleclue.dcem.core.jpa.FilterProperty;
import com.doubleclue.dcem.core.jpa.JpaLazyModel;
import com.doubleclue.dcem.core.jpa.VariableType;

@Named("groupView")
@SessionScoped
public class GroupView extends DcemView {

	@Inject
	private GroupSubject groupSubject;

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private GroupDialogBean groupDialog;

	@Inject
	private GroupMembersDialogBean groupMembersDialogBean;
	
	@Inject
	private AdminActivationDialog adminActivationDialog;
	
	@Inject
	DcemApplicationBean applicationBean;

	private static final long serialVersionUID = 1L;

	@PostConstruct
	private void init() {	

		groupDialog.setParentView(this);
		adminActivationDialog.setParentView(this);
		subject = groupSubject;

		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		ResourceBundle adminResourceBundle = JsfUtils.getBundle(AdminModule.RESOURCE_NAME);


		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, groupDialog, DcemConstants.GROUP_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, groupDialog, DcemConstants.GROUP_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_MEMBERS, resourceBundle, groupMembersDialogBean, DcemConstants.GROUP_MEMBERS_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, groupDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
		
		if (applicationBean.getModule(DcemConstants.AS_MODULE_ID) != null) {
			addAutoViewAction(DcemConstants.CREATE_ACTIVATION_CODE, adminResourceBundle, adminActivationDialog, DcemConstants.SHOW_ACTIVATION_CODE_DIALOG);
		}
	}
	
	public LazyDataModel<?> getLazyModel() {
		if (lazyModel == null) {
			lazyModel = new JpaLazyModel<>(em, this);
			for (ViewVariable viewVariable : getViewVariables()) {
				if (viewVariable.getId().equals("name")) {
					lazyModel.addPreFilterProperties(
							new FilterProperty(viewVariable.getAttributes(), DcemConstants.GROUP_ROOT, null, VariableType.STRING, FilterOperator.NOT_EQUALS));
					break;
				}
			}
		}
		return lazyModel;
	}

	@Override
	public void reload() {
		autoViewBean.reload();
	}
}
