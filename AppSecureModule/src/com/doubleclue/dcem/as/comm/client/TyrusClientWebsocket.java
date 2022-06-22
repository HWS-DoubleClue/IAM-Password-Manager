package com.doubleclue.dcem.as.comm.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.ContainerProvider;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.transport.TTransportException;

import com.doubleclue.app.ws.api.WebSocketApiListener;
import com.doubleclue.dcem.as.comm.AppSession;
import com.doubleclue.dcem.as.comm.AppWsConnection;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.utils.DcemTrustManager;

public class TyrusClientWebsocket extends Endpoint {

	private static Logger logger = LogManager.getLogger(TyrusClientWebsocket.class);

	// TClientAtoS_WS_Transport clientWebSocketTransport;
	Session session;
	TyrusClientMsgHandler clientMsgHandler;
	URI uri;
	int appToServerCount;
	int serverToAppCount;
	String trustStorePassword;
	WebSocketApiListener listener;
	String[] supportedCiphers;
	int connectionTimeout;

	int keepAliveSeconds;
	long lastPacketSend;
	long lastPingSend;

	static ScheduledFuture<?> pingProxySchedule;

	TaskExecutor taskExecutor;

	SSLContext context;

	boolean passThroughMode = false;

	private CountDownLatch countDownLatch;

	private ConcurrentHashMap<String, WsSessionRp> sessionMap = new ConcurrentHashMap<>();

	Throwable onErrorThrowable;

	byte[] passwordEnryptionKey;

	public TyrusClientWebsocket() {

	}

	public void init(WebSocketApiListener listener, URI uri, List<byte[]> certList, int connectionTimeout,
			byte[] passwordEnryptionKey, TaskExecutor taskExecutor) throws Exception {

		this.uri = uri;
		this.connectionTimeout = connectionTimeout;
		this.listener = listener;
		// this.supportedCiphers = supportedCiphers;
		X509Certificate[] certs = new X509Certificate[certList.size()];
		if (certList != null && certList.isEmpty() == false) {
			int i = 0;
			for (byte[] cert : certList) {
				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				java.security.cert.Certificate xCert = cf.generateCertificate(new ByteArrayInputStream(cert));
				// builderPinner.add(uri.getHost(), CertificatePinner.pin(xCert));
				certs[i++] = (X509Certificate) xCert;
			}
		}
		DcemTrustManager trustManager = new DcemTrustManager(certs);
		context = SSLContext.getInstance("TLS");
		context.init(null, new TrustManager[] { trustManager }, null);
		this.passwordEnryptionKey = passwordEnryptionKey;
		this.taskExecutor = taskExecutor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.doubleclue.app.ws.api.WebSocketApi#connect()
	 */
	public void connect() throws Throwable {
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();

		// ClientManager clientManager = ClientManager.createClient();
		// clientManager.getProperties();
		ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create().build();
		clientEndpointConfig.getUserProperties().put("org.apache.tomcat.websocket.SSL_CONTEXT", context);
		clientMsgHandler = null;
		onErrorThrowable = null;
		countDownLatch = new CountDownLatch(1);

		passThroughMode = false;
		session = container.connectToServer(this, clientEndpointConfig, uri);
		
		countDownLatch.await(connectionTimeout + 1000, TimeUnit.MILLISECONDS);
		if (clientMsgHandler == null) {
			if (onErrorThrowable != null) {
				throw onErrorThrowable;
			}
			throw new Exception("WebSocket onError, cause unknown");
		}

		countDownLatch = null;
		logger.info("Connected to: " + uri.toString());
		lastPacketSend = System.currentTimeMillis();
	}

	/**
	 * Callback hook for Connection open events.
	 *
	 * the userSession which is opened.
	 */
	@OnOpen
	public void onOpen(Session session, EndpointConfig config) {
		this.session = session;
		clientMsgHandler = new TyrusClientMsgHandler(this);
		session.addMessageHandler(clientMsgHandler);
		session.setMaxIdleTimeout(60000); // 1 minute idle time
		countDownLatch.countDown();
	}

	@OnMessage
	public void onMessage(ByteBuffer buffer) {
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {

		try {
			this.session.close();
		} catch (IOException e) {
		}
		session = null;
		if (reason.getCloseCode() != CloseCodes.GOING_AWAY) {
			logger.info("closing websocket: reason=" + reason.getCloseCode() + reason.getReasonPhrase());
			listener.onCloseConnection(reason.getReasonPhrase());
		}

		for (WsSessionRp wsSessionRp : sessionMap.values()) {
			AppSession appSession = wsSessionRp.getAppSession();
			AppWsConnection.getInstance().closing(appSession, reason);
		}
		sessionMap.clear();
	}

	@OnError
	public void onError(Session userSession, Throwable throwable) {
		logger.info("OnError: websocket: reason=", throwable);
		onErrorThrowable = throwable;
		try {
			if (session != null) {
				session.close();
			}
		} catch (IOException e) {
		}
		session = null;
		String reason = "onError";
		if (throwable != null) {
			reason = throwable.getMessage();
			throwable.printStackTrace();
		}
		if (countDownLatch != null) {
			countDownLatch.countDown();
		}
		listener.onCloseConnection(reason);
	}

	public void close(CloseReason closeReason) throws IOException {
		if (session != null) {
			session.close(closeReason);
		}
		session = null;
		passThroughMode = false;
	}

	// @Override
	// public WebSocketApiListener getListener() {
	// return listener;
	// }

	public void close(String reason) throws IOException {
		if (session != null) {
			if (reason != null) {
				session.close(new CloseReason(CloseCodes.GOING_AWAY, ""));
			} else {
				session.close();
			}
		}
		session = null;

	}

	public void send(byte[] buffer) throws IOException, Exception {
		ByteBuffer byteBuffer = ByteBuffer.allocate(buffer.length);
		byteBuffer.put(buffer);
		byteBuffer.flip();
		resetReceiveBuffer();
		session.getBasicRemote().sendBinary(byteBuffer);
	}
	//
	// public void send(byte[] buffer, long sessionId) throws IOException {
	// if (session == null) {
	// logger.warn("Session is closed");
	// return;
	// }
	// ByteBuffer byteBuffer = ByteBuffer.allocate(buffer.length);
	// byteBuffer.put(buffer);
	// byteBuffer.flip();
	// resetReceiveBuffer();
	// session.getBasicRemote().sendBinary(byteBuffer);
	// }

	public ByteBuffer waitForReceive() {
		return clientMsgHandler.waitForReceive();
	}

	public void setPassThroughMode() {
		passThroughMode = true;
	}

	public void sendBinary(ByteBuffer buffer) throws IOException {
		// TODO Auto-generated method stub

	}

	public ByteBuffer getRecieveBuffer() throws TTransportException {
		// TODO Auto-generated method stub
		return null;
	}

	public ByteBuffer getWaitForRecieveBuffer() throws TTransportException {
		// TODO Auto-generated method stub
		return null;
	}

	public void resetReceiveBuffer() {
		clientMsgHandler.resetReceiveBuffer();
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;

	}

	public void setMaxIdleTimeout(long timeout) {
		// TODO Auto-generated method stub
	}

	public void sendPing() throws IllegalArgumentException, IOException {
		if (lastPacketSend < lastPingSend) {
			setLastPacketSend();
//			logger.debug("Send Ping");
			session.getBasicRemote().sendPing(ByteBuffer.wrap("ping".getBytes()));
			startPingScheduler(keepAliveSeconds);
		} else {
			lastPingSend = System.currentTimeMillis();
			long delay = ((System.currentTimeMillis() - lastPacketSend) / 1000);
			if (delay > keepAliveSeconds) {
				delay = 0;
			}
			startPingScheduler (keepAliveSeconds - delay);
		}
		session.getBasicRemote().sendPing(ByteBuffer.wrap("ping".getBytes()));
	}
	
	public void startPingScheduler(long keepAlive) {
		lastPingSend = System.currentTimeMillis();
		if (pingProxySchedule != null) {
			pingProxySchedule.cancel(false);
		}
		// Send Ping every minute
		pingProxySchedule = taskExecutor.schedule(new PingDcemProxyTask(this), 60, TimeUnit.SECONDS);
	}

	public void stopPingFutureFeature() {
		if (pingProxySchedule != null) {
			pingProxySchedule.cancel(false);
			pingProxySchedule = null;
		}
	}

	public int getKeepAliveSeconds() {
		return keepAliveSeconds;
	}

	public void setKeepAliveSeconds(int keepAliveSeconds) {
		this.keepAliveSeconds = keepAliveSeconds;
	}

	public void setLastPacketSend() {
		lastPacketSend = System.currentTimeMillis();
	}

	public ConcurrentHashMap<String, WsSessionRp> getSessionMap() {
		return sessionMap;
	}

}
