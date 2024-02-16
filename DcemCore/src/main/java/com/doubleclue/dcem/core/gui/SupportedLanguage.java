package com.doubleclue.dcem.core.gui;

import java.util.Locale;

/**
 * @author eg
 * 
 * Operator locales stored in DB
 *  DO NOT CHANGE THE ORDINALS NUMBERS !!
 */
public enum SupportedLanguage {

	/*
	 *  DO NOT CHANGE THE ORDINALS !!
	 */
	English(new Locale("en")),
	French(new Locale("fr")),
	German(new Locale("de")),
	Italian(new Locale("it")),
	English_UK(Locale.UK);
	

	Locale locale;

	private SupportedLanguage(Locale locale) {
		this.locale = locale;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public static SupportedLanguage fromLocale(Locale locale) {
		for (SupportedLanguage b : SupportedLanguage.values()) {
			if (b.locale.getLanguage().equals(locale.getLanguage())) {
				return b;
			}
		}
		return SupportedLanguage.English;
	}

	public static String toLanguageName(String langKey) {
		for (SupportedLanguage b : SupportedLanguage.values()) {
			if (String.valueOf(b.locale.toString()).equals(langKey)) {
				return b.toString();
			}
		}
		return null;
	}

	public static String toLanguageKey(String langName) {
		for (SupportedLanguage b : SupportedLanguage.values()) {
			if (String.valueOf(b).equals(langName)) {
				return b.locale.toString();
			}
		}
		return null;
	}
	
	public static Locale toLocale(String langName) {
		for (SupportedLanguage b : SupportedLanguage.values()) {
			if (String.valueOf(b).equals(langName)) {
				return b.locale;
			}
		}
		return null;
	}
	
	public boolean equals (Object object1, Object object2) {
		if (object1 == null || object2 == null) {
			return false;
		}
		return ((SupportedLanguage)object1).getLocale().equals(((SupportedLanguage)object2).getLocale());
	}
}