package com.doubleclue.dcem.as.logic.cloudsafe;

import java.io.InputStream;

import javax.persistence.EntityManager;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;

public interface CloudSafeContentI {
	
	void initiateTenant(String teneantName) throws Exception;
	
	InputStream getContentInputStream(EntityManager em, int id) throws DcemException;
	public int writeContentOutput (EntityManager em, CloudSafeEntity cloudSafeEntity, InputStream inputStream) throws DcemException;
	void delete (EntityManager em, int id);
	
		
	InputStream getS3Data(int id, String prefix) throws DcemException;
	public void writeS3Data (int id, String prefix, InputStream inputStream, int length) throws DcemException;
	void deleteS3Data (int id, String prefix) throws DcemException;
}
