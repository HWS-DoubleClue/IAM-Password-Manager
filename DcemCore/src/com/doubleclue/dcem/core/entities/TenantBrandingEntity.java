package com.doubleclue.dcem.core.entities;

import java.util.TimeZone;

public class TenantBrandingEntity {

	private String timezone = TimeZone.getDefault().getID();

	private String signInPageText;

	private String signTextStyleCSS;

	private byte[] companyLogo;

	private boolean backgroundTypeColor = true;

	private byte[] backgroundImage;

	private String backgroundColor = "#f0f8ff";

	final static String BANNER_STYLE_CSS = "color: #ffffff;vertical-align: middle !important;font-size: large;background-color: #005078;text-align: center;";

	private String bannerStyleCSS = BANNER_STYLE_CSS;

	private String bannerStyleCSSsaml = BANNER_STYLE_CSS;

	private String bannerStyleCSSOauth = BANNER_STYLE_CSS;

	private String bannerStyleCSSUserPortal = "background-color: #bae2f5; text-align:center; font-size:large;";

	private String bannerTextEnterpriseManagment;

	private String bannerTextUserPortal;

	public boolean isBackgroundTypeColor() {
		return backgroundTypeColor;
	}

	public void setBackgroundTypeColor(boolean backgroundTypeColor) {
		this.backgroundTypeColor = backgroundTypeColor;
	}

	public String getSignInPageText() {
		return signInPageText;
	}

	public void setSignInPageText(String signInPageText) {
		this.signInPageText = signInPageText;
	}

	public byte[] getCompanyLogo() {
		return companyLogo;
	}

	public void setCompanyLogo(byte[] companyLogo) {
		this.companyLogo = companyLogo;
	}

	public byte[] getBackgroundImage() {
		return backgroundImage;
	}

	public void setBackgroundImage(byte[] backgroundImage) {
		this.backgroundImage = backgroundImage;
	}

	public String getBackgroundColor() {
		return backgroundColor;
	}

	public void setBackgroundColor(String backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	public String getBannerStyleCSS() {
		return bannerStyleCSS;
	}

	public void setBannerStyleCSS(String bannerStyleCSS) {
		this.bannerStyleCSS = bannerStyleCSS;
	}

	public String getBannerTextEnterpriseManagment() {
		return bannerTextEnterpriseManagment;
	}

	public void setBannerTextEnterpriseManagment(String bannerTextEnterpriseManagment) {
		this.bannerTextEnterpriseManagment = bannerTextEnterpriseManagment;
	}

	public String getSignTextStyleCSS() {
		return signTextStyleCSS;
	}

	public void setSignTextStyleCSS(String signTextStyleCSS) {
		this.signTextStyleCSS = signTextStyleCSS;
	}

	public String getTimezone() {
		return timezone;
	}

	public void setTimezone(String timezone) {
		this.timezone = timezone;
	}

	public String getBannerTextUserPortal() {
		return bannerTextUserPortal;
	}

	public void setBannerTextUserPortal(String bannerTextUserPortal) {
		this.bannerTextUserPortal = bannerTextUserPortal;
	}

	public String getBannerStyleCSSUserPortal() {
		return bannerStyleCSSUserPortal;
	}

	public void setBannerStyleCSSUserPortal(String bannerStyleCSSUserPortal) {
		this.bannerStyleCSSUserPortal = bannerStyleCSSUserPortal;
	}

	public String getBannerStyleCSSsaml() {
		return bannerStyleCSSsaml;
	}

	public void setBannerStyleCSSsaml(String bannerStyleCSSsaml) {
		this.bannerStyleCSSsaml = bannerStyleCSSsaml;
	}

	public String getBannerStyleCSSOauth() {
		return bannerStyleCSSOauth;
	}

	public void setBannerStyleCSSOauth(String bannerStyleCSSOauth) {
		this.bannerStyleCSSOauth = bannerStyleCSSOauth;
	}

	@Override
	public String toString() {
		return "TenantBrandingEntity [timezones=" + timezone + ", signInPageText=" + signInPageText + ", companyLogo=" + companyLogo + ", backgroundImage="
				+ backgroundImage + ", backgroundColor=" + backgroundColor + ", backgroundTypeColor=" + backgroundTypeColor + ", bannerStyleCSS="
				+ bannerStyleCSS + ", bannerTextEnterpriseManagment=" + bannerTextEnterpriseManagment + ", signTextStyleCSS=" + signTextStyleCSS
				+ ",bannerTextUserPortal=" + bannerTextUserPortal + ",bannerStyleCSSUserPortal=" + bannerStyleCSSUserPortal + ",bannerStyleCSSsaml="
				+ bannerStyleCSSsaml + ",bannerStyleCSSOauth=" + bannerStyleCSSOauth + "]";
	}

}
