package com.doubleclue.dcem.core.entities;

import java.util.ArrayList;
import java.util.List;

public class DependencyClasses {

	/*
	 *  This method is required when bilding SQL Scripts in Hibernate
	 */
	public static List<Class<?>> getCoreDependencyClasses() {
		List<Class<?>> systemClasses = new ArrayList<>();
		systemClasses.add(DcemRole.class);
		systemClasses.add(DomainEntity.class);
		systemClasses.add(DcemAction.class);
		systemClasses.add(DcemUser.class);
		systemClasses.add(DcemUserExtension.class);
		systemClasses.add(DepartmentEntity.class);
		systemClasses.add(DcemGroup.class);
		systemClasses.add(DcemTemplate.class);
		return systemClasses;
	}


}
