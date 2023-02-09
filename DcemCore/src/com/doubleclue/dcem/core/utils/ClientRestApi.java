package com.doubleclue.dcem.core.utils;

import java.io.IOException;
import java.util.Base64;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
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
import com.doubleclue.dcem.core.gui.DcemApplicationBean;

@ApplicationScoped
@Named("clientRestApi")
public class ClientRestApi {
	
	boolean traceRestApi; 
	
	enum HttpVerb {
		HTTP_GET, HTTP_PUT, HTTP_DELETE, HTTP_POST;
	}

	private Logger logger = LogManager.getLogger(ClientRestApi.class);

	CloseableHttpClient httpClient;
	
	public CloseableHttpResponse postRequest(String url, UsernamePasswordCredentials credentials, String body,
			String contentType, boolean unseruce, int timeoutSeconds) throws Exception {
		return getResponse(url, false, HttpVerb.HTTP_POST, credentials, body, contentType, unseruce, timeoutSeconds);
	}
	
	public CloseableHttpResponse postRequest(String url, String authHeader, String body,
			String contentType, String accept, boolean unseruce, int timeoutSeconds) throws Exception {
		return getResponse(url, false, HttpVerb.HTTP_POST, authHeader, body, contentType, accept, unseruce, timeoutSeconds);
	}

	public CloseableHttpResponse deleteRequest(String url, UsernamePasswordCredentials credentials, boolean unsecure,
			int timeoutSeconds) throws Exception {
		return getResponse(url, false, HttpVerb.HTTP_DELETE, credentials, null, null, unsecure, timeoutSeconds);
	}

	public CloseableHttpResponse putRequest(String url, UsernamePasswordCredentials credentials, String body,
			String contentType, boolean unsecure, int timeoutSeconds) throws Exception {
		return getResponse(url, false, HttpVerb.HTTP_PUT, credentials, body, contentType, unsecure, timeoutSeconds);
	}

	private CloseableHttpResponse getResponse(String url, boolean closeConnection, HttpVerb httpVerb,
			UsernamePasswordCredentials credentials, String body, String contentType, boolean unsecure,
			int timeoutSeconds) throws Exception {

		String encodedAuth = Base64.getEncoder().encodeToString((credentials.getUserName() + ":" + credentials.getPassword()).getBytes());
		String authHeader = "Basic " + new String(encodedAuth);
		return getResponse(url, closeConnection, httpVerb, authHeader, body, contentType, null, unsecure, timeoutSeconds);
	}

	private CloseableHttpResponse getResponse(String url, boolean closeConnection, HttpVerb httpVerb, String authHeader, String body,
			String contentType, String accept, boolean unsecure, int timeoutSeconds) throws Exception {
		long start = System.currentTimeMillis();
		CloseableHttpResponse response = null;

		HttpUriRequest request = null;
		StringEntity entity;
		switch (httpVerb) {
		case HTTP_DELETE:
			request = new HttpDelete(url);
			break;
		case HTTP_GET:
			request = new HttpGet(url);
			break;
		case HTTP_POST:
			request = new HttpPost(url);
			entity = new StringEntity(body);			
			((HttpPost) request).setEntity(entity);
			break;
		case HTTP_PUT:
			request = new HttpPut(url);
			entity = new StringEntity(body);
			((HttpPut) request).setEntity(entity);
			break;
		default:
			break;
		}
		
		if(authHeader != "") {
			request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
		}
		request.addHeader(DcemConstants.USER_AGENT, DcemConstants.CHROME_AGENT);
		if (contentType != null) {
			request.addHeader(HttpHeaders.CONTENT_TYPE, contentType);
		}
		if(accept != null) {
			request.addHeader(HttpHeaders.ACCEPT, accept);
		}
	
		
		// adding our own TrustManager
		DcemTrustManager trustManager = new DcemTrustManager(unsecure, true);
		trustManager.addDefaultTrustManager();
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, new TrustManager[] { trustManager }, null);

		HostnameVerifier hostnameVerifier = null;
		if (unsecure == true) {
			hostnameVerifier = new HostnameVerifier() {
				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					return true;
				}
			};
		}
		if (timeoutSeconds == 0) {
			timeoutSeconds = 10;
		}
		RequestConfig config = RequestConfig.custom().setConnectTimeout(timeoutSeconds * 1000).setConnectionRequestTimeout((timeoutSeconds + 4) * 1000)
				.setSocketTimeout(timeoutSeconds * 1000).build();

		CloseableHttpClient client = null;
		if (hostnameVerifier == null) {
			client = HttpClientBuilder.create().setSslcontext(context).setDefaultRequestConfig(config).build();
		} else {
			client = HttpClientBuilder.create().setSslcontext(context).setSSLHostnameVerifier(hostnameVerifier).setDefaultRequestConfig(config).build();
		}

		response = client.execute(request);
		if (response != null && closeConnection) {
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
			sb.append(url);
			sb.append(System.lineSeparator());
			sb.append("Time (msec): ");
			sb.append(System.currentTimeMillis()- start);
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
					sb.append (":");
					sb.append(element.getMethodName());
					sb.append(System.lineSeparator());
				} else {
					break;
				}
			}
			sb.append(System.lineSeparator());
			logger.debug("REST API, " + sb.toString());
		}
		return response;
	}

	public String getResponseHeader(String url, String headerName, UsernamePasswordCredentials credentials,
			int timeoutSeconds) throws Exception {
		return getResponseHeader(url, headerName, credentials, false, timeoutSeconds);
	}

	public String getResponseHeader(String url, String headerName, UsernamePasswordCredentials credentials,
			boolean unsecure, int timeoutSeconds) throws Exception {
		CloseableHttpResponse response = getResponse(url, true, HttpVerb.HTTP_GET, credentials, null, null, unsecure,
				timeoutSeconds);
		if (response != null) {
			Header header = response.getFirstHeader(headerName);
			return (header != null) ? header.getValue() : null;
		}
		return null;
	}

	public String getResponseBody(String url, UsernamePasswordCredentials credentials, int timeoutSeconds)
			throws Exception {
		return getResponseBody(url, credentials, false, timeoutSeconds);
	}

	public String getResponseBody(String url, String authHeader, boolean unsecure, int timeoutSeconds) throws Exception {
		
		CloseableHttpResponse response = getResponse(url, false, HttpVerb.HTTP_GET, authHeader, null, null, "*/*", unsecure, timeoutSeconds);
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
	
	public String getResponseBody(String url, UsernamePasswordCredentials credentials, boolean unsecure,
			int timeoutSeconds) throws Exception {
		CloseableHttpResponse response = getResponse(url, false, HttpVerb.HTTP_GET, credentials, null, null, unsecure,
				timeoutSeconds);
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

	public CloseableHttpResponse simplePost(String url, String body) throws Exception{

		HttpPost request = new HttpPost(url);
		StringEntity entity = new StringEntity(body);
		request.setEntity(entity);
		request.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");
		
		DcemTrustManager trustManager = new DcemTrustManager(true, true);
		trustManager.addDefaultTrustManager();
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, new TrustManager[] { trustManager }, null);
		HostnameVerifier hostnameVerifier = new HostnameVerifier() {
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				return true;
			}
		};
		
		CloseableHttpClient client = HttpClientBuilder.create().setSSLContext(context).setSSLHostnameVerifier(hostnameVerifier).build();
		try {
			return client.execute(request);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	public boolean isTraceRestApi() {
		return traceRestApi;
	}

	public void setTraceRestApi(boolean traceRestApi) {
		this.traceRestApi = traceRestApi;
	}
}
