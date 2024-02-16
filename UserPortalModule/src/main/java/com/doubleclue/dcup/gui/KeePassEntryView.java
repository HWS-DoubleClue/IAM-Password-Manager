package com.doubleclue.dcup.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.validator.constraints.Length;
import org.primefaces.PrimeFaces;
import org.primefaces.component.tree.TreeDragDropInfo;
import org.primefaces.event.DragDropEvent;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.TreeNode;

import com.doubleclue.dcem.core.as.DcemUploadFile;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcup.logic.PasswordSafeEntry;

import de.slackspace.openkeepass.domain.Attachment;
import de.slackspace.openkeepass.domain.Entry;
import de.slackspace.openkeepass.domain.Group;
import de.slackspace.openkeepass.domain.Property;

@Named("keePassEntryView")
@SessionScoped
public class KeePassEntryView extends AbstractPortalView {

	@Inject
	KeePassView keePassView;

	@Inject
	private PortalSessionBean portalSessionBean;

	List<Attachment> attachmentsBin = new ArrayList<Attachment>();
	PasswordSafeEntry moveFromEntry;
	Group moveToGroup;
	Group moveFromGroup;
	Group moveFromGroupParent;

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}

	private Logger logger = LogManager.getLogger(KeePassEntryView.class);
	private static final long serialVersionUID = 1L;
	private boolean editingProperty;
	@Length(min = 1, max = 64)
	private String customPropertyName;
	@Length(min = 1, max = 1024)
	private String customPropertyValue;
	private Property selectedProperty;
	private List<DcemUploadFile> uploadedFiles;
	private MyAttachment selectedAttachmentFile;


	public List<MyAttachment> getCurrentFiles() {
		if (keePassView.getCurrentEntry() == null) {
			return null;
		}
		List<Attachment> attachments = keePassView.getCurrentEntry().getEntry().getAttachments();
		List<MyAttachment> myAttachments = new ArrayList<>(attachments.size());
		try {
			for (Attachment attachment : attachments) {
				myAttachments.add(new MyAttachment(attachment.getKey(), attachment.getData().length, attachment.getRef()));
			}
			return myAttachments;
		} catch (Exception e) {
			logger.error(e);
			return null;
		}
	}

	public void handleFileUpload(FileUploadEvent event) {
		List<Attachment> attachments = keePassView.getCurrentEntry().getEntry().getAttachments();
		attachments.add(new Attachment(event.getFile().getFileName(), -1, event.getFile().getContent()));
	}

	public void deleteAttachment() {
		if (selectedAttachmentFile == null) {
			JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("message.keePassSelectAttachment"));
			return;
		}
		List<Attachment> attachments = keePassView.getCurrentEntry().getAttachments();
		for (int i = 0; i < attachments.size(); i++) {
			if (attachments.get(i).getKey().equals(selectedAttachmentFile.getKey())) {
				attachments.remove(i);
				break;
			}
		}
		PrimeFaces.current().ajax().update("processEntryForm:tabView:attachmentsTable");
	}

	public void actionDownloadAttachment() {
		if (selectedAttachmentFile == null) {
			JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("message.keePassSelectAttachment"));
			PrimeFaces.current().executeScript("PF('processEntryDialog').show();");
			return;
		}
		List<Attachment> attachments = keePassView.getCurrentEntry().getEntry().getAttachments();
		Attachment attachmentFound = null;
		for (Attachment attachment : attachments) {
			if (attachment.getRef() == selectedAttachmentFile.getRef()) {
				attachmentFound = attachment;
			}
		}
		try {
			JsfUtils.downloadFile("",attachmentFound.getKey(), attachmentFound.getData());
		} catch (IOException e) {
			JsfUtils.addErrorMessage(e.toString());
		}
		
	}

	public List<Property> getCustomProperties() {
		if (keePassView.getCurrentEntry() == null) {
			return null;
		}
		sortProperties();
		return keePassView.getCurrentEntry().getEntry().getCustomProperties();
	}

	public void upAction() {
		if (selectedProperty == null) {
			JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("message.keePassSelectProperty"));
			return;
		}
		List<Property> properties = keePassView.getCurrentEntry().getEntry().getProperties();
		int propertyInedx = properties.indexOf(selectedProperty);
		if (propertyInedx == 0) {
			return;
		}
		Collections.swap(properties, propertyInedx, propertyInedx - 1);
		PrimeFaces.current().ajax().update("processEntryForm:tabView:customPropertiesTable");
	}

	public void downAction() {
		if (selectedProperty == null) {
			JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("message.keePassSelectProperty"));
			return;
		}

		List<Property> properties = keePassView.getCurrentEntry().getEntry().getProperties();
		int propertyInedx = properties.indexOf(selectedProperty);
		if (properties.size() == propertyInedx + 1) {
			return;
		}
		Collections.swap(properties, propertyInedx, propertyInedx + 1);
		PrimeFaces.current().ajax().update("processEntryForm:tabView:customPropertiesTable");
	}

	public void addNewProperty() {
		customPropertyName = null;
		customPropertyValue = null;
		selectedProperty = null;
		editingProperty = false;
		PrimeFaces.current().executeScript("PF('addNewPropertyDialog').show();");
		PrimeFaces.current().ajax().update("addNewPropertyForm");
	}

	public void editProperty() {
		if (selectedProperty == null) {
			JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("message.keePassSelectProperty"));
			return;
		}
		editingProperty = true;
		setCustomPropertyName(selectedProperty.getKey());
		setCustomPropertyValue(selectedProperty.getValue());
		PrimeFaces.current().ajax().update("addNewPropertyForm");
		PrimeFaces.current().executeScript("PF('addNewPropertyDialog').show();");
	}

	public void deleteProperty() {
		if (selectedProperty == null) {
			JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("message.keePassSelectProperty"));
			return;
		}
		List<Property> properties = keePassView.getCurrentEntry().getEntry().getProperties();
		properties.remove(selectedProperty);
		PrimeFaces.current().executeScript("PF('addNewPropertyDialog').hide();");
		PrimeFaces.current().ajax().update("processEntryForm:tabView:customPropertiesTable");
	}

	public void actionCustomProperty() {
		if (editingProperty == false) {
			List<Property> properties = keePassView.getCurrentEntry().getEntry().getProperties();
			for (Property property : properties) {
				if (property.getKey().equalsIgnoreCase(getCustomPropertyName())) {
					JsfUtils.addInfoMessage(portalSessionBean.getResourceBundle().getString("message.keePassPropertyAlreadyExist"));
					return;
				}
			}
			properties.add(new Property(customPropertyName, customPropertyValue, false));
		} else {
			List<Property> properties = keePassView.getCurrentEntry().getEntry().getProperties();
			int propertyInedx = properties.indexOf(selectedProperty);
			Property prob = new Property(customPropertyName, customPropertyValue, false);
			properties.set(propertyInedx, prob);
		}
		PrimeFaces.current().ajax().update("processEntryForm:tabView:customPropertiesTable");
		PrimeFaces.current().executeScript("PF('addNewPropertyDialog').hide();");
	}

	public boolean onDropGroup(TreeDragDropInfo event) {
		TreeNode dragNode = event.getDragNode();
		TreeNode dropNode = event.getDropNode();
		moveFromGroup = (Group) dragNode.getData();
		moveToGroup = (Group) dropNode.getData();
		moveFromGroupParent = (Group) dragNode.getParent().getData();
		PrimeFaces.current().executeScript("PF('moveGroupConfirmationDialog').show();");
		PrimeFaces.current().ajax().update("moveGroupConfirmationForm:moveGroupConfirmation");
		return false;
	}

	public void onDropEntry(DragDropEvent event) {
		event.getDropId();
		moveToGroup = (Group) event.getComponent().getAttributes().get("currentNode");
		moveFromEntry = (PasswordSafeEntry) event.getData();
		PrimeFaces.current().executeScript("PF('moveEntryConfirmationDialog').show();");
		PrimeFaces.current().ajax().update("moveEntryConfirmationForm:moveEntryConfirmation");
	}

	public void actionMoveGroup() throws DcemException {
		if (moveToGroup == null || moveToGroup == null) {
			JsfUtils.addErrorMessage("MoveTo or Move From is not defined");
			return;
		}
		if (moveFromGroup == keePassView.getRycleBinGroup()) {
			JsfUtils.addErrorMessage(portalSessionBean.getResourceBundle().getString("error.MOVE_RECYCLING_BIN"));
			return;
		}
		moveFromGroupParent.getGroups().remove(moveFromGroup);
		moveToGroup.getGroups().add(moveFromGroup);
		keePassView.updateFileAndPage();
		PrimeFaces current = PrimeFaces.current();
		current.executeScript("PF('moveGroupConfirmationDialog').hide();");

	}

	public void actionMoveGroupOnDelete(Group moveToGroup, Group moveFromGroup, Group moveFromGroupParent) {
		if (moveToGroup == null || moveToGroup == null) {
			JsfUtils.addErrorMessage("MoveTo or Move From is not defined");
			return;
		}
		moveFromGroupParent.getGroups().remove(moveFromGroup);
		moveToGroup.getGroups().add(moveFromGroup);
	}

	public void movePassWordSafeEntry() {
		if (moveFromEntry == null || moveToGroup == null) {
			JsfUtils.addErrorMessage("MoveTo or Move From is not defined");
			return;
		}
		Group group = keePassView.getGroup(moveFromEntry.getEntry());
		group.getEntries().remove(moveFromEntry);
		moveToGroup.getEntries().add(moveFromEntry.getEntry());
		keePassView.updateFileAndPage();
		PrimeFaces current = PrimeFaces.current();
		current.executeScript("PF('moveEntryConfirmationDialog').hide();");

	}

	public void moveEntry(Group moveToGroup, Entry moveFromEntry) {
		if (moveFromEntry == null || moveToGroup == null) {
			JsfUtils.addErrorMessage("MoveTo or Move From is not defined");
			return;
		}
		Group group = keePassView.getGroup(moveFromEntry);
		group.getEntries().remove(moveFromEntry);
		moveToGroup.getEntries().add(moveFromEntry);
		// keePassView.updateFileAndPage();
		// PrimeFaces current = PrimeFaces.current();
		// current.executeScript("PF('moveConfirmationDialog').hide();");

	}

	public void moveRow() {
		keePassView.getTreeGroup().getChildren().get(0);
		keePassView.getPasswordSafeEntries();
		List<Entry> list = keePassView.getEntries();

		list.clear();
		for (PasswordSafeEntry passwordSafeEntry : keePassView.getPasswordSafeEntries()) {
			list.add(passwordSafeEntry.getEntry());
		}
		keePassView.updateFileAndPage();
		return;
	}

	public PasswordSafeEntry getMoveFromEntry() {
		return moveFromEntry;
	}

	public void setMoveFromEntry(PasswordSafeEntry moveFromEntry) {
		this.moveFromEntry = moveFromEntry;
	}

	public Group getMoveToGroup() {
		return moveToGroup;
	}

	public void setMoveToGroup(Group moveToGroup) {
		this.moveToGroup = moveToGroup;
	}

	public Group getMoveFromGroup() {
		return moveFromGroup;
	}

	public void setMoveFromGroup(Group moveFromGroup) {
		this.moveFromGroup = moveFromGroup;
	}

	public Group getMoveFromGroupParent() {
		return moveFromGroupParent;
	}

	public void setMoveFromGroupParent(Group moveFromGroupParent) {
		this.moveFromGroupParent = moveFromGroupParent;
	}

	public boolean isEditingProperty() {
		return editingProperty;
	}

	public void setEditingProperty(boolean editingProperty) {
		this.editingProperty = editingProperty;
	}

	public String getCustomPropertyName() {
		return customPropertyName;
	}

	public void setCustomPropertyName(String customPropertyName) {
		this.customPropertyName = customPropertyName;
	}

	public String getCustomPropertyValue() {
		return customPropertyValue;
	}

	public void setCustomPropertyValue(String customPropertyValue) {
		this.customPropertyValue = customPropertyValue;
	}

	public Property getSelectedProperty() {
		return selectedProperty;
	}

	public void setSelectedProperty(Property selectedProperty) {
		this.selectedProperty = selectedProperty;
	}

	private void sortProperties() {
		List<Property> properties = keePassView.getCurrentEntry().getEntry().getProperties();
		Collections.sort(properties, new ComparatorCustomProperty());
	}

	public List<DcemUploadFile> getUploadedFiles() {
		return uploadedFiles;
	}

	public void setUploadedFiles(List<DcemUploadFile> uploadedFiles) {
		this.uploadedFiles = uploadedFiles;
	}

	public MyAttachment getSelectedAttachmentFile() {
		return selectedAttachmentFile;
	}

	public void setSelectedAttachmentFile(MyAttachment selectedAttachmentFile) {
		this.selectedAttachmentFile = selectedAttachmentFile;
	}

}
