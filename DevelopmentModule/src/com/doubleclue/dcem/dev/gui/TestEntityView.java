package com.doubleclue.dcem.dev.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.dev.subjects. TestEntitySubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;

@SuppressWarnings("serial")
@Named("testEntityView")
@SessionScoped
public class TestEntityView extends DcemView {

	@Inject
	private TestEntitySubject testEntitySubject;

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private TestEntityDialog testEntityDialog;  // small letters


	@Inject
	DcemApplicationBean applicationBean;

	@PostConstruct
	private void init() {
		subject = testEntitySubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE, operatorSessionBean.getLocale());		
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, testEntityDialog, "/modules/dev/TestEntity.xhtml");
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, testEntityDialog, "/modules/dev/TestEntity.xhtml");
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, testEntityDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);		
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
