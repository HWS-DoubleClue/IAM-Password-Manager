package com.doubleclue.dcem.core.as;

import java.io.Serializable;

/**
 * QrCodeResponse
 */

@SuppressWarnings("serial")
public class QrCodeResponse implements Serializable {

	private String data = null;

	private int timeToLive = 0;

	public QrCodeResponse(String qrCodeData, int timeout) {
		this.data = qrCodeData;
		this.timeToLive = timeout;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}
	
}
