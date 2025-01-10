package com.doubleclue.dcem.core.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.utils.DcemTrustManager;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.dcem.system.logic.SystemPreferences;
import com.doubleclue.utils.StringUtils;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.FlagTerm;

public class SystemReceiveMailTask extends CoreTask {

	private static Logger logger = LogManager.getLogger(SystemReceiveMailTask.class);

	private Date emailsStartDate;
	private Date emailsEndDate;
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

	public SystemReceiveMailTask() {
		super(SystemReceiveMailTask.class.getSimpleName());
		emailsStartDate = null;
		emailsEndDate = null;
	}

	public SystemReceiveMailTask(Date startDate, Date endDate) {
		super(SystemReceiveMailTask.class.getSimpleName());
		emailsStartDate = startDate;
		emailsEndDate = endDate;
	}

	@Override
	public void runTask() {
		if (DcemCluster.getDcemCluster().isClusterMaster()) {
			SystemModule systemModule = CdiUtils.getReference(SystemModule.class);
			SystemPreferences prefs = systemModule.getPreferences();
			SSLContext ctx;
			SocketFactory socketFactory;
			try {
				ctx = SSLContext.getInstance("TLS");
				if (prefs.isIgnoreCertificates() == true) {
					DcemTrustManager trustManager = new DcemTrustManager(true);
					trustManager.setSaveServerChainCertificates(false);
					ctx.init(null, new TrustManager[] { trustManager }, null);
					socketFactory = ctx.getSocketFactory();
				} else {
					socketFactory = SSLSocketFactory.getDefault();
				}
			} catch (Exception e) {
				logger.error("Can't get the SSL Socket Factory: ", e);
				return;
			}
			Thread.currentThread().setName(this.getClass().getSimpleName());
			Store emailStore = null;
			Folder inbox = null;
			try {
				Properties properties = System.getProperties();
				properties.setProperty("mail.store.protocol", "imap");
				properties.setProperty("mail.imap.ssl.enable", "true");
				properties.put("mail.imap.ssl.socketFactory", socketFactory);
				Session emailSession = Session.getInstance(properties, null);

				// Session emailSession = Session.getInstance(properties, new javax.mail.Authenticator() {
				// protected PasswordAuthentication getPasswordAuthentication() {
				// return new PasswordAuthentication(prefs.geteMailReceiveAccount(), prefs.geteMailReceivePassword());
				// }
				// });
				emailStore = emailSession.getStore("imap");
				emailStore.connect(prefs.geteMailReceiveHostAddress(), prefs.geteMailReceivePort(), prefs.geteMailReceiveAccount(),
						prefs.geteMailReceivePassword());
				inbox = emailStore.getFolder("inbox");

				// Fetch unseen messages from inbox folder
				inbox.open(Folder.READ_WRITE);
				Message[] messages;
	//			if (emailsStartDate == null && emailsEndDate == null) {
					messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
//				} else {
//					messages = inbox.search(
//							new AndTerm(new ReceivedDateTerm(ComparisonTerm.GT, emailsStartDate), new ReceivedDateTerm(ComparisonTerm.LT, emailsEndDate)));
//				}
				for (Message message : messages) {
					try {
						String subject = message.getSubject();
						// check token
						int ind = subject.indexOf("{{");
						if (ind == -1) {
							throw new Exception("without start token");
						}
						int endInd = subject.indexOf("}}", ind);
						if (endInd == -1) {
							throw new Exception("without token end");
						}
						String token = subject.substring(ind + 2, endInd);
						subject = subject.substring(0, ind) + subject.substring(endInd+2) ;

						// calculate the checksum
						token.charAt(0);
						byte[] hasharray = StringUtils.hexStringToBinary(token.substring(0, 2));
						byte calculatedHash = (byte) (token.substring(2).hashCode() & 0xFF);
						if (calculatedHash != hasharray[0]) {
							throw new Exception("wrong hash");
						}
						// tenant Id
						token = token.substring(2);
						String[] components = token.split("\\.");
						if (components.length < 4) {
							throw new Exception("component missing");
						}
						// check tenant Id
						DcemApplicationBean dcemApplication = CdiUtils.getReference(DcemApplicationBean.class);
						TenantEntity tenantEntity = dcemApplication.getTenantById(Integer.parseInt(components[0]));
						if (tenantEntity == null) {
							throw new Exception("wrong tenant id");
						}
						DcemModule dcemModule = dcemApplication.getModule(components[1]);
						if (dcemModule == null) {
							throw new Exception("wrong module");
						}
						File tempFile = File.createTempFile("dcem-", ".eml");
						message.writeTo(new FileOutputStream(tempFile));
						TaskExecutor taskExecutor = CdiUtils.getReference(TaskExecutor.class);
						taskExecutor.execute(new ProcessReceiveMailTask(tenantEntity, dcemModule, subject, components[2], components[3], tempFile));
	 					message.setFlag(Flags.Flag.SEEN, true);
					} catch (Exception e) {
						logger.error("Received Mails: from: " + message.getFrom()[0] + " Cause:" + e.getMessage());
						message.setFlag(Flags.Flag.DELETED, true);
						continue;
					}
				}
				// logger.info("Receiving E-Mails from: " + prefs.getEmailReceiveAccountName() + " Messages: " + messages.length);
				inbox.close(true); // with expunge
				emailStore.close();
			} catch (Exception e) {
				logger.error("RECEVING MAILS ERROR: ", e);
			} finally {
				if (inbox != null && inbox.isOpen()) {
					try {
						inbox.close(false);
					} catch (Exception e) {
					}
				}
				if (emailStore != null && emailStore.isConnected()) {
					try {
						emailStore.close();
					} catch (Exception e) {
					}
				}
			}
		}
	}
}
