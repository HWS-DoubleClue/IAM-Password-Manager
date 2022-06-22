package com.doubleclue.dcem.as.comm.thrift.transport;

import java.io.OutputStream;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.doubleclue.dcem.as.comm.WsSessionI;
import com.doubleclue.dcem.core.utils.ByteBufferedOutputStream;

public class TServerAtoS_WsTransport extends TTransport {

	private static Logger logger = LogManager.getLogger(TServerAtoS_WsTransport.class);

	// Session session;
	boolean open;
	ByteBuffer byteBuffer;
	ByteBufferedOutputStream sendBufferStream;
	// int appToServerCount;
	WsSessionI wsSession;

	OutputStream outputStream;

	public TServerAtoS_WsTransport() {

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
		open = false;
	}

	@Override
	public int read(byte[] buf, int offset, int length) throws TTransportException {
		if (open == false) {
			throw new TTransportException("Connection is closed");
		}
		try {
			byteBuffer = wsSession.getRecieveBufferAtoS();
			int remainLength = byteBuffer.limit() - byteBuffer.position();
			if (length > remainLength) {
				length = remainLength;
			}
			if (length > 0) {
				byteBuffer.get(buf, offset, length);
			}
		} catch (Exception exp) {
			logger.error("Thrift couldn't read", exp);
		}

		// TODO check length
		return length;
	}

	@Override
	public void write(byte[] buffer, int offset, int length) throws TTransportException {

		try {
			sendBufferStream = wsSession.getOutputStreamAtoS();
			sendBufferStream.write(buffer, offset, length);
		} catch (Exception exp) {
			throw new TTransportException(exp);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.thrift.transport.TTransport#flush()
	 */
	public void flush() throws TTransportException {

		
		try {
			wsSession.sendBinaryAtoS();
		} catch (Exception exp) {
			logger.error("Websocket sendBinary Failed", exp);
			throw new TTransportException(exp.toString());
		}
		sendBufferStream.buffer().rewind();
	}

	protected void closeReceived() {
		open = false;
	}

	public WsSessionI getWsSession() {
		return wsSession;
	}

	public void setWsSession(WsSessionI wsSession) {
		this.wsSession = wsSession;
	}

}