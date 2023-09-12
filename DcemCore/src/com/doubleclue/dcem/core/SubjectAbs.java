package com.doubleclue.dcem.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;

import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.RawAction;

@SuppressWarnings("serial")
public abstract class SubjectAbs implements Serializable {

	@Inject
	DcemApplicationBean dcemApplicationBean;

	final static String classSuffix = "Subject";

	// List<DcemAction> dcemActions;

	protected List<com.doubleclue.dcem.core.logic.RawAction> rawActions = new LinkedList<>();

	public abstract String getModuleId();

	public abstract int getRank();

	private boolean hiddenMenu = false;

	public abstract String getIconName();

	public abstract String getPath();

	public Class<?> getKlass() {
		return null;
	}

	public String getName() {
		String name = this.getClass().getSimpleName();
		if (name.lastIndexOf(classSuffix) == -1) {
			return name;
		}
		return name.substring(0, name.length() - classSuffix.length());
	}

	public String getViewName() {
		String name = this.getName();
		StringBuffer sb = new StringBuffer();
		sb.append(Character.toLowerCase(name.charAt(0)));
		sb.append(name.substring(1));
		sb.append(DcemConstants.VIEW_SUFFEIX);
		return sb.toString();
	}

	public List<com.doubleclue.dcem.core.logic.RawAction> getRawActions() {
		return rawActions;
	}

	// public List<DcemAction> getDcemActions() {
	// return dcemActions;
	// }
	//
	// public void setDcemActions(List<DcemAction> actions) {
	// dcemActions = actions;
	// }

	// public DcemAction getDcemActionByName (String name) {
	// for (DcemAction dcemAction : dcemActions) {
	// if (dcemAction.getAction().equals(name)) {
	// return dcemAction;
	// }
	// }
	// return null;
	// }

	public RawAction getRawAction(String action) {
		for (RawAction rawAction : rawActions) {
			if (rawAction.getName().equals(action)) {
				return rawAction;
			}
		}
		return null;
	}

	public String getDisplayName() {
		String resourceName = dcemApplicationBean.getModule(this.getModuleId()).getResourceName();
		ResourceBundle resourceBundle = JsfUtils.getBundle(resourceName);
		return JsfUtils.getStringSafely(resourceBundle, this.getViewName());
	}

	public String getTableStyle() {
		return DcemConstants.TABLE_STYLE;
	}

	public List<DcemAction> getDcemActions() {
		List<DcemAction> dcemActions = new ArrayList<>(rawActions.size());
		for (RawAction rawAction : rawActions) {
			dcemActions.add(new DcemAction(this.getModuleId(), this.getName(), rawAction.getName()));
		}
		return dcemActions;
	}

	public String toString() {
		return getName();
	}

	public boolean isHiddenMenu() {
		return hiddenMenu;
	}

	public void setHiddenMenu(boolean hiddenMenu) {
		this.hiddenMenu = hiddenMenu;
	}

}
