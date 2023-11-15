package com.doubleclue.dcem.dev.gui;

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
import com.doubleclue.dcem.dev.entities.TestEntity;
import com.doubleclue.dcem.dev.logic.TestEntityLogic;
import com.doubleclue.dcem.dev.logic.DevModule;
import java.util.List;
import java.util.ArrayList;
import javax.faces.model.SelectItem;
import java.util.ResourceBundle;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;

@Named("testEntity.Dialog")
@SessionScoped
public class TestEntityDialog extends DcemDialog {

	private Logger logger = LogManager.getLogger(TestEntityDialog.class);

	@Inject
	private TestEntityLogic testEntityLogic;

	@Inject
	private TestEntityView testEntityView;

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

		
		return true;
	}


//	public String getHeight() {
//		return "650";
//	}

//	public String getWidth() {
//		return "1000";
//	}

	public List<SelectItem> getDevObjectTypesEnums () {
				ResourceBundle resourceBundle = JsfUtils.getBundle(DevModule.RESOURCE_NAME, operatorSessionBean.getLocale());
				List<SelectItem> list = new ArrayList<>();
				for (com.doubleclue.dcem.dev.logic.DevObjectTypes enumObject :  com.doubleclue.dcem.dev.logic.DevObjectTypes.values()) {
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
		
	}

	public void leaving() {
		// clear some objects whcih are not needed
	}


	
}
