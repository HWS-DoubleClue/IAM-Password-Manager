package com.doubleclue.dcem.oauth.sso.gui;

import java.io.Serializable;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

import com.doubleclue.oauth.oauth2.OAuthRequest;
import com.doubleclue.oauth.oauth2.enums.OAuthParam;

@SuppressWarnings("serial")
@SessionScoped
@Named("oauthReturnView")
public class OAuthReturnView implements Serializable {

	public class FormItem {

		private final String name;
		private final String value;

		public FormItem(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}
	}

	private FormItem[] formItems = new FormItem[0];
	private String redirectUri = null;

	public void setResponse(OAuthRequest response, String redirectUri) {
		Map<OAuthParam, Object> paramMap = response.getParamMap();
		Map<String, Object> customParamMap = response.getCustomParamMap();
		formItems = new FormItem[paramMap.size() + customParamMap.size()];
		int i = 0;
		for (OAuthParam param : paramMap.keySet()) {
			formItems[i] = new FormItem(param.toString(), paramMap.get(param).toString());
			i++;
		}
		for (String param : customParamMap.keySet()) {
			formItems[i] = new FormItem(param, customParamMap.get(param).toString());
			i++;
		}
		this.redirectUri = redirectUri;
	}

	public FormItem[] getFormItems() {
		return formItems;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

}
