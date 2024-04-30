package com.doubleclue.dcup.logic;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import com.doubleclue.dcem.admin.subjects.UserSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.entities.DcemUserExtension;
import com.doubleclue.dcem.core.jpa.DcemTransactional;
import com.doubleclue.dcem.core.logic.AuditingLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.core.utils.compare.CompareUtils;

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
			changeInfo = CompareUtils.compareObjects(clonedUser, dcemUser);
		} catch (Exception e) {
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
		DcemUserExtension dcemUserExtensionDb = em.find(DcemUserExtension.class, dcemUser.getId());
		if (dcemUserExtensionDb == null) {
			dcemUserExtension.setId(dcemUser.getId());
			em.persist(dcemUserExtension);
			dcemUser.setDcemUserExt(dcemUserExtension);
		} else {
			if (dcemUserExtension.getPhoto() != null) {
				dcemUserExtensionDb.setPhoto(dcemUserExtension.getPhoto());
			}
			dcemUserExtensionDb.setCountry(dcemUserExtension.getCountry());
			dcemUserExtensionDb.setTimezone(dcemUserExtension.getTimezone());
		}
		// userLogic.updateDcemUserExtension(dcemUser, dcemUserExtension);
		if (changeInfo != null || changeInfo.isEmpty() == false) {
			auditingLogic.addAudit(new DcemAction(userSubject, DcemConstants.ACTION_EDIT), dcemUser, changeInfo);
		}
	}

}
