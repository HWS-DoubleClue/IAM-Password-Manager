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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
/**
 * AsApiException
 */

@SuppressWarnings("serial")
public class DcemApiException  extends Exception {
	
	
 static public final int SERVER_LOGIC_EXCEPTION = 599;
	
  @JsonProperty("code")
  private String code = "0";

  @JsonProperty("message")
  private String message = null;

  @JsonProperty("details")
  private String details = null;

  public DcemApiException code(String code) {
    this.code = code;
    return this;
  }

   /**
   * Get code
   * @return code
  **/
  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public DcemApiException message(String message) {
    this.message = message;
    return this;
  }

   /**
   * Get message
   * @return message
  **/
  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public DcemApiException details(String details) {
    this.details = details;
    return this;
  }

   /**
   * Get details
   * @return details
  **/
  public String getDetails() {
    return details;
  }

  public void setDetails(String details) {
    this.details = details;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DcemApiException asApiException = (DcemApiException) o;
    return Objects.equals(this.code, asApiException.code) &&
        Objects.equals(this.message, asApiException.message) &&
        Objects.equals(this.details, asApiException.details);
  }

  @Override
  public int hashCode() {
    return Objects.hash(code, message, details);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AsApiException {\n");
    
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    details: ").append(toIndentedString(details)).append("\n");
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

