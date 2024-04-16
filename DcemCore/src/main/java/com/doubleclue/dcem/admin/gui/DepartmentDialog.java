package com.doubleclue.dcem.admin.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.DefaultOrganigramNode;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.OrganigramNode;
import org.primefaces.model.StreamedContent;

import com.doubleclue.dcem.admin.logic.DepartmentLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DcemUserExtension;
import com.doubleclue.dcem.core.entities.DepartmentEntity;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.UserLogic;

@Named("departmentDialog")
@SessionScoped
public class DepartmentDialog extends DcemDialog {

	@Inject
	UserLogic userLogic;

	@Inject
	DepartmentLogic departmentLogic;

	@Inject
	DepartmentView departmentView;

	@Inject
	JpaLogic jpaLogic;

	DcemUser dcemUser;
	DcemUser dcemUserDeputy;
	String parentDepartment;

	private OrganigramNode rootNode;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public boolean actionOk() throws Exception {

		DepartmentEntity departmentEntity = (DepartmentEntity) this.getActionObject();
		DcemUser headOf = dcemUser;
		DepartmentEntity parentDepartmentEntity = null;
		if (parentDepartment != null && parentDepartment.isEmpty() == false) {
			parentDepartmentEntity = departmentLogic.getDepartmentByName(parentDepartment);
			if (parentDepartmentEntity == null) {
				JsfUtils.addErrorMessage(departmentView.getResourceBundle(), "departmentDialog.invalidDepartment");
				return false;
			}
		}
		departmentEntity.setDeputy(dcemUserDeputy);
		departmentEntity.setHeadOf(headOf);
		departmentEntity.setParentDepartment(parentDepartmentEntity);
		jpaLogic.addOrUpdateEntity(departmentEntity, this.getAutoViewAction().getDcemAction());
		return true;
	}

	public List<String> completeDepartment(String name) {
		return departmentLogic.getCompleteDepartmentList(name, 50);
	}

	public String getHeight() {
		return "650";
	}

	public String getWidth() {
		return "800";
	}

	/* (non-Javadoc)
	 * @see com.doubleclue.dcem.core.gui.DcemDialog#show(com.doubleclue.dcem.core.gui.DcemView, com.doubleclue.dcem.core.gui.AutoViewAction)
	 */
	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		String action = this.getAutoViewAction().getDcemAction().getAction();
		DepartmentEntity departmentEntity = (DepartmentEntity) this.getActionObject();
		dcemUserDeputy = null;
		if (action.equals(DcemConstants.ACTION_EDIT)) {
			dcemUser = departmentEntity.getHeadOf();
			if (departmentEntity.getDeputy() != null) {
				dcemUserDeputy = departmentEntity.getDeputy();
			}
		}
		if (action.equals(DcemConstants.ACTION_COPY)) {
			dcemUser = departmentEntity.getHeadOf();
			if (departmentEntity.getDeputy() != null) {
				dcemUserDeputy = departmentEntity.getDeputy();
			}
		}
		if (departmentEntity.getParentDepartment() != null) {
			parentDepartment = departmentEntity.getParentDepartment().getName();
		}
		parentView = dcemView;
		rootNode = null;
	}

	public void leaving() {
		rootNode = null;
	}

	public String getParentDepartment() {
		return parentDepartment;
	}

	public void setParentDepartment(String parentDepartment) {
		this.parentDepartment = parentDepartment;
	}

	public OrganigramNode getRootNode() {
		if (rootNode == null) {
			OrganigramNode mainNode;
			DepartmentEntity departmentEntity = (DepartmentEntity) this.getActionObject();
			DcemUser hod = userLogic.getUser(departmentEntity.getHeadOf().getId());
			DepartmentEntity departmentEntityHigher = departmentEntity.getParentDepartment();
			if (departmentEntityHigher != null) {
				rootNode = new DefaultOrganigramNode("root", departmentEntityHigher.getHeadOf(), null);
				mainNode = new DefaultOrganigramNode("root", hod, rootNode);
			} else {
				rootNode = new DefaultOrganigramNode("root", hod, null);
				mainNode = rootNode;
			}
			rootNode.setExpanded(true);
			List<DcemUser> users = departmentLogic.getEmployees(departmentEntity);
			for (DcemUser dcemUser : users) {
				new DefaultOrganigramNode("employee", dcemUser, mainNode);
			}
		}
		return rootNode;
	}

	public boolean isDeputy(DcemUser dcemUser) {
		if (dcemUser == null) {
			return false;
		}
		DepartmentEntity departmentEntity = (DepartmentEntity) this.getActionObject();
		return (departmentEntity.getDeputy() != null && (dcemUser.getId() == departmentEntity.getDeputy().getId()));
	}

	public StreamedContent getUserPhoto(DcemUser dcemUser) {
		if (dcemUser == null) {
			return null;
		}
		DcemUserExtension dcemUserExtension = dcemUser.getDcemUserExt();
		if (dcemUserExtension != null && dcemUserExtension.getPhoto() != null) {
			byte[] image = dcemUserExtension.getPhoto();
			InputStream in = new ByteArrayInputStream(image);
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
		} else {
			return JsfUtils.getDefaultUserImage();
		}
	}

	public void setRootNode(OrganigramNode rootNode) {
		this.rootNode = rootNode;
	}

	public DcemUser getDcemUser() {
		return dcemUser;
	}

	public void setDcemUser(DcemUser dcemUser) {
		this.dcemUser = dcemUser;
	}

	public DcemUser getDcemUserDeputy() {
		return dcemUserDeputy;
	}

	public void setDcemUserDeputy(DcemUser dcemUserDeputy) {
		this.dcemUserDeputy = dcemUserDeputy;
	}
}
