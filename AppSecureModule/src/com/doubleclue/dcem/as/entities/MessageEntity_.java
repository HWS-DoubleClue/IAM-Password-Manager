package com.doubleclue.dcem.as.entities;

import com.doubleclue.dcem.as.restapi.model.AsApiMsgStatus;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.DcemUser;
import java.util.Date;
import java.util.HashMap;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="Dali", date="2020-11-06T08:53:22.570+0100")
@StaticMetamodel(MessageEntity.class)
public class MessageEntity_ {
	public static volatile SingularAttribute<MessageEntity, Long> id;
	public static volatile SingularAttribute<MessageEntity, Date> createdOn;
	public static volatile SingularAttribute<MessageEntity, DcemUser> user;
	public static volatile SingularAttribute<MessageEntity, DeviceEntity> device;
	public static volatile SingularAttribute<MessageEntity, DcemTemplate> template;
	public static volatile SingularAttribute<MessageEntity, Boolean> responseRequired;
	public static volatile SingularAttribute<MessageEntity, Boolean> signed;
	public static volatile SingularAttribute<MessageEntity, AsApiMsgStatus> msgStatus;
	public static volatile SingularAttribute<MessageEntity, String> actionId;
	public static volatile SingularAttribute<MessageEntity, Boolean> retrieved;
	public static volatile SingularAttribute<MessageEntity, String> msgInfo;
	public static volatile SingularAttribute<MessageEntity, HashMap> outputData;
	public static volatile SingularAttribute<MessageEntity, HashMap> responseData;
	public static volatile SingularAttribute<MessageEntity, DcemUser> operator;
	public static volatile SingularAttribute<MessageEntity, PolicyAppEntity> policyAppEntity;
	public static volatile SingularAttribute<MessageEntity, byte[]> signature;
}
