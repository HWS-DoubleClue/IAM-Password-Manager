package com.doubleclue.dcem.petshop.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.GenericDcemDialog;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.petshop.logic.PetshopModule;
import com.doubleclue.dcem.petshop.subjects.PetAssetsSubject;
import com.doubleclue.dcem.petshop.subjects.PetOrderSubject;

@SuppressWarnings("serial")
@Named("petOrderView")
@SessionScoped
public class PetOrderView extends DcemView {
	
	private Logger logger = LogManager.getLogger(PetOrderView.class);

	@Inject
	private PetOrderSubject petOrderSubject;

	@Inject
	PetshopModule petshopModule;

	
	@Inject
	OperatorSessionBean operatorSessionBean;
	
	@Inject
	private GenericDcemDialog genericDcemDialog;


	@PostConstruct
	public void init() {
		genericDcemDialog.setParentView(this);
		subject = petOrderSubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(PetshopModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, genericDcemDialog, null);
	//	addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, genericDcemDialog, null);
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, genericDcemDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);
	}

	

	
}
