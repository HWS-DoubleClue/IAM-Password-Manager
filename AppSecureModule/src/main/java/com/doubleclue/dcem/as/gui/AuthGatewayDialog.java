package com.doubleclue.dcem.as.gui;

import java.nio.ByteBuffer;

import javax.enterprise.context.Conversation;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.comm.thrift.AuthGatewayConfig;
import com.doubleclue.dcem.admin.logic.SendByEnum;
import com.doubleclue.dcem.as.entities.AuthGatewayEntity;
import com.doubleclue.dcem.as.logic.AsAuthGatewayLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.utils.SecureUtils;
import com.doubleclue.utils.ThriftUtils;

@SuppressWarnings("serial")
@Named("authAppDialog")
@SessionScoped
public class AuthGatewayDialog extends DcemDialog {

	@Inject
	Conversation conversation;

	@Inject
	private AsAuthGatewayLogic asAuthAppLogic;

	String loginId;

	SendByEnum sendBy;

	@Override
	public boolean actionOk() throws Exception {
		AuthGatewayEntity authAppEntity = (AuthGatewayEntity) getActionObject();
		if (authAppEntity.getName() == null) {
			JsfUtils.addErrorMessage(AsModule.RESOURCE_NAME, "asAuthNameEmpty");
			return false;
		}
		asAuthAppLogic.addOrUpdateAuthApp(authAppEntity, getAutoViewAction().getDcemAction());
		return true;
	}

	public boolean actionDownload() throws Exception {
		AuthGatewayEntity authAppEntity = (AuthGatewayEntity) getActionObject();
		if (authAppEntity.isDisabled()) {
			JsfUtils.addErrorMessage("AuthApp is disabled");
			return false;
		}
//		byte[] sdkConfig;
//		try {
//			FileInputStream fileInputStream = new FileInputStream(LocalPaths.getCacheSdkConfigFile());
//			sdkConfig = KaraUtils.readInputStream(fileInputStream);
//		} catch (Exception e) {
//			JsfUtils.addErrorMessage(
//					"Couldn't load the cached SdkConfig.dcem. You should download an SdkConfig file from 'Versions' view.");
//			return false;
//		}
		AuthGatewayConfig authGatewayConfig = new AuthGatewayConfig(authAppEntity.getName(), ByteBuffer.wrap(SecureUtils.encryptDataCommon(authAppEntity.getSharedKey())), TenantIdResolver.getCurrentTenantName());
		byte [] config = ThriftUtils.serializeObject(authGatewayConfig, true);
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		ZipOutputStream zipOutputStream = new ZipOutputStream(baos);
//		ZipEntry zipEntry = new ZipEntry(AppSystemConstants.AuthGatewayFileName);
//		zipOutputStream.putNextEntry(zipEntry);
//		zipOutputStream.write(config);
//		zipOutputStream.closeEntry();
//		zipOutputStream.close();
		JsfUtils.downloadFile("application/octet", AppSystemConstants.AuthConnectorFileName, config);
		
		return false; // do not close the window
	}

}
