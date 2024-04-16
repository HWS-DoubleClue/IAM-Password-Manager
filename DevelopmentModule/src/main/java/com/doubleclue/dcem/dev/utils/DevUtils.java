package com.doubleclue.dcem.dev.utils;

import java.io.InputStream;

import com.doubleclue.utils.KaraUtils;

public class DevUtils {

	public static String getTemplateContent(String templateName) throws Exception {
		InputStream inputStream;
		inputStream = DevUtils.class.getClassLoader().getResourceAsStream(templateName);
		if (inputStream == null) {
			throw new Exception("Template not found: " + templateName);
		}
		return KaraUtils.readInputStreamText(inputStream);
	}
	


}
