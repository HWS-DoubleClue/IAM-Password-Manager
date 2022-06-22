package com.doubleclue.utils;

/**
 * 
 * @author Emanuel Galea
 *
 */
public class KaraException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	KaraErrorCodes karaErrorCodes;

	
	public KaraException(KaraErrorCodes karaErrorCodes, String message, Throwable cause) {
		super(message, cause);
		this.karaErrorCodes = karaErrorCodes;
	}

	public KaraException(KaraErrorCodes karaErrorCodes, String message) {
		super(message, null);
		this.karaErrorCodes = karaErrorCodes;
	}

	public KaraErrorCodes getErrorCode() {
		return karaErrorCodes;
	}

	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(karaErrorCodes.name());
		if (super.getMessage() != null) {
			sb.append("-");
			sb.append(super.getMessage());
		}
		if (super.getCause() != null) {
			sb.append(" / ");
			sb.append(super.getCause());
		}
		return sb.toString();
	}

}
