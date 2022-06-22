package com.doubleclue.utils;

import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
import org.apache.thrift.protocol.TJSONProtocol;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class ThriftUtils {

	/**
	 * @param sdkConfig
	 * @return
	 * @throws IOException
	 * @throws TException
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public static synchronized byte[] serializeObject(TBase sdkConfig, boolean withSignature) throws IOException, TException, InvalidKeyException, NoSuchAlgorithmException {

		TSerializer serializer = new TSerializer(new TJSONProtocol.Factory());
		byte[] data = serializer.serialize(sdkConfig);
		if (withSignature) {
			byte []  digest = SecureUtils.createMacDigestCommonSha2(data, 0, data.length);
			byte [] digest64 = StringUtils.binaryToHex(digest, digest.length, 0);
			byte [] signedData = new byte[digest64.length + data.length + 1];			
			System.arraycopy(digest64, 0, signedData, 0, digest64.length);
			signedData[digest64.length] = '\n';
			System.arraycopy(data, 0, signedData, digest64.length+1, data.length);
			data = signedData;
		} 
		return data;
	}

	/**
	 * @param inputData
	 * @param tBase
	 * @return
	 * @throws Exception 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public static synchronized boolean deserializeObject(byte[] inputData, TBase tBase, boolean withSignature)
			throws Exception {
		int length = inputData.length;
		int offset = 0;
		if (withSignature) {
			// check signature
			ByteArrayInputStream inputStream = new ByteArrayInputStream(inputData);
			String line = StringUtils.getNextLine(inputStream);
			if (line == null) {
				throw new Exception ("No Signature found.");
			}
			
			byte[] digest = StringUtils.hexStringToBinary(line);			
			
			length = inputStream.available();
			offset = inputData.length - length;
			if (Arrays.equals(digest, SecureUtils.createMacDigestCommonSha2(inputData, offset, length)) == false) {
				throw new Exception("Invalid Digest");
			}
		}
		TDeserializer deserializer = new TDeserializer(new TJSONProtocol.Factory());
		deserializer.deserialize(tBase, inputData, offset, length);
		return true;

	}

}
