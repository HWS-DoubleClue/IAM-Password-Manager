package com.doubleclue.dcem.as.restapi.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.entities.DeviceEntity;
import com.doubleclue.dcem.as.entities.FidoAuthenticatorEntity;
import com.doubleclue.dcem.as.logic.AsDeviceLogic;
import com.doubleclue.dcem.as.logic.AsFidoLogic;
import com.doubleclue.dcem.as.logic.AsModule;
import com.doubleclue.dcem.as.logic.DeviceState;
import com.doubleclue.dcem.as.logic.DeviceStatus;
import com.doubleclue.dcem.as.restapi.model.AsApiDevice;
import com.doubleclue.dcem.as.restapi.model.AsApiDevice.ClientTypeEnum;
import com.doubleclue.dcem.as.restapi.model.AsApiDevice.StateEnum;
import com.doubleclue.dcem.as.restapi.model.AsApiFidoAuthenticator;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.jpa.JpaSelectProducer;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.subjects.AsDeviceSubject;

public class DeviceApiServiceImpl {

	private static Logger logger = LogManager.getLogger(DeviceApiServiceImpl.class);

	@Inject
	EntityManager entityManager;
	
	@Inject
	UserLogic userLogic;

	@Inject
	JpaLogic jpaLogic;

	@Inject
	AsDeviceSubject deviceSubject;

	@Inject
	AsModule asModule;

	@Inject
	AsDeviceLogic deviceLogic;

	@Inject
	AsFidoLogic fidoLogic;

	public Response queryDevices(List<ApiFilterItem> filters, Integer offset, Integer maxResults, SecurityContext securityContext)
			throws DcemApiException {

		JpaSelectProducer<DeviceEntity> jpaSelectProducer = new JpaSelectProducer<DeviceEntity>(entityManager, DeviceEntity.class);
		int firstResult = 0;
		if (offset != null) {
			firstResult = offset.intValue();
		}
		int page = DcemConstants.MAX_DB_RESULTS;
		if (maxResults != null && maxResults.intValue() < page) {
			page = maxResults.intValue();
		}

		try {
			List<DeviceEntity> deviceEntities = jpaSelectProducer.selectCriteriaQueryFilters(filters, firstResult, page, null);
			List<AsApiDevice> devices = new LinkedList<>();
			for (DeviceEntity deviceEntity : deviceEntities) {
				AsApiDevice apiDevice = new AsApiDevice();
				try {
					apiDevice.setDeviceId(deviceEntity.getId());
					apiDevice.setLastTimeLogin(deviceEntity.getLastLoginTime());
					apiDevice.setName(deviceEntity.getName());
					apiDevice.setUserloginId(deviceEntity.getLoginId());
					apiDevice.setOnline(deviceEntity.getStatus() == DeviceStatus.Online || deviceEntity.getStatus() == DeviceStatus.OnlinePasswordLess);
					apiDevice.setClientType(ClientTypeEnum.valueOf(deviceEntity.getAsVersion().getClientType().name()));
					apiDevice.setState(StateEnum.fromValue(deviceEntity.getState().name()));
				} catch (Exception e) {
					throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString(), e);
				}
				devices.add(apiDevice);
			}
			return Response.ok().entity(devices).build();

		} catch (DcemException exp) {
			logger.info(exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}

	}

	public Response deleteDevice(int deviceId, SecurityContext securityContext) {

		try {
			DeviceEntity deviceEntity = deviceLogic.getDevice(deviceId);
			if (deviceEntity == null) {
				throw new DcemException(DcemErrorCodes.INVALID_DEVICE_ID, Integer.toString(deviceId));
			}
			List<DeviceEntity> list = new ArrayList<>(1);
			list.add(deviceEntity);
			deviceLogic.deleteDevices(list, new DcemAction(deviceSubject, DcemConstants.ACTION_DELETE));
		} catch (DcemException exp) {
			logger.info(exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}

		return Response.ok().build();
	}

	public Response setDeviceState(int deviceId, boolean enableState, SecurityContext securityContext) {
		try {
			DeviceEntity deviceEntity = deviceLogic.getDevice(deviceId);
			if (deviceEntity == null) {
				throw new DcemException(DcemErrorCodes.INVALID_DEVICE_ID, Integer.toString(deviceId));
			}
			DeviceState deviceState;
			if (enableState == true) {
				deviceState = DeviceState.Enabled;
			} else {
				deviceState = DeviceState.Disabled;
			}
			deviceLogic.setDeviceState(deviceId, deviceState);
		} catch (DcemException exp) {
			logger.info(exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
		return Response.ok().build();
	}

	public Response fidoStartRegistration(int userId, String rpId, SecurityContext securityContext) {
		try {
			DcemUser dcemUser = userLogic.getUser(userId);
			String request = fidoLogic.startRegistration(dcemUser, rpId);
			return Response.ok().entity(request).build();
		} catch (DcemException exp) {
			logger.info(exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
	}

	public Response fidoFinishRegistration(String regResponseJson, String displayName, SecurityContext securityContext) {
		try {
			String response = fidoLogic.finishRegistration(regResponseJson, displayName);
			return Response.ok().entity(response).build();
		} catch (DcemException exp) {
			logger.info(exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
	}

	public Response queryFidoAuthenticators(List<ApiFilterItem> filterItems, Integer offset, Integer maxResults, SecurityContext securityContext)
			throws DcemApiException {

		JpaSelectProducer<FidoAuthenticatorEntity> jpaSelectProducer = new JpaSelectProducer<FidoAuthenticatorEntity>(entityManager,
				FidoAuthenticatorEntity.class);

		int firstResult = 0;
		if (offset != null) {
			firstResult = offset.intValue();
		}

		int page = DcemConstants.MAX_DB_RESULTS;
		if (maxResults != null && maxResults.intValue() < page) {
			page = maxResults.intValue();
		}

		try {
			List<FidoAuthenticatorEntity> entities = jpaSelectProducer.selectCriteriaQueryFilters(filterItems, firstResult, page, null);
			List<AsApiFidoAuthenticator> fidoAuthenticators = new LinkedList<>();
			for (FidoAuthenticatorEntity entity : entities) {
				AsApiFidoAuthenticator fidoAuthenticator = new AsApiFidoAuthenticator();
				try {
					fidoAuthenticator.setFidoAuthenticatorId((int) entity.getId());
					fidoAuthenticator.setUserLoginId(entity.getUser().getLoginId());
					fidoAuthenticator.setRegisteredOn(entity.getRegisteredOn());
					fidoAuthenticator.setLastUsed(entity.getLastUsed());
					fidoAuthenticator.setDisplayName(entity.getDisplayName());
				} catch (Exception e) {
					throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString(), e);
				}
				fidoAuthenticators.add(fidoAuthenticator);
			}
			return Response.ok().entity(fidoAuthenticators).build();

		} catch (DcemException exp) {
			logger.info(exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
	}

	public Response deleteFidoAuthenticator(int fidoAuthenticatorId, SecurityContext securityContext) {
		try {
			fidoLogic.deleteFidoAuthenticator(fidoAuthenticatorId);
		} catch (DcemException exp) {
			logger.info(exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
		return Response.ok().build();
	}
}
