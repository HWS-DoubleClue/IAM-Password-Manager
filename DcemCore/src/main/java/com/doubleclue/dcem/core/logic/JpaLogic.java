package com.doubleclue.dcem.core.logic;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DcemTransactional;


@ApplicationScoped
@Named("jpaLogic")
public class JpaLogic {

	@Inject
	EntityManager em;

	
	@Inject
	AuditingLogic auditingLogic;
	
	
	
//	private static final Logger logger = LogManager.getLogger(JpaLogic.class);

	@DcemTransactional
	public void addOrUpdateEntity(EntityInterface entity, DcemAction dcemAction) throws DcemException {
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			entity.setId(null);
			auditingLogic.addAudit(dcemAction, entity);
			em.persist(entity);
		} else {
			auditingLogic.addAudit(dcemAction, entity);
			em.merge(entity);
			em.flush();
		}
		
	}

	@DcemTransactional
	public void deleteEntities(List<Object> actionObjects, DcemAction dcemAction) throws DcemException {
		StringBuffer sb = new StringBuffer();
			for (Object obj : actionObjects) {
				sb.append(obj.toString());
				sb.append(", ");
				obj = em.merge(obj);
				em.remove(obj);
			}
		auditingLogic.addAudit(dcemAction,  sb.toString());
	}

}
