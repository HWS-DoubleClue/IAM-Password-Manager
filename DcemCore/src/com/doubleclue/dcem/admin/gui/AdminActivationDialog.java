package com.doubleclue.dcem.admin.gui;

import java.time.LocalDateTime;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.SendByEnum;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AsModuleApi;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;

@Named("adminActivationDialog")
@SessionScoped
public class AdminActivationDialog extends DcemDialog {

	@Inject
	AdminModule adminModule;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	GroupLogic groupLogic;

	LocalDateTime validTill;

	SendByEnum sendBy;

	AsModuleApi asModuleApi;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PostConstruct
	private void init() {
		asModuleApi = (AsModuleApi) CdiUtils.getReference(DcemConstants.AS_MODULE_API_IMPL_BEAN);
		if (asModuleApi != null) {
			validTill = asModuleApi.getActivationCodeDefaultValidTill();
		}
	}

	public boolean actionOk() throws Exception {
		List<DcemUser> users;
		if (validTill == null) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "activationDialog.invalidUseTillUser");
			return false;
		}   
		if (validTill.isBefore(LocalDateTime.now())) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "activationDialog.invalidUseTillUser");
			return false;
		}
		List<Object> objects = this.getSelection();
		if (objects.get(0) instanceof DcemUser) {
			for (Object object : objects) {
				try {
					asModuleApi.createActivationCode((DcemUser) object, validTill, sendBy, "");
				} catch (DcemException e) {
					JsfUtils.addErrorMessage(e.getLocalizedMessage());
					return false;
				}
			}
		} else {
			DcemGroup dcemGroup;
			for (Object object : objects) {
				dcemGroup = (DcemGroup) object;
				try {
					users = groupLogic.getMembers(dcemGroup);
				} catch (DcemException e) {
					if (e.getErrorCode() == DcemErrorCodes.DOMAIN_DISABLED) {
						JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "ldap.error.disabled", e.getMessage());
						continue;
					}
					JsfUtils.addErrorMessage(e.toString());
					continue;
				}

				for (DcemUser user : users) {
					try {
						asModuleApi.createActivationCode(user, validTill, sendBy, "");
					} catch (DcemException e) {
						JsfUtils.addErrorMessage(e.toString());
					}
				}
			}
		}

		return true;
	}

	public void leavingDialog() {

	}

	public LocalDateTime getValidTill() {
		return validTill;
	}

	public void setValidTill(LocalDateTime validTill) {
		this.validTill = validTill;
	}

	public SendByEnum[] getSendByValues() {
		return SendByEnum.values();
	}

	public SendByEnum getSendBy() {
		return sendBy;
	}

	public void setSendBy(SendByEnum sendBy) {
		this.sendBy = sendBy;
	}

}
