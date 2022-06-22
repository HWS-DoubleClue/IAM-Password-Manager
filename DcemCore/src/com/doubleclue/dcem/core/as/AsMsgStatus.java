package com.doubleclue.dcem.core.as;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Gets or Sets asApiMsgStatus
 */
public enum AsMsgStatus {
	
// DO NOT CHANGE THE ORDINAL NUMBERS !
  
  OK("Ok"),
  
  WAITING("Waiting"),
  
  QUEUED("Queued"),
  
  SENDING("Sending"),
  
  REC_ERROR("Rec_Error"),
  
  SEND_ERROR("Send_Error"),
  
  DISCONNECTED("Disconnected"),
  
  CANCELLED("Cancelled"), 
  
  SIGNATURE_ERROR ("SignatureError");

  private String value;

  AsMsgStatus(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }
}


