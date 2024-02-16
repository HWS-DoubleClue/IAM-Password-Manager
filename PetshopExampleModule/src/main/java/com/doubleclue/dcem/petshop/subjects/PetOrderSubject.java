package com.doubleclue.dcem.petshop.subjects;

import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.ActionSelection;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.petshop.entities.PetEntity;
import com.doubleclue.dcem.petshop.entities.PetOrderEntity;
import com.doubleclue.dcem.petshop.logic.PetshopModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class PetOrderSubject extends SubjectAbs {

	public PetOrderSubject () {
		rawActions.add(new RawAction (DcemConstants.ACTION_ADD, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.CREATE_OBJECT));
		rawActions.add(new RawAction (DcemConstants.ACTION_EDIT, new String []  {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.ONE_ONLY));
		rawActions.add(new RawAction (DcemConstants.ACTION_DELETE, new String []  {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}, ActionSelection.ONE_OR_MORE));
				
		rawActions.add(new RawAction (DcemConstants.ACTION_VIEW, null));
		rawActions.add (new RawAction (DcemConstants.ACTION_MANAGE, new String [] {DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN}));
	
	};

	@Override
	public String getModuleId() {
		return PetshopModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 999;
	}

	@Override
	public String getIconName() {
		return "fa fa-money";
	}

	@Override
	public String getPath() {
		return DcemConstants.AUTO_VIEW_PATH;
	}

	@Override
	public Class<?> getKlass() {
		return PetOrderEntity.class;
	}
}
