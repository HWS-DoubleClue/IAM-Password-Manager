package com.doubleclue.dcem.core.entities;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.QueryHint;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.primefaces.model.SortOrder;

import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.jpa.FilterOperator;
import com.doubleclue.dcem.core.outofscope.DbJsonListConverter;
import com.doubleclue.dcem.core.utils.DisplayModes;

/**
 *  
 * 
 * @author Emanuel Galea
 */
@Entity
@Table(name="core_template", uniqueConstraints= @UniqueConstraint(name="UK_APP_TEMPLATE", columnNames={"dc_name", "language", "dc_version"}))
@Inheritance(strategy=InheritanceType.JOINED)
@Cache (usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)

@NamedQueries ({ 
@NamedQuery(name=DcemTemplate.GET_TEMPLATES, query="SELECT template FROM DcemTemplate template where template.active = true ORDER BY template.name ASC" ),
@NamedQuery(name=DcemTemplate.GET_TEMPLATES_BY_NAME_LOCALE, query="SELECT template FROM DcemTemplate template where template.active = true and template.name=?1 and template.language=?2", hints = {
		@QueryHint(name = "org.hibernate.cacheable", value = "true"),
		@QueryHint(name = "org.hibernate.cacheRegion", value = DcemTemplate.GET_TEMPLATES_BY_NAME_LOCALE) } ),
@NamedQuery(name=DcemTemplate.GET_FILTER_LIST, query="SELECT template FROM DcemTemplate template where template.name = ?1"),
@NamedQuery(name=DcemTemplate.GET_DEFAULT_TEMPLATE, query="SELECT template FROM DcemTemplate template where "
		+ "template.name = ?1 and template.active = true and template.defaultTemplate = true", hints = {
				@QueryHint(name = "org.hibernate.cacheable", value = "true"),
				@QueryHint(name = "org.hibernate.cacheRegion", value = DcemTemplate.GET_DEFAULT_TEMPLATE) }),
})

public class DcemTemplate extends EntityInterface {

//	private static Logger logger = LogManager.getLogger(AppDevice.class);
	
	public final static String GET_TEMPLATES = "tempalte.templates";

	public static final String GET_FILTER_LIST = "tempalte.templateList";

	public static final String GET_TEMPLATES_BY_NAME_LOCALE = "tempalte.templatesByName";

	public static final String GET_DEFAULT_TEMPLATE = "tempalte.defaultTempalte";
	
	
	@Id
	@Column(name = "dc_id")
	@TableGenerator( name = "coreSeqStoreAsTemplate", table = "core_seq", pkColumnName = "seq_name", pkColumnValue="APP_TEMPLATE_ID", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1 )
    @GeneratedValue( strategy = GenerationType.TABLE, generator = "coreSeqStoreAsTemplate" )
	@DcemGui (displayMode=DisplayModes.INPUT_DISABLED)
    private Integer id;
	
	@DcemGui (sortOrder = SortOrder.ASCENDING)
	@Column (name="dc_name", nullable=false, length=128)
	String name;
	
	@DcemGui
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "language", nullable = false)
	SupportedLanguage language = SupportedLanguage.English;
	
	@DcemGui 
	@Column(name = "dc_version")
	int version;
	
	
	
	@DcemGui
	boolean inUse;
	
	@DcemGui (name="default")
	boolean defaultTemplate;
	
	@Column (length=32)
	byte []  macDigest;
	
	@Basic(fetch = FetchType.LAZY)
	@Lob
	String content;
	
	@Column (name="dc_tokens", length=4096, nullable=true)
	@Convert (converter=DbJsonListConverter.class)
	LinkedList <String> tokens;
	
	@DcemGui
	@Temporal(TemporalType.TIMESTAMP)
	Date lastModified;
	
	@DcemGui (filterOperator=FilterOperator.IS_TRUE)
	boolean active;
	
	
	@Version
	int jpaVersion;
	

	public Integer getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public byte[] getMacDigest() {
		return macDigest;
	}

	public void setMacDigest(byte[] macDigest) {
		this.macDigest = macDigest;
	}

	public boolean isInUse() {
		return inUse;
	}

	public void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

		
	public int getJpaVersion() {
		return jpaVersion;
	}

	public void setJpaVersion(int jpaVersion) {
		this.jpaVersion = jpaVersion;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	
	public boolean isDefaultTemplate() {
		return defaultTemplate;
	}


	public void setDefaultTemplate(boolean defaultTemplate) {
		this.defaultTemplate = defaultTemplate;
	}

	public List<String> getTokens() {
		return tokens;
	}

	public void setTokens(LinkedList <String> tokens) {
		this.tokens = tokens;
	}
	
	@Transient
	public String toString() {
		return name + "-" + language;
	}

	public SupportedLanguage getLanguage() {
		return language;
	}

	public void setLanguage(SupportedLanguage language) {
		this.language = language;
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	
	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	
		
	
}