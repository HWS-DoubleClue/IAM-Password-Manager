package com.doubleclue.dcem.core.tasks;

import java.util.Date;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.ReceivedDateTerm;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.dcem.system.logic.SystemPreferences;

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
			Thread.currentThread().setName(this.getClass().getSimpleName());
			Store emailStore = null;
			Folder inbox = null;
			SystemModule systemModule = CdiUtils.getReference(SystemModule.class);
			try {
				Properties properties = new Properties();
				Session emailSession = Session.getDefaultInstance(properties);
				emailStore = emailSession.getStore("imaps");
				SystemPreferences prefs = systemModule.getPreferences();
				emailStore.connect(prefs.geteMailReceiveHostAddress(), prefs.geteMailReceivePort(), prefs.geteMailReceiveAccount(),
						prefs.geteMailReceivePassword());
				inbox = emailStore.getFolder("INBOX");
				// Fetch unseen messages from inbox folder
				inbox.open(Folder.READ_WRITE);
				Message[] messages;
				if (emailsStartDate == null && emailsEndDate == null) {
					messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
				} else {
					messages = inbox.search(
							new AndTerm(new ReceivedDateTerm(ComparisonTerm.GT, emailsStartDate), new ReceivedDateTerm(ComparisonTerm.LT, emailsEndDate)));
				}
	//			logger.info("Receiving E-Mails from: " + prefs.getEmailReceiveAccountName() + " Messages: " + messages.length);
	//			ShiftsAbsenceLogic shiftsAbsenceLogic = CdiUtils.getReference(ShiftsAbsenceLogic.class);
	//			shiftsAbsenceLogic.parseAbsenceEmails(messages);
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
