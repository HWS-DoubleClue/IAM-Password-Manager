package com.doubleclue.dcem.as.gui;

import java.util.Date;
import java.util.List;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.SendByEnum;
import com.doubleclue.dcem.as.entities.ActivationCodeEntity;
import com.doubleclue.dcem.as.logic.AsActivationLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.UserLogic;

@SuppressWarnings("serial")
@Named("asActivationDialog")
@SessionScoped
public class AsActivationDialogBean extends DcemDialog {
	
	private static Logger logger = LogManager.getLogger(AsActivationDialogBean.class);

	@Inject
	Conversation conversation;

	@Inject
	AsActivationLogic activationLogic;

	@Inject
	private UserLogic userLogic;

	String loginId;

	SendByEnum sendBy;

	String domainName;

	@Override
	public boolean actionOk() throws Exception {

		ActivationCodeEntity actionObject = (ActivationCodeEntity) this.getActionObject();

		try {
			DcemUser dcemUser = userLogic.getDistinctUser(loginId);
			if (dcemUser == null) {
				JsfUtils.addErrorMessage(AsModule.RESOURCE_NAME, "activationDialog.invalidUser");
				return false;
			}
			Date date = actionObject.getValidTill();
			if (date == null) {
				JsfUtils.addErrorMessage(AsModule.RESOURCE_NAME, "activationDialog.invalidUseTillUser");
				return false;
			}
			if (date.before(new Date())) {
				JsfUtils.addErrorMessage(AsModule.RESOURCE_NAME, "activationDialog.invalidUseTillUser");
				return false;
			}
			actionObject.setUser(dcemUser);
			activationLogic.addUpdateActivationCode(actionObject, this.getAutoViewAction().getDcemAction(), sendBy, true);

			JsfUtils.addInformationMessage(AsModule.RESOURCE_NAME, "activationDialog.success", actionObject.getActivationCode());

			actionObject.setActivationCode(null); // reset the activation-code so that it is not repeated
			if (this.getAutoViewAction().getDcemAction().equals(DcemConstants.ACTION_ADD)) {
				return false;
			}
			return true;

		} catch (DcemException exp) {
			logger.info(exp);
			JsfUtils.addErrorMessage(exp.getLocalizedMessage() + " (" + exp.getMessage() + ")");
			return false;

		} catch (Exception exp) {
			logger.info(exp);
			JsfUtils.addErrorMessage(AsModule.RESOURCE_NAME, "exception.error", exp.getMessage());
			return false;
		}
	}
	
	public void changeDomain() {
		loginId = null;
	}

	public List<String> completeUser(String name) {
		if (domainName == null || domainName.isEmpty()) {
			return userLogic.getCompleteUserList(name, 50);
		} else {
			return userLogic.getCompleteUserList(domainName  + DcemConstants.DOMAIN_SEPERATOR +  name, 50);
		}
	}

	public String getLoginId() {
		ActivationCodeEntity activationCode = (ActivationCodeEntity) getActionObject();
		if (activationCode.getUser() == null) {
			return null;
		}
		loginId = activationCode.getUser().getLoginId();
		return loginId;
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public String getActivationCode() {
		ActivationCodeEntity activationCode = (ActivationCodeEntity) this.getActionObject();
		return activationCode.getActivationCode();
	}

	public SendByEnum getSendBy() {
		return sendBy;
	}

	public void setSendBy(SendByEnum sendBy) {
		this.sendBy = sendBy;
	}

	public SendByEnum[] getSendByValues() {
		return SendByEnum.values();
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	
	@Override
	public String getHeight() {
		return "350px";
	}
	
	
}
