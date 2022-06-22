package com.doubleclue.oauth.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

public class OAuthUtils {

	public static String getJsonFromMaps(Map<?, ?>[] maps) {
		StringBuilder sb = new StringBuilder("{");
		for (Map<?, ?> map : maps) {
			for (Object key : map.keySet()) {
				if (sb.length() > 1) {
					sb.append(",");
				}
				sb.append("\"" + key + "\": ");
				sb.append(getJsonValue(map.get(key)));
			}
		}
		sb.append("}");
		return sb.toString();
	}

	private static String getJsonValue(Object o) {
		if (o == null) {
			return "null";
		} else {
			Class<?> valueClass = o.getClass();
			if (valueClass.isArray()) {
				StringBuilder sb = new StringBuilder("[");
				for (Object item : (Object[]) o) {
					if (sb.length() > 1) {
						sb.append(", ");
					}
					sb.append(getJsonValue(item));
				}
				sb.append("]");
				return sb.toString();
			} else if (valueClass.equals(String.class)) {
				return "\"" + o + "\"";
			} else {
				return o.toString();
			}
		}
	}

	public static String getQueryStringFromMaps(Map<?, ?>[] maps) {
		StringBuilder sb = new StringBuilder();
		try {
			for (Map<?, ?> map : maps) {
				for (Object key : map.keySet()) {
					Object value = map.get(key);
					if (value != null) {
						if (sb.length() > 0) {
							sb.append("&");
						}
						sb.append(key + "=");
						sb.append(value.getClass().equals(String.class) ? URLEncoder.encode((String) value, "UTF-8") : value);
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
}
