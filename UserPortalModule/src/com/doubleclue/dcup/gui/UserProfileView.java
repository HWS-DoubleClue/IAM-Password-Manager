package com.doubleclue.dcup.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DcemUserExtension;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcup.logic.UserPortalProfileLogic;
import com.doubleclue.utils.KaraUtils;

@Named("userProfileView")
@SessionScoped
public class UserProfileView extends AbstractPortalView {

	private Logger logger = LogManager.getLogger(UserProfileView.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	private PortalSessionBean portalSessionBean;

	@Inject
	AdminModule adminModule;

	@Inject
	UserPortalProfileLogic profileLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	DcemApplicationBean applicationBean;

	private DcemUser clonedDcemUser;

	@Inject
	DomainLogic domainLogic;

	private String lang;

	private String reg = "([\\+(]?(\\d){2,}[)]?[- \\.]?(\\d){2,}[- \\.]?(\\d){2,}[- \\.]?(\\d){2,}[- \\.]?(\\d){2,})|([\\+(]?(\\d){2,}[)]?[- \\.]?(\\d){2,}[- \\.]?(\\d){2,}[- \\.]?(\\d){2,})|([\\+(]?(\\d){2,}[)]?[- \\.]?(\\d){2,}[- \\.]?(\\d){2,})";

	private byte[] photoProfile;

	private String country;

	private String continentTimezone;

	private String countryTimezone;

	private UploadedFile uploadPhotoProfile;

	public byte[] getPhotoProfile() {
		return photoProfile;
	}

	public void setPhotoProfile(byte[] photoProfile) {
		this.photoProfile = photoProfile;
	}

	public UploadedFile getUploadPhotoProfile() {
		return uploadPhotoProfile;
	}

	public void setUploadPhotoProfile(UploadedFile uploadPhotoProfile) {
		this.uploadPhotoProfile = uploadPhotoProfile;
	}

	@PostConstruct
	public void init() {
		onView();

	}

	@Override
	public String getName() {
		return "userProfileView";
	}

	@Override
	public String getPath() {
		return "userProfileView.xhtml";
	}

	public DcemUser getUser() {
		return clonedDcemUser;
	}

	public void setUser(DcemUser user) {
		this.clonedDcemUser = user;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public void onView() {
		photoProfile = null;
		try {
			clonedDcemUser = (DcemUser) portalSessionBean.getDcemUser().clone();
			DcemUserExtension dcemUserExtension = userLogic.getDcemUserExtension(clonedDcemUser);
			updateTimeZone(dcemUserExtension);
			if (dcemUserExtension != null) {
				country = dcemUserExtension.getCountry();
			}
			if (country == null) {
				if (adminModule.getPreferences().getUserDefaultLanguage() == SupportedLanguage.German) {
					country = "DE";
				} else {
					country = "US";
				}
			}
			setLang(portalSessionBean.getDcemUser().getLanguage().name());
		} catch (CloneNotSupportedException e) {

		}
		// System.out.println("UserProfileView.onView() " +
		// clonedDcemUser.getJpaVersion());
	}

	public void onExit() {
		photoProfile = null;
	}

	public void actionSave() throws DcemException {
		clonedDcemUser.setLanguage(SupportedLanguage.valueOf(lang));
		if (validator()) {
			try {
				DcemUserExtension dcemUserExtension = new DcemUserExtension();
				dcemUserExtension.setPhoto(photoProfile);
				dcemUserExtension.setCountry(country);
				dcemUserExtension.setTimezoneString(countryTimezone);
				profileLogic.updateUserProfile(clonedDcemUser, dcemUserExtension);
				DcemUser dcemUser = portalSessionBean.updateUser();
				if (dcemUser.isDomainUser() == true && photoProfile != null) {
					domainLogic.changeUserPhotoProfile(dcemUser, photoProfile, null);
				}
				photoProfile = null;
				portalSessionBean.changeLanguage(SupportedLanguage.toLanguageKey(lang));
				onView();
				JsfUtils.addInfoMessage(
						portalSessionBean.getResourceBundle().getString("userProfile.saveSuccessfully"));
			} catch (DcemException exp) {
				logger.warn("Error at profile save", exp);
				if (exp.getErrorCode() == DcemErrorCodes.CONSTRAIN_VIOLATION_DB) {
					JsfUtils.addErrorMessage(
							portalSessionBean.getResourceBundle().getString("error.REGISTRATION_USER_ALREADY_EXISTS"));
				} else {
					JsfUtils.addErrorMessage(portalSessionBean.getErrorMessage(exp));
					logger.error(exp.getLocalizedMessage());
				}
			} catch (Exception e) {
				logger.warn("Error at profile save", e);
				JsfUtils.addErrorMessage(e.toString());
			}
		}
	}

	private boolean validator() {
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = null;
		Matcher matcherPhone = null;
		if (clonedDcemUser.isDomainUser() == false) {
			if (clonedDcemUser.getMobileNumber() != null) {
				matcher = pattern.matcher(clonedDcemUser.getMobileNumber());
			}
			if (clonedDcemUser.getTelephoneNumber() != null) {
				matcherPhone = pattern.matcher(clonedDcemUser.getTelephoneNumber());
			}
			if (clonedDcemUser.getLoginId().isEmpty()) {
				JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.MISSING_USERNAME"));
				return false;
			}
			if (clonedDcemUser.getDisplayName().isEmpty()) {
				JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.MISSING_DISPLAYNAME"));
				return false;
			}
			if (clonedDcemUser.getEmail().isEmpty()) {
				JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.MISSING_EMAIL"));
				return false;
			} else if (KaraUtils.isEmailValid(clonedDcemUser.getEmail()) == false) {
				JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.INVALID_EMAIL"));
				return false;
			}
			if (clonedDcemUser.getMobileNumber() != null && clonedDcemUser.getMobileNumber().isEmpty() == false) {
				if (matcher.matches() == false) {
					JsfUtils.addErrorMessage(
							portalSessionBean.getResourceBundle().getString("error.INVALID_MOBILE_NUMBER"));
					return false;
				}
			}
			if (clonedDcemUser.getTelephoneNumber() != null && clonedDcemUser.getTelephoneNumber().isEmpty() == false) {
				if (matcherPhone.matches() == false) {
					JsfUtils.addErrorMessage(
							portalSessionBean.getResourceBundle().getString("error.INVALID_PHONE_NUMBER"));
					return false;
				}
			}
		}
		if (clonedDcemUser.getPrivateMobileNumber() != null
				&& clonedDcemUser.getPrivateMobileNumber().isEmpty() == false) {
			Matcher matcherPrv = pattern.matcher(clonedDcemUser.getPrivateMobileNumber());
			if (matcherPrv.matches() == false) {
				JsfUtils.addErrorMessage(
						portalSessionBean.getResourceBundle().getString("error.INVALID_PRIVATE_MOBILE_NUMBER"));
				return false;
			}
		}
		if (clonedDcemUser.getPrivateEmail() != null && clonedDcemUser.getPrivateEmail().isEmpty() == false) {
			if (KaraUtils.isEmailValid(clonedDcemUser.getEmail()) == false) {
				JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.INVALID_EMAIL"));
				return false;
			}
		}
		if (lang == null) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.NO_LANGUAGE_SELECTED"));
			return false;
		}
		return true;
	}

	public List<SelectItem> getAvailableCountries() {
		return applicationBean.getAvailableCountries(portalSessionBean.getLocale());
	}

	public StreamedContent getPhotoUserProfile() {
		DcemUserExtension userExtension = clonedDcemUser.getDcemUserExt();
		if (photoProfile != null) {
			InputStream in = new ByteArrayInputStream(photoProfile);
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
		} else if (userExtension != null && userExtension.getPhoto() != null) {
			InputStream in = new ByteArrayInputStream(userExtension.getPhoto());
			return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
		} else {
			return JsfUtils.getDefaultUserImage();
		}
	}

	public void photoProfileListener(FileUploadEvent event) {
		if (event == null) {
			return;
		}
		try {
			photoProfile = DcemUtils.resizeImage(event.getFile().getContent(), DcemConstants.IMAGE_MAX);
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(e.getLocalizedMessage());
			logger.error("upload photo failed " + e.toString());
		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			logger.error("upload photo failed " + e.toString());
		}
		return;
	}

	private void updateTimeZone(DcemUserExtension dcemUserExtension) {
		TimeZone timeZone = DcemUtils.getUserTimeZone(dcemUserExtension, adminModule.getTimezone());
		String[] continentAndCountry = DcemUtils.getContinentAndIdFromTimezone(timeZone);
		continentTimezone = continentAndCountry[0];
		countryTimezone = continentAndCountry[1];
	}

	public List<SelectItem> getContinentTimezones() {
		return DcemUtils.getContinentTimezones();
	}

	public List<SelectItem> getCountryTimezones() {
		return DcemUtils.getCountryTimezones(continentTimezone);
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getContinentTimezone() {
		return continentTimezone;
	}

	public void setContinentTimezone(String continentTimezone) {
		this.continentTimezone = continentTimezone;
	}

	public String getCountryTimezone() {
		return countryTimezone;
	}

	public void setCountryTimezone(String countryTimezone) {
		this.countryTimezone = countryTimezone;
	}

}