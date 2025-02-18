
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.search.FlagTerm;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import com.doubleclue.dcem.core.utils.DcemTrustManager;
import com.drew.lang.Charsets;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestMail {

	static boolean ignoreCertificats = true;
	static boolean setMarkSeen = false;

	public static void main(String[] args) {
		String token = null;
		try {
	//		token = getAuthToken("1626cafb-2274-4113-86b5-61bfa2cec5c1", "svc_doubleclue_smtp_exo@hws-gruppe.de", "Ha1AGAAkGdLjCivOFb5Q" );
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		System.out.println("TestMail.main() Token is: " + token);
		receiveEmail();
		System.exit(0);
	}
	
	static public String getAuthToken(String tanantId,String clientId,String client_secret) throws Exception {
	    CloseableHttpClient client = HttpClients.createDefault();
	    HttpPost loginPost = new HttpPost("https://login.microsoftonline.com/" + tanantId + "/oauth2/v2.0/token");
	    String scopes = "https://outlook.office365.com/.default";
	    String encodedBody = "client_id=" + clientId + "&scope=" + scopes + "&client_secret=" + client_secret
	            + "&grant_type=client_credentials";
	    loginPost.setEntity(new StringEntity(encodedBody, ContentType.APPLICATION_FORM_URLENCODED));
	    loginPost.addHeader(new BasicHeader("cache-control", "no-cache"));
	    CloseableHttpResponse loginResponse = client.execute(loginPost);
	    InputStream inputStream = loginResponse.getEntity().getContent();
	    byte[] response = IOUtils.toByteArray(inputStream);
	    String  replay = new String (response, Charsets.UTF_8);
	    if (loginResponse.getStatusLine().getStatusCode() > 299) {
	    	throw new Exception (loginResponse.getStatusLine().toString() + "\n Response: \n " + replay);
	    }	   
	    ObjectMapper objectMapper = new ObjectMapper();
	    JavaType type = objectMapper.constructType(
	            objectMapper.getTypeFactory().constructParametricType(Map.class, String.class, String.class));
	    Map<String, String> parsed = new ObjectMapper().readValue(response, type);
	    return parsed.get("access_token");
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
			properties.setProperty("mail.imap.host", "outlook.office365.com");
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

			emailStore.connect("outlook.office365.com", 993, "svc_doubleclue_smtp_exo@hws-gruppe.de", "xx");
			// emailStore.connect("imap.gmail.com", );
			

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
