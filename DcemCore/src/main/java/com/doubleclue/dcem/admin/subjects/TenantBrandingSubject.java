package com.doubleclue.dcem.admin.subjects;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;
//import com.doubleclue.petshop.entities.PetEntity;     //here did changes*****
//import com.doubleclue.petshop.logic.PetshopModule;   //here did changes*****

@SuppressWarnings("serial")
@ApplicationScoped
@Named("tenantBrandingSubject")
public class TenantBrandingSubject extends SubjectAbs {

	public TenantBrandingSubject () {
		rawActions.add(new RawAction (DcemConstants.ACTION_SAVE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction (DcemConstants.ACTION_VIEW, null));
		rawActions.add (new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));
	
	};

	@Override
	public String getModuleId() {
		//return PetshopModule.MODULE_ID;
		return AdminModule.MODULE_ID;            //here did changes*****
	}

	@Override
	public int getRank() {
		return 90;
	}

	@Override
	public String getIconName() {
		return "fa fa-building";
	}

	@Override
	public String getPath() {
		//return "/modules/petshop/tenantBranding.xhtml";            //here did changes*****
		return "/modules/admin/tenantBranding.xhtml";
		
	}

	@Override
	public Class<?> getKlass() {
		return null;
	}
}

