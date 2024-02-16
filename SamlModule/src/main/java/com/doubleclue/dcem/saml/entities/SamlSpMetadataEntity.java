package com.doubleclue.dcem.saml.entities;

import java.io.Serializable;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.saml.logic.SamlIdpSettings;
import com.doubleclue.dcem.saml.logic.SamlUtils;
import com.doubleclue.dcem.saml.logic.enums.NameIdFormatEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("serial")
@Entity
// @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE) We make here our own cache
@Table(name = "saml_sp_metadata", uniqueConstraints = { @UniqueConstraint(name = "UK_SP_METADATA_ENTITYID", columnNames = { "entityId" }),
		@UniqueConstraint(name = "UK_SP_METADATA_DISPLAY_NAME", columnNames = { "display_name" }) })
@NamedQueries({
		@NamedQuery(name = SamlSpMetadataEntity.GET_SP_METADATA_BY_ENTITY_ID, query = "SELECT spm FROM SamlSpMetadataEntity spm where spm.entityId = ?1 AND spm.disabled=false", hints = {
				@QueryHint(name = "org.hibernate.cacheable", value = "true"),
				@QueryHint(name = "org.hibernate.cacheRegion", value = SamlSpMetadataEntity.GET_SP_METADATA_BY_ENTITY_ID) }),
		@NamedQuery(name = SamlSpMetadataEntity.GET_ALL_SP_METADATA, query = "SELECT spm FROM SamlSpMetadataEntity spm WHERE spm.disabled=false", hints = {
				@QueryHint(name = "org.hibernate.cacheable", value = "true"),
				@QueryHint(name = "org.hibernate.cacheRegion", value = SamlSpMetadataEntity.GET_ALL_SP_METADATA) }) })

public class SamlSpMetadataEntity extends EntityInterface implements Serializable {

	private static final Logger logger = LogManager.getLogger(SamlSpMetadataEntity.class);

	public static final String GET_SP_METADATA_BY_ENTITY_ID = "spMetadata.getByEntityId";
	public static final String GET_ALL_SP_METADATA = "spMetadata.getAll";

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqSamlSpMeta", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "SAML_SP_METADATA.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqSamlSpMeta")
	private Integer id;

	@Column(name = "dc_metadata")
	@Lob
	private String metadata;

	@Column(name = "display_name", nullable = false)
	@DcemGui
	private String displayName;

	@Column(name = "entityId", nullable = false)
	@DcemGui
	private String entityId;

	@Column(name = "acs_location", nullable = false)
	@DcemGui
	private String acsLocation;

	@Column(name = "certificateString")
	@Lob
	private String certificateString;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "name_id_format", nullable = false)
	private NameIdFormatEnum nameIdFormat;

	@Column(name = "dc_disabled", nullable = false)
	@DcemGui
	private boolean disabled = false;

	@Column(name = "requests_signed", nullable = false)
	private boolean requestsSigned = true;

	@Column(name = "logout_location")
	private String logoutLocation;

	@Column(name = "logout_is_post", nullable = false)
	private boolean logoutIsPost = true;

	@Column(name = "idp_settings", length = 4096)
	private String idpSettingsJson;

	@Transient
	private Certificate certificate;

	@Transient
	private SamlIdpSettings idpSettings;

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public String getAcsLocation() {
		return acsLocation;
	}

	public void setAcsLocation(String acsLocation) {
		this.acsLocation = acsLocation;
	}

	public String getCertificateString() {
		return certificateString;
	}

	public void setCertificateString(String certificateString) throws CertificateException {
		this.certificate = SamlUtils.getX509CertificateFromBase64String(certificateString);
		this.certificateString = certificateString;
	}

	public Certificate getCertificate() throws CertificateException {
		if (certificate == null) {
			certificate = SamlUtils.getX509CertificateFromBase64String(getCertificateString());
		}
		return certificate;
	}

	public NameIdFormatEnum getNameIdFormat() {
		return nameIdFormat;
	}

	public void setNameIdFormat(NameIdFormatEnum nameIdFormat) {
		this.nameIdFormat = nameIdFormat;
	}

	@Override
	public String toString() {
		return getDisplayName();
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disable) {
		this.disabled = disable;
	}

	public boolean isRequestsSigned() {
		return requestsSigned;
	}

	public void setRequestsSigned(boolean requestsSigned) {
		this.requestsSigned = requestsSigned;
	}

	public String getLogoutLocation() {
		return logoutLocation;
	}

	public void setLogoutLocation(String logoutLocation) {
		this.logoutLocation = logoutLocation;
	}

	public boolean isLogoutIsPost() {
		return logoutIsPost;
	}

	public void setLogoutIsPost(boolean logoutIsPost) {
		this.logoutIsPost = logoutIsPost;
	}

	public String getIdpSettingsJson() {
		return idpSettingsJson;
	}

	public void setIdpSettingsJson(String idpSettingsJson) {
		this.idpSettingsJson = idpSettingsJson;
		idpSettings = null;
	}

	public SamlIdpSettings getIdpSettings() {
		if (idpSettings == null) {
			idpSettings = new SamlIdpSettings();
			if (idpSettingsJson != null && !idpSettingsJson.isEmpty()) {
				try {
					TypeReference<SamlIdpSettings> typeRef = new TypeReference<SamlIdpSettings>() {
					};
					ObjectMapper objectMapper = new ObjectMapper();
					idpSettings = objectMapper.readValue(idpSettingsJson, typeRef);
				} catch (Exception e) {
					logger.warn("Couldn't deserialize SP IdP Settings", e);
				}
			}
		}
		return idpSettings;
	}

	public void setIdpSettings(SamlIdpSettings idpSettings) {
		this.idpSettings = idpSettings;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			idpSettingsJson = objectMapper.writeValueAsString(idpSettings);
		} catch (Exception e) {
			logger.warn("Couldn't serialize SP IdP Settings", e);
		}
	}

	public void copyEntity(SamlSpMetadataEntity entity) throws CertificateException {
		setAcsLocation(entity.getAcsLocation());
		setCertificateString(entity.getCertificateString());
		setDisabled(entity.isDisabled());
		setDisplayName(entity.getDisplayName());
		setEntityId(entity.getEntityId());
		setIdpSettingsJson(entity.getIdpSettingsJson());
		setLogoutIsPost(entity.isLogoutIsPost());
		setLogoutLocation(entity.getLogoutLocation());
		setMetadata(entity.getMetadata());
		setNameIdFormat(entity.getNameIdFormat());
		setRequestsSigned(entity.isRequestsSigned());
	}

	public boolean isAzure() {
		return entityId.equals("urn:federation:MicrosoftOnline");
	}

	@Override
	public String getRowStyle() {
		// TODO Auto-generated method stub
		return null;
	}
}
