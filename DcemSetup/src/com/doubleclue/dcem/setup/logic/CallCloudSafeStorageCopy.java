package com.doubleclue.dcem.setup.logic;

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

	private void copyStorage(CloudSafeContentI source, CloudSafeContentI destination) throws Exception, SQLException, IOException {
		CloudSafeLogic cloudSafeLogic = CdiUtils.getReference(CloudSafeLogic.class);
		List<Integer> list = cloudSafeLogic.getIdsOfEntries();
		CloudSafeEntity cloudSafeEntity;
		InputStream inputStream = null;
		for (Integer id : list) {
			cloudSafeEntity = cloudSafeLogic.getCloudSafe(id);
			EntityManager em = CdiUtils.getReference(EntityManager.class);
			try {
				inputStream = source.getContentInputStream(em, id);
			} catch (DcemException e) {
				// if Entity has no content like folder
				if (e.getErrorCode() == DcemErrorCodes.CLOUD_SAFE_NOT_FOUND && cloudSafeEntity.isFolder() && cloudSafeEntity.isOption(CloudSafeOptions.FPD) == false) {
					continue;
				} else {
					throw e;
				}
			}
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

		}
	}

}
