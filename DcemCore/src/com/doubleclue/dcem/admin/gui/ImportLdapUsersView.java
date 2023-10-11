package com.doubleclue.dcem.admin.gui;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.SendByEnum;
import com.doubleclue.dcem.admin.subjects.ImportLdapUsersSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AsModuleApi;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.DomainApi;
import com.doubleclue.dcem.core.logic.DomainAzure;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.DomainType;
import com.doubleclue.dcem.core.logic.DomainUsers;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;

@Named("importLdapUsersView")
@SessionScoped
public class ImportLdapUsersView extends DcemView {

	@Inject
	private AdminModule adminModule;

	@Inject
	private ImportLdapUsersSubject importLdapUsersSubject;

	@Inject
	private DomainDialogBean domainDialogBean;

	@Inject
	private DomainLogic domainLogic;

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	UserLogic userLogic;

	@Inject
	GroupLogic groupLogic;

	@Inject
	RoleLogic roleLogic;

	private static final Logger logger = LogManager.getLogger(ImportLdapUsersView.class);

	ResourceBundle resourceBundle;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	List<String> selectedUsers = new LinkedList<>();
	List<String> selectedGroups = new LinkedList<>();
	// List<String> users;
	DomainUsers domainUsers;

	List<DcemGroup> groupSearchMap;

	SendByEnum sendBy;
	LocalDateTime validTill;
	boolean createActivationCode;
	boolean createActivationCodeExisting;

	SendByEnum sendByGroup;
	LocalDateTime validTillGroup;
	boolean createActivationCodeGroup;
	boolean createActivationCodeExistingGroup;

	String group;
	String ldapTree;
	String userAccount;

	String groupFilter;

	String domainName;

	int countUsers = 0;
	int countExistingUsers = 0;
	int countActivations = 0;
	AsModuleApi asModuleApi = null;

	@PostConstruct
	protected void init() {
		resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		domainDialogBean.setParentView(this);
		subject = importLdapUsersSubject;
		asModuleApi = (AsModuleApi) CdiUtils.getReference(DcemConstants.AS_MODULE_API_IMPL_BEAN);
		reload();
	}
	
	@Override
	public void reload() {
		validTill = operatorSessionBean.getUserZonedTime(asModuleApi.getActivationCodeDefaultValidTill());
		validTillGroup = validTill;
		List<SelectItem> list = domainLogic.getDomainNames();
		if (list.isEmpty() == false) {
			domainName = list.get(0).getValue().toString();
		}
	}

	public void searchGroup() {
		if (domainName == null || domainName.isEmpty()) {
			JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "importDomain.user.noDomainConfigured");
			return;
		}

		try {
			groupSearchMap = domainLogic.getGroups(domainName, groupFilter);
		} catch (DcemException e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());

		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	public void search() {
		if (domainName == null || domainName.isEmpty()) {
			JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "importDomain.user.noDomain");
			return;
		}
		DcemGroup dcemGroup = null;
		try {
			if (group != null && group.isEmpty() == false) {
				List<DcemGroup> groups = domainLogic.getGroups(domainName, group);
				if (groups.isEmpty() == false && groups.size() == 1) {
					dcemGroup = groups.get(0);
				}
			}
			if (userAccount != null && userAccount.isEmpty() == false) {
				domainUsers = domainLogic.getUsers(domainName, ldapTree, dcemGroup, userAccount, DomainLogic.PAGE_SIZE);
				if (domainUsers.isNextPageAvaliable() == true) {
					JsfUtils.addWarnMessage("Your search exided the maximum limit of " + domainUsers.getPageSize());
					JsfUtils.addInfoMessage("Please restrict your search." );
				}
			}
		} catch (DcemException e) {
			logger.warn(e);
			if (e.getErrorCode() == DcemErrorCodes.UNEXPECTED_ERROR) {
				JsfUtils.addErrorMessage(e.toString());
				return;
			}
			JsfUtils.addErrorMessage(e.getLocalizedMessage());

		} catch (Exception e) {
			logger.warn("Search User", e);
			JsfUtils.addErrorMessage(e.toString());
		}
	}

	/**
	 * @param withUsers
	 */
	public void actionImportGroups(boolean withUsers) {
		if (selectedGroups.isEmpty()) {
			JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "importDomain.group.no_selection");
			return;
		}

		DcemGroup dcemGroup;
		boolean existingGroup;
		int countGroups = 0;
		int countGroupActivations = 0;
		int countNewUsers = 0;

		sendBy = sendByGroup;
		createActivationCodeGroup = createActivationCode;
		createActivationCodeExistingGroup = createActivationCodeExisting;
		DcemGroup selectedGroup = null;
		validTill = validTillGroup;
		for (String groupName : selectedGroups) {
			selectedGroup = null;
			try {
				for (DcemGroup group : groupSearchMap) {
					if (group.getShortName().equals(groupName)) {
						selectedGroup = group;
						break;
					}
				}
				dcemGroup = groupLogic.getGroup(domainName, groupName);
				if (dcemGroup == null) {
					dcemGroup = selectedGroup;
					existingGroup = false;
					groupLogic.addOrUpdateGroupWoAuditing(dcemGroup);
				} else {
					existingGroup = true;
				}
			} catch (Exception e) {
				JsfUtils.addWarningMessage(DcemConstants.CORE_RESOURCE, "importDomain.group.error", groupName, e.toString());
				continue;
			}
			if (existingGroup == false) {
				countGroups++;
			}

			if (withUsers) {
				try {
					domainUsers = domainLogic.getUsers(domainName, null, dcemGroup, (String) null, DomainLogic.PAGE_SIZE);
					selectAll();
					importUsers(domainName);
					countNewUsers += countUsers;
					countGroupActivations += countActivations;
				} catch (DcemException e) {
					logger.warn(e);
				}
			}
		}
		JsfUtils.addInformationMessage(DcemConstants.CORE_RESOURCE, "importDomain.groupResult", countGroups, countNewUsers, countGroupActivations);

	}

	public SendByEnum getSendByGroup() {
		return sendByGroup;
	}

	public void setSendByGroup(SendByEnum sendByGroup) {
		this.sendByGroup = sendByGroup;
	}

	public LocalDateTime getValidTillGroup() {
		return validTillGroup;
	}

	public void setValidTillGroup(LocalDateTime validTillGroup) {
		this.validTillGroup = validTillGroup;
	}

	public boolean isCreateActivationCodeGroup() {
		return createActivationCodeGroup;
	}

	public void setCreateActivationCodeGroup(boolean createActivationCodeGroup) {
		this.createActivationCodeGroup = createActivationCodeGroup;
	}

	public boolean isCreateActivationCodeExistingGroup() {
		return createActivationCodeExistingGroup;
	}

	public void setCreateActivationCodeExistingGroup(boolean createActivationCodeExistingGroup) {
		this.createActivationCodeExistingGroup = createActivationCodeExistingGroup;
	}

	/**
	 * 
	 */
	public void actionImportUsers() {
		if (selectedUsers.isEmpty()) {
			JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "importDomain.user.no_selection");
		}
		importUsers(domainName);
		JsfUtils.addInformationMessage(DcemConstants.CORE_RESOURCE, "importDomain.result", countUsers, countExistingUsers, countActivations);
	}

	public void selectAll() {
		selectedUsers = getUsers();
	}

	public void deselectAll() {
		selectedUsers = new LinkedList<>();
	}

	public void selectAllGroups() {
		selectedGroups = getGroups();
	}

	public void deselectAllGroups() {
		selectedGroups = new LinkedList<>();
	}

	public List<String> completeGroup(String groupFilter) {
		if (domainName == null || domainName.isEmpty()) {
			JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "importDomain.user.noDomain");
			return null;
		}
		try {
			List<DcemGroup> groups = domainLogic.getGroups(domainName, groupFilter + "*", 30);
			List<String> completeGroups = new ArrayList<>(groups.size());
			for (DcemGroup dcemGroup : groups) {
				completeGroups.add(dcemGroup.getName().substring(domainName.length() + 1));
			}
			return completeGroups;
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.toString());
			return null;
		}
	}

	public List<String> completeLdapTree(String treeFilter) throws DcemException {
		if (treeFilter.endsWith(",") == false) {
			return null;
		}

		DomainApi domainApi = null;
		if (domainName == null || domainName.isEmpty()) {
			JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "importDomain.user.noDomain");
			return null;
		}
		domainApi = domainLogic.getDomainApi(domainName);
		try {
			return domainLogic.getSelectedTree(domainName, treeFilter, 50);
		} catch (Exception e) {
			logger.warn(e);
			JsfUtils.addErrorMessage(e.toString());
			return null;
		}
	}

	private void importUsers(String domainName) {
		boolean existingUser;
		AsModuleApi asModuleApi = null;
		if (createActivationCode) {
			asModuleApi = (AsModuleApi) CdiUtils.getReference(DcemConstants.AS_MODULE_API_IMPL_BEAN);
		}
		countExistingUsers = 0;
		countUsers = 0;
		countActivations = 0;
		DcemUser selectedUser;
		DcemRole userRole = roleLogic.getDcemRole(DcemConstants.SYSTEM_ROLE_USER);
		DomainApi domainApi = null;
		try {
			domainApi = domainLogic.getDomainApi(domainName);
		} catch (DcemException e1) {
		}
		for (String userName : selectedUsers) {
			selectedUser = null;
			DcemUser dcemUser;
			try {
				for (DcemUser user : domainUsers.getUsers()) {
					if (user.getShortLoginId().equals(userName)) {
						selectedUser = user;
						break;
					}
				}
				dcemUser = userLogic.getUser(domainName, userName);
				if (dcemUser == null) {
					dcemUser = selectedUser;
					dcemUser.setLanguage(adminModule.getPreferences().getUserDefaultLanguage());
					dcemUser.setDcemRole(userRole);
					existingUser = false;
				} else {
					existingUser = true;
				}
				dcemUser.updateDomainAttributes(selectedUser.getDcemLdapAttributes());
				userLogic.addOrUpdateUserWoAuditing(dcemUser);
				byte[] photo = domainApi.getUserPhoto(dcemUser);
				if (photo == null && domainApi.getDomainEntity().getDomainType() == DomainType.Active_Directory) {
					DomainAzure domainAzure = (DomainAzure) domainLogic.getDomainFromEmail(dcemUser.getUserPrincipalName(), DomainType.Azure_AD);
					if (domainAzure != null) {
						try {
							photo = domainAzure.getUserPhoto(dcemUser);
						} catch (Exception e) {
						}
					}
				}
				userLogic.updateExtention(dcemUser, photo, selectedUser.getDcemLdapAttributes());
			} catch (Exception e) {
				logger.info("Importing users", e);
				JsfUtils.addWarningMessage(DcemConstants.CORE_RESOURCE, "importDomain.user.error", userName, e.toString());
				continue;
			}
			if (existingUser) {
				countExistingUsers++;
			} else {
				countUsers++;
			}
			if (createActivationCode) {
				if (existingUser && createActivationCodeExisting == false) {
					continue;
				}
				try {
					asModuleApi.createActivationCode(dcemUser, operatorSessionBean.getDefaultZonedTime(validTill), sendBy, "Domain Import");
					countActivations++;
				} catch (DcemException e) {
					if (e.getErrorCode() == DcemErrorCodes.EMAIL_SEND_MSG_LIMIT) {
						JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "importDomain.createAc.emailLimit", userName, e.getLocalizedMessage());
						return;
					}
					JsfUtils.addWarningMessage(DcemConstants.CORE_RESOURCE, "importDomain.createAc.error", userName, e.getLocalizedMessage());

				} catch (Exception e) {
					JsfUtils.addWarningMessage(DcemConstants.CORE_RESOURCE, "importDomain.createAc.error", userName, e.toString());
				}
			}

		}

	}

	public List<String> getSelectedUsers() {
		return selectedUsers;
	}

	public void setSelectedUsers(List<String> selectedUsers) {
		this.selectedUsers = selectedUsers;
	}

	public List<String> getUsers() {
		List<String> userList = new LinkedList<>();
		if (domainName == null || domainName.isEmpty()) {
			return userList;
		}
		if (domainUsers == null) {
			return userList;
		}
		for (DcemUser user : domainUsers.getUsers()) {
			userList.add(user.getShortLoginId());
		}
		userList.sort(new SortIgnoreCase());
		return userList;
	}

	public List<String> getGroups() {
		List<String> groups = new LinkedList<>();
		if (groupSearchMap != null) {
			for (DcemGroup group : groupSearchMap) {
				groups.add(group.getName().substring(domainName.length() + 1));
			}
		}
		groups.sort(new SortIgnoreCase());
		return groups;
	}

	// public void setUsers(List<String> users) {
	// this.users = users;
	// }

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getUserAccount() {
		return userAccount;
	}

	public void setUserAccount(String userAccount) {
		this.userAccount = userAccount;
	}

	public SendByEnum[] getSendByValues() {
		return SendByEnum.values();
	}

	public SendByEnum getSendBy() {
		return sendBy;
	}

	public void setSendBy(SendByEnum sendBy) {
		this.sendBy = sendBy;
	}

	public LocalDateTime getValidTill() {
		return validTill;
	}

	public void setValidTill(LocalDateTime validTill) {
		this.validTill = validTill;
	}

	public boolean isCreateActivationCode() {
		return createActivationCode;
	}

	public void setCreateActivationCode(boolean createActivationCode) {
		this.createActivationCode = createActivationCode;
	}

	public boolean isCreateActivationCodeExisting() {
		return createActivationCodeExisting;
	}

	public void setCreateActivationCodeExisting(boolean createActivationCodeExisting) {
		this.createActivationCodeExisting = createActivationCodeExisting;
	}

	// public String getLdapName() {
	// LdapEntity ldapEntity = ldapLogic.getLdapEntity();
	// if (ldapEntity == null) {
	// return "No LDAP configuration found!";
	// }
	// return ldapEntity.getName();
	// }

	public List<SelectItem> getLdapDomainNames() {
		List<SelectItem> list = domainLogic.getDomainNames();
		if (domainName == null && list.size() > 0) {
			domainName = list.get(0).getValue().toString();
		}
		if (list.isEmpty()) {
			JsfUtils.addErrorMessage(DcemConstants.CORE_RESOURCE, "importDomain.user.noDomainConfigured");
		}
		return list;
	}

	/* (non-Javadoc)
	 * @see com.doubleclue.dcem.core.gui.DcemView#leavingView()
	 */
	@Override
	public void leavingView() {
		selectedUsers = new LinkedList<>();
		domainUsers = null;
		selectedGroups = new LinkedList<>();
		groupSearchMap = null;
		ldapTree = null;
	}

	public List<String> getSelectedGroups() {
		return selectedGroups;
	}

	public void setSelectedGroups(List<String> selectedGroups) {
		this.selectedGroups = selectedGroups;
	}

	public String getGroupFilter() {
		return groupFilter;
	}

	public void setGroupFilter(String groupFilter) {
		this.groupFilter = groupFilter;
	}

	public String getLdapTree() {
		return ldapTree;
	}

	public void setLdapTree(String ldapTree) {
		this.ldapTree = ldapTree;
	}

	public class SortIgnoreCase implements Comparator<Object> {
		public int compare(Object o1, Object o2) {
			String s1 = (String) o1;
			String s2 = (String) o2;
			return s1.toLowerCase().compareTo(s2.toLowerCase());
		}
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public void onChangeDomainName() {
		selectedGroups.clear();
		selectedUsers.clear();
		domainUsers = null;
		groupSearchMap = null;
		ldapTree = null;
	}

	public boolean isLdapDomain() {
		if (domainName == null) {
			return false;
		}
		DomainType domainType;
		try {
			domainType = domainLogic.getDomainType(domainName);
		} catch (DcemException e) {
			return false;
		}
		return domainType != DomainType.Azure_AD;
	}

	public DomainUsers getDomainUsers() {
		return domainUsers;
	}

}
