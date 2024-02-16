package com.doubleclue.dcem.radius.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.core.SupportedCharsets;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.AttributeTypeEnum;
import com.doubleclue.dcem.core.logic.ClaimAttribute;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.radius.dictionary.AttributeType;
import com.doubleclue.dcem.radius.dictionary.DefaultDictionary;
import com.doubleclue.dcem.radius.entities.RadiusClientEntity;
import com.doubleclue.dcem.radius.logic.RadiusClientSettings;
import com.doubleclue.dcem.radius.logic.RadiusConstants;
import com.doubleclue.dcem.radius.logic.RadiusModule;

@SuppressWarnings("serial")
@Named("radiusClientDialog")
@SessionScoped
public class RadiusClientDialog extends DcemDialog {

	@Inject
	JpaLogic jpaLogic;

	@Inject
	RadiusModule radiusModule;

	// private static Logger logger = LogManager.getLogger(RadiusClientDialog.class);

	private static List<SelectItem> attributeTypes = null;

	private String selectedAttributeType;
	private RadiusClientSettings radiusClientSettings;
	private ClaimAttribute selectedAttribute;
	private RadiusClientEntity radiusClientEntity;
	private boolean editingAttribute;

	private String supportedCharset;
	SupportedCharsets[] supportedCharsets;

	@PostConstruct
	private void init() {
	}

	@Override
	public boolean actionOk() throws Exception {
		RadiusClientEntity clientEntity = (RadiusClientEntity) this.getActionObject();
		RadiusClientEntity previosClientEntity = radiusModule.getRadiusClient(clientEntity.getIpNumber());

		clientEntity.setTenantName(TenantIdResolver.getCurrentTenantName());
		if (previosClientEntity != null && clientEntity.getTenantName().equals(previosClientEntity.getTenantName()) == false) {
			JsfUtils.addErrorMessage("NAS Address is already in use by an other tenant. Used by " + clientEntity.getTenantName());
			return false;
		}
		SupportedCharsets supportedChar = SupportedCharsets.valueOf(supportedCharset);
		radiusClientSettings.setSupportedCharset(supportedChar);
		clientEntity.setRadiusClientSettings(radiusClientSettings);
		jpaLogic.addOrUpdateEntity((EntityInterface) clientEntity, this.getAutoViewAction().getDcemAction());
		Exception exception = DcemUtils.reloadTaskNodes(RadiusModule.class, TenantIdResolver.getCurrentTenantName());
		if (exception != null) {
			throw exception;
		}
		return true;
	}

	@Override
	public void actionConfirm() {
		try {
			jpaLogic.deleteEntities(autoViewBean.getSelectedItems(), getAutoViewAction().getDcemAction());
		} catch (DcemException e) {
			JsfUtils.addErrorMessage("Couldn't delete NAS Client." + e.toString());
			return;
		}
		DcemUtils.reloadTaskNodes(RadiusModule.class, TenantIdResolver.getCurrentTenantName());
	}

	public List<SelectItem> getAttributeTypes() {
		if (attributeTypes == null) {
			attributeTypes = new LinkedList<>();
			for (AttributeTypeEnum propertyEnum : AttributeTypeEnum.values()) {
				if (propertyEnum == AttributeTypeEnum.STATIC_TEXT || propertyEnum == AttributeTypeEnum.AUTHENTICATOR_PASSCODE || propertyEnum == AttributeTypeEnum.USER_INPUT) {
					continue;
				}
				attributeTypes.add(new SelectItem(propertyEnum.name(), propertyEnum.getDisplayName()));
			}
		}
		return attributeTypes;
	}

	public List<SelectItem> getRadiusSubAttributes() {
		List<SelectItem> list = new ArrayList<>();
		list.add(new SelectItem(0, ""));
		for (int ind = 1; ind < 255; ind++) {
			list.add(new SelectItem(ind, Integer.toString(ind)));
		}
		return list;
	}

	public boolean isVendorSpecific() {
		return (selectedAttribute != null && selectedAttribute.getName() != null && selectedAttribute.getName().equals("26"));
	}

	public void addNewAttribute() {
		selectedAttribute = new ClaimAttribute(null, null, null);
		editingAttribute = false;
		selectedAttributeType = null;
		PrimeFaces.current().ajax().update("attributeForm");
		PrimeFaces.current().executeScript("PF('attributeDialog').show();");
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

	public void clearAttribute() {
		if (selectedAttribute == null) {
			JsfUtils.addErrorMessage("Please select an Attribute");
			return;
		}
		radiusClientSettings.getClaimAttributes().remove(selectedAttribute);
		PrimeFaces.current().executeScript("PF('attributeDialog').hide();");
		PrimeFaces.current().ajax().update("attributeForm");
	}

	public void actionAttribute() {
		try {
			int attributeId = Integer.parseInt(selectedAttribute.getName());
			if (attributeId < 1 || attributeId > 255) {
				throw new Exception();
			}
		} catch (Exception e) {
			JsfUtils.addErrorMessage("Attribute ID must be a number between 4 and 255");
			return;
		}
		selectedAttribute.setAttributeTypeEnum(AttributeTypeEnum.valueOf(selectedAttributeType));
		if (editingAttribute == false) {
			if (radiusClientSettings.getClaimAttributes().contains(selectedAttribute)) {
				JsfUtils.addErrorMessage("Attribute name exists already");
				return;
			}
			radiusClientSettings.getClaimAttributes().add(selectedAttribute);
		}
		PrimeFaces.current().ajax().update("regForm:tabView:attributesTable");
		PrimeFaces.current().executeScript("PF('attributeDialog').hide();");
	}

	public void listenerChangeAttributeType() {
		selectedAttribute.setAttributeTypeEnum(AttributeTypeEnum.valueOf(selectedAttributeType));
	}

	public boolean isAttributeWithValue() {
		if (selectedAttribute != null && selectedAttribute.getAttributeTypeEnum() != null) {
			return selectedAttribute.getAttributeTypeEnum().isValueRequired();
		}
		return false;
	}

	public String getVendorId() {
		return Integer.toString(RadiusConstants.VENDOR_ID);
	}

	public List<SelectItem> getRadiusAttributes() {
		List<SelectItem> list = new ArrayList<SelectItem>();
		Collection<AttributeType> attributes = DefaultDictionary.getDefaultDictionary().getAllAttributes().get(-1).values();
		for (AttributeType attributeType : attributes) {
			list.add(new SelectItem(attributeType.getTypeCode(), attributeType.getTypeCode() + " - " + attributeType.getName()));
		}
		return list;
	}

	public String getSelectedAttributeName() {
		if (selectedAttribute == null) {
			return "";
		}
		return selectedAttribute.getName();
	}

	public void setSelectedAttributeName(String value) {
		if (selectedAttribute != null) {
			selectedAttribute.setName(value);
		}
	}

	public String getSelectedAttributeSubName() {
		if (selectedAttribute == null) {
			return "";
		}
		return selectedAttribute.getSubName();
	}

	public void setSelectedAttributeSubName(String value) {
		if (selectedAttribute != null) {
			selectedAttribute.setSubName(value);
		}
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		super.show(dcemView, autoViewAction);
		radiusClientEntity = (RadiusClientEntity) this.getActionObject();
		radiusClientSettings = radiusClientEntity.getRadiusClientSettings();
		if (radiusClientSettings == null) {
			radiusClientSettings = new RadiusClientSettings();
		}
		supportedCharset = radiusClientSettings.getSupportedCharset().name();
	}

	public void leavingDialog() {
		radiusClientSettings = null;
		radiusClientEntity = null;
		selectedAttribute = null;
	}

	public String getSelectedAttributeType() {
		return selectedAttributeType;
	}

	public void setSelectedAttributeType(String selectedAttributeType) {
		this.selectedAttributeType = selectedAttributeType;
	}

	public RadiusClientSettings getRadiusClientSettings() {
		return radiusClientSettings;
	}

	public void setRadiusClientSettings(RadiusClientSettings radiusClientSettings) {
		this.radiusClientSettings = radiusClientSettings;
	}

	public boolean isEditingAttribute() {
		return editingAttribute;
	}

	public void setEditingAttribute(boolean editingAttribute) {
		this.editingAttribute = editingAttribute;
	}

	public void setUserPropertyTypes(List<SelectItem> userPropertyTypes) {
		RadiusClientDialog.attributeTypes = userPropertyTypes;
	}

	public ClaimAttribute getSelectedAttribute() {
		return selectedAttribute;
	}

	public void setSelectedAttribute(ClaimAttribute selectedAttribute) {
		this.selectedAttribute = selectedAttribute;
	}

	public String getSupportedCharset() {
		return supportedCharset;
	}

	public void setSupportedCharset(String supportedCharset) {
		this.supportedCharset = supportedCharset;
	}

	public SupportedCharsets[] getSupportedCharsets() {
		return SupportedCharsets.values();
	}

}
