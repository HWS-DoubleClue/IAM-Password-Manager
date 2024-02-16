package com.doubleclue.dcup.logic;

import java.util.List;
import java.util.UUID;

import com.doubleclue.dcem.userportal.logic.UserPortalConstants;

import de.slackspace.openkeepass.domain.Attachment;
import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.History;
import de.slackspace.openkeepass.domain.Property;
import de.slackspace.openkeepass.domain.Times;

public class PasswordSafeEntry {
	Entry entry;

	public PasswordSafeEntry(Entry entry) {
		super();
		this.entry = entry;
		
	}

	public UUID getUuid() {
		return entry.getUuid();
	}

	public int getIconId() {
		return entry.getIconId();
	}

	public UUID getCustomIconUuid() {
		return entry.getCustomIconUuid();
	}

	public byte[] getIconData() {
		return entry.getIconData();
	}

	public List<Property> getProperties() {
		return entry.getProperties();
	}

	public List<Property> getReferencedProperties() {
		return entry.getReferencedProperties();
	}

	public List<Property> getCustomProperties() {
		return entry.getCustomProperties();
	}

	public String getTitle() {
		return entry.getTitle();
	}
	
	public void setTitle(String title) {
		setValue(false, DcupConstants.KEEPASS_PROPERTY_TITLE, title);
	}

	public String getPassword() {
		return entry.getPassword();
	}
	
	public void setPassword(String password) {
		setValue(true, DcupConstants.KEEPASS_PROPERTY_PASSWORD, password);
	}

	public String getUrl() {
		return entry.getUrl();
	}
	
	public void setUrl(String url) {
		setValue(false, DcupConstants.KEEPASS_PROPERTY_URL, url);
	}

	public String getUrlShort() {
		if (this.getUrl() != null && this.getUrl().length() > UserPortalConstants.PASSOWRD_SAFE_MAX_FIELD_LENGTH) {
			return this.getUrl().substring(0, UserPortalConstants.PASSOWRD_SAFE_MAX_FIELD_LENGTH) + "...";
		}
		return this.getUrl();
	}

	public String getNotes() {
		if (entry.getNotes() != null) {
			return entry.getNotes();
		} else {
			return ""; // entry.getNotes() calls the method getValueFromProperty() which return null if nothing is written in the field Notes
		}
	}
	
	public void setNotes(String notes) {
		setValue(false, DcupConstants.KEEPASS_PROPERTY_NOTES, notes);
	}

	public String getNotesShort() {
		if (this.getNotes().length() > UserPortalConstants.PASSOWRD_SAFE_MAX_FIELD_LENGTH) {
			return this.getNotes().substring(0, UserPortalConstants.PASSOWRD_SAFE_MAX_FIELD_LENGTH) + "...";
		}
		return this.getNotes();
	}

	public String getUsername() {
		return entry.getUsername();
	}
	
	public void setUsername(String username) {
		setValue(false, DcupConstants.KEEPASS_PROPERTY_USER_NAME, username);
	}

	public boolean isTitleProtected() {
		return entry.isTitleProtected();
	}

	public boolean isPasswordProtected() {
		return entry.isPasswordProtected();
	}

	public Times getTimes() {
		return entry.getTimes();
	}

	public List<Attachment> getAttachments() {
		return entry.getAttachments();
	}

	public Property getPropertyByName(String name) {
		return entry.getPropertyByName(name);
	}

	public History getHistory() {
		return entry.getHistory();
	}

	public List<String> getTags() {
		return entry.getTags();
	}

	public String getForegroundColor() {
		return entry.getForegroundColor();
	}

	public String getBackgroundColor() {
		return entry.getBackgroundColor();
	}

	public int hashCode() {
		return entry.hashCode();
	}

	public boolean equals(Object obj) {
		return entry.equals(obj);
	}

	public String toString() {
		return entry.toString();
	}

	public Entry getEntry() {
		return entry;
	}

	public void setEntry(Entry entry) {
		this.entry = entry;
	}
	
	private void setValue(boolean isProtected, String propertyName, String propertyValue) {
        Property property = getPropertyByName(propertyName);
        if (property == null) {
            property = new Property(propertyName, propertyValue, isProtected);
            entry.getProperties().add(property);
        }
        else {
        	entry.getProperties().remove(property);
        	entry.getProperties().add(new Property(propertyName, propertyValue, isProtected));
        }
    }
}
