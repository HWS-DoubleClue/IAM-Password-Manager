package com.doubleclue.dcem.dm.logic;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.AppErrorCodes;
import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.CloudSafeThumbnailEntity;
import com.doubleclue.dcem.as.logic.CloudSafeDto;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.tasks.EmailTask;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.dm.entities.DmWorkflowEntity;

@ApplicationScoped
public class DmWorkflowLogic {

	private Logger logger = LogManager.getLogger(DmWorkflowLogic.class);

	@Inject
	AdminModule adminModule;

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	TaskExecutor taskExecutor;

	@Inject
	GroupLogic groupLogic;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	DmSolrLogic solrLogic;

	@DcemTransactional
	public void addOrUpdate(DmWorkflowEntity dmWorkflowEntity, DcemAction dcemAction) throws DcemException {
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			em.persist(dmWorkflowEntity);
		} else {
			em.merge(dmWorkflowEntity);
		}
		auditingLogic.addAudit(dcemAction, dmWorkflowEntity.toString());
	}
	
	@DcemTransactional
	public int deleteWorkflowForDocument (int id) throws DcemException {
		Query query = em.createNamedQuery(DmWorkflowEntity.DELETE_FOR_DOCUMENT);
		query.setParameter(1, id);
		return query.executeUpdate();
	}

	public List<DmWorkflowEntity> getWorkflows(CloudSafeEntity cloudSafeEntity, WorkflowTrigger trigger) {
		TypedQuery<DmWorkflowEntity> query = em.createNamedQuery(DmWorkflowEntity.GET_DOCUMENT_TRIGGER_LIST, DmWorkflowEntity.class);
		query.setParameter(1, cloudSafeEntity);
		query.setParameter(2, trigger);
		return query.getResultList();
	}

	@DcemTransactional
	public void checkWorkflow(CloudSafeEntity cloudSafeEntity, WorkflowTrigger trigger) throws Exception {
		List<DmWorkflowEntity> workflows = getWorkflows(cloudSafeEntity, trigger);
		if (cloudSafeEntity.getParent().equals(cloudSafeLogic.getCloudSafeRoot()) == false) {
			List<DmWorkflowEntity> workflowsParent = getWorkflows(cloudSafeEntity.getParent(), trigger);
			for (DmWorkflowEntity entity : workflowsParent) {
				entity.setChildCloudSafeEntity(cloudSafeEntity);
			}
			workflows.addAll(getWorkflows(cloudSafeEntity.getParent(), trigger));
		}
		processActions(workflows);
	}

	@DcemTransactional
	public void nightlyTask() {
		try {
			LocalDate now = LocalDate.now();
			TypedQuery<DmWorkflowEntity> query = em.createNamedQuery(DmWorkflowEntity.GET_TIME_TRIGGER_LIST, DmWorkflowEntity.class);
			query.setParameter(1, now.getDayOfWeek().getValue());
			query.setParameter(2, now.getDayOfMonth());
			query.setParameter(3, now.getMonth().getValue());
			List<DmWorkflowEntity> workflows = query.getResultList();
			processActions(workflows);
		} catch (DcemException e) {
			reportingLogic.addReporting(new DcemReporting(DocumentManagementModule.MODULE_ID, ReportAction.Workflow, null,
					AppErrorCodes.UNEXPECTED_ERROR.name(), null, e.toString(), AlertSeverity.ERROR, false));
			logger.error("Workflow processing faild", e);
		}
	}

	private void processActions(List<DmWorkflowEntity> workflows) throws DcemException {
		Map<String, Object> dataMap = new HashMap<>();
		for (DmWorkflowEntity workflowEntity : workflows) {
			dataMap.clear();
			dataMap.put(DcemConstants.MAIL_SUBJECT_PARAMETER, workflowEntity.getCloudSafeEntity().getName());
			switch (workflowEntity.getWorkflowAction()) {
			case Email: {
				List<DcemUser> recipients = new ArrayList<>();
				DcemUser dcemUser = workflowEntity.getUser();
				dcemUser.getLanguage(); // force loading
				recipients.add(dcemUser);
				if (workflowEntity.getUser2() != null) {
					DcemUser dcemUser2 = workflowEntity.getUser2();
					dcemUser.getLanguage();
					recipients.add(dcemUser2);
				}
				CloudSafeEntity cloudSafeEntity = workflowEntity.getCloudSafeEntity();
				if (workflowEntity.getChildCloudSafeEntity() != null) {
					cloudSafeEntity = workflowEntity.getChildCloudSafeEntity();
				}
				if (workflowEntity.isGroupMembers() == true) {
					DcemGroup dcemGroup = workflowEntity.getCloudSafeEntity().getGroup();
					if (dcemGroup != null) {
						for (DcemUser dcemUser3 : groupLogic.getMembers(dcemGroup)) {
							recipients.add(dcemUser3);
						}
					}
				}
				dataMap.put(DmConstants.DOCUMENT_NAME, cloudSafeEntity.getName());
				dataMap.put(DmConstants.WORKFLOW_USER, cloudSafeEntity.getUser().getDisplayName());

				dataMap.put(DmConstants.WORKFLOW_NAME, workflowEntity.getName());
				dataMap.put(DmConstants.WORKFLOW_INFORMATION, workflowEntity.getDescription());
				dataMap.put(DmConstants.WORKFLOW_TRIGGER, workflowEntity.getWorkflowTrigger().name());
				String subject = DmConstants.WORKFLOW_TIME_SUBJECT.replace("${documentName}", cloudSafeEntity.getName());
				taskExecutor.execute(new EmailTask(recipients, dataMap, DmConstants.WORKFLOW_TIME_TEMPLATE, subject, null));
			}
				break;
			// case Pushnotification: {
			// // TODO: Not Implemented Yet
			// }
			case MoveToTrash:
				try {
					List<CloudSafeEntity> list = new ArrayList<>(1);
					list.add(workflowEntity.getCloudSafeEntity());
					List<CloudSafeDto> deletedDbFiles = cloudSafeLogic.trashFiles(list, workflowEntity.getUser());
					solrLogic.removeDocumentsIndex(deletedDbFiles);
				} catch (Exception e) {
					logger.warn("documentView.warn.partialSolrDeletion", e);
					reportingLogic.addReporting(new DcemReporting(DocumentManagementModule.MODULE_ID, ReportAction.Workflow, null,
							AppErrorCodes.UNEXPECTED_ERROR.name(), null, e.toString(), AlertSeverity.ERROR, false));
				}
				break;
			default:
				throw new IllegalArgumentException("Unexpected value: " + workflowEntity.getWorkflowAction());
			}
		}
	}
}
