package com.doubleclue.dcem.as.entities;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
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

@Entity
@Table(name = "as_fido_authenticator", indexes = {
		@Index(name = "FIDO_AUTH_CREDENTIAL_ID_INDEX", columnList = "credentialId") }, uniqueConstraints = @UniqueConstraint(name = "UK_USER_CREDENTIAL_ID", columnNames = {
				"userId", "credentialId" }))
@NamedQueries({ @NamedQuery(name = FidoAuthenticatorEntity.GET_AUTHENTICATOR_BY_ID, query = "SELECT fido FROM FidoAuthenticatorEntity fido WHERE fido.id = ?1"),
		@NamedQuery(name = FidoAuthenticatorEntity.GET_AUTHENTICATORS_BY_USER, query = "SELECT fido FROM FidoAuthenticatorEntity fido WHERE fido.user = ?1"),
		@NamedQuery(name = FidoAuthenticatorEntity.GET_AUTHENTICATORS_BY_CREDENTIAL_ID, query = "SELECT fido FROM FidoAuthenticatorEntity fido WHERE fido.credentialId = ?1"),
		@NamedQuery(name = FidoAuthenticatorEntity.GET_AUTHENTICATOR_BY_USER_AND_CREDENTIAL_ID, query = "SELECT fido FROM FidoAuthenticatorEntity fido WHERE fido.user = ?1 AND fido.credentialId = ?2") })
public class FidoAuthenticatorEntity extends EntityInterface {

	public static final String GET_AUTHENTICATOR_BY_ID = "FidoAuthenticatorEntity.authenticatorById";
	public static final String GET_AUTHENTICATORS_BY_USER = "FidoAuthenticatorEntity.authenticatorsByUser";
	public static final String GET_AUTHENTICATORS_BY_CREDENTIAL_ID = "FidoAuthenticatorEntity.authenticatorsByCredentialID";
	public static final String GET_AUTHENTICATOR_BY_USER_AND_CREDENTIAL_ID = "FidoAuthenticatorEntity.authenticatorByUserAndCredentialID";

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqStoreFido", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "FIDO.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreFido")
	private Integer id;

	@DcemGui(name = "user", subClass = "loginId")
	@ManyToOne
	@JoinColumn(nullable = false, name = "userId", foreignKey = @ForeignKey(name = "FK_FIDO_USER"), insertable = true, updatable = false)
	private DcemUser user;

	@DcemGui
	@Column(name = "display_name", length = 255, nullable = false)
	private String displayName;

	@Column(updatable = false, nullable = false)
	private String credentialId;

	@Column(length = 1024, updatable = false, nullable = false)
	private byte[] publicKey;

	@Column(updatable = false, nullable = false)
	private boolean passwordless = false;

	@Column(updatable = false, nullable = false)
	@DcemGui
	private LocalDateTime registeredOn;


	@Column(nullable = false)
	@DcemGui
	private LocalDateTime lastUsed;

	@Override
	public String toString() {
		return user.getLoginId() + " - registered " + getRegisteredOn();
	}

	@Override
	public Number getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public DcemUser getUser() {
		return user;
	}

	public void setUser(DcemUser user) {
		this.user = user;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getCredentialId() {
		return credentialId;
	}

	public void setCredentialId(String credentialId) {
		this.credentialId = credentialId;
	}

	public byte[] getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}

	public boolean isPasswordless() {
		return passwordless;
	}

	public void setPasswordless(boolean passwordless) {
		this.passwordless = passwordless;
	}

	public LocalDateTime getRegisteredOn() {
		return registeredOn;
	}

	public void setRegisteredOn(LocalDateTime registeredOn) {
		this.registeredOn = registeredOn;
		this.lastUsed = registeredOn;
	}

	public LocalDateTime getLastUsed() {
		return lastUsed;
	}

	public void setLastUsed(LocalDateTime lastUsed) {
		this.lastUsed = lastUsed;
	}
}
