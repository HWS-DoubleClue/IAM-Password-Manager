package com.doubleclue.dcem.core.logic;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.system.logic.SystemModule;

@ApplicationScoped
@Named("actionLogic")
public class ActionLogic {

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	RoleLogic roleLogic;

	@Inject
	EntityManager em;

	private static Logger logger = LogManager.getLogger(ActionLogic.class);

	public DcemAction getDcemAction(DcemAction dcemAction) {
		TypedQuery<DcemAction> query = em.createNamedQuery(DcemAction.GET_ACTION, DcemAction.class);
		query.setParameter(1, dcemAction.getModuleId());
		query.setParameter(2, dcemAction.getSubject());
		query.setParameter(3, dcemAction.getAction());
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}
	
	@DcemTransactional
	public void addDcemActionIfNotExists (DcemAction dcemAction) throws DcemException {
		DcemAction  action = getDcemAction(dcemAction);
		if (action == null) {
			addDcemAction(dcemAction);
		}
	}

	public DcemAction getDcemAction(int id) {
		return em.find(DcemAction.class, id);
	}

	@DcemTransactional
	public void addDcemAction(DcemAction dcemAction) {
		try {
			em.persist(dcemAction);
		} catch (Exception e) {
			logger.error(e);
			throw e;
		}
	}

	@DcemTransactional
	public void addDcemActions(List<DcemAction> dcemActions) {
		for (DcemAction dcemAction : dcemActions) {
			dcemAction.setId(null);
			em.persist(dcemAction);
		}
	}

	public List<DcemAction> getAllDcemActions() {
		TypedQuery<DcemAction> query = em.createNamedQuery(DcemAction.GET_ALL, DcemAction.class);
		return query.getResultList();
	}

	public List<DcemAction> getAllDcemActions(String subject) {
		TypedQuery<DcemAction> query = em.createNamedQuery(DcemAction.GET_BY_SUBJECT, DcemAction.class);
		query.setParameter(1, subject);
		return query.getResultList();
	}

	@DcemTransactional
	public void createDbActions(TenantEntity tenantEntity) {

		List<DcemAction> dcemActionList = getAllDcemActions();
		List<DcemModule> moduleList = applicationBean.getSortedModules();
		for (DcemModule module : moduleList) {
			int counter = 0;
			SortedSet<SubjectAbs> subjectSet = applicationBean.getModuleSubjects(module);
			if (subjectSet != null) {
				for (SubjectAbs subject : subjectSet) {
					try {
						if (subject.getModuleId().equals(SystemModule.MODULE_ID) && tenantEntity.isMaster() == false) {
							continue;
						}
					} catch (Exception e) {
						logger.warn("", e);
					}
					for (RawAction rawAction : subject.getRawActions()) {
						if (rawAction.isMasterOnly() && tenantEntity.isMaster() == false) {
							continue;
						}
						DcemAction dcemAction = new DcemAction(subject.getModuleId(), subject.getName(),
								rawAction.getName());
						if (dcemActionList.contains(dcemAction) == false) {
							counter++;
							addDcemAction(dcemAction);
							roleLogic.addActionToRoles(dcemAction, rawAction);
						}
					}
				}
			}
			if (counter > 0) {
				logger.info("Actions added for Module: " + module.getName() + ", " + counter);
			}
		}

		for (DcemModule dcemModule : applicationBean.getSortedModules()) {
			if (dcemModule.getId().equals(SystemModule.MODULE_ID) && tenantEntity.isMaster() == false) {
				continue;
			}
			int counter = 0;
			for (RawAction rawAction : dcemModule.getRawActions()) {
				if (rawAction.isMasterOnly() && tenantEntity.isMaster() == false) {
					continue;
				}
				DcemAction dcemAction = new DcemAction(dcemModule.getId(), DcemConstants.EMPTY_SUBJECT_NAME,
						rawAction.getName());
				if (dcemActionList.contains(dcemAction) == false) {
					counter++;
					addDcemAction(dcemAction);
					roleLogic.addActionToRoles(dcemAction, rawAction);
				}
			}
			if (counter > 0) {
				logger.info("Actions added for Module: " + dcemModule.getName() + ", " + counter);
			}
		}
	}

}
