package com.doubleclue.dcem.core.gui;

import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;

@Named("predefinedDialog")
@SessionScoped
public class PredefinedFilterDialog extends DcemDialog {

	private static final Logger logger = LogManager.getLogger(PredefinedFilterDialog.class);

	@Inject
	ViewNavigator viewNavigator;

	@Inject
	AutoViewBean autoViewBean;

	FacesContext context;

	private static final long serialVersionUID = 3733919546663290317L;

	transient ResourceBundle resourceBundle;

	public PredefinedFilterDialog() {
	}

	
	@PostConstruct
	public void init() {
		String resourceName = viewNavigator.getActiveModule().getResourceName();
		resourceBundle = JsfUtils.getBundle(resourceName);
	}

	public String getId() {
		return DcemConstants.AUTO_DIALOG_ID;
	}

	/**
	 * @return
	 */
	public boolean actionOk() {
		return true;  // close dialog
	}

	

	

}
