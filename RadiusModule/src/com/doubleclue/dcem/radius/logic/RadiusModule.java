package com.doubleclue.dcem.radius.logic;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.config.ConnectionService;
import com.doubleclue.dcem.core.config.ConnectionServicesType;
import com.doubleclue.dcem.core.entities.DcemRole;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.jpa.ExportRecords;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.logic.module.ModulePreferences;
import com.doubleclue.dcem.core.tasks.ReloadClassInterface;
import com.doubleclue.dcem.core.tasks.TaskExecutor;
import com.doubleclue.dcem.radius.entities.RadiusClientEntity;
import com.doubleclue.dcem.radius.entities.RadiusReportEntity;
import com.doubleclue.dcem.radius.preferences.RadiusPreferences;
import com.doubleclue.dcem.system.logic.SystemModule;

@SuppressWarnings("serial")
@ApplicationScoped
@Named("radiusModule")
public class RadiusModule extends DcemModule implements ReloadClassInterface {

	@Inject
	TaskExecutor taskExecutor;

	@Inject
	UserLogic userLogic;

	@Inject
	RoleLogic roleLogic;

	@Inject
	RadiusReportLogic radiusReportLogic;

	@Inject
	RadiusClientLogic radiusClientLogic;

	@Inject
	ProcessRadiusPacket processRadiusPacket;

	@Inject
	ExportRecords exportRecords;
	
	@Inject
	SystemModule systemModule;

	private static Logger logger = LogManager.getLogger(RadiusModule.class);

	public final static String MODULE_ID = "radius";
	public final static String RESOUCE_NAME = "com.doubleclue.dcem.radius.resources.Messages";

	Map<String, RadiusClientEntity> ipNumberTenantMap = new ConcurrentHashMap<>();

	ConnectionService connectionServiceRadius;
	ConnectionService connectionServiceRadiusAccounting;

	public void start() throws DcemException {
		super.start();
		DcemUser dcemUser = userLogic.getUser(DcemConstants.RADIUS_OPERATOR_NAME);

		if (dcemUser == null) {
			DcemRole role = roleLogic.getDcemRole(DcemConstants.SYSTEM_ROLE_USER);
			dcemUser = new DcemUser(DcemConstants.RADIUS_OPERATOR_NAME, "radius@radius.com", DcemConstants.RADIUS_OPERATOR_NAME, role);
			userLogic.addOrUpdateUserWoAuditing(dcemUser);
		}
		connectionServiceRadius = DcemCluster.getInstance().getClusterConfig().getConnectionService(ConnectionServicesType.RADIUS);
		connectionServiceRadiusAccounting = DcemCluster.getInstance().getClusterConfig().getConnectionService(ConnectionServicesType.RADIUS_ACCOUNTING);
		// String adapterAddress = DcemCluster.getInstance().getClusterConfig().getRadiusListenAdapterAddress();

		// if (radiusPort > 0) {
		// InetAddress addr = null;
		// if (adapterAddress != null && adapterAddress.isEmpty() == false) {
		// try {
		// addr = InetAddress.getByName(adapterAddress);
		// } catch (UnknownHostException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		// }
		if (connectionServiceRadius.isEnabled()) {

			try {
				DatagramSocket ds = new DatagramSocket(connectionServiceRadius.getPort());
				ds.close();
				taskExecutor.schedule(new RadiusListenTask(connectionServiceRadius.getPort(), null, dcemUser), 5, TimeUnit.SECONDS);
				if (connectionServiceRadiusAccounting.isEnabled()) {
					taskExecutor.schedule(new RadiusListenTask(connectionServiceRadiusAccounting.getPort(), null, dcemUser), 6, TimeUnit.SECONDS);
				}
			} catch (SocketException e) {
				radiusReportLogic.addReporting(new RadiusReportEntity(null, RadiusReportAction.HandlingException,
						"Couldn't open the UDP Port: " + connectionServiceRadius.getPort() + ", " + e.toString()));
			}
		}

	}

	public String getResourceName() {
		return RESOUCE_NAME;
	}

	public String getName() {
		return "RADIUS";
	}

	@Override
	public String getId() {
		return MODULE_ID;
	}

	public int getRank() {
		return 50;
	}
	
	@Override
	public boolean isPluginModule() {
		return false;
	}

	@Override
	public DcemView getDefaultView() {
		return null;
	}

	public ModulePreferences getDefaultPreferences() {
		return new RadiusPreferences();
	}

	public RadiusPreferences getPreferences() {
		// ONLY master has preferences
		return (RadiusPreferences) moduleTenantMap.get(TenantIdResolver.getMasterTenant().getName()).getModulePreferences();
	}

	public Map<String, String> getStatisticValues() {
		Map<String, String> map = super.getStatisticValues();
		map.put("Requests", Integer.toString(processRadiusPacket.getRequests()));
		return map;
	}

	@Override
	public void init() throws DcemException {
	}

	@Override
	public void runNightlyTask() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		if (cal.get(Calendar.DAY_OF_MONTH) == 1 || systemModule.getSpecialPropery(DcemConstants.SPECIAL_PROPERTY_RUN_NIGHTLY_TASK) != null) {
			int days = getPreferences().getDurationForReportArchive();
			if (days > 0) {
				try {
					String[] result = exportRecords.archive(days, RadiusReportEntity.class, RadiusReportEntity.GET_AFTER, RadiusReportEntity.DELETE_AFTER);
					if (result != null) {
						logger.info("AsReporting-Archived: File=" + result[0] + " Records=" + result[1]);
					}
				} catch (DcemException exp) {
					logger.warn("Couldn't archive Reports", exp);
				}
			}
		}
	}

	@Override
	public List<PolicyAppEntity> getPolicyApplications() {
		return radiusClientLogic.getApplicationIdentifiers();
	}

	@Override
	public void initializeTenant(TenantEntity tenantEntity) throws DcemException {
		super.initializeTenant(tenantEntity);
		List<RadiusClientEntity> radiusClients = radiusClientLogic.getAllClients();
		for (RadiusClientEntity radiusClientEntity : radiusClients) {
			radiusClientEntity.setTenantName(tenantEntity.getName());
			ipNumberTenantMap.put(radiusClientEntity.getIpNumber(), radiusClientEntity);
		}
	}

	public RadiusClientEntity getRadiusClient(String ipNumber) {
		return ipNumberTenantMap.get(ipNumber);
	}

	@Override
	public void reload(String info) throws DcemException {
		Set<String> keys = ipNumberTenantMap.keySet();
		TenantEntity tenantEntity = TenantIdResolver.getCurrentTenant();
		// first remove all ipnumbers of tenenat
		for (String ipNumber : keys) {
			RadiusClientEntity radiusClientEntity = ipNumberTenantMap.get(ipNumber);
			if (radiusClientEntity.getTenantName().equals(tenantEntity.getName())) {
				ipNumberTenantMap.remove(ipNumber);
			}
		}
		initializeTenant(tenantEntity);
	}

	public ConnectionService getConnectionServiceRadius() {
		return connectionServiceRadius;
	}

	public ConnectionService getConnectionServiceRadiusAccounting() {
		return connectionServiceRadiusAccounting;
	}

}
