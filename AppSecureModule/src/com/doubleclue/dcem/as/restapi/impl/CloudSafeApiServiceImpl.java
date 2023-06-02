package com.doubleclue.dcem.as.restapi.impl;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.logic.AsDeviceLogic;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.as.restapi.model.AsApiCloudSafeFile;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jersey.DcemApiException;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.jpa.JpaSelectProducer;
import com.doubleclue.dcem.core.logic.UserLogic;

public class CloudSafeApiServiceImpl {

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	AsDeviceLogic deviceLogic;

	@Inject
	EntityManager entityManager;

	private static Logger logger = LogManager.getLogger(CloudSafeApiServiceImpl.class);

	/**
	 * @param owner
	 * @param name
	 * @param securityContext
	 * @return
	 * @throws NotFoundException
	 */
//	public Response readCloudSafeFile(AsApiCloudSafeFile cloudSafeFile, SecurityContext securityContext) throws ApiException {
//		CloudSafeEntity cloudDataEntity = null;
//		try {
//			CloudSafeOwner CloudSafeOwner = null;
//			if (cloudSafeFile.getId() == 0) {
//				try {
//					CloudSafeOwner = CloudSafeOwner.valueOf(cloudSafeFile.getOwner().name());
//				} catch (Exception e) {
//					throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_OWNER, cloudSafeFile.getOwner().name());
//				}
//
//				if (CloudSafeOwner == null) {
//					logger.info(DcemErrorCodes.INVALID_CLOUDDATA_OWNER.name());
//					throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_OWNER, null);
//				}
//				DcemUser user = null;
//				DeviceEntity device = null;
//				if (CloudSafeOwner == CloudSafeOwner.USER || CloudSafeOwner == CloudSafeOwner.DEVICE) {
//					user = userLogic.getUser(cloudSafeFile.getUserLoginId());
//					if (user == null) {
//						throw new DcemException(DcemErrorCodes.INVALID_USER_ID, cloudSafeFile.getUserLoginId());
//					}
//				}
//				if (CloudSafeOwner == CloudSafeOwner.DEVICE) {
//					device = deviceLogic.getDeviceByName(user, cloudSafeFile.getDeviceName());
//					if (device == null) {
//						throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_DEVICE, cloudSafeFile.getDeviceName());
//					}
//				}
//				cloudDataEntity = cloudSafeLogic.getCloudSafe(CloudSafeOwner, cloudSafeFile.getName(), user, device);
//			} else {
//				cloudDataEntity = cloudSafeLogic.getCloudSafe(cloudSafeFile.getId());
//			}
//
//			if (cloudDataEntity == null) {
//				throw new DcemException(DcemErrorCodes.CLOUDDATA_NOT_FOUND, null);
//			}
//			AsApiCloudSafeFile prop = new AsApiCloudSafeFile(cloudDataEntity);
//			char [] password = null;
//			if (cloudSafeFile.getPassword() != null) {
//				password = cloudSafeFile.getPassword().toCharArray();
//			}
//			prop.setContent(cloudSafeLogic.getCloudSafeContentAsBytes(cloudDataEntity, password));
//			return Response.ok().entity(prop).build();
//
//		} catch (DcemException e) {
//			logger.info("getDataMap Failed" + e.getErrorCode().name());
//			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new AsApiException(e)).build();
//		} catch (Exception e) {
//			logger.info("getDataMap Failed", e);
//			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new AsApiException(DcemErrorCodes.GENERAL.getErrorCode(), e.getMessage(), null))
//					.build();
//		}
//	}

	/**
	 * @param filterItems
	 * @param offset
	 * @param maxResults
	 * @param securityContext
	 * @return
	 * @throws DcemApiException
	 */
	public Response queryCloudSafe(List<ApiFilterItem> filterItems, Integer offset, Integer maxResults, SecurityContext securityContext)
			throws DcemApiException {

		JpaSelectProducer<CloudSafeEntity> jpaSelectProducer = new JpaSelectProducer<CloudSafeEntity>(entityManager,
				CloudSafeEntity.class);
		int firstResult = 0;
		if (offset != null) {
			firstResult = offset.intValue();
		}
		int page = DcemConstants.MAX_DB_RESULTS;
		if (maxResults != null && maxResults.intValue() < page) {
			page = maxResults.intValue();
		}

		try {
			List<CloudSafeEntity> properties = jpaSelectProducer.selectCriteriaQueryFilters(filterItems, firstResult, page, null);
			List<AsApiCloudSafeFile> asApiPropertys = new LinkedList<>();
			for (CloudSafeEntity cloudDataWoContentEntity : properties) {
				try {
					AsApiCloudSafeFile asApiCloudData = new AsApiCloudSafeFile(cloudDataWoContentEntity);
					asApiPropertys.add(asApiCloudData);
				} catch (Exception e) {
					throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, e.toString(), e);
				}

			}
			return Response.ok().entity(asApiPropertys).build();

		} catch (DcemException exp) {
			logger.info(exp);
			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new DcemApiException(exp)).build();
		}
	}

	/*
	 * 
	 */
//	public Response writeCloudSafeFile(AsApiCloudSafeFile asApiCloudSafeFile, String userLoginId, SecurityContext securityContext) throws NotFoundException {
//
//		CloudSafeOwner CloudSafeOwner = null;
//		try {
//			try {
//				CloudSafeOwner = CloudSafeOwner.valueOf(asApiCloudSafeFile.getOwner().name());
//			} catch (Exception e) {
//				throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_OWNER, asApiCloudSafeFile.getOwner().name());
//			}
//
//			String name = asApiCloudSafeFile.getName();
//			if (asApiCloudSafeFile.getName() == null || asApiCloudSafeFile.getName().isEmpty()) {
//				throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_NAME, null);
//			}
//
//			DcemUser user = null;
//			DeviceEntity device = null;
//
//			switch (CloudSafeOwner) {
//			case USER:
//				user = userLogic.getUser(asApiCloudSafeFile.getUserLoginId());
//				if (user == null) {
//					throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_USER, asApiCloudSafeFile.getUserLoginId());
//				}
//				break;
//			case DEVICE:
//				user = userLogic.getUser(asApiCloudSafeFile.getUserLoginId());
//				if (user == null) {
//					throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_USER, asApiCloudSafeFile.getUserLoginId());
//				}
//				device = deviceLogic.getDeviceByName(user, asApiCloudSafeFile.getDeviceName());
//				if (device == null) {
//					throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_DEVICE, asApiCloudSafeFile.getDeviceName());
//				}
//				break;
//			default:
//				break;
//			}
//
//			CloudSafeEntity cloudDataEntity = new CloudSafeEntity(CloudSafeOwner, user, device, name, asApiCloudSafeFile.getDiscardAfter(), asApiCloudSafeFile.getOptions(), null);
//
//			if (CloudSafeOwner != CloudSafeOwner.GLOBAL && user.getLoginId().equals(userLoginId) == false) { // shared file
//				DcemUser sharedUser = userLogic.getUser(userLoginId);
//				if (sharedUser == null) {
//					throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_USER, userLoginId);
//				}
//				CloudSafeEntity cloudSafeEntity = cloudSafeLogic.getUserCloudSafe(name, user);
//				if (cloudSafeEntity != null) {
//					CloudSafeShareEntity cloudShare = cloudSafeLogic.getCloudShare(cloudSafeEntity, sharedUser, null);
//					if (cloudShare == null || !cloudShare.isWriteAccess()) {
//						throw new DcemException(DcemErrorCodes.NO_WRITE_ACCESS,
//								"User " + userLoginId + " tried to overwrite " + user.getLoginId() + "'s file: " + name);
//					}
//				} else {
//					throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_USER, "Tried to overwrite nonexistent shared file");
//				}
//			}
//			if (asApiCloudSafeFile.getPassword() != null && asApiCloudSafeFile.getPassword().isEmpty() == false) {
//				if (cloudDataEntity.getOptions() == null || cloudDataEntity.getOptions().isEmpty()) {
//					cloudDataEntity.setOptions(CloudSafeOptions.PWD.name());
//				}
//				if (cloudDataEntity.getOptions().contains(CloudSafeOptions.PWD.name()) == false) {
//					cloudDataEntity.setOptions(cloudDataEntity.getOptions() + " " + CloudSafeOptions.PWD);
//				}
//				cloudSafeLogic.setCloudSafe(cloudDataEntity, true);
//				cloudSafeLogic.setCloudSafeContent(cloudDataEntity, asApiCloudSafeFile.getPassword().toCharArray(), asApiCloudSafeFile.getContent());
//			} else {
//				CloudSafeEntity cloudSafeEntity = cloudSafeLogic.setCloudSafe(cloudDataEntity, true);
//				cloudSafeLogic.setCloudSafeContent(cloudSafeEntity, null, asApiCloudSafeFile.getContent());
//			}
//
//		} catch (DcemException e) {
//			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new AsApiException(e)).build();
//		}
//		return Response.ok().build();
//	}

//	public Response deleteCloudSafeFile(AsApiCloudSafeFile cloudSafeFile, SecurityContext securityContext) {
//		try {
//			cloudSafeLogic.deleteCloudSafeFile(cloudSafeFile);
//		} catch (DcemException e) {
//			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new AsApiException(e)).build();
//		}
//		return Response.ok().build();
//	}

//	public Response shareCloudSafeFile(AsApiShareCloudSafe shareCloudSafe, SecurityContext securityContext) {
//	TODO	try {
//			cloudSafeLogic.shareCloudSafeFile(shareCloudSafe);
//		} catch (DcemException e) {
//			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new AsApiException(e)).build();
//		}
//		return Response.ok().build();
//	}

//	public Response getShareCloudSafeFiles(String userLoginId, String nameFilter, SecurityContext securityContext) {
//		try {
//			List<CloudSafeShareEntity> entities = cloudSafeLogic.getShareCloudSafeFiles(userLoginId, nameFilter);
//			List<AsApiShareCloudSafeDetails> list = new ArrayList<AsApiShareCloudSafeDetails>(entities.size());
//			for (CloudSafeShareEntity entity : entities) {
//				AsApiShareCloudSafeDetails apiShareCloudSafeDetails = new AsApiShareCloudSafeDetails();
//				apiShareCloudSafeDetails.setWriteAccess(entity.isWriteAccess());
//				apiShareCloudSafeDetails.setCloudSafeFile(new AsApiCloudSafeFile(entity.getCloudSafeEntity()));
//			}
//			return Response.ok().entity(list).build();
//		} catch (DcemException e) {
//			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new AsApiException(e)).build();
//		}
//	}

//	public Response removeShareCloudSafeFile(AsApiShareCloudSafe shareCloudSafe, SecurityContext securityContext) {
//		//TODO 
////		try {
////			cloudSafeLogic.removeShareCloudSafeFile(shareCloudSafe);
////		} catch (DcemException e) {
////			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new AsApiException(e)).build();
////		}
//		return Response.ok().build();
//	}

//	public Response getSharedCloudSafeUsersAccess(AsApiCloudSafeFile cloudSafeFile, SecurityContext securityContext) {
//		try {
//			List<CloudSafeShareEntity> dbList = cloudSafeLogic.getSharedCloudSafeUsersAccess(cloudSafeFile);
//			List<AsApiShareCloudSafe> list = new ArrayList<>(dbList.size());
//			for (CloudSafeShareEntity cloudDataShareEntity : dbList) {
//				list.add(new AsApiShareCloudSafe(cloudDataShareEntity));
//			}
//			return Response.ok().entity(list).build();
//		} catch (DcemException e) {
//			return Response.status(DcemConstants.SERVER_LOGIC_EXCEPTION).entity(new AsApiException(e)).build();
//		}
//	}
}
