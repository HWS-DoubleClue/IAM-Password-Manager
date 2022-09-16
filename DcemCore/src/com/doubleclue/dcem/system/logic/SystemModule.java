package com.doubleclue.dcem.system.logic;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.config.DatabaseConfig;
import com.doubleclue.dcem.core.config.LocalConfig;
import com.doubleclue.dcem.core.config.LocalConfigProvider;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.jpa.DatabaseTypes;
import com.doubleclue.dcem.core.jpa.DbFactoryProducer;
import com.doubleclue.dcem.core.jpa.EntityManagerProducer;
import com.doubleclue.dcem.core.jpa.JdbcUtils;
import com.doubleclue.dcem.core.jpa.StatisticCounter;
import com.doubleclue.dcem.core.licence.LicenceLogic;
import com.doubleclue.dcem.core.logging.LogUtils;
import com.doubleclue.dcem.core.logic.DiagConstants;
import com.doubleclue.dcem.core.logic.DiagnosticLogic;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.core.tasks.MonitoringTask;
import com.doubleclue.dcem.core.tasks.NightlyTask;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.core.utils.JsonConverter;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.system.send.MessageBird;
import com.doubleclue.dcem.system.send.SendEmail;
import com.doubleclue.utils.ProductVersion;
import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.PooledDataSource;

@ApplicationScoped
public class SystemModule extends DcemModule {

	private static Logger logger = LogManager.getLogger(SystemModule.class);

	@Inject
	EntityManagerProducer emp;

	@Inject
	EntityManager em;

	@Inject
	LicenceLogic licenceLogic;

	@Inject
	DiagnosticLogic diagnosticLogic;

	LocalConfig localConfig;

	// DbFactoryProducer dbFactoryProducer;

	@Inject
	TaskExecutor taskExecutor;

	@Inject
	MessageBird messageBird;

	@Inject
	DcemApplicationBean dcemApplication;

	@Inject
	AdminModule adminModule;

	@Inject
	SystemModule systemModule;

	@Inject
	OperatorSessionBean operatorSessionBean;

	@SuppressWarnings("restriction")
	com.sun.management.OperatingSystemMXBean operatingSystemMXBean;

	public final static String MODULE_ID = "system";
	public final static String RESOUCE_NAME = "com.doubleclue.dcem.core.resources.Messages";

	DecimalFormat df = new DecimalFormat("###,###.00");

	DecimalFormat dfKb = new DecimalFormat("###,###,###,### KB");

	// 11 is version 2.7.1
	public static final int DATABASE_VERSION = 11;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ScheduledFuture<?> monitorSchedule;
	ScheduledFuture<?> nightlySchedule;

	// HashMap<String, SubjectAbs> subjects = new HashMap<String, SubjectAbs>();

	@SuppressWarnings("restriction")
	@Override
	public void init() throws DcemException {
		operatingSystemMXBean = (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		setMasterOnly(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.doubleclue.dcem.core.logic.module.DcemModule#start()
	 */
	public void start() throws DcemException {
		super.start();
		localConfig = LocalConfigProvider.getLocalConfig();
		LogUtils.initLog4j(null, getPreferences().additionalLoggers, getPreferences().logLevel, DcemApplicationBean.debugMode);
		initStaticValues();
		emp.enableDbStatistics(getPreferences().enableDbStatistics);
		updateMonitoring();
		updateHttpProxy();
		SendEmail.setProperties(getPreferences());

		try {
			messageBird.initSms(getPreferences());
		} catch (Exception e) {
			logger.error("'Couldn't initialize SMS", e);
		}
		updateNightlyTaskSchedule();
	}

	public String getResourceName() {
		return RESOUCE_NAME;
	}

	public String getName() {
		return "System";
	}

	@Override
	public String getId() {
		return MODULE_ID;
	}

	public int getRank() {
		return 10;
	}

	@Override
	public DcemView getDefaultView() {
		try {
			return CdiUtils.getReference("welcomeView");
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	public ModulePreferences getDefaultPreferences() {
		return new SystemPreferences();
	}

	public SystemPreferences getPreferences() {
		return (SystemPreferences) super.getModulePreferences();
	}

	@Override
	public void preferencesValidation(ModulePreferences modulePreferences) throws DcemException {
		SystemPreferences preferences = (SystemPreferences) modulePreferences;
		if (preferences.eMailHostPort != 0) {
			if (preferences.eMailHostAddress.length() < 4) {
				throw new DcemException(DcemErrorCodes.EMAIL_INVALID_CONFIGURATION, "Please enter a correct Host Address");
			}
			SendEmail.setProperties(preferences);
			SendEmail.sendMessage(null, null, null);
		}
		if (preferences.getSmsProviderAccesKey() != null && preferences.getSmsProviderAccesKey().isEmpty() == false) {
			if (preferences.getSmsOriginatorName().isEmpty()) {
				throw new DcemException(DcemErrorCodes.SMS_INVALID_CONFIGURATION, "Please enter an SMS originator name");
			}
		}
		if (preferences.getPathEmbeddedDatabaseBackup() != null && preferences.getPathEmbeddedDatabaseBackup().isEmpty() == false) {
			File file = new File(preferences.getPathEmbeddedDatabaseBackup());
			if (file.exists() == false) {
				throw new DcemException(DcemErrorCodes.INVALID_PATH_EMBEDDED_DATABASE_BACKUP, "Please enter an existing path for 'PathEmbddedDatabaseBackup'");
			}
		}
		if (preferences.specialProperties != null && preferences.specialProperties.trim().isEmpty() == false) {
			try {
				JsonConverter.getAsMap(preferences.specialProperties);
			} catch (Exception e) {
				throw new DcemException(DcemErrorCodes.INVALID_SPECIAL_PROPERTIES_SYNTAX, "Cannot parse: " + preferences.specialProperties);
			}
		}
	}

	@Override
	public void checkPreferenceChanges(ModulePreferences modulePreferencesPrevious) {
		try {
			SystemPreferences previous = (SystemPreferences) modulePreferencesPrevious;
			SystemPreferences systemPreferences = getPreferences();
			if (previous.getAdditionalLoggers() == null) {
				previous.setAdditionalLoggers("");
			}
			if (previous.getLogLevel() != systemPreferences.getLogLevel()
					|| previous.getAdditionalLoggers().equals(getPreferences().getAdditionalLoggers()) == false) {
				LogUtils.initLog4j(null, getPreferences().getAdditionalLoggers(), getPreferences().getLogLevel(), DcemApplicationBean.debugMode);
			}
			if (previous.enableDbStatistics != getPreferences().enableDbStatistics) {
				emp.enableDbStatistics(getPreferences().enableDbStatistics);
			}
			try {
				if (previous.getSmsProviderAccesKey() == null) {
					previous.setSmsProviderAccesKey("");
				}
				if (previous.getSmsProviderAccesKey().equals(getPreferences().getSmsProviderAccesKey()) == false
						|| previous.getSmsOriginatorName().equals(getPreferences().getSmsOriginatorName()) == false) {
					try {
						messageBird.initSms(getPreferences());
					} catch (DcemException e) {
						logger.error(e);
					}
				}
			} catch (Exception e) {
				logger.error(e);
			}
			if ((previous.enableMonitoring != getPreferences().enableMonitoring) || (previous.monitoringInterval != getPreferences().monitoringInterval)) {
				updateMonitoring();
			}
			if (previous.nightlyTaskTime != getPreferences().nightlyTaskTime) {
				updateNightlyTaskSchedule();
			}
			updateHttpProxy();
			if (systemPreferences.specialProperties == null) {
				systemPreferences.specialProperties = "";
			}
			if (systemPreferences.specialProperties.trim().isEmpty()) {
				super.specialPorperties = null;
			} else {
				try {
					super.specialPorperties = JsonConverter.getAsMap(systemPreferences.specialProperties);
				} catch (Exception e) {
					// shouldn't occur, since it was check at validation
				}
			}
			if (getSpecialPropery(DcemConstants.SPECIAL_PROPERTY_RUN_NIGHTLY_TASK) != null) {
				taskExecutor.execute(new NightlyTask());
			}
		} catch (Exception e) {
			logger.warn("checkPreferenceChanges", e);
		}

	}

	private void updateMonitoring() {
		if (getPreferences().enableMonitoring) {
			if (monitorSchedule != null) {
				monitorSchedule.cancel(true);
			}
			monitorSchedule = taskExecutor.scheduleAtFixedRate(new MonitoringTask(), 2, getPreferences().monitoringInterval, TimeUnit.MINUTES);
		} else {
			if (monitorSchedule != null) {
				monitorSchedule.cancel(true);
			}
			monitorSchedule = null;
		}
	}

	private void updateHttpProxy() {
		SystemPreferences systemPreferences = getPreferences();
		if (systemPreferences.httpProxyPort > 0) {
			System.setProperty("http.proxyHost", systemPreferences.httpProxyHost);
			System.setProperty("http.proxyPort", Integer.toString(systemPreferences.httpProxyPort));
			System.setProperty("https.proxyHost", systemPreferences.httpProxyHost);
			System.setProperty("https.proxyPort", Integer.toString(systemPreferences.httpProxyPort));
			if (systemPreferences.httpProxyUser != null && systemPreferences.getHttpProxyUser().isEmpty() == false) {
				// String encoded =
				// Base64.getEncoder().encodeToString((systemPreferences.httpProxyUser + ":" +
				// systemPreferences.httpProxyPassword).getBytes());
				// con.setRequestProperty("Proxy-Authorization", "Basic " + encoded);
				Authenticator.setDefault(new ProxyAuth(systemPreferences.httpProxyUser, systemPreferences.httpProxyPassword));
			}
			ProxySelector.setDefault(new DcemProxySelector(systemPreferences.httpProxyHost, systemPreferences.httpProxyPort));
		} else {
			System.clearProperty("http.proxyHost");
			System.clearProperty("http.proxyPort");
			System.clearProperty("https.proxyHost");
			System.clearProperty("https.proxyPort");
			ProxySelector.setDefault(new DcemProxySelector(null, 0));

		}
	}

	private void updateNightlyTaskSchedule() {

		String timeFullString = getPreferences().getNightlyTaskTime();
		if (timeFullString != null && !timeFullString.isEmpty()) {
			String[] timeStrings = timeFullString.split(":");
			if (timeStrings.length == 2) {
				if (nightlySchedule != null) {
					nightlySchedule.cancel(true);
				}
				int hour = Integer.parseInt(timeStrings[0]);
				int minute = Integer.parseInt(timeStrings[1]);

				LocalDateTime localNow = LocalDateTime.now();
				ZoneId currentZone = ZoneId.systemDefault();
				ZonedDateTime zonedNow = ZonedDateTime.of(localNow, currentZone);
				ZonedDateTime zonedNextTarget = zonedNow.withHour(hour).withMinute(minute).withSecond(0);
				if (zonedNow.compareTo(zonedNextTarget) > 0) {
					zonedNextTarget = zonedNextTarget.plusDays(1);
				}
				Duration duration = Duration.between(zonedNow, zonedNextTarget);
				long initialDelayMinutes = duration.getSeconds() / 60;
				nightlySchedule = taskExecutor.scheduleAtFixedRate(new NightlyTask(), initialDelayMinutes, (24 * 60), TimeUnit.MINUTES);
			}
		}
	}

	public void initStaticValues() {
		Map<String, String> map = getStaticValues();
		map.clear();
		ProductVersion productVersion = dcemApplication.getProductVersion();
		map.put(DiagConstants.DCEM_VERSION, dcemApplication.getVersion() + "/" + productVersion.getSvnBuildNr());
		map.put(DiagConstants.AVAILABLE_PROCESSORS, Integer.toString(Runtime.getRuntime().availableProcessors()));
		map.put(DiagConstants.TOTAL_PHYSICAL_MEMORY, dfKb.format(operatingSystemMXBean.getTotalPhysicalMemorySize() / 1024));
		map.put(DiagConstants.MAX_MEMORY, dfKb.format(Runtime.getRuntime().maxMemory() / 1024));
		map.put(DiagConstants.OS_NAME, System.getProperty("os.name"));
		map.put(DiagConstants.SYSTEM_TIMEZONE, TimeZone.getDefault().getID());
		map.put(DiagConstants.OS_VERSION, System.getProperty("os.version"));
		DbFactoryProducer dbFactoryProducer = DbFactoryProducer.getInstance();
		DatabaseMetaData metaData = dbFactoryProducer.getDatabaseMetaData();
		if (metaData != null) {
			StringBuffer sb = new StringBuffer();
			try {
				sb.append(metaData.getDatabaseProductName());
			} catch (SQLException e) {

			}
			sb.append("-");
			try {
				sb.append(metaData.getDatabaseProductVersion());
			} catch (SQLException e) {

			}
			try {
				sb.append(", ");
				sb.append(metaData.getURL());
			} catch (SQLException e) {
			}

			sb.append(", db-name=");

			sb.append(localConfig.getDatabase().getDatabaseName());
			if (localConfig.getDatabase().getSchemaName().isEmpty() == false) {
				sb.append(".");
				sb.append(localConfig.getDatabase().getSchemaName());
			}

			map.put(DiagConstants.DB_METADATA, sb.toString());
			map.put(DiagConstants.JAVA_VERSION, System.getProperty("java.version"));
		}

		return;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.doubleclue.dcem.core.logic.module.DcemModule#getDiagnosticValues()
	 */
	@SuppressWarnings("restriction")
	@Override
	public Map<String, String> getStatisticValues() {
		Map<String, String> map = super.getStatisticValues();

		map.put(DiagConstants.ACTIVE_THREADS, Integer.toString(Thread.activeCount()));
		map.put(DiagConstants.FREE_PHYSICAL_MEMORY, dfKb.format(operatingSystemMXBean.getFreePhysicalMemorySize() / 1024));

		map.put(DiagConstants.SYSTEM_CPU_LOAD, df.format(operatingSystemMXBean.getSystemCpuLoad() * 100));
		map.put(DiagConstants.PROCESS_CPU_LOAD, df.format(operatingSystemMXBean.getProcessCpuLoad() * 100));

		map.put(DiagConstants.TOTAL_MEMORY, dfKb.format(Runtime.getRuntime().totalMemory() / 1024));
		map.put(DiagConstants.FREE_MEMORY, dfKb.format(Runtime.getRuntime().freeMemory() / 1024));

		@SuppressWarnings("unchecked")
		Iterator<PooledDataSource> iterator = C3P0Registry.getPooledDataSources().iterator();
		PooledDataSource pooledDataSource;
		int dbInd = 0;
		while (iterator.hasNext()) {
			dbInd++;
			pooledDataSource = (PooledDataSource) iterator.next();
			try {
				map.put(DiagConstants.DB_BUSY_CONNECTIONS + dbInd, Integer.toString(pooledDataSource.getNumBusyConnectionsAllUsers()));
				map.put(DiagConstants.DB_IDLE_CONNECTIONS + dbInd, Integer.toString(pooledDataSource.getNumIdleConnectionsAllUsers()));
				map.put(DiagConstants.DB_CONNECTIONS + dbInd, Integer.toString(pooledDataSource.getNumConnectionsAllUsers()));
				map.put(DiagConstants.DB_THREAD_POOL_ACTIVE + dbInd, Integer.toString(pooledDataSource.getThreadPoolNumActiveThreads()));
				map.put(DiagConstants.DB_THREAD_POOL_SIZE + dbInd, Integer.toString(pooledDataSource.getThreadPoolSize()));
			} catch (SQLException e) {
				// logger.error("Could not retrieved c3p0 statistics.", e);
			}
		}
		DateFormat df = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG);
		df.setTimeZone(adminModule.getTimezone());
		map.put(DiagConstants.TIME, df.format(new Date()));
		df.setTimeZone(TimeZone.getDefault());
		map.put(DiagConstants.SYSTEM_TIME, df.format(new Date()));
		return map;
	}

	@Override
	public Map<String, StatisticCounter> getStatisticCounters() {
		DbFactoryProducer dbFactoryProducer = DbFactoryProducer.getInstance();
		super.getStatisticCounters().putAll(emp.getQueryStatistics(dbFactoryProducer.getEntityManagerFactory()));
		return super.getStatisticCounters();
	}

	public void resetDiagCounters() {
		emp.clearDbStatistics();
		Map<String, String> map = super.getStatisticValues();
		map.put(DiagConstants.TIME_RESET_COUNTERS, new Date().toString());
	}

	@Override
	public void runNightlyTask() {
		try {
			// If it's Monday, send licence warnings as e-mails.
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY || systemModule.getSpecialPropery(DcemConstants.SPECIAL_PROPERTY_RUN_NIGHTLY_TASK) != null) {
				licenceLogic.sendLicenceWarningEmails();
			}
			// Clean up monitoring records.
			diagnosticLogic.cleanMonitoringRecords(getPreferences().maxMonitoringRecords);
		} catch (Exception e) {
			logger.error(e);
		}
		DbFactoryProducer.getInstance();
		if ((DbFactoryProducer.getDbType() == DatabaseTypes.DERBY) && (getPreferences().isRunEmbeddedDatabaseBackup() == true)
				&& (getPreferences().getPathEmbeddedDatabaseBackup().isEmpty() == false)) {
			DatabaseConfig databaseConfig = LocalConfigProvider.getLocalConfig().getDatabase();
			long executionTime;
			try {
				executionTime = JdbcUtils.backUpEmbeddedDatabase(databaseConfig, getPreferences().getPathEmbeddedDatabaseBackup());
				logger.info("Embedded-Databse was backup at: " + getPreferences().getPathEmbeddedDatabaseBackup());
				addCounter("EmbeddedDatabaseBackup", executionTime);
			} catch (SQLException e) {
				logger.error("ERROR: Couldn't  run backup for embedded database!", e);
			}
		}
	}

	public int getDbVersion() {
		// return 5; // set to 5 for DCEM 2.2
		// return 6; // set to 6 for DCEM 2.3
		// return 7; // set to 7 for DCEM 2.4
		// return 8; // set to 7 for DCEM 2.4.4
		return DATABASE_VERSION; // set to 8 for DCEM 2.5
	}

	public void initializeTenant(TenantEntity tenantEntity) throws DcemException {
		SystemTenantData coreTenantData = (SystemTenantData) getModuleTenantData();
		if (coreTenantData == null) {
			coreTenantData = new SystemTenantData();
			super.initializeTenant(tenantEntity, coreTenantData);
		}
	}

	public class ProxyAuth extends Authenticator {
		private PasswordAuthentication auth;

		private ProxyAuth(String user, String password) {
			auth = new PasswordAuthentication(user, password == null ? new char[] {} : password.toCharArray());
		}

		protected PasswordAuthentication getPasswordAuthentication() {
			return auth;
		}
	}

	class DcemProxySelector extends ProxySelector {

		ArrayList<Proxy> proxyList;

		public DcemProxySelector(String host, int port) {
			proxyList = new ArrayList<Proxy>(1);
			if (host != null) {
				proxyList.add(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port)));
			} else {
				proxyList.add(Proxy.NO_PROXY);
			}
		}

		@Override
		public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
			logger.info("connectFailed: " + ioe.getMessage());
		}

		@Override
		public List<Proxy> select(URI uri) {
			return proxyList;
		}

	}

}
