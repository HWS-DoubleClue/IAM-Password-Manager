package com.doubleclue.dcem.as.gui;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.dcem.as.entities.DeviceEntity;
import com.doubleclue.dcem.as.logic.AsDeviceLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.DeviceState;
import com.doubleclue.dcem.as.logic.DevicesUserDto;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.JpaLogic;

@SuppressWarnings("serial")
@Named("asDeviceDialog")
@SessionScoped
public class AsDeviceDialog extends DcemDialog {

	@Inject
	AsModule asModule;

	@Inject
	JpaLogic jpaLogic;

	@Inject
	AsDeviceLogic deviceLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	// ResourceBundle asResourceBundle;

	@PostConstruct
	private void init() {
		// asResourceBundle = JsfUtils.getBundle(AsModule.RESOUCE_NAME);
	}

	public boolean actionOk() throws Exception {
		return true;
	}

	@Override
	public void actionConfirm() {
		try {
			deviceLogic.deleteDevices(autoViewBean.getSelectedItems(), getAutoViewAction().getDcemAction());
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	public void enableDevice() {
		DeviceEntity device = (DeviceEntity) getActionObject();
		if (device.getState() == DeviceState.Enabled) {
			JsfUtils.addInformationMessage(AsModule.RESOURCE_NAME, "deviceIsEnabled");
		}
		try {
			device.setRetryCounter(0);
			device.setState(DeviceState.Enabled);
			deviceLogic.updateDeviceRc(device, getAutoViewAction().getDcemAction());
			JsfUtils.addInformationMessage(AsModule.RESOURCE_NAME, "deviceIsEnabled");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getMessage());
		}
	}

	public void showPushNotificationToken() {
		List<DevicesUserDto> list = new ArrayList<DevicesUserDto>(1);
		DeviceEntity device = (DeviceEntity) getActionObject();
		list.add(new DevicesUserDto(device.getId(), null, null));
		List<Integer> cloudSafes = null;
		try {
			cloudSafes = cloudSafeLogic.getCloudSafeFromIds(list, AppSystemConstants.PUSH_NOTIFICATION_TOKEN);
			if (cloudSafes != null && cloudSafes.size() == 1) {
				String pnToken = cloudSafeLogic.getContentAsStringWoChiper(cloudSafes.get(0));
				StringBuffer sb = new StringBuffer();
				int index = 0;
				while (index < pnToken.length()) {
					sb.append(pnToken.substring(index, Math.min(index + 32, pnToken.length())));
					index += 32;
					sb.append(" ");
				}
				JsfUtils.addInfoMessage("PushNotification Token: " + sb.toString());
			} else {
				JsfUtils.addWarnMessage("No PushNotification Token found");
			}
		} catch (DcemException e) {
			JsfUtils.addErrorMessage("Something went wrong. " + e.toString());
			return;
		}
	}

	public void disableDevice() {
		DeviceEntity device = (DeviceEntity) getActionObject();
		try {
			deviceLogic.setDeviceState(device.getId(), DeviceState.Disabled);
			JsfUtils.addInformationMessage(AsModule.RESOURCE_NAME, "deviceIsDisabled");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getMessage());
		}
	}

}
