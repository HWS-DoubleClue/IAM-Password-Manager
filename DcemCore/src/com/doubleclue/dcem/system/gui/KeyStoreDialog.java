package com.doubleclue.dcem.system.gui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.model.file.UploadedFile;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.KeyStorePurpose;
import com.doubleclue.dcem.core.entities.DcemNode;
import com.doubleclue.dcem.core.entities.KeyStoreEntity;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.system.logic.KeyStoreLogic;
import com.doubleclue.dcem.system.logic.NodeLogic;
import com.doubleclue.utils.RandomUtils;

@Named("keyStoreDialog")
@SessionScoped
public class KeyStoreDialog extends DcemDialog {

	Logger logger = LogManager.getLogger(KeyStoreDialog.class);

	@Inject
	AutoViewBean autoViewBean;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	JpaLogic jpaLogic;

	@Inject
	NodeLogic nodeLogic;

	@Inject
	KeyStoreLogic keyStoreLogic;

	String selectedNodeName;

	String selectedPurposeName;

	private UploadedFile uploadedFile;

	@Inject
	AuditingLogic auditingLogic;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public boolean actionOk() throws Exception {

		return true;
	}

	/**
	 * 
	 */
	public void upload() {
		KeyStoreEntity keyStoreEntity = (KeyStoreEntity) this.getActionObject();
		if (keyStoreEntity.getPurpose() == null) {
			JsfUtils.addErrorMessage("Please select a Purpose.");
			return;
		}
		if (uploadedFile == null || uploadedFile.getContent().length == 0) {
			JsfUtils.addErrorMessage("Please upload a Key-Store.");
			return;
		}

		if (keyStoreEntity.getPurpose() == KeyStorePurpose.ROOT_CA) {
			if (selectedNodeName != null && selectedNodeName.isEmpty() == false) {
				JsfUtils.addErrorMessage("Cannot Upload Root-CA for a node. Please unselect node.");
				return;
			}
		} else {
			if (selectedNodeName == null || selectedNodeName.isEmpty()) {
				JsfUtils.addErrorMessage("Please select a node.");
				return;
			}
		}

		try {
			KeyStore keyStore = KeyStore.getInstance("PKCS12");
			try {
				ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(uploadedFile.getContent());
				keyStore.load(arrayInputStream, keyStoreEntity.getPassword().toCharArray());
				arrayInputStream.close();
			} catch (Exception e) {
				logger.warn(e);
				JsfUtils.addErrorMessage("Please check file format and password. " + e.getMessage());
				return;
			}
			try {
				if (keyStoreEntity.getPurpose() == KeyStorePurpose.Saml_IdP_CA) {
					selectedNodeName = null;
				}
				KeyStoreEntity keyStoreEntity2 = keyStoreLogic.addReplaceKeystore(keyStore, keyStoreEntity.getPurpose(), keyStoreEntity.getPassword(),
						selectedNodeName, null);
				JsfUtils.addFacesInformationMessage("Key-Store uploaded succesful.");
				auditingLogic.addAudit(this.getAutoViewAction().getDcemAction(), keyStoreEntity2.toString());
				PrimeFaces.current().executeScript("PF('restart').show();");
			} catch (Exception e) {
				logger.warn(e);
				JsfUtils.addErrorMessage("Couldn't save uploaded Key-Store. Cause: " + e.getMessage());
				return;
			}

		} catch (KeyStoreException e) {
			logger.warn(e);
			JsfUtils.addErrorMessage("Upps, something went wrong. Cause: " + e.getMessage());
			return;
		}

	}

	/**
	 * 
	 */
	public void generate() {

		KeyStoreEntity keyStoreEntity = (KeyStoreEntity) this.getActionObject();

		if (keyStoreEntity.getPurpose() == null) {
			JsfUtils.addErrorMessage("Please select a Purpose.");
			return;
		}

		if (keyStoreEntity.getPurpose() == KeyStorePurpose.ROOT_CA) {
			if (selectedNodeName != null && selectedNodeName.isEmpty() == false) {
				JsfUtils.addErrorMessage("Cannot create Root-CA for a node. Please unselect node.");
				return;
			}
			try {
				String password = RandomUtils.generateRandomAlphaNumericString(16);
				KeyStore keyStoreMgt = SecureServerUtils.createKeyStore(DcemConstants.DEFAULT_KEY_PAIR_SIZE, "cn=" + keyStoreEntity.getCn(), null,
						keyStoreEntity.getIpAddress(), password.toCharArray(), null, keyStoreEntity.getPurpose().name(), keyStoreEntity.getExpiresOn());
				KeyStoreEntity keyStoreEntity2 = keyStoreLogic.addReplaceKeystore(keyStoreMgt, keyStoreEntity.getPurpose(), password, null,
						keyStoreEntity.getIpAddress());
				auditingLogic.addAudit(this.getAutoViewAction().getDcemAction(), keyStoreEntity2.toString());
				JsfUtils.addFacesInformationMessage("Root-CA Keystore created succesful. Please replace all other keystores.");
				PrimeFaces.current().executeScript("PF('restart').show();");
			} catch (Exception e) {
				logger.warn(e);
				JsfUtils.addErrorMessage("Upps, something went wrong. " + e.toString());
				return;
			}

		} else {
			if ((selectedNodeName == null || selectedNodeName.isEmpty()) && keyStoreEntity.getPurpose() != KeyStorePurpose.Saml_IdP_CA) {
				JsfUtils.addErrorMessage("Please select a node.");
				return;
			}

			try {
				List<KeyStoreEntity> list = keyStoreLogic.getKeyStoreByPurpose(KeyStorePurpose.ROOT_CA);
				if (list == null || list.isEmpty()) {
					throw new Exception("No root-CA found.");
				}
				KeyStoreEntity rootKeyStoreEntity = list.get(0);
				KeyStore rootKeyStore;

				rootKeyStore = KeyStore.getInstance("PKCS12");

				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(rootKeyStoreEntity.getKeyStore());
				rootKeyStore.load(byteArrayInputStream, rootKeyStoreEntity.getPassword().toCharArray());
				byteArrayInputStream.close();

				/*
				 *  Create Key-Store and Sign it with Root-CA
				 * 
				 */
				PrivateKey pvKey = (PrivateKey) rootKeyStore.getKey(KeyStorePurpose.ROOT_CA.name(), rootKeyStoreEntity.getPassword().toCharArray());
				String password = RandomUtils.generateRandomAlphaNumericString(16);
				KeyStore keyStoreMgt = null;
				if (keyStoreEntity.getPurpose() == KeyStorePurpose.Saml_IdP_CA) {
					// Do Self Signed
					selectedNodeName = null;
					keyStoreMgt = SecureServerUtils.createKeyStore(DcemConstants.DEFAULT_KEY_PAIR_SIZE, "cn=" + keyStoreEntity.getCn(), null, null,
							password.toCharArray(), null, keyStoreEntity.getPurpose().name(), keyStoreEntity.getExpiresOn());
				} else {
					keyStoreMgt = SecureServerUtils.createKeyStore(DcemConstants.DEFAULT_KEY_PAIR_SIZE, "cn=" + keyStoreEntity.getCn(),
							rootKeyStore.getCertificateChain(KeyStorePurpose.ROOT_CA.name()), keyStoreEntity.getIpAddress(), password.toCharArray(), pvKey,
							keyStoreEntity.getPurpose().name(), keyStoreEntity.getExpiresOn());
				}

				KeyStoreEntity keyStoreEntity2 = keyStoreLogic.addReplaceKeystore(keyStoreMgt, keyStoreEntity.getPurpose(), password, selectedNodeName,
						keyStoreEntity.getIpAddress());
				JsfUtils.addFacesInformationMessage("Keystore created succesful.");
				auditingLogic.addAudit(this.getAutoViewAction().getDcemAction(), keyStoreEntity2.toString());
				PrimeFaces.current().executeScript("PF('restart').show();");
			} catch (Exception exp) {
				logger.warn(exp);
				JsfUtils.addErrorMessage(exp.toString());
			}
		}

	}

	public boolean isWithNode() {
		KeyStoreEntity keyStoreEntity = (KeyStoreEntity) this.getActionObject();
		return (keyStoreEntity.getPurpose() != KeyStorePurpose.ROOT_CA && keyStoreEntity.getPurpose() != KeyStorePurpose.Saml_IdP_CA);
	}

	/**
	 * 
	 */
	public void downloadPk12() {
		KeyStoreEntity keyStoreEntity = (KeyStoreEntity) this.getActionObject();
		try {
			String node = "";
			if (keyStoreEntity.getNode() != null) {
				node = "_" + keyStoreEntity.getNode().getName();
			}
			JsfUtils.downloadFile(MediaType.APPLICATION_OCTET_STREAM, keyStoreEntity.getPurpose().name() + node + ".p12", keyStoreEntity.getKeyStore());
			if (keyStoreEntity.getNode() == null) {
				auditingLogic.addAudit(this.getAutoViewAction().getDcemAction(), "Purpose: " + keyStoreEntity.getPurpose());
			} else {
				auditingLogic.addAudit(this.getAutoViewAction().getDcemAction(),
						"Purpose: " + keyStoreEntity.getPurpose() + ", for Node: " + keyStoreEntity.getNode().getName());
			}
		} catch (IOException e) {
			logger.warn(e.toString());
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	/**
	 * 
	 */
	public void downloadPem() {
		KeyStoreEntity keyStoreEntity = (KeyStoreEntity) this.getActionObject();
		try {
			String node = "";
			if (keyStoreEntity.getNode() != null) {
				node = "_" + keyStoreEntity.getNode().getName();
			}

			byte[] pem = SecureServerUtils.convertPk12ToPem(keyStoreEntity.getKeyStore(), keyStoreEntity.getPassword(), keyStoreEntity.getPurpose().name());

			String fileName;
			if (keyStoreEntity.getPurpose() == KeyStorePurpose.ROOT_CA) {
				fileName = "TrustStore.pem";
			} else {
				fileName = keyStoreEntity.getPurpose().name() + node + ".pem";
			}
			JsfUtils.downloadFile(MediaType.APPLICATION_OCTET_STREAM, fileName, pem);
			if (keyStoreEntity.getNode() == null) {
				auditingLogic.addAudit(this.getAutoViewAction().getDcemAction(), "Purpose: " + keyStoreEntity.getPurpose());
			} else {
				auditingLogic.addAudit(this.getAutoViewAction().getDcemAction(),
						"Purpose: " + keyStoreEntity.getPurpose() + ", for Node: " + keyStoreEntity.getNode().getName());
			}
		} catch (Exception e) {
			logger.warn(e.toString());
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	public String getPassword() throws Exception {
		KeyStoreEntity ks = (KeyStoreEntity) this.getActionObject();
		return ks.getPassword();
	}

	public KeyStorePurpose[] getPurposes() {
		return KeyStorePurpose.values();
	}

	public List<SelectItem> getNodes() {
		List<DcemNode> list = nodeLogic.getNodes();
		List<SelectItem> selectList = new ArrayList<SelectItem>(list.size());
		for (DcemNode node : list) {
			selectList.add(new SelectItem(node.getName(), node.getName()));
		}
		return selectList;
	}

	public String getSelectedNodeName() {
		return selectedNodeName;
	}

	public void setSelectedNodeName(String selectedNodeName) {
		this.selectedNodeName = selectedNodeName;
	}

	public String getSelectedPurposeName() {
		return selectedPurposeName;
	}

	public void setSelectedPurposeName(String selectedPurposeName) {
		this.selectedPurposeName = selectedPurposeName;
	}

	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

}
