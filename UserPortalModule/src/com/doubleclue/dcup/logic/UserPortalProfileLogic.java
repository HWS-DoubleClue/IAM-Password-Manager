package com.doubleclue.dcup.logic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.doubleclue.dcem.admin.subjects.UserSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DcemUserExtension;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.DomainLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.DcemUtils;


@ApplicationScoped
public class UserPortalProfileLogic {

	@Inject
	UserLogic userLogic;

	@Inject
	UserSubject userSubject;

	@Inject
	AuditingLogic auditingLogic;
	
	@Inject
	EntityManager em;

	@DcemTransactional
	public void updateUserProfile(DcemUser clonedUser, DcemUserExtension dcemUserExtension) throws Exception {
		DcemUser dcemUser = userLogic.getUser(clonedUser.getId());
		String changeInfo;
		try {
			changeInfo = DcemUtils.compareObjects(clonedUser, dcemUser);
		} catch (DcemException e) {
			changeInfo = e.toString();
		}
		dcemUser.setLoginId(clonedUser.getLoginId());
		dcemUser.setDisplayName(clonedUser.getDisplayName());
		dcemUser.setEmail(clonedUser.getEmail());
		dcemUser.setPrivateEmail(clonedUser.getPrivateEmail());
		dcemUser.setTelephoneNumber(clonedUser.getTelephoneNumber());
		dcemUser.setMobileNumber(clonedUser.getMobile());
		dcemUser.setPrivateMobileNumber(clonedUser.getPrivateMobileNumber());
		dcemUser.setLanguage(clonedUser.getLanguage());
		userLogic.updateDcemUserExtension(dcemUser, dcemUserExtension);
		auditingLogic.addAudit(new DcemAction(userSubject, DcemConstants.ACTION_EDIT), dcemUser, changeInfo);
	}

}
