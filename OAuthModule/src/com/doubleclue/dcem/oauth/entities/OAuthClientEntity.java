package com.doubleclue.dcem.oauth.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.oauth.logic.OAuthIdpSettings;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("serial")
@Entity
@Table(name = "oauth_client", uniqueConstraints = { @UniqueConstraint(name = "UK_OAUTH_CLIENT_ENTITYID", columnNames = { "client_id" }),
		@UniqueConstraint(name = "UK_OAUTH_CLIENT_DISPLAY_NAME", columnNames = { "display_name" }) })
@NamedQueries({ @NamedQuery(name = OAuthClientEntity.GET_CLIENT_BY_ID, query = "SELECT c FROM OAuthClientEntity c where c.id = ?1 AND c.disabled=false"),
		@NamedQuery(name = OAuthClientEntity.GET_CLIENT_BY_CLIENT_ID, query = "SELECT c FROM OAuthClientEntity c where c.clientId = ?1 AND c.disabled=false"),
		@NamedQuery(name = OAuthClientEntity.GET_ALL_CLIENTS, query = "SELECT c FROM OAuthClientEntity c where c.disabled=false") })
public class OAuthClientEntity extends EntityInterface implements Serializable {

	private static final Logger logger = LogManager.getLogger(OAuthClientEntity.class);

	public static final String GET_CLIENT_BY_ID = "oauthClient.getById";
	public static final String GET_CLIENT_BY_CLIENT_ID = "oauthClient.getByClientId";
	public static final String GET_ALL_CLIENTS = "oauthClient.getAll";

	@Id
	@Column(name = "dc_id")
	@TableGenerator(name = "coreSeqOauthClient", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "OAUTH_CLIENT.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqOauthClient")
	private Integer id;

	@Column(name = "dc_metadata")
	@Lob
	private String metadata;

	@Column(name = "display_name", nullable = false)
	@DcemGui
	private String displayName;

	@Column(name = "client_id", nullable = false)
	@DcemGui
	private String clientId;

	@Column(name = "client_secret", nullable = false)
	private String clientSecret;

	@Column(name = "dc_disabled", nullable = false)
	@DcemGui
	private boolean disabled = false;

	@Column(name = "redirect_uris")
	private String redirectUris;

	@Column(name = "idp_settings", length = 4096)
	private String idpSettingsJson;

	@Transient
	private OAuthIdpSettings idpSettings;

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

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public String getRedirectUris() {
		return redirectUris;
	}

	public void setRedirectUris(String redirectUris) {
		this.redirectUris = redirectUris;
	}

	public String[] getRedirectUriArray() {
		return (redirectUris != null && !redirectUris.isEmpty()) ? redirectUris.replaceAll("\\s+", "").split(",") : new String[0];
	}

	public String getIdpSettingsJson() {
		return idpSettingsJson;
	}

	public void setIdpSettingsJson(String idpSettingsJson) {
		this.idpSettingsJson = idpSettingsJson;
		idpSettings = null;
	}

	public OAuthIdpSettings getIdpSettings() {
		if (idpSettings == null) {
			idpSettings = new OAuthIdpSettings();
			if (idpSettingsJson != null && !idpSettingsJson.isEmpty()) {
				try {
					TypeReference<OAuthIdpSettings> typeRef = new TypeReference<OAuthIdpSettings>() {
					};
					ObjectMapper objectMapper = new ObjectMapper();
					idpSettings = objectMapper.readValue(idpSettingsJson, typeRef);
				} catch (Exception e) {
					logger.warn("Couldn't deserialize OAuth IdP Settings", e);
				}
			}
		}
		return idpSettings;
	}

	public void setIdpSettings(OAuthIdpSettings idpSettings) {
		this.idpSettings = idpSettings;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			idpSettingsJson = objectMapper.writeValueAsString(idpSettings);
		} catch (Exception e) {
			logger.warn("Couldn't serialize OAuth IdP Settings", e);
		}
	}
}
