package com.doubleclue.dcem.radius.entities;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.SupportedCharsets;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.validators.IpNumberValidator;
import com.doubleclue.dcem.core.jpa.DbEncryptConverter;
import com.doubleclue.dcem.core.utils.DisplayModes;
import com.doubleclue.dcem.radius.logic.RadiusClientSettings;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * The persistent class for logging
 * @author Emanuel Galea
 * 
 */
@Entity
@Table(name="radius_client", uniqueConstraints = @UniqueConstraint(name = "UK_RADIUS_IPNUMBER", columnNames = {
"ipNumber" }) )
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NamedQueries({
	@NamedQuery(name = RadiusClientEntity.GET_CLIENT_BY_IPNUMBER, query = "SELECT rc FROM RadiusClientEntity rc where rc.ipNumber = ?1", hints = {
			@QueryHint(name = "org.hibernate.cacheable", value = "true"),
			@QueryHint(name = "org.hibernate.cacheRegion", value = RadiusClientEntity.GET_CLIENT_BY_IPNUMBER) }),
	@NamedQuery(name = RadiusClientEntity.GET_CLIENT_BY_NAME, query = "SELECT rc FROM RadiusClientEntity rc where rc.name = ?1", hints = {
			@QueryHint(name = "org.hibernate.cacheable", value = "true"),
			@QueryHint(name = "org.hibernate.cacheRegion", value = RadiusClientEntity.GET_CLIENT_BY_NAME) }),
	@NamedQuery(name = RadiusClientEntity.GET_ALL_CLIENTS, query = "SELECT rc FROM RadiusClientEntity rc" )

})


public class RadiusClientEntity extends EntityInterface {
	
	public final static String GET_CLIENT_BY_IPNUMBER = "radiusClient.getClientByIpNumber";
	public final static String GET_CLIENT_BY_NAME = "radiusClient.getClientByName";
	public final static String GET_ALL_CLIENTS = "radiusClient.getAllClients";
	
	private static Logger logger = LogManager.getLogger(RadiusClientEntity.class);

	
	@Id
	@Column(name = "dc_id")
	@TableGenerator( name = "coreSeqStoreRadiusClient", table = "core_seq", pkColumnName = "seq_name", pkColumnValue="RADIUS_CLIENT.ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1 )
    @GeneratedValue( strategy = GenerationType.TABLE, generator = "coreSeqStoreRadiusClient" )
//	@DcemGui (columnWidth="45px", displayMode=DisplayModes.INPUT_DISABLED)
    private Integer id;
	
    @Column(name = "name", nullable=false)
    @DcemGui (sortOrder=SortOrder.ASCENDING )
    @Size(min = 2, max = 64)
	private String name;	

    @Column(nullable=false)
    @DcemGui
    @IpNumberValidator
	private String ipNumber;	
 
    @DcemGui (password = true, displayMode = DisplayModes.INPUT_ONLY)
    @Column(length = 128, nullable = false)
    @Convert(converter = DbEncryptConverter.class)
    @Size(min = 4, max = 64)
    private String sharedSecret;
    
    @DcemGui (help="If the challenge response is not used, the access request timeout should be long enough to give the user time to respond.")
    boolean useChallenge = false;
    
    @DcemGui (help="The RADIUS password will be ignored.")
    boolean ignoreUsersPassword = false;
    
	@Column(name = "settingsJson", length = 4096)
	private String settingsJson;
	
	public String getSettingsJson() {
		return settingsJson;
	}

	public void setSettingsJson(String settingsJson) {
		this.settingsJson = settingsJson;
	}

	@Transient
	private RadiusClientSettings radiusClientSettings;
    
    @Transient
    String tenantName;
    
   
	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Number id) {
		this.id = (Integer) id;		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIpNumber() {
		return ipNumber;
	}

	public void setIpNumber(String ipNumber) {
		this.ipNumber = ipNumber;
	}

	public String getSharedSecret() {
		return sharedSecret;
	}

	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}

	@Override
	public String toString() {
		return "RadiusClientEntity [name=" + name + ", ipNumber=" + ipNumber + "]";
	}

	public boolean isUseChallenge() {
		return useChallenge;
	}

	public void setUseChallenge(boolean useChallenge) {
		this.useChallenge = useChallenge;
	}

	public boolean isIgnoreUsersPassword() {
		return ignoreUsersPassword;
	}

	public void setIgnoreUsersPassword(boolean ignoreUsersPassword) {
		this.ignoreUsersPassword = ignoreUsersPassword;
	}

	public String getTenantName() {
		return tenantName;
	}

	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}
	
	public RadiusClientSettings getRadiusClientSettings() {
		if (radiusClientSettings == null) {
			radiusClientSettings = new RadiusClientSettings();
			if (settingsJson != null && settingsJson.isEmpty() == false) {
				try {
					TypeReference<RadiusClientSettings> typeRef = new TypeReference<RadiusClientSettings>() {
					};
					ObjectMapper objectMapper = new ObjectMapper();
					radiusClientSettings = objectMapper.readValue(settingsJson, typeRef);
				} catch (Exception e) {
					logger.warn("Couldn't deserialize SP IdP Settings", e);
				}
			}
		}
		return radiusClientSettings;
	}
	
	public void setRadiusClientSettings(RadiusClientSettings radiusClientSettings) {
		this.radiusClientSettings = radiusClientSettings;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			settingsJson = objectMapper.writeValueAsString(radiusClientSettings);
		} catch (Exception e) {
			logger.warn("Couldn't serialize SP IdP Settings", e);
		}
	}

	@Override
	public String getRowStyle() {
		// TODO Auto-generated method stub
		return null;
	}
	
}