package com.doubleclue.dcem.dm.gui;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;
import org.primefaces.event.CaptureEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

import com.doubleclue.comm.thrift.CloudSafeOptions;
import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.as.dm.UploadDocument;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.CloudSafeTagEntity;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.CloudSafeTagLogic;
import com.doubleclue.dcem.as.logic.cloudsafe.DocumentVersion;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.DcemUploadFile;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.utils.mail.MailUtils;
import com.doubleclue.dcem.core.utils.typedetector.DcemMediaType;
import com.doubleclue.dcem.core.utils.typedetector.FileUploadDetector;
import com.doubleclue.dcem.dm.entities.DmWorkflowEntity;
import com.doubleclue.dcem.dm.logic.DmConstants;
import com.doubleclue.dcem.dm.logic.DmSolrLogic;
import com.doubleclue.dcem.dm.logic.DmUtils;
import com.doubleclue.dcem.dm.logic.DocumentLogic;
import com.doubleclue.dcem.dm.logic.DocumentManagementModule;
import com.doubleclue.dcem.dm.logic.PdfManagement;
import com.doubleclue.dcem.dm.subjects.DmNewDocumentSubject;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.utils.KaraUtils;

@SuppressWarnings("serial")
@Named("dmNewDocumentView")
@SessionScoped
public class DmNewDocumentView extends DcemView {

	@Inject
	DmNewDocumentSubject dmNewDocumentSubject;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	PdfManagement pdfManagement;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	DmDocumentShareDialog dmDocumentShareDialog;

	@Inject
	DmDocumentView dmDocumentView;

	@Inject
	DmSharedDocumentView dmSharedDocumentView;

	@Inject
	CloudSafeTagLogic cloudSafeTagLogic;

	@Inject
	DocumentLogic documentLogic;

	@Inject
	DocumentManagementModule documentManagementModule;

	@Inject
	DmSolrLogic solrLogic;
	
	@Inject
	DmUploader uploader;

	private DcemMediaType dcemMediaType;
	private File contentFile = null;
	private File originalFile = null;
	private UploadedFile uploadedFilePage;
	private CloudSafeEntity cloudSafeEntity;
	private String ownerGroup;
	private String ocrText;
	private CloudSafeTagEntity toBeAddedTag;

	private DualListModel<CloudSafeTagEntity> tagDualList;
	private List<CloudSafeEntity> cloudSafeEntityFiles;
	private List<CloudSafeTagEntity> toBeAddedTags;

	private byte[] thumbnail;
	private int maxPages;
	private int deletePageFrom;
	private int deletePageTo;
	ResourceBundle resourceBundle;
	boolean createThumbnail;

	DocumentVersion selectedVersion;
	List<DocumentVersion> cacheVersion;

	int viewColumns = 2;
	boolean documentModified;

	@PostConstruct
	private void init() {
		subject = dmNewDocumentSubject;
		resourceBundle = JsfUtils.getBundle(DocumentManagementModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		toBeAddedTags = new ArrayList<>();
		reload();
	}

	public void preRenderView() {
		if (dcemMediaType == null) {
			viewNavigator.setActiveView(DocumentManagementModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + dmDocumentView.getSubject().getViewName());
		}
	}

	@Override
	public String getDisplayName() {
		if (cloudSafeEntity.getId() == null) {
			return JsfUtils.getStringSafely(resourceBundle, "dmNewDocumentView");
		} else {
			return JsfUtils.getStringSafely(resourceBundle, "dmEditDocumentView");
		}
	}

	@Override
	public void reload() {
		if (cloudSafeEntity == null) {
			cloudSafeEntity = new CloudSafeEntity();
			cloudSafeEntity.setParent(cloudSafeLogic.getCloudSafeRoot());
			cloudSafeEntity.setOwner(CloudSafeOwner.USER);
			cloudSafeEntity.setUser(operatorSessionBean.getDcemUser());
			cloudSafeEntity.setOptions(CloudSafeOptions.ENC.name());
			cloudSafeEntity.setDcemMediaType(DcemMediaType.PDF);
			tagDualList = new DualListModel<CloudSafeTagEntity>();
		}
		toBeAddedTag = new CloudSafeTagEntity();
		try {
			List<CloudSafeTagEntity> tagList = cloudSafeTagLogic.getAllTags();
			tagList.removeAll(tagDualList.getTarget());
			tagDualList.setSource(tagList);
			reloadContentFileStream();
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			logger.error("", e);
		}

	}

	@Override
	public void leavingView() {
		if (contentFile != null) {
			contentFile.delete();
			contentFile = null;
		}
	}

	public void uploadPageListener(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		try {
			logger.debug("starting..");
			ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(uploadedFile.getContent());
			DcemMediaType dcemMediaType = FileUploadDetector.detectDcemMediaType(arrayInputStream);
			switch (dcemMediaType) {
			case PDF:
				File outputFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
				maxPages = pdfManagement.mergePdf(contentFile, (File) null, uploadedFile.getInputStream(), outputFile);
				contentFile.delete();
				contentFile = outputFile;
				documentModified = true;
				break;
			case JPEG:
			case PNG:
				addImagePdf(uploadedFile.getContent());
				logger.debug("adter addImagePdf");
				documentModified = true;
				break;
			default:
				JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "editDocument.message.formatNotSupported");
			}
		} catch (Exception e) {
			logger.error("Couldn't upload file", e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		}
	}

	private void reloadContentFileStream() {
		try {
			if (contentFile == null) {
				contentFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
				pdfManagement.createEmptyPdfFile(contentFile);
			}
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			logger.error("Couldn't load Pdf File", e);
		}
	}

	public void actionSave() {
		cloudSafeEntity.setLastModified(LocalDateTime.now());
		File fileToSave = contentFile;
		if (cloudSafeEntity.getId() == null) {
			try { // check for name
				CloudSafeEntity cloudSafeEntityExists = cloudSafeLogic.getUserdocument(operatorSessionBean.getDcemUser(), cloudSafeEntity.getName(),
						cloudSafeEntity.getParent());
				if (cloudSafeEntityExists != null) {
					JsfUtils.addErrorMessage(resourceBundle.getString("documentView.error.fileNameAlreadyExists"));
					return;
				}
			} catch (Exception e) {
				JsfUtils.addErrorMessage(resourceBundle, "warning.notindexed");
				logger.error("Couldn't retrieve document", e);
				return;
			}
		}
		try {
			cloudSafeEntity.setLastModifiedUser(operatorSessionBean.getDcemUser());
			cloudSafeEntity.setLength(contentFile.length());
			switch (dcemMediaType) {
			case TEXT: {
				createThumbnail = false;
				String text = DmUtils.convertTextToHtml(originalFile);
				File outputFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
				pdfManagement.convertHtmlStringToPDF(text, outputFile);
				thumbnail = pdfManagement.createThumbnail(outputFile, DmConstants.THUMBNAIL_DIMENSION);
				ocrText = getStringContent();
				outputFile.delete();
				fileToSave = originalFile; // update always
			}
				break;
			case XHTML:
				createThumbnail = false;
				File outputFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
				pdfManagement.convertHtmlToPDF(originalFile, outputFile);
				thumbnail = pdfManagement.createThumbnail(outputFile, DmConstants.THUMBNAIL_DIMENSION);
				ocrText = pdfManagement.getOcrText(outputFile, DcemMediaType.PDF);
				outputFile.delete();
				fileToSave = originalFile; // update always
				break;
			case PDF:
				if (documentModified == true) {
					ocrText = documentLogic.generateOcr(cloudSafeEntity, contentFile);
					fileToSave = contentFile;
				} else {
					fileToSave = null;
				}
				break;
			default:
				fileToSave = originalFile;
				if (cloudSafeEntity.getId() != null) {
					fileToSave = null;
				}
				ocrText = null; // no indexing required
				break;
			}
			cloudSafeEntity.setTags(new TreeSet<>(tagDualList.getTarget()));
			try {
				if (createThumbnail == true) {
					thumbnail = documentLogic.getThumbnail(dcemMediaType, contentFile);
				}
				documentLogic.saveDocument(cloudSafeEntity, thumbnail, fileToSave, ocrText, toBeAddedTags, operatorSessionBean.getDcemUser(), true);
			} catch (DcemException e) {
				JsfUtils.addWarnMessage(e.getLocalizedMessage());
				logger.warn("Couldn't index file", e);
				return;
			} catch (Exception e) {
				JsfUtils.addErrorMessage(resourceBundle, "warning.notindexed");
				logger.error("Couldn't index file", e);
				return;
			} finally {
				if (fileToSave != null) {
					fileToSave.delete();
				}
				fileToSave = null;
			}
			if (dmDocumentView.isShareDocumentsMode() == false) {
				viewNavigator
						.setActiveView(DocumentManagementModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + dmDocumentView.getSubject().getViewName());
			} else {
				viewNavigator.setActiveView(
						DocumentManagementModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + dmSharedDocumentView.getSubject().getViewName());
			}

			if (cloudSafeEntity.getId() == null) {
				JsfUtils.addInfoMessage(DocumentManagementModule.RESOURCE_NAME, "editDocument.message.uploadSuccessful");
			} else {
				JsfUtils.addInfoMessage(DocumentManagementModule.RESOURCE_NAME, "editDocument.message.editSuccessful");
			}
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			logger.error("Couldn't upload file", e);
		}
	}

	public String getOcrText() {
		System.out.println("DmNewDocumentView.getOcrText() ");
		return ocrText;
	}

	public List<DocumentVersion> getVersions() {
		try {
			if (cacheVersion == null) {
				cacheVersion = cloudSafeLogic.getS3Versions(cloudSafeEntity);
			}
			return cacheVersion;
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			logger.error("Couldn't ger Doc version", e);
			return null;
		}
	}

	public LocalDateTime getVersionLastModified(DocumentVersion documentVersion) {
		LocalDateTime localDateTime = documentVersion.getLastModified().atZone(operatorSessionBean.getMyTimeZone().toZoneId()).toLocalDateTime();
		localDateTime = operatorSessionBean.getUserZonedTime(localDateTime);
		return localDateTime;
	}

	public void actionConvertRichText() {
		String text;
		try {
			text = DmUtils.convertTextToHtml(contentFile);
			setStringContent(text);
		} catch (Exception e) {
			logger.error("Couldn't convert to HTML", e);
			JsfUtils.addErrorMessage(e.toString());
			return;
		}
		cloudSafeEntity.setDcemMediaType(DcemMediaType.XHTML);
		dcemMediaType = DcemMediaType.XHTML;
	}

	public StreamedContent getStreamedContent() {
		try {
			FileInputStream inputStream = new FileInputStream(contentFile);
			String mediatypeString = "";
			if (isPdfViewer() == true) {
				mediatypeString = DcemMediaType.PDF.getMediaType();
			} else {
				mediatypeString = dcemMediaType.getMediaType();
			}
			return DefaultStreamedContent.builder().contentType(mediatypeString).stream(() -> inputStream).build();
		} catch (Exception e) {
			return null;
		}
	}

	public StreamedContent getStreamedThumbnail() {
		try {
			if (thumbnail == null) {
				dcemMediaType.getIconResource();
				return null;
			}
			String contentType = "image/jpeg";
			if (dcemMediaType == DcemMediaType.SVG) {
				contentType = dcemMediaType.getMediaType();
			}
			InputStream in = new ByteArrayInputStream(thumbnail);
			return DefaultStreamedContent.builder().contentType(contentType).stream(() -> in).build();
		} catch (Exception e) {
			return null;
		}
	}

	public String getStringContent() {
		try {
			return Files.readString(contentFile.toPath(), StandardCharsets.UTF_8);
		} catch (MalformedInputException e) {
			try {
				return Files.readString(contentFile.toPath(), StandardCharsets.ISO_8859_1);
			} catch (IOException e1) {
				logger.error("Couldn't load file to string", e);
				JsfUtils.addErrorMessage(e.toString());
			}
		} catch (Exception e) {
			logger.error("Couldn't load file to string", e);
			JsfUtils.addErrorMessage(e.toString());
		}
		return null;
	}

	public void setStringContent(String text) {
		try {
			if (text == null) {
				text = "";
			}
			Files.write(contentFile.toPath(), text.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			logger.error("Couldn't write file ", e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		}
	}

	public void actionDeletePage() {
		try {
			File outputFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
			maxPages = pdfManagement.deletePages(contentFile, outputFile, deletePageFrom, deletePageTo);
			contentFile.delete();
			contentFile = outputFile;
			createThumbnail = true;
			documentModified = true;
			PrimeFaces.current().ajax().update("documentForm");
			PrimeFaces.current().ajax().update("deletePageForm");
			hideDialog("deletePageDialog");
		} catch (DcemException e) {
			JsfUtils.addErrorMessageToComponentId(e.getLocalizedMessage(), "deletePageForm:deletePageMessages");
			return;
		} catch (Exception e) {
			logger.error("Couldn't delete Pdf Page", e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		}
	}

	private void addImagePdf(byte[] data) {
		try {
			File outputFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
			maxPages = pdfManagement.addImageToPdf(contentFile, outputFile, data, null);
			contentFile.delete();
			contentFile = outputFile;
			if (maxPages == 1 && (cloudSafeEntity.getName() == null || cloudSafeEntity.getName().isBlank())) {
				if (ocrText.isEmpty() == false) {
					int ind = ocrText.indexOf("\n");
					if (ind != -1 && ind < 128) {
						cloudSafeEntity.setName(ocrText.substring(0, ind));
					}
				}
			}
			createThumbnail = true;
		} catch (DcemException e) {
			if (e.getErrorCode() == DcemErrorCodes.OCR_TESSERACT_NOT_CONFIGURED) {
				JsfUtils.addWarnMessage(e.getLocalizedMessage());
			} else {
				JsfUtils.addErrorMessage(e.getLocalizedMessage());
			}
		} catch (Throwable e) {
			logger.error("addImagePdf", e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
		}
	}

	public void actionToDocuments() {
		if (dmDocumentView.isShareDocumentsMode() == true) {
			viewNavigator
					.setActiveView(DocumentManagementModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + dmSharedDocumentView.getSubject().getViewName());
			return;
		}
		viewNavigator.setActiveView(DocumentManagementModule.MODULE_ID + DcemConstants.MODULE_VIEW_SPLITTER + dmDocumentView.getSubject().getViewName());
		return;
	}

	public void onCapture(CaptureEvent captureEvent) {
		byte[] data = captureEvent.getData();
		try {
			addImagePdf(data);
			reloadContentFileStream();
			createThumbnail = true;
			PrimeFaces.current().dialog().closeDynamic(null);
		} catch (Throwable e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			logger.error("", e);
		}
	}

	public int getMaxPages() {
		return maxPages;
	}

	public void setMaxPages(int maxPages) {
		this.maxPages = maxPages;
	}

	public String getTextContent() {
		return DmUtils.convertTextToHtml(ocrText);
	}

	public CloudSafeEntity getCloudSafeEntity() {
		return cloudSafeEntity;
	}

	public void actionRecoverVersion() {
		try {
			InputStream inputStream = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, null, null, selectedVersion.getVersionId());
			File originalFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, "");
			FileOutputStream fileOutputStream = new FileOutputStream(originalFile);
			KaraUtils.copyStream(inputStream, fileOutputStream);
			fileOutputStream.close();
			LocalDateTime localDateTime = getVersionLastModified(selectedVersion);
			String newName = cloudSafeEntity.getName() + localDateTime.format(DcemConstants.DATE_TIME_FORMATTER_EXIST);
//			CloudSafeEntity cloudSafeEntityDb = cloudSafeLogic.getCloudSafe(cloudSafeEntity.getId());
			CloudSafeEntity parent = cloudSafeLogic.getCloudSafe(cloudSafeEntity.getParent().getId());
			UploadDocument uploadDocument = new UploadDocument (newName, contentFile, parent, cloudSafeEntity.getDcemMediaType());
			uploadDocument.setRecoverFrom(cloudSafeEntity);
			uploader.uploadDocument(uploadDocument);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			logger.error("", e);
			return;
		}

	}

	public void addDocument(DcemMediaType mediaType_, CloudSafeEntity folder) throws Exception {
		createNewCloudSafeEntity(folder);
		cloudSafeEntity.setDcemMediaType(mediaType_);
		ocrText = null;
		documentModified = true;
		thumbnail = null;
		createThumbnail = true;
		this.dcemMediaType = mediaType_;
		contentFile = originalFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, "");
		if (dcemMediaType == DcemMediaType.PDF) {
			pdfManagement.createEmptyPdfFile(contentFile);
		}
		return;
	}

	private void createNewCloudSafeEntity(CloudSafeEntity folder) throws Exception {
		cloudSafeEntity = new CloudSafeEntity();
		cloudSafeEntity.setParent(folder);
		cloudSafeEntity.setOwner(CloudSafeOwner.USER);
		cloudSafeEntity.setUser(operatorSessionBean.getDcemUser());
		cloudSafeEntity.setOptions(CloudSafeOptions.ENC.name());
		if (contentFile == null) {
			contentFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, "");
		}
		cloudSafeEntity.setCreatedOn(LocalDateTime.now());
		ocrText = null;
		thumbnail = null;
		tagDualList = new DualListModel<CloudSafeTagEntity>();
	}

	public void actionShowOcr() {
		try {
			if (ocrText == null) {
				ocrText = documentLogic.generateOcr(cloudSafeEntity, originalFile);
			}
			this.showDialog("ocrTextDialog");
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			logger.error("", e);
			return;
		}
	}

	public void editDocument(CloudSafeEntity cloudSafeEntity, List<CloudSafeEntity> cloudSafeEntityFiles) throws Exception {
		cacheVersion = null;
		this.cloudSafeEntity = cloudSafeEntity;
		this.cloudSafeEntityFiles = cloudSafeEntityFiles;
		if (cloudSafeEntity.isFolder()) {
			thumbnail = null;
			createThumbnail = false;
			dcemMediaType = DcemMediaType.Folder;
			return;
		}
		dcemMediaType = cloudSafeEntity.getDcemMediaType();
		
		tagDualList.setTarget(new ArrayList<CloudSafeTagEntity>(cloudSafeEntity.getTags()));
		originalFile = documentLogic.getDocumentContent(cloudSafeEntity);
		if (dcemMediaType == null) {
			dcemMediaType =  FileUploadDetector.getMediaType(cloudSafeEntity.getName(), originalFile);
			cloudSafeEntity.setDcemMediaType(dcemMediaType);
		}
		ocrText = null;
		thumbnail = cloudSafeEntity.getThumbnail();
		createThumbnail = false;
		toBeAddedTags.clear();
		documentModified = false;
		selectedVersion = null;
		switch (cloudSafeEntity.getDcemMediaType()) {
		case ODT:
			contentFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
			pdfManagement.convertOdtToPDF(originalFile, contentFile);
			break;
		case WORD:
			contentFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
			pdfManagement.convertWordToPDF(originalFile, contentFile);
			break;
		case XLSX:
			contentFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
			pdfManagement.convertExcelToPdf(originalFile, contentFile);
			break;
		case JPEG:
		case PNG:
		case GIF:
		case SVG:
			contentFile = originalFile;
			break;
		case PDF:
			maxPages = pdfManagement.getNoOfPages(originalFile);
			contentFile = originalFile;
			break;
		case MAIL:
			List<DcemUploadFile> listFiles = MailUtils.processReceivedMail(originalFile, resourceBundle);
			contentFile = documentLogic.emailToPDF(resourceBundle, listFiles);
			break;
		default:
			contentFile = originalFile;
			break;
		}
	}

	public List<CloudSafeEntity> getFolderDocuments() {
		// System.out.println("DmNewDocumentView.getFolderDocuments()");
		try {
			if (cloudSafeEntity.getOwner() == CloudSafeOwner.GROUP) {
				return cloudSafeLogic.getByParentId(cloudSafeEntity);
			} else {
				return cloudSafeLogic.getCloudSafeByUserAndParentId(cloudSafeEntity.getId(), operatorSessionBean.getDcemUser(),
						operatorSessionBean.getUserGroups(), false);
			}
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			logger.error("", e);
			return null;
		}
	}

	public List<DmWorkflowEntity> getWorkflows() {
		System.out.println("DmNewDocumentView.getWorkflows()");
		List<DmWorkflowEntity> dmWorkflowEntities = new ArrayList<>();
		return dmWorkflowEntities;
	}

	public boolean isPdfViewer() {
		switch (dcemMediaType) {
		case PDF:
		case WORD:
		case ODT:
		case XLSX:
		case MAIL:
			return true;
		default:
			return false;
		}
	}

	public boolean isImageViewer() {
		switch (dcemMediaType) {
		case PNG:
		case JPEG:
		case GIF:
		case SVG:
			return true;
		default:
			return false;
		}
	}

	public void actionStartCamera() {
		if (cloudSafeEntity.getDcemMediaType() == DcemMediaType.PDF) {
			Map<String, Object> options = new HashMap<String, Object>();
			options.put("position", "top");
			options.put("headerElement", "customheader");
			options.put("contentHeight", 700);
			options.put("position", "top");
			options.put("width", 700);
			options.put("contentWidth", 700);
			options.put("responsive", true);
			// options.put("closable", false);
			PrimeFaces.current().dialog().openDynamic(DcemConstants.WEB_MGT_CONTEXT + "/modules/dm/dmPhotoCamDialog.xhtml", options, null);
		}
	}

	public void actionChangeOwnerShip() {
		try {
			DcemGroup currentOwnerGroup = null;
			if (ownerGroup == null || ownerGroup.trim().isEmpty()) {
				currentOwnerGroup = null;
			} else {
				ownerGroup = ownerGroup.trim();
				currentOwnerGroup = dmDocumentShareDialog.findGroup(ownerGroup);
				if (currentOwnerGroup == null) {
					JsfUtils.addErrorMessage(resourceBundle.getString("message.wrongGroup")); // TODO RESOURCE
					return;
				}
			}
			if (getExistingFileGroup(cloudSafeEntity, currentOwnerGroup)) {
				JsfUtils.addErrorMessage(resourceBundle.getString("message.fileGroupExist")); // TODO RESOURCE
				return;
			}
			cloudSafeLogic.changeOnwerShipCloudSafeEntity(cloudSafeEntity, currentOwnerGroup, operatorSessionBean.getDcemUser());
			JsfUtils.addInfoMessageToComponentId("Group changed succesfully", "groupOwnerTabMsg"); // TODO RESOURCE
		} catch (DcemException e) {
			logger.warn("Couldn't change ownerShip of file to Group.", e);
			JsfUtils.addWarnMessage(e.getLocalizedMessage());
			return;
		} catch (Exception e) {
			logger.warn("something went wrong by changing the ownership.", e);
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			return;
		}
	}

	private boolean getExistingFileGroup(CloudSafeEntity cloudSafeEntityToGroup, DcemGroup currentOwnerGroup) {
		if (cloudSafeEntityFiles == null || currentOwnerGroup == null) {
			return false;
		}
		for (CloudSafeEntity cloudSafeEntity : cloudSafeEntityFiles) {
			if (cloudSafeEntity.getName().equalsIgnoreCase(cloudSafeEntityToGroup.getName())
					&& cloudSafeEntity.getGroup().getId().equals(currentOwnerGroup.getId())) {
				return true;
			}
		}
		return false;
	}

	public void actionNewTag() {
		toBeAddedTag.setName(toBeAddedTag.getName().trim());
		if (toBeAddedTag.getName().isEmpty() && toBeAddedTag.getName().length() < 2) {
			JsfUtils.addErrorMessage(DocumentManagementModule.RESOURCE_NAME, "newTag.invalid.name");
			return;
		}
		if (DmUtils.isValidName(toBeAddedTag.getName()) == false) {
			JsfUtils.addErrorMessage(
					JsfUtils.getStringSafely(DocumentManagementModule.RESOURCE_NAME, "newTag.invalidCharacters") + ": " + DmConstants.SPECIAL_CHARACTERS);
			return;
		}
		CloudSafeTagEntity existingTag = cloudSafeTagLogic.getTagByName(toBeAddedTag.getName());
		if (existingTag != null) {
			JsfUtils.addErrorMessage(SystemModule.RESOURCE_NAME, "db.constrain.at.insert");
			return;
		}
		toBeAddedTags.add(toBeAddedTag);
		toBeAddedTag = new CloudSafeTagEntity();
		hideDialog("overlayTagPanelVar");
	}

	public void actionLeftRight() {
		viewColumns = 2;
	}

	public void actionUpDown() {
		viewColumns = 1;
	}

	public void removeToBeAddedTag(CloudSafeTagEntity removeTag) {
		toBeAddedTags.removeIf(tag -> tag.getName().equals(removeTag.getName()));
	}

	public boolean isPdf() {
		return cloudSafeEntity.getDcemMediaType() == DcemMediaType.PDF;
	}

	public byte[] getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(byte[] thumbnail) {
		this.thumbnail = thumbnail;
	}

	public UploadedFile getUploadedFilePage() {
		return uploadedFilePage;
	}

	public void setUploadedFilePage(UploadedFile uploadedFilePage) {
		this.uploadedFilePage = uploadedFilePage;
	}

	public String getLastModifiedFormatted() {
		return DcemUtils.formatDateTime(operatorSessionBean.getLocale(), cloudSafeEntity.getLastModified());
	}

	public String getCreatedOnFormatted() {
		return DcemUtils.formatDateTime(operatorSessionBean.getLocale(), cloudSafeEntity.getCreatedOn());
	}

	public boolean isNewDocument() {
		return cloudSafeEntity.getId() == null;
	}

	public String getOwnerGroup() {
		return ownerGroup;
	}

	public void setOwnerGroup(String ownerGroup) {
		this.ownerGroup = ownerGroup;
	}

	public DualListModel<CloudSafeTagEntity> getTagDualList() {
		return tagDualList;
	}

	public void setTagDualList(DualListModel<CloudSafeTagEntity> tagDualList) {
		this.tagDualList = tagDualList;
	}

	public CloudSafeTagEntity getToBeAddedTag() {
		return toBeAddedTag;
	}

	public void setToBeAddedTag(CloudSafeTagEntity toBeAddedTag) {
		this.toBeAddedTag = toBeAddedTag;
	}

	public List<CloudSafeTagEntity> getToBeAddedTags() {
		return toBeAddedTags;
	}

	public void setToBeAddedTags(List<CloudSafeTagEntity> toBeAddedTags) {
		this.toBeAddedTags = toBeAddedTags;
	}

	public int getDeletePageFrom() {
		return deletePageFrom;
	}

	public void setDeletePageFrom(int deletePageFrom) {
		this.deletePageFrom = deletePageFrom;
	}

	public int getDeletePageTo() {
		return deletePageTo;
	}

	public void setDeletePageTo(int deletePageTo) {
		this.deletePageTo = deletePageTo;
	}

	public String getConfirmationText() {
		return MessageFormat.format(resourceBundle.getString("editDocument.confirmationText"), cloudSafeEntity.getName());
	}

	public DcemMediaType getDcemMediaType() {
		if (dcemMediaType == null) {
			dcemMediaType = DcemMediaType.Unknown;
		}
		return dcemMediaType;
	}

	public void setDcemMediaType(DcemMediaType dcemMediaType) {
		this.dcemMediaType = dcemMediaType;
	}

	public int getViewColumns() {
		return viewColumns;
	}

	public void setViewColumns(int viewColumns) {
		this.viewColumns = viewColumns;
	}

	public DocumentVersion getSelectedVersion() {
		return selectedVersion;
	}

	public void setSelectedVersion(DocumentVersion selectedVersion) {
		this.selectedVersion = selectedVersion;
	}
}
