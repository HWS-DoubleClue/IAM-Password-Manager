package com.doubleclue.dcem.core.logic;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.BranchLocation;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DcemTransactional;

@ApplicationScoped
@Named("branchLocationLogic")
public class BranchLocationLogic {

	@SuppressWarnings("unused")
	private Logger logger = LogManager.getLogger(BranchLocationLogic.class);

	@Inject
	AdminModule adminModule;

	@Inject
	EntityManager em;

	@Inject
	AuditingLogic auditingLogic;

	@DcemTransactional
	public void addOrUpdate(BranchLocation branchLocation, DcemAction dcemAction) throws DcemException {
		auditingLogic.addAudit(dcemAction, branchLocation);
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			em.persist(branchLocation);
		} else {
			em.merge(branchLocation);
		}
	}

	public List<BranchLocation> getAllBranchLocations() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<BranchLocation> query = cb.createQuery(BranchLocation.class);
		Root<BranchLocation> root = query.from(BranchLocation.class);
		query.select(root);
		return em.createQuery(query).getResultList();
	}

}
