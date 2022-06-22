package com.doubleclue.dcem.as.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.doubleclue.dcem.as.comm.AppSession;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.entities.DeviceEntity;
import com.doubleclue.dcem.as.entities.FingerprintId;
import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.as.entities.PolicyEntity;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.module.ModuleTenantData;
import com.hazelcast.core.IAtomicLong;
import com.hazelcast.core.IMap;
import com.hazelcast.flakeidgen.FlakeIdGenerator;
import com.yubico.webauthn.data.ByteArray;

import jersey.repackaged.com.google.common.cache.CacheBuilder;
import jersey.repackaged.com.google.common.cache.CacheLoader;
import jersey.repackaged.com.google.common.cache.LoadingCache;

public class AsTenantData extends ModuleTenantData {

	private final AtomicReference<HashMap<PolicyAppEntity, Map<String, PolicyEntity>>> applicationPolicies = new AtomicReference<>();
	private IAtomicLong globalCloudSafeUsageTotal = null;

	private Map<AuthApplication, PolicyAppEntity> mainPolicyApps;
	private List<PolicyAppEntity> policyAppEntities;
	private FlakeIdGenerator msgIdGenerator;
	private IMap<Long, PendingMsg> pendingMsgs; // user id - message
	private IMap<String, LoginQrCode> loginQrCodes; // user id - message
	private IMap<FingerprintId, String> smsPasscodesMap;
	private PolicyEntity globalEntityPolicy;
	PushNotificationConfig pushNotificationConfig;
	CloudSafeEntity cloudSafeRoot = null;
	DeviceEntity deviceRoot = null;

	private final LoadingCache<ByteArray, FidoRegRequestInfo> fidoRegRequests = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES)
			.build(new CacheLoader<ByteArray, FidoRegRequestInfo>() {
				@Override
				public FidoRegRequestInfo load(ByteArray key) throws Exception {
					return null;
				}
			});

	private final LoadingCache<ByteArray, FidoAuthRequestInfo> fidoAuthRequests = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES)
			.build(new CacheLoader<ByteArray, FidoAuthRequestInfo>() {
				@Override
				public FidoAuthRequestInfo load(ByteArray key) throws Exception {
					return null;
				}
			});

	// device Id
	private final ConcurrentHashMap<Integer, AppSession> deviceSessions = new ConcurrentHashMap<Integer, AppSession>();

	// private final LoadingCache<Long, PolicyTransaction> pendingMessageFingerprints = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES)
	// .build(new CacheLoader<Long, PolicyTransaction>() {
	// @Override
	// public PolicyTransaction load(Long key) throws Exception {
	// return null;
	// }
	// });

	private final ConcurrentHashMap<String, AuthAppSession> authAppSessions = new ConcurrentHashMap<String, AuthAppSession>();

	private final ConcurrentHashMap<Long, AuthAppSession> authProxySessions = new ConcurrentHashMap<Long, AuthAppSession>();

	// getters and setters

	public Map<AuthApplication, PolicyAppEntity> getMainPolicyApps() {
		return mainPolicyApps;
	}

	public void setMainPolicyApps(Map<AuthApplication, PolicyAppEntity> mainPolicyApps) {
		this.mainPolicyApps = mainPolicyApps;
	}

	public List<PolicyAppEntity> getPolicyAppEntities() {
		return policyAppEntities;
	}

	public void setPolicyAppEntities(List<PolicyAppEntity> policyAppEntities) {
		this.policyAppEntities = policyAppEntities;
	}

	public FlakeIdGenerator getMsgIdGenerator() {
		return msgIdGenerator;
	}

	public void setMsgIdGenerator(FlakeIdGenerator msgIdGenerator) {
		this.msgIdGenerator = msgIdGenerator;
	}

	public IMap<Long, PendingMsg> getPendingMsgs() {
		return pendingMsgs;
	}

	public void setPendingMsgs(IMap<Long, PendingMsg> pendingMsgs) {
		this.pendingMsgs = pendingMsgs;
	}

	public IMap<String, LoginQrCode> getLoginQrCodes() {
		return loginQrCodes;
	}

	public void setLoginQrCodes(IMap<String, LoginQrCode> loginQrCodes) {
		this.loginQrCodes = loginQrCodes;
	}

	public IMap<FingerprintId, String> getSmsPasscodesMap() {
		return smsPasscodesMap;
	}

	public void setSmsPasscodesMap(IMap<FingerprintId, String> smsPasscodesMap) {
		this.smsPasscodesMap = smsPasscodesMap;
	}

	public PolicyEntity getGlobalEntityPolicy() {
		return globalEntityPolicy;
	}

	public void setGlobalEntityPolicy(PolicyEntity globalEntityPolicy) {
		this.globalEntityPolicy = globalEntityPolicy;
	}

	public ConcurrentHashMap<Integer, AppSession> getDeviceSessions() {
		return deviceSessions;
	}

	public ConcurrentHashMap<String, AuthAppSession> getAuthAppSessions() {
		return authAppSessions;
	}

	public AtomicReference<HashMap<PolicyAppEntity, Map<String, PolicyEntity>>> getApplicationPolicies() {
		return applicationPolicies;
	}

	public LoadingCache<ByteArray, FidoRegRequestInfo> getFidoRegRequests() {
		return fidoRegRequests;
	}

	public LoadingCache<ByteArray, FidoAuthRequestInfo> getFidoAuthRequests() {
		return fidoAuthRequests;
	}

	@SuppressWarnings("deprecation")
	public IAtomicLong getGlobalCloudSafeUsageTotal() {
		if (globalCloudSafeUsageTotal == null) {
			globalCloudSafeUsageTotal = DcemCluster.getInstance().getHazelcastInstance()
					.getAtomicLong(AsConstants.HAZELCAST_NAME_CLOUDSAFE_GLOBAL_USAGE + "@" + TenantIdResolver.getCurrentTenant().getName());
		}
		return globalCloudSafeUsageTotal;
	}

	public PushNotificationConfig getPushNotificationConfig() {
		return pushNotificationConfig;
	}

	public void setPushNotificationConfig(PushNotificationConfig pushNotificationConfig) {
		this.pushNotificationConfig = pushNotificationConfig;
	}

	public CloudSafeEntity getCloudSafeRoot() {
		return cloudSafeRoot;
	}

	public void setCloudSafeRoot(CloudSafeEntity cloudSafeRoot) {
		this.cloudSafeRoot = cloudSafeRoot;
	}

	public DeviceEntity getDeviceRoot() {
		return deviceRoot;
	}

	public void setDeviceRoot(DeviceEntity deviceRoot) {
		this.deviceRoot = deviceRoot;
	}
}
