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
import com.doubleclue.dcem.core.utils.DcemUtils;


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
		String information = null;
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			information = "";
			entity.setId(null);
			em.persist(entity);
		} else {
			information = DcemUtils.compareObjects(getPreviousObject(entity), entity);
			em.merge(entity);
			em.flush();
		}
		auditingLogic.addAudit(dcemAction, information);
	}

	public EntityInterface getPreviousObject(EntityInterface entity) {
		return em.find(entity.getClass(), entity.getId());
	}

	@DcemTransactional
	public void deleteEntities(List<Object> actionObjects, DcemAction dcemAction) throws DcemException {
		StringBuffer sb = new StringBuffer();
			for (Object obj : actionObjects) {
				sb.append(obj.toString());
				obj = em.merge(obj);
				em.remove(obj);
			}
		auditingLogic.addAudit(dcemAction,  sb.toString());
	}

}
