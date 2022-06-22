package com.doubleclue.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

public class ResourceBundleUtf8Control extends Control {

//	boolean autoEncoding = false;
	boolean utfEncoding = true;
	
	public ResourceBundleUtf8Control() {
		super();
	}

	public ResourceBundleUtf8Control(boolean utfEncoding) {
		super();
		this.utfEncoding = utfEncoding;
	}

	@Override
	public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {
		// The below is a copy of the default implementation.
		String bundleName = toBundleName(baseName, locale);
		String resourceName = toResourceName(bundleName, "properties");
		ResourceBundle bundle = null;
		InputStream stream = null;
		
		if (reload) {
			URL url = loader.getResource(resourceName);
			if (url != null) {
				URLConnection connection = url.openConnection();
				if (connection != null) {
					connection.setUseCaches(false);
					stream = connection.getInputStream();
				}
			}
		} else {
			stream = loader.getResourceAsStream(resourceName);
		}
		if (stream != null) {
//			if (autoEncoding) {
//				byte[] data = KaraUtils.readInputStream(stream);
//				stream.close();
//				int ind;
//				for (ind = 0; ind < data.length; ind++) {
//					if ((((int) data[ind]) & 0x00FF) > 0x007f) {
//						break;
//					}
//				}
//				if (ind < data.length) {
//					utfEncoding = true;
//				}
//				stream = new ByteArrayInputStream(data);
//			}
			try {
				// Only this line is changed to make it to read properties files as UTF-8.
				if (utfEncoding == true) {
					bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
				} else {
					bundle = new PropertyResourceBundle(stream);
				}
			} finally {
				stream.close();
			}
		}
		return bundle;
	}

}
