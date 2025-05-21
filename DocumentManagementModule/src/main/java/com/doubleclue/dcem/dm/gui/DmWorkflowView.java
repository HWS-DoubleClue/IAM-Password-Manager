package com.doubleclue.dcem.dm.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.CloudSafeEntity_;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewLink;
import com.doubleclue.dcem.dm.entities.DmWorkflowEntity;
import com.doubleclue.dcem.dm.entities.DmWorkflowEntity_;
import com.doubleclue.dcem.dm.logic.DmConstants;
import com.doubleclue.dcem.dm.logic.DocumentManagementModule;
import com.doubleclue.dcem.dm.subjects.DmDocumentSubject;
import com.doubleclue.dcem.dm.subjects.DmWorkflowSubject;

@SuppressWarnings("serial")
@Named("dmWorkflowView")
@SessionScoped
public class DmWorkflowView extends DcemView {

	@Inject
	private DmWorkflowSubject dmWorkflowEntitySubject;
	
	@Inject 
	DmDocumentSubject dmDocumentSubject;

	@Inject
	private DmWorkflowDialog dmWorkflowEntityDialog;  // small letters
	
	@Inject
	DmDocumentView documentView;

	CloudSafeEntity cloudSafeEntity;

	@Inject
	DcemApplicationBean applicationBean;

	@PostConstruct
	private void init() {
		subject = dmWorkflowEntitySubject;
		ResourceBundle resourceBundle = JsfUtils.getBundle(DcemConstants.CORE_RESOURCE, operatorSessionBean.getLocale());		
		addAutoViewAction(DcemConstants.ACTION_ADD, resourceBundle, dmWorkflowEntityDialog, "/modules/dm/workflowDialog.xhtml");
		addAutoViewAction(DcemConstants.ACTION_EDIT, resourceBundle, dmWorkflowEntityDialog, "/modules/dm/workflowDialog.xhtml");
		addAutoViewAction(DcemConstants.ACTION_DELETE, resourceBundle, dmWorkflowEntityDialog, DcemConstants.AUTO_CONFIRM_DIALOG_PATH);	
		ViewLink viewLink = new ViewLink(dmDocumentSubject, null, null);
	  	addAutoViewAction(DmConstants.ACTION_DOCUMENTS, resourceBundle, null, null, viewLink);	
		this.setTopComposition("/mgt/modules/dm/workflowTitle.xhtml");
	}
	
		
	/*
	* This method is called when the view is displayed or reloaded
	*
	*/
	@Override
	public void reload() {
		if (cloudSafeEntity == null) {
			viewNavigator.setActiveView(DocumentManagementModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + documentView.getSubject().getViewName());
		}
	}

    @Override
	public Object createActionObject() {
		return super.createActionObject();
	}

	public void editWorkflows(CloudSafeEntity cloudSafeEntity) {
		this.cloudSafeEntity = cloudSafeEntity;		
	}

	public CloudSafeEntity getCloudSafeEntity() {
		return cloudSafeEntity;
	}

	public void setCloudSafeEntity(CloudSafeEntity cloudSafeEntity) {
		this.cloudSafeEntity = cloudSafeEntity;
	}
	
	@Override
	public List<Predicate> getPredicates(CriteriaBuilder criteriaBuilder, Root<?> root) {
		ArrayList<Predicate> predicates = new ArrayList<Predicate>();
		if (cloudSafeEntity == null) {
			return new ArrayList<>(1);
		}
//		if (viewManager) {
//			return predicates;
//		}
		@SuppressWarnings("unchecked")
		Root<DmWorkflowEntity> questionRoot = (Root<DmWorkflowEntity>) root;
		Join<DmWorkflowEntity, CloudSafeEntity> cloudSafeJoin = questionRoot.join(DmWorkflowEntity_.cloudSafeEntity);
		
	//	Predicate userCategoryId = criteriaBuilder.equal(userCategoryRoot.get(KbUserCategoryEntity_.kbUser).get(KbUserEntity_.id), dcemUser.getId());
	//	Predicate predicate = criteriaBuilder.equal(cloudSafeJoin.get(DmWorkflowEntity_.cloudSafeEntity), cloudSafeEntity.getId());
		Predicate predicate = criteriaBuilder.equal(cloudSafeJoin.get(CloudSafeEntity_.ID), cloudSafeEntity.getId());
		predicates.add(predicate);
		return predicates;
	}

}
