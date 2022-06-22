package com.doubleclue.dcem.as.restapi.model;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import com.doubleclue.dcem.admin.logic.SendByEnum;

/**
 * AsApiActivationCode
 */

@SuppressWarnings("serial")
public class AsApiActivationCode implements Serializable {
	
	private int activationCodeId;
	
	private String userLoginId = null;

	private String activationCode = null;

	private SendByEnum sendBy;

	private Date validTill = null;
	
	private Date createdOn = null;

	private String info = null;

	
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

	
	/**
	* Get activationCode
	* @return activationCode
	**/

	public String getActivationCode() {
		return activationCode;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	
	/**
	* Get createdOn
	* @return createdOn
	**/

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	
	/**
	* This is a date.
	* @return validTill
	**/
	public Date getValidTill() {
		return validTill;
	}

	public void setValidTill(Date validTill) {
		this.validTill = validTill;
	}

	
	/**
	* Get info
	* @return info
	**/
	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	@Override
	public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		AsApiActivationCode asApiActivationCode = (AsApiActivationCode) o;
		return Objects.equals(this.userLoginId, asApiActivationCode.userLoginId)
				&& Objects.equals(this.activationCode, asApiActivationCode.activationCode)
				&& Objects.equals(this.createdOn, asApiActivationCode.createdOn)
				&& Objects.equals(this.validTill, asApiActivationCode.validTill)
				&& Objects.equals(this.info, asApiActivationCode.info);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userLoginId, activationCode, createdOn, validTill, info);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("class AsApiActivationCode {\n");

		sb.append("    userLoginId: ").append(toIndentedString(userLoginId)).append("\n");
		sb.append("    activationCode: ").append(toIndentedString(activationCode)).append("\n");
		sb.append("    createdOn: ").append(toIndentedString(createdOn)).append("\n");
		sb.append("    validTill: ").append(toIndentedString(validTill)).append("\n");
		sb.append("    info: ").append(toIndentedString(info)).append("\n");
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

	public SendByEnum getSendBy() {
		return sendBy;
	}

	public void setSendBy(SendByEnum sendBy) {
		this.sendBy = sendBy;
	}

	public int getActivationCodeId() {
		return activationCodeId;
	}

	public void setActivationCodeId(int activationCodeId) {
		this.activationCodeId = activationCodeId;
	}
}
