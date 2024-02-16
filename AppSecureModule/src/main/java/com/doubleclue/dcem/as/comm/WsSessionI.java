package com.doubleclue.dcem.as.comm;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.websocket.CloseReason;
import javax.websocket.Session;

import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransportException;

import com.doubleclue.dcem.core.utils.ByteBufferedOutputStream;

public interface WsSessionI {
	
	public void sendBinaryAtoS () throws Exception;
	
	public void sendBinaryStoA () throws Exception;
	
	public ByteBuffer getRecieveBufferAtoS () throws TTransportException;
	
	public ByteBuffer getWaitForRecieveBufferStoA () throws TTransportException;

	public void resetReceiveBufferStoA();
	
	public void close(CloseReason reason) throws IOException;
	
	public void setMaxIdleTimeout(long timeout);
	
	public void setAppSession(AppSession appSession);
	
	public void setProtocolAtoS(TProtocol protocolAtoS);

	public ByteBufferedOutputStream getOutputStreamAtoS() throws IOException;
	public ByteBufferedOutputStream getOutputStreamStoA() throws IOException;
	
	public void setSession(Session session);
	public Session getSession();
	
	public String getRemoteAddress();
	
	public String getSessionId();
	
	public String getTenantName();

	

}
