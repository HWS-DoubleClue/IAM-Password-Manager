package com.doubleclue.dcem.as.restapi.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * RequestLoginQrCodeResponse
 */

@SuppressWarnings("serial")
public class RequestLoginQrCodeResponse implements Serializable {

	private String data = null;

	private int timeToLive = 0;

	public RequestLoginQrCodeResponse(String qrCodeData, int timeout) {
		this.data = qrCodeData;
		this.timeToLive = timeout;
	}

	public RequestLoginQrCodeResponse data(String data) {
		this.data = data;
		return this;
	}

	/**
	 * Get data
	 * 
	 * @return data
	 **/
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public RequestLoginQrCodeResponse timeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
		return this;
	}

	/**
	 * Get timeToLive
	 * 
	 * @return timeToLive
	 **/
	public int getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(int timeToLive) {
		this.timeToLive = timeToLive;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		RequestLoginQrCodeResponse requestLoginQrCodeResponse = (RequestLoginQrCodeResponse) o;
		return Objects.equals(this.data, requestLoginQrCodeResponse.data)
				&& Objects.equals(this.timeToLive, requestLoginQrCodeResponse.timeToLive);
	}

	@Override
	public int hashCode() {
		return Objects.hash(data, timeToLive);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class RequestLoginQrCodeResponse {\n");

		sb.append("    data: ").append(toIndentedString(data)).append("\n");
		sb.append("    timeToLive: ").append(toIndentedString(timeToLive)).append("\n");
		sb.append("}");
		return sb.toString();
	}

	/**
	 * Convert the given object to string with each line indented by 4 spaces
	 * (except the first line).
	 */
	private String toIndentedString(java.lang.Object o) {
		if (o == null) {
			return "null";
		}
		return o.toString().replace("\n", "\n    ");
	}
}
