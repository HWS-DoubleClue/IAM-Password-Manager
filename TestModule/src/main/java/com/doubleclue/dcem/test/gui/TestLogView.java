package com.doubleclue.dcem.test.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.LazyDataModel;

import com.doubleclue.dcem.test.subjects. TestLogSubject;
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

@SuppressWarnings("serial")
@Named("testLogView")
@SessionScoped
public class TestLogView extends DcemView {

	@Inject
	private TestLogSubject testLogSubject;

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private TestLogDialog testLogDialog;  // small letters


	@Inject
	DcemApplicationBean applicationBean;

	@PostConstruct
	private void init() {
		subject = testLogSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE, operatorSessionBean.getLocale());		
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, testLogDialog, "/modules/test/TestLogDialog.xhtml");
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, testLogDialog, "/modules/test/TestLogDialog.xhtml");
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, testLogDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);		
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
    
//    public LazyDataModel<?> getLazyModel() {
//		if (lazyModel == null) {
//			lazyModel = new JpaLazyModel<>(em, this);
//		}
//		return lazyModel;
//	}
}
