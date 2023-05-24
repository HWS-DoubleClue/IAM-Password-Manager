package com.doubleclue.dcem.core.gui;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;

import com.doubleclue.dcem.core.gui.converters.DefaultConvertor;
import com.doubleclue.dcem.core.jpa.FilterItem;
import com.doubleclue.dcem.core.jpa.FilterOperator;
import com.doubleclue.dcem.core.jpa.VariableType;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.utils.MethodProperty;
import com.doubleclue.dcem.core.weld.CdiUtils;

/**
 * @author Emanuel Galea
 */
public class ViewVariable implements Serializable {

	private static final long serialVersionUID = 4725468617809734809L;

	private static final Logger logger = LogManager.getLogger(ViewVariable.class);

	OperatorSessionBean operatorSessionBean;
	ViewNavigator viewNavigator;

	String id;
	String displayName;
	String helpText;
	VariableType variableType;
	Class klass;
	DcemGui dcemGui;
	Object value;
	FilterItem filterItem = new FilterItem();
	boolean restricted;
	boolean visible = true;
	IPhoto iPhoto;

	ArrayList<MethodProperty> methodProperties = null;
	ArrayList<Attribute<?, ?>> attributes;
	Method restrictedMethod;

	public ViewVariable() {
	}

	public ViewVariable(String id, String displayName, String helpText, VariableType variableType, DcemGui dcemGui, Object value,
			ArrayList<Attribute<?, ?>> attributes) {
		super();
		this.id = id;
		this.displayName = displayName;
		this.helpText = helpText;
		this.variableType = variableType;
		this.dcemGui = dcemGui;
		this.value = value;
		this.attributes = attributes;
		visible = dcemGui.visible();
	}

	public ViewVariable(String id, String displayName, String helpText, VariableType variableType, DcemGui dcemGui, Object value,
			ArrayList<Attribute<?, ?>> attributes, FilterItem filterItem) {
		super();
		this.id = id;
		this.displayName = displayName;
		this.helpText = helpText;
		this.variableType = variableType;
		this.dcemGui = dcemGui;
		this.value = value;
		this.attributes = attributes;
		this.filterItem = filterItem;
		visible = dcemGui.visible();
	}

	public ViewVariable(String id, String displayName, String helpText) {
		super();
		this.id = id;
		this.displayName = displayName;
		this.helpText = helpText;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getHelpText() {
		return helpText;
	}

	public String getStyle() {
		if (dcemGui == null) {
			return null;
		}
		return dcemGui.style();
	}

	public String getStyleClass() {
		return dcemGui.styleClass();
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public String getConverterId() {
		if (dcemGui != null && dcemGui.converterId().length() > 0) {
			return dcemGui.converterId();
		}
		return "com.doubleclue.defaultConverter";
	}

	public Converter getConverter() {
		if (dcemGui != null && dcemGui.converterId().length() > 0) {
			return FacesContext.getCurrentInstance().getApplication().createConverter(dcemGui.converterId());
		}
		return new DefaultConvertor();
	}

	public String getRecordData(Object klassObject) {
		getMethodProperties(klassObject);
		MethodProperty lastMethodProperty = null;

		for (MethodProperty methodProperty : methodProperties) {
			try {
				if (dcemGui.restricted() == true) {
					if (restrictedMethod == null) {
						restrictedMethod = klassObject.getClass().getMethod("isRestricted");
					}
					if (((boolean) restrictedMethod.invoke(klassObject)) == true) {
						if (viewNavigator == null) {
							viewNavigator = CdiUtils.getReference(ViewNavigator.class);
							operatorSessionBean = CdiUtils.getReference(OperatorSessionBean.class);
						}
						if (operatorSessionBean.isPermission(viewNavigator.getActiveView().getRevealAction(),
								viewNavigator.getActiveView().getManageAction()) == false) {
							return "---";
						}
					}
				}
			} catch (Exception e1) {
				logger.debug(lastMethodProperty, e1);
				return e1.getMessage();
			}
			try {
				lastMethodProperty = methodProperty;
				if (klassObject != null) {
					klassObject = methodProperty.getMethod().invoke(klassObject);
				}
			} catch (IllegalArgumentException exp) {
				PropertyDescriptor pd;
				try {
					pd = new PropertyDescriptor(methodProperty.getName(), klassObject.getClass());
					Method getterMethod = pd.getReadMethod();
					methodProperty.setMethod(getterMethod);
					klassObject = getterMethod.invoke(klassObject);
					logger.debug("Class does not match");
				} catch (Exception e) {
					logger.warn(lastMethodProperty, exp);
					return "?-?-?";
				}
			} catch (Exception exp) {
				logger.warn(lastMethodProperty, exp);
				return "???";
			}
		}
		if (lastMethodProperty == null) {
			return "???";
		}
		if (lastMethodProperty.getConverter() != null) {
			try {
				return lastMethodProperty.getConverter().getAsString(FacesContext.getCurrentInstance(), null, klassObject);
			} catch (Exception exp) {
				logger.warn(exp);
				return null;
			}
		} else {
			return klassObject.toString();
		}
	}

	public StreamedContent getRecordImage(Object klassObject) {
		byte[] image = null;
		if (klassObject != null) {
			try {
				image = ((IPhoto) klassObject).getPhoto();
				if (image != null) {
					InputStream in = new ByteArrayInputStream(image);
					return DefaultStreamedContent.builder().contentType("image/png").stream(() -> in).build();
				}
			} catch (Exception e) {
				logger.warn("Most probable the IPhoto interfaces is missing in entity", e);
			}
		}
		return JsfUtils.getDefaultUserImage();
	}

	@Override
	public int hashCode() {
		return new Integer(id).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (id != ((ViewVariable) obj).id) {
			return false;
		}
		return true;
	}

	public VariableType getVariableType() {
		return variableType;
	}

	public void setVariableType(VariableType variableType) {
		this.variableType = variableType;
	}

	public void addAttribute(Attribute<?, ?> attribute) {
		if (attributes == null) {
			attributes = new ArrayList<Attribute<?, ?>>();
		}
		attributes.add(attribute);
	}

	public ArrayList<Attribute<?, ?>> getAttributes() {
		return attributes;
	}

	public void setAttributes(ArrayList<Attribute<?, ?>> attributes) {
		this.attributes = attributes;
	}

	public Object getFilterValue() {
		// if (filterItem.getFilterValue() != null) {
		// System.out.println("ViewVariable.getFilterValue() " + this.getDisplayName() + "/" + filterItem.getFilterValue());
		// }
		return filterItem.getFilterValue();
	}

	public Date getFilterValueAsDate() {
		if (filterItem.getFilterValue() == null) {
			return null;
		}
		return (Date) filterItem.getFilterValue();
	}

	public void setFilterValueAsDate(Date date) {
		setFilterValue(date);
	}

	public void setFilterValue(Object filterValue) {
		filterItem.setFilterValue(filterValue);
	}

	public void setFilterValueOnly(Object filterValue) {
		filterItem.setFilterValue(filterValue);
	}

	public Object getFilterToValue() {
		return filterItem.getFilterToValue();
	}

	public LocalDateTime getFilterToValueAsLocalDateTime() {
		return (LocalDateTime) filterItem.getFilterToValue();
	}

	public void setFilterToValueAsLocalDateTime(LocalDateTime date) {
		filterItem.setFilterToValue(date);
	}

	public void setFilterToValue(Object filterToValue) {
		filterItem.setFilterToValue(filterToValue);
	}

	public FilterItem getFilterItem() {
		return filterItem;
	}

	public void setFilterItem(FilterItem filterItem) {
		this.filterItem = filterItem;
	}

	public void setFilterOperator(FilterOperator filterOperator) {
		filterItem.setFilterOperator(filterOperator);
	}

	public void setFilterOperatorOnly(FilterOperator filterOperator) {
		filterItem.setFilterOperator(filterOperator);
	}

	public FilterOperator getFilterOperator() {
		return filterItem.getFilterOperator();
	}

	public void setFilterSortOrderOnly(SortOrder sortOrder) {
		filterItem.setSortOrder(sortOrder);
	}

	public SortOrder getFilterSortOrder() {
		return filterItem.getSortOrder();
	}

	public Class getKlass() {
		return klass;
	}

	public void setKlass(Class klass) {
		this.klass = klass;
	}

	public LinkedList<SelectItem> getEnumItems() {
		LinkedList<SelectItem> list = new LinkedList<SelectItem>();
		list.add(new SelectItem("", ""));
		if (klass == null | klass.isEnum() == false) {
			return list;
		}
		Object[] items = klass.getEnumConstants();
		for (Object item : items) {
			list.add(new SelectItem(Integer.toString(((Enum) item).ordinal()), ((Enum) item).name()));
			// list.add(new SelectItem((((Enum)item).ordinal()),((Enum)item).name()));

		}
		return list;
	}

	public void enumChange() {
		if (getFilterValue() == null || ((String) getFilterValue()).isEmpty()) {
			filterItem.setFilterOperator(FilterOperator.NONE);
		} else {
			filterItem.setFilterOperator(FilterOperator.EQUALS);
		}
	}

	private void getMethodProperties(Object klassObject) {
		if (methodProperties == null) {
			MethodProperty methodProperty;
			if (attributes.size() == 0) {
				methodProperties = new ArrayList<MethodProperty>(1);
				PropertyDescriptor pd = null;
				try {
					pd = new PropertyDescriptor(id, klassObject.getClass());
				} catch (IntrospectionException e) {
					e.printStackTrace();
				}
				methodProperty = new MethodProperty(id);
				methodProperty.setMethod(pd.getReadMethod());
				methodProperty.setConverter(getConverter());
				methodProperties.add(methodProperty);
			} else {
				methodProperties = new ArrayList<MethodProperty>(attributes.size());
				Class<?> lastKlass = klassObject.getClass();
				for (Attribute<?, ?> attribute : attributes) {
					methodProperty = new MethodProperty(attribute.getName());
					try {
						PropertyDescriptor pd = new PropertyDescriptor(attribute.getName(), lastKlass);
						Method getterMethod = pd.getReadMethod();
						methodProperty.setMethod(getterMethod);
						lastKlass = getterMethod.getReturnType();
						methodProperty.setConverter(getConverter());
					} catch (Exception exp) {
						logger.warn("Converter not found" + dcemGui.converterId() + " for Field:" + id, exp);
					}
					methodProperties.add(methodProperty);
				}
			}
		}
	}

	public boolean isRestricted() {
		return restricted;
	}

	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}

	public String toString() {
		return "Id=" + id + ", Filter=" + filterItem + ", Attributes=" + attributes;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public DcemGui getDcemGui() {
		return dcemGui;
	}

	public void setDcemGui(DcemGui dcemGui) {
		this.dcemGui = dcemGui;
	}

}
