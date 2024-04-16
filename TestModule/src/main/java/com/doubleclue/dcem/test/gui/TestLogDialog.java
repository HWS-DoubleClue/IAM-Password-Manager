package com.doubleclue.dcem.test.gui;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.test.entities.TestLog;
import com.doubleclue.dcem.test.logic.TestLogLogic;
import com.doubleclue.dcem.test.logic.TestModule;
import java.util.List;
import java.util.ArrayList;
import javax.faces.model.SelectItem;
import java.util.ResourceBundle;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;

@Named("testLogDialog")
@SessionScoped
public class TestLogDialog extends DcemDialog {

	private Logger logger = LogManager.getLogger(TestLogDialog.class);

	@Inject
	private TestLogLogic testLogLogic;

	@Inject
	private TestLogView testLogView;

	@Inject
	JpaLogic jpaLogic;
	
	@Inject
	OperatorSessionBean operatorSessionBean;
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

/*
* This method is called on OK button
* @return true to close the dialog 
* 
*/
	@Override
	public boolean actionOk() throws Exception {
		TestLog testLog = (TestLog)this.getActionObject();
		testLogLogic.addOrUpdate( testLog, this.getAutoViewAction().getDcemAction());
		return true;
	}

//	public String getHeight() {
//		return "650";
//	}

//	public String getWidth() {
//		return "1000";
//	}

	public List<SelectItem> getActionEnums () {
				ResourceBundle resourceBundle = JsfUtils.getBundle(TestModule.RESOURCE_NAME, operatorSessionBean.getLocale());
				List<SelectItem> list = new ArrayList<>();
				for (com.doubleclue.dcem.test.logic.TestLogAction enumObject :  com.doubleclue.dcem.test.logic.TestLogAction.values()) {
					 list.add (new SelectItem (enumObject.name() , JsfUtils.getStringSafely(resourceBundle, enumObject.name())));
				}
				return list;
}

/*
* This method is called before Dialog is opened
* 
*/
	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		String action = this.getAutoViewAction().getDcemAction().getAction();
		/*
		*  Initialze the local variables
		*/
		
	}

	public void leaving() {
		/*
		*  clear local variables
		*/
	}
}
