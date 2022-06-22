package com.doubleclue.dcem.test.units;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.doubleclue.dcem.as.entities.PolicyAppEntity;
import com.doubleclue.dcem.as.entities.PolicyEntity;
import com.doubleclue.dcem.as.policy.DcemPolicy;
import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.as.policy.PolicyTreeItem;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.as.AuthApplication;
import com.doubleclue.dcem.core.as.AuthMethod;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.radius.attribute.RadiusAttribute;
import com.doubleclue.dcem.radius.attribute.RadiusAttributeEnum;
import com.doubleclue.dcem.radius.client.RadiusClient;
import com.doubleclue.dcem.radius.entities.RadiusClientEntity;
import com.doubleclue.dcem.radius.logic.AccessRequest;
import com.doubleclue.dcem.radius.logic.RadiusClientLogic;
import com.doubleclue.dcem.radius.logic.RadiusModule;
import com.doubleclue.dcem.radius.logic.RadiusPacket;
import com.doubleclue.dcem.radius.logic.RadiusPacketType;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;
import com.doubleclue.sdk.api.AsErrorCodes;
import com.doubleclue.sdk.api.ReceivedMessage;
import com.doubleclue.sdk.api.ResponseMessage;
import com.doubleclue.sdk.api.SdkListenerMethods;

@ApplicationScoped
@Named("RadiusTest")
public class RadiusTest extends AbstractTestUnit {

	private static final String CLIENT_IP = "127.0.0.1";
	private static final String CLIENT_NAME = "TestunitRadius";
	private static final String POLICY_NAME = "TestunitRadius_Policy";
	private static final List<AuthMethod> ALLOWED_AUTH_METHODS = Stream.of(AuthMethod.PUSH_APPROVAL).collect(Collectors.toList());
	private static final String DEFAULT_SHARED_SECRET = "password";

	@Inject
	TestModule testModule;

	@Inject
	ActivateLoginTest activateLoginTest;

	@Inject
	AddUserWithActivationTest userWithActivationTest;

	@Inject
	RadiusClientLogic radiusClientLogic;

	@Inject
	EntityManager em;

	@Inject
	PolicyLogic policyLogic;

	@Override
	public String getDescription() {
		return "This unit will create a RADIUS client and test some requests.";
	}

	@Override
	public String getAuthor() {
		return "Emanuel Galea";
	}

	@Override
	public List<String> getDependencies() {
		List<String> dependencies = new LinkedList<String>();
		dependencies.add(AddUserWithActivationTest.class.getSimpleName());
		dependencies.add(ActivateLoginTest.class.getSimpleName());
		return dependencies;
	}	
	
	@Override
	public TestUnitGroupEnum getParent() {
		return TestUnitGroupEnum.RADIUS;
	}

	@Override
	public boolean isRunnableTest() {
		return true;
	}

	private PolicyEntity getPolicyEntity() {
		TypedQuery<PolicyEntity> query = em.createNamedQuery(PolicyEntity.GET_POLICY_BY_NAME, PolicyEntity.class);
		query.setParameter(1, POLICY_NAME);
		return query.getSingleResult();
	}

	@Override
	public String start() throws Exception {

		// 1. Create Radius client

		setInfo("Creating Radius Client");
		RadiusClientEntity clientEntity = radiusClientLogic.getRadiusClientName(CLIENT_NAME);
		if (clientEntity == null) {
			clientEntity = new RadiusClientEntity();
			clientEntity.setIpNumber(CLIENT_IP);
			clientEntity.setName(CLIENT_NAME);
			clientEntity.setSharedSecret(DEFAULT_SHARED_SECRET);
			clientEntity.setUseChallenge(true);
			radiusClientLogic.add(clientEntity);
			clientEntity = radiusClientLogic.getRadiusClientName(CLIENT_NAME);
			Exception exception = DcemUtils.reloadTaskNodes(RadiusModule.class);
			if (exception == null) {
				setInfo("Error while reloading task nodes " + exception.toString());
				return null;
			}
		}

		// 2. Create Policy and Apply

		setInfo("Creating Policy");
		PolicyEntity policyEntity;
		try {
			policyEntity = getPolicyEntity();
		} catch (NoResultException e) { // not found
			try {
				DcemPolicy dcemPolicy = new DcemPolicy();
				dcemPolicy.setAllowedMethods(ALLOWED_AUTH_METHODS);

				policyEntity = new PolicyEntity();
				policyEntity.setName(POLICY_NAME);
				policyEntity.setDcemPolicy(dcemPolicy);

				DcemAction action = new DcemAction();
				action.setAction(DcemConstants.ACTION_ADD);
				policyLogic.addOrUpdatePolicy(policyEntity, action, false);

				policyEntity = getPolicyEntity();
				PolicyAppEntity pae = policyLogic.getDetachedPolicyApp(AuthApplication.RADIUS, clientEntity.getId());
				policyLogic.assignPolicy(new PolicyTreeItem(pae, null), 0, policyEntity.getId(), 0);

				Exception exception = DcemUtils.reloadTaskNodes(RadiusModule.class);
				if (exception == null) {
					setInfo("Error while reloading task nodes " + exception.toString());
					return null;
				}
			} catch (Exception e2) {
				setInfo("Error while creating Policy: " + e2.getLocalizedMessage());
				return null;
			}
		} catch (Exception e) { // unexpected error
			setInfo("Error while finding Policy: " + e.toString());
			return null;
		}

		// 3. Send Request

		RadiusClient rc = new RadiusClient(clientEntity.getIpNumber(), clientEntity.getSharedSecret());
		rc.setAuthPort(testModule.getPreferences().getRadiusPort());
		AccessRequest ar = new AccessRequest(activateLoginTest.getUser(), userWithActivationTest.getInitialPassword().getBytes());
		ar.setAuthProtocol(AccessRequest.AUTH_PAP); // or AUTH_CHAP
		ar.addAttribute("NAS-Identifier", "nas-identifier.de");
		ar.addAttribute("NAS-IP-Address", "192.168.2.127");
		ar.addAttribute("Service-Type", "Login-User");

		setInfo("Sending Valid Request");
		RadiusPacket response = rc.authenticate(ar);
		if (response.getRadiusPacketType() != RadiusPacketType.ACCESS_CHALLENGE) {
			throw new Exception("Waiting for Radius Challenge: Invalid Packet Type received.");
		}

		setInfo("Awaiting Message Response");
		waitFor(SdkListenerMethods.onReceiveMessage, 4000);
		ReceivedMessage receivedMessage = appSdkListnerImplSync.getReceivedMessage();
		ResponseMessage responseMessage = new ResponseMessage(receivedMessage.getId(), AsErrorCodes.OK, null);
		responseMessage.setActionId("ok");
		appSdkImplSync.sendMessageResponse(responseMessage);

		RadiusAttribute attr = response.getAttribute(RadiusAttributeEnum.State);
		ar.addAttribute(attr);
		response = rc.authenticate(ar);
		if (response.getRadiusPacketType() != RadiusPacketType.ACCESS_ACCEPT) {
			throw new Exception("Waiting for Radius Accept: Invalid Packet Type received.");
		}

		setInfo("Sending Wrong Password");
		ar = new AccessRequest(activateLoginTest.getUser(), "wrong password".getBytes());
		response = rc.authenticate(ar);
		if (response.getRadiusPacketType() != RadiusPacketType.ACCESS_REJECT) {
			throw new Exception("Waiting for Radius Reject: Invalid Packet Type received.");
		}

		setInfo("Sending Wrong Username");
		ar = new AccessRequest("wrong user", userWithActivationTest.getInitialPassword().getBytes());
		response = rc.authenticate(ar);
		if (response.getRadiusPacketType() != RadiusPacketType.ACCESS_REJECT) {
			throw new Exception("Waiting for Radius Reject: Invalid Packet Type received.");
		}

		// // 2. Send Accounting-Request
		// AccountingRequest acc = new AccountingRequest("mw", AccountingRequest.ACCT_STATUS_TYPE_START);
		// acc.addAttribute("Acct-Session-Id", "1234567890");
		// acc.addAttribute("NAS-Identifier", "this.is.my.nas-identifier.de");
		// acc.addAttribute("NAS-Port", "0");
		//
		// System.out.println(acc + "\n");
		// response = rc.account(acc);

		setInfo("OK, :-)");

		return null;
	}
}
