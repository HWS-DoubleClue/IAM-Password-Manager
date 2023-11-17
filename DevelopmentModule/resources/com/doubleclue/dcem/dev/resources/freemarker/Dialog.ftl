package com.doubleclue.dcem.${ModuleId}.gui;

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
import com.doubleclue.dcem.${ModuleId}.entities.${EntityName};
import com.doubleclue.dcem.${ModuleId}.logic.${EntityName}Logic;
import com.doubleclue.dcem.${ModuleId}.logic.${ModuleClass};
import java.util.List;
import java.util.ArrayList;
import javax.faces.model.SelectItem;
import java.util.ResourceBundle;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;

@Named("${EntityNameVariable}Dialog")
@SessionScoped
public class ${EntityName}Dialog extends DcemDialog {

	private Logger logger = LogManager.getLogger(${EntityName}Dialog.class);

	@Inject
	private ${EntityName}Logic ${EntityNameVariable}Logic;

	@Inject
	private ${EntityName}View ${EntityNameVariable}View;

	@Inject
	JpaLogic jpaLogic;
	
	@Inject
	OperatorSessionBean operatorSessionBean;
	
	${(dialogVariables)!}
	
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
		${EntityName} ${EntityNameVariable} = (${EntityName})this.getActionObject();
		${EntityNameVariable}Logic.addOrUpdate( ${EntityNameVariable}, this.getAutoViewAction().getDcemAction());
		return true;
	}

//	public String getHeight() {
//		return "650";
//	}

//	public String getWidth() {
//		return "1000";
//	}

	${(dialogMethods)!}
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
