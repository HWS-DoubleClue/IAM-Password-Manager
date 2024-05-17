package com.doubleclue.dcem.core.utils.rest;

import java.io.IOException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
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
import com.doubleclue.dcem.core.utils.DcemTrustManager;
import com.doubleclue.dcem.core.utils.KeyValuePair;

@ApplicationScoped
@Named("clientRestApi")
public class ClientRestApi {

	boolean traceRestApi;

	private Logger logger = LogManager.getLogger(ClientRestApi.class);
		
	public CloseableHttpResponse restGet (ClientRestApiParams clientRestApiParams) throws Exception {
		HttpUriRequest request  = new HttpGet(clientRestApiParams.url);
		return getResponse(request, clientRestApiParams);
	}
	
	public CloseableHttpResponse restDelete (ClientRestApiParams clientRestApiParams) throws Exception {
		HttpUriRequest request  = new HttpDelete(clientRestApiParams.url);
		return getResponse(request, clientRestApiParams);
	}
	
	public CloseableHttpResponse restPost (ClientRestApiParams clientRestApiParams, String requestBody) throws Exception {
		HttpPost request  = new HttpPost(clientRestApiParams.url);
		request.setEntity(new StringEntity(requestBody, "UTF-8"));
		return getResponse(request, clientRestApiParams);
	}
	
	public CloseableHttpResponse restPut (ClientRestApiParams clientRestApiParams, String requestBody) throws Exception {
		HttpPut request  = new HttpPut(clientRestApiParams.url);
		request.setEntity(new StringEntity(requestBody, "UTF-8"));
		return getResponse(request, clientRestApiParams);
	}
	
	public CloseableHttpResponse restPatch (ClientRestApiParams clientRestApiParams, String requestBody) throws Exception {
		HttpPatch request  = new HttpPatch(clientRestApiParams.url);
		request.setEntity(new StringEntity(requestBody, "UTF-8"));
		return getResponse(request, clientRestApiParams);
	}
	
	private CloseableHttpResponse getResponse(HttpUriRequest request, ClientRestApiParams clientRestApiParams) throws Exception {
		long start = System.currentTimeMillis();
		CloseableHttpResponse response = null;
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
				if (keyValuePair.getValue() == null) {
					request.removeHeaders(keyValuePair.getKey());
				} else {
					request.addHeader(keyValuePair.getKey(), keyValuePair.getValue());
				}
			}
		}
		// adding our own TrustManager
		DcemTrustManager trustManager = new DcemTrustManager(clientRestApiParams.unsecure, false);
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
		clientRestApiParams.setResponseCode(response.getStatusLine().getStatusCode());
		try {
			clientRestApiParams.setResponseBody(EntityUtils.toString(response.getEntity()));
		} catch (Exception exp) {
			throw exp;
		}
		clientRestApiParams.responseTime = System.currentTimeMillis() - start;
		if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299) {
			throw new RestApiStatusException (response.getStatusLine().getStatusCode(), clientRestApiParams.getResponseBody());
		}
		return response;
	}

	public String getResponseHeader(ClientRestApiParams apiParams, String headerName) throws Exception {
		CloseableHttpResponse response = restGet(apiParams);
		if (response != null) {
			Header header = response.getFirstHeader(headerName);
			return (header != null) ? header.getValue() : null;
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
