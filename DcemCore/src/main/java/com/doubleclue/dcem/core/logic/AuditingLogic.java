package com.doubleclue.dcem.core.logic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Hibernate;

import com.doubleclue.dcem.core.entities.Auditing;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.utils.compare.CompareUtils;

@ApplicationScoped
@Named("auditingLogic")
public class AuditingLogic {
	
	private static final Logger logger = LogManager.getLogger(AuditingLogic.class);

	@Inject
	EntityManager em;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	ActionLogic actionLogic;

	@DcemTransactional
	public void addAudit(DcemAction dcemAction, String information) {
		if (information.isEmpty() == false) {
			if (dcemAction.getId() == null) {
				dcemAction = actionLogic.getDcemAction(dcemAction);
			}
			Auditing auditing = new Auditing(dcemAction, information, operatorSessionBean.getDcemUser());
			em.persist(auditing);
		}
	}

	@DcemTransactional
	public void addAudit(DcemAction dcemAction, EntityInterface newEntity) {
		if (dcemAction.getId() == null) {
			dcemAction = actionLogic.getDcemAction(dcemAction);
		}
		String changeInfo;
		if (newEntity.getId() == null) {
			changeInfo = newEntity.toString();
		} else {
			EntityInterface oldEntity = null;
			try {
				oldEntity = (EntityInterface) em.find(Hibernate.unproxy(newEntity).getClass(), newEntity.getId());
				changeInfo = CompareUtils.compareObjects(oldEntity, newEntity);
			} catch (Exception exp) {
				logger.warn("Couldn't compare operator", exp);
				changeInfo = "ERROR: " + exp.getMessage();
			} finally {
				if (oldEntity != null) {
					em.detach(oldEntity);
				}
			}
		}
		if (changeInfo.isEmpty() == false) {
			Auditing auditing = new Auditing(dcemAction, changeInfo, operatorSessionBean.getDcemUser());
			em.persist(auditing);
		}
	}

	@DcemTransactional
	public void addAudit(DcemAction dcemAction, DcemUser dcemUser, String information) {
		if (dcemAction.getId() == null) {
			dcemAction = actionLogic.getDcemAction(dcemAction);
		}
		Auditing auditing = new Auditing(dcemAction, information, dcemUser);
		em.persist(auditing);
	}

	@DcemTransactional
	public void deleteAllAuditsForUser(DcemUser dcemUser) {
		Query query = em.createNamedQuery(Auditing.DELETE_BY_USER);
		query.setParameter(1, dcemUser);
		query.executeUpdate();
	}
}
