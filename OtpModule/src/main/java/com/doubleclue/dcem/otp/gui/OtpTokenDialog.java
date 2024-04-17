package com.doubleclue.dcem.otp.gui;

import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.file.UploadedFile;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.otp.entities.OtpTokenEntity;
import com.doubleclue.dcem.otp.logic.OtpLogic;
import com.doubleclue.dcem.otp.logic.OtpModule;
import com.doubleclue.dcem.otp.logic.OtpTypes;

@Named("otpTokenDialog")
@SessionScoped
public class OtpTokenDialog extends DcemDialog {

	Logger logger = LogManager.getLogger(OtpTokenDialog.class);

	@Inject
	AutoViewBean autoViewBean;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	OtpLogic otpLogic;

	@Inject
	UserLogic userLogic;

	OtpTypes tokenType;
	DcemUser dcemUser;
	boolean assignToken;
	String encryptionKey;

	private UploadedFile uploadedFile;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean actionOk() throws Exception {
		OtpTokenEntity otpTokenEntity = (OtpTokenEntity) this.getActionObject();
		if (assignToken == false) {
			otpTokenEntity.setUser(null);
		} else {
			otpTokenEntity.setUser(dcemUser);
		}
		otpLogic.editToken(otpTokenEntity, this.getAutoViewAction().getDcemAction());
		return true;
	}

	public OtpTypes[] getTokenTypes() {
		return OtpTypes.values();
	}
	/**
	 * 
	 */
	public void importTokens() {
		if (uploadedFile == null || uploadedFile.getContent().length == 0) {
			JsfUtils.addErrorMessage("Please select a file");
			return;
		}
		try {
			List<OtpTokenEntity> listTokens = otpLogic.parseTokens(tokenType, uploadedFile.getContent(), encryptionKey);
			System.out.println("OtpTokenDialog.importTokens()");
			int addedTokens = otpLogic.addTokens(listTokens, this.getAutoViewAction().getDcemAction());
			JsfUtils.addInformationMessage(OtpModule.RESOURCE_NAME, "otpImport", listTokens.size(), addedTokens);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
		}

	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public OtpTypes getTokenType() {
		return tokenType;
	}

	public void setTokenType(OtpTypes tokenType) {
		this.tokenType = tokenType;
	}

	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		encryptionKey = null;
		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_IMPORT)) {
			return;
		}
		OtpTokenEntity otpTokenEntity = (OtpTokenEntity) this.getActionObject();
		dcemUser = otpTokenEntity.getUser();
		if (dcemUser != null) {
			assignToken = true;
		} else {
			assignToken = false;
		}

	}

	public boolean isAssignToken() {
		return assignToken;
	}

	public void setAssignToken(boolean assignToken) {
		this.assignToken = assignToken;
	}

	public String getEncryptionKey() {
		return encryptionKey;
	}

	public void setEncryptionKey(String encryptionKey) {
		this.encryptionKey = encryptionKey;
	}
	
	@Override
	public void leavingDialog() {
		uploadedFile = null;
		encryptionKey = null;
		dcemUser = null;
	}

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
	}

}
