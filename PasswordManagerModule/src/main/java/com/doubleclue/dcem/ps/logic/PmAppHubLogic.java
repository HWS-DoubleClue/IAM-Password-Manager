package com.doubleclue.dcem.ps.logic;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AttributeTypeEnum;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.utils.DcemTrustManager;
import com.doubleclue.dcem.ps.entities.ApplicationHubEntity;
import com.doubleclue.utils.KaraUtils;

import de.slackspace.openkeepass.domain.Attachment;
import de.slackspace.openkeepass.domain.Binaries;
import de.slackspace.openkeepass.domain.BinariesBuilder;
import de.slackspace.openkeepass.domain.Binary;
import de.slackspace.openkeepass.domain.BinaryBuilder;
import de.slackspace.openkeepass.domain.KeePassFile;
import de.slackspace.openkeepass.domain.KeePassFileBuilder;
import de.slackspace.openkeepass.domain.Meta;
import de.slackspace.openkeepass.domain.MetaBuilder;

@ApplicationScoped
@Named("upAppHubLogic")
public class PmAppHubLogic {

	@Inject
	EntityManager em;

	@Inject
	GroupLogic groupLogic;

	@Inject
	PasswordSafeModule psModule;

	@Inject
	AdminModule adminModule;

	private Logger logger = LogManager.getLogger(PmAppHubLogic.class);

	public List<ApplicationHubEntity> getAllApplicationsByName(String name) {
		TypedQuery<ApplicationHubEntity> query = em.createNamedQuery(ApplicationHubEntity.GET_APPLICATIONS_WITH_NAME, ApplicationHubEntity.class);
		if (name == null || name.isEmpty()) {
			name = "%";
		} else {
			name = '%' + name.toLowerCase() + '%';
		}
		query.setParameter(1, name);
		return query.getResultList();
	}

	public ApplicationHubEntity getApplicationByName(String name) {
		TypedQuery<ApplicationHubEntity> query = em.createNamedQuery(ApplicationHubEntity.GET_APPLICATION_BY_NAME, ApplicationHubEntity.class);
		query.setParameter(1, name.toLowerCase());
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public ApplicationHubEntity getApplicationById(Integer appId) {
		return em.find(ApplicationHubEntity.class, appId);
	}

	public List<AppHubAction> getInputActions(ApplicationHubEntity applicationEntity) {
		List<AppHubAction> inputActions = new ArrayList<AppHubAction>();
		for (AppHubAction action : applicationEntity.getApplication().getActions()) {
			if (("input").equals(action.getType()) || ("password").equals(action.getType()) || ("email").equals(action.getType())) {
				inputActions.add(action);
			}
		}
		return inputActions;
	}

	@DcemTransactional
	public void updateApplication(ApplicationHubEntity app) throws DcemException {
		if (app.getId() == null) {
			em.persist(app);
		} else {
			em.merge(app);
		}
	}

	@DcemTransactional
	public void deleteApplication(ApplicationHubEntity app) throws Exception {
		ApplicationHubEntity appEntity = em.merge(app);
		em.remove(appEntity);
	}

	public KeePassFile createKeePassEntry(PasswordSafeEntry currentEntry, KeePassFile keePassFile) {
		if (currentEntry.getEntry().getAttachments() != null) {
			if (keePassFile.getMeta().getBinaries() == null) {
				List<Binary> binaryList = new ArrayList<>();
				Binaries binaries = new BinariesBuilder().binaries(binaryList).build();
				Meta meta = new MetaBuilder(keePassFile.getMeta()).binaries(binaries).historyMaxSize(0).historyMaxItems(0).build();
				keePassFile = new KeePassFileBuilder(keePassFile).withMeta(meta).build();
			}
			List<Attachment> attachments = currentEntry.getEntry().getAttachments();
			int id = getNextBinaryId(keePassFile);
			for (int i = 0; i < attachments.size(); i++) {
				if (attachments.get(i).getRef() == -1) {
					attachments.set(i, new Attachment(attachments.get(i).getKey(), id, attachments.get(i).getData()));
					Binary binary = new BinaryBuilder().data(attachments.get(i).getData()).id(id).isCompressed(false).build();
					List<Binary> allBinary = keePassFile.getMeta().getBinaries().getBinaries();
					allBinary.add(binary);
					id++;
				}
			}
		}
		return keePassFile;
	}

	private int getNextBinaryId(KeePassFile keePassFile) {
		int nextBinaryId = 0;
		if (keePassFile.getMeta().getBinaries() == null) {
			List<Binary> binaryList = new ArrayList<>();
			Binaries binaries = new BinariesBuilder().binaries(binaryList).build();
			Meta meta = new MetaBuilder(keePassFile.getMeta()).binaries(binaries).historyMaxSize(0).historyMaxItems(0).build();
			keePassFile = new KeePassFileBuilder(keePassFile).withMeta(meta).build();
			return nextBinaryId;
		} else {
			List<Binary> allBinary = keePassFile.getMeta().getBinaries().getBinaries();
			for (int i = 0; i < allBinary.size(); i++) {
				if (allBinary.get(i).getId() > nextBinaryId) {
					nextBinaryId = allBinary.get(i).getId();
				}
			}
			nextBinaryId++;
		}
		return nextBinaryId;
	}

	public byte[] appUrlValueValidate(String appUrlValue, boolean withLogo) throws Exception {
		if (appUrlValue == null || appUrlValue.isEmpty() == true) {
			throw new Exception(JsfUtils.getStringSafely(PasswordSafeModule.RESOURCE_NAME, "appHubAdmin.error.urlNotReachable"));
		}
		appUrlValue = appUrlValue.trim();
		URL url = null;
		InputStream inputStream = null;
		HttpsURLConnection conn = null;
		SSLSocketFactory socketFactory;
		HostnameVerifier hostnameVerifier = new HostnameVerifier() {
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		};

		// Making sure URL is in a secure https environment
		url = new URL(appUrlValue);
		String urlString = appUrlValue;
		if (urlString.endsWith("/")) {
			urlString = urlString.substring(0, urlString.length() - 1);
		}
		if (url.getProtocol().equalsIgnoreCase("https") == false) {
			throw new Exception(JsfUtils.getStringSafely(PasswordSafeModule.RESOURCE_NAME, "appHubAdmin.error.httpNotSupported"));
		}

		SSLContext ctx = SSLContext.getInstance("TLS");
		ctx.init(null, new TrustManager[] { new DcemTrustManager(true) }, null);
		socketFactory = ctx.getSocketFactory();
		// Just in case we swithc to apach HttpClient
		// try {
		// CloseableHttpClient httpClient = HttpClientBuilder.create().setRedirectStrategy(new LaxRedirectStrategy()).build();
		// // CloseableHttpClient httpclient = HttpClients.createDefault().
		// HttpGet httpget = new HttpGet(url.toString());
		//
		// httpget.setHeader(UserPortalConstants.USER_AGENT, UserPortalConstants.CHROME_AGENT);
		// CloseableHttpResponse response = httpClient.execute(httpget);
		// HttpEntity entity = response.getEntity();
		// // inputStream = entity.getContent();
		//
		//
		// } catch (Exception e) {
		// logger.debug("Unreachagbe url " + appUrlValue + " Cause: " + e.toString());
		// throw new Exception(JsfUtils.getStringSafely(UserPortalModule.RESOURCE_NAME, "appHubAdmin.error.urlNotReachable") + " Cause: " + e.getMessage());
		// }

		if (inputStream == null) {
			try {
				// Validating connection

				conn = (HttpsURLConnection) url.openConnection();
				conn.setSSLSocketFactory(socketFactory);
				conn.setHostnameVerifier(hostnameVerifier);
				conn.setRequestProperty(PmConstants.USER_AGENT, PmConstants.CHROME_AGENT);
				conn.setConnectTimeout(10000);
				conn.setReadTimeout(10000);
				conn.setInstanceFollowRedirects(false);
				conn.connect();
				inputStream = conn.getInputStream();
			} catch (Exception e) {
				logger.debug("Unreachagbe url " + appUrlValue + " Cause: " + e.toString());
				throw new Exception(
						JsfUtils.getStringSafely(PasswordSafeModule.RESOURCE_NAME, "appHubAdmin.error.urlNotReachable") + " Cause: " + e.getMessage());
			}
		}
		if (withLogo == false) {
			return null;
		}

		try {
			byte[] logoImage = null;
			StringWriter stringWriter = new StringWriter();
			KaraUtils.copyStream(inputStream, stringWriter);
			String stringBuffer = stringWriter.toString();
			inputStream.close();
			int ind = stringBuffer.indexOf("<body");
			if (ind != -1) {
				stringBuffer = stringBuffer.substring(0, ind); // remove body
			}
			if (logger.isTraceEnabled()) {
				logger.trace("ApplicationHubAdminView.appUrlValueValidate() READ  LINE: " + stringBuffer);
			}
			// now search all links with ref icon
			int indexStart = 0;
			int bufferIndex = 0;
			while (bufferIndex < stringBuffer.length() && logoImage == null) {
				int indexEnd = 0;
				indexStart = stringBuffer.indexOf("<link ", bufferIndex);
				if (indexStart == -1) {
					break; // no links found
				}
				indexStart += 6;
				indexEnd = stringBuffer.indexOf(">", indexStart);
				if (indexEnd == -1) {
					break; // no link-end found
				}
				String linkElement = stringBuffer.substring(indexStart, indexEnd);
				bufferIndex = indexEnd;
				if (linkElement.contains("icon") == false) {
					continue;
				}
				String parsedFaviconUrl = StringUtils.substringBetween(linkElement, "href=\"", "\"");
				if (parsedFaviconUrl == null) {
					parsedFaviconUrl = StringUtils.substringBetween(linkElement, "href='", "'");
				}
				if (parsedFaviconUrl == null) {
					ind = linkElement.indexOf("href=");
					if (ind != -1) {
						parsedFaviconUrl = linkElement.substring(ind + "href=".length());
					}
				}
				if (parsedFaviconUrl == null) {
					continue;
				}
				URL faviconUrl = formUrl(url, urlString, parsedFaviconUrl);
				logoImage = getDefaultLogoFromUrl(faviconUrl, hostnameVerifier, socketFactory);
			}
			if (logoImage == null) {
				URL faviconUrl = new URL(urlString + "/favicon.ico");
				logoImage = getDefaultLogoFromUrl(faviconUrl, hostnameVerifier, socketFactory);
			}
			if (logoImage == null) {
				ind = stringBuffer.indexOf("<meta property=\"og:image\"");
				if (ind != -1) {
					String iconString = StringUtils.substringBetween(stringBuffer.substring(ind), "content=\"", "\"");
					if (iconString == null) {
						iconString = StringUtils.substringBetween(stringBuffer.substring(ind), "content='", "'");
					}
					URL faviconUrl = formUrl(url, urlString, iconString);
					logoImage = getDefaultLogoFromUrl(faviconUrl, hostnameVerifier, socketFactory);
					if (logoImage != null) {
						return logoImage;
					}
				}
			}
			return logoImage;
		} catch (Exception e) {
			logger.info("Couldn't load logo from html elements. " + urlString, e);
			return null;
		}
	}

	private byte[] getDefaultLogoFromUrl(URL faviconUrl, HostnameVerifier hostnameVerifier, SSLSocketFactory socketFactory) throws Exception {
		byte[] image = getLogoFromUrl(faviconUrl, hostnameVerifier, socketFactory);
		if (image == null) {
			String rootURL = faviconUrl.getProtocol() + "://" + faviconUrl.getHost();
			String path = faviconUrl.getPath();
			int ind = path.lastIndexOf('/');
			String icon = path.substring(ind);
			faviconUrl = new URL(rootURL + icon);
			return getLogoFromUrl(faviconUrl, hostnameVerifier, socketFactory);
		} else {
			return image;
		}
	}

	private byte[] getLogoFromUrl(URL faviconUrl, HostnameVerifier hostnameVerifier, SSLSocketFactory socketFactory) throws Exception {
		logger.debug("Get Icon from: " + faviconUrl);
		HttpsURLConnection conn = (HttpsURLConnection) faviconUrl.openConnection();
		conn.setSSLSocketFactory(socketFactory);
		conn.setHostnameVerifier(hostnameVerifier);
		InputStream inputStream = null;
		try {
			conn.setRequestProperty(PmConstants.USER_AGENT, PmConstants.CHROME_AGENT);
			conn.connect();
			inputStream = conn.getInputStream();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			KaraUtils.copyStream(inputStream, output);
			ByteArrayInputStream bis = new ByteArrayInputStream(output.toByteArray());
			BufferedImage icon;
			icon = ImageIO.read(bis);
			if (icon != null) {
				logger.debug("Get Icon FOUND from: " + faviconUrl);
				return output.toByteArray();
			}
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

	private URL formUrl(URL url, String urlString, String favicon) throws Exception {
		if (favicon.startsWith("http")) {
			return new URL(favicon);
		}
		if (favicon.startsWith("//")) {
			return new URL("https:" + favicon);
		}
		if (favicon.startsWith("/")) {
			return new URL(urlString + favicon);
		}
		return new URL(urlString + "/" + favicon);
	}

	@DcemTransactional
	public List<ApplicationHubEntity> migrateApplications26() {
		List<ApplicationHubEntity> applications = getAllApplicationsByName(null);
		for (ApplicationHubEntity applicationHubEntity : applications) {
			for (AppHubAction appHubAction : applicationHubEntity.getApplication().getActions()) {
				int i = KaraUtils.getNumeric(appHubAction.getValueSourceType());
				if (i == -1) {
					continue;
				} else {
					appHubAction.setValueSourceType(AttributeTypeEnum.values()[i].name());
				}
			}
		}
		return applications;
	}

}
