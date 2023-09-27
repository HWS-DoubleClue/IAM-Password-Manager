package com.doubleclue.dcem.admin.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.DepartmentLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DcemUserExtension;
import com.doubleclue.dcem.core.entities.DepartmentEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
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
import com.doubleclue.utils.StringUtils;

@SuppressWarnings("serial")
@Named("userDialog")
@SessionScoped
public class UserDialogBean extends DcemDialog {

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

	public boolean actionOk() throws Exception {
		DcemUser user = (DcemUser) getActionObject();
		if (userOutranksOperator()) {
			throw new DcemException(DcemErrorCodes.CANNOT_EDIT_OUTRANKING_USER, user.getLoginId());
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
				user.setLoginId(domainName + DcemConstants.DOMAIN_SEPERATOR + loginId);
			} else {
				if (loginId.indexOf('\\') != -1) {
					throw new DcemException(DcemErrorCodes.ID_WITH_SPECIAL_CHARACTERS, user.getLoginId());
				}
				user.setLoginId(loginId);
				user.setUserDn(null);
			}
			DepartmentEntity departmentEntity = null;
			if (department != null && department.isEmpty() == false) {
				departmentEntity = departmentLogic.getDepartmentByName(department);
				if (departmentEntity == null) {
					JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "userDialog.error.invalidDepartment");
					return false;
				}
			}
			userLogic.addOrUpdateUser(user, getAutoViewAction().getDcemAction(), true,
					adminModule.getPreferences().isNumericPassword(),
					adminModule.getPreferences().getUserPasswordLength(), false);
			DcemUserExtension dcemUserExtension = new DcemUserExtension();
			dcemUserExtension.setCountry(country);
			dcemUserExtension.setJobTitle(jobTitle);
			if (defaultTimezone) {
				dcemUserExtension.setTimezoneString(null);
			} else {
				dcemUserExtension.setTimezoneString(countryTimezone);
			}
			dcemUserExtension.setDepartment(departmentEntity);
			userLogic.updateDcemUserExtension(user, dcemUserExtension);
			StringUtils.wipeString(user.getInitialPassword());
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
					JsfUtils.addErrorMessage(semExp.toString());
				}
			}
		}
	}

	public void actionResetPassword() throws DcemException {
		DcemUser user = (DcemUser) getActionObject();
		if (userOutranksOperator()) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "userDialog.error.userOutranksOperator");
		} else {
			if (newPassword == null || newPassword.isEmpty()) {
				JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "userDialog.error.emptyPassword");
			} else {
				try {
					user.setInitialPassword(newPassword);
					userLogic.addOrUpdateUser(user, getAutoViewAction().getDcemAction(), true,
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

	public void actionDisableUser() {
		DcemUser dcemUser = (DcemUser) getActionObject();
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
		DcemUser user = (DcemUser) getActionObject();
		try {
			groups = groupLogic.getAllUserGroups(user, false);
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

	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		DcemUser user = (DcemUser) this.getActionObject();
		if (user.isDomainUser()
				&& autoViewAction.getRawAction().getName().equals(DcemConstants.ACTION_RESET_PASSWORD)) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "userDialog.resetPassword.notAllowed");
		}

		userType = user.isDomainUser() == true ? DcemConstants.TYPE_DOMAIN : DcemConstants.TYPE_LOCAL;
		if (user.getLoginId() != null) {
			String[] domainUser = user.getLoginId().split(DcemConstants.DOMAIN_SEPERATOR_REGEX);
			if (domainUser.length > 1) {
				domainName = domainUser[0];
				loginId = domainUser[1];
			} else {
				loginId = user.getLoginId();
			}
		} else {
			loginId = null;
			domainName = null;
		}
		department = null;
		if (user.getDcemUserExt() == null) {
			country = null;
			jobTitle = null;
		} else {
			country = user.getDcemUserExt().getCountry();
			jobTitle = user.getDcemUserExt().getJobTitle();
			if (user.getDcemUserExt().getDepartment() != null) {
				department = user.getDcemUserExt().getDepartment().getName();
			}
		}
		updateTimeZone(user.getDcemUserExt());
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
		leaving = false;
	}

	public void leavingDialog() {
		groups = null;
		availableRoles = null;
		leaving = true;
		continentTimezone = null;
		countryTimezone = null;

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
		DcemUser user = (DcemUser) getActionObject();
		if (user.isDomainUser()) {
			return true;
		}
		return userOutranksOperator();
	}

	public String getSelectedUserLoginId() {
		return ((DcemUser) getActionObject()).getLoginId();
	}

	public boolean userOutranksOperator() {
		DcemUser user = (DcemUser) this.getActionObject();
		DcemRole userRole = user.getDcemRole();
		return (userRole != null) ? userRole.getRank() > getOperatorRank() : false;
	}

	private int getOperatorRank() {
		return operatorSessionBean.getHighestRole().getRank();
	}

	public String getSelectedRole() {
		DcemUser user = (DcemUser) this.getActionObject();
		return (user.getDcemRole() != null) ? user.getDcemRole().getName()
				: ((DcemRole) getAvailableRoles().getLast().getValue()).getName();
	}

	public void setSelectedRole(String selectedRole) {
		DcemUser user = (DcemUser) this.getActionObject();
		if (user != null) {
			for (SelectItem selectItem : getAvailableRoles()) {
				DcemRole role = (DcemRole) selectItem.getValue();
				if (role.getName().equals(selectedRole)) {
					user.setDcemRole(role);
					break;
				}
			}
		}
	}

	public StreamedContent getPhoto() {
		DcemUser user = (DcemUser) this.getActionObject();
		DcemUserExtension dcemUserExtension = user.getDcemUserExt();
		if (dcemUserExtension != null && dcemUserExtension.getPhoto() != null) {
			byte[] image = dcemUserExtension.getPhoto();
			InputStream in = new ByteArrayInputStream(image);
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
		} else {
			return JsfUtils.getDefaultUserImage();
		}
	}

	public String getReportsTo() {
		DcemUser user = (DcemUser) this.getActionObject();
		DcemUserExtension dcemUserExtension = user.getDcemUserExt();
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
		if (defaultTimezone) {
			setContinentAndCountryTimezone(adminModule.getTimezone());
		}
		return DcemUtils.getContinentTimezones();
	}

	public List<SelectItem> getCountryTimezones() {
		return DcemUtils.getCountryTimezones(continentTimezone);
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
}
