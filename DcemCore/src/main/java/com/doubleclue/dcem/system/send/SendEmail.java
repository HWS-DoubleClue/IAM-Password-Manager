package com.doubleclue.dcem.system.send;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.AuthenticationFailedException;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.dcem.system.logic.SystemPreferences;
import com.sun.mail.smtp.SMTPSendFailedException;
import com.sun.mail.smtp.SMTPTransport;

public class SendEmail {

	private static Logger logger = LogManager.getLogger(SendEmail.class);

	static Properties prop = null;
	static SmtpAuthenticator auth;
	static String fromEmail;
	static String fromPerson;

	static public void setProperties(SystemPreferences systemPreferences) {

		if (systemPreferences.geteMailHostPort() == 0 || systemPreferences.geteMailHostAddress() == null || systemPreferences.geteMailHostAddress().isEmpty()) {
			prop = null;
			return;
		}
		prop = new Properties();

		prop.setProperty("mail.smtp.host", systemPreferences.geteMailHostAddress());
		prop.setProperty("mail.smtp.socketFactory.fallback", "false");
		prop.put("mail.smtp.port", Integer.toString(systemPreferences.geteMailHostPort()));
		prop.put("mail.transport.protocol", "smtp");
		prop.put("mail.smtp.ssl.trust", "*");
		// prop.put("mail.debug", "true");
		prop.put("mail.smtp.starttls.enable", "true");
		prop.put("mail.smtp.ssl.enable", "false");
		prop.put("mail.smtp.timeout", "5000");
		prop.put("mail.smtp.connectiontimeout", "5000");
		prop.put("mail.smtp.ssl.protocols", systemPreferences.geteMailProtocol());
		if (systemPreferences.geteMailPassword() == null || systemPreferences.geteMailPassword().isBlank()) {
			auth = null;
			prop.setProperty("mail.smtp.auth", "false");
		} else {
			prop.setProperty("mail.smtp.auth", "true");
			auth = new SmtpAuthenticator(systemPreferences.geteMailUser(), systemPreferences.geteMailPassword());
		}
		fromEmail = systemPreferences.geteMailFromEmail();
		fromPerson = systemPreferences.geteMailFromPerson();
	}

	private static void reloadProperties() {
		try {
			WeldContextUtils.activateRequestContext();
			SystemModule systemModule = CdiUtils.getReference(SystemModule.class);
			SystemPreferences preferences = systemModule.getPreferences();
			setProperties(preferences);
		} catch (Exception e) {
			logger.error("Could not load eMail configs", e);
		}
	}

	public static void sendMessage(String toReceiver, String body, String subject) throws DcemException {
		sendMessage(toReceiver, body, subject, new ArrayList<EmailAttachment>(0));
	}

	public static void sendMessage(String toReceiver, String body, String subject, EmailAttachment attachment) throws DcemException {
		List<String> recipients = new ArrayList<String>();
		if (toReceiver != null) {
			recipients.add(toReceiver);
		}
		sendMessage(recipients, body, subject, attachment);
	}

	public static void sendMessage(List<String> recipients, String body, String subject, EmailAttachment attachment) throws DcemException {
		List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();
		if (attachment != null) {
			attachments.add(attachment);
		}
		sendMessage(recipients, body, subject, attachments);
	}

	public static void sendMessage(String toReceiver, String body, String subject, List<EmailAttachment> attachments) throws DcemException {
		List<String> recipients = new ArrayList<String>();
		if (toReceiver != null) {
			recipients.add(toReceiver);
		}
		sendMessage(recipients, body, subject, attachments);
	}

	public static void sendMessage(List<String> toReceiver, String body, String subject, List<EmailAttachment> attachments) throws DcemException {
		if (toReceiver == null || toReceiver.isEmpty()) {
			return;
		}
		if (prop == null) {
			reloadProperties();
		}
		if (prop == null) {
			DcemErrorCodes errorCode = DcemErrorCodes.EMAIL_INVALID_CONFIGURATION;
			throw new DcemException(errorCode, "Email configuration is not set up in preferences section.");
		}
		Session session;
		session = Session.getInstance(prop, auth);
		// session.setDebug(true);

		SMTPTransport tx = null;
		try {
			tx = createSMTPTransport(session);
			MimeMessage msg = createMimeMessage(session, toReceiver, body, subject, attachments);
			tx.sendMessage(msg, msg.getAllRecipients());
		} catch (SMTPSendFailedException exp) {
			if (exp.getMessage().indexOf("421 4.4.2") != -1) {
				throw new DcemException(DcemErrorCodes.EMAIL_SEND_MSG_LIMIT, null, exp);
			}
			throw new DcemException(DcemErrorCodes.EMAIL_SEND_MSG_FAILED, exp.getMessage(), exp);
		} catch (DcemException dcemExp) {
			throw dcemExp;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.EMAIL_SEND_MSG_FAILED, e.getMessage(), e);
		} finally {
			try {
				if (tx != null) {
					tx.close();
				}
			} catch (MessagingException e) {
				logger.warn("Could not close SMTP connection", e);
			}
		}
	}

	private static SMTPTransport createSMTPTransport(Session session) throws DcemException {
		boolean isMasterTenant = TenantIdResolver.isCurrentTenantMaster();
		SMTPTransport tx = null;
		try {
			tx = (SMTPTransport) session.getTransport("smtp");
			tx.connect();
		} catch (AuthenticationFailedException e) {
			DcemErrorCodes errorCode = DcemErrorCodes.EMAIL_AUTHENTICATION_FAILED;
			if (isMasterTenant) {
				createWelcomeViewAlert(errorCode);
			}
			throw new DcemException(errorCode, e.getMessage(), e);
		} catch (Exception e) {
			DcemErrorCodes errorCode = DcemErrorCodes.EMAIL_CONNECTION_FAILED;
			if (isMasterTenant) {
				createWelcomeViewAlert(errorCode);
			}
			throw new DcemException(errorCode, e.getMessage(), e);
		}
		return tx;
	}

	private static void createWelcomeViewAlert(DcemErrorCodes errorCode) {
		WeldRequestContext requestContext = null;
		try {
			requestContext = WeldContextUtils.activateRequestContext();
			DcemReportingLogic reportingLogic = CdiUtils.getReference(DcemReportingLogic.class);
			reportingLogic.addWelcomeViewAlert(SystemModule.MODULE_ID, errorCode, null, AlertSeverity.ERROR, true);
		} catch (Exception ex) {
		} finally {
			if (errorCode.equals(DcemErrorCodes.EMAIL_AUTHENTICATION_FAILED)) {
				WeldContextUtils.deactivateRequestContext(requestContext);
			}
		}
	}

	private static MimeMessage createMimeMessage(Session session, List<String> toReceiver, String body, String subject, List<EmailAttachment> attachments)
			throws DcemException {
		if (attachments == null) {
			attachments = new ArrayList<EmailAttachment>();
		}
		MimeMessage msg = new MimeMessage(session);
		try {
			if (fromPerson != null && fromPerson.length() > 0) {
				msg.setFrom(new InternetAddress(fromEmail, fromPerson));
			} else {
				msg.setFrom(new InternetAddress(fromEmail));
			}
			for (String reciever : toReceiver) {
				msg.addRecipient(Message.RecipientType.TO, new InternetAddress(reciever));
			}
			msg.setSubject(subject);

			Multipart multipart = new MimeMultipart("related");

			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(body, "text/html; charset=utf-8");
			multipart.addBodyPart(messageBodyPart);

			for (EmailAttachment attachment : attachments) {
				messageBodyPart = new MimeBodyPart();
				ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(attachment.getAttachment(), attachment.getMimeType());
				messageBodyPart.setDataHandler(new DataHandler(byteArrayDataSource));
				messageBodyPart.setFileName(attachment.getFileName());
				messageBodyPart.setHeader("Content-ID", String.format("<%s>", attachment.getContentId()));
				if (attachment.isDispositionInline()) {
					messageBodyPart.setDisposition(MimeBodyPart.INLINE);
				} else {
					messageBodyPart.setDisposition(MimeBodyPart.ATTACHMENT);
				}
				multipart.addBodyPart(messageBodyPart);
			}
			msg.setContent(multipart);
			msg.saveChanges();
			return msg;
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.EMAIL_MESSAGE_FAILED, e.getMessage(), e);
		}
	}

	public static void reloadWelcomeViewAlerts() {
		try {
			sendMessage(null, null, null);
		} catch (DcemException e) {
			// do nothing
		}
	}
}

class SmtpAuthenticator extends javax.mail.Authenticator {
	String user;
	String password;

	public SmtpAuthenticator(String user, String password) {
		super();
		this.user = user;
		this.password = password;
	}

	public PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(user, password);
	}
}
