package com.doubleclue.dcem.admin.gui;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TimeZone;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.file.UploadedFile;
import org.primefaces.shaded.commons.io.FilenameUtils;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.TenantBrandingLogic;
import com.doubleclue.dcem.admin.subjects.TenantBrandingSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.TenantBrandingEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.GenericDcemDialog;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.utils.DcemUtils;

@SuppressWarnings("serial")
@Named("tenantBrandingView")
@SessionScoped
public class TenantBrandingView extends DcemView {

	private Logger logger = LogManager.getLogger(TenantBrandingView.class);
	final static String BACKGROUND_TYPE_COLOR = "Color";
	final static String BACKGROUND_TYPE_IMAGE = "Image";
	final static String OTHER_CONTINENT = "Other...";
	final static String OTHER_CONTRIES = "Other Contries...";
	final static long COMPANY_LOGO_MAX = 10 * 1024;
	final static long COMPANY_BACKGROUNDMAGE_MAX = 300 * 1024;

	@Inject
	private TenantBrandingSubject tenantBrandingSubject;

	@Inject
	AdminModule adminModule;
	// PetshopModule petshopModule; //here did changes*****

	@Inject
	OperatorSessionBean operatorSessionBean;

	@Inject
	TenantBrandingLogic tenantBrandingLogic;

	@Inject
	private GenericDcemDialog genericDcemDialog;

	ResourceBundle resourceBundle;

	private UploadedFile fileLogo;

	private UploadedFile fileBackgroundImage;

	private TenantBrandingEntity branding;

	String continentTimezone;

	String countryTimezone;
	
	String logoFileName;
	
	String backgroundFileName;

	public StreamedContent getFileLogoimg() {
		if (branding.getCompanyLogo() == null) {
			return JsfUtils.getEmptyImage();
		}
		return DefaultStreamedContent.builder().contentType("image/png")
				.stream(() -> new ByteArrayInputStream(branding.getCompanyLogo())).build();
	}

	public StreamedContent getfileBackgroundimg() {
		if (branding.getBackgroundImage() == null) {
			return JsfUtils.getEmptyImage();
		}
		return DefaultStreamedContent.builder().contentType("image/png")
				.stream(() -> new ByteArrayInputStream(branding.getBackgroundImage())).build();
	}

	public UploadedFile getFileLogo() {
		return fileLogo;
	}

	public void setFileLogo(UploadedFile fileLogo) {
		this.fileLogo = fileLogo;
	}

	public UploadedFile getFileBackgroundImage() {
		return fileBackgroundImage;
	}

	public void setFileBackgroundImage(UploadedFile fileBackgroundImage) {
		this.fileBackgroundImage = fileBackgroundImage;
	}

	@PostConstruct
	public void init() {
		genericDcemDialog.setParentView(this);
		subject = tenantBrandingSubject;
		resourceBundle = JsfUtils.getBundle(AdminModule.RESOURCE_NAME, operatorSessionBean.getLocale());
		addAutoViewAction(DcemConstants.ACTION_SAVE, resourceBundle, null, null);
		branding = tenantBrandingLogic.getTenantBrandingEntity();
	}

	public void reload() {
		branding = tenantBrandingLogic.getTenantBrandingEntity();
		updateTimeZone();
	}
	
	private void updateTimeZone() {
		countryTimezone = branding.getTimezone();
		continentTimezone = DcemUtils.getContinentFromTimezone(countryTimezone);
	}

	public void fileLogoListener(FileUploadEvent event) {
		branding.setCompanyLogo(event.getFile().getContent());
	    logoFileName = FilenameUtils.getName(event.getFile().getFileName());
		return;
	}
	
	public String getLogoName() {
		return logoFileName;
	}
	
	public void fileBackgroundListener(FileUploadEvent event) {
		branding.setBackgroundImage(event.getFile().getContent());
		backgroundFileName = FilenameUtils.getName(event.getFile().getFileName());
		return;
	}
	
	public String getBackgroundFileName() {
		return backgroundFileName;
	}
		
	public void actionSave() {
		try {
			branding.setTimezone(countryTimezone);
			tenantBrandingLogic.setTenantBrandingEntity(branding);
			DcemUtils.reloadTaskNodes(TenantBrandingLogic.class, TenantIdResolver.getCurrentTenantName());
			JsfUtils.addInformationMessage(AdminModule.RESOURCE_NAME, "tenantBranding.save.ok");
			
			TimeZone timeZone = adminModule.getTimezone();
			ExternalContext ec = JsfUtils.getExternalContext();
			((HttpServletRequest) ec.getRequest()).getSession().setAttribute((String) DcemConstants.SESSION_TIMEZONE, timeZone);	
	        //PrimeFaces.current().executeScript("location.reload(1);");
		} catch (DcemException e) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, e.getLocalizedMessage());
			logger.warn("Tenant Branding couldn't save", e);
		} catch (Exception e) {
			JsfUtils.addErrorMessage(AdminModule.RESOURCE_NAME, "tenantBranding.save.error", e.getMessage());
			logger.warn("Tenant Branding couldn't save", e);
		}
		return;

	}

	public static String getBackgroundTypeColor() {
		return BACKGROUND_TYPE_COLOR;
	}

	public List<SelectItem> getContinentTimezones() {
		return DcemUtils.getContinentTimezones();
	}

	public List<SelectItem> getCountryTimezones() {
		return DcemUtils.getCountryTimezones(continentTimezone);
	}
	
	public void setDefault() {
		branding = new TenantBrandingEntity();
		logoFileName = null;
		backgroundFileName = null;
		updateTimeZone();
	}

	public TenantBrandingEntity getBranding() {
		return branding;
	}

	public void setBranding(TenantBrandingEntity branding) {
		this.branding = branding;
	}

	public boolean isPermissionSave() {
		DcemAction dcemAction = new DcemAction(subject, DcemConstants.ACTION_SAVE);
		DcemAction dcemActionManage = new DcemAction(subject, DcemConstants.ACTION_MANAGE);
		return operatorSessionBean.isPermission(dcemActionManage, dcemAction);
	}

	public String getBackgroundType() {
		if (branding.isBackgroundTypeColor()) {
			return BACKGROUND_TYPE_COLOR;
		}
		return BACKGROUND_TYPE_IMAGE;
	}

	public void setBackgroundType(String type) {
		if (type.equals(BACKGROUND_TYPE_COLOR)) {
			branding.setBackgroundTypeColor(true);

		} else {
			branding.setBackgroundTypeColor(false);
		}
	}

	public void leavingView() {
		branding = null;
	}

	public String getContinentTimezone() {
		return continentTimezone;
	}

	public void setContinentTimezone(String continentTimezone) {
		this.continentTimezone = continentTimezone;
	}

	public void setCountryTimezone(String countryTimezone) {
		this.countryTimezone = countryTimezone;
	}

	public String getCountryTimezone() {
		return countryTimezone;
	}

}
