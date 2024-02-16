package com.doubleclue.dcem.radius.logic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.apache.commons.codec.Charsets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.as.comm.AsMessageHandler;
import com.doubleclue.dcem.as.logic.AsDeviceLogic;
import com.doubleclue.dcem.as.logic.PendingMsg;
import com.doubleclue.dcem.as.policy.AuthenticationLogic;
import com.doubleclue.dcem.as.policy.PolicyTransaction;
import com.doubleclue.dcem.as.restapi.model.AsApiMessageResponse;
import com.doubleclue.dcem.as.restapi.model.AsApiMsgStatus;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.as.AuthMethod;
import com.doubleclue.dcem.core.as.AuthRequestParam;
import com.doubleclue.dcem.core.as.AuthenticateResponse;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.TenantEntity;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.logic.ClaimAttribute;
import com.doubleclue.dcem.core.logic.DbResourceBundle;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.radius.attribute.RadiusAttribute;
import com.doubleclue.dcem.radius.attribute.RadiusAttributeEnum;
import com.doubleclue.dcem.radius.attribute.StringAttribute;
import com.doubleclue.dcem.radius.attribute.VendorSpecificAttribute;
import com.doubleclue.dcem.radius.dictionary.DefaultDictionary;
import com.doubleclue.dcem.radius.entities.RadiusClientEntity;
import com.doubleclue.dcem.radius.entities.RadiusReportEntity;
import com.doubleclue.utils.KaraUtils;
import com.doubleclue.utils.StringUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheLoader.InvalidCacheLoadException;
import com.google.common.cache.LoadingCache;



@ApplicationScoped
public class ProcessRadiusPacket {

	private static final Logger logger = LogManager.getLogger(ProcessRadiusPacket.class);

	@Inject
	DcemApplicationBean applicationBean;

	@Inject
	RadiusClientLogic radiusClientLogic;

	@Inject
	RadiusModule radiusModule;

	@Inject
	AsMessageHandler messageHandler;

	@Inject
	RadiusReportLogic reportLogic;

	@Inject
	UserLogic userLogic;

	@Inject
	TemplateLogic templateLogic;

	@Inject
	AsDeviceLogic asDeviceLogic;

	@Inject
	AuthenticationLogic authenticationLogic;

	@Inject
	AsMessageHandler asMessageHandler;

	@Inject
	EntityManager em;

	int requests;

	private long duplicateInterval = 5 * 60 * 1000; // 5 minutes
	private List<ReceivedPacket> receivedPackets = new LinkedList<>();

	private LoadingCache<Long, AccessRequest> cache;
	int nodeId;

	int radiusPort;
	int radiusAccountingPort;

	public ProcessRadiusPacket() {

	}

	@PostConstruct
	public void init() {
		cache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<Long, AccessRequest>() {
			@Override
			public AccessRequest load(Long key) throws Exception {
				return null;
			}
		});
		nodeId = DcemCluster.getDcemCluster().getDcemNode().getId();
		radiusPort = radiusModule.getConnectionServiceRadius().getPort();
		radiusAccountingPort = radiusModule.getConnectionServiceRadiusAccounting().getPort();
	}

	public void onMsgResponseReceived(@Observes PendingMsg pendingMsg) {
		if (pendingMsg.getPolicyTransaction().getPolicyAppEntity().getAuthApplication() != AuthApplication.RADIUS) {
			return;
		}
		try {
			asMessageHandler.retrieveMessageResponse(pendingMsg.getId(), 0);
		} catch (DcemException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			// System.out.println("ProcessRadiusPacket.onMsgResponseReceived() " +
			// pendingMsg.getId());
			AccessRequest accessRequest = null;
			try {
				accessRequest = cache.get(pendingMsg.getId());
			} catch (InvalidCacheLoadException e) {
				return;
			} catch (Exception e) {
				return;
			}
			RadiusPacket radiusResponse;

			if ((pendingMsg.getMsgStatus() == AsApiMsgStatus.OK) && (pendingMsg.getActionId().equals("ok"))) {
				PolicyTransaction policyTransaction = pendingMsg.getPolicyTransaction();
				RadiusClientEntity radiusClientEntity = radiusClientLogic.getRadiusClientById(policyTransaction.getPolicyAppEntity().getSubId());
				radiusResponse = acceptPacket(pendingMsg.getUserLoginId(), accessRequest, radiusClientEntity, userLogic.getUser(pendingMsg.getUserId()),
						policyTransaction.getPolicyName());
			} else {
				radiusResponse = rejectPacket(pendingMsg.getUserLoginId(), RadiusReportAction.Rejected, accessRequest);
			}
			try {
				sendResponse(accessRequest, radiusResponse);
			} catch (IOException e) {
				reportLogic
						.addReporting(new RadiusReportEntity(null, RadiusReportAction.SendError, "send from MsgResponse: " + accessRequest.getRemoteAddress()));
			}

		} catch (Exception e) {
			logger.info(e);
		}
	}

	/**
	 * @param inPacket
	 * @param authSocket
	 * @throws IOException
	 * @throws RadiusException
	 */
	public void process(DatagramPacket inPacket, DatagramSocket authSocket) throws IOException {

		InetSocketAddress localAddress = (InetSocketAddress) authSocket.getLocalSocketAddress();
		InetSocketAddress remoteAddress = new InetSocketAddress(inPacket.getAddress(), inPacket.getPort());
		String remoteIp = remoteAddress.getAddress().getHostAddress();
		byte[] recData = inPacket.getData();
		if (radiusModule.getPreferences().isTraceData()) {
			logger.info("RADIUS-Receive-Packet: From: " + remoteIp + ", Data: " + StringUtils.binaryToHexString(recData, 0, recData.length));
		}

		RadiusClientEntity radiusClientEntity = radiusModule.getRadiusClient(remoteIp);

		if (radiusClientEntity == null) {
			reportLogic.addReporting(new RadiusReportEntity(null, RadiusReportAction.SharedSecret, "No NAS-Client found for: " + remoteIp));
			if (logger.isInfoEnabled()) {
				logger.info("Ignoring packet from unknown client: " + remoteAddress);
			}
			return;
		}
		TenantEntity tenantEntity = applicationBean.getTenant(radiusClientEntity.getTenantName());
		TenantIdResolver.setCurrentTenant(tenantEntity);

		// parse packet
		ByteArrayInputStream in = new ByteArrayInputStream(recData);
		RadiusPacket request;
		Charset charset = StandardCharsets.UTF_8;
		try {
			charset = Charset.forName(radiusClientEntity.getRadiusClientSettings().getSupportedCharset().name().replace('_', '-'));
		} catch (Exception exp) {
			logger.info("Invalid Charset ", exp);
		}

		try {
			request = RadiusPacket.decodePacket(DefaultDictionary.getDefaultDictionary(), in, radiusClientEntity.getSharedSecret(), null);
			request.setNasClient(radiusClientEntity.getName());
			request.setCharset(charset);
		} catch (RadiusException e) {
			reportLogic.addReporting(new RadiusReportEntity(radiusClientEntity.getName(), RadiusReportAction.DecodingException, e.toString()));
			return;
		}
		if (radiusModule.getPreferences().isTraceData()) {
			logger.info("RADIUS Received: " + request);
		}
		request.setDatagramSocket(authSocket);
		request.setSharedSecret(radiusClientEntity.getSharedSecret());
		request.setRemoteAddress(remoteAddress);
		RadiusPacket response;
		try {
			response = handlePacket(localAddress, remoteAddress, request, radiusClientEntity);
		} catch (RadiusException | DcemException e) {
			reportLogic.addReporting(new RadiusReportEntity(radiusClientEntity.getName(), RadiusReportAction.HandlingException, e.toString()));
			return;
		}

		// send response
		if (response != null) {
			sendResponse(request, response);
		}

	}

	private void sendResponse(RadiusPacket radiusRequest, RadiusPacket radiusResponse) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		radiusResponse.encodeResponsePacket(bos, radiusRequest.getSharedSecret(), radiusRequest);
		byte[] data = bos.toByteArray();
		if (radiusModule.getPreferences().isTraceData()) {
			String remoteIp = radiusRequest.getRemoteAddress().getAddress().getHostAddress();
			logger.info("RADIUS-Send-Packet: to: " + remoteIp + ", Data: " + StringUtils.binaryToHexString(data, 0, data.length));
			logger.info("RADIUS send response: " + radiusResponse);
		}
		DatagramPacket datagram = new DatagramPacket(data, data.length, radiusRequest.getRemoteAddress().getAddress(),
				radiusRequest.getRemoteAddress().getPort());
		radiusRequest.getDatagramSocket().send(datagram);

		// cleear passwords
		RadiusAttribute radiusAttribute = radiusResponse.getAttribute(RadiusAttributeEnum.UserPassword);
		if (radiusAttribute != null) {
			Arrays.fill(radiusAttribute.getAttributeData(), (byte) 0x0);
		}
		radiusAttribute = radiusRequest.getAttribute(RadiusAttributeEnum.UserPassword);
		if (radiusAttribute != null) {
			Arrays.fill(radiusAttribute.getAttributeData(), (byte) 0x0);
		}

	}

	/**
	 * Handles the received Radius packet and constructs a response.
	 * 
	 * @param localAddress  local address the packet was received on
	 * @param remoteAddress remote address the packet was sent by
	 * @param request       the packet
	 * @return response packet or null for no response
	 * @throws RadiusException
	 * @throws DcemException
	 */
	protected RadiusPacket handlePacket(InetSocketAddress localAddress, InetSocketAddress remoteAddress, RadiusPacket request,
			RadiusClientEntity radiusClientEntity) throws RadiusException, IOException, DcemException {

		RadiusPacket response = null;

		// check for duplicates
		if (isPacketDuplicate(request, remoteAddress) == true) {
			logger.info("RADIUS: ignore duplicate packet");
			return null;
		}
		if (localAddress.getPort() == radiusPort) {
			// handle packets on auth port
			if (request instanceof AccessRequest) {
				RadiusAttribute stateAttribute = request.getAttribute(RadiusAttributeEnum.State);
				// System.out.println("ProcessRadiusPacket.handlePacket() RequestPacket");
				if (stateAttribute == null) {
					response = accessRequestReceived((AccessRequest) request, remoteAddress, radiusClientEntity);
				} else {
					response = challengeResponseReceived((AccessRequest) request, radiusClientEntity, stateAttribute);
				}
			} else
				logger.error("RADIUS: unknown packet type: " + request.getRadiusPacketType().name());
		} else if (localAddress.getPort() == radiusAccountingPort) {
			// handle packets on acct port
			if (request instanceof AccountingRequest)
				response = accountingRequestReceived((AccountingRequest) request, remoteAddress);
			else
				logger.error("RADIUS: unknown packet type: " + request.getRadiusPacketType().name());
		} else {
			// ignore packet on unknown port
		}

		return response;
	}

	/**
	 * Constructs an answer for an Access-Request packet. Either this method or
	 * isUserAuthenticated should be overriden.
	 * 
	 * @param accessRequest      Radius request packet
	 * @param radiusClientEntity
	 * @param client             address of Radius client
	 * @return response packet or null if no packet shall be sent
	 * @exception RadiusException malformed request packet; if this exception is
	 *                            thrown, no answer will be sent
	 */
	public RadiusPacket accessRequestReceived(AccessRequest accessRequest, InetSocketAddress remoteAddress, RadiusClientEntity radiusClientEntity)
			throws RadiusException {

		String userLoginId = accessRequest.getUserName();
		if (userLoginId == null) {
			return rejectPacket("No user attribute", RadiusReportAction.NoUserAttribute, accessRequest);
		}
		boolean useChallenge = radiusClientEntity.isUseChallenge();

		String passcode = null;
		String password = null;
		if (accessRequest.getUserPassword() == null && radiusClientEntity.isIgnoreUsersPassword() == false) {
			return rejectPacket(userLoginId, RadiusReportAction.NoPasswordReceived, accessRequest);
		}

		AuthenticateResponse authenticateResponse;
		try {
			AuthRequestParam requestParam = new AuthRequestParam();
			requestParam.setTemplateName(DcemConstants.RADIUS_LOGIN_TEMPLATE);
			if (radiusClientEntity.isIgnoreUsersPassword() == true) {
				requestParam.setIgnorePassword(true);
			} else {
				password = new String(accessRequest.getUserPassword(), accessRequest.getCharset());
			}

			authenticateResponse = authenticationLogic.authenticate(AuthApplication.RADIUS, radiusClientEntity.getId(), userLoginId, null, password, passcode,
					requestParam);
			if (authenticateResponse.getBinPassword() != null) {
				// replace the original password
				accessRequest.getAttribute(RadiusAttributeEnum.UserPassword).setAttributeData(authenticateResponse.getBinPassword());
			}
			if (authenticateResponse.isSuccessful()) {
				return acceptPacket(userLoginId, accessRequest, radiusClientEntity, authenticateResponse.getDcemUser(), authenticateResponse.getPolicyName());
			}
			if (authenticateResponse.getDcemException() != null) {
				return rejectPacket(userLoginId, RadiusReportAction.Rejected, accessRequest);
			}
			
			if (authenticateResponse.getAuthMethods() == null) {
				return rejectPacket(userLoginId, RadiusReportAction.InvalidCharacterEncoding, accessRequest);
			}
			if (authenticateResponse.getAuthMethods().size() > 1) {
				return rejectPacket(userLoginId, RadiusReportAction.MULTI_AUTH_METHODS, accessRequest);
			}
			AuthMethod authMethod = authenticateResponse.getAuthMethods().get(0);
			switch (authMethod) {
			case PUSH_APPROVAL:
				if (useChallenge == false) {
					cache.put(authenticateResponse.getSecureMsgId(), accessRequest);
					return null;
				} else {
					return createChallengPacket(authenticateResponse.getDcemUser(), accessRequest, authenticateResponse.getSecureMsgId(),
							authenticateResponse.getSecureMsgRandomCode());
				}
			case SMS:
			case VOICE_MESSAGE:
				if (useChallenge == false) {
					return rejectPacket(userLoginId, RadiusReportAction.SMS_WITHOUT_CHALLENG, accessRequest);
				} else {
					return createChallengPacket(authenticateResponse.getDcemUser(), accessRequest, authenticateResponse.getSecureMsgId(),
							authenticateResponse.getSecureMsgRandomCode());
				}
			default:
				break;

			}

		} catch (DcemException exp) {
			RadiusReportAction reportAction = RadiusReportAction.Rejected;
			return rejectPacket(userLoginId + " / " + exp.toString(), reportAction, accessRequest);

		} catch (Exception exp) {
			RadiusReportAction reportAction = RadiusReportAction.Rejected;
			return rejectPacket(userLoginId + " / " + exp.toString(), reportAction, accessRequest);
		} 
		return null;

	}

	/**
	 * @param accessRequest
	 * @param client
	 * @param radiusClientEntity
	 * @return
	 * @throws RadiusException
	 * @throws DcemException
	 */
	public RadiusPacket challengeResponseReceived(AccessRequest accessRequest, RadiusClientEntity radiusClientEntity, RadiusAttribute stateAttribute)
			throws RadiusException, DcemException {

		byte[] replyData = stateAttribute.getAttributeData();
		String replyMsg = new String(replyData, Charsets.ISO_8859_1);
		String[] challengeStateReplay = replyMsg.split(RadiusConstants.CHALLENGE_STATE_DELIMETER);
		if (challengeStateReplay.length < 4 || replyMsg.startsWith(RadiusConstants.CHALLENGE_STATE_MSG) == false) {
			reportLogic.addReporting(new RadiusReportEntity(radiusClientEntity.getName(), RadiusReportAction.InvalidChallengState, replyMsg));
			RadiusPacket packet = new RadiusPacket(RadiusPacketType.ACCESS_REJECT, accessRequest);
			return packet;
		}
		Long msgId = Long.valueOf(challengeStateReplay[3]);
		AsApiMessageResponse messageResponse;
		try {
			messageResponse = messageHandler.retrieveMessageResponse(msgId.longValue(), 0);
			if (messageResponse.getUserLoginId().equals(challengeStateReplay[1]) == false) {
				reportLogic.addReporting(new RadiusReportEntity(radiusClientEntity.getName(), RadiusReportAction.InvalidStateUser, replyMsg));
				throw new DcemException(DcemErrorCodes.INVALID_USERID, challengeStateReplay[1]);
			}
		} catch (DcemException e) {
			return rejectPacket(e.toString(), RadiusReportAction.RetrieveMsg, accessRequest);
		}
		if (messageResponse.getFinal()) {
			if ((messageResponse.getMsgStatus() == AsApiMsgStatus.OK) && (messageResponse.getActionId().equals("ok"))) {
				DcemUser dcemUser = userLogic.getUser(messageResponse.getUserLoginId());
				return acceptPacket(messageResponse.getUserLoginId(), accessRequest, radiusClientEntity, dcemUser, null);
			} else {
				return rejectPacket(messageResponse.getUserLoginId(), RadiusReportAction.Rejected, accessRequest);
			}
		} else {
			DcemUser user = userLogic.getUser(accessRequest.getUserName());
			return createChallengPacket(user, accessRequest, msgId, challengeStateReplay[2]);
		}
	}

	// public void messageReceived(@Observes PendingMsg pendingMsg) {
	//
	// }

	/**
	 * Constructs an answer for an Accounting-Request packet. This method should be
	 * overriden if accounting is supported.
	 * 
	 * @param accountingRequest Radius request packet
	 * @param client            address of Radius client
	 * @return response packet or null if no packet shall be sent
	 * @exception RadiusException malformed request packet; if this exception is
	 *                            thrown, no answer will be sent
	 */
	public RadiusPacket accountingRequestReceived(AccountingRequest accountingRequest, InetSocketAddress client) throws RadiusException {
		return new RadiusPacket(RadiusPacketType.ACCOUNTING_RESPONSE, accountingRequest.getPacketIdentifier(),
				accountingRequest.getAttributes(RadiusAttributeEnum.ProxyState.getType()));
	}

	/**
	 * Checks whether the passed packet is a duplicate. A packet is duplicate if
	 * another packet with the same identifier has been sent from the same host in
	 * the last time.
	 * 
	 * @param packet  packet in question
	 * @param address client address
	 * @return true if it is duplicate
	 */
	protected boolean isPacketDuplicate(RadiusPacket packet, InetSocketAddress address) {
		long now = System.currentTimeMillis();
		long intervalStart = now - duplicateInterval;

		byte[] authenticator = packet.getAuthenticator();

		synchronized (receivedPackets) {
			for (Iterator<ReceivedPacket> i = receivedPackets.iterator(); i.hasNext();) {
				ReceivedPacket receivePacket = i.next();
				if (receivePacket.receiveTime < intervalStart) {
					// packet is older than duplicate interval
					i.remove();
				} else {
					if (receivePacket.address.equals(address) && receivePacket.packetIdentifier == packet.getPacketIdentifier()) {
						if (authenticator != null && receivePacket.authenticator != null) {
							// packet is duplicate if stored authenticator is
							// equal to the packet authenticator
							return Arrays.equals(receivePacket.authenticator, authenticator);
						} else {
							// should not happen, packet is duplicate
							return true;
						}
					}
				}
			}
			// add packet to receive list
			ReceivedPacket rp = new ReceivedPacket(packet.getPacketIdentifier(), now, address, authenticator);
			receivedPackets.add(rp);
		}

		return false;
	}

	protected RadiusPacket createChallengPacket(DcemUser user, AccessRequest accessRequest, long msgId, String code) throws DcemException {
		RadiusPacket responsePacket = new RadiusPacket(RadiusPacketType.ACCESS_CHALLENGE, accessRequest);
		StringAttribute attribute = new StringAttribute(RadiusAttributeEnum.State.getType(),
				RadiusConstants.CHALLENGE_STATE_MSG + RadiusConstants.CHALLENGE_STATE_DELIMETER + user.getLoginId() + RadiusConstants.CHALLENGE_STATE_DELIMETER
						+ code + RadiusConstants.CHALLENGE_STATE_DELIMETER + Long.toString(msgId));
		responsePacket.addAttribute(attribute);

		DbResourceBundle dbResourceBundle = DbResourceBundle.getDbResourceBundle(user.getLanguage().getLocale());
		String challengeText = dbResourceBundle.getString(RadiusConstants.CHALLENGE_BUNDLE_KEY);
		Map<String, String> map = new HashMap<>(1);
		map.put("code", code);
		String contents = StringUtils.substituteTemplate(challengeText, map);

		attribute = new StringAttribute(RadiusAttributeEnum.ReplyMessage.getType(), contents);
		responsePacket.addAttribute(attribute);
		return responsePacket;
	}

	RadiusPacket rejectPacket(String details, RadiusReportAction action, AccessRequest accessRequest) {
		reportLogic.addReporting(new RadiusReportEntity(accessRequest.getNasClient(), action, details));
		List<RadiusAttribute> attrs = accessRequest.getAttributes(RadiusAttributeEnum.ProxyState.getType());
		attrs.add(new RadiusAttribute(RadiusAttributeEnum.ReplyMessage.getType(), (action.name() + " - " + details).getBytes()));
		return new RadiusPacket(RadiusPacketType.ACCESS_REJECT, accessRequest.getPacketIdentifier(), attrs);
	}

	RadiusPacket acceptPacket(String details, AccessRequest accessRequest, RadiusClientEntity radiusClientEntity, DcemUser dcemUser, String policyName) {
		if (radiusModule.getPreferences().isWriteReportForValidAuthentication() == true) {
			reportLogic.addReporting(new RadiusReportEntity(accessRequest.getNasClient(), RadiusReportAction.OK, details));
		}
		RadiusPacket radiusPacket = new RadiusPacket(RadiusPacketType.ACCESS_ACCEPT, accessRequest.getPacketIdentifier(),
				accessRequest.getAttributes(RadiusAttributeEnum.ProxyState.getType()));
		RadiusAttribute radiusAttribute = accessRequest.getAttribute(RadiusAttributeEnum.UserPassword);
		String password = null;
		if (radiusAttribute != null) {
			password = radiusAttribute.getAttributeValueString();
		}
		em.detach(radiusClientEntity);
		List<ClaimAttribute> claimAttributes = authenticationLogic.getClaimAttributeValues(radiusClientEntity.getRadiusClientSettings().getClaimAttributes(),
				dcemUser, policyName, password);
		int attributeId;
		for (ClaimAttribute claimAttribute : claimAttributes) {
			if (claimAttribute.getValue() != null && claimAttribute.getValue().isEmpty() == false) {
				try {
					attributeId = Integer.parseInt(claimAttribute.getName());
					if (attributeId == RadiusAttributeEnum.VendorSpecific.getType()) {
						int vendorId = 0;
						if (claimAttribute.getSubName() != null) {
							vendorId = Integer.parseInt(claimAttribute.getSubName());
						}
						if (vendorId == 0) {
							byte[] value = claimAttribute.getValue().getBytes(accessRequest.getCharset());
							byte[] result = new byte[4 + value.length];
							KaraUtils.intToByteArray(RadiusConstants.VENDOR_ID, result);
							System.arraycopy(value, 0, result, 4, value.length);
							radiusAttribute = new RadiusAttribute(attributeId, result);
							radiusPacket.addAttribute(radiusAttribute);
						} else {
							VendorSpecificAttribute vendorSpecificAttribute = new VendorSpecificAttribute(RadiusConstants.VENDOR_ID);
							radiusAttribute = new RadiusAttribute(vendorId, claimAttribute.getValue().getBytes(accessRequest.getCharset()));
							radiusAttribute.setVendorId(RadiusConstants.VENDOR_ID);
							vendorSpecificAttribute.addSubAttribute(radiusAttribute);
							radiusPacket.addAttribute(vendorSpecificAttribute);
						}
					} else {
						radiusAttribute = new RadiusAttribute(attributeId, claimAttribute.getValue().getBytes(accessRequest.getCharset()));
						radiusPacket.addAttribute(radiusAttribute);
					}
				} catch (Exception e) {
					logger.warn("invalid RADIUS Attribute Id", e);
					continue;
				}
			}
		}
		return radiusPacket;
	}

	/**
	 * This internal class represents a packet that has been received by the server.
	 */
	class ReceivedPacket {

		public ReceivedPacket(int packetIdentifier, long receiveTime, InetSocketAddress address, byte[] authenticator) {
			super();
			this.packetIdentifier = packetIdentifier;
			this.receiveTime = receiveTime;
			this.address = address;
			this.authenticator = authenticator;
		}

		public int packetIdentifier;
		public long receiveTime;
		public InetSocketAddress address;
		public byte[] authenticator;

	}

	public int getRequests() {
		return requests;
	}

}
