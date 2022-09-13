package com.doubleclue.dcem.admin.gui;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewNavigator;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;

@SuppressWarnings("serial")
@Named("mfaLoginView")
@SessionScoped
public class MfaLoginView extends LoginViewAbstract {

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	UserLogic userLogic;
	
	@Inject
	ViewNavigator viewNavigator;

	private static Logger logger = LogManager.getLogger(MfaLoginView.class);

	AtomicBoolean loginReEnter = new AtomicBoolean(false);
	
	@PostConstruct
	public void init() {
		super.init();
		progress = 0;
		stopQrCode = true;
		connectionServicesType = ConnectionServicesType.MANAGEMENT;
	}

	public void actionLogin() {
		super.actionLogin();
	}

	@Override
	public void finishLogin() throws DcemException {
		super.finishLogin();
		if (dcemUser == null) {
			dcemUser = userLogic.getUser(userLoginId);
		} else {
			dcemUser = userLogic.getUser(dcemUser.getId());
		}
		try { 
			operatorSessionBean.loggedInOperator(dcemUser, null);
			if (mgtUrlView != null && mgtUrlView.isEmpty() == false) {
				viewNavigator.setActiveView(mgtUrlView);
			} else {
				viewNavigator.setActiveView(mgtActiveView);
			}
			FacesContext fc = FacesContext.getCurrentInstance();
			ExternalContext ec = fc.getExternalContext();
			((HttpServletRequest) ec.getRequest()).changeSessionId(); // avoid session hijacking
			ec.redirect("index.xhtml");
		} catch (DcemException e) {
			throw e;
		} catch (Exception e) {
			logger.warn("finishLogin", e);
			JsfUtils.addErrorMessage(e.getMessage());
		}
	}

	@Override
	public void showChangePassword() {
		loginPanelRendered = false;
		passwordPanelRendered = true;
		PrimeFaces.current().ajax().update("loginForm");
	}


}
