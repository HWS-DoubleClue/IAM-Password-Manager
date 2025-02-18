package com.doubleclue.dcem.setup.logic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;
import javax.persistence.EntityManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.doubleclue.comm.thrift.CloudSafeOptions;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.cloudsafe.CloudSafeContentI;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.doubleclue.utils.KaraUtils;

public class CallCloudSafeStorageCopy implements Callable<Exception> {

	private static final Logger logger = LogManager.getLogger(CallCloudSafeStorageCopy.class);

	TenantEntity tenantEntity;
	CloudSafeContentI source;
	CloudSafeContentI destination;

	public CallCloudSafeStorageCopy(TenantEntity tenantEntity, CloudSafeContentI source, CloudSafeContentI destination) {
		this.tenantEntity = tenantEntity;
		this.source = source;
		this.destination = destination;
	}

	@Override
	public Exception call() {
		TenantIdResolver.setCurrentTenant(tenantEntity);
		WeldRequestContext requestContext = null;
		try {
			requestContext = WeldContextUtils.activateRequestContext();
			try {
				copyStorage(source, destination);
			} catch (Exception exp) {
				return exp;
			}
			return null;
		} finally {
			WeldContextUtils.deactivateRequestContext(requestContext);
		}
	}

	/**
	 * @param source
	 * @param destination
	 * @throws Exception
	 * @throws SQLException
	 * @throws IOException
	 */
	private void copyStorage(CloudSafeContentI source, CloudSafeContentI destination) throws Exception {
		CloudSafeLogic cloudSafeLogic = CdiUtils.getReference(CloudSafeLogic.class);
		List<Integer> list = cloudSafeLogic.getIdsOfAllEntries();
		CloudSafeEntity cloudSafeEntity;
		InputStream inputStream = null;
		EntityManager em = CdiUtils.getReference(EntityManager.class);
		for (Integer id : list) {
			cloudSafeEntity = cloudSafeLogic.getCloudSafe(id);
//			File file = new File(fileDir, cloudSafeEntity.getName());
//			FileOutputStream fileOutputStream = null;
//			try {
//				inputStream = source.getContentInputStream(em, id);
//				fileOutputStream = new FileOutputStream(file);
//				KaraUtils.copyStream(inputStream, fileOutputStream);
//			} catch (DcemException e) {
//				// if Entity has no content like folder
//				if (e.getErrorCode() == DcemErrorCodes.CLOUD_SAFE_NOT_FOUND && cloudSafeEntity.isFolder()
//						&& cloudSafeEntity.isOption(CloudSafeOptions.FPD) == false) {
//					continue;
//				} else {
//					throw e;
//				}
//			} finally {
//				if (fileOutputStream != null) {
//					fileOutputStream.close();
//				}
//			}
			
			try {
				inputStream = source.getContentInputStream(em, id);
			} catch (DcemException e) {
				// if Entity has no content like folder
				if (e.getErrorCode() == DcemErrorCodes.CLOUD_SAFE_NOT_FOUND && cloudSafeEntity.isFolder()
						&& cloudSafeEntity.isOption(CloudSafeOptions.FPD) == false) {
					continue;
				} else {
					throw e;
				}
			}
			if (cloudSafeEntity.isGcm() == false && cloudSafeEntity.isOption(CloudSafeOptions.ENC)) {
				System.out.println("CallCloudSafeStorageCopy.copyStorage()");
			}
			logger.info("Reading: " + cloudSafeEntity.isGcm() + " " + cloudSafeEntity.getOptions() + " " + cloudSafeEntity.getId() + ",  Name: " + cloudSafeEntity.getName() + "Size: " + cloudSafeEntity.getLength() 
			);
			cloudSafeEntity.setNewEntity(true);
			try {
				destination.writeContentOutput(em, cloudSafeEntity, inputStream);
			} catch (DcemException e) {
				if (e.getErrorCode() == DcemErrorCodes.CLOUD_SAFE_DUPLICAE_ENTRY) {
					inputStream = source.getContentInputStream(em, id);
					cloudSafeEntity.setNewEntity(false);
					destination.writeContentOutput(em, cloudSafeEntity, inputStream);
				} else {
					logger.warn("could not write the content of:  " + cloudSafeEntity.getName(), e);
					throw e;
				}
			}
			logger.info("Write done for :  " + cloudSafeEntity.getId() + ",  Name: " + cloudSafeEntity.getName() + "Size: " + cloudSafeEntity.getLength());
		}

	}

}
