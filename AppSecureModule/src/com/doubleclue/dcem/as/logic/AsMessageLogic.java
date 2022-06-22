package com.doubleclue.dcem.as.logic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.doubleclue.dcem.as.entities.DeviceEntity;
import com.doubleclue.dcem.as.entities.MessageEntity;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.TemplateLogic;
import com.doubleclue.dcem.core.logic.UserLogic;

@ApplicationScoped
public class AsMessageLogic {

	// private static Logger logger = LogManager.getLogger(AsMessageLogic.class);

	@Inject
	EntityManager em;

	@Inject
	AsModule asModule;

	@Inject
	TemplateLogic templateLogic;

	@Inject
	UserLogic userLogic;

//	public Long getLastId() {
//		TypedQuery<Long> query = em.createQuery("SELECT msg.id FROM MessageEntity msg ORDER BY msg.id DESC", Long.class);
//		query.setMaxResults(1);
//		try {
//			return query.getSingleResult();
//		} catch (NoResultException exp) {
//			return null;
//		}
//
//	}


	@DcemTransactional
	public void addUserMsg(PendingMsg pendingMsg, boolean retrieved) {
		AsPreferences preferences = asModule.getPreferences();
		if (preferences.getMessageStorePolicy() == null || 	preferences.getMessageStorePolicy() == MsgStoragePolicy.Dont_Store) {
			return;
		}
		DcemTemplate template = null;
		if (pendingMsg.getTemplateId() > 0) {
			template = templateLogic.getTemplate(pendingMsg.getTemplateId());
		}

		DcemUser user = new DcemUser();
		user.setId(pendingMsg.getUserId());
		DeviceEntity device = null;
		if (pendingMsg.getDeviceId() > 0) {
			device = new DeviceEntity();
			device.setId(pendingMsg.getDeviceId());
		}

		MessageEntity messageEntity = new MessageEntity(pendingMsg.getId(), user, template);
		switch (preferences.getMessageStorePolicy()) {
		case Data_To_Device:
			messageEntity.setOutputData(pendingMsg.getOutputData());
			break;
		case Data_From_Device:
			messageEntity.setResponseData(pendingMsg.getResponseData());
			break;
		case Both_Data:
			messageEntity.setOutputData(pendingMsg.getOutputData());
			messageEntity.setResponseData(pendingMsg.getResponseData());
			break;
		case No_Data:
		default:
			break;
		}
		messageEntity.setRetrieved(retrieved);
		messageEntity.setResponseRequired(pendingMsg.isResponseRequired());
		messageEntity.setDevice(device);
		messageEntity.setMsgStatus(pendingMsg.getMsgStatus());
		messageEntity.setMsgInfo(pendingMsg.getInfo());
		messageEntity.setActionId(pendingMsg.getActionId());
		messageEntity.setSigned(pendingMsg.isSignitureRequired());
		if (pendingMsg.getOperatorId() > 0) {
			messageEntity.setOperator(new DcemUser(pendingMsg.getOperatorId()));
		}
		messageEntity.setPolicyAppEntity(pendingMsg.getPolicyTransaction().getPolicyAppEntity());
//		System.out.println("AsMessageLogic.add() " + messageEntity.getId());
		em.persist(messageEntity);
	}

	public void deleteUserMsg(DcemUser dcemUser) {
			Query query = em.createNamedQuery(MessageEntity.DELETE_USER_MSG);
			query.setParameter(1, dcemUser);
			query.executeUpdate();		
	}

	public void deleteDeviceMsg(DeviceEntity device) {
		Query query = em.createNamedQuery(MessageEntity.DELETE_DEVICE_MSG);
		query.setParameter(1, device);
		query.executeUpdate();
		
	}

}
