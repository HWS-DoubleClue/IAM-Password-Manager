package com.doubleclue.dcem.saml.gui;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.saml.logic.SamlConstants;
import com.doubleclue.dcem.saml.logic.SamlLogic;

@SuppressWarnings("serial")
@Named("downloadIdpMetadataDialog")
@SessionScoped
public class DownloadIdpMetadataDialog extends DcemDialog {

	private String metadata;

	@Inject
	SamlLogic samlLogic;
	
	public void downloadMetadata() throws UnsupportedEncodingException, IOException {
		String metadata = getMetadata();
		if (metadata != null) {
			JsfUtils.downloadFile("application/octet", SamlConstants.FILENAME_IDP_METADATA, metadata.getBytes(DcemConstants.CHARSET_UTF8));
		}
	}
	
	public void downloadCertificate() throws UnsupportedEncodingException, IOException {
		String pemString = samlLogic.getIdPCertificatePem();
		if (pemString != null) {
			JsfUtils.downloadFile("application/octet", SamlConstants.FILENAME_IDP_CERTIFICATE, pemString.getBytes(DcemConstants.CHARSET_UTF8));
		}
	}

	public String getMetadata() {
		if (metadata == null) {
			try {
				metadata = samlLogic.getIdpMetadataString();
			} catch (DcemException e) {
				String error = e.toString();
				JsfUtils.addErrorMessage(error);
				return error;
			}
		}
		return metadata;
	}
	
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		metadata = null;
	}
}
