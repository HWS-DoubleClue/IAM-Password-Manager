package com.doubleclue.dcem.core.tasks;

import java.io.File;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.logic.UrlTokenLogic;
import com.doubleclue.dcem.core.logic.UrlTokenType;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.weld.CdiUtils;

import jakarta.mail.Address;

public class ProcessReceiveMailTask extends CoreTask {

	private static Logger logger = LogManager.getLogger(ProcessReceiveMailTask.class);

	TenantEntity tenantEntity;
	DcemModule dcemModule;
	String identifier;
	String token;
	String subjectName;
	File emlFile;
	Address addressFrom;
	/*
	 * 
	 *  Every E-Mial must have an email token in subject. 
	 *  If no token is found the email will be deleted
	 *  The token is build as follows:
	 *  
	 *  {{			starting of token
	 *  XX			Hexa byte. This is the checksum of the token rest string without the end delimeter
	 *  teneantId	tenant db id (zero for master tenant)
	 * 	.			seperator
	 *  moduleId	
	 *  .			seperator
	 *  identifierId	this identifier depend on the module
	 *  .			seperator	
	 *  token		this is usually a GUID
	 *  }}			end of Token
	 * 
	 * 
	 * 
	 */

	public ProcessReceiveMailTask(TenantEntity tenantEntity, DcemModule dcemModule, String subjectName, String identifier, String token, File emlFile, Address address) {
		super (ProcessReceiveMailTask.class.getSimpleName(), tenantEntity);
		this.tenantEntity = tenantEntity;
		this.dcemModule = dcemModule;
		this.identifier = identifier;
		this.token = token;
		this.emlFile = emlFile;
		this.subjectName = subjectName;
		this.addressFrom = address;
	}

	

	@Override
	public void runTask() {
		
		UrlTokenLogic urlTokenLogic = CdiUtils.getReference(UrlTokenLogic.class);
		try {
			urlTokenLogic.verifyUrlToken(token, UrlTokenType.EmailToken.name());
		} catch (Exception e) {
			DcemReportingLogic dcemReportingLogic = CdiUtils.getReference(DcemReportingLogic.class);
			DcemReporting asReporting = new DcemReporting(dcemModule.getName(), ReportAction.Invalid_Email_Received, (DcemUser)null, e.getMessage(), null,
					"From: " + addressFrom, AlertSeverity.FAILURE);
			dcemReportingLogic.addReporting(asReporting);
			logger.error("Invalid EMail Token from " + addressFrom + ", cause: " + e.toString());
			emlFile.delete();
			return;
		}
		try {
			String report = dcemModule.receiveMail (subjectName, identifier, emlFile);
		} catch (Exception e) {
			logger.error("Invalid EMail from " + addressFrom + ", cause: " + e.toString(), e);
			DcemReportingLogic dcemReportingLogic = CdiUtils.getReference(DcemReportingLogic.class);
			DcemReporting asReporting = new DcemReporting(dcemModule.getName(), ReportAction.Invalid_Email_Received, (DcemUser)null, e.toString(), null,
					"From: " + addressFrom, AlertSeverity.FAILURE);
			dcemReportingLogic.addReporting(asReporting);
		} 
		emlFile.delete();
	}
}
