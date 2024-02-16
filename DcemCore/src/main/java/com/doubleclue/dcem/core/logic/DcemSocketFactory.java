package com.doubleclue.dcem.core.logic;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.doubleclue.dcem.core.entities.DomainEntity;
import com.doubleclue.dcem.core.utils.DcemTrustManager;

public class DcemSocketFactory extends SocketFactory implements Comparator<Object> {

	// private static final TrustManager[] ALWAYS_TRUST_MANAGER = new TrustManager[] { new TrustAlwaysManager() };

	static ThreadLocal<DomainEntity> threadLocalDomainEntity = new ThreadLocal<DomainEntity>();

	DomainEntity domainEntity;

	public DcemSocketFactory() {
		domainEntity = threadLocalDomainEntity.get(); 
	}

	public static DcemSocketFactory getDefault() {
		return new DcemSocketFactory();
	}

	@Override
	public Socket createSocket() throws IOException, UnknownHostException {
		domainEntity = threadLocalDomainEntity.get();
		if (domainEntity == null) {
 			throw new IOException("No DomainEntity set in threadLocalDomainEntity");
		}
		if (domainEntity.getDomainConfig().isRemote()) {
//			System.out.println("DcemSocketFactory.createSocket() " + domainEntity.getName());
			return new JndiRemoteSocket(domainEntity);
		}

		if (domainEntity.getHost().startsWith("ldaps")) {
			SSLContext ctx;
			try {
				SocketFactory socketFactory;
				ctx = SSLContext.getInstance("TLS");
				if (domainEntity.getDomainConfig().isVerifyCertificate() == false) {
					DcemTrustManager trustManager = new DcemTrustManager(true);
					ctx.init(null, new TrustManager[] { trustManager }, null);
					socketFactory = ctx.getSocketFactory();
				} else {
					socketFactory = SSLSocketFactory.getDefault();
				}
				return socketFactory.createSocket();
			} catch (KeyManagementException e) {
				throw new RuntimeException(e);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			}

		} else {
			return new Socket();
		}
	}

	public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
		return new Socket(host, port);
	}

	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
		// System.out.println("[creating a custom socket (method 2)]");
		return new Socket(host, port, localHost, localPort);
	}

	public Socket createSocket(InetAddress host, int port) throws IOException {
		return new Socket(host, port);
	}

	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
		return new Socket(address, port, localAddress, localPort);
	}

	public int compare(Object o1, Object o2) {
		if (domainEntity == null) {
			return -1;
		}
		if (domainEntity.getVersion() != threadLocalDomainEntity.get().getVersion()) {
 			System.out.println("DcemSocketFactory.compare() domainEntity.getVersion()=" + domainEntity.getVersion() +  " ThreadVersion=" + threadLocalDomainEntity.get().getVersion());
			return -1;
		}
		if (o1.equals(o2)) {
			return 0;
		}
		return -1;
	}

}
