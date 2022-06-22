import java.io.IOException;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FlagTerm;

public class ReceiveMail {

	public static void receiveEmail(String pop3Host, String storeType, String user, String password) {
		Store emailStore;
		try {
			if (System.getProperty("os.name").startsWith("Windows")) {
				System.setProperty("javax.net.ssl.trustStoreType", "Windows-ROOT");
			}
			// 1) get the session object
			Properties properties = new Properties();
			properties.setProperty("mail.imaps.starttls.enable", "true");
			properties.setProperty("mail.imap.auth.plain.disable", "false");
			properties.setProperty("mail.imaps.auth.ntlm.disable", "true");
			properties.setProperty("mail.imaps.auth.gssapi.disable", "true");
			// props.put("mail.imap.sasl.mechanisms", "XOAUTH2");

			Session emailSession = Session.getDefaultInstance(properties);
			emailSession.setDebug(true);
			// 2) create the POP3 store object and connect with the pop server
			// POP3Store emailStore = (POP3Store) emailSession.getStore(storeType);
			emailStore = emailSession.getStore("imaps");
			emailStore.connect("owa.hws-gruppe.de", 993, "abcd", "xxxxxx");

			Folder inbox = emailStore.getFolder("INBOX");
			// Folder inbox = emailStore.getFolder("shiftplan");
			// Fetch unseen messages from inbox folder
			inbox.open(Folder.READ_WRITE);
			// inbox.open(Folder.READ_ONLY);
			Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
			// 4) retrieve the messages from the folder in an array and print it
			// Message[] messages = emailFolder.getMessages();
			for (int i = 0; i < messages.length; i++) {
				Message message = messages[i];
				// message.setFlag(Flags.Flag.SEEN, true);
				
				System.out.println("---------------------------------");
				System.out.println("Email Number " + (i + 1));
				System.out.println("Subject: " + message.getSubject());
				System.out.println("From: " + message.getFrom()[0]);
				
				String result = "";
				if (message.isMimeType("text/plain")) {
					result = message.getContent().toString();
				} else if (message.isMimeType("multipart/*")) {
					MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
					result = getTextFromMimeMultipart(mimeMultipart);
				}
				message.setFlag(Flags.Flag.DELETED, true);
				System.out.println("Text: " + result);
			}

			// 5) close the store and folder objects
			inbox.close(false);
			emailStore.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("ReceiveMail.receiveEmail() Closing");
		System.exit(0);
	}

	private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
		String result = "";
		int count = mimeMultipart.getCount();
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				result = result + "\n" + bodyPart.getContent();
				break; // without break same text appears twice in my tests
			} else if (bodyPart.isMimeType("text/html")) {
				String html = (String) bodyPart.getContent();
				//result = result + "\n" + org.jsoup.Jsoup.parse(html).text();
				result = html;
			} else if (bodyPart.getContent() instanceof MimeMultipart) {
				result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
			}
		}
		return result;
	}

	public static void main(String[] args) {

		String host = "owa.hws-gruppe.de"; // change accordingly
		String mailStoreType = "pop3s";
		String username = "1234@hws-gruppe.de";
		String password = "yourpassword";// change accordingly

		receiveEmail(host, mailStoreType, username, password);

	}
}