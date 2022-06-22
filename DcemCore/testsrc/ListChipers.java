import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

import javax.net.ssl.SSLSocketFactory;

import org.spongycastle.jce.provider.BouncyCastleProvider;

public class ListChipers {

	public static final String ENCRYPTION_ALGORITHM = "AES";
	public static final String ENCRYPTION_CRYPTOMODE = "EBC";
//	public static final String ENCRYPTION_PADDING = "PKCS5PADDING";

	public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchProviderException {

		Security.addProvider(new BouncyCastleProvider());
		
		SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
				
		for (String cipher :factory.getSupportedCipherSuites()) {
			System.out.println(cipher);
		}

//		for (Provider provider : Security.getProviders()) {
//			System.out.println("Provider Name: " + provider.getName());
//
//			for (String key : provider.stringPropertyNames()) {
//				System.out.println("\t" + key + "\t" + provider.getProperty(key));
//			}
//			System.out.println("_________________________________________________________");
//		}
		System.exit(0);

	}

}
