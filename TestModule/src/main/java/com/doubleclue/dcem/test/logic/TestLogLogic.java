package com.doubleclue.dcem.test.logic;

import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.test.entities.TestLog;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.entities.DcemAction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@ApplicationScoped
public class TestLogLogic {

	private Logger logger = LogManager.getLogger(TestLogLogic.class);

	@Inject
	AdminModule adminModule;

	@Inject
	EntityManager em;
	
	@Inject
	AuditingLogic auditingLogic;
	
	@DcemTransactional
	public void addOrUpdate (TestLog testLog, DcemAction dcemAction) throws DcemException {
		if (dcemAction.getAction().equals(DcemConstants.ACTION_ADD)) {
			em.persist(testLog);
		} else {			
			em.merge(testLog);
		}
		auditingLogic.addAudit(dcemAction, testLog.toString());
	}
	
	
	
}
