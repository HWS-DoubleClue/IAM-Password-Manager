package com.doubleclue.dcem.core.gui;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.SelectItem;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SetAttribute;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.jpa.FilterItem;
import com.doubleclue.dcem.core.jpa.FilterOperator;
import com.doubleclue.dcem.core.jpa.VariableType;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.utils.MethodProperty;
import com.doubleclue.dcem.core.utils.MethodProperty.MethodPropertyType;
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
	Class<?> listClass; // if set this variable is a List

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
		return null;
	}

	public String getRecordData(Object klassObject) {
		getMethodProperties(klassObject);
		MethodProperty lastMethodProperty = null;
		for (MethodProperty methodProperty : methodProperties) {
			try {
				if (dcemGui.restricted() == true) {
					if (restrictedMethod == null) {
						restrictedMethod = klassObject.getClass().getMethod(DcemConstants.METHOD_IS_RESTRICTED);
					}
					if (((boolean) restrictedMethod.invoke(klassObject)) == true) {
						if (viewNavigator == null) {
							viewNavigator = CdiUtils.getReference(ViewNavigator.class);
						}
						if (getOperatorSessionBean().isPermission(viewNavigator.getActiveView().getRevealAction()) == false) {
							return DcemConstants.RESTRICTED_REPLACEMENT;
						}
					}
				}
			} catch (Exception e1) {
				logger.debug(lastMethodProperty, e1);
				return e1.getMessage();
			}
			try {
				lastMethodProperty = methodProperty;
				if (methodProperty.getObjectType() != null) {
					switch (methodProperty.getObjectType()) {
					case LIST:
						List<?> list = (List<?>) methodProperty.getMethod().invoke(klassObject);
						return list.toString();
					case SORTEDLIST:
						SortedSet<?> sortedSet = (SortedSet<?>) methodProperty.getMethod().invoke(klassObject);
						return sortedSet.toString();
					default:
						if (klassObject != null) {
							klassObject = methodProperty.getMethod().invoke(klassObject);
						}
					}
				} else {
					if (klassObject != null) {
						klassObject = methodProperty.getMethod().invoke(klassObject);
					}
				}
			} catch (IllegalArgumentException exp) {
				try {
					Method getterMethod = DcemUtils.getGetterMethodFromString(methodProperty.getName(), klassObject.getClass());
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
		try {
			if (lastMethodProperty.getConverter() != null) {
				return lastMethodProperty.getConverter().getAsString(FacesContext.getCurrentInstance(), null, klassObject);
			}
			return convertAsString(klassObject);
		} catch (Exception exp) {
			logger.warn(klassObject.toString(), exp);
			return null;
		}
	}

	private void getMethodProperties(Object klassObject) {
		if (methodProperties == null) {
			MethodProperty methodProperty;
			if (attributes.size() == 0) {
				methodProperties = new ArrayList<MethodProperty>(1);
				methodProperty = new MethodProperty(id);
				try {
					Method getterMethod = DcemUtils.getGetterMethodFromString(id, klassObject.getClass());
					methodProperty.setMethod(getterMethod);
					methodProperty.setConverter(getConverter());
					methodProperties.add(methodProperty);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				methodProperties = new ArrayList<MethodProperty>(attributes.size());
				Class<?> lastKlass = klassObject.getClass();
				for (Attribute<?, ?> attribute : attributes) {
					methodProperty = new MethodProperty(attribute.getName());
					try {

						Method getterMethod = DcemUtils.getGetterMethodFromString(attribute.getName(), lastKlass);
						methodProperty.setMethod(getterMethod);
						methodProperty.setConverter(getConverter());
						if (attribute instanceof ListAttribute<?, ?>) {
							methodProperty.setObjectType(MethodPropertyType.LIST);
							lastKlass = listClass;
						} else if (attribute instanceof SetAttribute<?, ?>) {
							methodProperty.setObjectType(MethodPropertyType.SORTEDLIST);
							lastKlass = listClass;
						} else {
							lastKlass = getterMethod.getReturnType();
						}
					} catch (Exception exp) {
						logger.warn("Converter not found" + dcemGui.converterId() + " for Field:" + id, exp);
					}
					methodProperties.add(methodProperty);
				}
			}
		}
	}

	private String convertAsString(Object value) {
		if (value == null) {
			return "";
		}
		Locale locale = getOperatorSessionBean().getLocale();
		String resultValue;
		DateTimeFormatter dateFormatter;
		switch (variableType) {
		case TIME:
			dateFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(locale);
			resultValue = ((LocalTime) value).format(dateFormatter);
			break;
		case DATE:
			resultValue = JsfUtils.getLocalDateFormat((LocalDate) value);
			break;
		case DATE_TIME:
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM).withLocale(locale);
			LocalDateTime ldt = getOperatorSessionBean().getUserZonedTime((LocalDateTime) value);
			resultValue = ldt.format(dateTimeFormatter);
			break;
		case BOOLEAN:
			if ((Boolean) value == true) {
				return "\u2705";
			} else {
				return "\u2718";
			}
		default:
			resultValue = value.toString();
		}
		return resultValue;
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

	public void setKlass(Class cls) {
		this.klass = cls;
	}

	public LinkedList<SelectItem> getEnumItems() {
		LinkedList<SelectItem> list = new LinkedList<SelectItem>();
		if (klass == null | klass.isEnum() == false) {
			return list;
		}
		Object[] items = klass.getEnumConstants();
		for (Object item : items) {
			list.add(new SelectItem(Integer.toString(((Enum) item).ordinal()), ((Enum) item).name()));
		}
		if (dcemGui.sortEnumerations() == true) {
			Collections.sort(list, new Comparator<SelectItem>() {
				@Override
				public int compare(SelectItem o1, SelectItem o2) {
					return o1.getLabel().compareTo(o2.getLabel());
				}
			});
		}
		return list;
	}

	public List<String> getRatings(Object klassObject) {
		String value = getRecordData(klassObject);
		float rating = Float.parseFloat(value);
		List<String> list = new ArrayList<>();
		list.add(rating > 0 ? "-on" : "");
		list.add(rating > 1 ? "-on" : "");
		list.add(rating > 2 ? "-on" : "");
		list.add(rating > 3 ? "-on" : "");
		list.add(rating > 4 ? "-on" : "");
		return list;
	}

	public void enumChange() {
		if (getFilterValue() == null || ((String) getFilterValue()).isEmpty()) {
			filterItem.setFilterOperator(FilterOperator.NONE);
		} else {
			filterItem.setFilterOperator(FilterOperator.EQUALS);
		}
	}

	public boolean isRestricted() {
		return restricted;
	}

	public void setRestricted(boolean restricted) {
		this.restricted = restricted;
	}

	public String toString() {
		return "Id=" + id + ", Type=" + variableType + ", Filter=" + filterItem + ", Attributes=" + attributes;
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

	public Class<?> getListClass() {
		return listClass;
	}

	public void setListClass(Class<?> listClass) {
		this.listClass = listClass;
	}

	public String getColumnStyle() {
		if (dcemGui != null && dcemGui.style().isEmpty() == false) {
			return dcemGui.style();
		}
		switch (this.variableType) {
		case BOOLEAN:
		case NUMBER:
			return "text-align: center !Important;";
		default:
			return "text-align: left; word-break: break;  white-space: normal";
		}
	}

	public boolean isLink() {
		return dcemGui.linkUrl().isEmpty() == false;
	}

	public boolean isText() {
		return variableType != VariableType.IMAGE && isLink() == false;
	}

	public String getLinkUrl(Object value) {
		return dcemGui.linkUrl().replaceAll("#\\{value}", getRecordData(value));
	}

	private OperatorSessionBean getOperatorSessionBean() {
		if (operatorSessionBean == null) {
			operatorSessionBean = CdiUtils.getReference(OperatorSessionBean.class);
		}
		return operatorSessionBean;
	}

}
