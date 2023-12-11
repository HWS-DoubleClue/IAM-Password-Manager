package com.doubleclue.dcem.admin.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
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
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.utils.KaraUtils;

@Named("userProfileDialog")
@SessionScoped
public class UserProfileDialog extends DcemDialog {

	private Logger logger = LogManager.getLogger(UserProfileDialog.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
	private OperatorSessionBean operatorSessionBean;

	@Inject
	AdminModule adminModule;

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

	private boolean defaultTimezone;

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

	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		photoProfile = null;
		try {
			clonedDcemUser = (DcemUser) operatorSessionBean.getDcemUser().clone();
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
			setLang(operatorSessionBean.getDcemUser().getLanguage().name());
		} catch (CloneNotSupportedException e) {

		}
	}

	public void onExit() {
		photoProfile = null;
	}

	public void actionSave() throws DcemException {
		clonedDcemUser.setLanguage(SupportedLanguage.valueOf(lang));
		ResourceBundle resourceBundle = JsfUtils.getBundle(AdminModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		if (validator()) {
			try {
				DcemUserExtension dcemUserExtension = new DcemUserExtension();
				dcemUserExtension.setPhoto(photoProfile);
				dcemUserExtension.setCountry(country);
				if (defaultTimezone) {
					dcemUserExtension.setTimezoneString(null);
				} else {
					dcemUserExtension.setTimezoneString(countryTimezone);
				}
				userLogic.updateUserProfile(clonedDcemUser, dcemUserExtension);
				DcemUser dcemUser = operatorSessionBean.refeshUser();
				if (dcemUser.isDomainUser() == true && photoProfile != null) {
					domainLogic.changeUserPhotoProfile(dcemUser, photoProfile, null);
				}
				photoProfile = null;
				FacesContext.getCurrentInstance().getViewRoot().setLocale(dcemUser.getLanguage().getLocale());
				JsfUtils.addInfoMessage(resourceBundle.getString("userProfile.saveSuccessfully"));
			} catch (DcemException exp) {
				logger.warn("Error at profile save", exp);
				if (exp.getErrorCode() == DcemErrorCodes.CONSTRAIN_VIOLATION_DB) {
					JsfUtils.addErrorMessage(resourceBundle.getString("error.REGISTRATION_USER_ALREADY_EXISTS"));
				} else {
					JsfUtils.addErrorMessage(exp.getLocalizedMessage());
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
		ResourceBundle resourceBundle = JsfUtils.getBundle(AdminModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		if (clonedDcemUser.isDomainUser() == false) {
			if (clonedDcemUser.getMobileNumber() != null) {
				matcher = pattern.matcher(clonedDcemUser.getMobileNumber());
			}
			if (clonedDcemUser.getTelephoneNumber() != null) {
				matcherPhone = pattern.matcher(clonedDcemUser.getTelephoneNumber());
			}
			if (clonedDcemUser.getLoginId().isEmpty()) {
				JsfUtils.addErrorMessage(resourceBundle.getString("error.MISSING_USERNAME"));
				return false;
			}
			if (clonedDcemUser.getDisplayName().isEmpty()) {
				JsfUtils.addErrorMessage(resourceBundle.getString("error.MISSING_DISPLAYNAME"));
				return false;
			}
			if (clonedDcemUser.getEmail().isEmpty()) {
				JsfUtils.addErrorMessage(resourceBundle.getString("error.MISSING_EMAIL"));
				return false;
			} else if (KaraUtils.isEmailValid(clonedDcemUser.getEmail()) == false) {
				JsfUtils.addErrorMessage(resourceBundle.getString("error.INVALID_EMAIL"));
				return false;
			}
			if (clonedDcemUser.getMobileNumber() != null && clonedDcemUser.getMobileNumber().isEmpty() == false) {
				if (matcher.matches() == false) {
					JsfUtils.addErrorMessage(resourceBundle.getString("error.INVALID_MOBILE_NUMBER"));
					return false;
				}
			}
			if (clonedDcemUser.getTelephoneNumber() != null && clonedDcemUser.getTelephoneNumber().isEmpty() == false) {
				if (matcherPhone.matches() == false) {
					JsfUtils.addErrorMessage(resourceBundle.getString("error.INVALID_PHONE_NUMBER"));
					return false;
				}
			}
		}
		if (clonedDcemUser.getPrivateMobileNumber() != null
				&& clonedDcemUser.getPrivateMobileNumber().isEmpty() == false) {
			Matcher matcherPrv = pattern.matcher(clonedDcemUser.getPrivateMobileNumber());
			if (matcherPrv.matches() == false) {
				JsfUtils.addErrorMessage(resourceBundle.getString("error.INVALID_PRIVATE_MOBILE_NUMBER"));
				return false;
			}
		}
		if (clonedDcemUser.getPrivateEmail() != null && clonedDcemUser.getPrivateEmail().isEmpty() == false) {
			if (KaraUtils.isEmailValid(clonedDcemUser.getEmail()) == false) {
				JsfUtils.addErrorMessage(resourceBundle.getString("error.INVALID_EMAIL"));
				return false;
			}
		}
		if (lang == null) {
			JsfUtils.addErrorMessage(resourceBundle.getString("error.NO_LANGUAGE_SELECTED"));
			return false;
		}
		return true;
	}

	public List<SelectItem> getAvailableCountries() {
		return applicationBean.getAvailableCountries(operatorSessionBean.getLocale());
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
		TimeZone timeZone;
		if (dcemUserExtension == null || dcemUserExtension.getTimezone() == null) {
			defaultTimezone = true;
			timeZone = adminModule.getTimezone();
		} else {
			defaultTimezone = false;
			timeZone = dcemUserExtension.getTimezone();
		}
		setContinentAndCountryTimezone(timeZone);
	}

	private void setContinentAndCountryTimezone(TimeZone timeZone) {
		countryTimezone = timeZone.getID();
		continentTimezone = DcemUtils.getContinentFromTimezone(countryTimezone);
	}

	public List<SelectItem> getContinentTimezones() {
		if (defaultTimezone) {
			setContinentAndCountryTimezone(adminModule.getTimezone());
		}
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

	public boolean isDefaultTimezone() {
		return defaultTimezone;
	}

	public void setDefaultTimezone(boolean defaultTimezone) {
		this.defaultTimezone = defaultTimezone;
	}

}