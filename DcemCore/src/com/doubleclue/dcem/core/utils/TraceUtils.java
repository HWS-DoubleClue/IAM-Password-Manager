package com.doubleclue.dcem.core.utils;

import java.nio.ByteBuffer;

import com.doubleclue.utils.StringUtils;

public class TraceUtils {
	
	/**
	 * @param buffer
	 * @param hexa
	 * @return
	 */
	public final static String traceBuffer(ByteBuffer buffer, boolean hexa) {
		int length = buffer.limit() - buffer.position();
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("\nLength: ");
		stringBuffer.append(length);
		stringBuffer.append("\nData: ");
		if (hexa == false) {
			stringBuffer.append(new String (buffer.array(), buffer.position(), length));
		} else {
			stringBuffer.append(StringUtils.binaryToHexString(buffer.array(), buffer.position(), length));
		}
		return stringBuffer.toString();
	}

}
