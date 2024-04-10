package com.doubleclue.dcem.admin.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.logic.UserLogic;


@Named("autoCompleteUser")
@RequestScoped
public class AutoCompleteUser  {
	
	@Inject
	UserLogic userLogic;
	
	@Inject
	OperatorSessionBean operatorSessionBean;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Inject
	DcemApplicationBean applicationBean;

	public String getOperator() {
		return operatorSessionBean.getDcemUser().getAccountName();
	}

	public List<DcemUser> completeUser(String name) {
		return userLogic.getCompleteDcemUser(name, 30);
	}
	
	public StreamedContent getUserPhoto(DcemUser dcemUser_) {
		try {
			byte[] image = dcemUser_.getPhoto();
			if (image == null) {
				return JsfUtils.getDefaultUserImage();
			} else {
				InputStream in = new ByteArrayInputStream(image);
				return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
			}
		} catch (Exception e) {
			return null;
		}
	}
	
}
