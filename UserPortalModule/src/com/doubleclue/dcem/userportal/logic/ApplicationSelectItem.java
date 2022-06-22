package com.doubleclue.dcem.userportal.logic;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

public class ApplicationSelectItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private byte[] logo;

	private String name;

	private static final Logger logger = LogManager.getLogger(ApplicationSelectItem.class);

	public ApplicationSelectItem(Integer id, String name, byte[] logo) {
		super();
		this.id = id;
		this.logo = logo;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public byte[] getLogo() {
		return logo;
	}

	public void setLogo(byte[] logo) {
		this.logo = logo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public StreamedContent getLogoImg() {
		byte[] logoImg = getLogo();
		if (logoImg == null) {
			return DefaultStreamedContent.builder().contentType("image/png")
					.stream(() -> this.getClass().getResourceAsStream("/appHub/DC_Logo_transp_01.2.png")).build();
		}
		return DefaultStreamedContent.builder().contentType("image/png").stream(() -> {
			try {
				return new ByteArrayInputStream(logoImg);
			} catch (Exception e) {
				logger.warn(e);
				return new ByteArrayInputStream(new byte[] {});
			}
		}).build();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + Arrays.hashCode(logo);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ApplicationSelectItem other = (ApplicationSelectItem) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (!Arrays.equals(logo, other.logo))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	/** DO NOT CHANGE THIS TOSTRING FUNCTION **/
	@Override
	public String toString() {
		return id.toString();
	}

}