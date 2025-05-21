package com.doubleclue.dcem.dm.logic;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;

import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.comm.thrift.CloudSafeOptions;
import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.as.dm.DmModuleApi;
import com.doubleclue.dcem.as.dm.UploadDocument;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.CloudSafeTagEntity;
import com.doubleclue.dcem.as.entities.CloudSafeThumbnailEntity;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.logic.CloudSafeTagLogic;
import com.doubleclue.dcem.as.logic.DataUnit;
import com.doubleclue.dcem.core.as.DcemUploadFile;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.utils.mail.MailUtils;
import com.doubleclue.dcem.core.utils.typedetector.DcemMediaType;
import com.doubleclue.dcem.core.utils.typedetector.FileUploadDetector;
import com.doubleclue.utils.KaraUtils;

@ApplicationScoped
@Named("documentLogic")
public class DocumentLogic implements DmModuleApi {

	@Inject
	UserLogic userLogic;

	@Inject
	GroupLogic groupLogic;

	@Inject
	DmSolrLogic solrLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	DmWorkflowLogic workflowLogic;

	@Inject
	CloudSafeTagLogic cloudSafeTagLogic;

	@Inject
	PdfManagement pdfManagement;

	@Inject
	DocumentManagementModule documentManagementModule;

	@DcemTransactional
	public String addEmailDocument(String subjectName, String identifier, File emlFile) throws Exception {
		String[] ids = identifier.split("-");
		// user or group, id of user or group, folder Id
		DcemUser dcemUser = null;
		DcemGroup dcemGroup = null;
		if (ids.length != 3) {
			throw new Exception("Wrong Identifiers");
		}
		if (ids[0].equals("0")) { // for user mail
			int userId = Integer.parseInt(ids[1]);
			dcemUser = userLogic.getUser(userId);
			if (dcemUser == null) {
				throw new Exception("Wrong User Identifier");
			}
		} else if (ids[0].equals("1")) {
			int userGroupId = Integer.parseInt(ids[1]);
			dcemGroup = groupLogic.getGroup(userGroupId);
		} else {
			throw new Exception("Wrong Identifier type");
		}
		ResourceBundle resourceBundle;
		if (dcemUser != null) {
			resourceBundle = ResourceBundle.getBundle(DocumentManagementModule.RESOURCE_NAME, dcemUser.getLanguage().getLocale());
		} else {
			resourceBundle = ResourceBundle.getBundle(DocumentManagementModule.RESOURCE_NAME);
		}
		List<DcemUploadFile> listFiles = MailUtils.processReceivedMail(emlFile, resourceBundle);
		if (dcemUser != null) {
			resourceBundle = ResourceBundle.getBundle(DocumentManagementModule.RESOURCE_NAME, dcemUser.getLanguage().getLocale());
		} else {
			resourceBundle = ResourceBundle.getBundle(DocumentManagementModule.RESOURCE_NAME);
		}
		File pdfFile = emailToPDF(resourceBundle, listFiles);
		CloudSafeEntity cloudSafeEntity = createNewCloudSafeEntity(cloudSafeLogic.getCloudSafeRoot(), dcemUser);
		cloudSafeEntity.setDcemMediaType(DcemMediaType.MAIL);
		String name = com.doubleclue.utils.StringUtils.removeInvalidCharacters(subjectName);
		if (name.isBlank()) {
			name = "Mail-without-Subject";
		}
		cloudSafeEntity.setName(name);
		CloudSafeEntity cloudSafeEntityParent = cloudSafeLogic.getCloudSafe(Integer.parseInt(ids[2]));

		if (dcemUser != null) {
			cloudSafeEntity.setOwner(CloudSafeOwner.USER);
			cloudSafeEntity.setUser(dcemUser);
		} else {
			cloudSafeEntity.setOwner(CloudSafeOwner.GROUP);
			cloudSafeEntity.setGroup(dcemGroup);
			cloudSafeEntity.setUser(userLogic.getSuperAdmin());
		}
		cloudSafeEntity.setParent(cloudSafeEntityParent);
		cloudSafeLogic.updateNameIfDoubleName(cloudSafeEntity);
		cloudSafeEntity.setInfo(listFiles.getFirst().info);
		byte[] thumbnail = pdfManagement.createThumbnail(pdfFile, DmConstants.THUMBNAIL_DIMENSION);
		CloudSafeThumbnailEntity thumbnailEntity = new CloudSafeThumbnailEntity(thumbnail);
		thumbnailEntity.setCloudSafeEntity(cloudSafeEntity);
		cloudSafeEntity.setThumbnailEntity(thumbnailEntity);
		String ocrText = pdfManagement.getOcrText(pdfFile, DcemMediaType.PDF);
		try {
			saveDocument(cloudSafeEntity, thumbnail, emlFile, ocrText, null, dcemUser, true);
		} catch (Exception e) {
			pdfFile.delete();
			throw e;
		}
		pdfFile.delete();
		return null;
	}

	public File emailToPDF(ResourceBundle resourceBundle, List<DcemUploadFile> listFiles) throws Exception {
		File pdfFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, "");
		pdfManagement.createEmptyPdfFile(pdfFile);
		DcemUploadFile dcemUploadFileBody = listFiles.getFirst();
		pdfManagement.convertHtmlToPDF(dcemUploadFileBody.file, pdfFile);
		File fileTemp = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, "");
		String emailPrefix = resourceBundle.getString("email.introAttachmentTemplate");
		for (int i = 1; i < listFiles.size(); i++) {
			DcemUploadFile dcemUploadFile = listFiles.get(i);
			File fileOutput = addAttachmentPrefix(pdfFile, dcemUploadFile, emailPrefix);
			pdfFile.delete();
			pdfFile = fileOutput;
			fileOutput = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, ".pdf");
			switch (dcemUploadFile.dcemMediaType) {
			case WORD:
				pdfManagement.convertWordToPDF(dcemUploadFile.file, fileTemp);
				pdfManagement.mergePdf(pdfFile, fileTemp, null, fileOutput);
				break;
			case XLSX:
				pdfManagement.convertExcelToPdf(dcemUploadFile.file, fileTemp);
				pdfManagement.mergePdf(pdfFile, fileTemp, null, fileOutput);
				break;
			case TEXT:
				String text = DmUtils.convertTextToHtml(dcemUploadFile.file);
				pdfManagement.convertHtmlStringToPDF(text, fileTemp);
				pdfManagement.mergePdf(pdfFile, fileTemp, null, fileOutput);
				break;
			case XHTML:
				pdfManagement.convertHtmlToPDF(dcemUploadFile.file, fileTemp);
				pdfManagement.mergePdf(pdfFile, fileTemp, null, fileOutput);
				break;
			case PDF:
				pdfManagement.mergePdf(pdfFile, dcemUploadFile.file, null, fileOutput);
				break;
			case JPEG:
			case PNG:
			case GIF:
				byte[] data = Files.readAllBytes(dcemUploadFile.file.toPath());
				pdfManagement.addImageToPdf(pdfFile, fileOutput, data, dcemUploadFile.fileName);
				break;
			default:
				String notMediaSupporte = resourceBundle.getString("email.noMediaSupportTemplate");
				String html = String.format(notMediaSupporte, dcemUploadFile.fileName, dcemUploadFile.dcemMediaType, dcemUploadFile.info);
				pdfManagement.convertHtmlStringToPDF(html, fileTemp);
				pdfManagement.mergePdf(pdfFile, fileTemp, null, fileOutput);
				break;
			}
			dcemUploadFile.file.delete();
			pdfFile.delete();
			pdfFile = fileOutput;
		}
		fileTemp.delete();
		File fileOutput = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, ".pdf");
		pdfManagement.removeEmptyPages(pdfFile, fileOutput);
		pdfFile.delete();
		return fileOutput;
	}

	private File addAttachmentPrefix(File pdfFile, DcemUploadFile dcemUploadFile, String emailPrefix) throws Exception {
		String lengthStr = DataUnit.getByteCountAsString(dcemUploadFile.file.length());
		String html = String.format(emailPrefix, dcemUploadFile.fileName, dcemUploadFile.dcemMediaType, lengthStr, dcemUploadFile.info);
		File fileHtml = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, ".pdf");
		pdfManagement.convertHtmlStringToPDF(html, fileHtml);
		File fileOutput = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, ".pdf");
		pdfManagement.mergePdf(pdfFile, fileHtml, null, fileOutput);
		fileHtml.delete();
		return fileOutput;
	}

	public File getDocumentContent(CloudSafeEntity cloudSafeEntity) throws Exception {
		InputStream inputStream = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, null, null, null);
		File originalFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, "");
		FileOutputStream fileOutputStream = new FileOutputStream(originalFile);
		KaraUtils.copyStream(inputStream, fileOutputStream);
		fileOutputStream.close();
		return originalFile;
	}

	public String generateOcr(CloudSafeEntity cloudSafeEntity, File file) throws Exception {
		DcemMediaType dcemMediaType = cloudSafeEntity.getDcemMediaType();
		File tempFile;
		String ocrText = null;
		switch (dcemMediaType) {
		case ODT:
			tempFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
			pdfManagement.convertOdtToPDF(file, tempFile);
			ocrText = pdfManagement.getOcrText(tempFile, DcemMediaType.PDF);
			break;
		case WORD:
			tempFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
			pdfManagement.convertWordToPDF(file, tempFile);
			ocrText = pdfManagement.getOcrText(tempFile, DcemMediaType.PDF);
			break;
		case XLSX:
			tempFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
			pdfManagement.convertExcelToPdf(file, tempFile);
			ocrText = FileUploadDetector.parseFileToString(file);
			break;
		case JPEG:
		case PNG:
		case GIF:
		case SVG:
			ocrText = pdfManagement.getOcrText(file, dcemMediaType);
			break;
		case PDF:
			ocrText = pdfManagement.getOcrText(file, dcemMediaType);
			break;
		case TEXT:
			ocrText = Files.readString(file.toPath(), StandardCharsets.UTF_8);
			break;
		case XHTML:
			File outputFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
			pdfManagement.convertHtmlToPDF(file, outputFile);
			ocrText = pdfManagement.getOcrText(outputFile, DcemMediaType.PDF);
			outputFile.delete();
			break;
		default:
			ocrText = null;
			break;
		}
		return removeDoubleLines(ocrText);
	}

	@DcemTransactional
	public CloudSafeEntity saveDocument(CloudSafeEntity cloudSafeEntity, byte[] thumbnail, File fileContent, String ocrText,
			List<CloudSafeTagEntity> toBeAddedTags, DcemUser modifiedBy, boolean overwrite) throws Exception {
		if (ocrText != null) {
			cloudSafeEntity.setTextExtract(ocrText.substring(0, Math.min(ocrText.length(), DmConstants.MAX_TEXT_EXTRACT)));
		}
		if (thumbnail != null) {
			if (cloudSafeEntity.getThumbnailEntity() == null) {
				CloudSafeThumbnailEntity thumbnailEntity = new CloudSafeThumbnailEntity(thumbnail);
				thumbnailEntity.setCloudSafeEntity(cloudSafeEntity);
				cloudSafeEntity.setThumbnailEntity(thumbnailEntity);
			} else {
				cloudSafeEntity.getThumbnailEntity().setThumbnail(thumbnail);
			}
		}
		if (toBeAddedTags != null && toBeAddedTags.isEmpty() == false) {
			cloudSafeTagLogic.addMultipleTags(toBeAddedTags);
			for (CloudSafeTagEntity cloudSafeTagEntity : toBeAddedTags) {
				cloudSafeEntity.getTags().add(cloudSafeTagEntity);
			}
		}
		// we do not save the ocr any more and it is now always overwrire true
		CloudSafeEntity cloudSafeEntityDb = cloudSafeLogic.addDocument(cloudSafeEntity, null, modifiedBy, fileContent, null, true);
		if (cloudSafeEntity.isFile() == true && ocrText != null) {
			try {
				solrLogic.indexDocument(cloudSafeEntityDb, ocrText);
			} catch (IOException e) {
				throw new DcemException(DcemErrorCodes.SOLR_NO_CONNECTION, cloudSafeEntity.getName(), e);
			} catch (Exception e) {
				throw new DcemException(DcemErrorCodes.SOLR_NOT_INDEX, cloudSafeEntity.getName(), e);
			}
		}
		workflowLogic.checkWorkflow(cloudSafeEntityDb, cloudSafeEntity.isNewEntity() ? WorkflowTrigger.Added : WorkflowTrigger.Modify);
		return cloudSafeEntityDb;
	}

	private String removeDoubleLines(String ocrText) {
		Scanner scanner = new Scanner(ocrText);
		StringBuilder sb = new StringBuilder();
		String line;
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();
			if (line.isBlank() == false) {
				sb.append(removeSpecialChars(line));
				sb.append("\n");
			}
		}
		scanner.close();
		return sb.toString();
	}

	private String removeSpecialChars(String text) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (ch == '[' || ch == ']' || ch == '{' || ch == '}') {
				continue;
			}
			sb.append(ch);
		}
		return sb.toString();
	}

	public byte[] getThumbnail(DcemMediaType dcemMediaType, File fileContent) throws Exception {
		byte[] thumbnail = null;
		switch (dcemMediaType) {
		case WORD:
		case XLSX:
		case PDF:
		case ODT:
			thumbnail = pdfManagement.createThumbnail(fileContent, DmConstants.THUMBNAIL_DIMENSION);
			break;
		case JPEG:
		case PNG:
		case GIF:
			byte[] data = Files.readAllBytes(fileContent.toPath());
			thumbnail = DcemUtils.resizeImage(data, DmConstants.THUMBNAIL_DIMENSION.height, DmConstants.THUMBNAIL_DIMENSION.width, 1024 * 64, false);
			break;
		case SVG:
			BufferedImage bufferedImage = ImageIO.read(fileContent);
			if (bufferedImage != null) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ImageIO.write(bufferedImage, "jpeg", os);
				return os.toByteArray();
			}
			thumbnail = Files.readAllBytes(fileContent.toPath());
			break;
		case TEXT:
		case XHTML:
			File outputFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
			pdfManagement.convertHtmlToPDF(fileContent, outputFile);
			thumbnail = pdfManagement.createThumbnail(outputFile, DmConstants.THUMBNAIL_DIMENSION);
			outputFile.delete();
			break;
		case MP4:
		case QT:
			try {
				File thumbnailFile = DmUtils.createMp4Thumbnail(fileContent, documentManagementModule.getMasterPreferences().getFfmpegPath());
				thumbnail = Files.readAllBytes(thumbnailFile.toPath());
				thumbnail = DcemUtils.resizeImage(thumbnail, DmConstants.THUMBNAIL_DIMENSION.height, DmConstants.THUMBNAIL_DIMENSION.width, 1024 * 64, false);
				thumbnailFile.delete();
			} catch (IOException e) {
				throw new DcemException(DcemErrorCodes.FFMPEG_NOT_INSTALLED, "", e);
			}
		default:
			break;
		}
		return thumbnail;
	}

	@DcemTransactional
	public CloudSafeEntity saveNewDocument(UploadDocument uploadDocument, DcemUser dcemUser, Map<String, CloudSafeEntity> folderCache) throws Exception {
		CloudSafeEntity cloudSafeEntity;
		if (uploadDocument.getRecoverFrom() != null) {
			cloudSafeEntity = uploadDocument.getRecoverFrom();
			cloudSafeEntity.setId(null);
		} else {
			cloudSafeEntity = createNewCloudSafeEntity(uploadDocument.getParentFolder(), dcemUser);
			cloudSafeEntity.setLastModified(LocalDateTime.now());
		}
		DcemMediaType dcemMediaType = uploadDocument.getDcemMediaType();
		cloudSafeEntity.setName(uploadDocument.getName());
		cloudSafeEntity.setDcemMediaType(dcemMediaType);
		byte[] thumbnail = null;
		String ocrText = null;
		File pdfFile = null;
		cloudSafeEntity.setLength(uploadDocument.getFile().length());
		switch (dcemMediaType) {
		case XHTML:
			pdfFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
			pdfManagement.convertHtmlToPDF(uploadDocument.getFile(), pdfFile);
			thumbnail = pdfManagement.createThumbnail(pdfFile, DmConstants.THUMBNAIL_DIMENSION);
			ocrText = pdfManagement.getOcrText(pdfFile, DcemMediaType.PDF);
			break;
		case TEXT:
			String text = DmUtils.convertTextToHtml(uploadDocument.getFile());
			File outputFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
			pdfManagement.convertHtmlStringToPDF(text, outputFile);
			thumbnail = pdfManagement.createThumbnail(outputFile, DmConstants.THUMBNAIL_DIMENSION);
			try {
				ocrText = Files.readString(uploadDocument.getFile().toPath(), StandardCharsets.UTF_8);
			} catch (Exception e) {
				ocrText = Files.readString(uploadDocument.getFile().toPath(), StandardCharsets.ISO_8859_1);
			}
			break;
		case ODT:
			pdfFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
			pdfManagement.convertOdtToPDF(uploadDocument.getFile(), pdfFile);
			ocrText = pdfManagement.getOcrText(pdfFile, DcemMediaType.PDF);
			thumbnail = pdfManagement.createThumbnail(pdfFile, DmConstants.THUMBNAIL_DIMENSION);
			break;
		case WORD:
			pdfFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
			pdfManagement.convertWordToPDF(uploadDocument.getFile(), pdfFile);
			ocrText = pdfManagement.getOcrText(pdfFile, DcemMediaType.PDF);
			thumbnail = pdfManagement.createThumbnail(pdfFile, DmConstants.THUMBNAIL_DIMENSION);
			break;
		case XLSX:
			pdfFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
			pdfManagement.convertExcelToPdf(uploadDocument.getFile(), pdfFile);
			ocrText = FileUploadDetector.parseFileToString(uploadDocument.getFile());
			break;
		case JPEG:
		case PNG:
		case GIF:
			byte[] data = Files.readAllBytes(uploadDocument.getFile().toPath());
			thumbnail = DcemUtils.resizeImage(data, DmConstants.THUMBNAIL_DIMENSION.height, DmConstants.THUMBNAIL_DIMENSION.width, 1024 * 64, false);
			ocrText = pdfManagement.getOcrText(uploadDocument.getFile(), dcemMediaType);
			break;
		case SVG:
			ocrText = pdfManagement.getOcrText(uploadDocument.getFile(), dcemMediaType);
			BufferedImage bufferedImage = ImageIO.read(uploadDocument.getFile());
			if (bufferedImage != null) {
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ImageIO.write(bufferedImage, "jpeg", os);
				thumbnail = os.toByteArray();
			} else {
				thumbnail = Files.readAllBytes(uploadDocument.getFile().toPath());
			}
			break;
		case PDF:
			ocrText = pdfManagement.getOcrText(uploadDocument.getFile(), DcemMediaType.PDF);
			thumbnail = pdfManagement.createThumbnail(uploadDocument.getFile(), DmConstants.THUMBNAIL_DIMENSION);
			break;
		default:
			break;
		}
		try {
			if (uploadDocument.getWebPath() != null && uploadDocument.getWebPath().contains(CloudSafeLogic.FOLDER_SEPERATOR)) {
				CloudSafeEntity parent = cloudSafeLogic.makeDirectories(uploadDocument.getParentFolder(), uploadDocument.getWebPath(), dcemUser, folderCache);
				cloudSafeEntity.setParent(parent);
				uploadDocument.setParentFolder(parent);
			}
			saveDocument(cloudSafeEntity, thumbnail, uploadDocument.getFile(), ocrText, (List<CloudSafeTagEntity>) null, dcemUser,
					uploadDocument.isOverwrite());
			return cloudSafeEntity;
		} catch (Exception e) {
			throw e;
		} finally {
			if (pdfFile != null) {
				pdfFile.delete();
			}
		}
	}

	public void convertDocumentToPdfStream(String documentId, OutputStream outputStream) throws Exception {
		CloudSafeEntity cloudSafeEntity = cloudSafeLogic.getCloudSafe(Integer.getInteger(documentId));
		InputStream inputStream = cloudSafeLogic.getCloudSafeContentAsStream(cloudSafeEntity, null, null, null);
		switch (cloudSafeEntity.getDcemMediaType()) {
		case ODT:
			// contentFile = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, DmConstants.PDF_EXTENSION);
			pdfManagement.streamConvertOdtToPDF(inputStream, outputStream);
			break;
		case WORD:
			pdfManagement.streamConvertWordToPDF(inputStream, outputStream);
			break;
		case XLSX:
			pdfManagement.streamConvertExcelToPdf(inputStream, outputStream);
			break;
		case JPEG:
		case PNG:
		case GIF:
		case SVG:
		case PDF:
			KaraUtils.copyStream(inputStream, outputStream);
			break;
		case MAIL:
		//TODO 	List<DcemUploadFile> listFiles = MailUtils.processReceivedMail(originalFile, resourceBundle);
			// TODO contentFile = emailToPDF(resourceBundle, listFiles);
			break;
		default:
			KaraUtils.copyStream(inputStream, outputStream);
			break;
		}
	}

	static private CloudSafeEntity createNewCloudSafeEntity(CloudSafeEntity folder, DcemUser dcemUser) throws Exception {
		CloudSafeEntity cloudSafeEntity = new CloudSafeEntity();
		cloudSafeEntity.setParent(folder);
		cloudSafeEntity.setOwner(CloudSafeOwner.USER);
		cloudSafeEntity.setUser(dcemUser);
		cloudSafeEntity.setOptions(CloudSafeOptions.ENC.name());
		return cloudSafeEntity;
	}

}
