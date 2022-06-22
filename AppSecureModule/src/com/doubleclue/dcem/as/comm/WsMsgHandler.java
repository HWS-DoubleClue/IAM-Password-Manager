package com.doubleclue.dcem.as.comm;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.MessageHandler;
import javax.websocket.Session;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransportException;

import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.comm.thrift.CommClientType;
import com.doubleclue.comm.thrift.RpOpen;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.AsUtils;
import com.doubleclue.dcem.as.logic.ReverseProxyConnection;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.utils.ByteBufferedOutputStream;
import com.doubleclue.dcem.core.utils.TraceUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.core.weld.WeldContextUtils;
import com.doubleclue.dcem.core.weld.WeldRequestContext;
import com.doubleclue.dcem.core.weld.WeldSessionContext;
import com.doubleclue.utils.ThriftUtils;

public class WsMsgHandler implements MessageHandler.Whole<ByteBuffer>, MessageHandler.Partial<ByteBuffer>, WsSessionI {

	public static Logger logger = LogManager.getLogger(WsMsgHandler.class);

	// final static int AQCUIRED_TIMEOUT = 4;

	int MAX_BUFFER_LIMIT = 1024 * 8;

	ByteBuffer receiveBufferStoA;
	ByteBuffer receiveBufferAtoS;
	protected ByteBufferedOutputStream outputStreamStoA;
	protected ByteBufferedOutputStream outputStreamAtoS;
	Object recWaiting = new Object();
	TProtocol protocolAtoS;
	protected AppSession appSession;
	protected AsModule asModule;
	protected Session session;
	private ByteBuffer fragmentedMessage = null;

	// Semaphore semaphore = new Semaphore(1);
	// SendHandler sendHandler = new SemaphoreSendHandler(semaphore);

	public WsMsgHandler() {
		asModule = CdiUtils.getReference(AsModule.class);
	}

	@Override
	public void onMessage(ByteBuffer messagePart, boolean last) {
		if (fragmentedMessage == null) {
			fragmentedMessage = messagePart;
		} else {
			ByteBuffer messageSoFar = fragmentedMessage;
			fragmentedMessage = ByteBuffer.allocate(fragmentedMessage.limit() + messagePart.limit());
			fragmentedMessage.put(messageSoFar);
			fragmentedMessage.put(messagePart);
			fragmentedMessage.flip();
		}
		if (last) {
			onMessage(fragmentedMessage);
			fragmentedMessage = null;
		}
	}

	/**
	 * Receiving message
	 * 
	 * @param buffer
	 */
	@Override
	public void onMessage(ByteBuffer buffer) {
		AppWsConnection.getInstance().getLocalAppSession().set(appSession);
		if (appSession.state == ConnectionState.rpClientOpen) {
			try {
				((WsMsgHandler) appSession.reverseProxySession.getWsSession()).reverseProxyOpen(buffer, appSession);
			} catch (Exception exp) {
				logger.warn("Something went wrong on reverseProxyOpen ", exp);
				return;
			}
			appSession.state = ConnectionState.rpClientPassThrough;
			return;
		}
		if (appSession.state == ConnectionState.rpClientPassThrough) {
			try {
				((WsMsgHandler) appSession.reverseProxySession.getWsSession()).reverseProxySendDataToDcem(buffer, appSession.getWsSession().getSessionId());
			} catch (Exception e) {
				logger.warn(e);
			}
			return;
		}

		if (buffer.get() != AppSystemConstants.PROTOCOL_VERSION) {
			AppWsConnection.getInstance().closeSession(appSession, new CloseReason(CloseCodes.PROTOCOL_ERROR, "INVALID-VERSION"));
			return;
		}

		switch (buffer.get()) {
		case AppSystemConstants.PROTOCOL_SERVER_TO_APP:
			synchronized (recWaiting) {
				receiveBufferStoA = buffer;
				if (logger.isTraceEnabled()) {
					logger.trace("Receiving S > A: Length" + TraceUtils.traceBuffer(receiveBufferStoA, true));
				}
				recWaiting.notify();
			}
			break;
		case AppSystemConstants.PROTOCOL_APP_TO_SERVER:
			TenantIdResolver.setCurrentTenant(appSession.getTenantEntity());
			receiveBufferAtoS = buffer;
			// appToServerCount = receivedBuffer.getInt(); // counter ignore
			if (logger.isTraceEnabled()) {
				logger.debug("tenant: " + appSession.getTenantEntity());
				logger.debug("Receiving A > S: " + TraceUtils.traceBuffer(receiveBufferAtoS, true));
			}
			WeldRequestContext requestContext = null;
			WeldSessionContext sessionContext = null;
			Map<String, Object> sessionStorage = new HashMap<>();
	//		long start = System.currentTimeMillis();
			try {
				requestContext = WeldContextUtils.activateRequestContext();
				sessionContext = WeldContextUtils.activateSessionContext(sessionStorage);
				AppWsConnection.getInstance().getProcessor().process(protocolAtoS, protocolAtoS);
			} catch (TException e) {
				logger.warn(e);
			} finally {
				// System.currentTimeMillis() - start);
				WeldContextUtils.deactivateSessionContext(sessionContext);
				WeldContextUtils.deactivateRequestContext(requestContext);
			}
			break;
		case AppSystemConstants.PROTOCOL_REVERSE_PROXY:
			/*
			 * Data received from ReverseProxy-DCEM
			 */
			byte command = buffer.get();
			String sessionId = AsUtils.getStringFromBuffer(buffer);
			ReverseProxyConnection rpConnection = ReverseProxyConnections.get(appSession.domainName);
			AppSession destAppSession = rpConnection.getSubSession(sessionId);
			if (destAppSession == null) {
				try {
					reverseProxyClose(appSession, null);
				} catch (Exception e) {
					logger.warn(e);
				}
				return;
			}

			switch (command) {
			case AppSystemConstants.REVERSE_PROXY_CLOSE:
				destAppSession.setState(ConnectionState.rpClientDisconnected);
				destAppSession.getWsSession().setMaxIdleTimeout(AsConstants.SESSION_TIMEOUT_AFTER_DISCONNECT);
				try {
					((WsMsgHandler) destAppSession.wsSession).reverseProxySendDataToApp(buffer);
				} catch (Exception e) {
					logger.warn(e);
				}

				break;
			case AppSystemConstants.REVERSE_PROXY_DATA:

				try {
					((WsMsgHandler) destAppSession.wsSession).reverseProxySendDataToApp(buffer);
				} catch (Exception e) {
					logger.warn(e);
				}
			}

		} // switch protocol

	}

	// private void close() {
	// synchronized (recWaiting) {
	// receiveBufferStoA = null;
	// receiveBufferAtoS = null;
	// if (logger.isDebugEnabled()) {
	// logger.debug("Closing");
	// }
	// recWaiting.notify();
	// }
	// }

	public ByteBuffer getWaitForRecieveBufferStoA() {
		synchronized (recWaiting) {
			try {
				if (receiveBufferStoA == null) {
					recWaiting.wait(AsConstants.MAX_WAIT_FOR_CLIENT_RESPONSE);
				}
			} catch (InterruptedException e) {
				logger.warn("getWaitForRecieveBufferStoA wend wrong", e);
			}
		}
		return receiveBufferStoA;
	}

	public ByteBuffer getRecieveBufferAtoS() throws TTransportException {
		return receiveBufferAtoS;
	}

	public void sendBinaryStoA() throws Exception {

		outputStreamStoA.buffer().flip();
		if (logger.isTraceEnabled()) {
			logger.trace("Sending  S > A: (" + session.getId() + ") " + TraceUtils.traceBuffer(outputStreamStoA.buffer(), false));
		}
		// System.out.println("sendBinaryStoA Start " + outputStreamStoA.buffer().limit() );
		waitAndSend(outputStreamStoA.buffer());
	}

	public void sendBinaryAtoS() throws Exception {
		outputStreamAtoS.buffer().flip();
		if (logger.isTraceEnabled()) {
			logger.trace("Sending  A > S : (" + session.getId() + ") " + TraceUtils.traceBuffer(outputStreamAtoS.buffer(), false));
		}
		waitAndSend(outputStreamAtoS.buffer());
	}

	public void reverseProxyOpen(ByteBuffer byteBuffer, AppSession sourceAppSession)
			throws IOException, InvalidKeyException, NoSuchAlgorithmException, TException, Exception {
		String remoteAddress = sourceAppSession.getWsSession().getRemoteAddress();
		String tenantName = null;
		if (sourceAppSession.getTenantEntity() != null) {
			tenantName = sourceAppSession.getTenantEntity().getName();
		}
		RpOpen rpOpen = new RpOpen(remoteAddress, sourceAppSession.getAppVersion(), sourceAppSession.getLibVersion(), tenantName);
		byte[] rpOpenData = ThriftUtils.serializeObject(rpOpen, false);
		String sessionId = sourceAppSession.getWsSession().getSessionId();
		ByteBuffer outbuffer = ByteBuffer.allocate(byteBuffer.remaining() + sessionId.length() + rpOpenData.length + 20);
		outbuffer.put(AppSystemConstants.PROTOCOL_VERSION);
		outbuffer.put(AppSystemConstants.PROTOCOL_REVERSE_PROXY);
		outbuffer.put(AppSystemConstants.REVERSE_PROXY_OPEN);

		AsUtils.putStringToBuffer(outbuffer, sessionId);

		outbuffer.putInt(rpOpenData.length);
		outbuffer.put(rpOpenData);
		outbuffer.put(byteBuffer);
		outbuffer.flip();

		ReverseProxyConnections.get(appSession.domainName).addSubSession(sourceAppSession);
		waitAndSend(outbuffer);
	}

	public void reverseProxySendDataToDcem(ByteBuffer byteBuffer, String sessionId) throws Exception {
		ByteBuffer outbuffer = ByteBuffer.allocate(byteBuffer.remaining() + sessionId.length() + 10);
		outbuffer.put(AppSystemConstants.PROTOCOL_VERSION);
		outbuffer.put(AppSystemConstants.PROTOCOL_REVERSE_PROXY);
		outbuffer.put(AppSystemConstants.REVERSE_PROXY_DATA);
		AsUtils.putStringToBuffer(outbuffer, sessionId);
		outbuffer.put(byteBuffer);
		outbuffer.flip();
		waitAndSend(outbuffer);
	}

	public void reverseProxySendDataToApp(ByteBuffer byteBuffer) throws Exception {
		if (logger.isTraceEnabled()) {
			logger.trace("Sending  Rp > APP : (" + session.getId() + ") " + TraceUtils.traceBuffer(byteBuffer, false));
		}
		byte[] data = new byte[byteBuffer.remaining()];
		byteBuffer.get(data);
		waitAndSend(ByteBuffer.wrap(data));
	}

	public void reverseProxyClose(AppSession sourceAppSession, CloseReason closeReason) throws Exception {
		String sessionId = sourceAppSession.getWsSession().getSessionId();
		String reason = "";
		if (closeReason != null) {
			reason = closeReason.getCloseCode().getCode() + "-" + closeReason.getReasonPhrase();
		}
		ByteBuffer outbuffer = ByteBuffer.allocate(sessionId.length() + reason.length() + 50);
		outbuffer.put(AppSystemConstants.PROTOCOL_VERSION);
		outbuffer.put(AppSystemConstants.PROTOCOL_REVERSE_PROXY);
		outbuffer.put(AppSystemConstants.REVERSE_PROXY_CLOSE);
		AsUtils.putStringToBuffer(outbuffer, sessionId);
		AsUtils.putStringToBuffer(outbuffer, reason);
		outbuffer.flip();
		waitAndSend(outbuffer);
	}

	public void resetReceiveBufferStoA() {
		this.receiveBufferStoA = null;
	}

	public TProtocol getProtocolAtoS() {
		return protocolAtoS;
	}

	public void setProtocolAtoS(TProtocol protocolAtoS) {
		this.protocolAtoS = protocolAtoS;
	}

	public AppSession getAppSession() {
		return appSession;
	}

	public void setAppSession(AppSession appSession) {
		this.appSession = appSession;
	}

	public void close(CloseReason reason) throws IOException {
		if (reason == null) {
			reason = new CloseReason(CloseCodes.GOING_AWAY, "");
		}
		if (appSession.state == ConnectionState.rpClientPassThrough) {
			try {
				if (appSession.reverseProxySession.getWsSession().getSession().isOpen()) {
					((WsMsgHandler) appSession.reverseProxySession.getWsSession()).reverseProxyClose(appSession, reason);
				}

			} catch (Exception e) {
				logger.info(e.toString());
				// e.printStackTrace();
			}
		} else {
			session.close(reason);
		}
	}

	public void setMaxIdleTimeout(long timeout) {
		if (session != null) {
			session.setMaxIdleTimeout(timeout);
		}
	}

	public ByteBufferedOutputStream getOutputStreamStoA() throws IOException {
		if (outputStreamStoA == null) {
			outputStreamStoA = new ByteBufferedOutputStream(AsConstants.WS_BUFFER_SIZE, 2, true);
			initializeBufferStoA();
		}
		if (outputStreamStoA.isReadyToUse()) {
			outputStreamStoA.clear();
			initializeBufferStoA();
		}
		return outputStreamStoA;
	}

	protected void initializeBufferStoA() {
		outputStreamStoA.write(AppSystemConstants.PROTOCOL_VERSION);
		outputStreamStoA.write(AppSystemConstants.PROTOCOL_SERVER_TO_APP);
	}

	public ByteBufferedOutputStream getOutputStreamAtoS() throws IOException {
		if (outputStreamAtoS == null) {
			outputStreamAtoS = new ByteBufferedOutputStream(AsConstants.WS_BUFFER_SIZE, 2, true);
		}
		if (outputStreamAtoS.buffer().position() == 0) {
			initializeBufferAtoS();
		}
		return outputStreamAtoS;
	}

	protected void initializeBufferAtoS() {
		outputStreamAtoS.write(AppSystemConstants.PROTOCOL_VERSION);
		outputStreamAtoS.write(AppSystemConstants.PROTOCOL_APP_TO_SERVER);
	}

	@Override
	public void setSession(Session session) {
		this.session = session;
	}

	@Override
	public String getRemoteAddress() {
		if (session == null) {
			return null;
		}
		if (session.isOpen() == false) {
			return "Session is closed." + " - " + session.getId();
		}
		InetSocketAddress inetSocketAddress = AppWsConnection.getRemoteWsAddress(session);
		if (inetSocketAddress != null) {
			return inetSocketAddress.getAddress().getHostAddress();
		}
		return null;
	}

	@Override
	public String getSessionId() {
		return session.getId();
	}

	public Session getSession() {
		return session;
	}

	private void waitAndSend(ByteBuffer buffer) throws InterruptedException, Exception {
		long start = System.currentTimeMillis();
		// if (semaphore.tryAcquire(AQCUIRED_TIMEOUT, TimeUnit.SECONDS) == false) {
		// throw new Exception("AQCUIRED_TIMEOUT");
		// }

		int size = buffer.limit();
		int position = 0;
		int getNext;
		boolean lastBuffer = false;
		if (size <= MAX_BUFFER_LIMIT) {
			session.getBasicRemote().sendBinary(buffer);
		} else {
			// do fragmentation
			while (position < size) {
				buffer.position(position);
				getNext = Math.min(MAX_BUFFER_LIMIT, (size - position));
				position += getNext;
				buffer.limit(position);
				if (position < size == false) {
					lastBuffer = true;
				}
				session.getBasicRemote().sendBinary(buffer, lastBuffer);
			}
		}
		// session.getAsyncRemote().sendBinary(buffer, sendHandler);
		if (appSession.getCommClientType() == CommClientType.DCEM_AS_CLIENT) {
			asModule.addCounter("Packets-Proxy", System.currentTimeMillis() - start);
		} else {
			asModule.addCounter("Packets", System.currentTimeMillis() - start);
		}
		// System.out.println("WsMsgHandler.waitAndSend() length: " + size + " time:" + (System.currentTimeMillis() -
		// start) );
	}

	/**
	* @author emanuel.galea
	* Was used for Asynch sending which caused a lot of problem
	*/
	// private class SemaphoreSendHandler implements SendHandler {
	//
	// private final Semaphore semaphore;
	//
	// private SemaphoreSendHandler(Semaphore semaphore) {
	// this.semaphore = semaphore;
	// }
	//
	// @Override
	// public void onResult(SendResult result) {
	// semaphore.release();
	// }
	// }

	@Override
	public String getTenantName() {
		return null;
	}

}
