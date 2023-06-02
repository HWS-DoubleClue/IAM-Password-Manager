package com.doubleclue.dcem.as.logic;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import com.doubleclue.comm.thrift.ActivationParam;
import com.doubleclue.comm.thrift.AppException;
import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.as.entities.AsVersionEntity;
import com.doubleclue.dcem.as.entities.DeviceEntity;
import com.doubleclue.dcem.as.entities.DeviceEntity_;
import com.doubleclue.dcem.as.tasks.KillDeviceTask;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemNode;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.JpaSelectProducer;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.subjects.AsDeviceSubject;
import com.doubleclue.utils.RandomUtils;
import com.doubleclue.utils.TimeBasedPasscodeGenerator;

@ApplicationScoped

public class AsDeviceLogic {

	@Inject
	EntityManager em;

	@Inject
	AsModule module;

	@Inject
	AdminModule adminModule;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	AsMessageLogic messageLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	DcemReportingLogic reportingLogic;

	@Inject
	DcemApplicationBean dcemApplicationBean;

	@Inject
	AsDeviceSubject deviceSubject;

	@Inject
	AsVersionLogic versionLogic;

	@DcemTransactional
	public DeviceEntity addDevice(DcemUser user, AsVersionEntity version, ActivationParam activationParam, PublicKey publicKey) throws AppException {

		DeviceEntity device = new DeviceEntity();
		device.setUser(user);
		device.setAsVersion(version);
		device.setName(activationParam.getDeviceName());
		device.setAppOsVersion(activationParam.getOsVersion());
		device.setDeviceHash(activationParam.getDigest());
		device.setManufacture(activationParam.getManufacture());
		device.setUdid(activationParam.getUdid());
		device.setDeviceKey(RandomUtils.getRandom(32));
		device.setOfflineKey(RandomUtils.getRandom(16));
		device.setState(DeviceState.Enabled);
		device.setStatus(DeviceStatus.Offline);
		if (publicKey != null) {
			device.setPublicKey(publicKey.getEncoded());
		}
		device.setLastLoginTime(new Date());
		em.persist(device);
		return device;
	}

	@DcemTransactional
	public DeviceEntity addRootDevice(DcemUser user) throws AppException {

		DeviceEntity device = new DeviceEntity();
		device.setUser(user);
		device.setName(DcemConstants.DEVICE_ROOT);
		device.setState(DeviceState.Disabled);
		device.setStatus(DeviceStatus.Offline);
		device.setLastLoginTime(new Date());
		em.persist(device);
		return device;
	}

	public DeviceEntity getDevice(int id) {
		// System.out.println("AsDeviceLogic.getDevice()");
		return em.find(DeviceEntity.class, id);
	}

	public DeviceEntity findDevice(int id) {
		return em.find(DeviceEntity.class, id);
	}

	public DeviceEntity getDeviceDetached(int id) {
		// System.out.println("AsDeviceLogic.getDeviceDetached()");
		DeviceEntity device = em.find(DeviceEntity.class, id);
		if (device == null) {
			return null;
		}
		em.detach(device);
		return device;
	}

	public boolean verifyUserHash(DeviceEntity device, byte[] pinHash) {
		if (Arrays.equals(pinHash, device.getUser().getHashPassword())) {
			return true;
		}
		incRetryCounter(device);
		return false;
	}

	public void incRetryCounter(DeviceEntity device) {
		int rc = device.getRetryCounter();
		rc++;
		if (rc > adminModule.getPreferences().getPasswordMaxRetryCounter()) {
			device.setState(DeviceState.Disabled);
		}
		device.setRetryCounter(rc); // do DB-updated afterwards

	}

	public List<DevicesUserDto> getDevicesByUser(DcemUser user) {
		TypedQuery<DevicesUserDto> query = em.createNamedQuery(DeviceEntity.GET_DEVICES_BY_USER, DevicesUserDto.class);
		query.setParameter(1, user);
		return query.getResultList();
	}

	public List<DevicesUserDtoOffline> getDevicesOffByUser(DcemUser user) {
		TypedQuery<DevicesUserDtoOffline> query = em.createNamedQuery(DeviceEntity.GET_DEVICES_OFF_BY_USER, DevicesUserDtoOffline.class);
		query.setParameter(1, user);
		return query.getResultList();
	}

	private List<DeviceEntity> getDevicesOnByUser(DcemUser user) {
		TypedQuery<DeviceEntity> query = em.createNamedQuery(DeviceEntity.GET_DEVICE_ON_BY_USER, DeviceEntity.class);
		query.setParameter(1, user);
		return query.getResultList();
	}

	@DcemTransactional
	public void setDeviceOnline(DeviceEntity deviceDetached) {
		// System.out.println("AsDeviceLogic.saveDetachedDevice()");

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaUpdate<DeviceEntity> updateCriteria = cb.createCriteriaUpdate(DeviceEntity.class);
		Root<DeviceEntity> root = updateCriteria.from(DeviceEntity.class);
		// update dateOfBirth property
		updateCriteria.set(root.get(DeviceEntity_.status.getName()), deviceDetached.getStatus());
		updateCriteria.set(root.get(DeviceEntity_.lastLoginTime.getName()), new Date());
		updateCriteria.set(root.get(DeviceEntity_.nodeId.getName()), DcemCluster.getInstance().getDcemNode().getId());
		updateCriteria.set(root.get(DeviceEntity_.retryCounter), deviceDetached.getRetryCounter());
		// set where clause
		updateCriteria.where(cb.equal(root.get("id"), deviceDetached.getId()));
		em.createQuery(updateCriteria).executeUpdate();

	}

	@DcemTransactional
	public void updateDeviceVersion(DeviceEntity deviceDetached) {

		// System.out.println("AsDeviceLogic.saveDetachedDevice()");

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaUpdate<DeviceEntity> updateCriteria = cb.createCriteriaUpdate(DeviceEntity.class);
		Root<DeviceEntity> root = updateCriteria.from(DeviceEntity.class);
		// update dateOfBirth property
		updateCriteria.set(root.get(DeviceEntity_.asVersion.getName()), deviceDetached.getAsVersion());
		// set where clause
		updateCriteria.where(cb.equal(root.get("id"), deviceDetached.getId()));
		em.createQuery(updateCriteria).executeUpdate();

	}

	public List<String> getDeviceNames(DcemUser user) {
		TypedQuery<String> query = em.createNamedQuery(DeviceEntity.GET_USER_DEVICES_NAMES, String.class);
		query.setParameter(1, user);
		return query.getResultList();
	}

	public DeviceEntity getDeviceByName(DcemUser user, String deviceName, DeviceState state) {
		TypedQuery<DeviceEntity> query = em.createNamedQuery(DeviceEntity.GET_DEVICE_BY_NAME, DeviceEntity.class);
		query.setParameter(1, user);
		query.setParameter(2, deviceName);
		query.setParameter(3, state);
		try {
			return query.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	public String getUniqueDeviceName(DcemUser user, String deviceName) {
		TypedQuery<String> query = em.createNamedQuery(DeviceEntity.GET_DEVICE_BY_NAME_USER, String.class);
		query.setParameter(1, user);
		query.setParameter(2, deviceName + "%");
		query.setMaxResults(100);
		try {
			List<String> nameList = query.getResultList();
			if (nameList.contains(deviceName) == false) {
				return deviceName;
			}
			nameList.remove(deviceName);
			int ind;
			int maxInd = 0;
			for (String name : nameList) {
				ind = name.lastIndexOf(':');
				try {
					ind = Integer.parseInt(name.substring(ind + 1));
					if (ind > maxInd) {
						maxInd = ind;
					}
				} catch (Exception e) {
					maxInd++;
				}
			}
			maxInd++;
			return deviceName + ":" + maxInd;
		} catch (NoResultException exp) {
			return deviceName;
		}
	}

	@DcemTransactional
	public void setDeviceOff(DeviceEntity device) {
		if (device == null || device.getId() == null) {
			return;
		}
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaUpdate<DeviceEntity> updateCriteria = cb.createCriteriaUpdate(DeviceEntity.class);
		Root<DeviceEntity> root = updateCriteria.from(DeviceEntity.class);
		// update dateOfBirth property
		updateCriteria.set(root.get("status"), DeviceStatus.Offline);
		// set where clause
		updateCriteria.where(cb.equal(root.get("id"), device.getId()));
		em.createQuery(updateCriteria).executeUpdate();
	}

	@DcemTransactional
	public void updateDeviceRc(DeviceEntity device, DcemAction action) {
		if (device == null || device.getId() == null) {
			return;
		}
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaUpdate<DeviceEntity> updateCriteria = cb.createCriteriaUpdate(DeviceEntity.class);
		Root<DeviceEntity> root = updateCriteria.from(DeviceEntity.class);
		// update dateOfBirth property
		updateCriteria.set(root.get("retryCounter"), device.getRetryCounter());
		updateCriteria.set(root.get("state"), device.getState());
		// set where clause
		updateCriteria.where(cb.equal(root.get("id"), device.getId()));
		em.createQuery(updateCriteria).executeUpdate();
		if (action != null) {
			auditingLogic.addAudit(action, device.toString());
		}
	}

	@DcemTransactional
	public int resetDevicesStatus(DcemNode node) {
		Query query = em.createNamedQuery(DeviceEntity.RESET_DEVICES_STATUS);
		query.setParameter(1, node.getId());
		return query.executeUpdate();
	}

	@DcemTransactional
	public void setDeviceState(int deviceId, DeviceState deviceState) throws DcemException {
		DeviceEntity deviceEntity = getDevice(deviceId); // reload from DB
		if (deviceEntity == null) {
			throw new DcemException(DcemErrorCodes.INVALID_DEVICE_ID, Integer.toString(deviceId));
		}
		deviceEntity.setState(deviceState);
		DcemAction dcemAction = null;
		switch (deviceState) {

		case Disabled:
		case TempLocked:
			dcemAction = new DcemAction(deviceSubject, DcemConstants.ACTION_DISABLE);
			break;
		case Enabled:
			dcemAction = new DcemAction(deviceSubject, DcemConstants.ACTION_ENABLE);
		}
		auditingLogic.addAudit(dcemAction, deviceEntity.toString());
		if (deviceState.equals(DeviceState.Disabled)) {
			setDeviceOff(deviceEntity);
			killDevice(deviceEntity);
		}
	}

	private void killDevice(DeviceEntity device) {
		if (device.getStatus() == DeviceStatus.Online || device.getStatus() == DeviceStatus.OnlinePasswordLess) {
			DcemNode dcemNode = dcemApplicationBean.getDcemNodeById(device.getNodeId());
			if (dcemNode != null) {
				DcemCluster.getInstance().getExecutorService().executeOnMember(new KillDeviceTask(device.getId(), TenantIdResolver.getCurrentTenant()),
						DcemCluster.getInstance().getMember(dcemNode));
			}
		}
	}

	@DcemTransactional
	public void deleteDevices(List<?> list, DcemAction dcemAction) {
		deleteDevices(list, dcemAction, true);
	}

	@DcemTransactional
	public void deleteDevices(List<?> list, DcemAction dcemAction, boolean killDeviceSessions) {
		StringBuffer sb = new StringBuffer();

		for (Object obj : list) {
			sb.append(obj.toString());
			DeviceEntity device = (DeviceEntity) obj;
			device = findDevice(device.getId()); // reload from DB
			if (device == null) {
				continue;
			}
			if (killDeviceSessions) {
				killDevice(device);
			}
			messageLogic.deleteDeviceMsg(device);
			cloudSafeLogic.deleteDeviceData(device);
			em.remove(device);
			if (dcemAction != null) {
				DcemReporting reporting = new DcemReporting(ReportAction.DeleteDevice, (DcemUser) null, null, null, device.toString());
				reportingLogic.addReporting(reporting);
			}

		}
		if (dcemAction != null) {
			auditingLogic.addAudit(dcemAction, sb.toString());
		}
	}

	public void verifyUserPasscode(DcemUser dcemUser, String passcode) throws DcemException {
		int rc = dcemUser.getPassCounter();
		if (rc >= adminModule.getPreferences().getPasswordMaxRetryCounter()) {
			throw new DcemException(DcemErrorCodes.USER_PASSWORD_MAX_RETRIES, dcemUser.getLoginId());
		}
		int inPasscode;
		try {
			inPasscode = Integer.parseInt(passcode);
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.PASSCODE_NOT_NUMERIC, null);
		}
		List<DevicesUserDtoOffline> devicesUserDtos = getDevicesOffByUser(dcemUser);
		int validFor = module.getPreferences().getPasscodeValidFor();
		int window = module.getPreferences().getPasscodeWindow();
		for (DevicesUserDtoOffline device : devicesUserDtos) {
			try {
				if (device.getOfflineKey() != null) {
					if (TimeBasedPasscodeGenerator.verifyPasscode(device.getOfflineKey(), device.getUdid(), validFor * 60 * 1000, inPasscode, window) == true) {
						userLogic.resetPasswordCounter(dcemUser);
						return; // OK
					}
				}
			} catch (Exception e) {
				throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString());
			}
		}
		throw new DcemException(DcemErrorCodes.INVALID_OTP, dcemUser.getLoginId());
	}

	public void verifyDevicePasscode(DeviceEntity deviceEntity, int passcode) throws DcemException {
		if (deviceEntity.getOfflineKey() != null) {
			int validFor = module.getPreferences().getPasscodeValidFor();
			try {
				if (TimeBasedPasscodeGenerator.verifyPasscode(deviceEntity.getOfflineKey(), deviceEntity.getUdid(), validFor * 60 * 1000, passcode,
						module.getPreferences().getPasscodeWindow()) == true) {
					userLogic.resetPasswordCounter(deviceEntity.getUser());
					return; // OK
				}
			} catch (Exception e) {
				throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString());
			}
		}
		throw new DcemException(DcemErrorCodes.INVALID_OTP, deviceEntity.getUser().getLoginId());
	}

	@DcemTransactional
	public void deleteUserDevices(DcemUser dcemUser) {
		TypedQuery<DeviceEntity> query = em.createNamedQuery(DeviceEntity.GET_ALL_USER_DEVICES, DeviceEntity.class);
		query.setParameter(1, dcemUser);
		deleteDevices(query.getResultList(), null);
	}

	public List<DeviceEntity> queryDevices(List<ApiFilterItem> filterItems, Integer offset, Integer maxResults) throws DcemException {
		JpaSelectProducer<DeviceEntity> jpaSelectProducer = new JpaSelectProducer<DeviceEntity>(em, DeviceEntity.class);
		int firstResult = 0;
		if (offset != null) {
			firstResult = offset.intValue();
		}
		int page = DcemConstants.MAX_DB_RESULTS;
		if (maxResults != null && maxResults.intValue() < page) {
			page = maxResults.intValue();
		}
		return jpaSelectProducer.selectCriteriaQueryFilters(filterItems, firstResult, page, null);
	}

	public void killUserDevices(DcemUser dcemUser) {
		List<DeviceEntity> list = getDevicesOnByUser(dcemUser);
		for (DeviceEntity entity : list) {
			killDevice(entity);
		}
	}

}
