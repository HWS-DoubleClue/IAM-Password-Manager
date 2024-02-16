package com.doubleclue.dcem.core.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
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
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlRootElement;

import com.doubleclue.dcem.core.gui.DcemGui;

/**
 * 
 * @author Paulo Zenida - Linkare TI
 * 
 */
@Entity
@Table(name = "core_textMessage", uniqueConstraints = @UniqueConstraint(name = "UK_RESOURCE_MESSAGE_KEY", columnNames = { "dc_key", "textResourceBundle" }))
@XmlRootElement
@NamedQueries({ @NamedQuery(name = TextMessage.QUERYNAME_FIND_ALL, query = TextMessage.QUERY_FIND_ALL),
		@NamedQuery(name = TextMessage.QUERYNAME_COUNT_ALL, query = TextMessage.QUERY_COUNT_ALL),
		@NamedQuery(name = TextMessage.QUERYNAME_FIND_KEY, query = TextMessage.QUERY_FIND_KEY),
		@NamedQuery(name = TextMessage.QUERYNAME_DELETE_BY_KEY, query = TextMessage.QUERY_DELETE_BY_KEY) })
public class TextMessage extends EntityInterface {

	// private static final long serialVersionUID = 1L;

	public static final String QUERYNAME_FIND_ALL = "TextMessage.findAll";
	public static final String QUERY_FIND_ALL = "Select r from TextMessage r";

	public static final String QUERYNAME_COUNT_ALL = "TextMessage.countAll";
	public static final String QUERY_COUNT_ALL = "select count(r) from TextMessage r";

	public static final String QUERYNAME_FIND_KEY = "TextMessage.findKey";
	public static final String QUERY_FIND_KEY = "Select r from TextMessage r where r.textResourceBundle = ?1 AND r.key = ?2";

	public static final String QUERYNAME_DELETE_BY_KEY = "TextMessage.deleteByKey";
	public static final String QUERY_DELETE_BY_KEY = "DELETE FROM TextMessage r WHERE r.textResourceBundle = ?1 AND r.key = ?2";

	public TextMessage() {
	}

	@Id
	@TableGenerator(name = "coreSeqStoreResourceMessage", table = "core_seq", pkColumnName = "seq_name", pkColumnValue = "RESOURCE_MESSAGE", valueColumnName = "seq_value", initialValue = 1, allocationSize = 50)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "coreSeqStoreResourceMessage")
	@Column(name = "dc_id")
	Integer id;

	@Column(name = "dc_key")
	@DcemGui
	private String key;

	@Column(name = "dc_value", length = 4096)
	@DcemGui
	private String value;

	@ManyToOne
	@JoinColumn(name = "textResourceBundle", foreignKey = @ForeignKey(name = "FK_RESOURCE_MESSAGE_BUNDLE"))
	@DcemGui(subClass = "locale", name = "Language")
	private TextResourceBundle textResourceBundle;

	@Version
	private int jpaVersion;

	/**
	 * @return the key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * @param key
	 *            the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return key + "=" + value;
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

	public TextResourceBundle getTextResourceBundle() {
		return textResourceBundle;
	}

	public void setTextResourceBundle(TextResourceBundle textResourceBundle) {
		this.textResourceBundle = textResourceBundle;
	}

	@Override
	public String getRowStyle() {
		// TODO Auto-generated method stub
		return null;
	}
}