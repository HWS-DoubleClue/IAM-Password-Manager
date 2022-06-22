package com.doubleclue.dcem.as.logic.cloudsafe;

import java.io.InputStream;

import javax.persistence.EntityManager;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;

public interface CloudSafeContentI {
	
	InputStream getContentInputStream(EntityManager em, int id) throws DcemException;
	
	public int writeContentOutput (EntityManager em, CloudSafeEntity cloudSafeEntity, InputStream inputStream) throws DcemException;
	
	void delete (EntityManager em, int id);
	
	
}
