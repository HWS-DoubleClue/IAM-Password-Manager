package com.doubleclue.dcem.otp.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;

import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.jpa.DbEncryptConverterBinary;
import com.doubleclue.dcem.otp.logic.OtpTypes;

/**
 * The persistent class for logging
 * 
 * @author Emanuel Galea
 * 
 */
@Entity
@Table(name = "otp_token", uniqueConstraints= @UniqueConstraint(name="UK_OTP_SERIAL", columnNames={"serialNumber"}))
@NamedQueries({
	@NamedQuery(name=OtpTokenEntity.GET_TOKEN_BY_SERIAL_NO,
			query="SELECT ot FROM OtpTokenEntity ot where ot.serialNumber = ?1"),
	@NamedQuery(name=OtpTokenEntity.GET_USER_TOKENS,
	query="SELECT ot FROM OtpTokenEntity ot where ot.user = ?1 AND ot.disabled = false"),
	@NamedQuery(name=OtpTokenEntity.GET_ALL_USER_TOKENS,
	query="SELECT ot FROM OtpTokenEntity ot where ot.user =?1"),
	@NamedQuery(name=OtpTokenEntity.GET_DISABLED_USER_TOKENS,
	query="SELECT ot FROM OtpTokenEntity ot where ot.user = ?1 AND ot.disabled = true" ),
	
//	@NamedQuery(name = OtpTokenEntity.GET_TOKEN_COUNT, query = 
//			"SELECT COUNT(*) FROM OtpTokenEntity d WHERE d.lastUsed > ?1"),
	
})

public class OtpTokenEntity extends EntityInterface {
	
	public final static String GET_TOKEN_BY_SERIAL_NO = "OtpTokenEntity.getTokenBySerialNo";
	public final static String GET_USER_TOKENS = "OtpTokenEntity.getUserTokens";
	public final static String GET_ALL_USER_TOKENS = "OtpTokenEntitiy.getAllUserTokens";
	public final static String GET_DISABLED_USER_TOKENS = "OtpTokenEntitiy.getDisabledState";
//	public static final String GET_TOKEN_COUNT = "OtpTokenEntity.getTokenCount";

	public OtpTokenEntity() {

	}
	
	
	
	public OtpTokenEntity(OtpTypes otpType, String serialNumber, byte[] secretKey) {
		super();
		this.otpType = otpType;
		this.serialNumber = serialNumber;
		this.secretKey = secretKey;
	}



	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqStoreOtpToken", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "OPT_TOKEN.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 20)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreOtpToken")
	// @DcemGui (columnWidth="45px", displayMode=DisplayModes.INPUT_DISABLED)
	private Integer id;


	@DcemGui
	@Enumerated(EnumType.ORDINAL)
	@Column(nullable = false)
	private OtpTypes otpType;
	
	@DcemGui
	@Column(nullable = false)
	String serialNumber;
	
	@DcemGui(name = "assignedTo", subClass = "loginId")
	@ManyToOne
	@JoinColumn(nullable = true, name="userId", foreignKey = @ForeignKey(name = "FK_OTP_TOKEN_USER"), insertable = true, updatable = true)
	private DcemUser user;
	
//	@Temporal(TemporalType.TIMESTAMP)
//	@Column(name = "lastUsed", nullable = true)
//	@DcemGui
//	private Date lastUsed;
	
	@DcemGui
	@Column(name = "dc_disabled")
	boolean disabled;

	@Column(length = 255, nullable = true)
	@DcemGui
	private String info;
	
	int counter;		// incase of HOTP
	
	@Column(nullable = false)
	@Convert(converter = DbEncryptConverterBinary.class)
	byte [] secretKey;

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = (Integer) id;		
	}

	public OtpTypes getOtpType() {
		return otpType;
	}

	public void setOtpType(OtpTypes otpType) {
		this.otpType = otpType;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}


	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public byte[] getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(byte[] secretKey) {
		this.secretKey = secretKey;
	}


	@Override
	public String toString() {
		return "OtpTokenEntity [otpType=" + otpType + ", serialNumber=" + serialNumber + ", dcemUser=" + user + "]";
	}



	public DcemUser getUser() {
		return user;
	}



	public void setUser(DcemUser assignedTo) {
		this.user = assignedTo;
	}


	

}