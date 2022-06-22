package com.doubleclue.dcem.as.comm.thrift.transport;

import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TProcessor;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.doubleclue.dcem.as.comm.AppWsConnection;
import com.doubleclue.dcem.as.comm.WsSessionI;
import com.doubleclue.dcem.core.utils.ByteBufferedOutputStream;


public class TServerStoA_WsTransport extends TTransport {

	private static Logger logger = LogManager.getLogger(TServerStoA_WsTransport.class);
	TProcessor processor;
	boolean open;
	AppWsConnection appWsConnection;
	int count;
	WsSessionI wsSession;
	
	ByteBufferedOutputStream sendBufferStream;

	public TServerStoA_WsTransport() {
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public void open() throws TTransportException {
		open = true;
		return;

	}
	
	

	@Override
	public void close() {
		logger.trace("TServerStoA_WsTransport.close()");
	}

	@Override
	public int read(byte[] buf, int offset, int length) throws TTransportException {
		if (open == false) {
			throw new TTransportException("Connection is closed");
		}
		ByteBuffer	recByteBuffer = wsSession.getWaitForRecieveBufferStoA();
		
		if (recByteBuffer == null) {
			throw new TTransportException ("Received a null buffer. Probably session closed");
		}
		int remainLength = recByteBuffer.limit() - recByteBuffer.position();
		if (length > remainLength) {
			length = remainLength;
		}
		if (length > 0) {
			recByteBuffer.get(buf, offset, length);
		}
		if (length < 1) {
			logger.debug("TServerStoA_WsTransport.read() length less than 1");
		}
		return length;
	}

	@Override
	public void write(byte[] buffer, int offset, int length) throws TTransportException {
		try {
			ByteBufferedOutputStream sendBufferStream = wsSession.getOutputStreamStoA();
			sendBufferStream.write(buffer, offset, length);
		} catch (Exception exp) {
			throw new TTransportException(exp);
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.thrift.transport.TTransport#flush()
	 */
	public void flush()  throws TTransportException  {
		
		wsSession.resetReceiveBufferStoA();
		try {
		//	System.out.println("TServerStoA_WsTransport.flush() Start "   );
			wsSession.sendBinaryStoA();
		//	System.out.println("TServerStoA_WsTransport.flush() END "   );
		} catch (Exception e) {
			throw new TTransportException(e.getMessage());
		}
	}

	protected void closeReceived() {
		wsSession.resetReceiveBufferStoA();
		open = false;
	}

	public WsSessionI getWsSession() {
		return wsSession;
	}

	public void setWsSession(WsSessionI wsSession) {
		this.wsSession = wsSession;
	}
	
}