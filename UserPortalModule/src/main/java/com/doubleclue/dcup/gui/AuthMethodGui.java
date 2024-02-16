package com.doubleclue.dcup.gui;

import java.util.ResourceBundle;

import com.doubleclue.dcem.core.as.AuthMethod;

public class AuthMethodGui {

	private AuthMethod method;
	private String name;
	private String image;
	private int height;
	private int width;

	private static final String PREFIX_NAME = "auth.";
	private static final String PREFIX_IMAGE = "authImage.";
	private static final String PREFIX_HEIGHT = "authHeight.";
	private static final String PREFIX_WIDTH = "authWidth.";

	AuthMethodGui(AuthMethod method, ResourceBundle resourceBundle) {
		this.method = method;
		if (method != null) {
			String value = method.name();
			this.name = resourceBundle.getString(PREFIX_NAME + value);
			this.image = resourceBundle.getString(PREFIX_IMAGE + value);
			this.height = Integer.parseInt(resourceBundle.getString(PREFIX_HEIGHT + value));
			this.width = Integer.parseInt(resourceBundle.getString(PREFIX_WIDTH + value));
		}
	}

	public AuthMethod getMethod() {
		return method;
	}

	public String getName() {
		return name;
	}

	public String getImage() {
		return image;
	}

	public int getHeight() {
		return height;
	}

	public int getWidth() {
		return width;
	}

	@Override
	public String toString() {
		return name;
	}
}