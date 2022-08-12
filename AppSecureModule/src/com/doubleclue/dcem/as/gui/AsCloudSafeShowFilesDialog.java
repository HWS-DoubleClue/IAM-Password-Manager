package com.doubleclue.dcem.as.gui;

import java.util.List;
import java.util.ResourceBundle;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.CloudSafeLimitEntity;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.GroupLogic;

@SuppressWarnings("serial")
@Named("asCloudSafeShowFilesDialog")
@SessionScoped
public class AsCloudSafeShowFilesDialog extends DcemDialog {

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	GroupLogic groupLogic;

	public List<CloudSafeEntity> getCloudSafeList() throws DcemException {
		DcemUser dcemUser = ((CloudSafeLimitEntity) getActionObject()).getUser();
		List<DcemGroup> allUsersGroups = groupLogic.getAllUserGroups(dcemUser, true);
		return cloudSafeLogic.getAllUserCloudSafe(dcemUser, allUsersGroups);
	}

	public String getUser() {
		return ((CloudSafeLimitEntity) getActionObject()).getUser().getLoginId();
	}

	public String getRecoveryKey() {
		DcemUser dcemUser = ((CloudSafeLimitEntity) getActionObject()).getUser();
		try {
			CloudSafeEntity cloudSafeEntity = cloudSafeLogic.getCloudSafe(CloudSafeOwner.USER, DcemConstants.RECOVERY_KEY, dcemUser, null, 0, null);
			return cloudSafeLogic.getContentAsString(cloudSafeEntity, null, null);
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			ResourceBundle asResourceBundle = JsfUtils.getBundle(AsModule.RESOURCE_NAME);
			return asResourceBundle.getString("error.noRecoveryKey");
		}
	}
}
