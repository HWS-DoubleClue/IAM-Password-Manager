package com.doubleclue.dcem.core.as;

import java.io.Serializable;
import java.util.Objects;


/**
 * QueryLoginResponse
 */

@SuppressWarnings("serial")
public class QueryLoginResponse  implements Serializable {
  private String userLoginId = null;

  private String deviceName = null;

  public QueryLoginResponse(String userName, String deviceName2) {
	userLoginId = userName;
	deviceName = deviceName2;
}

public QueryLoginResponse userLoginId(String userLoginId) {
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

  public QueryLoginResponse deviceName(String deviceName) {
    this.deviceName = deviceName;
    return this;
  }

   /**
   * Get deviceName
   * @return deviceName
  **/
  public String getDeviceName() {
    return deviceName;
  }

  public void setDeviceName(String deviceName) {
    this.deviceName = deviceName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    QueryLoginResponse queryLoginResponse = (QueryLoginResponse) o;
    return Objects.equals(this.userLoginId, queryLoginResponse.userLoginId) &&
        Objects.equals(this.deviceName, queryLoginResponse.deviceName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userLoginId, deviceName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class QueryLoginResponse {\n");
    
    sb.append("    userLoginId: ").append(toIndentedString(userLoginId)).append("\n");
    sb.append("    deviceName: ").append(toIndentedString(deviceName)).append("\n");
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

