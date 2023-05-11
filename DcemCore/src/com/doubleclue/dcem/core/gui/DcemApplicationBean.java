package com.doubleclue.dcem.core.gui;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.app.sec.api.SecureUtilsApi;
import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ClusterConfig;
import com.doubleclue.dcem.core.config.ConnectionService;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemNode;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.FilterOperator;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.licence.LicenceLogic;
import com.doubleclue.dcem.core.logic.ActionLogic;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.DcFreeMarkerStringLoader;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.TenantLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.tasks.CallInittializeTenant;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.utils.SecureUtilsImpl;
import com.doubleclue.dcem.system.logic.NodeLogic;
import com.doubleclue.dcem.system.logic.NodeState;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.ProductVersion;
import com.doubleclue.utils.SecureUtils;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;
import jersey.repackaged.com.google.common.collect.Lists;
import jersey.repackaged.com.google.common.net.InetAddresses;

/**
 * 
 * @author Emanuel Galea
 *
 */
@Named("dcemApplication")
@ApplicationScoped
public class DcemApplicationBean implements Serializable {

	@Inject
	@Any
	Instance<SubjectAbs> subjects;

	@Inject
	ViewNavigator viewNavigator;

	@Inject
	ActionLogic actionLogic;

	@Inject
	NodeLogic nodeLogic;

	@Inject
	ConfigLogic configLogic;

	@Inject
	TenantLogic tenantLogic;

	@Inject
	TaskExecutor taskExecutor;

	@Inject
	DomainLogic domainLogic;

	@Inject
	AdminModule adminModule;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	LicenceLogic licenceLogic;

	ServletContext servletContext;

	public static boolean debugMode = false;
	public static boolean jUnitTestMode = false;

	private static List<DcemException> initExceptions = new ArrayList<>(1);

	private static final long serialVersionUID = 1L;
	private final static Logger logger = LogManager.getLogger(DcemApplicationBean.class);
	private HashMap<String, SortedSet<SubjectAbs>> viewsMap; // key is the moduleId
	private HashMap<String, TenantEntity> tenantMap = new HashMap<>();

	private List<SelectItem> numberOparators;
	private List<SelectItem> booleanOparator;
	private List<SelectItem> dateOparators;
	private ProductVersion productVersion;

	private HashMap<String, String> fileIconsMap = new HashMap<String, String>();

	private boolean captchaOn;

	Configuration freeMarkerConfiguration;

	@Inject
	@Any
	Instance<DcemModule> modules;

	@Inject
	private transient @Any Event<DcemAction> EventDcemAction;
	/**
	 * 
	 */
	private Map<String, DcemModule> modulesControlMap = new HashMap<String, DcemModule>();

	List<DcemModule> sortedModules = Collections.synchronizedList(new ArrayList<DcemModule>());

	Map<Integer, DcemNode> dcemNodes = new ConcurrentHashMap<>();

	@PostConstruct
	public void init() {
		Iterator<DcemModule> modulesIterator = modules.iterator();
		while (modulesIterator.hasNext()) {
			DcemModule dcemModule = modulesIterator.next();
			try {
				dcemModule.init();
			} catch (DcemException e) {
				e.printStackTrace();
				continue;
			}
			sortedModules.add(dcemModule);
		}

		Collections.sort(sortedModules, new Comparator<DcemModule>() {
			public int compare(DcemModule sasModule1, DcemModule sasModule2) {
				return sasModule1.getRank() - sasModule2.getRank();
			}
		});
		updateViewsMap();
		fileIconsMap.put("jpg", "picture-file.svg");
		fileIconsMap.put("png", "picture-file.svg");
		fileIconsMap.put("kdbx", "keepass-icon.png");
		fileIconsMap.put("pdf", "pdf-file.svg");
		fileIconsMap.put("txt", "txt-file.png");
		fileIconsMap.put("pptx", "powerpoint-file.png");
		fileIconsMap.put("ppt", "powerpoint-file.png");
		fileIconsMap.put("xlsx", "excel-file.png");
		fileIconsMap.put("xls", "excel-file.png");
		fileIconsMap.put("docx", "word-file.png");
		fileIconsMap.put("doc", "word-file.png");
		fileIconsMap.put("zip", "zip-file.svg");
		fileIconsMap.put("cer", "cer-file.svg");
		fileIconsMap.put("html", "html-file.svg");
		fileIconsMap.put("xhtml", "html-file.svg");
		fileIconsMap.put("msg", "msg-file.png");
		fileIconsMap.put("exe", "exe-file.svg");
	}

	/**
	 * 
	 */
	public void startModules() {
		for (DcemModule module : sortedModules) {
			try {
				module.start();
			} catch (DcemException e) {
				reportingLogic.addWelcomeViewAlert(module.getName(), DcemErrorCodes.STARTING_MODULE,
						(e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : e.getErrorCode().toString(), AlertSeverity.ERROR, false);
				logger.warn("Failed to start module: " + module.toString(), e);
			}
		}
	}

	/**
	 * 
	 */
	public void stopModules() {
		for (DcemModule module : modulesControlMap.values()) {
			try {
				module.stop();
			} catch (DcemException e) {
				reportingLogic.addWelcomeViewAlert(module.getName(), DcemErrorCodes.STOPPING_MODULE,
						(e.getMessage() != null && !e.getMessage().isEmpty()) ? e.getMessage() : e.getErrorCode().toString(), AlertSeverity.WARNING, false);
				logger.warn("Failed to stop module: " + module.toString(), e);
			}
		}
	}

	/**
	 * 
	 */
	public DcemModule getDefaultModule() {
		for (DcemModule module : Lists.reverse(sortedModules)) {
			if (module.getDefaultView() != null) {
				return module;
			}
		}
		return null;
	}

	@DcemTransactional
	public void initialize(ServletContext servletContext) throws DcemException {
		configLogic.getClusterConfig(); // put it to cache
		this.servletContext = servletContext;
		SecureUtilsApi secureUtilsApi;
		try {
			secureUtilsApi = (SecureUtilsApi) Class.forName(SecureUtilsImpl.class.getName()).newInstance();
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Cant load the secureutilApi", e);
		}
		SecureUtils.setSecureUtilsImpl(secureUtilsApi);

		dateOparators = new ArrayList<SelectItem>(5);
		dateOparators.add(new SelectItem(FilterOperator.NONE, ""));
		dateOparators.add(new SelectItem(FilterOperator.EQUALS, "="));
		dateOparators.add(new SelectItem(FilterOperator.GREATER, ">"));
		dateOparators.add(new SelectItem(FilterOperator.LESSER, "<"));
		// dateOparators.add(new SelectItem(FilterOperator.NOT_EQUALS, "\u2260"));
		// dateOparators.add(new SelectItem(FilterOperator.LESS_NOW, "< Now"));
		dateOparators.add(new SelectItem(FilterOperator.BETWEEN, "()"));

		numberOparators = new ArrayList<SelectItem>(5);
		numberOparators.add(new SelectItem(FilterOperator.NONE, ""));
		numberOparators.add(new SelectItem(FilterOperator.EQUALS, "="));
		numberOparators.add(new SelectItem(FilterOperator.GREATER, ">"));
		numberOparators.add(new SelectItem(FilterOperator.LESSER, "<"));
		numberOparators.add(new SelectItem(FilterOperator.NOT_EQUALS, "\u2260"));

		//
		booleanOparator = new ArrayList<SelectItem>(3);
		booleanOparator.add(new SelectItem(null, ""));
		booleanOparator.add(new SelectItem(Boolean.TRUE, "True"));
		booleanOparator.add(new SelectItem(Boolean.FALSE, "False"));
		//
		try {
			productVersion = KaraUtils.getProductVersion(DcemApplicationBean.class);
			productVersion.setAppName("DCEM");
		} catch (Exception exp) {
			logger.error("Couldn't retrieve Product Version", exp);
		}
		nodeLogic.setNodeState(DcemCluster.getInstance().getNodeName(), NodeState.Active);
		updateNodeMap();
		populateInitModuleList();
		updateInitializeTenantMap();
	}

	private void updateViewsMap() {
		viewsMap = new HashMap<String, SortedSet<SubjectAbs>>();
		Iterator<SubjectAbs> subjectIter = subjects.iterator();
		SortedSet<SubjectAbs> moduleViewList;
		while (subjectIter.hasNext()) {
			SubjectAbs subject = subjectIter.next();
			moduleViewList = viewsMap.get(subject.getModuleId());
			if (moduleViewList == null) {
				moduleViewList = new TreeSet<SubjectAbs>(new ViewComparator());
				viewsMap.put(subject.getModuleId(), moduleViewList);
			}
			moduleViewList.add(subject);
		}
	}

	public void updateNodeMap() {
		List<DcemNode> nodes = nodeLogic.getNodes();
		for (DcemNode dcemNode : nodes) {
			dcemNodes.put(dcemNode.getId(), dcemNode);
		}
	}

	public DcemNode getDcemNodeById(int id) {
		return dcemNodes.get(id);
	}

	public String getVersion() {
		if (productVersion == null) {
			try {
				productVersion = KaraUtils.getProductVersion(JsfUtils.class);
				return productVersion.getVersionStr();
			} catch (Exception exp) {
				logger.error("Couldn't retrieve Product Version", exp);
			}
			return null;
		}
		return productVersion.getVersionStr();
	}

	/**
	 * @param moduleId
	 * @param viewName
	 * @return
	 */
	public SubjectAbs getSubjectByName(String moduleId, String subjectName) {

		SortedSet<SubjectAbs> subjects = viewsMap.get(moduleId);
		if (subjects == null) {
			return null;
		} else {
			for (SubjectAbs view : subjects) {
				if (view.getName().equals(subjectName)) {
					return view;
				}
			}
		}
		return null;
	}

	/**
	 * @param activeModule
	 * @return
	 */
	public SortedSet<SubjectAbs> getModuleSubjects(DcemModule activeModule) {
		if (activeModule == null) {
			return null;
		}
		return viewsMap.get(activeModule.getId());
	}

	public List<DcemException> getInitMessages() {
		return initExceptions;

	}

	public static void addInitException(DcemException dcemException) {
		logger.error("INITIALIZATION ERROR", dcemException);
		initExceptions.add(dcemException);
	}

	public static List<DcemException> getInitExceptions() {
		return initExceptions;
	}

	/**
	 * Destroy the session
	 * 
	 * @return logout
	 */
	public String logout() {
		// the external context
		ExternalContext extCon = FacesContext.getCurrentInstance().getExternalContext();
		HttpSession session = (HttpSession) extCon.getSession(true);
		// close the session
		session.invalidate();
		return "logout";
	}

	/**
	 * @param allActions
	 * @throws DcemException
	 */
	private void populateInitModuleList() throws DcemException {
		/**
		 * fill module control and module info list
		 */
		Map<String, DcemModule> moduleControlMap = new HashMap<String, DcemModule>();
		for (DcemModule dcemModule : sortedModules) {
			dcemModule.init();
			moduleControlMap.put(dcemModule.getId(), dcemModule);
		}
		modulesControlMap = Collections.unmodifiableMap(moduleControlMap);
	}

	public List<DcemModule> getSortedModules() {
		return sortedModules;
	}

	public List<DcemModule> getSortedEnabledModules() {
		String[] disabledModules = adminModule.getAdminTenantData().getDisabledModules();
		String[] pluginModules = adminModule.getAdminTenantData().getEnabledPluginModules();
		if (disabledModules == null || disabledModules.length == 0) {
			return sortedModules;
		}
		List<DcemModule> list = new ArrayList<DcemModule>(sortedModules.size());
		boolean disabled;
		for (DcemModule dcemModule : sortedModules) {
			disabled = false;
			for (String id : disabledModules) {
				if (dcemModule.getId().compareToIgnoreCase(id) == 0) {
					disabled = true;
					break;
				}
			}
			if (disabled == false) {
				if (dcemModule.isPluginModule() == true) {
					if (pluginModules != null) {
						if (pluginModules.length > 0 && pluginModules[0].compareToIgnoreCase("all") == 0) {
							list.add(dcemModule);
						} else {
							for (String id : pluginModules) {
								if (dcemModule.getId().compareToIgnoreCase(id) == 0) {
									list.add(dcemModule);
									break;
								}
							}
						}
					}
				} else {
					list.add(dcemModule);
				}
			}
		}
		return list;
	}

	public DcemModule getModule(String moduleId) {
		return modulesControlMap.get(moduleId);
	}

	/**
	 * @param id
	 * @return
	 */
	public String getModuleName(String id) {
		DcemModule dcemModule = modulesControlMap.get(id);
		if (dcemModule == null) {
			return null;
		} else {
			return dcemModule.getName();
		}
	}

	/**
	 * @param id
	 * @return
	 */
	public String getSubjectName(String moduleId, String subjectName) {
		if (subjectName.equals(DcemConstants.EMPTY_SUBJECT_NAME)) {
			return subjectName;
		}
		SortedSet<SubjectAbs> subjects = viewsMap.get(moduleId);
		if (subjects != null) {
			for (SubjectAbs subject : subjects) {
				if (subject.getName().equals(subjectName)) {
					return subject.getDisplayName();
				}
			}
		}
		return null;
	}

	public List<SelectItem> getNumberOperators() {
		return numberOparators;
	}

	public List<SelectItem> getBooleanOperators() {
		return booleanOparator;
	}

	public List<SelectItem> getDateOparators() {
		return dateOparators;
	}

	public ServletContext getServletContext() {
		return servletContext;
	}

	public ProductVersion getProductVersion() {
		return productVersion;
	}

	public void updateInitializeTenantMap() throws DcemException {

		List<TenantEntity> dbTenants = tenantLogic.getAllTenants();
		dbTenants.add(0, TenantIdResolver.getMasterTenant());
		// first we remove non-existing tenants
		List<TenantEntity> tenantList = new ArrayList<>(tenantMap.values());
		for (TenantEntity tenantEntity : tenantList) {
			if (dbTenants.contains(tenantEntity) == false) {
				tenantMap.remove(tenantEntity.getName().toUpperCase());
			}
		}

		for (TenantEntity tenantEntity : dbTenants) {
			if (tenantMap.get(tenantEntity.getName().toUpperCase()) == null) {
				// New Tenant
				tenantMap.put(tenantEntity.getName().toUpperCase(), tenantEntity);
				Future<Exception> future = taskExecutor.submit(new CallInittializeTenant(tenantEntity, sortedModules));
				try {
					Exception exp = future.get();
					if (exp != null) {
						throw exp;
					}
				} catch (Exception e) {
					String msg = "Error on initialization Tenant: " + tenantEntity.getName() + " Cause: " + e.toString();
					logger.fatal(msg, e);
					reportingLogic.addWelcomeViewAlert(DcemConstants.ALERT_CATEGORY_DCEM, DcemErrorCodes.UNEXPECTED_ERROR,
							"TENANT " + tenantEntity.getName() + " : " + msg, AlertSeverity.ERROR, false);
					// throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "Cant Initialize
					// Tenant: " + tenantEntity.getName(), e);
				}
				// for (TenantEntity tenantEntity : tenantMap.values()) {
				// future = taskExecutor.submit(new CallInittializeTenant(tenantEntity,
				// sortedModules, addActions));
				// try {
				// future.get();
				// } catch (Exception e) {
				// logger.error(e);
				// }
				// }
			}

		}
	}

	private URL[] dcemUrls;

	private URL[] getDcemUrls() throws DcemException {
		if (dcemUrls == null) {
			String dcemHostDomainNamesString = DcemCluster.getInstance().getClusterConfig().getDcemHostDomainName();
			if (dcemHostDomainNamesString != null && dcemHostDomainNamesString.isEmpty() == false) {
				dcemUrls = validateDcemUrls(dcemHostDomainNamesString);
			} else {
				dcemUrls = new URL[1];
				try {
					dcemUrls[0] = new URL("https://Your-Host-Name");
				} catch (MalformedURLException e) {
					logger.warn("Failed url parsing", e);
				}
			}
		}
		return dcemUrls;
	}

	public URL[] validateDcemUrls(String dcemHostDomainNamesString) throws DcemException {
		if (dcemHostDomainNamesString == null || dcemHostDomainNamesString.isEmpty()) {
			return null;
		}
		String urlsStr[] = dcemHostDomainNamesString.split(";");
		URL urls[] = new URL[urlsStr.length];
		for (int i = 0; i < urls.length; i++) {
			urlsStr[i] = urlsStr[i].toLowerCase();
			urlsStr[i] = urlsStr[i].trim();
			if (urlsStr[i].startsWith(DcemConstants.HTTPS_PROTOCOL_FULL) == false) {
				urlsStr[i] = DcemConstants.HTTPS_PROTOCOL_FULL + urlsStr[i];
			}
			try {
				urls[i] = new URL(urlsStr[i]);
			} catch (Exception e) {
				logger.warn("Failed url parsing" + urls[i], e);
				throw new DcemException(DcemErrorCodes.INVALID_URL_FORMAT, "", e);
			}
		}
		return urls;
	}

	public String getDcemManagementUrl(String tenantName) throws DcemException {
		return getServiceUrl(ConnectionServicesType.MANAGEMENT, tenantName);
	}

	public String getUserPortalUrl(String tenantName) throws DcemException {
		return getServiceUrl(ConnectionServicesType.USER_PORTAL, tenantName);
	}

	public String getServiceUrl(ConnectionServicesType connectionServicesType) throws DcemException {
		return getServiceUrl(connectionServicesType, null);
	}

	public String getServiceUrl(ConnectionServicesType connectionServicesType, String tenantName) throws DcemException {
		ClusterConfig config = configLogic.getClusterConfig();
		ConnectionService connectionService = config.getConnectionService(connectionServicesType);
		StringBuffer sb = new StringBuffer();
		URL url = getDcemUrls()[0];
		if (connectionServicesType == ConnectionServicesType.WEB_SOCKETS) {
			sb.append("wss://");
		} else {
			sb.append("https://");
		}
		String host = url.getHost();
		if (TenantIdResolver.isCurrentTenantMaster() == false || tenantName != null) {
			if (host.startsWith("www.")) {
				host = host.substring(4);
			}
			if (tenantName == null) {
				tenantName = TenantIdResolver.getCurrentTenantName();
			}
			sb.append(tenantName);
			sb.append('.');
		}

		sb.append(host);
		sb.append(connectionService.getExternalPortAsString());
		sb.append('/');
		sb.append(connectionServicesType.getPath());
		return sb.toString();
	}

	public TenantEntity getTenantFromUrlHostName(String hostName) throws DcemException {
		hostName = hostName.toLowerCase();
		TenantEntity tenant = null;
		String message = null;
		URL[] urls = getDcemUrls();
		if (urls == null || urls.length == 0) {
			// shoudl not be.
			if (InetAddresses.isInetAddress(hostName) == true) {
				tenant = TenantIdResolver.getMasterTenant();
				return tenant;
			}
			if (hostName.contains(".")) {
				String[] names = hostName.split("\\.");
				String tenantName = names[0];
				TenantEntity tenantEntity = getTenant(tenantName.substring(0, tenantName.length() - 1));
				if (tenantEntity == null) {
					message = "Invalid tenant name '" + tenantName + "'. Perhaps you need to set a domain name in Cluster Config.";
				} else {
					tenant = tenantEntity;
				}
			} else {
				tenant = TenantIdResolver.getMasterTenant();
			}
		} else {
			for (URL url : urls) {
				if (hostName.endsWith(url.getHost())) {
					String tenantName = hostName.substring(0, hostName.length() - url.getHost().length());
					if (tenantName.isEmpty()) {
						tenant = TenantIdResolver.getMasterTenant();
					} else if (tenantName.endsWith(".") == false) {
						message = "The tenant name is not seperated by a '.'";
					} else {
						tenant = getTenant(tenantName.substring(0, tenantName.length() - 1));
						if (tenant == null) {
							message = "The tenant '" + tenantName + "' does not exist";
						}
					}
					if (tenant != null || message != null) {
						break;
					}
				}
			}
		}
		if (message != null) {
			throw new DcemException(DcemErrorCodes.INVALID_TENANT, "Error while retreiving tenant from '" + hostName + "': " + message);
		}
		return tenant;
	}

	public TenantEntity getTenant(String tenantName) {
		if (tenantName == null) {
			return null;
		}
		return tenantMap.get(tenantName.toUpperCase());
	}

	public boolean isMultiTenant() {
		return tenantMap.size() > 1;
	}

	public HashMap<String, TenantEntity> getTenantMap() {
		return tenantMap;
	}

	public String getNodeName() {
		return DcemCluster.getInstance().getDcemNode().getName();
	}

	public void removeAlert(String category, String key, Long alertMessageId) {
		if (category != null && key != null && !key.isEmpty()) {
			reportingLogic.closeAlertMessage(alertMessageId);
		}
	}

	public TenantEntity getTenantFromRequest(HttpServletRequest request) {
		TenantEntity tenant = null;
		if (isMultiTenant()) {
			try {
				// Get host name
				String hostName = request.getServerName();
				String forwardedHost = request.getHeader("X-FORWARDED-HOST");
				if (forwardedHost != null) {
					int ind = forwardedHost.indexOf(':');
					if (ind != -1) {
						hostName = forwardedHost.substring(0, ind);
					} else {
						hostName = forwardedHost;
					}
				}
				tenant = getTenantFromUrlHostName(hostName);
			} catch (Exception e) {
				logger.warn("DcemFilter - Exception while retreiving tenant", e);
				tenant = TenantIdResolver.getMasterTenant();
			}
		} else {
			// no tenants configured
			tenant = TenantIdResolver.getMasterTenant();
		}
		return tenant;
	}

	public int getPollInterval() {
		return DcemConstants.LOGIN_POLL_INTERVAL_MILLI_SECONDS / 1000;
	}

	public int getPollIntervalMilli() {
		return DcemConstants.LOGIN_POLL_INTERVAL_MILLI_SECONDS;
	}

	public boolean isCaptchaOn() {
		return captchaOn;
	}

	public void setCaptchaOn(boolean captchaOn) {
		this.captchaOn = captchaOn;
	}

	public String getApplicationTheme() {
		String path = JsfUtils.getHostUrl().getPath();
		if (path.startsWith(DcemConstants.EMBEDDED_USER_PORTAL_PATH)) {
			return DcemConstants.DCEM_PORTAL_THEME;
		}
		return DcemConstants.DCEM_THEME;
	}

	public HashMap<String, String> getFileIconsMap() {
		return fileIconsMap;
	}

	public int getInactivityTime() {
		return JsfUtils.getSessionTimeout() / 60;
	}

	public List<SelectItem> getAvailableCountries(Locale userLocale) {
		List<SelectItem> selectItems = new ArrayList<SelectItem>();
		String[] countries = Locale.getISOCountries();
		for (String countryCode : countries) {
			Locale locale = new Locale("", countryCode);
			selectItems.add(new SelectItem(countryCode, locale.getDisplayCountry(userLocale)));
		}
		Collections.sort(selectItems, new SelectItemComparator());
		return selectItems;
	}

	public Configuration getFreeMarkerConfiguration() {
		if (freeMarkerConfiguration == null) {
			freeMarkerConfiguration = new Configuration(Configuration.VERSION_2_3_29);
			freeMarkerConfiguration.setDefaultEncoding("UTF-8");
			freeMarkerConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
			freeMarkerConfiguration.setLogTemplateExceptions(false);
			freeMarkerConfiguration.setWrapUncheckedExceptions(true);
			freeMarkerConfiguration.setFallbackOnNullLoopVariable(false);
			DcFreeMarkerStringLoader templateLoader = new DcFreeMarkerStringLoader();
			freeMarkerConfiguration.setTemplateLoader(templateLoader);
		}
		return freeMarkerConfiguration;
	}

	public Template getTemplateFromConfig(DcemTemplate dcemTemplate) throws Exception {
		getFreeMarkerConfiguration();
		Template template = null;
		String name = dcemTemplate.getFullName() + "-" + TenantIdResolver.getCurrentTenantName();
		try {
			template = freeMarkerConfiguration.getTemplate(name);
		} catch (TemplateNotFoundException e) {
			DcFreeMarkerStringLoader stringLoader = (DcFreeMarkerStringLoader) freeMarkerConfiguration.getTemplateLoader();
			stringLoader.putTemplate(name, dcemTemplate.getContent());
			// Wait until the template is loaded in the configuration
			for (int sleepCounter = 0; sleepCounter < 20; sleepCounter++) {
				try {
					template = freeMarkerConfiguration.getTemplate(name);
					break;
				} catch (TemplateNotFoundException exp) {
					Thread.sleep(500);
				}
			}
			if (template == null) {
				throw new TemplateNotFoundException(name, null, "The template could not be loaded after 10 seconds");
			}
		} catch (Exception e) {
			throw e;
		}
		return template;
	}

	public void removeFreeMarkerTemplate(String templateFullName) {
		if (freeMarkerConfiguration != null) {
			((DcFreeMarkerStringLoader) freeMarkerConfiguration.getTemplateLoader())
					.removeTemplate(templateFullName + "-" + TenantIdResolver.getCurrentTenantName());
		}
	}

	public void updateFreeMarkerCache() {
		LocalDateTime localDateTime = LocalDateTime.now();
		localDateTime.minusDays(5);
		if (freeMarkerConfiguration != null) {
			long epoch = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
			((DcFreeMarkerStringLoader) freeMarkerConfiguration.getTemplateLoader()).updataTemplateCache(epoch);
		}
	}

}

/////////////////////////////////////////////////////////////////////////////////////////////
class ViewComparator implements Comparator<SubjectAbs> {

	@Override
	public int compare(SubjectAbs e1, SubjectAbs e2) {
		if (e1.getRank() > e2.getRank()) {
			return 1;
		} else {
			return -1;
		}
	}
}

class SelectItemComparator implements Comparator<SelectItem> {
	public int compare(SelectItem item1, SelectItem item2) {
		return item1.getLabel().compareTo(item2.getLabel());
	}
}
