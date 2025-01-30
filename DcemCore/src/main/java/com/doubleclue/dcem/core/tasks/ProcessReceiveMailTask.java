package com.doubleclue.dcem.core.tasks;

import java.io.File;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.as.DcemUploadFile;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.logic.UrlTokenLogic;
import com.doubleclue.dcem.core.logic.UrlTokenType;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.utils.DcemTrustManager;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.dcem.system.logic.SystemPreferences;
import com.doubleclue.utils.StringUtils;

public class ProcessReceiveMailTask extends CoreTask {

	private static Logger logger = LogManager.getLogger(ProcessReceiveMailTask.class);

	TenantEntity tenantEntity;
	DcemModule dcemModule;
	String identifier;
	String token;
	String subjectName;
	File emlFile;
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

	public ProcessReceiveMailTask(TenantEntity tenantEntity, DcemModule dcemModule, String subjectName, String identifier, String token, File emlFile) {
		super (ProcessReceiveMailTask.class.getSimpleName(), tenantEntity);
		this.tenantEntity = tenantEntity;
		this.dcemModule = dcemModule;
		this.identifier = identifier;
		this.token = token;
		this.emlFile = emlFile;
		this.subjectName = subjectName;
	}

	@Override
	public void runTask() {
		UrlTokenLogic urlTokenLogic = CdiUtils.getReference(UrlTokenLogic.class);
		String from = "?";
		try {
		//	from = message.getFrom()[0].toString();
			urlTokenLogic.verifyUrlToken(token, UrlTokenType.EmailToken.name());
		} catch (Exception e) {
			logger.error("Invalid EMail Token from " + from + ", cause: " + e.toString());
			return;
		}
		try {
			String report = dcemModule.receiveMail (subjectName, identifier, emlFile);
			// TODO send E-Mail with error
		} catch (Exception e) {
			logger.error("Invalid EMail from " + from + ", cause" + e.getMessage(), e);
			// TODO send E-Mail with error
		}
		emlFile.delete();
	}
}
