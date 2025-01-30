package com.doubleclue.dcem.core.utils.mail;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.as.DcemUploadFile;
import com.doubleclue.dcem.core.utils.typedetector.DcemMediaType;
import com.doubleclue.dcem.core.utils.typedetector.FileUploadDetector;

import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;

public class MailUtils {

	private static final Pattern IMG_CID_REGEX = Pattern.compile("cid:(.*?)[\"']", Pattern.DOTALL);
	private static final Pattern IMG_CID_PLAIN_REGEX = Pattern.compile("\\[cid:(.*?)\\]", Pattern.DOTALL);
	private static final Pattern HTML_META_CHARSET_REGEX = Pattern.compile("(<meta(?!\\s*(?:name|value)\\s*=)[^>]*?charset\\s*=[\\s\"']*)([^\\s\"'/>]*)",
			Pattern.DOTALL);
	private static final String HTML_WRAPPER_TEMPLATE = "<!DOCTYPE html><html><head><style>body{font-size: 0.5cm;}</style><meta charset=\"%s\"><title>title</title></head><body>%s</body></html>";
	static final String MAIL_INFO = "<div><h2>EMAIL</h2>\nFrom: %s<br></br>Encoded: %s</div>";

	private static final Logger logger = LogManager.getLogger(MailUtils.class);

	public static List<DcemUploadFile> processReceivedMail(File emlFile) throws Exception {
		MimeMessage message = new MimeMessage(null, new FileInputStream(emlFile));
		String from = message.getHeader("From", null);
		MimeObjectEntry<String> bodyEntry = MimeMessageParser.findBodyPart(message);
		final String charsetName = bodyEntry.getContentType().getParameter("charset");
		final HashMap<String, MimeObjectEntry<String>> inlineImageMap = MimeMessageParser.getInlineImageMap(message);
		String htmlBody = bodyEntry.getEntry();
		if (bodyEntry.getContentType().match("text/html")) {
			if (inlineImageMap.isEmpty() == false) {
				logger.debug("Embed the referenced images (cid) using <img src=\"data:image ...> syntax");
				// find embedded images and embed them in html using <img src="data:image ...> syntax
				htmlBody = StringReplacer.replace(htmlBody, IMG_CID_REGEX, new StringReplacerCallback() {
					@Override
					public String replace(Matcher m) throws Exception {
						String cid = m.group(1);
						MimeObjectEntry<String> base64Entry = inlineImageMap.get("<" + cid + ">");
						// heuristic to find entry with in eml cid=X and Content-ID=<X@...>
						if (base64Entry == null) {
							for (String key : inlineImageMap.keySet()) {
								if (key.startsWith("<" + cid + "@") && key.endsWith(">")) {
									base64Entry = inlineImageMap.get(key);
									break;
								}
							}
						}
						// found no image for this cid, just return the matches string as it is
						if (base64Entry == null) {
							logger.error("Found no inline image for cid: %s", cid);
							return m.group();
						}
						return "data:" + base64Entry.getContentType().getBaseType() + ";base64," + base64Entry.getEntry() + "\"";
					}
				});
			}
			// overwrite html declared charset with email header charset
			htmlBody = StringReplacer.replace(htmlBody, HTML_META_CHARSET_REGEX, new StringReplacerCallback() {
				@Override
				public String replace(Matcher m) throws Exception {
					String declaredCharset = m.group(2);
					if (!charsetName.equalsIgnoreCase(declaredCharset)) {
						logger.debug("Html declared different charset (%s) then the email header (%s), override with email header", declaredCharset,
								charsetName);
					}
					return m.group(1) + charsetName;
				}
			});
		} else {
			logger.debug("No html message body could be found, fall back to text/plain and embed it into a html document");
			htmlBody = "<div style=\"white-space: pre-wrap\">" + htmlBody.replace("\n", "<br>").replace("\r", "") + "</div>";
			htmlBody = String.format(HTML_WRAPPER_TEMPLATE, charsetName, htmlBody);
			if (inlineImageMap.size() > 0) {
				logger.debug("Embed the referenced images (cid) using <img src=\"data:image ...> syntax");
				// find embedded images and embed them in html using <img src="data:image ...> syntax
				htmlBody = StringReplacer.replace(htmlBody, IMG_CID_PLAIN_REGEX, new StringReplacerCallback() {
					@Override
					public String replace(Matcher m) throws Exception {
						MimeObjectEntry<String> base64Entry = inlineImageMap.get("<" + m.group(1) + ">");
						// found no image for this cid, just return the matches string
						if (base64Entry == null) {
							return m.group();
						}
						return "<img src=\"data:" + base64Entry.getContentType().getBaseType() + ";base64," + base64Entry.getEntry() + "\" />";
					}
				});
			}
		}
		File tempFile = File.createTempFile("dcem-", "-mail");
		Files.write(tempFile.toPath(), htmlBody.getBytes(StandardCharsets.UTF_8));
		List<DcemUploadFile> emailFiles = new ArrayList<DcemUploadFile>();

		emailFiles.add(new DcemUploadFile("Body.html", tempFile, DcemMediaType.XHTML, String.format(MAIL_INFO, from, MediaType.TEXT_HTML.toString())));
		Object content = message.getContent();
		if (content instanceof Multipart) {
			Multipart multiPart = (Multipart)content;
			int numberOfParts = multiPart.getCount();
			for (int partCount = 0; partCount < numberOfParts; partCount++) {
				MimeBodyPart part = (MimeBodyPart) multiPart.getBodyPart(partCount);
				if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
					tempFile = File.createTempFile("dcem-", "-mail");
					part.saveFile(tempFile);
					String mediaType = FileUploadDetector.detectMediaType(tempFile);
					DcemMediaType dcemMediaType = DcemMediaType.getDcemMediaType(mediaType);
					emailFiles.add(new DcemUploadFile(part.getFileName(), tempFile, dcemMediaType, mediaType));
				}
			}
		}
		return emailFiles;
	}
}
