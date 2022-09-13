package com.doubleclue.dcem.core.jersey;

import java.util.Objects;

import com.doubleclue.dcem.core.exceptions.DcemException;

/**
 * AsApiException
 */

@SuppressWarnings("serial")
public class DcemApiException extends Exception {

	private String code;

	private String message = null;

	private String details = null;

	public DcemApiException code(String code) {
		this.code = code;
		return this;
	}

	public DcemApiException(String code, String message, String details) {
		super();
		this.code = code;
		this.message = message;
		this.details = details;
	}

	public DcemApiException(DcemException exp) {
		this.code = exp.getErrorCode().name();
		this.message = exp.getErrorCode().name();
		this.details = exp.getLocalizedMessage();
	}

	/**
	 * Get code
	 * 
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
	 * 
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
	 * 
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
		return Objects.equals(this.code, asApiException.code) && Objects.equals(this.message, asApiException.message)
				&& Objects.equals(this.details, asApiException.details);
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
