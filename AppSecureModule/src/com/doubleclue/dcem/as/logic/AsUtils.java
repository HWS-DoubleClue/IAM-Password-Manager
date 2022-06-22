package com.doubleclue.dcem.as.logic;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import com.doubleclue.comm.thrift.AppErrorCodes;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;

public class AsUtils {

	public static String getStringFromBuffer(ByteBuffer buffer) {
		try {
			return new String(getBytesFromBuffer(buffer), DcemConstants.CHARSET_UTF8);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

	public static byte[] getBytesFromBuffer(ByteBuffer buffer) {
		int length = buffer.getInt();
		byte[] array = new byte[length];
		buffer.get(array);
		return array;
	}

	public static void putStringToBuffer(ByteBuffer buffer, String string) {
		byte[] bytes = null;
		try {
			bytes = string.getBytes(DcemConstants.CHARSET_UTF8);
		} catch (UnsupportedEncodingException e) {
		}
		buffer.putInt(bytes.length);
		buffer.put(bytes);
	}

	public static AppErrorCodes convertToAppErrorCodes(DcemErrorCodes dcemErrorCodes) {
		AppErrorCodes appErrorCode = null;
		try {
			appErrorCode = AppErrorCodes.valueOf(dcemErrorCodes.name());
		} catch (Exception e) {
		}
		if (appErrorCode == null) {
			appErrorCode = AppErrorCodes.UNEXPECTED_ERROR;
		}
		return appErrorCode;
	}

}
