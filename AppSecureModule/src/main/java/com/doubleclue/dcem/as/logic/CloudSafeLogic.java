package com.doubleclue.dcem.as.logic;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.AbstractQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;

import com.doubleclue.comm.thrift.CloudSafeOptions;
import com.doubleclue.comm.thrift.CloudSafeOwner;
import com.doubleclue.comm.thrift.SdkCloudSafe;
import com.doubleclue.comm.thrift.SdkCloudSafeKey;
import com.doubleclue.dcem.admin.logic.AlertSeverity;
import com.doubleclue.dcem.admin.logic.DcemReportingLogic;
import com.doubleclue.dcem.admin.logic.ReportAction;
import com.doubleclue.dcem.as.dm.DmModuleApi;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.CloudSafeEntity_;
import com.doubleclue.dcem.as.entities.CloudSafeLimitEntity;
import com.doubleclue.dcem.as.entities.CloudSafeShareEntity;
import com.doubleclue.dcem.as.entities.CloudSafeTagEntity;
import com.doubleclue.dcem.as.entities.CloudSafeThumbnailEntity;
import com.doubleclue.dcem.as.entities.DeviceEntity;
import com.doubleclue.dcem.as.logic.cloudsafe.CloudSafeContentDb;
import com.doubleclue.dcem.as.logic.cloudsafe.CloudSafeContentI;
import com.doubleclue.dcem.as.logic.cloudsafe.CloudSafeContentNas;
import com.doubleclue.dcem.as.logic.cloudsafe.CloudSafeContentS3;
import com.doubleclue.dcem.as.logic.cloudsafe.DocumentVersion;
import com.doubleclue.dcem.as.restapi.model.AsApiCloudSafeFile;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.DcemUploadFile;
import com.doubleclue.dcem.core.config.CloudSafeStorageType;
import com.doubleclue.dcem.core.config.ClusterConfig;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemGroup;
import com.doubleclue.dcem.core.entities.DcemReporting;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.ApiFilterItem;
import com.doubleclue.dcem.core.jpa.DbEncryption;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.jpa.JpaSelectProducer;
import com.doubleclue.dcem.core.licence.LicenceKeyContent;
import com.doubleclue.dcem.core.licence.LicenceLogic;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.ConfigLogic;
import com.doubleclue.dcem.core.logic.GroupLogic;
import com.doubleclue.dcem.core.logic.JpaLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.utils.SecureServerUtils;
import com.doubleclue.dcem.core.weld.CdiUtils;
import com.doubleclue.dcem.subjects.AsCloudSafeSubject;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.RandomUtils;
import com.doubleclue.utils.StringUtils;
import com.google.common.primitives.Bytes;
import com.hazelcast.core.IAtomicLong;

import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@ApplicationScoped
public class CloudSafeLogic {

	private static Logger logger = LogManager.getLogger(CloudSafeLogic.class);

	final static int MAX_CIPHER_BUFFER = 1024 * 64;
	public static final byte[] FOLDER_CONTENT_TO_ENCRYPT = { 0x01, 0x02, 0x3, 0x04, 0x01, 0x02, 0x3, 0x04, 0x01, 0x02, 0x3, 0x040, 0x01, 0x02, 0x3, 0x04, 0x01,
			0x02, 0x3, 0x04, 0x01, 0x02, 0x3, 0x04 };

	final static String AUDIT_SHARED_BY = ", Shared By: ";
//	final static String OCR_TEXT = "ocr_text";

	final static public String FOLDER_SEPERATOR = "/";

	@Inject
	EntityManager em;

	@Inject
	AsModule asModule;

	@Inject
	UserLogic userLogic;

	@Inject
	GroupLogic groupLogic;

	@Inject
	AsDeviceLogic deviceLogic;

	@Inject
	JpaLogic jpaLogic;

	@Inject
	AuditingLogic auditingLogic;

	@Inject
	AsCloudSafeSubject asCloudSafeSubject;

	@Inject
	private ConfigLogic configLogic;

	@Inject
	private DcemReportingLogic reportingLogic;

	@Inject
	LicenceLogic licenceLogic;

	CloudSafeContentI cloudSafeContentI;
	CloudSafeStorageType cloudSafeStorageType;

	@PostConstruct
	public void init() {
		try {
			ClusterConfig clusterConfig = configLogic.getClusterConfig();
			cloudSafeStorageType = clusterConfig.getCloudSafeStorageType();
			switch (cloudSafeStorageType) {
			case Database:
				cloudSafeContentI = new CloudSafeContentDb();
				break;
			case NetworkAccessStorage:
				File file = new File(clusterConfig.getNasDirectory());
				if (file.exists() == false) {
					file.mkdir();
				}
				cloudSafeContentI = new CloudSafeContentNas(file);
				break;
			case AwsS3:
				cloudSafeContentI = new CloudSafeContentS3(clusterConfig.getName(), clusterConfig.getAwsS3Url(), clusterConfig.getAwsS3AccesskeyId(),
						clusterConfig.getAwsS3SecretAccessKey());
				break;
			default:
				reportingLogic.addWelcomeViewAlert(DcemConstants.ALERT_CATEGORY_DCEM, DcemErrorCodes.INVALID_CLOUDSTORAGE_TYPE,
						clusterConfig.getCloudSafeStorageType().name(), AlertSeverity.ERROR, true);
				break;
			}

		} catch (DcemException e) {
			reportingLogic.addWelcomeViewAlert(DcemConstants.ALERT_CATEGORY_DCEM, e.getErrorCode(), e.getMessage(), AlertSeverity.ERROR, false);
		} catch (Exception e) {
			logger.error("couldn't initialze CloudSafe: " + cloudSafeStorageType, e);
			reportingLogic.addWelcomeViewAlert(DcemConstants.ALERT_CATEGORY_DCEM, DcemErrorCodes.CLOUD_SAFE_CONFIGURATION, e.getMessage(), AlertSeverity.ERROR,
					false);
		}
	}

	public String getContentAsStringWoChiper(int id) throws DcemException {
		InputStream inputStream = null;
		StringWriter writer = new StringWriter();
		try {
			inputStream = cloudSafeContentI.getContentInputStream(em, id);
			KaraUtils.copyStream(inputStream, writer);
			return writer.toString();
		} catch (Exception exp) {
			throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_ID, "");
		} finally {
			if (inputStream != null) {
				try {
					writer.close();
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public String getContentAsString(CloudSafeEntity cloudSafeEntity, char[] password, DcemUser auditUser) throws DcemException {
		InputStream inputStream = null;
		try {
			inputStream = getCloudSafeContentAsStream(cloudSafeEntity, password, auditUser, null);
			return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
		} catch (Exception exp) {
			throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_ID, "");
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/*
	 * Use this method for short entries only
	 */
	public byte[] getContentAsBytes(CloudSafeEntity cloudSafeEntity, char[] password, DcemUser auditUser) throws DcemException {
		InputStream inputStream = null;
		try {
			inputStream = getCloudSafeContentAsStream(cloudSafeEntity, password, auditUser, null);
			return KaraUtils.readInputStream(inputStream);
		} catch (Exception exp) {
			throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_ID, "", exp);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	@DcemTransactional
	public InputStream getCloudSafeContentAsStream(CloudSafeEntity cloudSafeEntity, char[] password, DcemUser auditUser) throws DcemException {
		return getCloudSafeContentAsStream(cloudSafeEntity, password, auditUser, null);
	}

	@DcemTransactional
	public InputStream getCloudSafeContentAsStream(CloudSafeEntity cloudSafeEntity, char[] password, DcemUser auditUser, String versionId) throws DcemException {
		if (cloudSafeEntity.getName().endsWith(AsConstants.EXTENSION_PASSWORD_SAFE)) {
			if (passwordSafeEnabled(cloudSafeEntity.getUser()) == false) {
				throw new DcemException(DcemErrorCodes.PASSWORD_SAFE_NOT_ENABLED, "PasswordSafe is disabled for this user. Please contact your Administrator.");
			}
		}
		if (cloudSafeEntity.getId() == null) {
			if (cloudSafeEntity.getParent() == null) {
				cloudSafeEntity.setParent(getCloudSafeRoot());
			}
			cloudSafeEntity = getCloudSafe(cloudSafeEntity.getOwner(), cloudSafeEntity.getName(), cloudSafeEntity.getUser(), null,
					cloudSafeEntity.getParent().getId(), cloudSafeEntity.getGroup(), cloudSafeEntity.isRecycled());
		}
		try {
			InputStream inputStream;
			if (versionId != null) {
				inputStream = cloudSafeContentI.getS3ContentInputStream(cloudSafeEntity.getId(), null, versionId);
			} else {
				inputStream = cloudSafeContentI.getContentInputStream(em, cloudSafeEntity.getId());
			}
			if (cloudSafeEntity.isOption(CloudSafeOptions.PWD) || cloudSafeEntity.isOption(CloudSafeOptions.FPD)) {
				if (password == null || password.length == 0) {
					throw new DcemException(DcemErrorCodes.PASSWORD_MISSING, cloudSafeEntity.getName());
				}
				inputStream = SecureServerUtils.getBufferCipherInputStream(password, true, cloudSafeEntity.getSalt(), inputStream, cloudSafeEntity.isGcm());
			} else if (cloudSafeEntity.isOption(CloudSafeOptions.ENC)) {
				inputStream = DbEncryption.getBlockCipherInputStream(false, cloudSafeEntity.getSalt(), inputStream, cloudSafeEntity.isGcm());
				if (cloudSafeEntity.isGcm() == false) {
					int seedLength = inputStream.read(new byte[4]);
					if (seedLength != 4) {
						throw new DcemException(DcemErrorCodes.CLOUD_SAFE_FILE_DECRYPTION, "Wrong Seed Length " + cloudSafeEntity.getName());
					}
				}
			}
			if (auditUser != null && cloudSafeEntity.getOwner() == CloudSafeOwner.USER && asModule.getModulePreferences().isEnableAuditUser() == true) {
				DcemAction dcemAction = new DcemAction(asCloudSafeSubject, DcemConstants.ACTION_VIEW);
				String shareUser = "";
				if (auditUser.getId() != cloudSafeEntity.getUser().getId()) {
					shareUser = AUDIT_SHARED_BY + cloudSafeEntity.getUser().getDisplayNameOrLoginId();
				}
				auditingLogic.addAudit(dcemAction, auditUser, "File: " + cloudSafeEntity.getName() + shareUser);
			}
			return inputStream;
		} catch (DcemException exp) {
			throw exp;
		} catch (NoSuchKeyException exp) {
			exp.printStackTrace();
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_NOT_FOUND, cloudSafeEntity.getName() );
		} catch (Exception exp) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_FILE_DECRYPTION, cloudSafeEntity.getName());
		}
	}

	public List<Integer> getCloudSafeFromIds(List<DevicesUserDto> devicesUserDtos, String key) throws DcemException {
		TypedQuery<Integer> query = em.createNamedQuery(CloudSafeEntity.GET_DEVICES_CLOUDDATA_IN, Integer.class);
		query.setParameter(1, key);
		List<DeviceEntity> devices = new ArrayList<>(devicesUserDtos.size());
		DeviceEntity dummyDevice;
		for (DevicesUserDto devicesUserDto : devicesUserDtos) {
			dummyDevice = new DeviceEntity();
			dummyDevice.setId(devicesUserDto.getId());
			devices.add(dummyDevice);
		}
		query.setParameter(2, devices);
		return query.getResultList();
	}

	/**
	 * @param fileName
	 * @param dcemUser
	 * @return
	 * @throws DcemException
	 */
	public CloudSafeEntity getOwnedOrSharedCloudSafeFromPath(DcemUser dcemUser, long cloudSafeFileId, String path) throws DcemException {
		CloudSafeEntity cloudSafeEntity = null;
		if (cloudSafeFileId > 0) {
			cloudSafeEntity = getCloudSafeNotRecycled((int) cloudSafeFileId);
			if (cloudSafeEntity == null) {
				throw new DcemException(DcemErrorCodes.CLOUD_SAFE_NOT_FOUND, "File ID: " + cloudSafeFileId);
			}
		}
		int ind = path.lastIndexOf(AsConstants.SHARE_BY_SEPERATOR);
		if (ind != -1) { // shared
			if (cloudSafeEntity == null) {
				String username = path.substring(0, ind);
				String filename = path.substring(ind + 1);
				DcemUser shareUSer = userLogic.getDistinctUser(username);
				cloudSafeEntity = getValidatedCloudSafe(shareUSer, filename);
			}
			CloudSafeShareEntity shareEntity = getCloudSafeShareFileByParentId(dcemUser, cloudSafeEntity);
			cloudSafeEntity.setWriteAccess(shareEntity.isWriteAccess());
			cloudSafeEntity.setRestrictDownload(shareEntity.isRestrictDownload());
		} else { // owned
			if (cloudSafeEntity == null) {
				cloudSafeEntity = getValidatedCloudSafe(dcemUser, path);
			}
			cloudSafeEntity.setWriteAccess(true);
			cloudSafeEntity.setRestrictDownload(false);
		}
		return cloudSafeEntity;
	}

	private CloudSafeEntity getValidatedCloudSafe(DcemUser dcemUser, String path) throws DcemException {
		CloudSafeEntity cloudSafeEntity;
		try {
			cloudSafeEntity = getCloudSafeFromPath(CloudSafeOwner.USER, path, dcemUser, getRootDevice());
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_NOT_FOUND, "Path: " + path);
		}
		if (cloudSafeEntity.isRecycled()) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_NOT_FOUND, "Path: " + path);
		}
		if (cloudSafeEntity.getDiscardAfter() != null && cloudSafeEntity.getDiscardAfter().isBefore(LocalDateTime.now())) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_NOT_FOUND, "file expired: " + path);
		}
		return cloudSafeEntity;
	}

	private CloudSafeShareEntity getCloudSafeShareFileByParentId(DcemUser dcemUser, CloudSafeEntity cloudSafeEntity) throws DcemException {
		List<DcemGroup> groups = groupLogic.getAllUserGroups(dcemUser);
		TypedQuery<CloudSafeShareEntity> query = em.createNamedQuery(CloudSafeShareEntity.GET_USER_SHARE_FILES_BY_PARENT_ID, CloudSafeShareEntity.class);
		query.setParameter(1, dcemUser);
		query.setParameter(2, groups.size() > 0 ? groups : null);
		query.setParameter(3, cloudSafeEntity);
		query.setParameter(4, LocalDateTime.now());
		List<CloudSafeShareEntity> list = query.getResultList();
		CloudSafeShareEntity cloudSafeShareEntity = null;

		// Is this required at all ???? Need to check this for other Databases Is
		for (CloudSafeShareEntity cloudSafeShareEntity2 : list) {
			cloudSafeShareEntity = cloudSafeShareEntity2;
			if (cloudSafeShareEntity2.getUser() != null) {
				break;
			}
		}
		return cloudSafeShareEntity;
	}

	public CloudSafeEntity getUserdocument(DcemUser dcemUser, String name, CloudSafeEntity parent) throws DcemException {
		TypedQuery<CloudSafeEntity> query = em.createNamedQuery(CloudSafeEntity.GET_USER_CLOUDDATA, CloudSafeEntity.class);
		query.setParameter(1, name);
		query.setParameter(2, dcemUser);
		query.setParameter(3, parent.getId());
		query.setParameter(4, false);
		try {
			return query.getSingleResult();
		} catch (NoResultException exp) {
			return null;
		}
	}

	/**
	 * @param owner
	 * @param key
	 * @param user
	 * @param device
	 * @return
	 */
	public CloudSafeEntity getCloudSafe(CloudSafeOwner owner, String key, DcemUser user, DeviceEntity device, Integer parentId, DcemGroup dcemGroup,
			boolean recycled) throws DcemException {
		TypedQuery<CloudSafeEntity> query = null;
		if (parentId == null) {
			parentId = getCloudSafeRoot().getId();
		}
		switch (owner) {
		case GLOBAL:
			query = em.createNamedQuery(CloudSafeEntity.GET_GLOBAL_CLOUDDATA, CloudSafeEntity.class);
			query.setParameter(1, key);
			break;
		case USER:
			query = em.createNamedQuery(CloudSafeEntity.GET_USER_CLOUDDATA, CloudSafeEntity.class);
			query.setParameter(1, key);
			query.setParameter(2, user);
			query.setParameter(3, parentId);
			query.setParameter(4, recycled);
			break;
		case DEVICE:
			query = em.createNamedQuery(CloudSafeEntity.GET_DEVICE_CLOUDDATA, CloudSafeEntity.class);
			query.setParameter(1, key);
			query.setParameter(2, device);
			break;
		case GROUP:
			query = em.createNamedQuery(CloudSafeEntity.GET_GROUP_CLOUDDATA, CloudSafeEntity.class);
			query.setParameter(1, key);
			query.setParameter(2, dcemGroup);
			query.setParameter(3, parentId);
			query.setParameter(4, recycled);
			break;
		default:
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_NOT_FOUND, key);
		}
		try {
			CloudSafeEntity cloudSafeEntity = query.getSingleResult();
			return cloudSafeEntity;
		} catch (NoResultException exp) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_NOT_FOUND, key);
		}
	}

	public CloudSafeEntity getCloudSafe(CloudSafeOwner owner, String key, DcemUser user, DeviceEntity device, Integer parentId, DcemGroup dcemGroup)
			throws DcemException {
		return getCloudSafe(owner, key, user, device, parentId, dcemGroup, false);
	}

	public CloudSafeEntity getUserCloudSafe(String name, DcemUser user, int parentId) throws DcemException {
		TypedQuery<CloudSafeEntity> query = em.createNamedQuery(CloudSafeEntity.GET_USER_CLOUDDATA, CloudSafeEntity.class);
		query.setParameter(1, name);
		query.setParameter(2, user);
		query.setParameter(3, parentId);
		try {
			CloudSafeEntity cloudSafeEntity = query.getSingleResult();
			return cloudSafeEntity;
		} catch (NoResultException exp) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_NOT_FOUND, name);
		}
	}

	public List<CloudSafeEntity> getAllUserCloudSafe(DcemUser dcemUser, List<DcemGroup> groups) {
		if (groups != null && groups.isEmpty() == false) {
			TypedQuery<CloudSafeEntity> query = em.createNamedQuery(CloudSafeEntity.GET_ALL_USER_CLOUDSAFE_WITH_GROUP, CloudSafeEntity.class);
			query.setParameter(1, dcemUser);
			query.setParameter(2, groups);
			return query.getResultList();
		}
		TypedQuery<CloudSafeEntity> query = em.createNamedQuery(CloudSafeEntity.GET_ALL_USER_CLOUDSAFE, CloudSafeEntity.class);
		query.setParameter(1, dcemUser);
		return query.getResultList();
	}

	public List<CloudSafeEntity> getUserCloudSafeByName(DcemUser dcemUser) {
		TypedQuery<CloudSafeEntity> query = em.createNamedQuery(CloudSafeEntity.GET_USER_CLOUDSAFE_BY_NAME, CloudSafeEntity.class);
		query.setParameter(1, dcemUser);
		query.setParameter(2, getCloudSafeRoot());
		return query.getResultList();
	}

	public List<CloudSafeEntity> getCloudSafeEntitiesByIds(List<Integer> ids) throws DcemException {
		if (ids == null || ids.isEmpty()) {
			return new ArrayList<>();
		}
		TypedQuery<CloudSafeEntity> query = em.createNamedQuery(CloudSafeEntity.GET_BY_IDS, CloudSafeEntity.class);
		query.setParameter("ids", ids);
		return query.getResultList();
	}

	@DcemTransactional
	public CloudSafeEntity setCloudSafeByteArray(CloudSafeEntity cloudSafeEntity, char[] password, byte[] content, DcemUser loggedInUser,
			CloudSafeEntity originalDbCloudSafeEntity) throws DcemException {
		return setCloudSafeStream(cloudSafeEntity, password, new ByteArrayInputStream(content), content.length, loggedInUser, originalDbCloudSafeEntity, null);
	}

	public SortedSet<CloudSafeTagEntity> getTagsSafely(CloudSafeEntity entity) {
		if (Persistence.getPersistenceUtil().isLoaded(entity, CloudSafeEntity_.TAGS) == true) {
			return entity.getTags();
		}
		Query query = em.createNamedQuery(CloudSafeEntity.GET_ALL_TAGS);
		query.setParameter(1, entity.getId());
		List<CloudSafeTagEntity> list = query.getResultList();
		SortedSet<CloudSafeTagEntity> sortedSet = new TreeSet<>(list);
		entity.setTags(sortedSet);
		return sortedSet;
	}

	/**
	 * @param cloudSafeEntity
	 * @param password
	 * @param inputStream
	 * @param length
	 * @param loggedInUser
	 * @param originalDbCloudSafeEntity
	 * @return
	 * @throws DcemException
	 */
	private CloudSafeEntity setCloudSafeStream(CloudSafeEntity cloudSafeEntity, char[] password, InputStream inputStream, int length, DcemUser loggedInUser,
			CloudSafeEntity originalDbCloudSafeEntity, String ocrText) throws DcemException {

		SortedSet<CloudSafeTagEntity> tags = new TreeSet<CloudSafeTagEntity>();
		if (Persistence.getPersistenceUtil().isLoaded(cloudSafeEntity, "tags") == true) {
			if (cloudSafeEntity.getTags() != null) {
				for (CloudSafeTagEntity cloudSafeTagEntity : cloudSafeEntity.getTags()) {
					tags.add(em.find(CloudSafeTagEntity.class, cloudSafeTagEntity.getId())); // attach tags
				}
				cloudSafeEntity.setTags(tags);
			}
		}
		if (cloudSafeEntity.getSalt() == null) {
			cloudSafeEntity.setSalt(RandomUtils.getRandom(16));
		}
		if (length >= 0) {
			cloudSafeEntity.setLength(length);
		}
		CloudSafeEntity dbCloudSafeEntity = updateCloudSafeEntity(cloudSafeEntity, loggedInUser, true, originalDbCloudSafeEntity);
		long delta = 0;
		if (length >= 0) {
			delta = validateCloudSafeContentChange(dbCloudSafeEntity, length);
			dbCloudSafeEntity.setLength(length);
		}
		cloudSafeEntity.setId(dbCloudSafeEntity.getId());
		if (cloudSafeEntity.isFolder() == false) {
			if (length >= 0) {
				InputStream encryptedStream = getEncryptStream(dbCloudSafeEntity, inputStream, password);
				long start = System.currentTimeMillis();
				cloudSafeContentI.writeContentOutput(em, dbCloudSafeEntity, encryptedStream);
				logger.debug("Write to Storage for Length " + length + " took: " + (System.currentTimeMillis() - start));
				if (dbCloudSafeEntity.getOwner() == CloudSafeOwner.USER) {
					updateCloudSafeUsage(dbCloudSafeEntity.getUser().getId(), delta);
				}
			}
//			if (cloudSafeStorageType == CloudSafeStorageType.AwsS3) {
//				if (ocrText != null) {
//					byte[] ocrData = ocrText.getBytes(StandardCharsets.UTF_8);
//					InputStream ocrStream = new ByteArrayInputStream(ocrData);
//					InputStream encryptedStream = getEncryptStream(dbCloudSafeEntity, ocrStream, password);
//					dbCloudSafeEntity.setTextLength((long) ocrData.length);
//					cloudSafeContentI.writeS3Data(dbCloudSafeEntity.getId(), OCR_TEXT, encryptedStream, ocrData.length + 16);
//				}
//			}
			if (loggedInUser != null && asModule.getPreferences().isEnableAuditUser() == true && cloudSafeEntity.getOwner() != CloudSafeOwner.DEVICE) {
				DcemAction dcemAction = new DcemAction(asCloudSafeSubject, DcemConstants.ACTION_EDIT);
				String passwordProtected = "";
				if (password != null) {
					passwordProtected = ", Password Protected";
				}
				String shareUser = "";
				if (loggedInUser.getId() != cloudSafeEntity.getUser().getId()) {
					shareUser = AUDIT_SHARED_BY + cloudSafeEntity.getUser().getDisplayNameOrLoginId();
				}
				auditingLogic.addAudit(dcemAction, loggedInUser, "File: " + cloudSafeEntity.getName() + passwordProtected + shareUser);
			}
		}
		return dbCloudSafeEntity;
	}

	private InputStream getEncryptStream(CloudSafeEntity dbCloudSafeEntity, InputStream orgInputStream, char[] password) throws DcemException {
		if (dbCloudSafeEntity.isOption(CloudSafeOptions.PWD) || (dbCloudSafeEntity.isOption(CloudSafeOptions.FPD))) {
			if (password == null || password.length == 0) {
				throw new DcemException(DcemErrorCodes.PASSWORD_MISSING, dbCloudSafeEntity.getName());
			}
			return SecureServerUtils.getBufferCipherInputStream(password, false, dbCloudSafeEntity.getSalt(), orgInputStream, dbCloudSafeEntity.isGcm());
		} else if (dbCloudSafeEntity.isOption(CloudSafeOptions.ENC)) {
			try {
				return DbEncryption.getBlockCipherInputStream(true, dbCloudSafeEntity.getSalt(), orgInputStream, dbCloudSafeEntity.isGcm());
			} catch (Exception e) {
				throw new DcemException(DcemErrorCodes.CLOUD_SAFE_FILE_DECRYPTION, dbCloudSafeEntity.getName());
			}
		} else {
			return orgInputStream;
		}
	}

	private CloudSafeEntity updateCloudSafeEntity(CloudSafeEntity cloudSafeEntity, DcemUser loggedInUser, boolean allowRecycled,
			CloudSafeEntity originalDbEntity) throws DcemException {
		if (StringUtils.isValidFileName(cloudSafeEntity.getName()) == false) {
			throw new DcemException(DcemErrorCodes.FILE_NAME_WITH_SPECIAL_CHARACTERS, cloudSafeEntity.getName());
		}
		if (originalDbEntity == null) {
			if (cloudSafeEntity.getId() != null && cloudSafeEntity.getId() > 0) {
				if (allowRecycled == false) {
					originalDbEntity = getCloudSafeNotRecycled(cloudSafeEntity.getId());
				} else {
					originalDbEntity = em.find(CloudSafeEntity.class, cloudSafeEntity.getId());
				}
				if (originalDbEntity == null) {
					throw new DcemException(DcemErrorCodes.CLOUD_SAFE_NOT_FOUND, cloudSafeEntity.getName());
				}
			}
		}
		try {
			if (originalDbEntity == null) {
				// can only be user owner for backward compatibility
				originalDbEntity = getCloudSafe(cloudSafeEntity.getOwner(), cloudSafeEntity.getName(), cloudSafeEntity.getUser(), cloudSafeEntity.getDevice(),
						cloudSafeEntity.getParent() == null ? getCloudSafeRoot().getId() : cloudSafeEntity.getParent().getId(), cloudSafeEntity.getGroup());
			}
			if (cloudSafeEntity.getLastModified() != null) {
				// Check synchronization !!!
				LocalDateTime localDateTime = cloudSafeEntity.getLastModified().plusSeconds(1);
				// logger.debug("UPDATE CloudSafe File" + cloudSafeEntity.getName() + " New Time: " + localDateTime + " Original:"
				// + originalDbEntity.getLastModified());
				if (localDateTime.isBefore(originalDbEntity.getLastModified())) {
					// logger.debug("CLOUDDATA_OUT_OF_DATE" + cloudSafeEntity.getName() + " New Time: " + localDateTime + " Original:"
					// + originalDbEntity.getLastModified());
					throw new DcemException(DcemErrorCodes.CLOUDDATA_OUT_OF_DATE, originalDbEntity.toString());
				}
			} else {
				logger.debug("UPDATE CloudSafe File lastModified is NULL");
			}
			cloudSafeEntity.setLastModifiedUser(loggedInUser);
			CloudSafeThumbnailEntity thumbnailEntity = originalDbEntity.getThumbnailEntity();
			
			originalDbEntity.copyEntity(cloudSafeEntity);
			if (thumbnailEntity != null) {
				if (thumbnailEntity.getId() == null) {
					thumbnailEntity.setId(originalDbEntity.getId());
					thumbnailEntity.setCloudSafeEntity(originalDbEntity);
					em.persist(thumbnailEntity);
				} else {
					originalDbEntity.getThumbnailEntity().setThumbnail(cloudSafeEntity.getThumbnail());
					// thumbnailEntity.setCloudSafeEntity(originalDbEntity);
					em.merge(originalDbEntity.getThumbnailEntity());
				}
			}
			return originalDbEntity;
		} catch (DcemException e) {
			if (e.getErrorCode() == DcemErrorCodes.CLOUD_SAFE_NOT_FOUND) {
				// CREATE NEW Entityadd
				if (cloudSafeEntity.getOwner() == CloudSafeOwner.USER) {
					validateNewCloudSafeEntity(cloudSafeEntity);
				}
				if (cloudSafeEntity.getDevice() == null) {
					cloudSafeEntity.setDevice(getRootDevice());
				}
				if (cloudSafeEntity.getParent() == null) {
					cloudSafeEntity.setParent(getCloudSafeRoot());
				}
				if (cloudSafeEntity.getGroup() == null) {
					try {
						cloudSafeEntity.setGroup(groupLogic.getRootGroup());
					} catch (Exception ex) {
						throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_GROUP, "setCloudSafe", ex);
					}
				}
				cloudSafeEntity.setGcm(true);
				cloudSafeEntity.setLastModified(LocalDateTime.now());
				cloudSafeEntity.setLastModifiedUser(loggedInUser);
				em.persist(cloudSafeEntity);
				cloudSafeEntity.setNewEntity(true);
				return cloudSafeEntity;
			} else {
				throw e;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, "setCloudSafe", e);
		}
	}

	// TODO is this neccessary??
	public void verifyCloudSafe(CloudSafeEntity cloudSafe) throws Exception {
		if (cloudSafe.getOwner() == null) {
			throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_OWNER, null);
		}
		if (cloudSafe.getOwner() != CloudSafeOwner.GLOBAL) {
			if (cloudSafe.getUser() == null) {
				if (cloudSafe.getLoginId() == null) {
					throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_USER, null);
				}
				DcemUser user = userLogic.getDistinctUser(cloudSafe.getLoginId());
				if (user == null) {
					throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_USER, null);
				}
				cloudSafe.setUser(user);
			}
			if (cloudSafe.getOwner() == CloudSafeOwner.DEVICE) {
				if (cloudSafe.getDevice() == null) {
					if (cloudSafe.getDeviceName() == null) {
						throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_DEVICE, null);
					}
					DeviceEntity device = deviceLogic.getDeviceByName(cloudSafe.getUser(), cloudSafe.getDeviceName(), DeviceState.Enabled);
					if (device == null) {
						throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_DEVICE, null);
					}
					cloudSafe.setDevice(device);
				}
			}
		}
		String name = cloudSafe.getName();
		if (name == null || name.isEmpty()) {
			throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_NAME, name);
		}
	}

	@DcemTransactional
	public void deleteExpiredCloudSafe() throws Exception {
		deleteExpiredCloudShare();
		TypedQuery<CloudSafeEntity> query = em.createNamedQuery(CloudSafeEntity.GET_EXPIRED_DATA, CloudSafeEntity.class);
		query.setParameter(1, LocalDateTime.now().minusDays(1));
		List<CloudSafeEntity> list = query.getResultList();
		deleteFiles(list, null);
	}

	private void deleteExpiredCloudShare() {
		TypedQuery<CloudSafeShareEntity> query = em.createNamedQuery(CloudSafeShareEntity.GET_SHARE_DISCARDED, CloudSafeShareEntity.class);
		query.setParameter(1, LocalDateTime.now().minusDays(1));
		List<CloudSafeShareEntity> list = query.getResultList();
		for (CloudSafeShareEntity entity : list) {
			em.remove(entity);
		}
	}
			
	/**
	 * This will NOT delete the contents
	 * 
	 * @param cloudSafeEntity
	 * @throws DcemException
	 */
	private void deleteCloudSafeFile(int id) throws DcemException {
		try {
			DmModuleApi dmModuleApi = (DmModuleApi) CdiUtils.getReference(AsConstants.DM_MODULE_API_IMPL_BEAN);
			dmModuleApi.deleteWorkflowForDocument(id);
		} catch (Exception e) {
			logger.info(e);
		}		
		deleteCloudSafeThumbnail (id);
		Query query = em.createNamedQuery(CloudSafeShareEntity.DELETE_SHARE_BY_CLOUD_DATA);
		query.setParameter(1, id);
		query.executeUpdate();
		query = em.createNamedQuery(CloudSafeEntity.DELETE_CLOUD_SAFE_BY_ID);
		query.setParameter(1, id);
		query.executeUpdate();
	}
	
	

	private void deleteCloudSafeThumbnail(int id) throws DcemException {
		Query query = em.createNamedQuery(CloudSafeThumbnailEntity.DELETE_CLOUD_SAFE_THUMBNAIL_BY_ID);
		query.setParameter(1, id);
		query.executeUpdate();
	}
	
	public void deleteCloudSafeFileByOwnerGroup(DcemGroup ownerGroup) throws DcemException {
		Query query = em.createNamedQuery(CloudSafeEntity.DELETE_CLOUD_SAFE_BY_OWNER_GROUP);
		query.setParameter(1, ownerGroup.getId());
		query.executeUpdate();
	}

	@DcemTransactional
	public void deleteAllUserRelatedData(DcemUser dcemUser) throws DcemException {
		Query queryUpdateLastModify = em.createNamedQuery(CloudSafeEntity.UPDATE_LAST_MODIFY_STATE_BY_USER);
		queryUpdateLastModify.setParameter(1, dcemUser);
		queryUpdateLastModify.executeUpdate();

		Query queryDelete = em.createNamedQuery(CloudSafeShareEntity.DELETE_SHARE_BY_USER);
		queryDelete.setParameter(1, dcemUser);
		queryDelete.executeUpdate();

		CloudSafeEntity rootEntity = getCloudSafeRoot();
		CloudSafeDto rootDto = new CloudSafeDto(rootEntity);
		List<CloudSafeDto> cloudSafeToDeleteList = deleteSubdirectories(rootDto, new ArrayList<CloudSafeDto>(), dcemUser);
		deleteCloudSafeFilesContent(cloudSafeToDeleteList);
		CloudSafeLimitEntity cloudSafeLimitEntity = getCloudSafeLimitEntity(dcemUser.getId());
		if (cloudSafeLimitEntity != null) {
			em.remove(cloudSafeLimitEntity); // delete limits set to the user
		}
	}

	@DcemTransactional
	public void deleteDeviceData(DeviceEntity asDevice) {
		TypedQuery<CloudSafeEntity> query = em.createNamedQuery(CloudSafeEntity.GET_DEVICE_LIST, CloudSafeEntity.class);
		query.setParameter(1, asDevice);
		List<CloudSafeEntity> list = query.getResultList();
		for (CloudSafeEntity entity : list) {
			cloudSafeContentI.delete(em, entity.getId());
			em.remove(entity);
		}
	}

	public CloudSafeEntity getCloudSafe(Integer id) {
		return em.find(CloudSafeEntity.class, id);
	}

	private CloudSafeEntity getCloudSafeNotRecycled (Integer id) {
		CloudSafeEntity cloudSafeEntity = em.find(CloudSafeEntity.class, id);
		if (cloudSafeEntity == null || cloudSafeEntity.isRecycled()) {
			return null;
		}
		return cloudSafeEntity;
	}

	// private CloudSafeShareEntity getCloudShareById(CloudSafeEntity
	// cloudSafeEntity, DcemUser user, DcemGroup group) throws DcemException {
	// List<DcemGroup> groups;
	// if (group == null) {
	// groups = groupLogic.getAllUserGroups(user);
	// } else {
	// groups = new ArrayList<DcemGroup>(1);
	// groups.add(group);
	// }
	// TypedQuery<CloudSafeShareEntity> query =
	// em.createNamedQuery(CloudSafeShareEntity.GET_SHARE_BY_ID,
	// CloudSafeShareEntity.class);
	// query.setParameter(1, cloudSafeEntity);
	// query.setParameter(2, user);
	// query.setParameter(3, groups.size() > 0 ? groups : null);
	// try {
	// return query.getSingleResult();
	// } catch (Exception e) {
	// return null;
	// }
	// }

	public CloudSafeShareEntity getCloudShareByShareId(int id) {
		TypedQuery<CloudSafeShareEntity> query = em.createNamedQuery(CloudSafeShareEntity.GET_SHARE_BY_SHARE_ID, CloudSafeShareEntity.class);
		query.setParameter(1, id);
		try {
			return query.getSingleResult();
		} catch (Exception e) {
			return null;
		}
	}

	private boolean isNullOrEmpty(String s) {
		return s == null || s.isEmpty();
	}

	@DcemTransactional
	public void addOrEditShareCloudSafeFile(CloudSafeShareEntity cloudSafeShareEntity, DcemUser user, DcemGroup dcemGroup) throws DcemException {
		if (cloudSafeShareEntity == null) {
			throw new DcemException(DcemErrorCodes.INVALID_PARAMETER, "AsApiShareCloudSafe");
		}
		if (cloudSafeShareEntity.getId() == null) {
			CloudSafeShareEntity entityFromDb = getCloudShare(cloudSafeShareEntity.getCloudSafe(), user, dcemGroup);
			if (entityFromDb != null) {
				entityFromDb.setWriteAccess(cloudSafeShareEntity.isWriteAccess());
				entityFromDb.setRestrictDownload(cloudSafeShareEntity.isRestrictDownload());
			} else {
				cloudSafeShareEntity.setGroup(dcemGroup);
				cloudSafeShareEntity.setUser(user);
				em.persist(cloudSafeShareEntity);
			}
		} else {
			cloudSafeShareEntity.setGroup(dcemGroup);
			cloudSafeShareEntity.setUser(user);
			em.merge(cloudSafeShareEntity);
		}
	}

	private CloudSafeShareEntity getCloudShare(CloudSafeEntity cloudSafeEntity, DcemUser user, DcemGroup group) throws DcemException {
		TypedQuery<CloudSafeShareEntity> query = em.createNamedQuery(CloudSafeShareEntity.GET_SHARE, CloudSafeShareEntity.class);
		query.setParameter(1, cloudSafeEntity);
		query.setParameter(2, user);
		query.setParameter(3, group);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@DcemTransactional
	public void removeShareCloudSafeFile(CloudSafeShareEntity cloudSafeShareEntity) throws DcemException {
		if (cloudSafeShareEntity == null) {
			throw new DcemException(DcemErrorCodes.INVALID_PARAMETER, "AsApiShareCloudSafe");
		}
		cloudSafeShareEntity = em.find(CloudSafeShareEntity.class, cloudSafeShareEntity.getId());
		em.remove(cloudSafeShareEntity);
	}

	public List<CloudSafeShareEntity> getUserCloudSafeShareEntities(DcemUser dcemUser, String nameFilter) throws DcemException {
		if (dcemUser == null) {
			throw new DcemException(DcemErrorCodes.INVALID_USERID, null);
		}
		List<DcemGroup> groups = groupLogic.getAllUserGroups(dcemUser);
		TypedQuery<CloudSafeShareEntity> query = em.createNamedQuery(CloudSafeShareEntity.GET_USER_SHARE_FILES, CloudSafeShareEntity.class);
		query.setParameter(1, dcemUser);
		query.setParameter(2, groups.size() > 0 ? groups : null);
		query.setParameter(3, isNullOrEmpty(nameFilter) ? "%" : nameFilter);
		query.setParameter(4, LocalDateTime.now());
		HashMap<Integer, CloudSafeShareEntity> sharedCloudSafeFilesMap = new HashMap<Integer, CloudSafeShareEntity>();
		List<CloudSafeShareEntity> list = query.getResultList();
		for (CloudSafeShareEntity cloudSafeShareEntity : list) {
			if (cloudSafeShareEntity.getUser() != null) {
				sharedCloudSafeFilesMap.put(cloudSafeShareEntity.getCloudSafe().getId(), cloudSafeShareEntity);
				continue;
			}
			// for the group
			if (sharedCloudSafeFilesMap.containsKey(cloudSafeShareEntity.getCloudSafe().getId()) == false) {
				sharedCloudSafeFilesMap.put(cloudSafeShareEntity.getCloudSafe().getId(), cloudSafeShareEntity);
			}
		}
		return new ArrayList<CloudSafeShareEntity>(sharedCloudSafeFilesMap.values());
	}

	public List<CloudSafeShareEntity> getCloudSafeShareEntities(DcemUser dcemUser, CloudSafeEntity cloudSafeEntity) throws DcemException {
		if (dcemUser == null) {
			throw new DcemException(DcemErrorCodes.INVALID_USERID, "");
		}
		List<DcemGroup> groups = groupLogic.getAllUserGroups(dcemUser);
		TypedQuery<CloudSafeShareEntity> query = em.createNamedQuery(CloudSafeShareEntity.GET_CLOUD_SHARE_FILES, CloudSafeShareEntity.class);
		query.setParameter(1, dcemUser);
		query.setParameter(2, groups.size() > 0 ? groups : null);
		query.setParameter(3, cloudSafeEntity);
		query.setParameter(4, LocalDateTime.now());
		return query.getResultList();
	}

	/**
	 * @param cloudSafeFile
	 * @return
	 * @throws DcemException
	 */
	public List<CloudSafeShareEntity> getSharedCloudSafeUsersAccess(AsApiCloudSafeFile cloudSafeFile) throws DcemException {
		CloudSafeEntity cloudSafeEntity = getCloudSafeEntity(cloudSafeFile);
		TypedQuery<CloudSafeShareEntity> query = em.createNamedQuery(CloudSafeShareEntity.GET_SHARE_ACCESS, CloudSafeShareEntity.class);
		query.setParameter(1, cloudSafeEntity);
		return query.getResultList();
	}

	public List<CloudSafeShareEntity> getSharedCloudSafeUsersAccess(CloudSafeEntity cloudSafeEntity) throws DcemException {
		TypedQuery<CloudSafeShareEntity> query = em.createNamedQuery(CloudSafeShareEntity.GET_SHARE_ACCESS, CloudSafeShareEntity.class);
		query.setParameter(1, cloudSafeEntity);
		return query.getResultList();
	}

	private CloudSafeEntity getCloudSafeEntity(AsApiCloudSafeFile cloudSafeFile) throws DcemException {
		CloudSafeEntity cloudSafeEntity = em.find(CloudSafeEntity.class, cloudSafeFile.getId());
		if (cloudSafeEntity == null) {
			throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_ID, cloudSafeFile.toString());
		}
		if (cloudSafeEntity.getName().equals(cloudSafeFile.getName()) == false) {
			throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_ID, cloudSafeFile.toString());
		}
		return cloudSafeEntity;
	}

	@DcemTransactional
	public void deleteUserShareData(DcemUser dcemUser) {
		TypedQuery<CloudSafeShareEntity> query = em.createNamedQuery(CloudSafeShareEntity.GET_SHARE_USER, CloudSafeShareEntity.class);
		query.setParameter(1, dcemUser); // TODO Is this normal?
		query.setParameter(2, dcemUser);

		List<CloudSafeShareEntity> list = query.getResultList();
		for (CloudSafeShareEntity cloudSafeShareEntity : list) {
			em.remove(cloudSafeShareEntity);
		}
		em.flush();
	}

	@DcemTransactional
	public void deleteShareGroupRelatedData(DcemGroup dcemGroup) {
		Query query = em.createNamedQuery(CloudSafeShareEntity.DELETE_SHARE_BY_GROUP);
		query.setParameter(1, dcemGroup);
		query.executeUpdate();
	}

	// public List<CloudSafeEntity> getCloudSafeAllFileList(int userId, String nameFilter, long modifiedFromEpoch, CloudSafeOwner owner, boolean withShareFiles)
	// throws DcemException {
	// DcemUser user = userLogic.getUser(userId);
	// if (user == null) {
	// throw new DcemException(DcemErrorCodes.USER_IS_NULL, "Cannot get User for Cloud Data Filenames.");
	// }
	// LocalDateTime modifiedFrom = DcemUtils.convertEpoch(modifiedFromEpoch);
	// String like = nameFilter == null || nameFilter.isEmpty() ? "%" : nameFilter;
	// TypedQuery<CloudSafeEntity> query = em.createNamedQuery(CloudSafeEntity.GET_OWNED_FILE_KEYS, CloudSafeEntity.class);
	// query.setParameter(1, like);
	// query.setParameter(2, LocalDateTime.now());
	// query.setParameter(3, user);
	// query.setParameter(4, owner);
	// query.setParameter(5, modifiedFrom);
	// List<CloudSafeEntity> cloudSafeEntities = query.getResultList();
	//
	// for (CloudSafeEntity cloudSafeEntity : cloudSafeEntities) {
	// String path = getFullPath(cloudSafeEntity.getId(), null);
	// cloudSafeEntity.setName(path);
	// }
	// fullPathCache.clear();
	// return cloudSafeEntities;
	// }

	public List<SdkCloudSafe> getCloudSafeFileList(int userId, String nameFilter, long modifiedFromEpoch, CloudSafeOwner owner, boolean withShareFiles,
			int libVersion) throws DcemException {
		DcemUser user = userLogic.getUser(userId);
		HashMap<Integer, CloudSafeNameDto> fullPathCache = new HashMap<Integer, CloudSafeNameDto>();
		if (user == null) {
			throw new DcemException(DcemErrorCodes.USER_IS_NULL, "Cannot get User for Cloud Data Filenames.");
		}
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime modifiedFrom = DcemUtils.convertEpoch(modifiedFromEpoch);
		String like = nameFilter == null || nameFilter.isEmpty() ? "%" : nameFilter;
		List<DcemGroup> allUsersGroups = groupLogic.getAllUserGroups(user);

		TypedQuery<CloudSafeEntity> query = em.createNamedQuery(CloudSafeEntity.GET_USER_FILE_LIST, CloudSafeEntity.class);
		query.setParameter(1, like);
		query.setParameter(2, now);
		query.setParameter(3, user);
		if (allUsersGroups == null || allUsersGroups.isEmpty() == true) {
			query.setParameter(4, null);
		} else {
			query.setParameter(4, allUsersGroups);
		}

		query.setParameter(5, modifiedFrom);
		query.setParameter(6, false); // isFolder
		List<CloudSafeEntity> cloudSafeEntities = query.getResultList();
		ArrayList<SdkCloudSafe> list = new ArrayList<>(cloudSafeEntities.size());
		for (CloudSafeEntity cloudSafeEntity : cloudSafeEntities) {
			String path = getFullPath(cloudSafeEntity.getId(), null, fullPathCache);
			SdkCloudSafeKey cloudSafeKey = new SdkCloudSafeKey(cloudSafeEntity.getOwner(), path);
			cloudSafeKey.setDbId(cloudSafeEntity.getId());

			if (libVersion < AsConstants.LIB_VERION_2) { // For backward compatible with old appVersion 2.5
				cloudSafeKey.setOwner(CloudSafeOwner.USER); // For backward compatible with old appVersion 2.5
			}
			if (cloudSafeEntity.getOwner() == CloudSafeOwner.GROUP) {
				cloudSafeKey.setGroupName(cloudSafeEntity.getGroup().getName());
				cloudSafeKey.setOwner(CloudSafeOwner.GROUP);
			}
			list.add(new SdkCloudSafe(cloudSafeKey, null, cloudSafeEntity.getOptions(), cloudSafeEntity.getDiscardAfterAsLong(),
					cloudSafeEntity.getLastModified() != null ? (cloudSafeEntity.getLastModified().toEpochSecond(ZoneOffset.UTC) * 1000) : 0, null,
					cloudSafeEntity.getLength(), null, true, false));
		}
		if (withShareFiles) {
			List<CloudSafeShareEntity> cloudSafeShareEntities = getUserCloudSafeShareEntities(user, like);
			boolean doubleFile;
			for (CloudSafeShareEntity cloudSafeShareEntity : cloudSafeShareEntities) {
				if (cloudSafeShareEntity.getCloudSafe().isFolder() == false) {
					CloudSafeEntity cloudSafeEntity = cloudSafeShareEntity.getCloudSafe();
					doubleFile = false;
					for (SdkCloudSafe sdkCloudSafe : list) {
						if (cloudSafeEntity.getId() == sdkCloudSafe.getUniqueKey().getDbId()) {
							doubleFile = true;
							break;
						}
					}
					if (doubleFile == true) {
						continue;
					}
					SdkCloudSafeKey cloudSafeKey;
					if (cloudSafeShareEntity.getCloudSafe().getOwner().equals(CloudSafeOwner.GROUP)) {
						cloudSafeKey = new SdkCloudSafeKey((libVersion < AsConstants.LIB_VERION_2 ? CloudSafeOwner.USER : CloudSafeOwner.GROUP),
								AsConstants.SHARE_BY_GROUP_START + cloudSafeShareEntity.getCloudSafe().getGroup() + AsConstants.SHARE_BY_GROUP_END
										+ AsConstants.SHARE_BY_SEPERATOR + (cloudSafeEntity.getParent().getId().equals(getCloudSafeRoot().getId()) ? ""
												: cloudSafeEntity.getParent().getId() + FOLDER_SEPERATOR)
										+ cloudSafeEntity.getName());
					} else {
						cloudSafeKey = new SdkCloudSafeKey(owner,
								cloudSafeShareEntity.getCloudSafe().getUser().getLoginId() + AsConstants.SHARE_BY_SEPERATOR
										+ (cloudSafeEntity.getParent().getId().equals(getCloudSafeRoot().getId()) ? ""
												: cloudSafeEntity.getParent().getId() + FOLDER_SEPERATOR)
										+ cloudSafeEntity.getName());
					}
					cloudSafeKey.setDbId(cloudSafeEntity.getId());
					// cloudSafeKey.setOwner(cloudSafeEntity.getOwner());
					list.add(new SdkCloudSafe(cloudSafeKey, null, cloudSafeEntity.getOptions(), cloudSafeEntity.getDiscardAfterAsLong(),
							cloudSafeEntity.getLastModified() != null ? (cloudSafeEntity.getLastModified().toEpochSecond(ZoneOffset.UTC) * 1000) : 0, null,
							cloudSafeEntity.getLength(), cloudSafeShareEntity.getUser() != null ? cloudSafeShareEntity.getUser().getLoginId() : null,
							cloudSafeShareEntity.isWriteAccess(), cloudSafeShareEntity.isRestrictDownload()));
				}
			}
		}
		return list;
	}

	public List<SdkCloudSafe> getCloudSafeSharedFileList(int userId, String nameFilter, long modifiedFromEpoch, CloudSafeOwner owner, int libVersion)
			throws DcemException {
		DcemUser user = userLogic.getUser(userId);
		if (user == null) {
			throw new DcemException(DcemErrorCodes.USER_IS_NULL, "Cannot get User for Cloud Data Filenames.");
		}

		String like = nameFilter == null || nameFilter.isEmpty() ? "%" : nameFilter;
		ArrayList<SdkCloudSafe> result = new ArrayList<SdkCloudSafe>();

		List<CloudSafeShareEntity> cloudSafeShareEntities = getUserCloudSafeShareEntities(user, like);
		boolean doubleFile;
		for (CloudSafeShareEntity cloudSafeShareEntity : cloudSafeShareEntities) {
			if (cloudSafeShareEntity.getCloudSafe().isFolder() == false) {
				CloudSafeEntity cloudSafeEntity = cloudSafeShareEntity.getCloudSafe();
				doubleFile = false;
				for (SdkCloudSafe sdkCloudSafe : result) {
					if (cloudSafeEntity.getId() == sdkCloudSafe.getUniqueKey().getDbId()) {
						doubleFile = true;
						break;
					}
				}
				if (doubleFile == true) {
					continue;
				}
				SdkCloudSafeKey cloudSafeKey;
				if (cloudSafeShareEntity.getCloudSafe().getOwner().equals(CloudSafeOwner.GROUP)) {
					cloudSafeKey = new SdkCloudSafeKey((libVersion < AsConstants.LIB_VERION_2 ? CloudSafeOwner.USER : CloudSafeOwner.GROUP),
							AsConstants.SHARE_BY_GROUP_START + cloudSafeShareEntity.getCloudSafe().getGroup() + AsConstants.SHARE_BY_GROUP_END
									+ AsConstants.SHARE_BY_SEPERATOR + (cloudSafeEntity.getParent().getId().equals(getCloudSafeRoot().getId()) ? ""
											: cloudSafeEntity.getParent().getId() + FOLDER_SEPERATOR)
									+ cloudSafeEntity.getName());
				} else {
					cloudSafeKey = new SdkCloudSafeKey(owner,
							cloudSafeShareEntity.getCloudSafe().getUser().getLoginId() + AsConstants.SHARE_BY_SEPERATOR
									+ (cloudSafeEntity.getParent().getId().equals(getCloudSafeRoot().getId()) ? ""
											: cloudSafeEntity.getParent().getId() + FOLDER_SEPERATOR)
									+ cloudSafeEntity.getName());
				}
				cloudSafeKey.setDbId(cloudSafeEntity.getId());
				// cloudSafeKey.setOwner(cloudSafeEntity.getOwner());
				result.add(new SdkCloudSafe(cloudSafeKey, null, cloudSafeEntity.getOptions(), cloudSafeEntity.getDiscardAfterAsLong(),
						cloudSafeEntity.getLastModified() != null ? (cloudSafeEntity.getLastModified().toEpochSecond(ZoneOffset.UTC) * 1000) : 0, null,
						cloudSafeEntity.getLength(), cloudSafeShareEntity.getUser() != null ? cloudSafeShareEntity.getUser().getLoginId() : null,
						cloudSafeShareEntity.isWriteAccess(), cloudSafeShareEntity.isRestrictDownload()));
			}
		}
		return result;
	}

	@DcemTransactional
	public void renameCloudSafe(CloudSafeEntity cloudSafeEntity, String newName, DcemUser loggedInUser, boolean allowRecycled) throws DcemException {
		try {
			if (cloudSafeEntity.getUser().getId().equals(loggedInUser.getId()) == false && cloudSafeEntity.getOwner().equals(CloudSafeOwner.GROUP) == false) {
				throw new DcemException(DcemErrorCodes.CLOUD_SAFE_CANNOT_RENAME_SHARED_FILE, cloudSafeEntity.getName());
			}
			cloudSafeEntity.setName(newName);
			updateCloudSafeEntity(cloudSafeEntity, loggedInUser, allowRecycled, null);
		} catch (ConstraintViolationException e) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_DUPLICATE_NAME, "Name already exists.");
		} catch (DcemException exp) {
			throw exp;
		} catch (Exception exp) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_RENAME_FAILED, "Failed to rename file.", exp);
		}
	}

	@DcemTransactional
	public List<CloudSafeDto> trashFiles(List<CloudSafeEntity> list, DcemUser loggedInUser) throws Exception {
		List<CloudSafeDto> allDeletedFiles = new ArrayList<CloudSafeDto>();
		for (CloudSafeEntity cloudSafeEntity : list) {
			if (loggedInUser != null && cloudSafeEntity.getOwner() == CloudSafeOwner.USER && asModule.getModulePreferences().isEnableAuditUser() == true
					&& cloudSafeEntity.isRecycled() == false) {
				DcemAction dcemAction = new DcemAction(asCloudSafeSubject, DcemConstants.ACTION_TRASH);
				String shareUser = "";
				if (loggedInUser.getId() != cloudSafeEntity.getUser().getId()) {
					shareUser = AUDIT_SHARED_BY + cloudSafeEntity.getUser().getDisplayNameOrLoginId();
				}
				auditingLogic.addAudit(dcemAction, loggedInUser, "File: " + cloudSafeEntity.getName() + shareUser);
			}
			allDeletedFiles.addAll(trashFile(cloudSafeEntity));
		}
		return allDeletedFiles;
	}

	@DcemTransactional
	public List<CloudSafeDto> deleteFiles(List<CloudSafeEntity> list, DcemUser loggedInUser) throws Exception {
		List<CloudSafeDto> allDeletedFiles = new ArrayList<CloudSafeDto>();
		for (CloudSafeEntity cloudSafeEntity : list) {
			if (loggedInUser != null && cloudSafeEntity.getOwner() == CloudSafeOwner.USER && asModule.getModulePreferences().isEnableAuditUser() == true
					&& cloudSafeEntity.isRecycled() == false) {
				DcemAction dcemAction = new DcemAction(asCloudSafeSubject, DcemConstants.ACTION_DELETE);
				String shareUser = "";
				if (loggedInUser.getId() != cloudSafeEntity.getUser().getId()) {
					shareUser = AUDIT_SHARED_BY + cloudSafeEntity.getUser().getDisplayNameOrLoginId();
				}
				auditingLogic.addAudit(dcemAction, loggedInUser, "File: " + cloudSafeEntity.getName() + shareUser);
			}
			allDeletedFiles.addAll(deleteFile(cloudSafeEntity));
		}
		return allDeletedFiles;
	}

	@DcemTransactional
	public List<CloudSafeDto> recoverCloudSafeFiles(List<CloudSafeEntity> filesToRecover) throws DcemException {
		List<CloudSafeDto> recoverdFiles = new ArrayList<CloudSafeDto>();
		for (CloudSafeEntity cloudSafeEntity : filesToRecover) {
			cloudSafeEntity.setDiscardAfter(null);
			cloudSafeEntity.setRecycled(false);
			CloudSafeDto cloudSafeFolder = new CloudSafeDto(cloudSafeEntity.getId(), cloudSafeEntity.isFolder());
			if (cloudSafeEntity.isFolder()) {
				recoverSubDirectories(cloudSafeFolder, recoverdFiles, cloudSafeEntity.getUser());
			}
			em.merge(cloudSafeEntity);
			recoverdFiles.add(new CloudSafeDto(cloudSafeEntity));
		}
		return recoverdFiles;
	}

	@DcemTransactional
	public void deleteCloudSafeFilesContent(List<CloudSafeDto> list) throws DcemException {
		for (CloudSafeDto cloudSafeDto : list) {
			cloudSafeContentI.delete(em, cloudSafeDto.getId());
		}
	}

	public boolean updateNameIfDoubleName(CloudSafeEntity cloudSafeEntity) {
		CloudSafeEntity cloudSafeEntityExists = null;
		if (cloudSafeEntity.getOwner() == CloudSafeOwner.USER) {
			cloudSafeEntityExists = getCloudSafeUserSingleResult(cloudSafeEntity.getName(), cloudSafeEntity.getParent().getId(), false,
					cloudSafeEntity.getUser().getId(), cloudSafeEntity.isRecycled());
		} else {
			cloudSafeEntityExists = getCloudSafeGroupSingleResult(cloudSafeEntity.getName(), cloudSafeEntity.getParent().getId(),
					cloudSafeEntity.getGroup().getId(), cloudSafeEntity.isRecycled());
			cloudSafeEntity.setUser(userLogic.getSuperAdmin());
		}
		if (cloudSafeEntityExists != null) {
			cloudSafeEntity.setName(cloudSafeEntity.getName() + LocalDateTime.now().format(DcemConstants.DATE_TIME_FORMATTER_EXIST));
			return true;

		} else {
			return false;
		}
	}

	/**
	 * @param cloudSafeEntity
	 * @return the realy DB trashed files
	 * @throws DcemException
	 */
	@DcemTransactional
	public List<CloudSafeDto> trashFile(CloudSafeEntity cloudSafeEntity) throws DcemException {
		// Recycle to bin
		cloudSafeEntity.setDiscardAfter(LocalDateTime.now().plusDays(30));
		cloudSafeEntity.setRecycled(true);
		CloudSafeDto cloudSafeDto = new CloudSafeDto(cloudSafeEntity.getId(), cloudSafeEntity.isFolder());
		List<CloudSafeDto> cloudSafeDtos = new ArrayList<CloudSafeDto>(0);
		if (cloudSafeEntity.isFolder()) {
			recycleSubDirectories(cloudSafeDto, cloudSafeDtos, cloudSafeEntity.getUser());
		}
		updateNameIfDoubleName(cloudSafeEntity);
		em.merge(cloudSafeEntity);
		cloudSafeDtos.add(cloudSafeDto);
		return cloudSafeDtos;

	}

	/**
	 * @param cloudSafeEntity
	 * @return the realy DB deleted files
	 * @throws DcemException
	 */
	@DcemTransactional
	public List<CloudSafeDto> deleteFile(CloudSafeEntity cloudSafeEntity) throws DcemException {
		if (cloudSafeEntity.isFolder()) {
			return deleteCloudSafeFolder(cloudSafeEntity);
		} else {
//			CloudSafeThumbnailEntity thumbnailEntity = cloudSafeEntity.getThumbnailEntity();
//			if (thumbnailEntity != null) {
//				deleteCloudSafeThumbnail((int) thumbnailEntity.getId());
//			}
			deleteCloudSafeFile(cloudSafeEntity.getId());
			CloudSafeLimitEntity cloudSafeLimitEntity = getCloudSafeLimitEntity(cloudSafeEntity.getUser().getId());
			cloudSafeLimitEntity.setUsed(cloudSafeLimitEntity.getUsed() - cloudSafeEntity.getLength());
			em.merge(cloudSafeLimitEntity);
			List<CloudSafeDto> list = new ArrayList<CloudSafeDto>(1);
			list.add(new CloudSafeDto(cloudSafeEntity.getId(), cloudSafeEntity.getTextLength().intValue()));
			return list;
		}
	}

	private void recycleSubDirectories(CloudSafeDto parentFolder, List<CloudSafeDto> toRecycleFiles, DcemUser dcemUser) throws DcemException {
		TypedQuery<CloudSafeDto> query = em.createNamedQuery(CloudSafeEntity.SELECT_CLOUD_SAFE_FOLDER_STRUCTURE, CloudSafeDto.class);
		query.setParameter(1, parentFolder.getId());
		query.setParameter(2, dcemUser);
		List<CloudSafeDto> children = query.getResultList();
		toRecycleFiles.addAll(children);
		for (CloudSafeDto cloudSafeDto : children) {
			if (cloudSafeDto.isFolder()) {
				recycleSubDirectories(cloudSafeDto, toRecycleFiles, dcemUser);
			}
			CloudSafeEntity toRecycleCloudSafe = getCloudSafe(cloudSafeDto.getId());
			toRecycleCloudSafe.setDiscardAfter(LocalDateTime.now().plusDays(30));
			toRecycleCloudSafe.setRecycled(true);
			em.merge(toRecycleCloudSafe);
		}
	}

	private void recoverSubDirectories(CloudSafeDto parentFolder, List<CloudSafeDto> toRecoverFiles, DcemUser dcemUser) throws DcemException {
		TypedQuery<CloudSafeDto> query = em.createNamedQuery(CloudSafeEntity.SELECT_CLOUD_SAFE_FOLDER_STRUCTURE, CloudSafeDto.class);
		query.setParameter(1, parentFolder.getId());
		query.setParameter(2, dcemUser);
		List<CloudSafeDto> children = query.getResultList();
		toRecoverFiles.addAll(children);
		for (CloudSafeDto cloudSafeDto : children) {
			if (cloudSafeDto.isFolder()) {
				recoverSubDirectories(cloudSafeDto, toRecoverFiles, dcemUser);
			}
			CloudSafeEntity toRecycleCloudSafe = getCloudSafe(cloudSafeDto.getId());
			toRecycleCloudSafe.setDiscardAfter(null);
			toRecycleCloudSafe.setRecycled(false);
			em.merge(toRecycleCloudSafe);
		}
	}

	private String getFullPath(int id, String path, HashMap<Integer, CloudSafeNameDto> fullPathCache) {
		CloudSafeNameDto cloudsafe;
		if (fullPathCache.containsKey(id) == false) {
			TypedQuery<CloudSafeNameDto> query = em.createNamedQuery(CloudSafeEntity.GET_CLOUDSAFE_BY_ID, CloudSafeNameDto.class);
			query.setParameter(1, id);
			cloudsafe = query.getSingleResult();
			fullPathCache.put(id, new CloudSafeNameDto(id, cloudsafe.getName(), cloudsafe.getParentId()));
		} else {
			cloudsafe = fullPathCache.get(id);
		}
		path = path == null ? cloudsafe.getName() : cloudsafe.getName() + FOLDER_SEPERATOR + path;
		if (getCloudSafeRoot().getId().equals(cloudsafe.getParentId()) == false && cloudsafe.getParentId() != null
				&& cloudsafe.getParentId() != cloudsafe.getId()) {
			return getFullPath(cloudsafe.getParentId(), path, fullPathCache);
		} else {
			return path;
		}
	}

	/**
	 * @param filterItems
	 * @param offset
	 * @param maxResults
	 * @return
	 * @throws DcemException
	 */
	public List<CloudSafeEntity> queryCloudSafeFiles(List<ApiFilterItem> filterItems, Integer offset, Integer maxResults) throws DcemException {

		JpaSelectProducer<CloudSafeEntity> jpaSelectProducer = new JpaSelectProducer<CloudSafeEntity>(em, CloudSafeEntity.class);
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

	public CloudSafeEntity getCloudSafeFromPath(CloudSafeOwner owner, String path, DcemUser user, DeviceEntity device) {

		String[] dirs = path.split(FOLDER_SEPERATOR);
		String parent = DcemConstants.CLOUD_SAFE_ROOT;
		if (dirs.length > 0) {
			parent = dirs[dirs.length - 1];
		}
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CloudSafeEntity> fileQuery = builder.createQuery(CloudSafeEntity.class);
		Root<CloudSafeEntity> fileRoot = fileQuery.from(CloudSafeEntity.class);
		fileQuery.select(fileRoot).where(builder.equal(fileRoot.get("name"), parent),
				user == null ? builder.isNull(fileRoot.get("user")) : builder.equal(fileRoot.get("user"), user), builder.equal(fileRoot.get("isFolder"), false),
				builder.equal(fileRoot.get("owner"), owner),
				device == null ? builder.equal(fileRoot.get("device"), getRootDevice()) : builder.equal(fileRoot.get("device"), device));

		if (dirs.length > 1) {
			AbstractQuery<?> lastQuery = fileQuery;
			Root<CloudSafeEntity> lastRoot = fileRoot;
			for (int i = dirs.length - 2; i >= 0; i--) {
				Subquery<Integer> folderQuery = lastQuery.subquery(Integer.class);
				Root<CloudSafeEntity> folderRoot = folderQuery.from(CloudSafeEntity.class);
				folderQuery.select(folderRoot.get("id")).where(builder.equal(folderRoot.get("name"), dirs[i]),
						user == null ? builder.isNull(fileRoot.get("user")) : builder.equal(folderRoot.get("user"), user),
						builder.equal(folderRoot.get("isFolder"), true), builder.equal(folderRoot.get("owner"), owner),
						device == null ? builder.equal(fileRoot.get("device"), getRootDevice()) : builder.equal(fileRoot.get("device"), device));

				if (i == 0) {
					folderQuery.where(folderQuery.getRestriction(), builder.equal(folderRoot.get("parent"), getCloudSafeRoot()));
				}
				lastQuery.where(lastQuery.getRestriction(), lastRoot.get("parent").in(folderQuery));
				lastRoot = folderRoot;
				lastQuery = folderQuery;
			}
		} else {
			fileQuery.where(fileQuery.getRestriction(), builder.equal(fileRoot.get("parent"), getCloudSafeRoot()));
		}
		TypedQuery<CloudSafeEntity> typedQuery = em.createQuery(fileQuery);
		return typedQuery.getSingleResult();
	}

	/**
	 * ONLY FOR WEBDAV
	 * 
	 * @param user
	 * @param path
	 * @return
	 */
	public CloudSafeEntity getParentFromPath(DcemUser user, String path) {
		if (path.equals(FOLDER_SEPERATOR)) {
			return getCloudSafeRoot();
		}
		if (path.startsWith(FOLDER_SEPERATOR)) {
			path = path.substring(1);
		}
		String[] dirs = path.split(FOLDER_SEPERATOR);
		String dirRoot = DcemConstants.CLOUD_SAFE_ROOT;
		if (dirs.length > 0) {
			dirRoot = dirs[dirs.length - 1];
		}
		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<CloudSafeEntity> fileQuery = builder.createQuery(CloudSafeEntity.class);
		Root<CloudSafeEntity> fileRoot = fileQuery.from(CloudSafeEntity.class);
		fileQuery.select(fileRoot).where(builder.equal(fileRoot.get(CloudSafeEntity_.user), user), builder.equal(fileRoot.get(CloudSafeEntity_.isFolder), true),
				builder.equal(fileRoot.get(CloudSafeEntity_.owner), CloudSafeOwner.USER), builder.equal(fileRoot.get(CloudSafeEntity_.name), dirRoot));

		if (dirs.length > 1) {
			AbstractQuery<?> lastQuery = fileQuery;
			Root<CloudSafeEntity> lastRoot = fileRoot;
			for (int i = dirs.length - 2; i >= 0; i--) {
				Subquery<Integer> folderQuery = lastQuery.subquery(Integer.class);
				Root<CloudSafeEntity> folderRoot = folderQuery.from(CloudSafeEntity.class);
				folderQuery.select(folderRoot.get("id")).where(builder.equal(folderRoot.get("name"), dirs[i]), builder.equal(folderRoot.get("user"), user),
						builder.equal(folderRoot.get("isFolder"), true), builder.equal(folderRoot.get("owner"), CloudSafeOwner.USER));

				if (i == 0) {
					folderQuery.where(folderQuery.getRestriction(), builder.equal(folderRoot.get("parent"), getCloudSafeRoot()));
				}

				lastQuery.where(lastQuery.getRestriction(), lastRoot.get("parent").in(folderQuery));
				lastRoot = folderRoot;
				lastQuery = folderQuery;
			}
		}
		// else {
		// fileQuery.where(fileQuery.getRestriction(),
		// builder.equal(fileRoot.get("parent"), getCloudSafeRoot()));
		// }

		TypedQuery<CloudSafeEntity> typedQuery = em.createQuery(fileQuery);
		return typedQuery.getSingleResult();
	}

	// Cloud Safe Billing

	public CloudSafeLimitEntity getCloudSafeLimitEntity(int userId) {
		return em.find(CloudSafeLimitEntity.class, userId);
	}

	public List<String> getLimitBreachingUsernames() {
		TypedQuery<String> query = em.createNamedQuery(CloudSafeLimitEntity.GET_LIMIT_REACHING_USERNAMES, String.class);
		return query.getResultList();
	}

	@DcemTransactional
	public void setCloudSafeLimits(List<Integer> userIds, long limit, LocalDateTime expiryDate, boolean passwordSafeEnabled) throws DcemException {

		validateCloudSafeLimits(userIds, limit, expiryDate);

		// update existing limits
		Query query = em.createNamedQuery(CloudSafeLimitEntity.SET_LIMIT_FOR_USERS);
		query.setParameter(1, limit);
		query.setParameter(2, expiryDate);
		query.setParameter(3, passwordSafeEnabled);
		query.setParameter(4, userIds.size() > 0 ? userIds : null);
		int updatedUserCount = query.executeUpdate();

		if (updatedUserCount < userIds.size()) { // create non existing limits
			TypedQuery<Integer> tQuery = em.createNamedQuery(CloudSafeLimitEntity.GET_EXISTING_USERS, Integer.class);
			tQuery.setParameter(1, userIds);
			List<Integer> existingUsers = tQuery.getResultList();
			List<Integer> newUserIds = userIds.stream().filter(i -> !existingUsers.contains(i)).collect(Collectors.toList());
			List<DcemUser> newUsers = userLogic.getUsers(newUserIds);
			for (DcemUser user : newUsers) {
				CloudSafeLimitEntity entity = new CloudSafeLimitEntity(user, limit, 0, expiryDate, passwordSafeEnabled);
				em.persist(entity);
			}
		}
	}

	private void validateCloudSafeLimits(List<Integer> userIds, long limit, LocalDateTime expiryDate) throws DcemException {

		if (userIds == null || userIds.isEmpty()) {
			throw new DcemException(DcemErrorCodes.USER_IS_NULL, "No user is defined");
		} else if (limit < 0) {
			throw new DcemException(DcemErrorCodes.INVALID_PARAMETER, "The limit cannot be a negative value.");
		} else if (expiryDate != null && expiryDate.isBefore(LocalDateTime.now())) {
			throw new DcemException(DcemErrorCodes.INVALID_PARAMETER, "The expiry date cannot be in the past.");
		}

		LicenceKeyContent licenceKeyContent = licenceLogic.getLicenceKeyContent();
		LocalDateTime licenceExpiryDate = licenceKeyContent.getLdtExpiresOn();
		if (expiryDate != null && expiryDate.isAfter(licenceExpiryDate)) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_LIMIT_EXCEEDS_GLOBAL,
					"The expiry date " + getStringFromDate(expiryDate) + " exceeds the licence expiry date of " + getStringFromDate(licenceExpiryDate));
		}

		long globalLimit = licenceKeyContent.getCloudSafeStoageMb() * (1024 * 1024);
		if (limit > globalLimit) {
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_LIMIT_EXCEEDS_GLOBAL,
					"The limit of " + DataUnit.getByteCountAsString(limit) + " exceeds the licence limit of " + DataUnit.getByteCountAsString(globalLimit));
		}
	}

	private void validateNewCloudSafeEntity(CloudSafeEntity cloudSafe) throws DcemException {
		DcemUser user = cloudSafe.getUser();
		// 1. Check User
		if (cloudSafe.getOwner() == CloudSafeOwner.USER && user == null) {
			throw new DcemException(DcemErrorCodes.USER_IS_NULL, "no user defined", null);
		}

		// 2. Check if PasswordSafe file, then check against Billing table
		if (cloudSafe.getName().endsWith(AsConstants.EXTENSION_PASSWORD_SAFE)) {
			if (!passwordSafeEnabled(user)) {
				throw new DcemException(DcemErrorCodes.PASSWORD_SAFE_NOT_ENABLED, "PasswordSafe is disabled for this user. Please contact your Administrator.");
			}
		}
	}

	private boolean passwordSafeEnabled(DcemUser user) {
		CloudSafeLimitEntity limitEntity = getCloudSafeLimitEntity(user.getId());
		return limitEntity != null ? limitEntity.isPasswordSafeEnabled() : getDefaultPasswordSafeEnabled();
	}

	private String getStringFromDate(LocalDateTime date) {
		return date.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM));
	}

	private long validateCloudSafeContentChange(CloudSafeEntity cloudSafeEntity, long newLength) throws DcemException {
		if (cloudSafeEntity.getOwner() == CloudSafeOwner.USER) {
			if (cloudSafeEntity.getUser() == null) {
				throw new DcemException(DcemErrorCodes.USER_IS_NULL, "No user specified for new CloudSafe content.");
			} else {
				return validateUserCloudSafeContentChange(cloudSafeEntity, newLength);
				// return newLength;
			}
		} else { // do not check limits if Global or Device
			return 0;
		}
	}

	private long validateUserCloudSafeContentChange(CloudSafeEntity cloudSafeEntity, long newLength) throws DcemException {

		LicenceKeyContent licenceKeyContent = licenceLogic.getLicenceKeyContent();
		// In Bytes
		long globalLimit = licenceKeyContent.getCloudSafeStoageMb() * (1024 * 1024);
		long globalUsed = getGlobalCloudSafeUsageTotal().get();
		LocalDateTime now = LocalDateTime.now();

		DcemUser user = cloudSafeEntity.getUser();
		CloudSafeLimitEntity limitEntity = getCloudSafeLimitEntity(user.getId());

		// 1. Check expiry dates
		// Date expiryDate = limitEntity != null ? limitEntity.getExpiryDate() : null;
		// if (expiryDate != null && now.after(expiryDate)) {
		// throw new DcemException(DcemErrorCodes.CLOUD_SAFE_USER_EXPIRY_DATE_REACHED,
		// "This user cannot use CloudSafe because the expiry date has passed: " +
		// getStringFromDate(expiryDate));
		// } else if (now.after(licenceKeyContent.getExpiresOn())) {
		// throw new DcemException(DcemErrorCodes.LICENCE_EXPIRED, "This user cannot use
		// CloudSafe because the licence has expired");
		// }

		// 2. Check against user's Cloud Safe limit
		long userLimit = limitEntity != null ? limitEntity.getLimit() : getDefaultUserLimit();
		TypedQuery<Long> query = em.createNamedQuery(CloudSafeEntity.GET_USER_TOTAL_EXCLUDING_ENTITY, Long.class);
		query.setParameter(1, user.getId());
		query.setParameter(2, now);
		query.setParameter(3, cloudSafeEntity.getId());
		long userUsedExcludingEntity;
		try {
			userUsedExcludingEntity = query.getSingleResult();
		} catch (NoResultException e) {
			userUsedExcludingEntity = 0;
		} catch (Exception e) {
			System.out.println("CloudSafeLogic.validateUserCloudSafeContentChange()");
			throw e;
		}
		long newUserTotal = userUsedExcludingEntity + newLength;
		long previousUserTotal = limitEntity != null ? limitEntity.getUsed() : 0;
		long delta = newUserTotal - previousUserTotal;
		if (newUserTotal >= userLimit) {
			String errorMessage = previousUserTotal > userLimit
					? "User's CloudSafe limit (" + DataUnit.getByteCountAsString(userLimit) + ") has already been reached."
					: "Tried to add " + DataUnit.getByteCountAsString(delta) + " in " + user.getLoginId() + "'s CloudSafe when only "
							+ DataUnit.getByteCountAsString(userLimit - previousUserTotal) + " are left.";
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_USER_LIMIT_REACHED, errorMessage);
		}

		// 3. Check against global Cloud Safe limit
		if (globalUsed + delta >= globalLimit) {
			String errorMessage = globalUsed > globalLimit
					? "The Licence's CloudSafe limit (" + DataUnit.getByteCountAsString(globalLimit) + ") has already been reached."
					: "Tried to add " + DataUnit.getByteCountAsString(delta) + " in CloudSafe are left in the licence.";
			throw new DcemException(DcemErrorCodes.CLOUD_SAFE_GLOBAL_LIMIT_REACHED, errorMessage);
		}
		return delta;
	}

	private void updateCloudSafeUsage(int userId, long delta) {
		CloudSafeLimitEntity entity = getCloudSafeLimitEntity(userId);
		if (entity == null) {
			long used = delta > 0 ? delta : 0;
			DcemUser user = userLogic.getUser(userId);
			entity = new CloudSafeLimitEntity(user, getDefaultUserLimit(), used, null, getDefaultPasswordSafeEnabled());
			em.persist(entity);
		} else {
			long used = entity.getUsed() + delta;
			used = used > 0 ? used : 0;
			entity.setUsed(used);
		}
	}

	public void updateCloudSafeGlobalUsage(long delta) {
		long globalUsed = getGlobalCloudSafeUsageTotal().get() + delta;
		globalUsed = globalUsed > 0 ? globalUsed : 0;
		getGlobalCloudSafeUsageTotal().getAndSet(globalUsed);
	}

	public boolean getDefaultPasswordSafeEnabled() {
		return asModule.getPreferences().isPasswordSafeEnabledByDefault();
	}

	public long getDefaultUserLimit() {
		return DataUnit.getByteCount(asModule.getPreferences().getCloudSafeDefaultLimit(), DataUnit.MEGABYTE);
	}

	public void synchroniseGlobalCloudSafeUsageTotal() {
		TypedQuery<Long> query = em.createNamedQuery(CloudSafeLimitEntity.GET_TOTAL_USED, Long.class);
		Long result;
		try {
			result = query.getSingleResult();
			if (result == null) {
				result = Long.valueOf(0);
			}
		} catch (Exception e) {
			result = Long.valueOf(0);
		}
		getGlobalCloudSafeUsageTotal().getAndSet(result.longValue());
	}

	public IAtomicLong getGlobalCloudSafeUsageTotal() {
		return asModule.getTenantData().getGlobalCloudSafeUsageTotal();
	}

	@DcemTransactional
	public List<CloudSafeEntity> saveMultipleFiles(List<CloudSafeUploadFile> uploadedFiles, DcemUser dcemUser) throws Exception {
		HashSet<String> hashSet = new HashSet<>();
		List<CloudSafeEntity> savedFiles = new ArrayList<>();
		for (CloudSafeUploadFile cloudSafeUploadedFile : uploadedFiles) {
			if (hashSet.contains(cloudSafeUploadedFile.fileName)) {
				continue;
			}
			if (cloudSafeUploadedFile.getCloudSafeEntity().getParent() == null) {
				cloudSafeUploadedFile.getCloudSafeEntity().setParent(getCloudSafeRoot());
			}
			CloudSafeEntity cloudSafeEntity = setCloudSafeStream(cloudSafeUploadedFile.getCloudSafeEntity(), (char[]) null,
					new FileInputStream(cloudSafeUploadedFile.file), (int) cloudSafeUploadedFile.file.length(), dcemUser, null, null); // no OCR
			hashSet.add(cloudSafeUploadedFile.fileName);
			savedFiles.add(cloudSafeEntity);
		}
		return savedFiles;
	}

	@DcemTransactional
	public List<CloudSafeEntity> saveMultipleFiles(List<DcemUploadFile> uploadedFiles, DcemUser dcemUser, String filePassword, LocalDateTime expiryDate,
			boolean passwordProtected, boolean encryptProtected, CloudSafeEntity parent, DcemUser lastModifiedUser, DcemGroup groupOwner,
			CloudSafeOwner cloudSafeOwner) throws Exception {
		HashSet<String> hashSet = new HashSet<>();
		List<CloudSafeEntity> savedFiles = new ArrayList<>();
		for (DcemUploadFile uploadedFile : uploadedFiles) {
			if (hashSet.contains(uploadedFile.fileName)) {
				continue;
			}
			if (parent == null) {
				parent = getCloudSafeRoot();
			}
			CloudSafeEntity cloudSafeEntity = new CloudSafeEntity();
			cloudSafeEntity.setName(uploadedFile.fileName);
			cloudSafeEntity.setInfo(uploadedFile.info);
			cloudSafeEntity.setDiscardAfter(expiryDate);
			switch (cloudSafeOwner) {
			case GLOBAL:
				cloudSafeEntity.setGroup(groupLogic.getRootGroup());
				cloudSafeEntity.setUser(userLogic.getSuperAdmin());
				break;
			case USER:
				cloudSafeEntity.setGroup(groupLogic.getRootGroup());
				cloudSafeEntity.setUser(dcemUser);
				break;
			case GROUP:
				cloudSafeEntity.setGroup(groupOwner);
				cloudSafeEntity.setUser(userLogic.getSuperAdmin());
				break;
			default:
				break;
			}
			cloudSafeEntity.setOwner(cloudSafeOwner);
			cloudSafeEntity.setParent(parent);
			cloudSafeEntity.setLastModifiedUser(lastModifiedUser);
			if (passwordProtected) {
				if (uploadedFile.fileName.endsWith(AsConstants.EXTENSION_PASSWORD_SAFE)) {
					throw new DcemException(DcemErrorCodes.PASSWORD_NOT_SUPPORTED_KDBX, uploadedFile.fileName);
				}
				cloudSafeEntity.setOptions(CloudSafeOptions.PWD.name());
			} else if (parent != null) {
				if (parent.isOption(CloudSafeOptions.PWD) || parent.isOption(CloudSafeOptions.FPD)) {
					cloudSafeEntity.setOptions(CloudSafeOptions.FPD.name());
				} else {
					cloudSafeEntity.setOptions(CloudSafeOptions.ENC.name());
				}
			} else {
				cloudSafeEntity.setOptions(CloudSafeOptions.ENC.name());
			}
			cloudSafeEntity = setCloudSafeStream(cloudSafeEntity, filePassword == null ? null : filePassword.toCharArray(),
					new FileInputStream(uploadedFile.file), (int) uploadedFile.file.length(), lastModifiedUser, null, null);
			hashSet.add(uploadedFile.fileName);
			savedFiles.add(cloudSafeEntity);
		}
		return savedFiles;
	}

	@DcemTransactional
	public CloudSafeEntity addDocument(CloudSafeEntity cloudSafeEntity, char[] password, DcemUser dcemUser, File file, String ocrText, boolean overwrite) throws Exception {
		CloudSafeEntity cloudSafeEntityExist = null;
		if (cloudSafeEntity.getId() == null) { // new 
			cloudSafeEntityExist = getUserdocument(cloudSafeEntity.getUser(), cloudSafeEntity.getName(), cloudSafeEntity.getParent());
			if (cloudSafeEntityExist != null) {
				if (overwrite == false) {
					cloudSafeEntity.setName(cloudSafeEntity.getName() + LocalDateTime.now().format(DcemConstants.DATE_TIME_FORMATTER_EXIST));
					cloudSafeEntityExist = null;
				} else {
					cloudSafeEntity.setId(cloudSafeEntityExist.getId());  // edit mode
				}
			}
		}		
		if (file == null) {
			return setCloudSafeStream(cloudSafeEntity, password, null, -1, dcemUser, cloudSafeEntityExist, ocrText);
		}
		return setCloudSafeStream(cloudSafeEntity, password, new FileInputStream(file), (int) file.length(), dcemUser, cloudSafeEntityExist, ocrText);
	}

	@DcemTransactional
	public void addCloudSafeFolder(CloudSafeEntity cloudSafeEntity) throws DcemException {
		try {
			if (cloudSafeEntity.getDevice() == null) {
				cloudSafeEntity.setDevice(getRootDevice());
			}
			em.persist(cloudSafeEntity);
		} catch (Exception e) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, cloudSafeEntity.toString(), e);
		}
	}

	public CloudSafeEntity createCloudSafeEntityFolder(CloudSafeEntity selectedFolder, DcemUser currentUser, String folderName, boolean passwordProtected,
			boolean encryptProtected, String folderPassword) throws DcemException {
		// if (selectedFolder == null) {
		// throw new DcemException(DcemErrorCodes.INVALID_PARAMETER, "selectedFolder
		// cannot be null");
		// }
		CloudSafeEntity cloudSafeEntity = new CloudSafeEntity();
		cloudSafeEntity.setOwner(CloudSafeOwner.USER);
		if (cloudSafeEntity.getGroup() == null) {
			try {
				cloudSafeEntity.setGroup(groupLogic.getRootGroup());
			} catch (Exception ex) {
				throw new DcemException(DcemErrorCodes.INVALID_CLOUDDATA_GROUP, "setCloudSafe", ex);
			}
		}
		cloudSafeEntity.setUser(currentUser);
		cloudSafeEntity.setName(folderName);
		cloudSafeEntity.setFolder(true);
		cloudSafeEntity.setParent(selectedFolder);
		cloudSafeEntity.setGcm(true);
		cloudSafeEntity.setLastModified(LocalDateTime.now().plusSeconds(1));
		cloudSafeEntity.setLastModifiedUser(currentUser);
		if (passwordProtected || folderPassword != null) {
			if (selectedFolder.getName().equals(DcemConstants.CLOUD_SAFE_ROOT) == false) {
				if (selectedFolder.isOption(CloudSafeOptions.PWD) || selectedFolder.isOption(CloudSafeOptions.FPD)) {
					cloudSafeEntity.setOptions(CloudSafeOptions.FPD.name());
					byte[] randomByte = RandomUtils.getRandom(8);
					byte[] content = Bytes.concat(randomByte, FOLDER_CONTENT_TO_ENCRYPT);
					InputStream is = new ByteArrayInputStream((content));
					cloudSafeEntity = setCloudSafeStream(cloudSafeEntity, folderPassword.toCharArray(), is, content.length, null, null, null);
				} else if (selectedFolder.isOption(CloudSafeOptions.ENC) && passwordProtected) {
					cloudSafeEntity.setOptions(CloudSafeOptions.PWD.name());
					byte[] randomByte = RandomUtils.getRandom(8);
					byte[] content = Bytes.concat(randomByte, FOLDER_CONTENT_TO_ENCRYPT);
					InputStream is = new ByteArrayInputStream((content));
					cloudSafeEntity = setCloudSafeStream(cloudSafeEntity, folderPassword.toCharArray(), is, content.length, null, null, null);
				}
			} else {
				cloudSafeEntity.setOptions(CloudSafeOptions.PWD.name());
				byte[] randomByte = RandomUtils.getRandom(8);
				byte[] content = Bytes.concat(randomByte, FOLDER_CONTENT_TO_ENCRYPT);
				InputStream is = new ByteArrayInputStream((content));
				cloudSafeEntity = setCloudSafeStream(cloudSafeEntity, folderPassword.toCharArray(), is, content.length, null, null, null);
			}
		} else if (encryptProtected) {
			cloudSafeEntity.setOptions(CloudSafeOptions.ENC.name());
		}
		return cloudSafeEntity;
	}

	private List<CloudSafeDto> deleteCloudSafeFolder(CloudSafeEntity cloudSafeEntity) throws DcemException {
		CloudSafeDto cloudSafeFolder = new CloudSafeDto(cloudSafeEntity.getId(), cloudSafeEntity.isFolder());
		List<CloudSafeDto> cloudSafeToDelete = deleteSubdirectories(cloudSafeFolder, new ArrayList<CloudSafeDto>(), cloudSafeEntity.getUser());
		deleteCloudSafeFile(cloudSafeEntity.getId());
		cloudSafeToDelete.add(new CloudSafeDto(cloudSafeEntity));
		return cloudSafeToDelete;
	}

	private List<CloudSafeDto> deleteSubdirectories(CloudSafeDto parentFolder, List<CloudSafeDto> cloudSafeToDeleteList, DcemUser dcemUser)
			throws DcemException {
		TypedQuery<CloudSafeDto> query = em.createNamedQuery(CloudSafeEntity.SELECT_CLOUD_SAFE_FOLDER_CHILDREN, CloudSafeDto.class);
		query.setParameter(1, parentFolder.getId());
		List<CloudSafeDto> children = query.getResultList();
		cloudSafeToDeleteList.addAll(children);
		for (CloudSafeDto cloudSafeDto : children) {
			if (cloudSafeDto.isFolder()) {
				deleteSubdirectories(cloudSafeDto, cloudSafeToDeleteList, dcemUser);
			}
//			Query thumbnailQuery = em.createNamedQuery(CloudSafeThumbnailEntity.DELETE_CLOUD_SAFE_THUMBNAIL_BY_CLOUD_SAFE_ID);
//			thumbnailQuery.setParameter(1, cloudSafeDto.getId());
//			thumbnailQuery.executeUpdate();
			deleteCloudSafeFile(cloudSafeDto.getId());
		}
		return cloudSafeToDeleteList;
	}

	@DcemTransactional
	public void setNewFolderName(CloudSafeEntity folder) throws DcemException {
		Query query = em.createNamedQuery(CloudSafeEntity.SET_FOLDER_NAME);
		query.setParameter(1, folder.getName());
		query.setParameter(2, folder.getId());
		query.executeUpdate();
	}

	@DcemTransactional
	public void moveCurrentEntry(CloudSafeEntity cloudSafeEntity, String folderPassword, Integer moveTo, DcemUser loggedInUser) throws DcemException {
		InputStream inputStream = null;
		if (cloudSafeEntity.isRecycled()) {
			if (moveTo == null) {
				moveTo = getCloudSafeRoot().getId();
			}
			if (moveTo == getCloudSafeRoot().getId()) {
				cloudSafeEntity.setRecycled(false);
				cloudSafeEntity.setDiscardAfter(null);
				cloudSafeEntity.setParent(getCloudSafeRoot());
			} else {
				CloudSafeEntity moveToFile = getCloudSafe(moveTo);
				while (moveToFile.getParent().getId() != moveToFile.getId()) {
					moveToFile = moveToFile.getParent();
				}
			}
			cloudSafeEntity.setLastModified(LocalDateTime.now());
			cloudSafeEntity.setLastModifiedUser(loggedInUser);
			em.merge(cloudSafeEntity);
		}
		// EG TODO folderPassword is not supported anymore
		if (folderPassword != null && !cloudSafeEntity.isOption(CloudSafeOptions.FPD)) {
			if (moveTo != null && (getCloudSafe(moveTo).isOption(CloudSafeOptions.PWD) || getCloudSafe(moveTo).isOption(CloudSafeOptions.FPD))) {
				inputStream = getCloudSafeContentAsStream(cloudSafeEntity, null, loggedInUser, null);
				cloudSafeEntity.setOptions((CloudSafeOptions.FPD.name()));
			} else if (moveTo == null || getCloudSafe(moveTo).isOption(CloudSafeOptions.ENC) && cloudSafeEntity.isOption(CloudSafeOptions.FPD)) {
				inputStream = getCloudSafeContentAsStream(cloudSafeEntity, folderPassword.toCharArray(), loggedInUser, null);
				cloudSafeEntity.setOptions((CloudSafeOptions.PWD.name()));
			} else if (getCloudSafe(moveTo).isOption(CloudSafeOptions.ENC) && cloudSafeEntity.isOption(CloudSafeOptions.ENC)
					&& cloudSafeEntity.isFolder() == false) {
				inputStream = getCloudSafeContentAsStream(cloudSafeEntity, null, loggedInUser, null);
			}
			File tempFile = null;
			FileOutputStream fileOutputStream = null;
			FileInputStream fileInputStreamTemp = null;
			try {
				tempFile = File.createTempFile("dcem-", "-cloudSafe");
				fileOutputStream = new FileOutputStream(tempFile);
				KaraUtils.copyStream(inputStream, fileOutputStream, 1024 * 64);
				fileOutputStream.close();
				fileInputStreamTemp = new FileInputStream(tempFile);
				cloudSafeEntity = setCloudSafeStream(cloudSafeEntity, folderPassword.toCharArray(), fileInputStreamTemp, (int) cloudSafeEntity.getLength(),
						loggedInUser, null, null);
			} catch (Exception ex) {
				throw new DcemException(DcemErrorCodes.CLOUD_SAFE_MOVE_FILE, "Could not move file " + cloudSafeEntity.getName(), ex);
			} finally {
				if (fileInputStreamTemp != null || tempFile != null) {
					try {
						fileInputStreamTemp.close();
						tempFile.delete();
					} catch (Exception e) {
						logger.info("couldn't close temporary files");
					}
				}
			}
		}
		if (moveTo == null || getCloudSafe(moveTo).isOption(CloudSafeOptions.ENC) && cloudSafeEntity.isOption(CloudSafeOptions.FPD)) {
			cloudSafeEntity.setOptions((CloudSafeOptions.PWD.name()));
		}
		Query query = em.createNamedQuery(CloudSafeEntity.MOVE_ENTRY);
		query.setParameter(1, moveTo);
		query.setParameter(2, cloudSafeEntity.getId());
		query.setParameter(3, cloudSafeEntity.getOptions());
		query.executeUpdate();
		if (loggedInUser != null && asModule.getModulePreferences().isEnableAuditUser() == true) {
			DcemAction dcemAction = new DcemAction(asCloudSafeSubject, DcemConstants.ACTION_MOVE);
			String shareUser = "";
			if (loggedInUser.getId() != cloudSafeEntity.getUser().getId()) {
				shareUser = AUDIT_SHARED_BY + cloudSafeEntity.getUser().getDisplayNameOrLoginId();
			}
			auditingLogic.addAudit(dcemAction, loggedInUser, "File: " + cloudSafeEntity.getName() + shareUser);
		}
	}

	public List<CloudSafeEntity> getByParentId(CloudSafeEntity cloudSafeEntity) throws DcemException {
		TypedQuery<CloudSafeEntity> query;
		query = em.createNamedQuery(CloudSafeEntity.GET_CLOUDSAFE_BY_PARENT, CloudSafeEntity.class);
		query.setParameter(1, cloudSafeEntity);
		return query.getResultList();
	}

	public List<CloudSafeEntity> getCloudSafeByUserAndParentId(Integer parentId, DcemUser user, List<DcemGroup> allUsersGroups, boolean recycled)
			throws DcemException {
		TypedQuery<CloudSafeEntity> query;
		query = em.createNamedQuery(CloudSafeEntity.GET_USER_CLOUDSAFE_DATA, CloudSafeEntity.class);
		query.setParameter(1, parentId);
		query.setParameter(2, user);
		if (allUsersGroups == null || allUsersGroups.isEmpty() == true) {
			query.setParameter(3, null);
		} else {
			query.setParameter(3, allUsersGroups);
		}
		query.setParameter(4, recycled);
		return query.getResultList();
	}

	public List<CloudSafeEntity> getByUserAndParentIdDocuments(Integer parentId, DcemUser user, List<DcemGroup> allUsersGroups) throws DcemException {
		TypedQuery<CloudSafeEntity> query;
		query = em.createNamedQuery(CloudSafeEntity.GET_USER_CLOUDSAFE_DOCUMENTS, CloudSafeEntity.class);
		query.setParameter(1, parentId);
		query.setParameter(2, user);
		if (allUsersGroups == null || allUsersGroups.isEmpty() == true) {
			query.setParameter(3, null);
		} else {
			query.setParameter(3, allUsersGroups);
		}
		return query.getResultList();
	}

	public List<CloudSafeEntity> getCloudSafeByUserFlat(DcemUser user, List<DcemGroup> allUsersGroups, boolean recycled) throws DcemException {
		TypedQuery<CloudSafeEntity> query;
		query = em.createNamedQuery(CloudSafeEntity.GET_USER_CLOUDSAFE_DATA_FLAT, CloudSafeEntity.class);
		query.setParameter(1, user);
		if (allUsersGroups == null || allUsersGroups.isEmpty() == true) {
			query.setParameter(2, null);
		} else {
			query.setParameter(2, allUsersGroups);
		}
		query.setParameter(3, recycled);
		return query.getResultList();
	}

	public CloudSafeEntity getCloudSafeUserSingleResult(String name, Integer parentId, boolean isFolder, Integer userId, boolean recycled) {
		TypedQuery<CloudSafeEntity> query;
		if (parentId == null || parentId == 0) {
			parentId = getCloudSafeRoot().getId();
		}
		query = em.createNamedQuery(CloudSafeEntity.GET_SINGLE_USER, CloudSafeEntity.class);
		query.setParameter(1, name);
		query.setParameter(2, parentId);
		query.setParameter(3, isFolder);
		query.setParameter(4, userId);
		query.setParameter(5, recycled);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public CloudSafeEntity getCloudSafeGroupSingleResult(String name, Integer parentId, Integer groupId, boolean recycled) {
		if (parentId == null || parentId == 0) {
			parentId = getCloudSafeRoot().getId();
		}
		TypedQuery<CloudSafeEntity> query = em.createNamedQuery(CloudSafeEntity.GET_SINGLE_GROUP, CloudSafeEntity.class);
		query.setParameter(1, name);
		query.setParameter(2, parentId);
		query.setParameter(3, groupId);
		query.setParameter(4, recycled);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	@DcemTransactional
	public CloudSafeEntity getCloudSafeRoot() {
		CloudSafeEntity rootCloudSafeEntity = asModule.getTenantData().getCloudSafeRoot();
		if (rootCloudSafeEntity == null) {
			try {
				TypedQuery<CloudSafeEntity> query = em.createNamedQuery(CloudSafeEntity.GET_CLOUDSAFE_ROOT, CloudSafeEntity.class);
				query.setParameter(1, DcemConstants.CLOUD_SAFE_ROOT);
				query.setParameter(2, userLogic.getUser(DcemConstants.SUPER_ADMIN_OPERATOR));
				rootCloudSafeEntity = query.getSingleResult();
				asModule.getTenantData().setCloudSafeRoot(rootCloudSafeEntity);
			} catch (NoResultException e) {
				try {
					DeviceEntity rootDevice = getRootDevice();
					if (rootDevice == null) {
						rootDevice = deviceLogic.addRootDevice(userLogic.getUser(DcemConstants.SUPER_ADMIN_OPERATOR));
					}
					DcemGroup dcemGroup = groupLogic.getRootGroup();
					// em.flush();
					updateToRootDevice(rootDevice);
					rootCloudSafeEntity = createCloudSafeEntityFolder(null, userLogic.getUser(DcemConstants.SUPER_ADMIN_OPERATOR),
							DcemConstants.CLOUD_SAFE_ROOT, false, true, null);
					rootCloudSafeEntity.setGroup(dcemGroup);
					addCloudSafeFolder(rootCloudSafeEntity); // Safe to Database
					rootCloudSafeEntity.setParent(rootCloudSafeEntity);
					updateToRoot(rootCloudSafeEntity);
					asModule.getTenantData().setCloudSafeRoot(rootCloudSafeEntity);

				} catch (Exception e1) {
					logger.error("Could not load cloud safe ' _ROOT_ ' entity from database.", e1);
				}
			} catch (Exception e1) {
				logger.error("Could not load cloud safe ' _ROOT_ ' entity from database.", e1);
			}
		}
		return rootCloudSafeEntity;
	}

	private void updateToRootDevice(DeviceEntity rootDevice) {
		Query queryRootDevice = em.createNamedQuery(CloudSafeEntity.UPDATE_ENTRIES_TO_ROOT_DEVICE);
		queryRootDevice.setParameter(1, rootDevice);
		queryRootDevice.executeUpdate();
	}

	private void updateToRoot(CloudSafeEntity root) {
		Query queryRootDevice = em.createNamedQuery(CloudSafeEntity.UPDATE_ENTRIES_TO_ROOT);
		queryRootDevice.setParameter(1, root);
		queryRootDevice.executeUpdate();
	}

	public DeviceEntity getRootDevice() {
		try {
			DeviceEntity rootDevice = asModule.getTenantData().getDeviceRoot();
			if (rootDevice == null) {
				rootDevice = deviceLogic.getDeviceByName(userLogic.getUser(DcemConstants.SUPER_ADMIN_OPERATOR), DcemConstants.DEVICE_ROOT,
						DeviceState.Disabled);
				asModule.getTenantData().setDeviceRoot(rootDevice);
			}
			return rootDevice;
		} catch (DcemException e) {
			logger.info("Root device was not found.", e);
		}
		return null;
	}

	@DcemTransactional
	public void changeOnwerShipCloudSafeEntity(CloudSafeEntity cloudSafeEntity, DcemGroup ownerGroup, DcemUser auditUser) throws DcemException {
		if (ownerGroup == null) {
			cloudSafeEntity.setOwner(CloudSafeOwner.USER);
			cloudSafeEntity.setGroup(groupLogic.getRootGroup());
			cloudSafeEntity.setUser(auditUser);
		} else {
			cloudSafeEntity.setOwner(CloudSafeOwner.GROUP);
			cloudSafeEntity.setGroup(ownerGroup);
			DcemUser superAdmin = userLogic.getSuperAdmin();
			cloudSafeEntity.setUser(superAdmin);
		}
		em.merge(cloudSafeEntity);
		if (auditUser != null && asModule.getModulePreferences().isEnableAuditUser() == true) {
			DcemAction dcemAction = new DcemAction(asCloudSafeSubject, DcemConstants.ACTION_CHANGE_OWNER);
			String shareUser = "";
			if (auditUser.getId() != cloudSafeEntity.getUser().getId()) {
				shareUser = AUDIT_SHARED_BY + cloudSafeEntity.getUser().getDisplayNameOrLoginId();
			}
			auditingLogic.addAudit(dcemAction, auditUser, "File: " + cloudSafeEntity.getName() + shareUser);
		}
	}

	public CloudSafeContentI getCloudSafeContentI() {
		return cloudSafeContentI;
	}

	public List<CloudSafeEntity> getAllCloudsafesByTag(Set<CloudSafeTagEntity> cloudSafeTagEntity) {
		TypedQuery<CloudSafeEntity> query = em.createNamedQuery(CloudSafeEntity.GET_BY_TAG, CloudSafeEntity.class);
		query.setParameter(1, cloudSafeTagEntity);
		return query.getResultList();
	}

	public List<CloudSafeEntity> getPathList(CloudSafeEntity cloudSafeEntity) {
		cloudSafeEntity = em.find(CloudSafeEntity.class, cloudSafeEntity.getId());
		CloudSafeEntity cloudSafeRoot = getCloudSafeRoot();
		List<CloudSafeEntity> list = new ArrayList<CloudSafeEntity>();
		CloudSafeEntity parent = cloudSafeEntity;
		if (cloudSafeEntity.isFolder() == false) {
			parent = cloudSafeEntity.getParent();
		}
		while (parent.getId() != cloudSafeRoot.getId()) {
			list.addFirst(parent);
			parent = parent.getParent();
		}
		return list;
	}

	public String getPath(CloudSafeEntity cloudSafeEntity) {
		try {
			List<CloudSafeEntity> list = getPathList(cloudSafeEntity);
			return getPathString(list);
		} catch (Exception e) {
			return "?Lazy";
		}
	}

	private String getPathString(List<CloudSafeEntity> list) {
		StringBuilder sb = new StringBuilder();
		for (CloudSafeEntity cloudSafeEntity : list) {
			if (sb.isEmpty() == false) {
				sb.append(FOLDER_SEPERATOR);
			}
			sb.append(cloudSafeEntity.getName());
		}
		return sb.toString();
	}

	public List<Integer> getIdsOfAllEntries() {
		TypedQuery<Integer> query = em.createNamedQuery(CloudSafeEntity.GET_ALL_IDS, Integer.class);
		return query.getResultList();
	}

	@DcemTransactional
	public CloudSafeEntity makeDirectories(CloudSafeEntity parent, String pathFile, DcemUser user, Map<String, CloudSafeEntity> folderCache) throws Exception {
		String[] pathSubstrings = pathFile.split(CloudSafeLogic.FOLDER_SEPERATOR);
		if (parent == null) {
			parent = getCloudSafeRoot();
		}
		DeviceEntity deviceEntity = getRootDevice();
		for (int i = 0; i < pathSubstrings.length - 1; i++) {
			String dirName = pathSubstrings[i];
			try {
				if (folderCache != null) {
					CloudSafeEntity parentCahced = folderCache.get(parent.getId() + dirName);
					if (parentCahced == null) {
						parent = getCloudSafe(CloudSafeOwner.USER, dirName, user, deviceEntity, parent.getId(), null);
						folderCache.put(parent.getId() + dirName, parentCahced);
					}
				} else {
					parent = getCloudSafe(CloudSafeOwner.USER, dirName, user, deviceEntity, parent.getId(), null);
				}
				
			} catch (DcemException e1) { // not found
				CloudSafeEntity newDir = new CloudSafeEntity(CloudSafeOwner.USER, user, deviceEntity, dirName, null, null, true, parent);
				try {
					parent = updateCloudSafeEntity(newDir, user, false, null);
				} catch (DcemException e2) {
					throw new ExceptionReporting(
							new DcemReporting(ReportAction.WriteCloudSafe, user, AsUtils.convertToAppErrorCodes(e2.getErrorCode()), null, user.getLoginId()),
							"Exception while writing new directories for file: " + pathFile);
				}
			}
		}
		return parent;
	}
	
	public List<DocumentVersion> getS3Versions (CloudSafeEntity document) throws DcemException {
		return cloudSafeContentI.getS3Versions(document.getId());
	}

}
