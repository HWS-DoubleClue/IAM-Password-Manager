package com.doubleclue.dcup.gui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.doubleclue.dcup.logic.DcupConstants;

import de.slackspace.openkeepass.domain.Property;

public class ComparatorCustomProperty implements Comparator<Property> {
	
	private static final List<String> PROPERTY_KEYS = new ArrayList<String>();
	static {
		PROPERTY_KEYS.add(DcupConstants.KEEPASS_PROPERTY_USER_NAME);
		PROPERTY_KEYS.add(DcupConstants.KEEPASS_PROPERTY_NOTES);
		PROPERTY_KEYS.add(DcupConstants.KEEPASS_PROPERTY_URL);
		PROPERTY_KEYS.add(DcupConstants.KEEPASS_PROPERTY_PASSWORD);
		PROPERTY_KEYS.add(DcupConstants.KEEPASS_PROPERTY_TITLE);
	}

	@Override
	public int compare(Property propertyA, Property propertyB) {
		boolean stardardPropA = PROPERTY_KEYS.contains(propertyA.getKey());
		boolean stardardPropB = PROPERTY_KEYS.contains(propertyB.getKey());
		if (stardardPropA == true && stardardPropB == false) {
			return -1;
		} else if (stardardPropA == false && stardardPropB == true) {
			return 1;
		}
		return 0;
	}
}
