package com.doubleclue.dcem.as.gui;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.entities.AppPolicyGroupEntity;
import com.doubleclue.dcem.as.entities.PolicyEntity;
import com.doubleclue.dcem.as.policy.DcemPolicy;
import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.as.policy.PolicyTreeItem;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthMethod;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewNavigator;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

@Named("policyDialog")
@SessionScoped
public class PolicyDialog extends DcemDialog {

	private static Logger logger = LogManager.getLogger(PolicyDialog.class);

	@Inject
	PolicyLogic policyLogic;

	@Inject
	GroupLogic groupLogic;

	List<String> allowedMethods;

	@Inject
	PolicyView policyView;

	@Inject
	ViewNavigator viewNavigator;

	@Inject
	UserLogic userLogic;

	Integer assignedGroup;
	Integer assignedPolicy;

	DcemUser dcemUser;

	PolicyEntity testPolicyEntity;

	int priority;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PostConstruct
	private void init() {
	}

	public void actionTestUserPolicy() {
		testPolicyEntity = null;
		if (dcemUser == null) {
			JsfUtils.addErrorMessage("Please select a user.");
			return;
		}
		if (dcemUser == null) {
			JsfUtils.addErrorMessage("Invalid user name.");
			return;
		}
		PolicyTreeItem pti = (PolicyTreeItem) policyView.getSelectedTreeNode().getData();
		if (pti.isGroup()) {
			pti = (PolicyTreeItem) policyView.getSelectedTreeNode().getParent().getData();
		}
		try {
			testPolicyEntity = policyLogic.getPolicy(pti.getAppEntity().getAuthApplication(), pti.getAppEntity().getSubId(), dcemUser);
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		}

	}

	public String getSelectedApplication() {
		PolicyTreeItem pti = (PolicyTreeItem) policyView.getSelectedTreeNode().getData();
		if (pti.isGroup()) {
			pti = (PolicyTreeItem) policyView.getSelectedTreeNode().getParent().getData();
		}
		return pti.getAppEntity().toString();
	}

	public void actionAssign() {

		try {
			PolicyTreeItem pti = (PolicyTreeItem) policyView.getSelectedTreeNode().getData();
			policyLogic.assignPolicy(pti, assignedGroup, assignedPolicy, priority);
		} catch (Exception e) {
			logger.info(e);
			JsfUtils.addErrorMessage(e.toString());
			return;
		}

		viewNavigator.getActiveView().closeDialog();
		Exception exception = DcemUtils.reloadTaskNodes(PolicyLogic.class, TenantIdResolver.getCurrentTenantName());
		if (exception != null) {
			JsfUtils.addErrorMessage(exception.toString());
			return;
		}
		JsfUtils.addFacesInformationMessage("successful", "mainMessages");
	}

	public boolean actionOk() throws Exception {
		PolicyEntity policyEntity = (PolicyEntity) getActionObject();
		DcemPolicy dcemPolicy = policyEntity.getDcemPolicy();
		if (dcemPolicy.getRememberBrowserFingerPrint() < 0) {
			JsfUtils.addErrorMessage("Please enter a positive value for 'Timeout'");
			return false;
		}
		if (dcemPolicy.getRememberBrowserFingerPrint() == 0 && (dcemPolicy.isEnableSessionAuthentication() || dcemPolicy.isRefrain2FaWithInTime())) {
			JsfUtils.addErrorMessage("Please enter a value for 'Timeout'");
			return false;
		}

		List<AuthMethod> authMethods = new ArrayList<>(allowedMethods.size());
		for (String method : allowedMethods) {
			authMethods.add(AuthMethod.valueOf(method));
		}
		policyEntity.getDcemPolicy().setAllowedMethods(authMethods);
		try {
			policyEntity.getDcemPolicy().updateIpranges();
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getMessage());
			return false;
		}
		policyLogic.addOrUpdatePolicy(policyEntity, this.getAutoViewAction().getDcemAction(), true);
		Exception exception = DcemUtils.reloadTaskNodes(PolicyLogic.class, TenantIdResolver.getCurrentTenantName());
		if (exception != null) {
			throw exception;
		}
		return true;
	}

	public List<SelectItem> getPolciesSelection() {
		return policyLogic.getPolciesSelection();
	}

	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		testPolicyEntity = null;
		dcemUser = null;
		PolicyEntity policyEntity = (PolicyEntity) getActionObject();
		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_ASSIGN)
				|| autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_TEST_USER_POLICY)) {
			if (policyView.getSelectedTreeNode() == null) {
				throw new DcemException(DcemErrorCodes.NO_ITEM_SELECTED, null);
			}
			PolicyTreeItem pti = (PolicyTreeItem) policyView.getSelectedTreeNode().getData();
			if (pti.getPolicyGroupEntity() != null) {
				AppPolicyGroupEntity appPolicyGroupEntity = policyLogic.getPolicyGroupEntity(pti.getPolicyGroupEntity().getId());

				assignedGroup = appPolicyGroupEntity.getGroup() != null ? appPolicyGroupEntity.getGroup().getId() : 0;
				priority = appPolicyGroupEntity.getPriority();
				assignedPolicy = appPolicyGroupEntity.getPolicyEntity() != null ? appPolicyGroupEntity.getPolicyEntity().getId() : 0;
			} else {
				assignedGroup = 0;
				assignedPolicy = 0;
				priority = 0;
			}

		} else {
			if (policyEntity.getJsonPolicy() == null) {
				policyEntity.setDcemPolicy(new DcemPolicy());
			} else {
				try {
					policyEntity.setDcemPolicy(new ObjectMapper().readValue(policyEntity.getJsonPolicy(), DcemPolicy.class));
				} catch (Exception e) {
					JsfUtils.addErrorMessage("Couldn't deserialize policy. Cause: " + e.toString());
					policyEntity.setDcemPolicy(new DcemPolicy());
				}
			}
			allowedMethods = new ArrayList<>(policyEntity.getDcemPolicy().getAllowedMethods().size());
			for (AuthMethod authMethod : policyEntity.getDcemPolicy().getAllowedMethods()) {
				allowedMethods.add(authMethod.name());
			}
		}
	}

	public List<AuthMethod> getAuthMethods() {
		AuthMethod[] methods = AuthMethod.values();
		List<AuthMethod> methodesName = new LinkedList<>();
		for (AuthMethod authMethod : methods) {
			if (authMethod.getValue() != null) {
				methodesName.add(authMethod);
			}
		}
		return methodesName;
	}

	@Override
	public void actionConfirm() throws Exception {
		List<Object> policies = autoViewBean.getSelectedItems();
		boolean canDelete = true;
		String name = null;
		for (Object policyEntity : policies) {
			name = ((PolicyEntity) policyEntity).getName();
			if (name.equals(DcemConstants.GLOBAL_POLICY) || name.equals(DcemConstants.MANAGEMENT_POLICY)) {
				canDelete = false;
				break;
			}
		}
		if (canDelete) {
			super.actionConfirm();
		} else {
			JsfUtils.addErrorMessage("You cannot delete " + name);
		}
	}

	public List<SelectItem> getPolicies() {
		return policyLogic.getPolciesSelection();
	}

	public List<SelectItem> getGroups() {
		List<DcemGroup> list = groupLogic.getAllGroups();
		List<SelectItem> selectItems = new ArrayList<>(list.size());
		for (DcemGroup group : list) {
			selectItems.add(new SelectItem(group.getId(), group.getName()));
		}
		return selectItems;
	}

	public String getAssignPolicyTo() {
		PolicyTreeItem pti = (PolicyTreeItem) policyView.getSelectedTreeNode().getData();
		if (pti.isGroup()) {
			pti = (PolicyTreeItem) policyView.getSelectedTreeNode().getParent().getData();
		}
		return pti.getName();
	}

	public Integer getAssignedPolicy() {
		return assignedPolicy;
	}

	public void setAssignedPolicy(Integer assignedPolicy) {
		this.assignedPolicy = assignedPolicy;
	}

	public Integer getAssignedGroup() {
		return assignedGroup;
	}

	public void setAssignedGroup(Integer assignedGroup) {
		this.assignedGroup = assignedGroup;
	}

	public List<String> getAllowedMethods() {
		return allowedMethods;
	}

	public void setAllowedMethods(List<String> allowedMethods) {
		this.allowedMethods = allowedMethods;
	}

	public String getWidth() {
		return "800";
	}

	public String getHeight() {
		return "520";
	}

	public String getTestPolicyName() {
		return testPolicyEntity == null ? null : testPolicyEntity.getName();
	}

	public String getTestPolicyContent() {
		return testPolicyEntity == null ? null : testPolicyEntity.getJsonPolicy();
	}

	public boolean isDisableTimeout() {
		DcemPolicy dcemPolicy = ((PolicyEntity) getActionObject()).getDcemPolicy();
		if (dcemPolicy.isDenyAccess()) {
			return true;
		}
		if (dcemPolicy.isEnableSessionAuthentication() || dcemPolicy.isRefrain2FaWithInTime()) {
			return false;
		}
		return true;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
	}

}
