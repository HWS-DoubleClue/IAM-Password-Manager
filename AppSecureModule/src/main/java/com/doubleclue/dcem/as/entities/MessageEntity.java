package com.doubleclue.dcem.as.entities;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.as.restapi.model.AsApiMsgStatus;
import com.doubleclue.dcem.core.entities.DcemTemplate;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.outofscope.DbJsonMapConverter;
import com.doubleclue.dcem.core.utils.DisplayModes;

/**
 * 
 * @author Emanuel Galea
 */
@Entity
@Table(name = "as_message")
@NamedQueries({ @NamedQuery(name = MessageEntity.DELETE_USER_MSG, query = "DELETE FROM MessageEntity me where me.user = ?1"),
		@NamedQuery(name = MessageEntity.DELETE_DEVICE_MSG, query = "DELETE FROM MessageEntity me where me.device = ?1"),
		@NamedQuery(name = MessageEntity.GET_AFTER, query = "SELECT rp FROM MessageEntity rp where rp.createdOn < ?1"),
		@NamedQuery(name = MessageEntity.DELETE_AFTER, query = "DELETE FROM MessageEntity rp where rp.createdOn < ?1"),

})

public class MessageEntity extends EntityInterface implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String DELETE_USER_MSG = "MessageEntity.deleteUserMsg";
	public static final String GET_AFTER = "MessageEntity.getAfter";
	public static final String DELETE_AFTER = "MessageEntity.deleteAfter";
	public static final String DELETE_DEVICE_MSG = "MessageEntity.deleteDeviceMsg";

	@Id
	@Column(name = "dc_id")
	// Unique Id is generated by Hazelcast
	@DcemGui
	private long id;

	@Column(nullable = false)
	@DcemGui(sortOrder = SortOrder.DESCENDING)
	private LocalDateTime createdOn;

	@ManyToOne
	@JoinColumn(name = "userId", nullable = false, foreignKey = @ForeignKey(name = "FK_APP_MSG_USER"), insertable = true, updatable = false)
	@DcemGui(subClass = "loginId", name = "user")
	private DcemUser user;

	@DcemGui(name = "device", subClass = "name")
	@ManyToOne
	@JoinColumn(nullable = true, foreignKey = @ForeignKey(name = "FK_APP_MSG_DEVICE"), insertable = true, updatable = false)
	private DeviceEntity device;

	@ManyToOne
	@JoinColumn(nullable = true, foreignKey = @ForeignKey(name = "FK_APP_MSG_TEMPLATE"), insertable = true, updatable = false)
	@DcemGui(name = "template", subClass = "name")
	private DcemTemplate template;

	// @DcemGui(name="template")
	// private String templateName; // used incase Tempalte is not known

	@Column(nullable = false)
	@DcemGui(name = "withResponse")
	private boolean responseRequired;

	@Column(nullable = false)
	@DcemGui
	boolean signed;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "dc_status", nullable = false)
	@DcemGui(name = "finalStatus")
	AsApiMsgStatus msgStatus;

	@Column(name = "actionId", length = 64, nullable = true)
	@DcemGui(name = "actionId")
	String actionId;

	@DcemGui
	boolean retrieved;

	@Column(name = "info", nullable = true, length = 255)
	@DcemGui
	String msgInfo;

	@Column(length = 4096)
	@Convert(converter = DbJsonMapConverter.class)
	@DcemGui(displayMode = DisplayModes.NONE)
	HashMap<String, String> outputData;

	@Convert(converter = DbJsonMapConverter.class)
	@Column(length = 4096)
	@DcemGui(displayMode = DisplayModes.NONE)
	HashMap<String, String> responseData;

	@ManyToOne
	@JoinColumn(name = "operatorId", foreignKey = @ForeignKey(name = "FK_MSG_OPERATOR"), nullable = true, insertable = true, updatable = false)
	@DcemGui(subClass = "displayName")
	DcemUser operator;

	@ManyToOne
	@JoinColumn(name = "policyAppId", foreignKey = @ForeignKey(name = "FK_MSG_POLICYAPP"), nullable = true, insertable = true, updatable = false)
	@DcemGui
	PolicyAppEntity policyAppEntity;

	byte[] signature;

	@Transient
	int responseTime;

	public MessageEntity() {
	}

	public MessageEntity(long id, DcemUser user, DcemTemplate template) {
		this.id = id;
		this.user = user;
		this.template = template;
		createdOn = LocalDateTime.now();
	}

	public DcemUser getUser() {
		return user;
	}

	public void setUser(DcemUser user) {
		this.user = user;
	}

	public DeviceEntity getDevice() {
		return device;
	}

	public void setDevice(DeviceEntity device) {
		this.device = device;
	}

	public DcemTemplate getTemplate() {
		return template;
	}

	public void setTemplate(DcemTemplate template) {
		this.template = template;
	}

	public LocalDateTime getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(LocalDateTime createdOn) {
		this.createdOn = createdOn;
	}

	public String getMsgInfo() {
		return msgInfo;
	}

	public void setMsgInfo(String msgInfo) {
		this.msgInfo = msgInfo;
	}

	public byte[] getSignature() {
		return signature;
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
	}

	public int getResponseTime() {
		return responseTime;
	}

	public void setResponseTime(int responseTime) {
		this.responseTime = responseTime;
	}

	public boolean isResponseRequired() {
		return responseRequired;
	}

	public void setResponseRequired(boolean responseRequired) {
		this.responseRequired = responseRequired;
	}

	public boolean isRetrieved() {
		return retrieved;
	}

	public void setRetrieved(boolean retrieved) {
		this.retrieved = retrieved;
	}

	public AsApiMsgStatus getMsgStatus() {
		return msgStatus;
	}

	public void setMsgStatus(AsApiMsgStatus msgStatus) {
		this.msgStatus = msgStatus;
	}

	public String getActionId() {
		return actionId;
	}

	public void setActionId(String actionId) {
		this.actionId = actionId;
	}

	public boolean isSigned() {
		return signed;
	}

	public void setSigned(boolean signed) {
		this.signed = signed;
	}

	public Map<String, String> getOutputData() {
		return outputData;
	}

	public void setOutputData(HashMap<String, String> outputData) {
		this.outputData = outputData;
	}

	public Map<String, String> getResponseData() {
		return responseData;
	}

	public void setResponseData(HashMap<String, String> responseData) {
		this.responseData = responseData;
	}

	public PolicyAppEntity getPolicyAppEntity() {
		return policyAppEntity;
	}

	public void setPolicyAppEntity(PolicyAppEntity policyAppEntity) {
		this.policyAppEntity = policyAppEntity;
	}

	@Override
	public Number getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = (long) id;
	}

	public DcemUser getOperator() {
		return operator;
	}

	public void setOperator(DcemUser operator) {
		this.operator = operator;
	}

}