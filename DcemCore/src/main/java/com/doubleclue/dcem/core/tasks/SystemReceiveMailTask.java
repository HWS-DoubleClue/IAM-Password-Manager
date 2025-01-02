package com.doubleclue.dcem.core.tasks;

import java.util.Date;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
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

import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.utils.DcemTrustManager;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.dcem.system.logic.SystemPreferences;
import com.doubleclue.utils.StringUtils;

public class SystemReceiveMailTask extends CoreTask {

	private static Logger logger = LogManager.getLogger(SystemReceiveMailTask.class);

	private boolean setMarkSeen;
	private Date emailsStartDate;
	private Date emailsEndDate;

	public SystemReceiveMailTask() {
		super(SystemReceiveMailTask.class.getSimpleName());
		setMarkSeen = false;
		emailsStartDate = null;
		emailsEndDate = null;
	}

	public SystemReceiveMailTask(Date startDate, Date endDate, boolean markSeen) {
		super(SystemReceiveMailTask.class.getSimpleName());
		setMarkSeen = markSeen;
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
				// properties.put("mail.imap.ssl.socketFactory", socketFactory);
				// properties.setProperty("mail.imap.socketFactory.fallback", "false");
				// Session emailSession = Session.getInstance(properties, null);

				Session emailSession = Session.getInstance(properties, new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication("galea1961", "");
					}
				});
				emailStore = emailSession.getStore("imap");

				// emailStore.connect(prefs.geteMailReceiveHostAddress(), prefs.geteMailReceivePort(), prefs.geteMailReceiveAccount(),
				// prefs.geteMailReceivePassword());
				emailStore.connect();
		//		emailStore.connect("imap.gmail.com", "galea1961", "googlek4s9Tewe");
				

				// inbox = emailStore.getDefaultFolder();
				inbox = emailStore.getFolder("inbox");

				// Fetch unseen messages from inbox folder
				inbox.open(Folder.READ_ONLY);
				Message[] messages;
				if (emailsStartDate == null && emailsEndDate == null) {
					messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
				} else {
					messages = inbox.search(
							new AndTerm(new ReceivedDateTerm(ComparisonTerm.GT, emailsStartDate), new ReceivedDateTerm(ComparisonTerm.LT, emailsEndDate)));
				}
				for (Message message : messages) {
					String subject = message.getSubject();
					// check token
					int ind = subject.indexOf("{{");
					if (ind == -1) {
						logger.error("message received without token start. From: " + message.getFrom()[0]);

						continue;  // ignore
					}
					int endInd = subject.indexOf("}}", ind);
					if (endInd == -1) {
						logger.error("message received without token end. From: " + message.getFrom()[0]);
						continue;  // ignore e-Mail
					}
					String token = subject.substring(ind + 2, endInd);
					System.out.println(token);
					subject = subject.substring(0, ind);
					
					// calculate the checksum	
					token.charAt(0);
					byte [] hasharray = StringUtils.hexStringToBinary(token.substring(0, 2));
					byte calculatedHash = (byte) (token.substring(2).hashCode() & 0xFF);
					if (calculatedHash != hasharray[0]) {
						logger.error("message received with wrong hash from: " + message.getFrom()[0]);
						continue;
					}
					// Check Tenant Id:
					
					

				}
				// logger.info("Receiving E-Mails from: " + prefs.getEmailReceiveAccountName() + " Messages: " + messages.length);

				if (setMarkSeen == true) {
					for (Message message : messages) {
						message.setFlag(Flags.Flag.SEEN, true);
					}
				}
				inbox.close(false);
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
