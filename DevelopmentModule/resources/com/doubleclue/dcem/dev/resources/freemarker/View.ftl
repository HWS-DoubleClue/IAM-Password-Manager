package com.doubleclue.dcem.${ModuleId}.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.${ModuleId}.subjects. ${EntityName}Subject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;

@SuppressWarnings("serial")
@Named("${EntityNameVariable}View")
@SessionScoped
public class ${EntityName}View extends DcemView {

	@Inject
	private ${EntityName}Subject ${EntityNameVariable}Subject;

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private ${EntityName}Dialog ${EntityNameVariable}Dialog;  // small letters


	@Inject
	DcemApplicationBean applicationBean;

	@PostConstruct
	private void init() {
		subject = ${EntityNameVariable}Subject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE, operatorSessionBean.getLocale());		
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, ${EntityNameVariable}Dialog, ${ViewPath});
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, ${EntityNameVariable}Dialog, ${ViewPath});
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, ${EntityNameVariable}Dialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);		
	}

	/*
	* This method is called when the view is displayed or reloaded
	*
	*/
	@Override
	public void reload() {
		
	}

    @Override
	public Object createActionObject() {
		return super.createActionObject();
	}
}
