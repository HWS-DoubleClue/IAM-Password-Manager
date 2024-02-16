 package com.doubleclue.dcem.as.restapi.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * AsApiMessage
 */

@SuppressWarnings("serial")
public class AsApiMessage  implements Serializable {
  private String userLoginId = null;

  private String templateName = null;

  private List<AsMapEntry> dataMap = new ArrayList<AsMapEntry>();

  private String deviceName = null;

  private String sessionId = null;

  private boolean responseRequired = false;

  private boolean signatureRequired = false;

  private int responseTimeout = 0;
  
  private int notifyNodeOnResponse = 0;
  
  private boolean allowPasswordLess = false;

  public AsApiMessage userLoginId(String userLoginId) {
    this.userLoginId = userLoginId;
    return this;
  }

   /**
   * Get userLoginId
   * @return userLoginId
  **/
  public String getUserLoginId() {
    return userLoginId;
  }

  public void setUserLoginId(String userLoginId) {
    this.userLoginId = userLoginId;
  }

  public AsApiMessage templateName(String templateName) {
    this.templateName = templateName;
    return this;
  }

   /**
   * Get templateName
   * @return templateName
  **/
  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  public AsApiMessage dataMap(List<AsMapEntry> dataMap) {
    this.dataMap = dataMap;
    return this;
  }

  public AsApiMessage addDataMapItem(AsMapEntry dataMapItem) {
    this.dataMap.add(dataMapItem);
    return this;
  }

   /**
   * Get dataMap
   * @return dataMap
  **/
  public List<AsMapEntry> getDataMap() {
    return dataMap;
  }

  public void setDataMap(List<AsMapEntry> dataMap) {
    this.dataMap = dataMap;
  }

  public AsApiMessage deviceName(String deviceName) {
    this.deviceName = deviceName;
    return this;
  }

   /**
   * This is optional. As default should be set to null as . If null, DCEM will send the message to the current online device or the first user's device which goes online. Else you can specify the user's device name which the message is intented for, in this cas SEMS will send the message only to this device.                 
   * @return deviceName
  **/
  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }

  public AsApiMessage sessionId(String sessionId) {
    this.sessionId = sessionId;
    return this;
  }

   /**
   * This is optional and is the portal session Id. The value will be returned in the response.
   * @return sessionId
  **/
  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public AsApiMessage responseRequired(boolean responseRequired) {
    this.responseRequired = responseRequired;
    return this;
  }

   /**
   * Get responseRequired
   * @return responseRequired
  **/
  public boolean getResponseRequired() {
    return responseRequired;
  }

  public void setResponseRequired(boolean responseRequired) {
    this.responseRequired = responseRequired;
  }

  public AsApiMessage signatureRequired(boolean signatureRequired) {
    this.signatureRequired = signatureRequired;
    return this;
  }

   /**
   * If true, on message-response the device will sign the dataMap, action-key and response data with the device private key. DCEM will verify the signature. In particular cases, this be required for high security reasons, but it will consumes more device and SEMS resources.                         
   * @return signatureRequired
  **/
  public boolean getSignatureRequired() {
    return signatureRequired;
  }

  public void setSignatureRequired(boolean signatureRequired) {
    this.signatureRequired = signatureRequired;
  }

  public AsApiMessage responseTimeout(int responseTimeout) {
    this.responseTimeout = responseTimeout;
    return this;
  }

   /**
   * This is the time in seconds within the device have to response to the messageor if response is not required, it is the time till device goes online If set to 0, the Response-Timeout will be retrieved from the DCEM Preferences 'Response Timeout'
   * @return responseTimeout
  **/
  public int getResponseTimeout() {
    return responseTimeout;
  }

  public void setResponseTimeout(int responseTimeout) {
    this.responseTimeout = responseTimeout;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AsApiMessage asApiMessage = (AsApiMessage) o;
    return Objects.equals(this.userLoginId, asApiMessage.userLoginId) &&
        Objects.equals(this.templateName, asApiMessage.templateName) &&
        Objects.equals(this.dataMap, asApiMessage.dataMap) &&
        Objects.equals(this.deviceName, asApiMessage.deviceName) &&
        Objects.equals(this.sessionId, asApiMessage.sessionId) &&
        Objects.equals(this.responseRequired, asApiMessage.responseRequired) &&
        Objects.equals(this.signatureRequired, asApiMessage.signatureRequired) &&
        Objects.equals(this.responseTimeout, asApiMessage.responseTimeout);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userLoginId, templateName, dataMap, deviceName, sessionId, responseRequired, signatureRequired, responseTimeout);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AsApiMessage {\n");
    
    sb.append("    userLoginId: ").append(toIndentedString(userLoginId)).append("\n");
    sb.append("    templateName: ").append(toIndentedString(templateName)).append("\n");
    sb.append("    dataMap: ").append(toIndentedString(dataMap)).append("\n");
    sb.append("    deviceName: ").append(toIndentedString(deviceName)).append("\n");
    sb.append("    sessionId: ").append(toIndentedString(sessionId)).append("\n");
    sb.append("    responseRequired: ").append(toIndentedString(responseRequired)).append("\n");
    sb.append("    signatureRequired: ").append(toIndentedString(signatureRequired)).append("\n");
    sb.append("    responseTimeout: ").append(toIndentedString(responseTimeout)).append("\n");
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

public int getNotifyNodeOnResponse() {
	return notifyNodeOnResponse;
}

public void setNotifyNodeOnResponse(int notifyNodeOnResponse) {
	this.notifyNodeOnResponse = notifyNodeOnResponse;
}

public boolean isAllowPasswordLess() {
	return allowPasswordLess;
}

public void setAllowPasswordLess(boolean allowPasswordLess) {
	this.allowPasswordLess = allowPasswordLess;
}


}

