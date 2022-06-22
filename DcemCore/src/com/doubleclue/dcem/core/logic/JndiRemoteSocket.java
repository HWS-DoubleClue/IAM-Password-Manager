package com.doubleclue.dcem.core.logic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AsModuleApi;
import com.doubleclue.dcem.core.as.AuthProxyListener;
import com.doubleclue.dcem.core.entities.DomainEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.weld.CdiUtils;

public class JndiRemoteSocket extends Socket implements AuthProxyListener {

	private static final Logger logger = LogManager.getLogger(JndiRemoteSocket.class);

	JndiProxyParam proxyParam;

	AsModuleApi asModuleApi;

	DomainEntity domainEntity;

	ProxyOutputStream outputStream;
	ProxyInputStream inputStream;

	private CountDownLatch countDownLatch;

	boolean connected;

	List<ByteData> receivedBytes = new ArrayList<ByteData>();

	public JndiRemoteSocket(DomainEntity domainEntity) {
		this.domainEntity = domainEntity;
	}

	public JndiRemoteSocket(String host, int port, InetAddress localHost, int localPort) {
		System.out.println("JndiSocket.JndiSocket()");
	}

	public JndiRemoteSocket(InetAddress address, int port, InetAddress localAddress, int localPort) {
		// TODO Auto-generated constructor stub
	}

	public JndiRemoteSocket(InetAddress host, int port) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void connect(SocketAddress endpoint) throws IOException {
		connect(endpoint, 5000);
	}

	@Override
	public void connect(SocketAddress endpoint, int timeout) throws IOException {
		asModuleApi = (AsModuleApi) CdiUtils.getReference(DcemConstants.AS_MODULE_API_IMPL_BEAN);
		String hostName = ((InetSocketAddress) endpoint).getHostName();
		int port = ((InetSocketAddress) endpoint).getPort();
		boolean secure = false;
		if (domainEntity.getHost().startsWith("ldaps://")) {
			secure = true;
		}
		try {
			logger.debug("JndiRemoteSocket.connect() " + connected);
			proxyParam = asModuleApi.openAuthProxyConnection(domainEntity.getDomainConfig().getAuthConnectorName(), hostName, port, secure, false, this);
			countDownLatch = new CountDownLatch(1);
			connected = true;
		} catch (DcemException e) {
			logger.warn(e.toString());
			throw new IOException(e.toString());
		} catch (Exception exp) {
			logger.warn("Remote Active Directory:  couldn't establish connection", exp);
			throw new IOException("Remote Active Directory:  couldn't establish connection", exp);
		}
	}

	@Override
	public InputStream getInputStream() {
		if (inputStream == null) {
			inputStream = new ProxyInputStream(this);
		}
		return inputStream;
	}

	@Override
	public OutputStream getOutputStream() {
		if (outputStream == null) {
			outputStream = new ProxyOutputStream();
		}
		return outputStream;
	}

	// @Override
	// public void close() {
	// asModuleApi.closeAuthProxyConnection(proxyParam);
	// }

	@Override
	public boolean isConnected() {
		System.out.println("JndiRemoteSocket.isConnected() " + connected);
		return connected;
	}

	public void close() throws IOException {
		asModuleApi.closeAuthProxyConnection(proxyParam);
		connected = false;
	}

	@Override
	public void onClose() {
		try {
			synchronized (receivedBytes) {
				inputStream.close();
				outputStream.close();
				receivedBytes.clear();
				receivedBytes = null;
			}
			if (countDownLatch != null) {
				countDownLatch.countDown();
			}
		} catch (IOException e) {
			logger.debug(e);
		} finally {
			connected = false;
		}
	}

	@Override
	public void onReceive(byte[] data) {
		synchronized (receivedBytes) {
			receivedBytes.add(new ByteData(data, 0));
			countDownLatch.countDown();
		}
//		System.out.println("JndiRemoteSocket.onReceive()  " + data.length);
	}

	/*
	* 
	* 
	*/
	class ProxyInputStream extends InputStream {
		JndiRemoteSocket jndiRemoteSocket;

		public ProxyInputStream(JndiRemoteSocket jndiRemoteSocket) {
			super();
			this.jndiRemoteSocket = jndiRemoteSocket;
		}

		@Override
		public int read() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

		public int read(byte buffer[]) throws IOException {
			return read(buffer, 0, buffer.length);
		}

		public int read(byte buffer[], int offset, int length) throws IOException {
//			System.out.println("JndiRemoteSocket.ProxyInputStream.read()");
			if (receivedBytes.isEmpty()) {
				try {
					countDownLatch.await();
				} catch (InterruptedException e) {
					logger.warn(e);
					return -1;
				}
			}
			if (receivedBytes == null) {
				return -1;
			}
			synchronized (receivedBytes) {
				int deleteCount = 0;
				int readData = 0;
				for (ByteData byteData : receivedBytes) {
					int rest = byteData.getRest();
					if (rest > length) {
						System.arraycopy(byteData.data, byteData.readPointer, buffer, offset, length);
						byteData.readPointer += length;
						readData += length;
						break;
					} else {
						System.arraycopy(byteData.data, byteData.readPointer, buffer, offset, rest);
						deleteCount++;
						offset += rest;
						readData += rest;
					}
				}
				for (int i = 0; i < deleteCount; i++) {
					receivedBytes.remove(i);
				}
				countDownLatch = new CountDownLatch(1);
	//			System.out.println("JndiRemoteSocket.ProxyInputStream.read() RETURN with Length: " + readData);
				return readData;
			}
		}

	}

	class ProxyOutputStream extends OutputStream {

		List<ByteData> toSend;

		public ProxyOutputStream() {
			toSend = new ArrayList<ByteData>();
		}

		@Override
		public void write(int b) throws IOException {
			System.out.println("JndiRemoteSocket.ProxyOutputStream.write()");
		//	toSend.add(new ByteData(data, readPointer));
		}

		@Override
		public void write(byte buffer[]) throws IOException {
			write(buffer, 0, buffer.length);
		}

		@Override
		public void write(byte buffer[], int offset, int length) throws IOException {
			if (logger.isTraceEnabled()) {
				logger.trace("ProxyOutputStream.write() " + length);
			}
			toSend.add(new ByteData(buffer, offset, length));
			// asModuleApi.sendDataAuthProxy(proxyParam, buffer, offset, length);
//			System.out.println("JndiRemoteSocket.ProxyOutputStream.write() Offset: " + offset + ", length: " + length);
		}

		@Override
		public void flush() throws IOException {
			try {
				if (toSend.size() > 1) {
					System.out.println("JndiRemoteSocket.ProxyOutputStream.flush() 222");
				}
				if (toSend.isEmpty()) {
					return;
				}
				ByteData byteData = toSend.get(0);
				if (logger.isTraceEnabled()) {
					logger.trace("ProxyOutputStream.write() " + byteData.data.length);
				}
				asModuleApi.sendDataAuthProxy(proxyParam, byteData.data, byteData.offset, byteData.length);
				toSend.clear();
			} catch (DcemException exp) {
				toSend.clear();
				throw new IOException(exp);
			} catch (Exception exp) {
				logger.info("Remote LDAP Write Failed", exp);
				toSend.clear();
				throw new IOException(exp);
			}
		}

	}

	class ByteData {
		public ByteData(byte[] data, int readPointer) {
			super();
			this.data = data;
			this.readPointer = readPointer;
		}
		public ByteData(byte[] data, int offset, int length) {
			super();
			this.data = data;
			this.readPointer = 0;
			this.offset = offset;
			this.length = length;
		}

		byte[] data;
		int readPointer = 0;
		int length;
		int offset;

		int getRest() {
			return data.length - readPointer;
		}
		@Override
		public String toString() {
			return "ByteData [data=" + Arrays.toString(data) + ", readPointer=" + readPointer + ", length=" + length + ", offset=" + offset + "]";
		}
		
		
	}

	@Override
	public DomainEntity getDomainEntity() {
		return domainEntity;
	}

}
