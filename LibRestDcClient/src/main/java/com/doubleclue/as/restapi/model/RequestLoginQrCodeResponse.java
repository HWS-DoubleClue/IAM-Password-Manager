/**
 * App Secure Authentication Messages
 * A secure App for secure authentication and messages
 *
 * OpenAPI spec version: 1.0.0
 * Contact: xxxx@hws-gruppe.de
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.doubleclue.as.restapi.model;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * RequestLoginQrCodeResponse
 */

@SuppressWarnings("serial")
public class RequestLoginQrCodeResponse  implements Serializable {
  @JsonProperty("data")
  private String data = null;

  @JsonProperty("timeToLive")
  private int timeToLive = 0;

  public RequestLoginQrCodeResponse data(String data) {
    this.data = data;
    return this;
  }

   /**
   * Get data
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
    return Objects.equals(this.data, requestLoginQrCodeResponse.data) &&
        Objects.equals(this.timeToLive, requestLoginQrCodeResponse.timeToLive);
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
