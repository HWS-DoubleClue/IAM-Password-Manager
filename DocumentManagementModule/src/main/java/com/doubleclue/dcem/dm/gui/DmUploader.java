package com.doubleclue.dcem.dm.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DialogFrameworkOptions;

import com.doubleclue.dcem.as.dm.UploadDocument;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.CloudSafeTagLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewNavigator;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.utils.typedetector.FileUploadDetector;
import com.doubleclue.dcem.dm.logic.DmConstants;
import com.doubleclue.dcem.dm.logic.DmUtils;
import com.doubleclue.dcem.dm.logic.tasks.SaveDocumentsTask;
import com.doubleclue.utils.KaraUtils;

@SuppressWarnings("serial")
@Named("dmUploader")
@SessionScoped
public class DmUploader implements Serializable {

	protected static final Logger logger = LogManager.getLogger(DmUploader.class);

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	CloudSafeTagLogic cloudSafeTagLogic;

	@Inject
	DmDocumentView dmDocumentView;

	@Inject
	TaskExecutor taskExecutor;

	@Inject
	protected ViewNavigator viewNavigator;

	SaveDocumentsTask saveDocumentsTask;
	List<UploadDocument> uploadingDocuments;
	List<UploadDocument> uploadingQueue = Collections.synchronizedList(new ArrayList<>());
	
	Future<?> future;

	boolean dialogOpen = false;
	int position = 0;

	public void uploadFileListener(FileUploadEvent event) {
	System.out.println("DmUploader.uploadFileListener() " + event.getFile().getWebkitRelativePath());
		if (uploadingDocuments == null) {
			uploadingDocuments =new ArrayList<>();
		}
		try {
			File file = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, "");
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			InputStream inputStream = event.getFile().getInputStream();
			KaraUtils.copyStream(inputStream, fileOutputStream);
			fileOutputStream.close();
			inputStream.close();
			UploadDocument uploadedDocument = new UploadDocument(event.getFile().getFileName(), file, dmDocumentView.getSelectedFolder(),
					FileUploadDetector.getMediaType(event.getFile().getFileName(), file));
			uploadedDocument.setWebPath(event.getFile().getWebkitRelativePath());
			uploadingDocuments.add(uploadedDocument);
		} catch (Exception e) {
			logger.error("Could not prepare files: ", e);
			JsfUtils.addErrorMessage("Could not prepare files: " + e.toString());
			return;
		}
	}

	public void onUploadComplete() {
		startUploading();
		return;
	}
	
	public void uploadDocument(UploadDocument uploadedDocument) {
		if (uploadingDocuments == null) {
			uploadingDocuments =new ArrayList<>();
		}
		uploadingDocuments.add(uploadedDocument);
		startUploading();
		return;
	}
	

	private void startUploading() {
		uploadingQueue.addAll(uploadingDocuments);
		uploadingDocuments.clear();
		if (saveDocumentsTask == null) {
			saveDocumentsTask = new SaveDocumentsTask(this, operatorSessionBean.getDcemUser());
			if (future == null || future.isDone()) {
				future = taskExecutor.submit(saveDocumentsTask);
			}
		}
		if (dialogOpen == false) {
			DialogFrameworkOptions options = DialogFrameworkOptions.builder().modal(false).width("400").height("340").contentHeight("100%").contentWidth("100%").closable(false)
					.position("right bottom").styleClass("uploaderDialog").minimizable(true).maximizable(true).iframeStyleClass("uploaderFrame").build();
			PrimeFaces.current().dialog().openDynamic(DcemConstants.WEB_MGT_CONTEXT + "/modules/dm/dmUploaderDialog.xhtml", options, null);
		}
	}

	public List<UploadDocument> getUploadingDocuments() {
		return uploadingQueue;
	}

	public UploadDocument callBackHasNext(UploadDocument uploadedDocument) {
		if (uploadedDocument != null) {
			position++;
			uploadedDocument.setSeconds((System.currentTimeMillis() / 1000) - uploadedDocument.getStartSeconds());
		}
		if (uploadingQueue == null || uploadingQueue.isEmpty()) {
			return null;
		}
		if (position == uploadingQueue.size()) {
			return null;
		}
		UploadDocument uploadDocument = uploadingQueue.get(position);
		uploadDocument.setStartSeconds(System.currentTimeMillis() / 1000);
		return uploadDocument;
	}

	public void actionPoll() {
		if ((future == null || future.isDone()) && isReady() == false) {
			future = taskExecutor.submit(saveDocumentsTask);
		}
	}

	public boolean isReady() {
		if (position == -1 || position == uploadingQueue.size()) {
			return true;
		}
		return false;
	}

	public String getInfo(UploadDocument uploadDocument) {
		switch (uploadDocument.getStatus()) {
		case Processing:
			return dmDocumentView.getResourceBundle().getString("processing");
		case Uploaded:
			return dmDocumentView.getResourceBundle().getString("uploaded");
		case Error:
			return uploadDocument.getException().getLocalizedMessage();
		case Waiting:
			return dmDocumentView.getResourceBundle().getString("waiting");
		}
		return null;
	}

	public String getTimeElapse(UploadDocument uploadDocument) {
		switch (uploadDocument.getStatus()) {
		case Processing:
			return getTimeFromSeconds((System.currentTimeMillis() / 1000) - uploadDocument.getStartSeconds());
		case Uploaded:
		case Error:
			return getTimeFromSeconds(uploadDocument.getSeconds());
		case Waiting:
			return "";
		}
		return null;
	}

	private String getTimeFromSeconds(long secondsCount) {
		long hourCount = TimeUnit.SECONDS.toHours(secondsCount);
		long minutesCount = TimeUnit.SECONDS.toMinutes(secondsCount);
		secondsCount -= TimeUnit.HOURS.toSeconds(hourCount);
		return String.format("%02d:%02d:%02d", hourCount, minutesCount, secondsCount);
	}

	public void actionClose() {
		position = 0;
		uploadingDocuments.clear();
		uploadingQueue.clear();
		PrimeFaces.current().dialog().closeDynamic(null);
		saveDocumentsTask = null;
		future = null;
		dmDocumentView.setShouldRefreshCurrentFiles(true);
	}

	public String getDocumentCounts() {
		return Integer.toString(position) + "/" + uploadingQueue.size();
	}

}
