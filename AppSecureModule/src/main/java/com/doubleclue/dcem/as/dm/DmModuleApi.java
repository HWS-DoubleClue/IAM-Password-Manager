package com.doubleclue.dcem.as.dm;

import java.util.Map;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.core.entities.DcemUser;

public interface DmModuleApi {
	
	public CloudSafeEntity saveNewDocument(UploadDocument uploadDocument, DcemUser dcemUser, Map<String, CloudSafeEntity> folderCache) throws Exception;
	
}