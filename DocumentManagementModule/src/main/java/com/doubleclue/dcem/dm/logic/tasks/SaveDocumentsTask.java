package com.doubleclue.dcem.dm.logic.tasks;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;

import com.doubleclue.dcem.as.dm.UploadDocument;
import com.doubleclue.dcem.as.dm.UploadDocumentStatus;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.tasks.CoreTask;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.dm.gui.DmUploader;
import com.doubleclue.dcem.dm.logic.DocumentLogic;
import com.doubleclue.dcem.dm.logic.PdfManagement;

public class SaveDocumentsTask extends CoreTask {

	protected static final Logger logger = LogManager.getLogger(SaveDocumentsTask.class);

	DcemUser dcemUser;
	PdfManagement pdfManagement;
	DocumentLogic documentLogic;
	DmUploader dmUploader;
	CloudSafeLogic cloudSafeLogic;
	Map<String, CloudSafeEntity> folderCache = new HashMap<String, CloudSafeEntity>();

	public SaveDocumentsTask(DmUploader dmUploader, DcemUser dcemUser) {
		super(SaveDocumentsTask.class.getSimpleName(), TenantIdResolver.getCurrentTenant());
		this.dcemUser = dcemUser;
		this.dmUploader = dmUploader;
		pdfManagement = CdiUtils.getReference(PdfManagement.class);
		documentLogic = CdiUtils.getReference(DocumentLogic.class);
		cloudSafeLogic = CdiUtils.getReference(CloudSafeLogic.class);
		;
	}

	@Override
	public void runTask() {
		// System.out.println("SaveDocumentsTask.runTask()");
		Thread.currentThread().setName(this.getClass().getSimpleName());
		ThreadContext.put(DcemConstants.MDC_USER_ID, dcemUser.getLoginId());
		UploadDocument uploadDocument = dmUploader.callBackHasNext(null);
		while (uploadDocument != null) {
			try {
				uploadDocument.setStatus(UploadDocumentStatus.Processing);
				documentLogic.saveNewDocument(uploadDocument, dcemUser, folderCache);
				uploadDocument.setStatus(UploadDocumentStatus.Uploaded);
			} catch (Exception exp) {
				uploadDocument.setStatus(UploadDocumentStatus.Error);
				uploadDocument.setException(exp);
				logger.error("Couldn't upload File: " + uploadDocument.getName(), exp);
			} catch (Throwable exp) {
				logger.error("Couldn't upload File: " + uploadDocument.getName(), exp);
			} finally {
				uploadDocument = dmUploader.callBackHasNext(uploadDocument);
			}
		}
	}
}
