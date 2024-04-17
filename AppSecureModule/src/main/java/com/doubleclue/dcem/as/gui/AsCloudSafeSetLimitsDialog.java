package com.doubleclue.dcem.as.gui;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.as.entities.CloudSafeLimitEntity;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.DataTuple;
import com.doubleclue.dcem.as.logic.DataUnit;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewNavigator;
import com.doubleclue.dcem.core.logic.UserLogic;

@SuppressWarnings("serial")
@Named("asCloudSafeSetLimitsDialog")
@SessionScoped
public class AsCloudSafeSetLimitsDialog extends DcemDialog {

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	ViewNavigator viewNavigator;

	private double limitSize;
	private DataUnit selectedDataUnit;

	private DcemUser dcemUser;
	private LocalDateTime expiryDate;
	private boolean psEnabled;

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		super.show(dcemView, autoViewAction);
		dcemUser = null;
		String action = autoViewAction.getDcemAction().getAction();
		if (action.equals(DcemConstants.ACTION_ADD)) {
			DataTuple dt = DataUnit.getByteCountAsTuple(cloudSafeLogic.getDefaultUserLimit());
			limitSize = Math.floor(dt.getSize() * 100) / 100; // truncate to 2 decimal places
			selectedDataUnit = dt.getUnit();
			expiryDate = null;
			psEnabled = cloudSafeLogic.getDefaultPasswordSafeEnabled();
		} else if (action.equals(DcemConstants.ACTION_EDIT)) {
			List<Object> selection = getSelection();
			CloudSafeLimitEntity firstEntity = (CloudSafeLimitEntity) selection.get(0);
			dcemUser = firstEntity.getUser();
			psEnabled = firstEntity.isPasswordSafeEnabled();
			long largestLimit = firstEntity.getLimit();
			LocalDateTime latestExpiryDate = firstEntity.getExpiryDate();
			if (selection.size() > 1) {
				for (Object object : selection.subList(1, selection.size())) {
					CloudSafeLimitEntity entity = (CloudSafeLimitEntity) object;
					if (entity.getLimit() > largestLimit) {
						largestLimit = entity.getLimit();
					}
					if (latestExpiryDate != null) {
						if (entity.getExpiryDate() == null) {
							latestExpiryDate = null;
						} else if (entity.getExpiryDate().isAfter(latestExpiryDate)) {
							latestExpiryDate = entity.getExpiryDate();
						}
					}
				}
			}
			DataTuple dt = DataUnit.getByteCountAsTuple(largestLimit);
			expiryDate = latestExpiryDate;
			limitSize = Math.floor(dt.getSize() * 100) / 100; // truncate to 2 decimal places
			selectedDataUnit = dt.getUnit();
		}
	}

	@Override
	public boolean actionOk() throws Exception {
		long limit = DataUnit.getByteCount(limitSize, selectedDataUnit);
		List<Integer> userIds = new ArrayList<>();
		if (viewNavigator.isAddAction()) {
				userIds.add(dcemUser.getId());
		} else {
			List<Object> selection = getSelection();
			for (Object actionObject : selection) {
				CloudSafeLimitEntity entity = (CloudSafeLimitEntity) actionObject;
				DcemUser user = entity.getUser();
				if (entity.getUsed() > limit) {
					JsfUtils.addErrorMessage("User '" + user.getLoginId() + "' has already used up " + DataUnit.getByteCountAsString(entity.getUsed())
							+ ", which is more than the new limit. Please set a higher limit or exclude this user from the selection.");
					return false;
				}
				userIds.add(user.getId());
			}
		}
		try {
			cloudSafeLogic.setCloudSafeLimits(userIds, limit, expiryDate, psEnabled);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getMessage());
			return false;
		}
		viewNavigator.getActiveView().closeDialog();
		return true;
	}

	public DataUnit[] getDataUnits() {
		return DataUnit.values();
	}

	public double getLimitSize() {
		return limitSize;
	}

	public void setLimitSize(double limitSize) {
		this.limitSize = limitSize;
	}

	public DataUnit getSelectedDataUnit() {
		return selectedDataUnit;
	}

	public void setSelectedDataUnit(DataUnit selectedDataUnit) {
		this.selectedDataUnit = selectedDataUnit;
	}

	public LocalDateTime getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(LocalDateTime expiryDate) {
		this.expiryDate = expiryDate;
	}

	public boolean isPsEnabled() {
		return psEnabled;
	}

	public void setPsEnabled(boolean psEnabled) {
		this.psEnabled = psEnabled;
	}

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
	}
}
