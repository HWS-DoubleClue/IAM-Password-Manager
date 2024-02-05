package com.doubleclue.dcem.admin.gui;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewNavigator;
import com.doubleclue.dcem.core.jpa.JpaEntityCacheLogic;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.utils.StringUtils;

@SuppressWarnings("serial")
@Named("userPasswordDialog")
@SessionScoped
public class UserPasswordDialog extends DcemDialog {

	private Logger logger = LogManager.getLogger(UserPasswordDialog.class);

	@Inject
	UserLogic userLogic;

	@Inject
	DomainLogic domainLogic;

	@Inject
	GroupLogic groupLogic;

	@Inject
	AdminModule adminModule;

	@Inject
	JpaLogic jpaLogic;

	@Inject
	RoleLogic roleLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	JpaEntityCacheLogic jpaEntityCacheLogic;

	@Inject
	ViewNavigator viewNavigator;

	@Inject
	DcemApplicationBean applicationBean;

	private String passwordOld = "";
	private String passwordNew;

	public void actionSave () {
		try {
			userLogic.changePassword(operatorSessionBean.getDcemUser(), passwordOld, passwordNew);
			passwordOld = null;
			passwordNew = null;
			actionCloseDialog();
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		} catch (Exception e) {
			logger.error("", e);
			JsfUtils.addErrorMessage(e.getMessage());
		}
		return;
	}

	public String getPasswordOld() {
		return passwordOld;
	}

	public void setPasswordOld(String passwordOld) {
		this.passwordOld = passwordOld;
	}

	public String getPasswordNew() {
		return passwordNew;
	}

	public void setPasswordNew(String passwordNew) {
		this.passwordNew = passwordNew;
	}
	
	public void actionCloseDialog () {
		PrimeFaces.current().dialog().closeDynamic(null);
	}

}
