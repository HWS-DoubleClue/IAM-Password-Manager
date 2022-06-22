package com.doubleclue.dcem.saml.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.file.UploadedFile;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.AttributeTypeEnum;
import com.doubleclue.dcem.core.logic.ClaimAttribute;
import com.doubleclue.dcem.saml.entities.SamlSpMetadataEntity;
import com.doubleclue.dcem.saml.logic.SamlIdpSettings;
import com.doubleclue.dcem.saml.logic.SamlLogic;
import com.doubleclue.dcem.saml.logic.SamlModule;
import com.doubleclue.dcem.saml.logic.enums.CanonicalizationAlgorithmEnum;
import com.doubleclue.dcem.saml.logic.enums.DigestAlgorithmEnum;
import com.doubleclue.dcem.saml.logic.enums.NameIdFormatEnum;
import com.doubleclue.dcem.saml.logic.enums.SignatureAlgorithmEnum;

@SuppressWarnings("serial")
@Named("spMetadataDialog")
@SessionScoped
public class SpMetadataDialog extends DcemDialog {

	private static Logger logger = LogManager.getLogger(SpMetadataDialog.class);
	private static List<SelectItem> userPropertyTypes = null;

	private String selectedPreset;
	private String selectedAttributeType;
	private SamlIdpSettings idpSettings;
	private ClaimAttribute selectedAttribute;
	private SamlSpMetadataEntity spMetadataEntity;
	private boolean editingAttribute;
	private List<String> presetPictures;

	@Inject
	SamlLogic samlLogic;

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		super.show(dcemView, autoViewAction);
		spMetadataEntity = (SamlSpMetadataEntity) this.getActionObject();
		idpSettings = spMetadataEntity.getIdpSettings();
		setSelectedPreset(null);
		presetPictures = new ArrayList<>();
		presetPictures.add("LogMeIn");
		presetPictures.add("LogMeIn");
		presetPictures.add("AWS");
		presetPictures.add("MicrosoftAzure");
		presetPictures.add("Dropbox");
	}

	@Override
	public void leavingDialog() {
		super.leavingDialog();
		idpSettings = null;
		selectedAttribute = null;
		spMetadataEntity = null;
		setSelectedPreset(null);
	}

	@Override
	public boolean actionOk() throws Exception {
		SamlSpMetadataEntity spMetadataEntity = (SamlSpMetadataEntity) this.getActionObject();
		if (shouldUpdateFieldsFromXml(spMetadataEntity)) {
			try {
				samlLogic.setUpEntityFromXml(spMetadataEntity.getMetadata(), spMetadataEntity, true);
			} catch (Exception e) {
				JsfUtils.addErrorMessage(JsfUtils.getStringSafely(SamlModule.RESOURCE_NAME, "error.invalidXml"));
				return false;
			}
		} else {
			spMetadataEntity.setIdpSettings(idpSettings); // serialize settings
		}
		samlLogic.addUpdateSpMetadata(getAutoViewAction().getDcemAction(), spMetadataEntity, true);
		return true;
	}

	public void actionAttribute() {
		selectedAttribute.setAttributeTypeEnum(AttributeTypeEnum.valueOf(selectedAttributeType));
		if (editingAttribute == false) {
			if (idpSettings.getAttributes().contains(selectedAttribute)) {
				JsfUtils.addErrorMessage("Attribute name exists already");
				return;
			}
			idpSettings.getAttributes().add(selectedAttribute);
		}

		PrimeFaces.current().ajax().update("regForm:tabView:attributesTable");
		PrimeFaces.current().executeScript("PF('attributeDialog').hide();");

	}

	private boolean shouldUpdateFieldsFromXml(SamlSpMetadataEntity spMetadataEntity) {
		return spMetadataEntity.getMetadata() != null && !spMetadataEntity.getMetadata().isEmpty()
				&& (spMetadataEntity.getEntityId() == null || spMetadataEntity.getEntityId().isEmpty() || spMetadataEntity.getDisplayName() == null
						|| spMetadataEntity.getDisplayName().isEmpty() || spMetadataEntity.getAcsLocation() == null
						|| spMetadataEntity.getAcsLocation().isEmpty());
	}

	public void upload(FileUploadEvent event) {
		UploadedFile file = event.getFile();
		if (file != null) {
			try {
				String fileContents = new String(file.getContent(), DcemConstants.CHARSET_UTF8);
				SamlSpMetadataEntity spMetadataEntity = (SamlSpMetadataEntity) this.getActionObject();
				samlLogic.setUpEntityFromXml(fileContents, spMetadataEntity, true);
			} catch (DcemException exp) {
				logger.warn("Couldn't upload a Valid meta data file", exp);
				JsfUtils.addErrorMessage(exp.getLocalizedMessage());
			} catch (Exception exp) {
				logger.warn("Couldn't upload a Valid meta data file", exp);
				JsfUtils.addErrorMessage(JsfUtils.getStringSafely(SamlModule.RESOURCE_NAME, "error.invalidFile"));
			}
		} else {
			JsfUtils.addErrorMessage(JsfUtils.getStringSafely(SamlModule.RESOURCE_NAME, "error.fileNotFound"));
		}
	}

	public NameIdFormatEnum[] getNameIdFormats() {
		return NameIdFormatEnum.values();
	}

	public SignatureAlgorithmEnum[] getSignatureAlgorithms() {
		return SignatureAlgorithmEnum.values();
	}

	public DigestAlgorithmEnum[] getDigestAlgorithms() {
		return DigestAlgorithmEnum.values();
	}

	public CanonicalizationAlgorithmEnum[] getCanonicalizationAlgorithms() {
		return CanonicalizationAlgorithmEnum.values();
	}

	@Override
	public String getHeight() {
		return "700";
	}

	@Override
	public String getWidth() {
		return "750";
	}

	public String getLogoutIsPost() {
		SamlSpMetadataEntity spMetadataEntity = (SamlSpMetadataEntity) this.getActionObject();
		return spMetadataEntity.isLogoutIsPost() ? "p" : "r";
	}

	public void setLogoutIsPost(String logoutIsPost) {
		SamlSpMetadataEntity spMetadataEntity = (SamlSpMetadataEntity) this.getActionObject();
		spMetadataEntity.setLogoutIsPost(logoutIsPost.equals("p"));
	}

	public void addNewAttribute() {
		selectedAttribute = new ClaimAttribute("", null, null);
		editingAttribute = false;
		selectedAttributeType = null;
		PrimeFaces.current().ajax().update("attributeForm");
		PrimeFaces.current().executeScript("PF('attributeDialog').show();");
	}

	public void clearAttribute() {
		if (selectedAttribute == null) {
			JsfUtils.addErrorMessage("Please select an Attribute");
			return;
		}
		idpSettings.getAttributes().remove(selectedAttribute);
		PrimeFaces.current().executeScript("PF('attributeDialog').hide();");
		PrimeFaces.current().ajax().update("attributeForm");
	}

	public void editAttribute() {
		if (selectedAttribute == null) {
			JsfUtils.addErrorMessage("Please select an Attribute");
			return;
		}
		editingAttribute = true;
		selectedAttributeType = selectedAttribute.getAttributeTypeEnum().name();
		PrimeFaces.current().executeScript("PF('attributeDialog').show();");
		PrimeFaces.current().ajax().update("attributeForm");
	}

	public boolean isAdding() {
		SamlSpMetadataEntity spMetadataEntity = (SamlSpMetadataEntity) this.getActionObject();
		return spMetadataEntity.getId() == null;
	}

	public boolean isRenderPresets() {
		return isAdding() && getSelectedPreset() == null;
	}

	public String getSelectedPreset() {
		return selectedPreset;
	}

	public void setSelectedPreset(String selectedPreset) {
		this.selectedPreset = selectedPreset;
	}

	public void onPresetSelected() {
		try {
			Map<String, SamlSpMetadataEntity> presetMap = samlLogic.getPresetMetadataMap();
			if (selectedPreset != null && !selectedPreset.isEmpty() && presetMap.containsKey(selectedPreset)) {
				SamlSpMetadataEntity spMetadataEntity = (SamlSpMetadataEntity) this.getActionObject();
				spMetadataEntity.copyEntity(presetMap.get(selectedPreset));
				idpSettings = spMetadataEntity.getIdpSettings();
			}
		} catch (Exception e) {
			System.out.println("Error while loading preset: " + e.toString());
		}
	}

	public List<SelectItem> getUserPropertyTypes() {
		if (userPropertyTypes == null) {
			userPropertyTypes = new LinkedList<>();
			for (AttributeTypeEnum propertyEnum : AttributeTypeEnum.values()) {
				if (propertyEnum != AttributeTypeEnum.PASSWORD && propertyEnum != AttributeTypeEnum.USER_INPUT
						&& propertyEnum != AttributeTypeEnum.AUTHENTICATOR_PASSCODE) {
					userPropertyTypes.add(new SelectItem(propertyEnum.name(), propertyEnum.getDisplayName()));
				}
			}
		}
		return userPropertyTypes;
	}

	public String getPresetImage(String presetString) {
		switch (presetString) {
		case "Custom":
			return "Custom";
		case "Benutzerdefiniert":
			return "Custom";
		default:
			return presetString;
		}
	}

	public Set<String> getPresetList() {
		return samlLogic.getPresetMetadataMap().keySet();
	}

	public SamlIdpSettings getIdpSettings() {
		return idpSettings;
	}

	public void setIdpSettings(SamlIdpSettings idpSettings) {
		this.idpSettings = idpSettings;
	}

	public ClaimAttribute getSelectedAttribute() {
		return selectedAttribute;
	}

	public void setSelectedAttribute(ClaimAttribute selectedAttribute) {
		this.selectedAttribute = selectedAttribute;
	}

	public boolean isEditingAttribute() {
		return editingAttribute;
	}

	public void setEditingAttribute(boolean editingAttribute) {
		this.editingAttribute = editingAttribute;
	}

	public String getSelectedAttributeType() {
		return selectedAttributeType;
	}

	public void setSelectedAttributeType(String selectedAttributeType) {
		this.selectedAttributeType = selectedAttributeType;
	}

	public boolean isAttributeWithValue() {
		boolean returnValue = false;
		if (selectedAttribute == null || selectedAttribute.getAttributeTypeEnum() == null) {
			return false;
		}
		switch (selectedAttribute.getAttributeTypeEnum()) {
		case CLOUD_SAFE_USER:
		// #if COMMUNITY_EDITION == false
		case DOMAIN_ATTRIBUTE:
		// #endif
		case STATIC_TEXT:
			returnValue = true;
			break;
		default:
			break;
		}
		return returnValue;
	}

	public void listenerChangeAttributeType() {
		selectedAttribute.setAttributeTypeEnum(AttributeTypeEnum.valueOf(selectedAttributeType));
	}

	public List<String> getPresetPictures() {
		return presetPictures;
	}
}
