package com.doubleclue.dcem.core.entities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.gui.DcemGui;

/**
 * 
 * @author Paulo Zenida - Linkare TI
 * 
 */
@Entity
@Table(name = "core_textResourceBundle", uniqueConstraints = @UniqueConstraint(name = "UK_RESOURCE_LOCALE_BASENAME", columnNames = { "locale",
		"baseName" }))
@XmlRootElement
@NamedQueries({ @NamedQuery(name = TextResourceBundle.QUERYNAME_FIND_ALL, query = "SELECT r FROM TextResourceBundle r"),
		@NamedQuery(name = TextResourceBundle.QUERYNAME_COUNT_ALL, query = "SELECT count(r) FROM TextResourceBundle r"),
		@NamedQuery(name = TextResourceBundle.QUERYNAME_FIND_BY_LOCALE, query = "SELECT r FROM TextResourceBundle r WHERE r.baseName = ?1 AND r.locale = ?2"),
		@NamedQuery(name = TextResourceBundle.QUERYNAME_FIND_BY_BASE_NAME, query = "SELECT r FROM TextResourceBundle r WHERE r.baseName = ?1") })

public class TextResourceBundle extends EntityInterface {

	public static final String QUERYNAME_FIND_ALL = "ResourceBundle.findAll";
	public static final String QUERYNAME_COUNT_ALL = "ResourceBundle.countAll";
	public static final String QUERYNAME_FIND_BY_LOCALE = "ResourceBundle.findByLocale";
	public static final String QUERYNAME_FIND_BY_BASE_NAME = "ResourceBundle.findByBaseName";

	public TextResourceBundle() {
	}

	public TextResourceBundle(String locale) {
		this.baseName = AdminModule.MODULE_ID;
		this.locale = locale;
		messages = new LinkedList<TextMessage>();
	}

	@Id
	@TableGenerator(name = "coreSeqStoreTextResource", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "TEXT_RESOURCE", valueColumnName = "seq_value", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreTextResource")
	@Column(name = "dc_id")
	Integer id;

	@Column(name = "locale")
	@DcemGui
	private String locale;

	@Column(name = "basename")
	@DcemGui
	private String baseName;

	@OneToMany(mappedBy = "textResourceBundle", fetch = FetchType.LAZY)
	private List<TextMessage> messages;

	@Version
	private int jpaVersion;

	/**
	 * @return the locale
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * @param locale
	 *            the locale to set
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

	/**
	 * @return the messages
	 */
	public List<TextMessage> getMessages() {
		if (messages == null) {
			messages = new ArrayList<TextMessage>();
		}
		return messages;
	}

	/**
	 * @param messages
	 *            the messages to set
	 */
	public void setMessages(List<TextMessage> messages) {
		this.messages = messages;
	}

	@Override
	public String toString() {
		return "TextResourceBundle [id=" + id + ", locale=" + locale + ", baseName=" + baseName + "]";
	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Number id) {
		this.id = (Integer) id;
	}

	public int getJpaVersion() {
		return jpaVersion;
	}

	public void setJpaVersion(int jpaVersion) {
		this.jpaVersion = jpaVersion;
	}

	@Override
	public String getRowStyle() {
		// TODO Auto-generated method stub
		return null;
	}

}
