package com.doubleclue.dcem.oauth.gui;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.AttributeTypeEnum;
import com.doubleclue.dcem.core.logic.ClaimAttribute;
import com.doubleclue.dcem.oauth.entities.OAuthClientEntity;
import com.doubleclue.dcem.oauth.logic.OAuthIdpSettings;
import com.doubleclue.dcem.oauth.logic.OAuthLogic;
import com.doubleclue.oauth.openid.enums.OpenIdClaim;

import io.jsonwebtoken.SignatureAlgorithm;

@SuppressWarnings("serial")
@Named("clientMetadataDialog")
@SessionScoped
public class ClientMetadataDialog extends DcemDialog {

	@Inject
	OAuthLogic oauthLogic;

	private static final List<OpenIdClaim> nonEditableStandardClaims = Arrays.asList(OpenIdClaim.ACR, OpenIdClaim.EMAIL_VERIFIED,
			OpenIdClaim.PHONE_NUMBER_VERIFIED, OpenIdClaim.UPDATED_AT, OpenIdClaim.AUTH_TIME, OpenIdClaim.NONCE, OpenIdClaim.ACCESS_TOKEN_HASH,
			OpenIdClaim.AUTH_CODE_HASH);

	private static List<SelectItem> userPropertyTypes = null;
	private static List<SelectItem> selectableStandardClaims = null;

	private String selectedClaimUserProperty;
	private OAuthIdpSettings idpSettings;
	private ClaimAttribute selectedClaim;
	private boolean editingClaim;

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		super.show(dcemView, autoViewAction);
		OAuthClientEntity entity = (OAuthClientEntity) this.getActionObject();
		idpSettings = entity.getIdpSettings();
	}

	@Override
	public void leavingDialog() {
		super.leavingDialog();
		idpSettings = null;
		selectedClaim = null;
		selectedClaimUserProperty = null;
	}

	@Override
	public boolean actionOk() throws Exception {
		try {
			OAuthClientEntity entity = (OAuthClientEntity) this.getActionObject();
			entity.setIdpSettings(idpSettings);
			oauthLogic.addUpdateClientMetadata(getAutoViewAction().getDcemAction(), entity, true);
			return true;
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			return false;
		}
	}

	public void saveClaim() {
		selectedClaim.setProperty(AttributeTypeEnum.valueOf(selectedClaimUserProperty));
		if (editingClaim == false) {
			String errorMessage = null;
			OpenIdClaim oidClaim = OpenIdClaim.fromString(selectedClaim.getName());
			if (oidClaim != null && nonEditableStandardClaims.contains(oidClaim)) {
				errorMessage = "This claim is system-reserved";
			} else if (idpSettings.getClaims().contains(selectedClaim)) {
				errorMessage = "Claim name exists already";
			} else {
				idpSettings.getClaims().add(selectedClaim);
			}
			if (errorMessage != null) {
				JsfUtils.addErrorMessage(errorMessage);
				return;
			}
		}
		PrimeFaces.current().ajax().update("regForm:tabView:claimsTable");
		PrimeFaces.current().executeScript("PF('claimDialog').hide();");
	}

	public void actionCreateClientId() {
		OAuthClientEntity entity = (OAuthClientEntity) this.getActionObject();
		entity.setClientId(oauthLogic.createClientId());
	}

	public void actionCreateClientSecret() {
		OAuthClientEntity entity = (OAuthClientEntity) this.getActionObject();
		entity.setClientSecret(oauthLogic.createClientSecret());
	}

	public SignatureAlgorithm[] getSigningAlgs() {
		return SignatureAlgorithm.values();
	}

	public void addNewClaim() {
		selectedClaim = new ClaimAttribute("", null, null);
		editingClaim = false;
		selectedClaimUserProperty = null;
		PrimeFaces.current().ajax().update("claimForm");
		PrimeFaces.current().executeScript("PF('claimDialog').show();");
	}

	public void deleteClaim() {
		if (selectedClaim == null) {
			JsfUtils.addErrorMessage("Please select a Claim");
			return;
		}
		idpSettings.getClaims().remove(selectedClaim);
		PrimeFaces.current().executeScript("PF('claimDialog').hide();");
		PrimeFaces.current().ajax().update("claimForm");
	}

	public void editClaim() {
		if (selectedClaim == null) {
			JsfUtils.addErrorMessage("Please select a Claim");
			return;
		}
		editingClaim = true;
		selectedClaimUserProperty = selectedClaim.getAttributeTypeEnum().name();
		PrimeFaces.current().executeScript("PF('claimDialog').show();");
		PrimeFaces.current().ajax().update("claimForm");
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

	public List<SelectItem> getSelectableClaims() {
		if (selectableStandardClaims == null) {
			selectableStandardClaims = new LinkedList<>();
			for (OpenIdClaim claim : OpenIdClaim.values()) {
				if (!nonEditableStandardClaims.contains(claim)) {
					selectableStandardClaims.add(new SelectItem(claim.getValue()));
				}
			}
		}
		return selectableStandardClaims;
	}

	public boolean isClaimWithValue() {
		boolean returnValue = false;
		if (selectedClaim == null || selectedClaim.getAttributeTypeEnum() == null) {
			return false;
		}
		switch (selectedClaim.getAttributeTypeEnum()) {
		case CLOUD_SAFE_USER:
			// #if COMMUNITY_EDITION == false
		case DOMAIN_ATTRIBUTE:
		case GROUPS:
			// #endif
		case USER_INPUT:
			returnValue = true;
			break;
		default:
			break;
		}
		return returnValue;
	}

	public void listenerChangeUserPropertyType() {
		selectedClaim.setProperty(AttributeTypeEnum.valueOf(selectedClaimUserProperty));
	}

	public String getSelectedClaimUserProperty() {
		return selectedClaimUserProperty;
	}

	public void setSelectedClaimUserProperty(String selectedClaimUserProperty) {
		this.selectedClaimUserProperty = selectedClaimUserProperty;
	}

	public OAuthIdpSettings getIdpSettings() {
		return idpSettings;
	}

	public void setIdpSettings(OAuthIdpSettings idpSettings) {
		this.idpSettings = idpSettings;
	}

	public ClaimAttribute getSelectedClaim() {
		return selectedClaim;
	}

	public void setSelectedClaim(ClaimAttribute selectedClaim) {
		this.selectedClaim = selectedClaim;
	}

	public boolean isEditingClaim() {
		return editingClaim;
	}

	public void setEditingClaim(boolean editingClaim) {
		this.editingClaim = editingClaim;
	}
}
