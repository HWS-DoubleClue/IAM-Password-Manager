package com.doubleclue.dcem.admin.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.DepartmentLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DcemUserExtension;
import com.doubleclue.dcem.core.entities.DepartmentEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoDialogBean;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.gui.ViewNavigator;
import com.doubleclue.dcem.core.jpa.JpaEntityCacheLogic;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.utils.RandomUtils;

@SuppressWarnings("serial")
@Named("userDialog")
@SessionScoped
public class UserDialogBean extends DcemDialog {

	private Logger logger = LogManager.getLogger(UserDialogBean.class);

	@Inject
	UserLogic userLogic;

	@Inject
	DomainLogic domainLogic;

	@Inject
	GroupLogic groupLogic;

	@Inject
	AdminModule adminModule;

	@Inject
	JpaLogic jpaLogic;

	@Inject
	RoleLogic roleLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	JpaEntityCacheLogic jpaEntityCacheLogic;

	@Inject
	ViewNavigator viewNavigator;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	DepartmentLogic departmentLogic;

	boolean leaving;

	private String domainName;
	private String loginId;
	private String newPassword;
	private List<DcemGroup> groups;
	private String userType = DcemConstants.TYPE_LOCAL;
	private LinkedList<SelectItem> availableRoles;
	String country;
	private String continentTimezone;
	private String countryTimezone;
	private boolean defaultTimezone;
	String department;
	String jobTitle;

	private byte[] photoProfile;
	private UploadedFile uploadPhotoProfile;

	DcemUser dcemUser;
	DcemAction dcemAction;

	public void actionAddSave() throws Exception {
		try {
			if (isUserProfile()) {
				if (addSave() == true) {
					actionCloseDialog();
				}
			} else {
				if (addSave() == true) {
					actionCloseDialog();
				}
			}
		} catch (DcemException dcemExp) {
			logger.warn("OK Action Failed", dcemExp);
			switch (dcemExp.getErrorCode()) {
			case CONSTRAIN_VIOLATION_DB:
				JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "db.constrain.at.insert", (Object[]) null);
				return;
			case CONSTRAIN_VIOLATION:
				JsfUtils.addErrorMessage("Verification Error: " + dcemExp.getMessage());
				return;
			case EXCEPTION:
				JsfUtils.addErrorMessage("Something went wrong. Cause: " + dcemExp.getMessage());
				return;
			default:
				JsfUtils.addErrorMessage(dcemExp.getLocalizedMessage());
				return;

			}
		} catch (Exception exp) {
			ConstraintViolationException constrainViolation = DcemUtils.getConstainViolation(exp);
			if (constrainViolation != null) {
				JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "db.constrain.at.insert", (Object[]) null);
			} else {
				JsfUtils.addErrorMessage(exp.toString());
				logger.warn("OK Action Failed", exp);
			}
			return;
		}
		JsfUtils.addFacesInformationMessage("successful", "mainMessages");
		return;
	}

	private boolean addSave() throws Exception {
		if (userOutranksOperator()) {
			throw new DcemException(DcemErrorCodes.CANNOT_EDIT_OUTRANKING_USER, dcemUser.getLoginId());
		} else {
			if (loginId == null) {
				JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "userDialog.loginId.empty");
				return false;
			}
			loginId = loginId.trim();
			if (userType.equals(DcemConstants.TYPE_DOMAIN)) {
				if (loginId.isEmpty()) {
					JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "userDialog.loginId.empty");
					return false;
				}
				dcemUser.setLoginId(domainName + DcemConstants.DOMAIN_SEPERATOR + loginId);
			} else {
				if (loginId.indexOf('\\') != -1) {
					throw new DcemException(DcemErrorCodes.ID_WITH_SPECIAL_CHARACTERS, dcemUser.getLoginId());
				}
				dcemUser.setLoginId(loginId);
				dcemUser.setUserDn(null);
			}
			DepartmentEntity departmentEntity = null;
			if (department != null && department.isEmpty() == false) {
				departmentEntity = departmentLogic.getDepartmentByName(department);
				if (departmentEntity == null) {
					JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "userDialog.error.invalidDepartment");
					return false;
				}
			}
			dcemUser = userLogic.addOrUpdateUser(dcemUser, dcemAction, true,
					adminModule.getPreferences().isNumericPassword(),
					adminModule.getPreferences().getUserPasswordLength(), false);
			DcemUserExtension dcemUserExtension = new DcemUserExtension();
			dcemUserExtension.setCountry(country);
			dcemUserExtension.setJobTitle(jobTitle);
			dcemUserExtension.setPhoto(photoProfile);
			if (defaultTimezone) {
				dcemUserExtension.setTimezoneString(null);
			} else {
				dcemUserExtension.setTimezoneString(countryTimezone);
			}
			dcemUserExtension.setDepartment(departmentEntity);
			userLogic.updateDcemUserExtension(dcemAction, dcemUser, dcemUserExtension);
			if (dcemUser.getId() == operatorSessionBean.getDcemUser().getId()) {
				FacesContext.getCurrentInstance().getViewRoot().setLocale(dcemUser.getLanguage().getLocale());
				operatorSessionBean.setDcemUser(dcemUser);
				viewNavigator.setMenuModel(null); // refresh menu
			}
			return true;
		}
	}

	@Override
	public void actionConfirm() throws Exception {
		List<Object> userList = autoViewBean.getSelectedItems();
		int operatorRank = getOperatorRank();
		boolean outranks = false;
		for (Object object : userList) {
			DcemUser user = (DcemUser) object;
			if (user.getDcemRole().getRank() > operatorRank) {
				JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "userDialog.error.userOutranksOperator");
				outranks = true;
			}
		}
		if (!outranks) {
			try {
				userLogic.deleteUsers(userList, getAutoViewAction().getDcemAction());
			} catch (DcemException semExp) {
				if (semExp.getErrorCode() == DcemErrorCodes.CONSTRAIN_VIOLATION_DB) {
					JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "db.constrain.at.delete", (Object[]) null);
				} else {
					logger.error("Delete user failed", semExp);
					JsfUtils.addErrorMessage(semExp.toString());
				}
			} catch (Exception exp) {
				JsfUtils.addErrorMessage(exp.toString());
				logger.error("Delete user failed", exp);
			}
		}
	}

	public void actionResetPassword() throws DcemException {
		if (userOutranksOperator()) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "userDialog.error.userOutranksOperator");
		} else {
			if (newPassword == null || newPassword.isEmpty()) {
				JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "userDialog.error.emptyPassword");
			} else {
				try {
					dcemUser.setInitialPassword(newPassword);
					userLogic.addOrUpdateUser(dcemUser, getAutoViewAction().getDcemAction(), true,
							adminModule.getPreferences().isNumericPassword(),
							adminModule.getPreferences().getUserPasswordLength(), false);
					super.dialogReturn(null);
				} catch (DcemException e) {
					JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "userDialog.error.userOutranksOperator");
				}
			}
		}
	}

	public void actionEnableUser() {
		DcemUser dcemUser = (DcemUser) getActionObject();
		if (userOutranksOperator()) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "userDialog.error.userOutranksOperator");
		} else {
			try {
				userLogic.enableUser(dcemUser, getAutoViewAction().getDcemAction());
				JsfUtils.addInformationMessage(AdminModule.RESOURCE_NAME, "userIsEnabled");
				PrimeFaces.current().ajax().update("autoForm:pTable");
			} catch (Exception e) {
				JsfUtils.addErrorMessage(e.getMessage());
			}
		}
	}

	public void actionResetStayLogin() {
		DcemUser dcemUser = (DcemUser) getActionObject();
		try {
			userLogic.resetStayLogin(dcemUser, getAutoViewAction().getDcemAction());
			JsfUtils.addInformationMessage(AdminModule.RESOURCE_NAME, "resetStayLoginMsg");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getMessage());
		}
	}

	public StreamedContent getPhotoUserProfile() {
		DcemUserExtension userExtension = dcemUser.getDcemUserExt();
		if (photoProfile != null) {
			InputStream in = new ByteArrayInputStream(photoProfile);
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
		} else if (userExtension != null && userExtension.getPhoto() != null) {
			InputStream in = new ByteArrayInputStream(userExtension.getPhoto());
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
		} else {
			return JsfUtils.getDefaultUserImage();
		}
	}

	public void actionDisableUser() {
		dcemUser = (DcemUser) getActionObject();
		if (userOutranksOperator()) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "userDialog.error.userOutranksOperator");
		} else {
			try {
				userLogic.disableUser(dcemUser, getAutoViewAction().getDcemAction());
				JsfUtils.addInformationMessage(AdminModule.RESOURCE_NAME, "userIsDisabled");
				PrimeFaces.current().ajax().update("autoForm:pTable");
			} catch (Exception e) {
				JsfUtils.addErrorMessage(e.getMessage());
			}
		}
	}

	public void changeType() {
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public List<DcemGroup> getGroups() {
		if (groups != null || leaving == true) {
			return groups;
		}
		try {
			groups = groupLogic.getAllUserGroups(dcemUser, false);
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.toString());
		}
		return groups;
	}

	public List<SelectItem> getDomainNames() {
		List<SelectItem> list = domainLogic.getDomainNames();
		if (list.size() == 0) {
			JsfUtils.addErrorMessage("No LDAP configuration found");
			return null;
		}
		if (domainName == null && list.size() > 0) {
			domainName = (String) list.get(0).getValue();
		}
		return list;
	}

	public String getLoginId() {
		return loginId;
	}

	public List<String> completeUser(String userFilter) {
		if (domainName == null || domainName.isEmpty()) {
			return null;
		}
		try {
			return domainLogic.getUserNames(domainName, userFilter + "*");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			return null;
		}
	}

	public List<String> completeDepartment(String name) {
		return departmentLogic.getCompleteDepartmentList(name, 50);
	}

	public void setLoginId(String loginId) {
		this.loginId = loginId;
	}

	public List<SelectItem> getAvailableCountries() {
		return applicationBean.getAvailableCountries(operatorSessionBean.getLocale());
	}

	public void showMyProfile() throws Exception {
		dcemUser = userLogic.getUser(operatorSessionBean.getDcemUser().getId());
		dcemAction = new DcemAction(AdminModule.MODULE_ID, DcemConstants.SUBJECT_TITLE_BAR,
				DcemConstants.ACTION_USER_PROFILE);
		updateShow();
	}

	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		dcemUser = (DcemUser) this.getActionObject();
		if (dcemUser.isDomainUser()
				&& autoViewAction.getRawAction().getName().equals(DcemConstants.ACTION_RESET_PASSWORD)) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "userDialog.resetPassword.notAllowed");
		}
		dcemAction = autoViewAction.getDcemAction();
		updateShow();
	}

	private void updateShow() throws Exception {
		userType = dcemUser.isDomainUser() == true ? DcemConstants.TYPE_DOMAIN : DcemConstants.TYPE_LOCAL;
		if (dcemUser.getLoginId() != null) {
			String[] domainUser = dcemUser.getLoginId().split(DcemConstants.DOMAIN_SEPERATOR_REGEX);
			if (domainUser.length > 1) {
				domainName = domainUser[0];
				loginId = domainUser[1];
			} else {
				loginId = dcemUser.getLoginId();
			}
		} else {
			loginId = null;
			domainName = null;
		}
		department = null;
		if (dcemUser.getDcemUserExt() == null) {
			country = null;
			jobTitle = null;
		} else {
			country = dcemUser.getDcemUserExt().getCountry();
			jobTitle = dcemUser.getDcemUserExt().getJobTitle();
			if (dcemUser.getDcemUserExt().getDepartment() != null) {
				department = dcemUser.getDcemUserExt().getDepartment().getName();
			}
		}
		updateTimeZone(dcemUser.getDcemUserExt());
		if (country == null) {
			if (adminModule.getPreferences().getUserDefaultLanguage() == SupportedLanguage.German) {
				country = DcemConstants.COUNTRY_CODE_GERMAN;
			} else {
				country = DcemConstants.COUNTRY_CODE_MALTA;
			}
		}
		groups = null;
		newPassword = null;
		if (userOutranksOperator()) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "userDialog.error.userOutranksOperator");
		}
		photoProfile = null;
		leaving = false;
	}

	public void leavingDialog() {
		groups = null;
		availableRoles = null;
		leaving = true;
		continentTimezone = null;
		countryTimezone = null;
		uploadPhotoProfile = null;
		dcemUser = null;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public LinkedList<SelectItem> getAvailableRoles() {
		if (availableRoles == null) {
			availableRoles = new LinkedList<SelectItem>();
			List<DcemRole> roles = roleLogic.getAllDcemRoles();
			int operatorRank = getOperatorRank();
			for (DcemRole role : roles) {
				availableRoles.add(new SelectItem(role, role.getName(), null, role.getRank() > operatorRank));
			}
		}
		return availableRoles;
	}

	public boolean isResetPasswordDisabled() {
		if (dcemUser.isDomainUser()) {
			return true;
		}
		return userOutranksOperator();
	}

	public String getSelectedUserLoginId() {
		return dcemUser.getLoginId();
	}

	public boolean userOutranksOperator() {
		if (dcemUser == null) {
			return false;
		}
		DcemRole userRole = dcemUser.getDcemRole();
		return (userRole != null) ? userRole.getRank() > getOperatorRank() : false;
	}

	private int getOperatorRank() {
		return operatorSessionBean.getHighestRole().getRank();
	}

	public String getSelectedRole() {
		return (dcemUser.getDcemRole() != null) ? dcemUser.getDcemRole().getName()
				: ((DcemRole) getAvailableRoles().getLast().getValue()).getName();
	}

	public void setSelectedRole(String selectedRole) {
		if (dcemUser != null) {
			for (SelectItem selectItem : getAvailableRoles()) {
				DcemRole role = (DcemRole) selectItem.getValue();
				if (role.getName().equals(selectedRole)) {
					dcemUser.setDcemRole(role);
					break;
				}
			}
		}
	}

	public StreamedContent getPhoto() {
		DcemUserExtension dcemUserExtension = dcemUser.getDcemUserExt();
		if (dcemUserExtension != null && dcemUserExtension.getPhoto() != null) {
			byte[] image = dcemUserExtension.getPhoto();
			InputStream in = new ByteArrayInputStream(image);
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
		} else {
			return JsfUtils.getDefaultUserImage();
		}
	}

	public String getReportsTo() {
		if (dcemUser == null) {
			return null;
		}
		DcemUserExtension dcemUserExtension = dcemUser.getDcemUserExt();
		if (dcemUserExtension != null && dcemUserExtension.getDepartment() != null
				&& dcemUserExtension.getDepartment().getHeadOf() != null) {
			return dcemUserExtension.getDepartment().getHeadOf().getDisplayName();
		}
		return null;
	}

	private void updateTimeZone(DcemUserExtension dcemUserExtension) {
		TimeZone timeZone;
		if (dcemUserExtension == null || dcemUserExtension.getTimezone() == null) {
			defaultTimezone = true;
			timeZone = adminModule.getTimezone();
		} else {
			defaultTimezone = false;
			timeZone = dcemUserExtension.getTimezone();
		}
		setContinentAndCountryTimezone(timeZone);
	}

	private void setContinentAndCountryTimezone(TimeZone timeZone) {
		countryTimezone = timeZone.getID();
		continentTimezone = DcemUtils.getContinentFromTimezone(countryTimezone);
	}

	public List<SelectItem> getContinentTimezones() {
		if (defaultTimezone == true) {
			setContinentAndCountryTimezone(adminModule.getTimezone());
		}
		return DcemUtils.getContinentTimezones();
	}

	public List<SelectItem> getCountryTimezones() {
		return DcemUtils.getCountryTimezones(continentTimezone);
	}

	public void actionGeneratePassword() {
		newPassword = RandomUtils.generateRandomPasswordExtAlphaNumeric(8);
		return;
	}

	public void photoProfileListener(FileUploadEvent event) {
		if (event == null) {
			return;
		}
		try {
			photoProfile = DcemUtils.resizeImage(event.getFile().getContent(), DcemConstants.PHOTO_MAX);
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			logger.error("upload photo failed " + e.toString());
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			logger.error("upload photo failed " + e.toString());
		}
		return;
	}

	public void actionCloseDialog() {
		if (isUserProfile() == true) {
			leavingDialog();
			PrimeFaces.current().dialog().closeDynamic(null);
		} else {
			viewNavigator.actionCloseDialog();
		}
	}
	
	public String getMemberOf () {
		StringBuilder sb = new StringBuilder();
		for (DcemGroup dcemGroup : operatorSessionBean.getUserGroups()) {
			if (sb.isEmpty() == false) {
				sb.append(", ");
			}
			sb.append(dcemGroup.getShortName());
		}
		return sb.toString();
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getWidth() {
		return "850px";
	}

	public String getHeight() {
		return "750px";
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getDepartment() {
		return department;
	}

	public void setDepartment(String department) {
		this.department = department;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public void setJobTitle(String jobTitle) {
		this.jobTitle = jobTitle;
	}

	public String getContinentTimezone() {
		return continentTimezone;
	}

	public void setContinentTimezone(String continentTimezone) {
		this.continentTimezone = continentTimezone;
	}

	public String getCountryTimezone() {
		return countryTimezone;
	}

	public void setCountryTimezone(String countryTimezone) {
		this.countryTimezone = countryTimezone;
	}

	public boolean isDefaultTimezone() {
		return defaultTimezone;
	}

	public void setDefaultTimezone(boolean defaultTimezone) {
		this.defaultTimezone = defaultTimezone;
	}

	public boolean isUserProfile() {
		return dcemAction.getAction() == DcemConstants.ACTION_USER_PROFILE;
	}

	public UploadedFile getUploadPhotoProfile() {
		return uploadPhotoProfile;
	}

	public void setUploadPhotoProfile(UploadedFile uploadPhotoProfile) {
		this.uploadPhotoProfile = uploadPhotoProfile;
	}

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
	}
}
