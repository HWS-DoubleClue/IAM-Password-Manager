package com.doubleclue.dcem.as.logic.cloudsafe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Named;
import javax.persistence.EntityManager;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.utils.KaraUtils;

@Named("cloudSafeContentNas")
public class CloudSafeContentNas implements CloudSafeContentI {
	
	File directory;

	public CloudSafeContentNas(File directory) {
		super();
		this.directory = directory;
	}

	@Override
	public InputStream getContentInputStream(EntityManager em, int id) throws DcemException {
		File tenantFile = new File (directory, TenantIdResolver.getCurrentTenantName());
		if (tenantFile.exists() == false) {
			tenantFile.mkdir();
		}
		File file = new File (tenantFile, Integer.toString(id));

		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_NOT_FOUND, Integer.toString(id), e);
		}
	}

	// @Override
	public int writeContentOutput(EntityManager em, CloudSafeEntity cloudSafeEntity, InputStream inputStream) throws DcemException {
		File tenantFile = new File (directory, TenantIdResolver.getCurrentTenantName());
		if (tenantFile.exists() == false) {
			tenantFile.mkdir();
		}
		File file = new File (tenantFile, Integer.toString(cloudSafeEntity.getId()));
		OutputStream outputStream = null;
		try {
			outputStream = new FileOutputStream(file);
			return KaraUtils.copyStream(inputStream, outputStream, DcemConstants.MAX_CIPHER_BUFFER);
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_WRITE_ERROR, Integer.toString(cloudSafeEntity.getId()), e);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
			}
			try {
				outputStream.close();
			} catch (IOException e) {
			}
		}
	}
	@Override
	public void delete(EntityManager em, int id) {
		File tenantFile = new File (directory, TenantIdResolver.getCurrentTenantName());
		if (tenantFile.exists() == false) {
			tenantFile.mkdir();
		}
		File file = new File (tenantFile, Integer.toString(id));
		if (file.exists()) {
			file.delete();
		}
	}

	@Override
	public String toString() {
		return "CloudSafeContentNas [directory=" + directory + "]";
	}
		
}
