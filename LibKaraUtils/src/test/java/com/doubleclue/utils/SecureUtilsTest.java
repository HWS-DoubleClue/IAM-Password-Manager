package com.doubleclue.utils;

public class SecureUtilsTest {

	
//
//	@Test
//	public void testConvertPemToTrustStore() {
//		String pem = "### CN=doublecheck.de\n" 
//+ "### Issuer: CN=HWS001L0123-sem_dbxxx\n"
//+ "-----BEGIN CERTIFICATE-----\n"
//+ "MIIDAjCCAeqgAwIBAgIIwXSbQv+CjbcwDQYJKoZIhvcNAQELBQAwIDEeMBwGA1UE\n"
//+ "AwwVSFdTMDAxTDAxMjMtc2VtX2RieHh4MCAXDTE3MDYxNDEyNTMyNloYDzIwNjcw\n"
//+ "NjEzMjIwMDAwWjAZMRcwFQYDVQQDDA5kb3VibGVjaGVjay5kZTCCASIwDQYJKoZI\n"
//+ "hvcNAQEBBQADggEPADCCAQoCggEBAM9d9aKwT9knwV7Q5ba9zirDDKByIJpPvdTn\n"
//+ "AFpc9QLuuVhewS9Wvvfhm3yo3E/aH3R/CoLcjHKXJ7fnRb5qI9Q5rpyYBV9vbEuH\n"
//+ "Wgo97TNlpPCXJtJc34kubd/2DfkV7MkU8w5dWo4S4ZHXPZ+RCTPbiIPQJu0B1Avm\n"
//+ "/5HUWX0erb5D/fTbSCxnzS5gpdivLhtxEledegwp4z0HDr0CJawgiqhgvwmi0lom\n"
//+ "ua5X1Zlw4MROmyIsCihiAfp+T7SVlumwkWugpouCwqVy479Wf2ucLSEoWhRs+9AP\n"
//+ "TLm8TzXIzELPy9sNHdtb9qXIFX5yy969+tq6jnGNGaptj9ruLu0CAwEAAaNFMEMw\n"
//+ "DwYDVR0TAQH/BAUwAwEB/zALBgNVHQ8EBAMCAbYwIwYDVR0lBBwwGgYIKwYBBQUH\n"
//+ "AwEGCCsGAQUFBwMCBgRVHSUAMA0GCSqGSIb3DQEBCwUAA4IBAQBlS+vdrd+8lDPUv"
//+ "CGbVeGbrLt4tcdgMNrinRwa3C0gWpM2mujNd0agce2nOBQwBWHmFuLoEg/H3/+2cv"
//+ "jBu9kkoQGFhnstqO8Njs5gt5VFWZn5S9co+cbOUgga4/hlwSh7GQhDEc8EJIREbMv"
//+ "taV9nojvPjHSmBfrbKyzsoNctKO85hxxI2jZwWWNlCo3UmDjepdNEOEUR2YmoK7i\n"
//+ "Q4Tcj/Ioc3jKI2Xzs9p5wR5r5n9fpZSBGYfNQ9mHgw0Tw9QTPs8Hf3IqYXXPNnUz\n"
//+ "ajaJLOoXW6srzCj7y8Aq4Rglyo2djZA51Vu/p/Lw0418SsXHyDQRf5sPU5HVGywJv"
//+ "AMGw55jz\n"
//+ "-----END CERTIFICATE-----\n"
//+ ""
//+ "### CN=HWS001L0123-sem_dbxxx\n"
//+ "### Issuer: CN=HWS001L0123-sem_dbxxx\n"
//+ "-----BEGIN CERTIFICATE-----\n"
//+ "MIIDCTCCAfGgAwIBAgIIOrxoLLZAiQ8wDQYJKoZIhvcNAQELBQAwIDEeMBwGA1UE\n"
//+ "AwwVSFdTMDAxTDAxMjMtc2VtX2RieHh4MCAXDTE3MDUyOTA3NDEyNloYDzIxMTcw\n"
//+ "NTI5MDc0MTI2WjAgMR4wHAYDVQQDDBVIV1MwMDFMMDEyMy1zZW1fZGJ4eHgwggEi\n"
//+ "MA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDEHkpQpHFKVyh74DPAjeAoO9JY\n"
//+ "h33d8AnQYDhhr5tImm+N/46yAyFAbqLfCzvHvGCkRvI/JbsAyLJU2RNAQIRmTdTC\n"
//+ "HO5X7QQ2Jx2DGA01BgXy3E7PUUQoiWr+2VkTDgsH8a0+zZe2/iVMaSSGsqUKqvDN\n"
//+ "p/w02c/+m0pOZnU9RdY8HEUTZ400P9q9oPwU/zbbIBeY3ILjuZbh2rbz2YbszBa0\n"
//+ "7Cl4RMImre3j4NmTs2vQFELIzx7/zKuiYeYe9jem8W0nUg6AT65gNiQ90EG5MCW0\n"
//+ "zLQUgymeElc1SDp+wuzs9Fij3dN7S1QWjXiPK/SHrHG9E1xjIM6kDJ8hRjs9AgMB\n"
//+ "AAGjRTBDMA8GA1UdEwEB/wQFMAMBAf8wCwYDVR0PBAQDAgG2MCMGA1UdJQQcMBoG\n"
//+ "CCsGAQUFBwMBBggrBgEFBQcDAgYEVR0lADANBgkqhkiG9w0BAQsFAAOCAQEAR5id\n"
//+ "9U/i31W8K+w7kwH7R3mReseZ6hsr90EGUv1RNyTpnnW1u+yd4IWzcO0h1gNQ/4cl\n"
//+ "JHPuRw7eutHDJ47kBpSJNDaH3bM15eYRFLK9OKD9497aiU3EM/QSt0TLNk2fOlmZ\n"
//+ "6W2+NNsvUEIHj1HXJPBCHM9lMFDmBhw0IzURUMjKorEeemDnFdlJ2Xh6kOs0HQBW\n"
//+ "EGuTbNAScnC3UolbZs+VZ3nP7pPaeXsojxL6vVsmSnn8z6BtwqVHV+PjHAJBpOSl\n"
//+ "mCpCfNXKVWISIthIjopxCofeJtDmqSHP1Sdsy6P3NcOks0ufB38SXn6mBrZSTIyL\n"
//+ "tOPw08vGxtxmmB0hLA==\n"
//+ "-----END CERTIFICATE-----\n";
//		byte[] content = null;
//		try {
//			content = pem.getBytes("UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			fail("Couldn't decoe string"); 
//		}
//		KeyStore ks = null;
//		try {
//			ks = SecureUtils.convertPemToTrustStore(content);
//		} catch (Exception e) {
//			e.printStackTrace();
//			fail("Parsing failed"); 
//		}
//		try {
//			if (ks.size() < 2) {
//				fail ("No certificates found");
//			}
//			Certificate certificate = ks.getCertificate("CN=HWS001L0123-sem_dbxxx");
//			if (certificate == null) {
//				fail ("Wrong certificates found");
//			}
//		} catch (KeyStoreException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		
//	}

	

}
