package com.doubleclue.dcem.core.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.ConnectionService;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DomainEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.oauth.oauth2.OAuthAuthorisationRequest;
import com.doubleclue.oauth.oauth2.OAuthGrantRopcRequest;
import com.doubleclue.oauth.oauth2.enums.OAuthResponseMode;
import com.doubleclue.oauth.oauth2.enums.OAuthResponseType;
import com.doubleclue.oauth.openid.OpenIdAccessTokenResponse;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;

public class AzureUtils {

	public static String getAuthority(DomainEntity domainEntity) {
		return DcemConstants.AZURE_AUTHORITY + domainEntity.getHost();
	}

	public static String getTenantID(DomainEntity domainEntity) {
		return domainEntity.getHost();
	}

	public void setTenantID(DomainEntity domainEntity, String tenantId) {
		domainEntity.setHost(tenantId);
	}

	public static String getClientID(DomainEntity domainEntity) {
		return domainEntity.getSearchAccount();
	}

	public static void setClientID(DomainEntity domainEntity, String clientId) {
		domainEntity.setSearchAccount(clientId);
	}

	public static String getClientSecret(DomainEntity domainEntity) {
		return domainEntity.getPassword();
	}

	public static void setClientSecret(DomainEntity domainEntity, String clientSecret) {
		domainEntity.setPassword(clientSecret);
	}

	public static AuthenticationResult getAuthResultByAuthCode(String dcemHost, String authCode,
			DomainEntity domainEntity) throws Exception {
		AuthenticationContext context = getAuthenticationContext(domainEntity);
		ClientCredential credential = getClientCredential(domainEntity);
		return context.acquireTokenByAuthorizationCode(authCode, getRedirectUri(dcemHost), credential,
				DcemConstants.AZURE_RESOURCE_GRAPH, null).get();
	}

	/*
	 * public static AuthenticationResult getAuthResultByRefreshToken(DomainEntity
	 * domainEntity) throws Exception { AuthenticationContext context =
	 * getAuthenticationContext(domainEntity); ClientCredential credential =
	 * getClientCredential(domainEntity); String refreshToken =
	 * domainEntity.getAzureAdConfig().getRefreshToken(); return
	 * context.acquireTokenByRefreshToken(refreshToken, credential, null).get(); }
	 */

	public static AuthenticationResult getAuthResultByClientCredentials(DomainEntity domainEntity) throws Exception {
		AuthenticationContext context = getAuthenticationContext(domainEntity);
		ClientCredential credential = getClientCredential(domainEntity);
		return context.acquireToken(DcemConstants.AZURE_RESOURCE_GRAPH, credential, null).get();
	}

	public static OpenIdAccessTokenResponse getAuthResultByRopc(DomainEntity domainEntity, String username,
			String password) throws DcemException {
		HttpsURLConnection conn = null;
		try {
			URL url = new URL(DcemConstants.AZURE_AUTHORITY + getTenantID(domainEntity) + "/oauth2/v2.0/token");
			conn = (HttpsURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(10 * 1000);  // 10 seconds
			conn.connect();
			String[] scopes = new String[] { "openid", "user.read.all" };
			OAuthGrantRopcRequest request = new OAuthGrantRopcRequest(domainEntity.getId().toString(), username,
					password, scopes, getClientID(domainEntity), getClientSecret(domainEntity));
			// request.setCustomParam("resource", DcemConstants.AZURE_RESOURCE_GRAPH);

			OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
			writer.write(request.getQueryString());
			writer.close();
			StringBuilder receivedData = new StringBuilder();
			String line;

			int responseCode = conn.getResponseCode();

			try {
				InputStream inputStream;
				if (conn.getErrorStream() != null) {
					inputStream = conn.getErrorStream();
				} else {
					inputStream = conn.getInputStream();
				}

				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
				while ((line = br.readLine()) != null) {
					receivedData.append(line);
				}
				br.close();
			} catch (Exception e) {
				// TODO: handle exception
			}

			switch (responseCode) {
			case 200:
			case 299:
				return new OpenIdAccessTokenResponse(receivedData.toString());
			case 400:	
				throw new DcemException(DcemErrorCodes.DOMAIN_WRONG_AUTHENTICATION, receivedData.toString());
			case 401:
				throw new DcemException(DcemErrorCodes.DOMAIN_WRONG_AUTHENTICATION, receivedData.toString());
			default:
				throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, receivedData.toString());
			}
		} catch (DcemException exp) {
			throw exp;
		} catch (Exception exp) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, username, exp);
		} finally {
			conn.disconnect();
		}
	}

	/*
	 * public static String getAccessToken(DomainEntity domainEntity) throws
	 * DcemException { try { AzureAdConfig config = domainEntity.getAzureAdConfig();
	 * if (config != null) { if (config.getAccessTokenExpiresOn().after(new Date()))
	 * { String refreshToken = config.getRefreshToken(); if (refreshToken != null) {
	 * AuthenticationResult result =
	 * AzureUtils.getAuthResultByRefreshToken(domainEntity);
	 * domainEntity.setAzureAdConfig(new AzureAdConfig(result)); DomainLogic
	 * domainLogic = CdiUtils.getReference(DomainLogic.class);
	 * domainLogic.addOrUpdateDcemLdap(domainEntity, AzureUtils.getEditAction()); }
	 * else { throw new DcemException(DcemErrorCodes.AZURE_DOMAIN_NOT_AUTHORISED,
	 * "The domain " + domainEntity.getName() + " does not have a refresh token.");
	 * } } return config.getAccessToken(); } else { throw new
	 * DcemException(DcemErrorCodes.AZURE_DOMAIN_NOT_AUTHORISED, "The domain " +
	 * domainEntity.getName() + " is not yet authorised."); } } catch (DcemException
	 * e) { throw e; } catch (Exception e) { throw new
	 * DcemException(DcemErrorCodes.GENERAL, "Error while obtaining Access Token: "
	 * + e.getMessage()); } }
	 */

	public static URI getRedirectUri(String dcemHost) throws Exception {
		ConfigLogic configLogic = CdiUtils.getReference(ConfigLogic.class);
		ConnectionService service = configLogic.getClusterConfig()
				.getConnectionService(ConnectionServicesType.AZURE_CALLBACK);
		if (service != null) {
			return new URI((service.isSecure() ? "https" : "http") + "://" + dcemHost + ":" + service.getPort()
					+ DcemConstants.DEFAULT_WEB_NAME + DcemConstants.AZURE_CALLBACK_SERVLET_PATH);
		}
		return null;
	}

	public static AuthenticationContext getAuthenticationContext(DomainEntity domainEntity)
			throws MalformedURLException {
		ExecutorService service = Executors.newSingleThreadExecutor();
		return new AuthenticationContext(getAuthority(domainEntity), true, service);
	}

	public static ClientCredential getClientCredential(DomainEntity domainEntity) {
		return new ClientCredential(getClientID(domainEntity), getClientSecret(domainEntity));
	}

	public static String getAuthorisationCodeUrl(DomainEntity domainEntity, String dcemHost) throws Exception {
		OAuthAuthorisationRequest request = new OAuthAuthorisationRequest();
		request.setResponseType(OAuthResponseType.AUTH_CODE);
		request.setClientId(AzureUtils.getClientID(domainEntity));
		request.setRedirectUri(getRedirectUri(dcemHost).toString());
		request.setState(domainEntity.getId().toString());
		request.setResponseMode(OAuthResponseMode.QUERY);
		StringBuilder sb = new StringBuilder(AzureUtils.getAuthority(domainEntity));
		sb.append("/oauth2/authorize?");
		sb.append(request.getQueryString());
		return sb.toString();
	}

	public static DcemAction getEditAction() {
		return new DcemAction(SystemModule.MODULE_ID, null, DcemConstants.ACTION_EDIT);
	}
}
