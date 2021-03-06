/*
 * DoubleClue REST API
 * DoubleClue URL http://yourhost:8001/dcem/restApi/as
 *
 * OpenAPI spec version: 1.5.0
 * Contact: emanuel.galea@hws-gruppe.de
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.doubleclue.dcem.as.restapi.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Gets or Sets asApiUrlTokenUsage
 */
public enum AsApiUrlTokenUsage {

  RESETPASSWORD("ResetPassword"),

  VERIFYEMAIL("VerifyEmail");

  private String value;

  AsApiUrlTokenUsage(String value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static AsApiUrlTokenUsage fromValue(String text) {
    for (AsApiUrlTokenUsage b : AsApiUrlTokenUsage.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

