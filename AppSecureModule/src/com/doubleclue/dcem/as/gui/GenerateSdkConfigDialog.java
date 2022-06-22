package com.doubleclue.dcem.as.gui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.comm.thrift.SdkConfig;
import com.doubleclue.dcem.as.entities.ActivationCodeEntity;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.config.LocalPaths;
import com.doubleclue.dcem.core.entities.KeyStoreEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.CertificateInfo;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.system.logic.KeyStoreLogic;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.utils.SecureUtils;
import com.doubleclue.utils.ThriftUtils;

@SuppressWarnings("serial")
@Named("generateSdkConfig")
@SessionScoped
public class GenerateSdkConfigDialog extends DcemDialog {

	private static Logger logger = LogManager.getLogger(GenerateSdkConfigDialog.class);

	String serverUrl = null;
	String portalUrl = "";

	@Inject
	AsModule asModule;

	@Inject
	ConfigLogic configLogic;

	@Inject
	KeyStoreLogic keyStoreLogic;

	@Inject
	DcemApplicationBean dcemApplicationBean;

	@Inject
	SystemModule systemModule;

	private UploadedFile uploadedFile;

	byte[] trustStorePem;

	private String chooseCertificate = "1";
	
	List<CertificateInfo> certInfos = new ArrayList<>();

	@Override
	public boolean actionOk() throws Exception {
		ActivationCodeEntity activationCode = (ActivationCodeEntity) this.getActionObject();
		JsfUtils.addInformationMessage(AsModule.RESOURCE_NAME, "activationDialog.success", activationCode.getActivationCode());
		return true;
	}

	public String getServerUrl() {
		if (serverUrl == null) {
			serverUrl = "--yourhost--";
			try {
				serverUrl = dcemApplicationBean.getServiceUrl(ConnectionServicesType.WEB_SOCKETS);
			} catch (DcemException e1) {
			}
		}
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getPortalUrl() {
		if (portalUrl == null || portalUrl.isEmpty()) {
			portalUrl = "--yourhost--";
			try {
				portalUrl = dcemApplicationBean.getServiceUrl(ConnectionServicesType.USER_PORTAL);
			} catch (DcemException e1) {
			}
		}
		return portalUrl;
	}

	public void setPortalUrl(String portalUrl) {
		this.portalUrl = portalUrl;
	}

	public void download() {
		SdkConfig sdkConfig = new SdkConfig();

		try {
			if ((serverUrl.startsWith("wss://") || serverUrl.startsWith("ws://")) == false) {
				throw new Exception("Invalid Server URL. URL must start with 'wss://'");
			}
		} catch (Exception e1) {
			JsfUtils.addErrorMessage(e1.getMessage());
			return;
		}

		sdkConfig.setPortalUrl(portalUrl);
		sdkConfig.setServerUrl(serverUrl);

		byte[] data = asModule.getPublicKey().getEncoded();
		byte[] serializedData = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			// TODO to be refactoed to logic
			ZipOutputStream zipOutputStream = new ZipOutputStream(baos);
			ZipEntry zipEntry = new ZipEntry(AppSystemConstants.TrustStoreFileName);
			zipOutputStream.putNextEntry(zipEntry);
			zipOutputStream.write(trustStorePem);
			zipOutputStream.closeEntry();

			zipEntry = new ZipEntry(AppSystemConstants.SdkConfigFileName);
			zipOutputStream.putNextEntry(zipEntry);
			data = SecureServerUtils.encryptDataCommon(data);
			sdkConfig.setServerPublicKey(data);
			data = asModule.getConnectionKey().getBytes("UTF-8");
			data = SecureServerUtils.encryptDataCommon(data);
			sdkConfig.setConnectionKey(data);
			serializedData = ThriftUtils.serializeObject(sdkConfig, true);
			zipOutputStream.write(serializedData);
			zipOutputStream.closeEntry();

			byte[] sigData = new byte[trustStorePem.length + serializedData.length];
			System.arraycopy(trustStorePem, 0, sigData, 0, trustStorePem.length);
			System.arraycopy(serializedData, 0, sigData, trustStorePem.length, serializedData.length);
			// System.out.println("GenerateSdkConfigDialog.download() TrustStore: " + trustStorePem.length + ",
			// SdkConfig: " + serializedData.length ) ;
			byte[] signature = SecureUtils.createMacDigestCommonSha2(sigData, 0, sigData.length);
			zipEntry = new ZipEntry(AppSystemConstants.SignatureFileName);
			zipOutputStream.putNextEntry(zipEntry);
			zipOutputStream.write(signature);
			zipOutputStream.closeEntry();
			zipOutputStream.close();

		} catch (Exception e) {
			logger.warn("Couln't create SDK Config", e);
			JsfUtils.addErrorMessage("Couldn't create the SDK-Configuration, Cause: " + e.getMessage());
			return;
		}
		try {
			try {
				File sdkConfigFile = LocalPaths.getCacheSdkConfigFile();
				FileOutputStream outputStream = new FileOutputStream(sdkConfigFile);
				outputStream.write(baos.toByteArray());
				outputStream.close();
			} catch (Exception e) {
				logger.info("couldn't cache SdkConfig.dcem", e);
				JsfUtils.addErrorMessage("couldn't cache SdkConfig.dcem. " + e.toString());
				return;

			}
			try {
				configLogic.setDcemConfiguration(AsModule.MODULE_ID, DcemConstants.CONFIG_KEY_SDK_CONFIG, baos.toByteArray());
			} catch (Exception e) {
				logger.info("couldn't save SdkConfig.dcem", e);
				JsfUtils.addErrorMessage("Couldn't save SdkConfig.dcem. " + e.toString());
				return;
			}

			JsfUtils.downloadFile("application/octet", AppSystemConstants.DcemFileName, baos.toByteArray());
		} catch (IOException e) {
			logger.warn(e);
			JsfUtils.addErrorMessage("Couldn't download the SDK Configuration, cause: " + e.getMessage());
		}
	}

	public void actionUrlCertificates() {
		certInfos = new ArrayList<>();
		try {
			URL url = new URL(serverUrl.replace("wss", "https"));
			X509Certificate[] certificates = SecureServerUtils.getCertificates(url.getHost(), url.getPort(), systemModule.getPreferences().getHttpProxyHost(),
					systemModule.getPreferences().getHttpProxyPort());
			ByteArrayOutputStream bos = SecureServerUtils.convertChainToPem(certificates);
			trustStorePem = bos.toByteArray();
			for (int i = 0; i < certificates.length; i++) {
				X509Certificate cert = certificates[i];
				certInfos.add(new CertificateInfo(cert.getIssuerDN().getName(), cert.getSubjectDN().getName(), cert.getNotAfter()));
			}
		} catch (Exception e) {
			logger.info("Couldn't get the certificates", e);
			JsfUtils.addErrorMessage("Couldn't load the certificates");
		}
	}

	public List<CertificateInfo> getCertificates() {
		
		try {
			switch (chooseCertificate) {
			case "1": // use DCEM Root
				certInfos.clear();
				KeyStoreEntity keyStoreEntity = keyStoreLogic.getKeyStoreRoot();
				trustStorePem = SecureServerUtils.convertPk12ToPem(keyStoreEntity.getKeyStore(), keyStoreEntity.getPassword(),
						keyStoreEntity.getPurpose().name());
				certInfos.add(new CertificateInfo(keyStoreEntity.getCn(), keyStoreEntity.getCn(), keyStoreEntity.getExpiresOn()));

				break;
			case "2": // use upload File
				if (uploadedFile == null) {
				//	JsfUtils.addErrorMessage("Please upload a truststore file");
					return null;
				}
				certInfos.clear();
				try {
					trustStorePem = uploadedFile.getContent();
					KeyStore keyStore = SecureServerUtils.convertPemToTrustStore(trustStorePem);
					if (keyStore.size() == 0) {
						throw new Exception("Keystore is empty");
					}
					Enumeration<String> aliases = keyStore.aliases();
					while (aliases.hasMoreElements()) {
						X509Certificate cert = (X509Certificate) keyStore.getCertificate(aliases.nextElement());
						certInfos.add(new CertificateInfo(cert.getIssuerDN().getName(), cert.getSubjectDN().getName(), cert.getNotAfter()));
					}
				} catch (Exception exp) {
					logger.error(exp);
					JsfUtils.addErrorMessage("TrustStore file is empty or has an invalid format");
					return null;
				}
				break;
			case "3": // get from URL
//				URL url = new URL(serverUrl.replace("wss", "https"));
//
//				X509Certificate[] certificates = SecureServerUtils.getCertificates(url.getHost(), url.getPort(),
//						systemModule.getPreferences().getHttpProxyHost(), systemModule.getPreferences().getHttpProxyPort());
//				ByteArrayOutputStream bos = SecureServerUtils.convertChainToPem(certificates);
//				trustStorePem = bos.toByteArray();
//				for (int i = 0; i < certificates.length; i++) {
//					X509Certificate cert = certificates[i];
//					certInfos.add(new CertificateInfo(cert.getIssuerDN().getName(), cert.getSubjectDN().getName(), cert.getNotAfter()));
//				}
//				break;
			}
		} catch (Exception e) {
			logger.info("Couldn't get the certificates", e);
			JsfUtils.addErrorMessage("Couldn't load the certificates");
		}

		return certInfos;
	}

	public void uploadedFileListener(FileUploadEvent fileUploadEvent) {
		uploadedFile = fileUploadEvent.getFile();
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	public String getChooseCertificate() {
		return chooseCertificate;
	}

	public void setChooseCertificate(String chooseCertificate) {
		this.chooseCertificate = chooseCertificate;
		certInfos = new ArrayList<>(); 
	}

	public String getWidth() {
		return "800";
	}
	
	public String getHeight() {
		return "850";
	}

}
