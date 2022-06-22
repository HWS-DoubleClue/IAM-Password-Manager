package com.doubleclue.dcem.as.comm.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes;

import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.dcem.as.comm.WsMsgHandler;
import com.doubleclue.dcem.as.logic.AsConstants;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.utils.TraceUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;

public class WsSessionRp extends WsMsgHandler {

	TyrusClientWebsocket tyrusClientWebsocket;
	String remoteAddress;
	byte[] sessionId;
	long timeout;
	String tenantName;

	static Object syncObject = new Object();

	public WsSessionRp(TyrusClientWebsocket tyrusClientWebsocket, byte[] sessionId) {
		super();
		this.tyrusClientWebsocket = tyrusClientWebsocket;
		this.sessionId = sessionId;
		asModule = CdiUtils.getReference(AsModule.class);
	}

	protected void initializeBufferStoA() {
		outputStreamStoA.write(AppSystemConstants.PROTOCOL_VERSION);
		outputStreamStoA.write(AppSystemConstants.PROTOCOL_REVERSE_PROXY);
		outputStreamStoA.write(AppSystemConstants.REVERSE_PROXY_DATA);
		outputStreamStoA.buffer().putInt(sessionId.length);
		outputStreamStoA.buffer().put(sessionId);
		outputStreamStoA.write(AppSystemConstants.PROTOCOL_VERSION);
		outputStreamStoA.write(AppSystemConstants.PROTOCOL_SERVER_TO_APP);
	}

	@Override
	protected void initializeBufferAtoS() {
		outputStreamAtoS.write(AppSystemConstants.PROTOCOL_VERSION);
		outputStreamAtoS.write(AppSystemConstants.PROTOCOL_REVERSE_PROXY);
		if (timeout == AsConstants.SESSION_TIMEOUT_AFTER_DISCONNECT) {
			outputStreamAtoS.write(AppSystemConstants.REVERSE_PROXY_CLOSE);
			try {
				tyrusClientWebsocket.getSessionMap().remove(new String (sessionId, DcemConstants.CHARSET_UTF8));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			asModule.getTenantData().getDeviceSessions().remove (getAppSession().getDeviceId());
		} else {
			outputStreamAtoS.write(AppSystemConstants.REVERSE_PROXY_DATA);
		}

		outputStreamAtoS.buffer().putInt(sessionId.length);
		outputStreamAtoS.buffer().put(sessionId);
		outputStreamAtoS.write(AppSystemConstants.PROTOCOL_VERSION);
		outputStreamAtoS.write(AppSystemConstants.PROTOCOL_APP_TO_SERVER);
	}

	public String getRemoteAddress() {
		return remoteAddress;
	}

	protected void setRemoteAddress(String remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public void sendBinaryAtoS() throws IOException {
		outputStreamAtoS.buffer().flip();
		if (logger.isTraceEnabled()) {
			logger.trace("Sending Proxy  A > S : " + TraceUtils.traceBuffer(outputStreamAtoS.buffer(), false));
		}
		// session.getBasicRemote().sendText("ABCDE");
		synchronized (syncObject) {
			tyrusClientWebsocket.session.getBasicRemote().sendBinary(outputStreamAtoS.buffer());
			tyrusClientWebsocket.setLastPacketSend();
		}
	}

	@Override
	public void sendBinaryStoA() throws IOException {
		outputStreamStoA.buffer().flip();
		if (logger.isTraceEnabled()) {
			logger.trace("Sending  S > A: " + TraceUtils.traceBuffer(outputStreamStoA.buffer(), false));
		}
		synchronized (syncObject) {
			tyrusClientWebsocket.setLastPacketSend();
			tyrusClientWebsocket.session.getBasicRemote().sendBinary(outputStreamStoA.buffer());
		}
	}

	@Override
	public String getSessionId() {
		return "RP-" + new String(sessionId);
	}

	@Override
	public void close(CloseReason reason) throws IOException {
		if (reason == null) {
			reason = new CloseReason(CloseCodes.GOING_AWAY, "");
		}
		// session.close(reason);

	}

	public void setMaxIdleTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Override
	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}

}
