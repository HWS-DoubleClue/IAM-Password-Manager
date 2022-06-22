package com.doubleclue.dcem.as.comm.client;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.thrift.TProcessor;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.doubleclue.comm.thrift.AppSystemConstants;
import com.doubleclue.dcem.as.comm.WsMsgHandler;
import com.doubleclue.utils.StringUtils;

public class TClientAtoS_WsTransport extends TTransport {

	private static Logger logger = LogManager.getLogger(WsMsgHandler.class);

	private TyrusClientWebsocket tyrusClientWebsocket;
	private boolean open;
	ByteBuffer recByteBuffer;
	private ByteArrayOutputStream outputStream;
	TProcessor tProcessor;
	private boolean writingInPorcess;

	private static byte[] header = new byte[] { AppSystemConstants.PROTOCOL_VERSION,
			AppSystemConstants.PROTOCOL_APP_TO_SERVER };

	public TClientAtoS_WsTransport(TyrusClientWebsocket tyrusClientWebsocket) {
		this.tyrusClientWebsocket = tyrusClientWebsocket;
	}

	@Override
	public boolean isOpen() {
		return open;
	}

	@Override
	public void open() throws TTransportException {
		try {
			tyrusClientWebsocket.connect();
			recByteBuffer = null;
			writingInPorcess = false;
			outputStream = new ByteArrayOutputStream(8 * 1024);

		} catch (Throwable exp) {
			logger.info("Connection failed. " + exp.toString());
			throw new TTransportException("Connection fails", exp);
		}
		open = true;
	}

	@Override
	public void close() {
		recByteBuffer = null;
		writingInPorcess = false;
		// synchronized (waitForReceiveSync) {
		// waitForReceiveSync.notify();
		// }
		open = false;
		try {
			tyrusClientWebsocket.close("GOING_AWAY");
		} catch (Exception e) {
			logger.info("Close exception");
		}
	}

	@Override
	public int read(byte[] buf, int offset, int length) throws TTransportException {
		if (open == false) {
			throw new TTransportException("Connection is closed");
		}
		if (recByteBuffer == null) {
			recByteBuffer = tyrusClientWebsocket.waitForReceive();
		}

		if (recByteBuffer == null) {
			throw new TTransportException(ProxyCommClient.SERVER_BUSY);
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
	public void write(byte[] buf, int offset, int length) throws TTransportException {
		if (open == false) {
			throw new TTransportException("Connection is closed");
		}
		try {
			if (writingInPorcess == false) {
				outputStream.reset();
				outputStream.write(header, 0, header.length);
				writingInPorcess = true;
			}
			outputStream.write(buf, offset, length);
		} catch (Exception exp) {
			throw new TTransportException(exp);
		}
	}

	// protected void closeReceived() {
	// open = false;
	// inputStream = null;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.thrift.transport.TTransport#flush()
	 */
	@Override
	public void flush() throws TTransportException {
		byte[] data;
		try {
			data = outputStream.toByteArray();
			if (logger.isTraceEnabled()) {
				logger.trace("Send A > S: " + StringUtils.traceBuffer(data, 0, data.length, false));
			}
			tyrusClientWebsocket.send(data);
		} catch (Exception e) {
			recByteBuffer = null; // the reading buffer
			writingInPorcess = false;
			throw new TTransportException("Flush failed ", e);
		}
		writingInPorcess = false;
		recByteBuffer = null; // the reading buffer
	}
}