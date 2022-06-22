package com.doubleclue.dcem.as.comm.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.doubleclue.app.ws.api.WebSocketApiListener;
import com.doubleclue.comm.thrift.AppErrorCodes;
import com.doubleclue.comm.thrift.AppException;
import com.doubleclue.comm.thrift.AppToServer;
import com.doubleclue.comm.thrift.AppVersion;
import com.doubleclue.comm.thrift.CommClientType;
import com.doubleclue.comm.thrift.LoginParam;
import com.doubleclue.comm.thrift.LoginResponse;
import com.doubleclue.comm.thrift.SdkSettings;
import com.doubleclue.comm.thrift.ServerSignatureParam;
import com.doubleclue.comm.thrift.ServerSignatureResponse;
import com.doubleclue.comm.thrift.SignatureParam;
import com.doubleclue.comm.thrift.SignatureResponse;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.RpConfig;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.utils.KaraException;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.ProductVersion;
import com.doubleclue.utils.RandomUtils;
import com.doubleclue.utils.SdkConfigDcem;
import com.doubleclue.utils.SecureUtils;

@ApplicationScoped
public class ProxyCommClient implements WebSocketApiListener {

	private static final Logger logger = LogManager.getLogger(ProxyCommClient.class);

	static final int MAX_REPORT = 500;

	public static final String SERVER_BUSY = "ServerBusy";

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	AsModule asModule;

	@Inject
	SystemModule systemModule;

	@Inject
	TaskExecutor taskExecutor;

	@Inject
	ConfigLogic configLogic;

	private AppVersion appVersion;
	
	SdkSettings sdkSettings;

	// private ProductVersion libVersion;
	String osVersion;

	private TTransport transport;

	AppToServer.Client appToServerClient;

	TyrusClientWebsocket tyrusClientWebsocket;
	// private WebSocketApi webSocketApi;
	private SdkConfigDcem sdkConfigDcem;

	public SdkConfigDcem getSdkConfigDcem() {
		return sdkConfigDcem;
	}

	private int keepAliveSeconds = 120;

	ReverseProxyState state = ReverseProxyState.No_Configuration;

	List<RpReport> reportList = new ArrayList<>(MAX_REPORT);

	RpConfig rpConfig;

	public ProxyCommClient() {

	}

	public void stop() {
		
		if (tyrusClientWebsocket != null) {
			tyrusClientWebsocket.stopPingFutureFeature();
			try {
				addReport(new RpReport(RpClientAction.Stopped, true, null));
				tyrusClientWebsocket.close((String) null);
				if (rpConfig.isEnableRp()) {
					state = ReverseProxyState.Initialized;
				} else {
					state = ReverseProxyState.No_Configuration;
				}
			} catch (IOException e) {
			}
		}
	}

	public void start() {

		state = ReverseProxyState.Initialized;
		try {
			if (initiate() == false) {
				state = ReverseProxyState.Disabled;
				return;
			}

		} catch (Exception exp) {
			logger.error("ReverseProxy initiate failed", exp);
			addReport(new RpReport(RpClientAction.Initialize, false, exp.toString()));
			asModule.updateReverseProxy(false, rpConfig);
			return;
		}
		state = ReverseProxyState.Initialized;

		addReport(new RpReport(RpClientAction.Initialize, true,
				"URL: " + sdkConfigDcem.getSdkConfig().getServerUrl() + ", Using Http-Proxy: " + withProxy()));
		try {
			connect();
		} catch (Exception exp) {
			logger.error("ReverseProxy connect failed. " + exp.getMessage());
			addReport(new RpReport(RpClientAction.Connect, false,
					exp.toString() + ", try to reconnect in " + rpConfig.getReconnect() + " minute/s"));
			asModule.updateReverseProxy(false, rpConfig);
			return;
		}
		state = ReverseProxyState.Connected;
		addReport(new RpReport(RpClientAction.Connect, true, null));
		try {
			serverSignature();
		} catch (AsException exp) {
			logger.error("ReverseProxy Server Signature failed" + exp.getMessage());
			addReport(new RpReport(RpClientAction.LogIn, false, "Server-Signature failed. Cause: " + exp.toString()));
			asModule.updateReverseProxy(false, rpConfig);
			return;
		}
		try {
			login();
		} catch (AppException e2) {
			logger.warn("login failed: " + e2.getError(), e2);
			// asException = new AsException(AsErrorCodes.SERVER_RESPONSE_ERROR, null, e2);
			addReport(new RpReport(RpClientAction.LogIn, false, "Login failed. Cause: " + e2.toString()));
			asModule.updateReverseProxy(false, rpConfig);
			return;
		} catch (TException e2) {
			logger.warn("login failed", e2);
			if (e2.getMessage() != null && e2.getMessage().equals(SERVER_BUSY)) {
				logger.warn("Login failed - Server Busy");
				addReport(new RpReport(RpClientAction.LogIn, false, "Server-Busy: "));
			} else {
				logger.warn("Login failed - Connection failed");
				addReport(new RpReport(RpClientAction.LogIn, false, e2.toString()));
			}
			asModule.updateReverseProxy(false, rpConfig);
			return;
		} catch (Exception exp) {
			logger.error("ReverseProxy Login failed", exp);
			asModule.updateReverseProxy(false, rpConfig);
			addReport(new RpReport(RpClientAction.LogIn, false, exp.toString()));
			return;
		}
		state = ReverseProxyState.LoggedIn;
		tyrusClientWebsocket.setKeepAliveSeconds(keepAliveSeconds);
		tyrusClientWebsocket.session.setMaxIdleTimeout((keepAliveSeconds + 10) * 1000);
		tyrusClientWebsocket.setPassThroughMode();
		tyrusClientWebsocket.startPingScheduler(keepAliveSeconds);
		addReport(new RpReport(RpClientAction.LogIn, true, null));
	}
	
	


	/**
	 * @throws DcemException
	 */
	public boolean initiate() throws DcemException {

		if (rpConfig.isEnableRp() == false) {
			return false;
		}

		try {
			sdkConfigDcem = KaraUtils.parseSdkConfig(rpConfig.getSdkConfigContent());
		} catch (KaraException e1) {
			throw new DcemException(DcemErrorCodes.INVALID_SDK_CONFIG, null, e1);
		}
		ProductVersion productVersion = applicationBean.getProductVersion();
		appVersion = new AppVersion(productVersion.getVersionInt(), productVersion.getAppName(),
				productVersion.getState());
		osVersion = System.getProperty("os.version");

		tyrusClientWebsocket = new TyrusClientWebsocket();

		try {
			tyrusClientWebsocket.init(this,
					new URI(sdkConfigDcem.getSdkConfig().getServerUrl() + "?key="
							+ new String(sdkConfigDcem.getConnectionKey(), DcemConstants.CHARSET_UTF8)),
					sdkConfigDcem.getTrustCertsBytes(), 10 * 1000, sdkConfigDcem.getConnectionKey(), taskExecutor);
		} catch (Exception e1) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, null, e1);
		}

		transport = new TClientAtoS_WsTransport(tyrusClientWebsocket);
		TProtocol tProtocol = new TJSONProtocol(transport);
		appToServerClient = new AppToServer.Client(tProtocol);
		return true;
	}

	/**
	 * @return
	 * @throws TTransportException
	 */
	boolean connect() throws Exception {

		if (transport.isOpen()) {
			return false;
		}
		try {
			transport.open();
		} catch (TException exp) {
			Throwable throwable = KaraUtils.getRootCause(exp, "SSL");
			if (throwable != null) {
				logger.info("SSL ERROR Received.", throwable);
				if (throwable.getMessage() != null && throwable.getMessage().indexOf("Certificate not trusted") > 0) {
					throw new AsException(AsErrorCodes.SECURE_CONNECTION_UNTRUSTED, throwable.getMessage(), throwable);
				}
				if (throwable.getCause() != null && throwable.getCause() instanceof SSLPeerUnverifiedException) {
					throw new AsException(AsErrorCodes.SECURE_CONNECTION_INVALID_HOST_NAME, throwable.getMessage(), throwable);
				}
				throw new AsException(AsErrorCodes.SECURE_CONNECTION_FAIL, throwable.getMessage(), throwable);
			}
			throw new AsException(AsErrorCodes.CONNECTION_FAIL, exp.getMessage(), exp);
		}
		return true;
	}

	String withProxy() {
		int port = systemModule.getPreferences().getHttpProxyPort();

		String host = systemModule.getPreferences().getHttpProxyHost();
		if (port == 0 || host.isEmpty()) {
			return "no";
		} else {
			return host + ":" + port;
		}
	}

	protected void login() throws UnsupportedEncodingException, Exception {
		LoginResponse loginResponse;
		LoginParam loginParam = new LoginParam();

		loginParam.setDeviceId(rpConfig.getReverseProxyProperties().deviceId);
		String reverseProxyPassword = rpConfig.getPassword();

		loginParam.setLocale(Locale.getDefault().getLanguage());
		byte[] encPassword = SecureUtils.encryptData(sdkConfigDcem.getConnectionKey(),
				reverseProxyPassword.getBytes(DcemConstants.CHARSET_UTF8));
		loginParam.setEncPassword(encPassword);
		loginParam.setUdid(rpConfig.getReverseProxyProperties().udid);
		loginParam.setCommClientType(CommClientType.DCEM_AS_CLIENT);
		loginResponse = appToServerClient.login(loginParam);
		SignatureResponse signatureResponse = clientSignature(loginResponse);
		keepAliveSeconds = signatureResponse.getKeepAliveSeconds();

	}

	/**
	 * @param appErrorCodes
	 * @param message
	 */
	public void disconnect(AppErrorCodes appErrorCodes, String message) {
		// TODO
	}

	

	/**
	 * @throws AppException
	 * @throws TException
	 * @throws AsException
	 */
	public ServerSignatureResponse serverSignature() throws AsException {
		ServerSignatureParam serverSignatureParam = new ServerSignatureParam();
		byte[] dataForSignature = RandomUtils.getRandom(32);
		serverSignatureParam.setDataForSignature(dataForSignature);
		serverSignatureParam.setAppVersion(appVersion);
		serverSignatureParam.setLibVersion(appVersion);
		serverSignatureParam.setCommClientType(CommClientType.DCEM_AS_CLIENT);
		serverSignatureParam.setDomainName(rpConfig.getReverseProxyProperties().domainName);
		ServerSignatureResponse serverSignatureResponse = null;
		byte[] serverPublicKey;
		try {
			serverPublicKey = SecureServerUtils.decryptDataCommon(sdkConfigDcem.getSdkConfig().getServerPublicKey());
		} catch (Exception e) {
			throw new AsException(AsErrorCodes.INVALID_SDK_CONFIG, "Couln't decrept SDK Config");
		}
		byte[] signature;
		boolean validSignature;
		try {
			serverSignatureResponse = appToServerClient.serverSignature(serverSignatureParam);
			signature = serverSignatureResponse.getServerSignature();
			validSignature = SecureUtils.isVerifySignature(SecureUtils.loadPublicKey(serverPublicKey), dataForSignature,
					signature);

		} catch (AppException exp) {
			throw new AsException(AsErrorCodes.SERVER_RESPONSE_ERROR, null, exp);
		} catch (TException e2) {
			throw new AsException(AsErrorCodes.CONNECTION_FAIL, null, e2);
		} catch (Exception exp) {
			throw new AsException(AsErrorCodes.GENERIC, null, exp);
		}

		if (!validSignature) {
			throw new AsException(AsErrorCodes.WRONG_SERVER_SIGNATURE,
					"Please check that you have the right SdkConfig");
		}

		return serverSignatureResponse;
	}

	/**
	 * @param loginResponse
	 * @return
	 * @throws AsException
	 * @throws TException
	 */
	SignatureResponse clientSignature(LoginResponse loginResponse) throws AsException, TException {

		SignatureParam signatureParam = new SignatureParam();
		byte[] appDigest = new byte[] { 0x1, 0x2, 0x3 }; // dummy
		signatureParam.setAppDigest(appDigest);

		byte[] privateKeyArray;
		PrivateKey privateKey;
		try {
			privateKeyArray = SecureServerUtils.decryptData(loginResponse.getDeviceKey(),
					rpConfig.getReverseProxyProperties().getEncryptedPv());
		} catch (Exception exp) {
			throw new AsException(AsErrorCodes.GENERIC, "decryption failure: " + exp.getMessage());
		}
		byte[] signature;
		try {
			privateKey = SecureServerUtils.loadPrivateKey(privateKeyArray);
			signature = SecureServerUtils.sign(privateKey, loginResponse.getOneTimePassword().getBytes("UTF-8"));
		} catch (Exception exp) {
			throw new AsException(AsErrorCodes.GENERIC, "Client signing failed: " + exp.getMessage());
		}
		signatureParam.setClientSignature(signature);
		return appToServerClient.clientSignature(signatureParam);
	}

	public ReverseProxyState getState() {
		return state;
	}

	public void setState(ReverseProxyState state) {
		this.state = state;
	}

	@Override
	public void onReceive(byte[] packet) {
		// ignore this
	}

	@Override
	public void onCloseConnection(String reason) {
		logger.error("Connection to ReverseProxy Close, reason: " + reason);
		if (tyrusClientWebsocket != null) {
			tyrusClientWebsocket.stopPingFutureFeature();
		}
		try {
			transport.close();
		} catch (Exception e) {

		}
		
		state = ReverseProxyState.Disconnected;
		addReport(new RpReport(RpClientAction.Disconnect_Received, true, "Close received, reason: " + reason));
		asModule.updateReverseProxy(false, rpConfig);
	}

	public void close(String reason) {
		logger.error("Connection to ReverseProxy Close, reason: " + reason);
		if (tyrusClientWebsocket != null) {
			tyrusClientWebsocket.stopPingFutureFeature();
		}
		try {
			transport.close();
		} catch (Exception e) {

		}
		state = ReverseProxyState.Disconnected;
		addReport(new RpReport(RpClientAction.Disconnect_Received, true, "Close received, reason: " + reason));
		asModule.updateReverseProxy(false, rpConfig);
	}

	

	public void addReport(RpReport rpReport) {
		if (reportList.size() == MAX_REPORT) {
			reportList.remove(0);
		}
		reportList.add(rpReport);
	}

	public List<RpReport> getReportList() {
		return reportList;
	}

	public void setReportList(List<RpReport> reportList) {
		this.reportList = reportList;
	}

	public void clearReportList() {
		this.reportList.clear();
	}

	public RpConfig getRpConfig() {
		return rpConfig;
	}

	public void setRpConfig(RpConfig rpConfig) {
		this.rpConfig = rpConfig;
	}

	public int getConnections() {
		if (tyrusClientWebsocket == null) {
			return 0;
		}
		return tyrusClientWebsocket.getSessionMap().size();
	}

}