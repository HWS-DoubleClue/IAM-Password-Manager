package com.doubleclue.dcem.as.comm.client;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCode;
import javax.websocket.CloseReason.CloseCodes;
import javax.websocket.MessageHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.comm.thrift.CommClientType;
import com.doubleclue.comm.thrift.RpOpen;
import com.doubleclue.dcem.as.comm.AppSession;
import com.doubleclue.dcem.as.comm.AppWsConnection;
import com.doubleclue.dcem.as.comm.AppWsConnection.ProtocolType;
import com.doubleclue.dcem.as.comm.ConnectionState;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsUtils;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.utils.TraceUtils;
import com.doubleclue.utils.ThriftUtils;

public class TyrusClientMsgHandler implements MessageHandler.Whole<ByteBuffer>, MessageHandler.Partial<ByteBuffer> {

	private static Logger logger = LogManager.getLogger(TyrusClientMsgHandler.class);

	TyrusClientWebsocket tyrusClientWebsocket;
	
	private ByteBuffer fragmentedMessage = null;

	public TyrusClientMsgHandler(TyrusClientWebsocket tyrusClientWebsocket) {
		this.tyrusClientWebsocket = tyrusClientWebsocket;
	}

	Object recWaiting = new Object();
	ByteBuffer receivedBuffer;
	
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
	

	@Override
	public void onMessage(ByteBuffer buffer) {
		if (buffer.get() != AppSystemConstants.PROTOCOL_VERSION) {
			logger.error("Web-Socket INVALID RECEIVED !!!");
			return;
		}
		byte protocolHead = buffer.get();
		if (protocolHead == AppSystemConstants.PROTOCOL_APP_TO_SERVER) {
			synchronized (recWaiting) {
				receivedBuffer = buffer;
				if (logger.isTraceEnabled()) {
					logger.trace("Receiving S > A: Length" + TraceUtils.traceBuffer(receivedBuffer, false));// +
				}
				recWaiting.notify();
			}
		} else if (protocolHead == AppSystemConstants.PROTOCOL_REVERSE_PROXY) {
			if (tyrusClientWebsocket.passThroughMode == true) {
				String sessionId = null;
				byte[] sessionIdArray;
				WsSessionRp wsSessionRp;
				byte command = buffer.get();
				switch (command) {

				case AppSystemConstants.REVERSE_PROXY_CLOSE:
					sessionId = AsUtils.getStringFromBuffer(buffer);
					wsSessionRp = tyrusClientWebsocket.getSessionMap().get(sessionId);
					if (wsSessionRp != null) {
						AppSession appSession = wsSessionRp.getAppSession();
						String reason = AsUtils.getStringFromBuffer(buffer);
						int ind = reason.indexOf('-');
						CloseCode closeCode = CloseCodes.NORMAL_CLOSURE;
						String reasonphrase = "";
						if (ind > 0) {
							int code = Integer.parseInt(reason.substring(0, ind));
							closeCode = CloseCodes.getCloseCode(code);
							reasonphrase = reason.substring(ind, reason.length());
						}
						CloseReason closeReason = new CloseReason(closeCode, reasonphrase);

						AppWsConnection.getInstance().closing(appSession, closeReason);
						tyrusClientWebsocket.getSessionMap().remove(sessionId);
					} else {
						logger.debug("Proxy Close received to a not existing session");
					}
					break;

				case AppSystemConstants.REVERSE_PROXY_OPEN:
					sessionIdArray = AsUtils.getBytesFromBuffer(buffer);
					wsSessionRp = new WsSessionRp(tyrusClientWebsocket, sessionIdArray);
					int length = buffer.getInt();
					byte[] rpOpenData = new byte[length];
					buffer.get(rpOpenData);
					RpOpen rpOpen = new RpOpen();
					try {
						ThriftUtils.deserializeObject(rpOpenData, rpOpen, false);
					} catch (Exception e1) {
						logger.error("Couldn't deserialize object", e1);
					}

					wsSessionRp.setRemoteAddress(rpOpen.getRemoteAddress());

					try {
						sessionId = new String(sessionIdArray, DcemConstants.CHARSET_UTF8);
					} catch (UnsupportedEncodingException e) {
					}
					wsSessionRp.setTenantName(rpOpen.tenantName);
					tyrusClientWebsocket.getSessionMap().put(sessionId, wsSessionRp);
					AppWsConnection.getInstance().start(wsSessionRp, tyrusClientWebsocket.passwordEnryptionKey, ProtocolType.JSON);
					if (wsSessionRp.getAppSession().getState() != ConnectionState.invalidTenant) {
						wsSessionRp.getAppSession().setState(ConnectionState.serverSignature);
						wsSessionRp.getAppSession().setCommClientType(CommClientType.APP);
					}
					wsSessionRp.getAppSession().setAppVersion(rpOpen.appVersion);
					wsSessionRp.getAppSession().setLibVersion(rpOpen.libVersion);
					wsSessionRp.onMessage(buffer);
					break;

				case AppSystemConstants.REVERSE_PROXY_DATA:
					sessionId = AsUtils.getStringFromBuffer(buffer);
					wsSessionRp = tyrusClientWebsocket.getSessionMap().get(sessionId);
					wsSessionRp.onMessage(buffer);
					break;
				}

			} else {
				logger.error("Invalid state, data received without passThroughMode");
			}

		} else {
			logger.error("Unkown header type");
		}
	}

	public void resetReceiveBuffer() {
		receivedBuffer = null;
	}

	public ByteBuffer waitForReceive() {
		synchronized (recWaiting) {
			try {
				if (receivedBuffer == null) {
					recWaiting.wait(AsConstants.MAX_WAIT_FOR_CLIENT_RESPONSE);
				} else {
				}
			} catch (InterruptedException e) {
				logger.warn("InterruptedException" + e.toString());
			}
		}
		return receivedBuffer;
	}

}
