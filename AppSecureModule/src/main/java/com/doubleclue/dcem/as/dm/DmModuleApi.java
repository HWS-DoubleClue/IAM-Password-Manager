package com.doubleclue.dcem.as.dm;

import java.util.Map;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemException;

public interface DmModuleApi {
	
	public CloudSafeEntity saveNewDocument(UploadDocument uploadDocument, DcemUser dcemUser, Map<String, CloudSafeEntity> folderCache) throws Exception;
	public int deleteWorkflowForDocument (int id) throws Exception;
	
}