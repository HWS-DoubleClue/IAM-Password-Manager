package com.doubleclue.dcem.as.gui;

import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import com.doubleclue.dcem.as.entities.AppPolicyGroupEntity;
import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.as.entities.PolicyEntity;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.policy.DcemPolicy;
import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.as.policy.PolicyTreeItem;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.subjects.PolicySubject;

@Named("policyView")
@SessionScoped
public class PolicyView extends DcemView {

	@Inject
	private PolicySubject policySubject;

	@Inject
	private AutoViewBean autoViewBean;

	@Inject
	private PolicyDialog policyDialog;

	@Inject
	private PolicyLogic policyLogic;

	TreeNode rootNode;

	TreeNode selectedTreeNode;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@PostConstruct
	private void init() {

		policyDialog.setParentView(this);

		subject = policySubject;

		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE);
		ResourceBundle asResourceBundle = JsfUtils.getBundle(AsModule.RESOURCE_NAME);

		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, policyDialog, AsConstants.POLICY_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, policyDialog, AsConstants.POLICY_DIALOG_PATH);
		// addAutoViewAction(DcemConstants.ACTION_MEMBERS, resourceBundle, policyDialog,
		// DcemConstants.GROUP_MEMBERS_DIALOG_PATH);

		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, policyDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);

		addAutoViewAction(DcemConstants.ACTION_ASSIGN, resourceBundle, policyDialog, AsConstants.POLICY_ASSIGN_DIALOG_PATH);
		addAutoViewAction(DcemConstants.ACTION_TEST_USER_POLICY, asResourceBundle, policyDialog, AsConstants.TEST_USER_POLICY_DIALOG_PATH);

		// addAutoViewAction(DcemConstants.ACTION_UNASSIGN, resourceBundle, policyDialog,
		// DcemConstants.POLICY_ASSIGN_DIALOG_PATH);
	}

	public List<PolicyEntity> getPolicies() {
		return policyLogic.getPoliciesWithAssignmrents();
	}

	@Override
	public void reload() {
		policyLogic.syncPolicyAppEntity();
		refreshTreeNode();
		autoViewBean.reload();
	}

	public TreeNode getRootNode() {
		return rootNode;
	}

	public void setRootNode(TreeNode rootNode) {
		this.rootNode = rootNode;
	}

	private void refreshTreeNode() {
		List<PolicyAppEntity> applications = policyLogic.getAllApplications();
		rootNode = new DefaultTreeNode(null, null);
		rootNode.setExpanded(true);
		TreeNode appParent = null;
		PolicyTreeItem pti;
		TreeNode treeNode = null;
		for (PolicyAppEntity appEntity : applications) {
			pti = new PolicyTreeItem(appEntity, null);
			if (appEntity.getSubId() == 0 || appParent == null) {
				appParent = new DefaultTreeNode(pti, rootNode);
				treeNode = appParent;
			} else {
				treeNode = new DefaultTreeNode(pti, appParent);
			}
			treeNode.setExpanded(true);
			List<AppPolicyGroupEntity> appPolicyGroupEntities = policyLogic.getPolicyGroups(appEntity);

			for (AppPolicyGroupEntity policyGroupEntity : appPolicyGroupEntities) {
				if (policyGroupEntity.getGroup() != null) {
					PolicyTreeItem groupPti = new PolicyTreeItem(appEntity, policyGroupEntity);
					new DefaultTreeNode(groupPti, treeNode);
				} else {
					pti.setPolicyGroupEntity(policyGroupEntity);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.doubleclue.dcem.core.gui.DcemView#creatActionObject()
	 */
	public Object createActionObject() {
		if (this.subject.getKlass() == null) {
			return null;
		}

		PolicyEntity entity = new PolicyEntity();
		DcemPolicy dcemPolicy = new DcemPolicy();
		entity.setDcemPolicy(dcemPolicy);
		actionObject = entity;
		return entity;
	}
	
	

	public TreeNode getSelectedTreeNode() {
		return selectedTreeNode;
	}

	public void setSelectedTreeNode(TreeNode selectedTreeNode) {
		this.selectedTreeNode = selectedTreeNode;
	}

	/* (non-Javadoc)
	 * @see com.doubleclue.dcem.core.gui.DcemView#leavingView()
	 */
	public void leavingView() {
		rootNode = null;
		selectedTreeNode = null;

		
	}
}
