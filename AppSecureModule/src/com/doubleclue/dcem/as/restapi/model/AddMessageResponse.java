package com.doubleclue.dcem.as.restapi.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * AddMessageResponse
 */

@SuppressWarnings("serial")
public class AddMessageResponse implements Serializable {

	private long msgId = 0l;
	private int timeToLive = 0;
	
	private boolean withPushNotification = false;

	public AddMessageResponse msgId(long msgId) {
		this.msgId = msgId;
		return this;
	}

	public AddMessageResponse() {
		return;
	}

	public AddMessageResponse(long msgId, int timeToLive, boolean withPushNotification) {

		this.msgId = msgId;
		this.timeToLive = timeToLive;
		this.withPushNotification = withPushNotification;
		return;
	}

	/**
	 * Get msgId
	 * 
	 * @return msgId
	 **/
	// @ApiModelProperty(value = "")
	public long getMsgId() {
		return msgId;
	}

	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}

	public AddMessageResponse timeToLive(int timeToLive) {
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
	    AddMessageResponse addMessageResponse = (AddMessageResponse) o;
	    return Objects.equals(this.msgId, addMessageResponse.msgId) &&
	        Objects.equals(this.timeToLive, addMessageResponse.timeToLive) &&
	        Objects.equals(this.withPushNotification, addMessageResponse.withPushNotification);
	  }

	@Override
	public int hashCode() {
		return Objects.hash(msgId);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("    msgId: ").append(toIndentedString(msgId)).append("\n");
		sb.append("    timeToLive: ").append(toIndentedString(timeToLive)).append("\n");
	    sb.append("    withPushNotification: ").append(toIndentedString(withPushNotification)).append("\n");
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

	public boolean isWithPushNotification() {
		return withPushNotification;
	}

	public void setWithPushNotification(boolean withPushNotification) {
		this.withPushNotification = withPushNotification;
	}

}
