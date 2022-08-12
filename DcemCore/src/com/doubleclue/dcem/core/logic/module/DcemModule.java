package com.doubleclue.dcem.core.logic.module;

import java.io.Serializable;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemNode;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.ViewNavigator;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.EntityManagerProducer;
import com.doubleclue.dcem.core.jpa.StatisticCounter;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.licence.LicenceLogic;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.ProductVersion;

public abstract class DcemModule implements Serializable {

	private static final long serialVersionUID = 1L;

	private DcemAction moduleAction;

	@Inject
	protected ViewNavigator viewNavigator;

	@Inject
	private ConfigLogic configLogic;

	@Inject
	EntityManagerProducer emp;

	@Inject
	LicenceLogic licenceLogic;

	@Inject
	DcemReportingLogic dcemReportingLogic;

	protected Map<String, ModuleTenantData> moduleTenantMap = new ConcurrentHashMap<String, ModuleTenantData>();
	
	protected static Logger logger = LogManager.getLogger(DcemModule.class);

	// private List<DcemAction> dcemActions;
	private static LinkedList<RawAction> rawActions = new LinkedList<>();

	private Map<String, StatisticCounter> counters = new HashMap<String, StatisticCounter>();
	private Map<String, String> values = new HashMap<String, String>();
	private Map<String, String> staticValues = new HashMap<String, String>();

	protected Map<String, String> specialPorperties;

	private int dbVersion = 1;

	private int moduleVersion = 0;

	protected boolean started;

	private boolean hasDbTables = true;

	boolean masterOnly = false;

	static {
		rawActions.add(new RawAction(DcemConstants.ACTION_REST_API, new String[] { DcemConstants.SYSTEM_ROLE_REST_SERVICE }));
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, null));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN }));
	};

	public void start() throws DcemException {
		started = true;
		if (getModuleVersion() > 0) {
			staticValues.put("ModuleVersion", Integer.toString(getModuleVersion()));
		}
	}

	public void stop() throws DcemException {
		started = false;
	}

	public boolean isStarted() throws DcemException {
		return started;
	}

	public DcemView getDefaultView() {
		return null;
	}

	public void init() throws DcemException {
	}

	abstract public String getResourceName();

	abstract public ModulePreferences getDefaultPreferences();

	public ModulePreferences getModulePreferences() {
		return getModuleTenantData().getModulePreferences();
	}
	
	

	// abstract public HashMap<Integer, String> getAuditActions ();
	//
	// abstract public HashMap<Integer, String> getAuditSubjects ();

	abstract public String getName();

	abstract public String getId();

	abstract public int getRank();

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Id=");
		sb.append(getId());

		// TODO add the rest.
		return sb.toString();
	}

	public DcemAction getModuleAction() {
		return moduleAction;
	}

	public void setModuleAction(DcemAction moduleAction) {
		this.moduleAction = moduleAction;
	}

	public LinkedList<RawAction> getRawActions() {
		return rawActions;
	}

	// public List<DcemAction> getDcemActions() {
	// return dcemActions;
	// }

	// public DcemAction getDcemAction (String action) {
	// for (DcemAction dcemAction : dcemActions) {
	// if (dcemAction.getAction().equals(action)) {
	// return dcemAction;
	// }
	// }
	// return null;
	// }

	// public void setDcemActions(List<DcemAction> dcemActions) {
	// this.dcemActions = dcemActions;
	// }

	public List<String> getTextResourceKeys() {
		return null;
	}

	public boolean isHasDbTables() {
		return hasDbTables;
	}

	public void preferencesValidation(ModulePreferences modulePreferences) throws DcemException {
		return;
	}

	public void checkPreferenceChanges(ModulePreferences modulePreferencesPrevious) {
		return;
	}
	
	
	public void savePreferences(ModulePreferences modulePreferencesPrevious, ModulePreferences modulePreferencesNew) {
		DcemUtils.copyObject(modulePreferencesNew, this.getModulePreferences());
		this.checkPreferenceChanges(modulePreferencesPrevious);
	}

	public Map<String, StatisticCounter> getStatisticCounters() {
		return counters;
	}

	/**
	 * @return
	 */
	public Map<String, String> getStatisticValues() {
		Attributes attributes = null;
		try {
			String className = this.getClass().getSimpleName();
			int ind = className.indexOf("$");
			if (ind != -1) {
				className = className.substring(0, ind) + ".class";
			}
			URL url = this.getClass().getResource(className);
			if (url == null) {
				values.put("ModuleVersion", "-");
				return values;
			}
			String classPath = url.toString();
			attributes = KaraUtils.getManifestInformation(classPath);
			if (attributes != null) {
				String implementationVersion = attributes.getValue(Name.IMPLEMENTATION_VERSION);
				String scmVersion = attributes.getValue(ProductVersion.SVN_NUMBER);
				values.put("ModuleVersion", implementationVersion + "/" + scmVersion);
			} else {
				values.put("ModuleVersion", "0.0.0");
			}
		} catch (Exception e) {
			values.put("ModuleVersion", "0");
		}

		return values;
	}

	public void addCounter(String name, long executionTime) {
		StatisticCounter counter = counters.get(name);
		if (counter == null) {
			counter = new StatisticCounter();
			counters.put(name, counter);
		}
		counter.count++;
		counter.executionTime += executionTime;
		if (counter.longestTime < executionTime) {
			counter.longestTime = executionTime;
		}
	}

	public Map<String, String> getStaticValues() {
		return staticValues;
	}

	public void resetDiagCounters() {
		for (StatisticCounter statisticCounter : counters.values()) {
			statisticCounter.reset();
		}
	}

	public void runNightlyTask() throws DcemException{
	}

	
	public List<DcemReporting> getLicenceAlerts() {
		return null;
	}

	public int getDbVersion() {
		return dbVersion;
	}

	public String getSpecialPropery(String prop) {
		if (specialPorperties == null || specialPorperties.isEmpty()) {
			return null;
		}
		return specialPorperties.get(prop);
	}

	public void onInstall(KeyStore rootKeyStore, String passwordMgt, DcemNode dcemNode) throws DcemException {

	}

	public void dbMigration(boolean before, int fromVersion, int toVersion, String table) throws DcemException {

	}

	public List<?> getPolicyApplications() {
		return null;
	}

	@DcemTransactional
	public void deleteUserFromDb(DcemUser dcemUser) throws DcemException {
		return;
	}
	
	@DcemTransactional
	public void deleteGroupFromDb(DcemGroup dcemGroup) throws DcemException {
		return;
	}

	public ModuleTenantData getModuleTenantData() {
		return moduleTenantMap.get(TenantIdResolver.getCurrentTenantName());
	}

	public List<DcemAction> getDcemActions() {
		List<DcemAction> dcemActions = new ArrayList<>(rawActions.size());
		for (RawAction rawAction : rawActions) {
			dcemActions.add(new DcemAction(this.getId(), DcemConstants.EMPTY_SUBJECT_NAME, rawAction.getName()));
		}
		return dcemActions;
	}

	public void initializeDb(DcemUser superAdmin) throws DcemException {

	}

	public void initializeTenant(TenantEntity tenantEntity) throws DcemException {
		ModuleTenantData tenantData = new ModuleTenantData();
		initializeTenant(tenantEntity, tenantData);
	}

	public void initializeTenant(TenantEntity tenantEntity, ModuleTenantData tenantData) throws DcemException {

		moduleTenantMap.put(tenantEntity.getName(), tenantData);
		if (this.getDefaultPreferences() != null) {
			try {
				ModulePreferences modulePreferences = configLogic.getModulePreferences(this.getId(), this.getDefaultPreferences().getClass());
				if (modulePreferences == null) {
					modulePreferences = this.getDefaultPreferences();
				}
				getModuleTenantData().modulePreferences = modulePreferences;
			} catch (DcemException exp) {
				if (exp.getErrorCode() == DcemErrorCodes.INVALID_PREFERENCES_FORMAT) {
					getModuleTenantData().modulePreferences = this.getDefaultPreferences();
				}
				dcemReportingLogic.addWelcomeViewAlert(DcemConstants.ALERT_CATEGORY_DCEM, exp.getErrorCode(), "Preferences - " + this.getId(),
						AlertSeverity.ERROR, true, exp.toString());
			} catch (Exception e) {
				dcemReportingLogic.addWelcomeViewAlert(DcemConstants.ALERT_CATEGORY_DCEM, DcemErrorCodes.INVALID_PREFERENCES_FORMAT, "Preferences",
						AlertSeverity.ERROR, true, e.toString());
			}
		}
	}

	public Map<String, ModuleTenantData> getModuleTenantMap() {
		return moduleTenantMap;
	}

	public boolean isMasterOnly() {
		return masterOnly;
	}

	public void setMasterOnly(boolean masterOnly) {
		this.masterOnly = masterOnly;
	}

	public void setDbVersion(int dbVersion) {
		this.dbVersion = dbVersion;
	}

	public int getModuleVersion() {
		return moduleVersion;
	}
}