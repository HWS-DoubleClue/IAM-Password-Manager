package com.doubleclue.dcem.as.logic.cloudsafe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import com.doubleclue.dcem.as.entities.CloudSafeContentEntity;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.JdbcUtils;

@Named("cloudSafeContentDb")
public class CloudSafeContentDb implements CloudSafeContentI {

	public CloudSafeContentDb() {
		super();
	}
	

	@Override
	public InputStream getContentInputStream(EntityManager em, int id) throws DcemException {
		File file = JdbcUtils.readCloudSafeContent(id);
		try {
			return new FileInputStream (file);
		} catch (FileNotFoundException e) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_READ_ERROR, "Couldn't read CloudSafe Content for " + id, e);
		}
	}
	

	@Override
	public int writeContentOutput(EntityManager em, CloudSafeEntity cloudSafeEntity, InputStream inputStream) throws DcemException {
		if (cloudSafeEntity.getLength() > DcemConstants.MAX_MEM_CLOUD_SAFE_LENGTH) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_FILE_TOO_BIG, cloudSafeEntity.getName());
		}
		return JdbcUtils.writeCloudSafeContent(cloudSafeEntity.getId(), inputStream, cloudSafeEntity.isNewEntity(), cloudSafeEntity.getLength());
	}
	

	@Override
	public void delete(EntityManager em, int id) {
		Query query = em.createNamedQuery(CloudSafeContentEntity.DELETE_ENTITY);
		query.setParameter(1, id);
		query.executeUpdate();
	}

	@Override
	public String toString() {
		return "CloudSafeContentDb []";
	}

	@Override
	public void initiateTenant(String teneantName) throws DcemException {

	}
	
	@Override
	public void writeS3Data(int id, String prefix, InputStream inputStream, int length) throws DcemException {
		throw new DcemException(DcemErrorCodes.NOT_IMPLEMENTED, prefix);
	}


	@Override
	public void deleteS3Data(int id, String prefix) throws DcemException {
		throw new DcemException(DcemErrorCodes.NOT_IMPLEMENTED, prefix);		
	}


	@Override
	public List<DocumentVersion> getS3Versions( int id) throws DcemException {
		throw new DcemException(DcemErrorCodes.NOT_IMPLEMENTED, "");
	}


	@Override
	public InputStream getS3ContentInputStream(int id, String prefix, String versionId) throws DcemException {
		throw new DcemException(DcemErrorCodes.NOT_IMPLEMENTED, "");
	}
	
}
