package com.doubleclue.dcem.dm.mail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.CloudSafeThumbnailEntity;
import com.doubleclue.dcem.core.as.DcemUploadFile;
import com.doubleclue.dcem.core.utils.mail.MailUtils;
import com.doubleclue.dcem.core.utils.typedetector.DcemMediaType;
import com.doubleclue.dcem.dm.logic.DmConstants;
import com.doubleclue.dcem.dm.logic.DmUtils;
import com.doubleclue.dcem.dm.logic.DocumentLogic;
import com.doubleclue.dcem.dm.logic.DocumentManagementModule;
import com.doubleclue.dcem.dm.logic.PdfManagement;

public class MailToPdf {

	protected static final Logger logger = LogManager.getLogger(MailToPdf.class);
	
	

	public static void main(String[] args) {

		try {
			mailToPdf();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);

	}

	private static void mailToPdf() throws Exception {
		PdfManagement pdfManagement = new PdfManagement();
		StringBuilder sb = new StringBuilder();
		List<DcemUploadFile> listFiles = MailUtils.processReceivedMail(new File ("C:\\temp\\message.eml"), ResourceBundle.getBundle(DocumentManagementModule.RESOURCE_NAME));
		File pdfFile = new File("C:\\temp\\mail\\Body.pdf");
		pdfManagement.createEmptyPdfFile(pdfFile);
		DcemUploadFile dcemUploadFileBody = listFiles.getFirst();
	//	CloudSafeEntity cloudSafeEntity = createNewCloudSafeEntity(cloudSafeLogic.getCloudSafeRoot(), dcemUser);
		pdfManagement.convertHtmlToPDF(dcemUploadFileBody.file, pdfFile);
		File pdfFileClean = new File("C:\\temp\\mail\\BodyClean.pdf");;
		List<Integer> blankPages =  pdfManagement.removeEmptyPages(pdfFile, pdfFileClean);
		System.out.println("MailToPdf.mailToPdf() BlankPages removed: " + blankPages);
	//	pdfFile.delete();
		pdfFile = pdfFileClean;
	//	cloudSafeEntity.setDcemMediaType(DcemMediaType.PDF);
 		String name = "emlmessage";
		CloudSafeEntity cloudSafeEntityExists = null;
//		if (dcemUser != null) {
//			cloudSafeEntity.setOwner(CloudSafeOwner.USER);
//			cloudSafeEntity.setUser(dcemUser);
//			cloudSafeEntityExists = cloudSafeLogic.getCloudSafeUserSingleResult(name, null, false, dcemUser.getId());
//		} else {
//			cloudSafeEntity.setOwner(CloudSafeOwner.GROUP);
//			cloudSafeEntity.setGroup(dcemGroup);
//			cloudSafeEntityExists = cloudSafeLogic.getCloudSafeGroupSingleResult(name, null, dcemGroup.getId());
//		}
//		if (cloudSafeEntityExists != null) {
//			name = name + LocalDateTime.now().format(dateTimeFormatterDoc);
//		}
		CloudSafeEntity cloudSafeEntity = new CloudSafeEntity();
		cloudSafeEntity.setName(name);
		cloudSafeEntity.setInfo(dcemUploadFileBody.info);
		byte[] thumbnail = pdfManagement.createThumbnail(pdfFile, DmConstants.THUMBNAIL_DIMENSION);
		CloudSafeThumbnailEntity thumbnailEntity = new CloudSafeThumbnailEntity(thumbnail);
		thumbnailEntity.setCloudSafeEntity(cloudSafeEntity);
		cloudSafeEntity.setThumbnailEntity(thumbnailEntity);
		for (int i = 1; i < listFiles.size(); i++) {
			DcemUploadFile dcemUploadFile = listFiles.get(i);
			File fileOutput = new File("C:\\temp\\mail\\Output.pdf");
			switch (dcemUploadFile.dcemMediaType) {
			case WORD:
				File file = new File("C:\\temp\\mail\\word.pdf");
				
				pdfManagement.convertWordToPDF(dcemUploadFile.file, file);
				pdfManagement.mergePdf(pdfFile, file, null, fileOutput);
				break;
			case EXE_MS:
				file = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, "");
				pdfManagement.convertExcelToPdf(dcemUploadFile.file, file);
				pdfManagement.mergePdf(pdfFile, dcemUploadFile.file, null, fileOutput);
				file.delete();
				break;
			case TEXT:
				String text = DmUtils.convertTextToHtml(dcemUploadFile.file);
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
				pdfManagement.mergePdf(pdfFile, (File)null, byteArrayInputStream, fileOutput);
				break;
			case XHTML:
				file = File.createTempFile(DmConstants.DOUBLE_CLUE_DM, "");
				pdfManagement.convertHtmlToPDF(dcemUploadFile.file, file);
				pdfManagement.mergePdf(pdfFile, file, null, fileOutput);
				file.delete();
				break;
			case PDF:
				pdfManagement.mergePdf(pdfFile, dcemUploadFile.file, null, fileOutput);
				break;
			default:
				sb.append("File ignored: " + dcemUploadFile.fileName);
				sb.append("\n");
				break;
			}
			dcemUploadFile.file.delete();
		}
	}
	
	

}
