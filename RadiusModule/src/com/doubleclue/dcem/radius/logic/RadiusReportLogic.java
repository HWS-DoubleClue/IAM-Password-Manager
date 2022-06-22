package com.doubleclue.dcem.radius.logic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.radius.entities.RadiusReportEntity;

@ApplicationScoped
public class RadiusReportLogic {

	private static Logger logger = LogManager.getLogger(RadiusReportLogic.class);

	@Inject
	EntityManager em;
	
	@Inject
	RadiusModule radiusModule;

	@DcemTransactional
	public void addReporting (RadiusReportEntity report) {
//		reporting.setId(appServices.getReportIdGenerator().newId());
		logger.debug(report.toString());
		em.persist(report);
	}
	
	
	
}
