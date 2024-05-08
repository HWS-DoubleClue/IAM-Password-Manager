package com.doubleclue.dcem.core.utils;

import java.io.IOException;
import java.util.ArrayList;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;

@ApplicationScoped
@Named("clientRestApi")
public class ClientRestApi {

	boolean traceRestApi;

	public enum HttpVerb {
		HTTP_GET, HTTP_PUT, HTTP_PATCH, HTTP_DELETE, HTTP_POST;
	}

	private Logger logger = LogManager.getLogger(ClientRestApi.class);

	CloseableHttpClient httpClient;

	public CloseableHttpResponse postRequest(String url, UsernamePasswordCredentials credentials, String body, String contentType, boolean unseruce,
			int timeoutSeconds) throws Exception {
		return getResponse(url, false, HttpVerb.HTTP_POST, credentials, body, contentType, unseruce, timeoutSeconds);
	}

	public CloseableHttpResponse postRequest(String url, String authHeader, String body, String contentType, String accept, boolean unsecure,
			int timeoutSeconds) throws Exception {
		ClientRestApiParams apiParams = new ClientRestApiParams(url, HttpVerb.HTTP_POST, null, body, contentType);
		apiParams.authHeader = authHeader;
		apiParams.unsecure = unsecure;
		apiParams.setTimeoutSeconds(timeoutSeconds);
		if (accept != null) {
			apiParams.headers = new ArrayList<KeyValuePair>();
			apiParams.headers.add(new KeyValuePair(HttpHeaders.ACCEPT, accept));
		}
		return getResponse(apiParams);
	}

	@Deprecated
	public CloseableHttpResponse patchRequest(String url, String authHeader, String body, String contentType, String accept, boolean unsecure,
			int timeoutSeconds) throws Exception {
		ClientRestApiParams apiParams = new ClientRestApiParams(url, HttpVerb.HTTP_PATCH, null, body, contentType);
		apiParams.setTimeoutSeconds(timeoutSeconds);
		apiParams.unsecure = unsecure;
		apiParams.authHeader = authHeader;
		if (accept != null) {
			apiParams.headers = new ArrayList<KeyValuePair>();
			apiParams.headers.add(new KeyValuePair(HttpHeaders.ACCEPT, accept));
		}
		return getResponse(apiParams);
	}

	@Deprecated
	public CloseableHttpResponse deleteRequest(String url, String authHeader, boolean unsecure, int timeoutSeconds) throws Exception {
		ClientRestApiParams apiParams = new ClientRestApiParams(url, HttpVerb.HTTP_DELETE, null, null, null);
		apiParams.setTimeoutSeconds(timeoutSeconds);
		apiParams.unsecure = unsecure;
		apiParams.authHeader = authHeader;
		return getResponse(apiParams);
	}
	
	public void deleteRequest(String url, String authHeader, String body, boolean unsecure, int timeoutSeconds, String accept, String contentType) throws Exception {
		ClientRestApiParams apiParams = new ClientRestApiParams(url, HttpVerb.HTTP_DELETE, null, body, contentType);
		apiParams.setTimeoutSeconds(timeoutSeconds);
		apiParams.unsecure = unsecure;
		apiParams.authHeader = authHeader;
		if (accept != null) {
			apiParams.headers = new ArrayList<KeyValuePair>();
			apiParams.headers.add(new KeyValuePair(HttpHeaders.ACCEPT, accept));
		}
		getResponse(apiParams);		
	}

	public CloseableHttpResponse deleteRequest(String url, UsernamePasswordCredentials credentials, boolean unsecure, int timeoutSeconds) throws Exception {
		return getResponse(url, false, HttpVerb.HTTP_DELETE, credentials, null, null, unsecure, timeoutSeconds);
	}

	@Deprecated 
	public CloseableHttpResponse putRequest(String url, UsernamePasswordCredentials credentials, String body, String contentType, boolean unsecure,
			int timeoutSeconds) throws Exception {
		return getResponse(url, false, HttpVerb.HTTP_PUT, credentials, body, contentType, unsecure, timeoutSeconds);
	}

	private CloseableHttpResponse getResponse(String url, boolean closeConnection, HttpVerb httpVerb, UsernamePasswordCredentials credentials, String body,
			String contentType, boolean unsecure, int timeoutSeconds) throws Exception {
		ClientRestApiParams apiParams = new ClientRestApiParams(url, httpVerb, credentials, body, contentType);
		apiParams.setTimeoutSeconds(timeoutSeconds);
		apiParams.unsecure = unsecure;
		return getResponse(apiParams);
	}

	@Deprecated
	public CloseableHttpResponse simplePostRequest(String url, String body, String contentType, boolean unsecure, int timeoutSeconds) throws Exception {
		ClientRestApiParams apiParams = new ClientRestApiParams(url, HttpVerb.HTTP_POST, null, body, contentType);
		apiParams.setTimeoutSeconds(timeoutSeconds);
		apiParams.unsecure = unsecure;
		return getResponse(apiParams);
	}

	// private CloseableHttpResponse getResponse(String url, boolean closeConnection, HttpVerb httpVerb, String authHeader, boolean userAgentHeader, String
	// body,
	// String contentType, String accept, boolean unsecure, int timeoutSeconds) throws Exception {
	// long start = System.currentTimeMillis();
	// CloseableHttpResponse response = null;

	public CloseableHttpResponse getResponse(ClientRestApiParams clientRestApiParams) throws Exception {
		long start = System.currentTimeMillis();
		CloseableHttpResponse response = null;

		HttpUriRequest request = null;
		StringEntity entity = null;
		if (clientRestApiParams.body != null) {
			entity = new StringEntity(clientRestApiParams.body, "UTF-8");
		}
		switch (clientRestApiParams.httpVerb) {
		case HTTP_DELETE:
			request = new HttpDelete(clientRestApiParams.url);
			break;
		case HTTP_GET:
			request = new HttpGet(clientRestApiParams.url);
			break;
		case HTTP_POST:
			request = new HttpPost(clientRestApiParams.url);
			((HttpPost) request).setEntity(entity);
			break;
		case HTTP_PUT:
			request = new HttpPut(clientRestApiParams.url);
			((HttpPut) request).setEntity(entity);
			break;
		case HTTP_PATCH:
			request = new HttpPatch(clientRestApiParams.url);
			((HttpPatch) request).setEntity(entity);
		default:
			break;
		}
		String authHeader = clientRestApiParams.getAuthHeader();
		if (authHeader != null) {
			request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		}
		if (clientRestApiParams.withoutUserAgent == false) {
			request.addHeader(DcemConstants.USER_AGENT, DcemConstants.CHROME_AGENT);
		}
		if (clientRestApiParams.contentType != null) {
			request.addHeader(HttpHeaders.CONTENT_TYPE, clientRestApiParams.contentType);
		}
//		if (accept != null) {
//			request.addHeader(HttpHeaders.ACCEPT, accept);
//		}
		if (clientRestApiParams.headers != null) {
			for (KeyValuePair keyValuePair : clientRestApiParams.headers) {
				if (keyValuePair.value == null) {
					request.removeHeaders(keyValuePair.key);
				} else {
					request.addHeader(keyValuePair.key, keyValuePair.value);
				}
			}
		}
		// adding our own TrustManager
		DcemTrustManager trustManager = new DcemTrustManager(clientRestApiParams.unsecure, true);
		trustManager.addDefaultTrustManager();
		SSLContext context = SSLContext.getInstance("TLSv1.2");
		context.init(null, new TrustManager[] { trustManager }, null);

		HostnameVerifier hostnameVerifier = null;
		if (clientRestApiParams.unsecure == true) {
			hostnameVerifier = new HostnameVerifier() {
				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			};
		}
		RequestConfig config = RequestConfig.custom().setConnectTimeout(clientRestApiParams.getTimeoutMilli()).setConnectionRequestTimeout(clientRestApiParams.getRequestTimeout())
				.setSocketTimeout(clientRestApiParams.getTimeoutMilli()).build();

		CloseableHttpClient client = null;
		if (hostnameVerifier == null) {
			client = HttpClientBuilder.create().setSSLContext(context).setDefaultRequestConfig(config).build();
		} else {
			client = HttpClientBuilder.create().setSSLContext(context).setSSLHostnameVerifier(hostnameVerifier).setDefaultRequestConfig(config).build();
		}

		response = client.execute(request);
		if (response != null && clientRestApiParams.closeConnection) {
			try {
				response.close();
			} catch (IOException e) {
				logger.warn("Failed to close response", e);
			}
		}
		if (traceRestApi == true && logger.isDebugEnabled()) {
			StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
			StringBuffer sb = new StringBuffer();
			sb.append(" URL=");
			sb.append(clientRestApiParams.url);
			sb.append(System.lineSeparator());
			sb.append("Response-Time (msec): ");
			sb.append(System.currentTimeMillis() - start);
			sb.append(System.lineSeparator());
			for (StackTraceElement element : stacktrace) {
				String className = element.getClassName();
				if (className.startsWith("java.lang")) {
					continue;
				}
				if (className.endsWith("ClientRestApi")) {
					continue;
				}
				if (className.startsWith("com.doubleclue")) {
					sb.append(className);
					sb.append(":");
					sb.append(element.getMethodName());
					sb.append(System.lineSeparator());
				} else {
					break;
				}
			}
			logger.debug("REST API, " + sb.toString());
		}
		clientRestApiParams.responseTime = System.currentTimeMillis() - start;
		return response;
	}

	public String getResponseHeader(ClientRestApiParams apiParams, String headerName)
			throws Exception {
		CloseableHttpResponse response = getResponse(apiParams);
		if (response != null) {
			Header header = response.getFirstHeader(headerName);
			return (header != null) ? header.getValue() : null;
		}
		return null;
	}
	
	@Deprecated
	public String getResponseHeader(String url, String headerName, UsernamePasswordCredentials credentials, int timeoutSeconds) throws Exception {
		return getResponseHeader(url, headerName, credentials, false, timeoutSeconds);
	}

	@Deprecated
	public String getResponseHeader(String url, String headerName, UsernamePasswordCredentials credentials, boolean unsecure, int timeoutSeconds)
			throws Exception {
		CloseableHttpResponse response = getResponse(url, true, HttpVerb.HTTP_GET, credentials, null, null, unsecure, timeoutSeconds);
		if (response != null) {
			Header header = response.getFirstHeader(headerName);
			return (header != null) ? header.getValue() : null;
		}
		return null;
	}

	@Deprecated
	public String getResponseBody(String url, UsernamePasswordCredentials credentials, int timeoutSeconds) throws Exception {
		return getResponseBody(url, credentials, false, timeoutSeconds);
	}

	@Deprecated
	public String getResponseBody(String url, String authHeader, boolean unsecure, int timeoutSeconds) throws Exception {
		ClientRestApiParams apiParams = new ClientRestApiParams(url, HttpVerb.HTTP_GET, null, null, null);
		apiParams.setTimeoutSeconds(timeoutSeconds);
		apiParams.unsecure = unsecure;
		apiParams.setAuthHeader(authHeader);
		apiParams.setTimeoutSeconds(timeoutSeconds);
		apiParams.headers = new ArrayList<KeyValuePair>();
		apiParams.headers.add(new KeyValuePair(HttpHeaders.ACCEPT, "*/*"));
		CloseableHttpResponse response = getResponse(apiParams);
		if (response != null) {
			try {
				return EntityUtils.toString(response.getEntity());
			} catch (Exception exp) {
				throw exp;
			} finally {
				try {
					response.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}
	
	
	public String getResponseBody(ClientRestApiParams apiParams) throws Exception {
		CloseableHttpResponse response = getResponse(apiParams);
		if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
			throw new Exception ("Invalid HTTP STATUS: " + response.getStatusLine());
		}
		if (response != null) {
			try {
				return EntityUtils.toString(response.getEntity());
			} catch (Exception exp) {
				throw exp;
			} finally {
				try {
					response.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	public String getResponseBody(String url, UsernamePasswordCredentials credentials, boolean unsecure, int timeoutSeconds) throws Exception {
		CloseableHttpResponse response = getResponse(url, false, HttpVerb.HTTP_GET, credentials, null, null, unsecure, timeoutSeconds);
		if (response != null) {
			try {
				return EntityUtils.toString(response.getEntity());
			} catch (Exception exp) {
				throw exp;
			} finally {
				try {
					response.close();
				} catch (IOException e) {
				}
			}
		}
		return null;
	}

	public boolean isTraceRestApi() {
		return traceRestApi;
	}

	public void setTraceRestApi(boolean traceRestApi) {
		this.traceRestApi = traceRestApi;
	}

	
}
