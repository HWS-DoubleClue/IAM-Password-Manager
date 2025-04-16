package com.doubleclue.dcem.dm.gui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.PrimeFaces;

import com.doubleclue.dcem.as.entities.CloudSafeTagEntity;
import com.doubleclue.dcem.as.logic.CloudSafeTagLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.AutoViewAction;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemView;

@Named("dmTagDialog")
@SessionScoped
public class DmTagDialog extends DcemDialog {

	// private static Logger logger = LogManager.getLogger(DmTagDialog.class);
	private static final long serialVersionUID = 1L;

	@Inject
	CloudSafeTagLogic cloudSafeTagLogic;

	private CloudSafeTagEntity cloudSafeTagEntity;
	private String tagColor;

	@PostConstruct
	public void init() {
	}

	@Override
	public boolean actionOk() throws Exception {
		cloudSafeTagEntity.setColor(tagColor);
		cloudSafeTagLogic.addOrUpdateTagEntity(cloudSafeTagEntity, this.getAutoViewAction().getDcemAction());
		return true;
	}

	@Override
	public void show(DcemView dcemView, AutoViewAction autoViewAction) throws Exception {
		if (autoViewAction.getDcemAction().getAction().equals(DcemConstants.ACTION_ADD)) {
			cloudSafeTagEntity = new CloudSafeTagEntity();
		} else {
			cloudSafeTagEntity = (CloudSafeTagEntity) this.getActionObject();
			tagColor = cloudSafeTagEntity.getColor();

		}
	}

	@Override
	public void actionConfirm() throws Exception {
		List<Object> tagsObj = autoViewBean.getSelectedItems();
		Set<CloudSafeTagEntity> tags = new HashSet<CloudSafeTagEntity>();
		for (Object tagObj : tagsObj) {
			tags.add((CloudSafeTagEntity) tagObj);
		}
		cloudSafeTagLogic.removeTags(tags, getAutoViewAction().getDcemAction());
	}

	@Override
	public void leavingDialog() {
		tagColor = null;
	}

	public CloudSafeTagEntity getCloudSafeTagEntity() {
		return cloudSafeTagEntity;
	}

	public void setCloudSafeTagEntity(CloudSafeTagEntity cloudSafeTagEntity) {
		this.cloudSafeTagEntity = cloudSafeTagEntity;
	}

	public String getWidth() {
		return "30vw";
	}

	public String getHeight() {
		return "55vh";
	}

	public String getTagColor() {
		return tagColor;
	}

	public void setTagColor(String tagColor) {
		this.tagColor = tagColor;
	}

}
