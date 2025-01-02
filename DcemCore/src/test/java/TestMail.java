
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.doubleclue.dcem.core.utils.DcemTrustManager;

public class TestMail {

	static boolean ignoreCertificats = true;
	static boolean setMarkSeen = false;

	public static void main(String[] args) {
		receiveEmail();
	}

	public static void receiveEmail() {

		SSLContext ctx;
		SocketFactory socketFactory;
		try {
			ctx = SSLContext.getInstance("TLS");
			if (ignoreCertificats == true) {
				DcemTrustManager trustManager = new DcemTrustManager(true);
				trustManager.setSaveServerChainCertificates(false);
				ctx.init(null, new TrustManager[] { trustManager }, null);
				socketFactory = ctx.getSocketFactory();
			} else {
				socketFactory = SSLSocketFactory.getDefault();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		Store emailStore = null;
		Folder inbox = null;
		try {
			Properties properties = System.getProperties();
			properties.setProperty("mail.store.protocol", "imap");
			properties.setProperty("mail.imap.ssl.enable", "true");
			properties.setProperty("mail.imap.auth.plain.disable", "false");
			properties.setProperty("mail.imap.host", "imap.strato.com");
			properties.setProperty("mail.imap.port", "993");
			properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			properties.setProperty("mail.imap.socketFactory.fallback", "false");
			// properties.put("mail.imap.ssl.socketFactory", socketFactory);
			// properties.setProperty("mail.imap.socketFactory.fallback", "false");
			 Session emailSession = Session.getInstance(properties, null);
			 emailSession.setDebug(true);

//			Session emailSession = Session.getInstance(properties, new javax.mail.Authenticator() {
//				protected PasswordAuthentication getPasswordAuthentication() {
//					return new PasswordAuthentication("galea1961", "googlek4s9Tewe");
//				}
//			});
			emailStore = emailSession.getStore("imap");

			// emailStore.connect(prefs.geteMailReceiveHostAddress(), prefs.geteMailReceivePort(), prefs.geteMailReceiveAccount(),
			// prefs.geteMailReceivePassword());
			// emailStore.connect("imap.gmail.com", );
			emailStore.connect("documents@doubleclue.com" , "y2LR}0I%,m#a4U4C" );

	//		inbox = emailStore.getDefaultFolder();
	 		inbox = emailStore.getFolder("INBOX");

			// Fetch unseen messages from inbox folder
			inbox.open(Folder.READ_ONLY);
			Message[] messages;
			messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

			for (Message message : messages) {
				String subject = message.getSubject();
				// check token
				int ind = subject.indexOf("{{");
				if (ind == -1) {
					continue;
				}
				int endInd = subject.indexOf("}}", ind);
				if (endInd == -1) {
					continue;
				}
				String token = subject.substring(ind + 2, endInd);
				System.out.println(token);

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
			e.printStackTrace();
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
