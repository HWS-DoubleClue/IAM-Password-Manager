//
//
package com.doubleclue.dcem.mydevices.subjects;

//
import javax.enterprise.context.ApplicationScoped;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.SubjectAbs;
import com.doubleclue.dcem.core.logic.RawAction;
import com.doubleclue.dcem.mydevices.logic.MyDevicesModule;

@SuppressWarnings("serial")
@ApplicationScoped
public class MyDevicesSubject extends SubjectAbs {

	public MyDevicesSubject() {
		rawActions.add(new RawAction(DcemConstants.ACTION_VIEW, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER }));
		rawActions.add(new RawAction(DcemConstants.ACTION_MANAGE, new String[] { DcemConstants.SYSTEM_ROLE_SUPERADMIN, DcemConstants.SYSTEM_ROLE_ADMIN, DcemConstants.SYSTEM_ROLE_USER }));
	}

	@Override
	public String getModuleId() {
		return MyDevicesModule.MODULE_ID;
	}

	@Override
	public int getRank() {
		return 1;
	}

	@Override
	public String getIconName() {
		return "fa fa-mobile-screen";
	}

	@Override
	public String getPath() {
		return "/modules/mydevices/devicesView.xhtml";
	}

	@Override
	public Class<?> getKlass() {
		return null;
	}

}
