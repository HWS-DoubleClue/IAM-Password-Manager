package com.doubleclue.oauth.openid.enums;

public enum OpenIdDisplay {

	PAGE("page"),
	POPUP("popup"),
	TOUCH("touch"),
	WAP("wap");

	private final String value;

	private OpenIdDisplay(String value) {
		this.value = value;
	}

	public static OpenIdDisplay fromString(String text) {
		if (text != null && !text.isEmpty()) {
			for (OpenIdDisplay b : OpenIdDisplay.values()) {
				if (b.value.equalsIgnoreCase(text)) {
					return b;
				}
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return value;
	}
}
