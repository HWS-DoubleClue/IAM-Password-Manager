package com.doubleclue.dcem.as.comm;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnOpen;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

//import org.apache.logging.LogFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.doubleclue.comm.thrift.AppToServer;
import com.doubleclue.comm.thrift.CommClientType;
import com.doubleclue.comm.thrift.ServerToApp;
import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.as.comm.thrift.transport.TServerAtoS_WsTransport;
import com.doubleclue.dcem.as.comm.thrift.transport.TServerStoA_WsTransport;
import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.as.logic.AsDeviceLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.AsTenantData;
import com.doubleclue.dcem.as.logic.AuthAppSession;
import com.doubleclue.dcem.as.logic.ReverseProxyConnection;
import com.doubleclue.dcem.as.logic.ReverseProxyReport;
import com.doubleclue.dcem.as.restapi.model.AsApiMsgStatus;
import com.doubleclue.dcem.core.as.AuthProxyListener;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.doubleclue.utils.LimitedArray;

/*
 * This endpoint is called only once
 * 
 */

@ServerEndpoint(value = "/ws/appConnection", configurator = com.doubleclue.dcem.as.comm.EndPointConfiguration.class)
public class AppWsConnection {

	public enum ProtocolType {

		JSON("json"), BINARY("binary"), COMPACT("compact");

		private String value;

		ProtocolType(String value) {
			this.value = value;
		}

		public static ProtocolType fromString(String s) {
			if (s != null && !s.isEmpty()) {
				for (ProtocolType e : ProtocolType.values()) {
					if (e.value.equalsIgnoreCase(s)) {
						return e;
					}
				}
			}
			return null;
		}

		@Override
		public String toString() {
			return String.valueOf(value);
		}
	}

	static AppWsConnection appWsConnection;

	private Logger logger;

	public AppToServerHandler appToServerhandler;
	private AppToServer.Processor<AppToServerHandler> processor;

	private ThreadLocal<AppSession> localAppSession = new ThreadLocal<>();
	private LimitedArray<ReverseProxyReport> rpReportList;

	private int port;
	private int externalPort;

	private static final String QUERY_KEY_PROTOCOL = "protocol";
	private static final String QUERY_KEY_CONNECTION_KEY = "key";

	private String connectionKey;
	private byte[] connectionKeyArray;

	public AppWsConnection() {
		try {
			appWsConnection = this;

		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	public void init(String connectionKey, byte[] connectionKeyArray) {
		try {
			logger = LogManager.getLogger(AppWsConnection.class);
			appToServerhandler = new AppToServerHandler();
			processor = new AppToServer.Processor<AppToServerHandler>(appToServerhandler);
			this.connectionKey = connectionKey;
			this.connectionKeyArray = connectionKeyArray;
			port = DcemCluster.getDcemCluster().getClusterConfig().getConnectionService(ConnectionServicesType.WEB_SOCKETS).getPort();
			Integer externalPort2 = DcemCluster.getDcemCluster().getClusterConfig().getConnectionService(ConnectionServicesType.WEB_SOCKETS).getExternalPort();
			if (externalPort2 == null) {
				externalPort = -1;
			} else {
				externalPort = externalPort2;
			}
		} catch (Exception exp) {
			exp.printStackTrace();
			logger.fatal("Couldn't initialize WebSocket", exp);
		}
	}

	public static AppWsConnection getInstance() {
		return appWsConnection;
	}

	@OnOpen
	public void onStart(Session session, EndpointConfig config) {

		session.getMaxBinaryMessageBufferSize();

		Map<String, List<String>> queryParameters = session.getRequestParameterMap();

		if ((queryParameters.containsKey(QUERY_KEY_CONNECTION_KEY) && queryParameters.get(QUERY_KEY_CONNECTION_KEY).get(0).equals(connectionKey)) == false) {

			logger.warn("Someone tries to connect with wrong Cluster-Key from " + AppWsConnection.getRemoteWsAddress(session));
			try {
				session.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, ""));
			} catch (IOException e) {
				logger.info(e);
			}
			return;
		}
		logger.debug("OnStart from: " + AppWsConnection.getRemoteWsAddress(session));

		ProtocolType protocolType = ProtocolType.JSON;
		if (queryParameters.containsKey(QUERY_KEY_PROTOCOL)) {
			protocolType = ProtocolType.fromString(queryParameters.get(QUERY_KEY_PROTOCOL).get(0));
			if (protocolType == null) {
				protocolType = ProtocolType.JSON;
			}
		}

		int receivePort = session.getRequestURI().getPort();
		if (receivePort == -1) {
			receivePort = 443; // set default port
		}

		if (receivePort != port && receivePort != externalPort) {
			logger.error("Access to Web-Socket with wrong Port, Configured Port=" + port + " , received Port=" + session.getRequestURI().getPort() + " from: "
					+ AppWsConnection.getRemoteWsAddress(session));
			try {
				session.close(new CloseReason(CloseCodes.CANNOT_ACCEPT, "Wrong Port " + receivePort));
			} catch (IOException e) {
				logger.info(e);
			}
			return;
		}

		WsMsgHandler wsMsgHandler = new WsMsgHandler();
		wsMsgHandler.setSession(session);
		session.addMessageHandler(wsMsgHandler);
		start(wsMsgHandler, connectionKeyArray, protocolType);
	}

	/**
	 * @param wsSession
	 */
	public void start(WsSessionI wsSession, byte[] passwordEncryptionKey, ProtocolType protocolType) {

		TTransport transportAtoS = new TServerAtoS_WsTransport();
		TProtocol protocolAtoS;

		TTransport transportStoA = new TServerStoA_WsTransport();
		TProtocol protocolStoA;

		switch (protocolType) {
		case BINARY:
			protocolAtoS = new TBinaryProtocol(transportAtoS);
			protocolStoA = new TBinaryProtocol(transportStoA);
			break;
		case COMPACT:
			protocolAtoS = new TCompactProtocol(transportAtoS);
			protocolStoA = new TCompactProtocol(transportStoA);
			break;
		default:
			protocolAtoS = new TJSONProtocol(transportAtoS);
			protocolStoA = new TJSONProtocol(transportStoA);
			break;
		}

		AppSession appSession = new AppSession(wsSession, new ServerToApp.Client(protocolStoA), protocolAtoS);
		appSession.passwordEncryptionKey = passwordEncryptionKey;
		// appSessions.put(session.getId(), appSession);
		wsSession.setAppSession(appSession);
		wsSession.setProtocolAtoS(protocolAtoS);
		if (wsSession.getTenantName() != null) {
			DcemApplicationBean applicationBean = CdiUtils.getReference(DcemApplicationBean.class);
			TenantEntity tenantEntity = applicationBean.getTenant(wsSession.getTenantName());
			if (tenantEntity == null) {
				logger.info("RP Invalid tenant name: " + wsSession.getTenantName());
				appSession.setState(ConnectionState.invalidTenant);
			}
			appSession.setTenantEntity(tenantEntity);
		}
		try {
			transportStoA.open();
			transportAtoS.open();
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		((TServerAtoS_WsTransport) transportAtoS).setWsSession(wsSession);
		((TServerStoA_WsTransport) transportStoA).setWsSession(wsSession);

		wsSession.setMaxIdleTimeout(60 * 1000); // 1 minute idle time
		// InetSocketAddress inetSocketAddress =
		// getRemoteAddress(wsSession.getSession());
		// if (inetSocketAddress != null) {
		// appSession.setRemoteAddress(inetSocketAddress.toString());
		// }
		if (logger.isDebugEnabled()) {
			wsSession.setMaxIdleTimeout(60 * 10000); // 10 minute idle time
			logger.debug("Connection from " + wsSession.getRemoteAddress() + ", SessionId=" + wsSession.getSessionId());
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {

		String closeReasonPhrase = null;
		if (closeReason != null) {
			closeReasonPhrase = closeReason.getReasonPhrase();
		}
		logger.debug("Connection onClose (" + session.getId() + "), Reason: " + closeReasonPhrase);
		Iterator<MessageHandler> iter = session.getMessageHandlers().iterator();
		if (iter.hasNext()) {
			WsMsgHandler wsMsgHandler = (WsMsgHandler) iter.next();
			AppSession appSession = wsMsgHandler.getAppSession();
			if (appSession != null && appSession.getCommClientType() != null) {
				switch (appSession.getCommClientType()) {
				case DCEM_AS_CLIENT:
					AppWsConnection.getInstance().addRpReport(new ReverseProxyReport(appSession.getDomainName(), "Received Close Session", ReverseProxyReport.RESULT_OK,
							appSession.getDomainName() + " - " + closeReasonPhrase, appSession.wsSession.getRemoteAddress()));
					removeReverseProxyConnection(appSession, closeReason);
					break;

				default:
					break;
				}
				closing(wsMsgHandler.appSession, closeReason);
			}
		}
		try {
			// Set<MessageHandler> handlers = session.getMessageHandlers();
			// for (MessageHandler handler : handlers) {
			// session.removeMessageHandler(handler);
			// }
			session.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// public void closeReverseProxyApp (AppSession appSession) {
	// try {
	// appSession.wsSession.close(null);
	// reverseProxySessions.remove(appSession.getSession().getSessionId());
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

	public void closing(AppSession appSession, CloseReason closeReason) {
		if (appSession != null) {
			if (logger.isDebugEnabled()) {
				String userDeviceId = "null";
				if (appSession.getDevice() != null) {
					userDeviceId = appSession.getDevice().getUser().getLoginId() + ":" + appSession.getDevice().getName();
				}
				logger.debug("Web Socket OnClose: User:Device=" + userDeviceId + ", State: " + appSession.state);
			}
			appSession.protocolAtoS.getTransport().close();
			appSession.serverToApp.getOutputProtocol().getTransport().close();
			AsModule asModule = CdiUtils.getReference(AsModule.class);
 			TenantIdResolver.setCurrentTenant(appSession.tenantEntity);
			AsTenantData tenantData = asModule.getTenantData();
			if (appSession.state == ConnectionState.rpClientPassThrough || appSession.state == ConnectionState.rpClientDisconnected) {
				try {
					appSession.getWsSession().close(closeReason);
				} catch (IOException e) {
					logger.warn("Could not send close to dcem-domain");
				}
				ReverseProxyConnections.removeSubSession(appSession.getDomainName(), appSession.getWsSession().getSessionId());
			} else {

				WeldRequestContext requestContext = null;
				try {
					requestContext = WeldContextUtils.activateRequestContext();
					if (appSession.state == ConnectionState.messagePending) {
						AsMessageHandler asMessageHandler = CdiUtils.getReference(AsMessageHandler.class);
						asMessageHandler.disconnectedPendingMsg(appSession, AsApiMsgStatus.DISCONNECTED);
					}
					if (appSession.getCommClientType() == CommClientType.AUTH_APP) {
						AuthAppSession authAppSession = tenantData.getAuthAppSessions().get(appSession.getWsSession().getSessionId());
						if (authAppSession != null && authAppSession.getAuthProxyListeners() != null) {
							for (AuthProxyListener authProxyListener : authAppSession.getAuthProxyListeners().values()) {
								authProxyListener.getDomainEntity().setVersion(-1);
							}
							if (authAppSession.getMessageId() > 0) {
								DcemReportingLogic reportingLogic = CdiUtils.getReference(DcemReportingLogic.class);
								DcemReporting asReporting = new DcemReporting(getAppName(authAppSession.getPolicyAppEntity()), ReportAction.Authenticate_push, authAppSession.getDcemUserDummy(),
										"Disconnected", null, "From: " + authAppSession.getWorkStation(),AlertSeverity.OK);
								reportingLogic.addReporting(asReporting);
							}
						}
					} else {
						if (appSession.state != ConnectionState.disconnected) {
							AsDeviceLogic deviceLogic = CdiUtils.getReference(AsDeviceLogic.class);
							deviceLogic.setDeviceOff(appSession.getDevice());
						}
					}
					appSession.state = ConnectionState.disconnected;

				} catch (Exception e) {
					logger.debug("", e);
				} finally {
					WeldContextUtils.deactivateRequestContext(requestContext);
				}
			}
			
			AppSession currentDeviceSession = tenantData.getDeviceSessions().get(appSession.deviceId);
			if (currentDeviceSession != null && currentDeviceSession.getWsSession().getSessionId().equals(appSession.getWsSession().getSessionId())) {
				tenantData.getDeviceSessions().remove(appSession.deviceId);
			}
			
			if (appSession.getCommClientType() == CommClientType.AUTH_APP) {
				tenantData.getAuthAppSessions().remove(appSession.getWsSession().getSessionId());
			}
			appSession = null;
		}
	}

	private String getAppName(PolicyAppEntity appEntity) {
		return appEntity.getSubName() != null ? appEntity.getSubName() : appEntity.getAuthApplication().name();
	}

	@OnError
	public void onError(Session session, Throwable t) throws Throwable {
		String expMsg = "";
		if (t != null) {
			expMsg = t.getMessage();
			logger.debug("WS onError: " + t.toString());
		}
		AppSession appSession = getAppSession();
		if (appSession != null && (appSession.state == ConnectionState.loggedIn || appSession.state == ConnectionState.rpDcemLoggedIn)) {
			logger.info("Web Socket OnError: " + t.toString() + ", SessionId=" + session.getId() + ", " + expMsg);
		}
		WsMsgHandler wsMsgHandler = (WsMsgHandler) session.getMessageHandlers().iterator().next();
		closing(wsMsgHandler.appSession, new CloseReason(CloseCodes.UNEXPECTED_CONDITION, expMsg));
	}

	/**
	 * @param appSession
	 * @param closeReason
	 */
	public void closeSession(AppSession appSession, CloseReason closeReason) {
		if (appSession == null) {
			appSession = getAppSession();
		}
		if (appSession != null) {
			try {
				if (closeReason == null) {
					closeReason = new CloseReason(CloseCodes.NORMAL_CLOSURE, "");
				}
				appSession.wsSession.close(closeReason);
			} catch (IOException e) {

			}
		}
		if (appSession != null) {
			closing(appSession, closeReason);
		}

	}

	public AppSession getAppSession() {
		AppSession appSession = localAppSession.get();
		if (appSession == null) {
			return null;
		}
		return appSession;
	}

	public AppToServer.Processor<AppToServerHandler> getProcessor() {
		return processor;
	}

	public ThreadLocal<AppSession> getLocalAppSession() {
		return localAppSession;
	}

	// public void addDeviceSession(int id, AppSession appSession) {
	// deviceSessions.put(id, appSession);
	// }
	//
	// public void removeDeviceSession(int id) {
	// deviceSessions.remove(id);
	// }
	//
	// public AppSession getDeviceSession(int id) {
	// return deviceSessions.get(id);
	// }

	// public int getConnectedSessions() {
	// return deviceSessions.size();
	// }

	/**
	 * @param appSession
	 */
	public void removeReverseProxyConnection(AppSession appSession, CloseReason closeReason) {
		ReverseProxyConnection reverseProxyConnection = ReverseProxyConnections.remove(appSession.getDomainName());
		if (reverseProxyConnection == null) {
			return;
		}
		ConcurrentHashMap<String, AppSession> subSessions = reverseProxyConnection.getSubSessions();
		if (subSessions == null) {
			return;
		}

		for (AppSession subAppSession : subSessions.values()) {
			try {
				subAppSession.getWsSession().close(closeReason);
			} catch (IOException e) {
				logger.info("Couldn't Close subSession: " + appSession.getDomainName() + " " + appSession.getWsSession().getSessionId(), e);
			}
		}
	}

	// public ReverseProxyConnection getReverseProxyConnection(String domainName) {
	// return ReverseProxyConnections.get(domainName);
	// }

	//
	// public void setReverseProxySessions(ConcurrentHashMap<String, AppSession>
	// reverseProxySessions) {
	// this.reverseProxySessions = reverseProxySessions;
	// }

	public void addRpReport(ReverseProxyReport rpReport) {
		if (rpReportList == null) {
			return;
		}
		rpReportList.add(rpReport);
	}

	public List<ReverseProxyReport> getRpReportList() {
		return rpReportList;
	}

	public void createRpReportList(int limit) {
		this.rpReportList = new LimitedArray<>(limit);
	}

	// public ConcurrentHashMap<String, ReverseProxyConnection>
	// getReverseProxyConnections() {
	// return reverseProxyConnections;
	// }

	public static InetSocketAddress getRemoteWsAddress(Session session) {
		if (session == null) {
			return null;
		}
		try {
			Async async = session.getAsyncRemote();

			InetSocketAddress addr = (InetSocketAddress) DcemUtils.getFieldInstance(async, "base#socketWrapper#socket#sc#remoteAddress");
			return addr;
		} catch (Throwable e) {
			return null;
		}
	}

	// public void addAuthAppSession(String sessionId, AuthAppSession
	// authAppSession) {
	// authAppSessions.put(sessionId, authAppSession);
	// }
	//
	// public AuthAppSession getAuthAppSession(String sessionId) {
	// return authAppSessions.get(sessionId);
	// }
	//
	// public AuthAppSession removeAuthAppSession(String sessionId) {
	// return authAppSessions.remove(sessionId);
	// }

}
