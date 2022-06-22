package com.doubleclue.dcem.core.logic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.doubleclue.dcem.core.entities.Auditing;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.jpa.DcemTransactional;

@ApplicationScoped
@Named("auditingLogic")
public class AuditingLogic {

	@Inject
	EntityManager em;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	ActionLogic actionLogic;

	@DcemTransactional
	public void addAudit(DcemAction dcemAction, String information) {
		// #if COMMUNITY_EDITION == false
		if (dcemAction.getId() == null) {
			dcemAction = actionLogic.getDcemAction(dcemAction);
		}
		Auditing auditing = new Auditing(dcemAction, information, operatorSessionBean.getDcemUser());
		em.persist(auditing);
		//#endif
	}
	
	@DcemTransactional
	public void addAudit(DcemAction dcemAction, DcemUser dcemUser, String information) {
		// #if COMMUNITY_EDITION == false
		if (dcemAction.getId() == null) {
			dcemAction = actionLogic.getDcemAction(dcemAction);
		}
		Auditing auditing = new Auditing(dcemAction, information, dcemUser);
		em.persist(auditing);
		//#endif
	}


	@DcemTransactional
	public void deleteAllAuditsForUser(DcemUser dcemUser) {
		// #if COMMUNITY_EDITION == false
		Query query = em.createNamedQuery(Auditing.DELETE_BY_USER);
		query.setParameter(1, dcemUser);
		query.executeUpdate();
		//#endif
	}
}
