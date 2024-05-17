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

import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.dcem.system.logic.SystemPreferences;
import com.sun.mail.smtp.SMTPSendFailedException;
import com.sun.mail.smtp.SMTPTransport;

public class SendEmail {

	static Properties prop = null;
	static SmtpAuthenticator auth;
	static String fromEmail;
	static String fromPerson;

	static public void setProperties(SystemPreferences systemPreferences) {

		if (systemPreferences.geteMailHostPort() == 0 || systemPreferences.geteMailHostAddress() == null
				|| systemPreferences.geteMailHostAddress().isEmpty()) {
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

	public static void sendMessage(String toReceiver, String body, String subject) throws DcemException {
		sendMessage(toReceiver, body, subject, null);
	}

	/**
	 * @param toReceiver
	 * @param body
	 * @param subject
	 * @throws Exception
	 */
	public static void sendMessage(String toReceiver, String body, String subject, byte[] attachment)
			throws DcemException {
		List<String> recipients = new ArrayList<String>();
		if (toReceiver != null) {
			recipients.add(toReceiver);
		}
		sendMessage(recipients, body, subject, attachment);
	}

	/**
	 * @param toReceiver
	 * @param body
	 * @param subject
	 * @throws Exception
	 */
	public static void sendMessage(List<String> toReceiver, String body, String subject, byte[] attachment) throws DcemException {

		Session session;
		boolean isMasterTenant = TenantIdResolver.isCurrentTenantMaster();
		if (prop == null) {
			try {
				WeldContextUtils.activateRequestContext();
				SystemModule systemModule = CdiUtils.getReference(SystemModule.class);
				SystemPreferences preferences = systemModule.getPreferences();
				setProperties(preferences);
			} catch (Exception e) {
			}
		}
		if (prop == null) {
			DcemErrorCodes errorCode = DcemErrorCodes.EMAIL_INVALID_CONFIGURATION;
			throw new DcemException(errorCode, "Email configuration is not set up in preferences section.");
		}
		session = Session.getInstance(prop, auth);
		SMTPTransport tx = null;
		try {
			tx = (SMTPTransport) session.getTransport("smtp");
			tx.connect();
		} catch (AuthenticationFailedException e) {
			DcemErrorCodes errorCode = DcemErrorCodes.EMAIL_AUTHENTICATION_FAILED;
			if (isMasterTenant) {
				WeldRequestContext requestContext = null;
				try {
					requestContext = WeldContextUtils.activateRequestContext();
					DcemReportingLogic reportingLogic = CdiUtils.getReference(DcemReportingLogic.class);
					reportingLogic.addWelcomeViewAlert(SystemModule.MODULE_ID, errorCode, null, AlertSeverity.ERROR, true);
				} catch (Exception ex) {
				} finally {
					WeldContextUtils.deactivateRequestContext(requestContext);
				}
			}
			throw new DcemException(errorCode, e.getMessage(), e);
		} catch (Exception e) {
			DcemErrorCodes errorCode = DcemErrorCodes.EMAIL_CONNECTION_FAILED;
			if (isMasterTenant) {
				try {
					WeldContextUtils.activateRequestContext();
					DcemReportingLogic reportingLogic = CdiUtils.getReference(DcemReportingLogic.class);
					reportingLogic.addWelcomeViewAlert(SystemModule.MODULE_ID, errorCode, null, AlertSeverity.ERROR, true);
				} catch (Exception ex) {
				}
			}
			throw new DcemException(errorCode, e.getMessage(), e);
		}

		// session.setDebug(true);
		if (toReceiver.size() > 0) {
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
				msg.setHeader("Content-Type", "text/html;  charset=utf-8");
				if (attachment == null) {
					msg.setContent(body, "text/html;  charset=utf-8");
				} else {
					BodyPart messageBodyPart = new MimeBodyPart();
					messageBodyPart.setContent(body, "text/html;  charset=utf-8");

					Multipart multipart = new MimeMultipart();
					multipart.addBodyPart(messageBodyPart);

					messageBodyPart = new MimeBodyPart();
					messageBodyPart.setFileName("activation.png");
					messageBodyPart.setHeader("Content-ID", "<activation>");
					messageBodyPart.setDisposition(MimeBodyPart.INLINE);

					ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(attachment, "image/png");
					messageBodyPart.setDataHandler(new DataHandler(byteArrayDataSource));

					
					messageBodyPart.setDisposition(MimeBodyPart.INLINE);
					multipart.addBodyPart(messageBodyPart);
					// Send the complete message parts
					msg.setContent(multipart);
				}
				msg.saveChanges();
			} catch (Exception e) {
				throw new DcemException(DcemErrorCodes.EMAIL_MESSAGE_FAILED, e.getMessage(), e);
			}
			try {
				tx.sendMessage(msg, msg.getAllRecipients());
			} catch (SMTPSendFailedException exp) {
				if (exp.getMessage().indexOf("421 4.4.2") != -1) {
					throw new DcemException(DcemErrorCodes.EMAIL_SEND_MSG_LIMIT, null, exp);
				}
				throw new DcemException(DcemErrorCodes.EMAIL_SEND_MSG_FAILED, exp.getMessage(), exp);
			} catch (Exception e) {
				throw new DcemException(DcemErrorCodes.EMAIL_SEND_MSG_FAILED, e.getMessage(), e);
			}

		}

		try {
			tx.close();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
