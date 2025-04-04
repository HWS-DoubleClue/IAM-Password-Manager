package com.doubleclue.dcem.core.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.context.FacesContext;
import javax.faces.convert.DoubleConverter;
import javax.faces.convert.IntegerConverter;
import javax.faces.convert.LongConverter;
import javax.faces.model.SelectItem;
import javax.imageio.ImageIO;
import javax.inject.Named;
import javax.persistence.metamodel.Attribute;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import org.primefaces.component.autocomplete.AutoComplete;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.overlaypanel.OverlayPanel;
import org.primefaces.component.password.Password;
import org.primefaces.component.selectbooleancheckbox.SelectBooleanCheckbox;
import org.primefaces.component.selectonemenu.SelectOneMenu;

import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.EntityInterface;
import com.doubleclue.dcem.core.entities.RoleRestriction;
import com.doubleclue.dcem.core.exceptions.DcemErrorCodes;
import com.doubleclue.dcem.core.exceptions.DcemException;
import com.doubleclue.dcem.core.gui.DcemDialog;
import com.doubleclue.dcem.core.gui.DcemGui;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.gui.ViewVariable;
import com.doubleclue.dcem.core.jpa.FilterItem;
import com.doubleclue.dcem.core.jpa.TenantIdResolver;
import com.doubleclue.dcem.core.jpa.VariableType;
import com.doubleclue.dcem.core.logic.LoginAuthenticator;
import com.doubleclue.dcem.core.logic.OperatorSessionBean;
import com.doubleclue.dcem.core.tasks.ReloadTask;
import com.doubleclue.utils.RandomUtils;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.twelvemonkeys.image.ResampleOp;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.internet.MimeMultipart;

public class DcemUtils {

	private static final Logger logger = LogManager.getLogger(DcemUtils.class);

	static SupportedLanguage[] supportedLanguages = SupportedLanguage.values();

	static public String getComputerName() {
		Map<String, String> env = System.getenv();
		if (env.containsKey("COMPUTERNAME"))
			return env.get("COMPUTERNAME");
		else if (env.containsKey("HOSTNAME"))
			return env.get("HOSTNAME");
		else {
			try {
				return InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e) {
				String nodeName = RandomUtils.generateRandomAlphaNumericString(8);
				logger.warn("Couldn't find any Hostname, so we take this: " + nodeName);
				return nodeName;
			}
		}
	}

	public static String getCountryCode(String country) {
		if (country == null) {
			return null;
		}
		String[] countries = Locale.getISOCountries();
		for (String countryCode : countries) {
			Locale locale = new Locale("", countryCode);
			if (country.equals(locale.getDisplayCountry())) {
				return countryCode;
			}
		}
		return null;
	}

	/**
	 * @param cls
	 * @param dcemGui
	 * @return
	 */
	static public ViewVariable convertFieldToViewVariable(Field field, ResourceBundle resourceBundle, String viewName, Object objectKlass) {
		return convertFieldToViewVariable(field, resourceBundle, viewName, objectKlass, null);
	}

	/**
	 * @param field
	 * @param resourceBundle
	 * @param viewName
	 * @param objectKlass
	 * @param viewVariable   Parent ViewVariable
	 * @return
	 */
	static public ViewVariable convertFieldToViewVariable(Field field, ResourceBundle resourceBundle, String viewName, Object objectKlass,
			ArrayList<Attribute<?, ?>> attributes) {
		Class<?> cls;
		String displayName = null;
		String helpText = null;
		DcemGui dcemGui = field.getAnnotation(DcemGui.class);
		cls = field.getType();
		Object filterValue = null;
		Object filterToValue = null;
		if (dcemGui == null) {
			return null;
		}
		if (objectKlass != null && dcemGui.displayMode() == DisplayModes.TABLE_ONLY) {
			return null;
		}
		if (objectKlass == null && dcemGui.displayMode() == DisplayModes.INPUT_ONLY) {
			return null;
		}
		if (dcemGui.masterOnly() == true && TenantIdResolver.isCurrentTenantMaster() == false) {
			return null;
		}
		String fieldName = field.getName();
		if (dcemGui.name().isEmpty() == false) {
			fieldName = dcemGui.name();
		}
		if (resourceBundle != null) {
			try {
				displayName = resourceBundle.getString(viewName + "." + fieldName);
			} catch (MissingResourceException mrex) {
			}
			try {
				helpText = resourceBundle.getString(viewName + "." + DcemConstants.PREF_HELP_RESOURCE + "." + fieldName);
			} catch (MissingResourceException e) {
				helpText = dcemGui.help();
			}
		}
		if (displayName == null || displayName.startsWith(JsfUtils.NO_RESOURCE_FOUND)) {
			displayName = resourceKeyToName(fieldName);
		}
		Class<?> metaClass = null;
		try {
			metaClass = Class.forName(field.getDeclaringClass().getName() + "_");
		} catch (Exception e1) {
			// logger.debug(e1);
		}

		String subClass = null;
		VariableType variableType = dcemGui.variableType();
		// VariableType variableType = VariableType.UNKNOWN;

		Object value = null;
		if (objectKlass != null) {
			try {
				field.setAccessible(true);
				// if (dcemGui.password() == true) {
				// value = DcemConstants.PASSWORD_REPLACEMENT;
				// } else {
				value = field.get(objectKlass);
				// }
			} catch (IllegalArgumentException e) {
				logger.debug("Class variable couldn't be transformed to HTML Variable: " + field.getName());
				// return null;
			} catch (IllegalAccessException e) {
				logger.debug("Class variable couldn't be transformed to HTML Variable: " + field.getName());
				return null;
			}
		}
		if (attributes == null) {
			attributes = new ArrayList<>();
		}
		if (metaClass != null) {
			try {
				if (dcemGui.dbMetaAttributeName().isEmpty()) {
					// Field fieldx = metaClass.getField(field.getName());
					attributes.add((Attribute<?, ?>) metaClass.getField(field.getName()).get(metaClass));
					// attributes.add((SingularAttribute<?, ?>)
					// metaClass.getDeclaredField(field.getName()).get(metaClass));
				} else {
					// attributes.add((SingularAttribute<?, ?>)
					// metaClass.getDeclaredField(dcemGui.dbMetaAttributeName()).get(metaClass));
					attributes.add((Attribute<?, ?>) metaClass.getField(dcemGui.dbMetaAttributeName()).get(metaClass));
				}
			} catch (Exception exp) {
				logger.debug(exp);
			}
		}
		if ((cls.equals(Integer.class)) || (cls.equals(int.class))) {
			variableType = VariableType.NUMBER;
			if (dcemGui.filterValue().isEmpty() == false) {
				filterValue = Integer.parseInt(dcemGui.filterValue());
			}
			if (dcemGui.filterToValue().isEmpty() == false) {
				filterToValue = Integer.parseInt(dcemGui.filterToValue());
			}
		} else if ((cls.equals(Long.class)) || (cls.equals(long.class))) {
			variableType = VariableType.NUMBER;
			if (dcemGui.filterValue().isEmpty() == false) {
				filterValue = Long.parseLong(dcemGui.filterValue());
			}
			if (dcemGui.filterToValue().isEmpty() == false) {
				filterToValue = Long.parseLong(dcemGui.filterToValue());
			}
		} else if ((cls.equals(Double.class)) || (cls.equals(double.class))) {
			variableType = VariableType.NUMBER;
			if (dcemGui.filterValue().isEmpty() == false) {
				filterValue = Double.parseDouble(dcemGui.filterValue());
			}
			if (dcemGui.filterToValue().isEmpty() == false) {
				filterToValue = Double.parseDouble(dcemGui.filterToValue());
			}
		} else if (cls.equals(String.class)) {
			variableType = VariableType.STRING;
			filterValue = dcemGui.filterValue();
			filterToValue = dcemGui.filterToValue();
		} else if ((cls.equals(Boolean.class)) || (cls.equals(boolean.class))) {
			variableType = VariableType.BOOLEAN;
			if (dcemGui.filterValue().isEmpty() == false) {
				filterValue = Boolean.parseBoolean(dcemGui.filterValue());
			}
			if (dcemGui.filterToValue().isEmpty() == false) {
				filterToValue = Boolean.parseBoolean(dcemGui.filterToValue());
			}
		} else if ((cls.equals(Date.class)) || (cls.equals(Timestamp.class)) || (cls.equals(LocalDateTime.class))) {
			variableType = VariableType.DATE_TIME;
		} else if (cls.equals(java.sql.Date.class) || cls.equals(LocalDate.class)) {
			variableType = VariableType.DATE;
		} else if (cls.equals(LocalTime.class)) {
			variableType = VariableType.TIME;
		} else if (cls.isEnum()) {
			variableType = VariableType.ENUM;
			if (dcemGui.filterValue().isEmpty() == false) {
				filterValue = dcemGui.filterValue();
			}
		} else if (dcemGui.variableType() == VariableType.IMAGE) {
			// ok

		} else {
			// it is a Class
			if (dcemGui.subClass().isEmpty() == false) {
				if (cls.equals(List.class) || cls.equals(SortedSet.class)) {
					variableType = VariableType.LIST;
					if (dcemGui.filterValue().isEmpty() == false) {
						filterValue = dcemGui.filterValue();
					}
					Type[] types = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
					try {
						Class<?> listClass = Class.forName(types[0].getTypeName());
						Field subField = listClass.getDeclaredField(dcemGui.subClass());
						ViewVariable subViewVariable = convertFieldToViewVariable(subField, null, null, objectKlass, attributes);
						if (subViewVariable == null) {
							return null;
						}
						if (subViewVariable.getDcemGui().subClass().isEmpty()) {
							variableType = subViewVariable.getVariableType();
							ViewVariable retViewVariable = new ViewVariable(field.getName() + "." + subViewVariable.getId(), displayName, helpText,
									variableType, dcemGui, value, attributes);
							retViewVariable.setKlass(subViewVariable.getKlass());
							if (objectKlass == null) {
								FilterItem filterItem = new FilterItem(retViewVariable.getId(), filterValue, filterToValue, dcemGui.filterOperator(), 0,
										dcemGui.sortOrder());
								retViewVariable.setFilterItem(filterItem);
							}
							retViewVariable.setListClass(listClass);
							return retViewVariable;
						} else {
							variableType = subViewVariable.getVariableType();
							FilterItem filterItem = new FilterItem(field.getName(), filterValue, filterToValue, dcemGui.filterOperator(), 0,
									dcemGui.sortOrder());
							ViewVariable retViewVariable = new ViewVariable(field.getName(), displayName, helpText, variableType, dcemGui, value, attributes,
									filterItem);
							retViewVariable.setKlass(subViewVariable.getKlass());
							retViewVariable.setListClass(listClass);
							return retViewVariable;
						}
						// System.out.println("DcemUtils.convertFieldToViewVariable()");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				} else {
					subClass = dcemGui.subClass();
					Field subField;
					try {
						subField = cls.getDeclaredField(subClass);
						ViewVariable subViewVariable = convertFieldToViewVariable(subField, null, null, objectKlass, attributes);
						if (subViewVariable == null) {
							return null;
						}
						if (subViewVariable.getDcemGui().subClass().isEmpty()) {
							variableType = subViewVariable.getVariableType();
							ViewVariable retViewVariable = new ViewVariable(field.getName() + "." + subViewVariable.getId(), displayName, helpText,
									variableType, dcemGui, value, attributes);
							retViewVariable.setKlass(subViewVariable.getKlass());
							if (objectKlass == null) {
								FilterItem filterItem = new FilterItem(retViewVariable.getId(), filterValue, filterToValue, dcemGui.filterOperator(), 0,
										dcemGui.sortOrder());
								retViewVariable.setFilterItem(filterItem);
							}
							return retViewVariable;
						} else {
							variableType = subViewVariable.getVariableType();
							FilterItem filterItem = new FilterItem(field.getName(), filterValue, filterToValue, dcemGui.filterOperator(), 0,
									dcemGui.sortOrder());
							ViewVariable retViewVariable = new ViewVariable(field.getName(), displayName, helpText, variableType, dcemGui, value, attributes,
									filterItem);
							retViewVariable.setKlass(subViewVariable.getKlass());
							return retViewVariable;
						}
					} catch (NoSuchFieldException | SecurityException exp2) {
						logger.error("SubClass field not found: " + subClass, exp2);
						return null;
					}
				}

			} else {
				if (variableType == null) {
					variableType = VariableType.OTHER;
				}
				filterValue = null;
				filterToValue = null;
				logger.debug("Other column Type: " + cls.getName());
			}
		}

		attributes.trimToSize();
		ViewVariable viewVariable = new ViewVariable(field.getName(), displayName, helpText, variableType, dcemGui, value, attributes);
		if (objectKlass == null) {
			FilterItem filterItem = new FilterItem(field.getName(), filterValue, filterToValue, dcemGui.filterOperator(), 0, dcemGui.sortOrder());
			viewVariable.setFilterItem(filterItem);
		}
		viewVariable.setKlass((Class<Object>) cls);
		return viewVariable;
	}

	/**
	 * Populate Dialog Table
	 * 
	 * @param viewVariables
	 * @param htmlPanelGrid
	 * @param object
	 * @param bindObject
	 * @param dcemDialog
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static HtmlPanelGrid populateTable(List<ViewVariable> viewVariables, HtmlPanelGrid htmlPanelGrid, Object object, String bindObject, boolean withHelp,
			DcemDialog dcemDialog) throws IllegalArgumentException, IllegalAccessException {

		htmlPanelGrid.getChildren().clear();
		ViewVariable viewVariable;
		if (viewVariables == null) {
			return htmlPanelGrid;
		}
		ExpressionFactory eFactory = FacesContext.getCurrentInstance().getApplication().getExpressionFactory();
		for (int i = 0; i < viewVariables.size(); i++) {
			viewVariable = viewVariables.get(i);
			if (viewVariable.getDcemGui().displayMode() == DisplayModes.TABLE_ONLY || viewVariable.getDcemGui().displayMode() == DisplayModes.NONE) {
				continue;
			}
			if (viewVariable.getDcemGui().separator().isEmpty() == false) {
				HtmlOutputText sep = new HtmlOutputText();
				sep.setValue("-");
				sep.setStyleClass("preferences-seperator");
				htmlPanelGrid.getChildren().add(sep);
				HtmlOutputText sep2 = new HtmlOutputText();
				sep2.setValue(viewVariable.getDcemGui().separator());
				sep2.setStyleClass("preferences-seperator");
				htmlPanelGrid.getChildren().add(sep2);
			}
			HtmlOutputText out = new HtmlOutputText();
			out.setValue(viewVariable.getDisplayName());
			htmlPanelGrid.getChildren().add(out);
			try {
				HtmlPanelGroup htmlPanelGroup = new HtmlPanelGroup();
				UIComponent input = getHtmlInput(eFactory, "#{" + bindObject + "." + viewVariable.getId() + "}", viewVariable, object, dcemDialog);
				input.setId(viewVariable.getId().replace('.', '_'));
				htmlPanelGroup.getChildren().add(input);
				if (withHelp && viewVariable.getHelpText() != null && viewVariable.getHelpText().isEmpty() == false) {
					HtmlOutputText helpButton = new HtmlOutputText();
					helpButton.setEscape(false);
					helpButton.setValue("<i class=\"fa fa-question-circle\" aria-hidden=\"true\"></i>");
					helpButton.setId("helpButton" + input.getId());
					// helpButton.setStyle("padding:2px;color:#005078;cursor:pointer;font-size: 1.2em;margin-left: 0.5em;");
					helpButton.setStyleClass("preferences-help-button");
					OverlayPanel op = new OverlayPanel();
					op.setDismissable(false);
					op.setShowCloseIcon(true);
					op.setFor("helpButton" + input.getId());
					HtmlOutputText helpText = new HtmlOutputText();
					helpText.setValue(viewVariable.getHelpText());
					op.getChildren().add(helpText);
					htmlPanelGroup.getChildren().add(helpButton);
					htmlPanelGroup.getChildren().add(op);
				}
				htmlPanelGrid.getChildren().add(htmlPanelGroup);
			} catch (Exception exp) {
				HtmlOutputText outError = new HtmlOutputText();
				outError.setValue(exp.getMessage());
				htmlPanelGrid.getChildren().add(outError);
			}
		}
		return htmlPanelGrid;
	}

	/**
	 * @param eFactory
	 * @param binding
	 * @param viewVariable
	 * @param object
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	static UIComponent getHtmlInput(ExpressionFactory eFactory, String binding, ViewVariable viewVariable, Object object, DcemDialog dcemDialog)
			throws IllegalArgumentException, IllegalAccessException {

		// FIXME: add validations of input parameter
		DcemGui dcemGui = viewVariable.getDcemGui();

		FacesContext fc = FacesContext.getCurrentInstance();

		switch (viewVariable.getVariableType()) {
		case NUMBER:
			HtmlInputText input = new InputText();
			input.setValueExpression("value", eFactory.createValueExpression(fc.getELContext(), binding, String.class));
			input.setValue(viewVariable.getValue());
			input.setMaxlength(8);
			if (viewVariable.getValue() != null) {
				if (viewVariable.getValue() instanceof Double) {
					input.setConverter(new DoubleConverter());
				} else if (viewVariable.getValue() instanceof Long) {
					input.setConverter(new LongConverter());
				} else {
					input.setConverter(new IntegerConverter());
				}
			} else {
				input.setConverter(new IntegerConverter());
			}

			input.setLabel(viewVariable.getDisplayName());
			if (viewVariable.getStyle() != null) {
				input.setStyle(viewVariable.getStyle());
			}
			if (viewVariable.getStyleClass() != null) {
				input.setStyleClass(viewVariable.getStyleClass());
			}
			if (dcemGui.displayMode() == DisplayModes.INPUT_DISABLED || dcemGui.displayMode() == DisplayModes.INPUT_ONLY_DISABLED) {
				input.setDisabled(true);
			}
			return input;
		case STRING:

			if (dcemGui != null && dcemGui.choose().length > 0) {
				HtmlSelectOneMenu select = new HtmlSelectOneMenu();
				select.setLabel(viewVariable.getDisplayName());
				select.setValueExpression("value", eFactory.createValueExpression(fc.getELContext(), binding, String.class));
				select.setValueExpression("appendTo", eFactory.createValueExpression(fc.getELContext(), "@this", String.class));
				// populate the drop down list
				UISelectItems items = new UISelectItems();
				ArrayList<SelectItem> arr = new ArrayList<SelectItem>(dcemGui.choose().length);
				if (dcemGui.choose()[0].startsWith("#{")) {
					List<Object> names = evalAsListString(dcemGui.choose()[0]);
					if (names.isEmpty() == false) {
						for (Object name : names) {
							if (names.get(0).getClass().equals(SelectItem.class)) {
								arr.add((SelectItem) name);
							} else {
								arr.add(new SelectItem((String) name, (String) name));
							}
						}
					}
				} else {
					for (int i = 0; i < dcemGui.choose().length; i++) {
						arr.add(new SelectItem((String) dcemGui.choose()[i], dcemGui.choose()[i]));
					}
				}
				items.setValue(arr);
				select.getChildren().add(items);
				select.setValue(viewVariable.getValue());
				if (viewVariable.getStyle() != null) {
					select.setStyle(viewVariable.getStyle());
				}
				if (viewVariable.getStyleClass() != null) {
					select.setStyleClass(viewVariable.getStyleClass());
				}

				return select;
			}

			UIInput inputStr;
			if (dcemGui != null && dcemGui.password()) {
				inputStr = new Password();
				((Password) inputStr).setLabel(viewVariable.getDisplayName());

				if (viewVariable.getStyle() != null) {
					((Password) inputStr).setStyle(viewVariable.getStyle());
				}
				if (viewVariable.getStyleClass() != null) {
					((Password) inputStr).setStyleClass(viewVariable.getStyleClass());
				}
				if (dcemGui.displayMode() == DisplayModes.INPUT_DISABLED || dcemGui.displayMode() == DisplayModes.INPUT_ONLY_DISABLED) {
					((Password) inputStr).setDisabled(true);
				}
				((Password) inputStr).setRedisplay(true);
				((Password) inputStr).setToggleMask(true);
			} else {
				if (dcemGui.autoComplete()) {
					inputStr = new AutoComplete();
					((HtmlInputText) inputStr).setLabel(viewVariable.getDisplayName());
					((AutoComplete) inputStr).setMinQueryLength(1);
					String expression = "#{" + getOriginalClassName(dcemDialog.getClass().getSimpleName()) + "." + viewVariable.getId() + "s}";

					MethodExpression methodExpression = createMethodExpression(expression, List.class, new Class<?>[] { String.class });
					((AutoComplete) inputStr).setCompleteMethod(methodExpression);

				} else {
					inputStr = new InputText();
				}
				((HtmlInputText) inputStr).setLabel(viewVariable.getDisplayName());
				((HtmlInputText) inputStr).setRequired(dcemGui.required());
				if (viewVariable.getStyle() != null) {
					((HtmlInputText) inputStr).setStyle(viewVariable.getStyle());
				}
				if (viewVariable.getStyleClass() != null) {
					((HtmlInputText) inputStr).setStyleClass(viewVariable.getStyleClass());
				}
				if (dcemGui.displayMode() == DisplayModes.INPUT_DISABLED || dcemGui.displayMode() == DisplayModes.INPUT_ONLY_DISABLED) {
					HtmlOutputText uiComponent = new HtmlOutputText();
					uiComponent.setValueExpression("value", eFactory.createValueExpression(fc.getELContext(), binding, String.class));
					uiComponent.setRendered(true);
					if (viewVariable.getValue() == null) {
						uiComponent.setValue("");
					} else {
						uiComponent.setValue(viewVariable.getValue());
					}
					return uiComponent;
				}
			}
			inputStr.setValueExpression("value", eFactory.createValueExpression(fc.getELContext(), binding, String.class));
			inputStr.setRendered(true);
			if (viewVariable.getValue() == null) {
				inputStr.setValue("");
			} else {
				inputStr.setValue(viewVariable.getValue());
			}
			return inputStr;

		case BOOLEAN:
			SelectBooleanCheckbox selectBooleanCheckbox = new SelectBooleanCheckbox();
			selectBooleanCheckbox.setValueExpression("value", eFactory.createValueExpression(fc.getELContext(), binding, Boolean.class));
			selectBooleanCheckbox.setValue(viewVariable.getValue());
			selectBooleanCheckbox.setLabel(viewVariable.getDisplayName());
			if (dcemGui.displayMode() == DisplayModes.INPUT_DISABLED || dcemGui.displayMode() == DisplayModes.INPUT_ONLY_DISABLED) {
				((HtmlSelectBooleanCheckbox) selectBooleanCheckbox).setDisabled(true);
			}

			return selectBooleanCheckbox;

		case DATE:
		case EPOCH_DATE:

			org.primefaces.component.calendar.Calendar uiCalendar = new org.primefaces.component.calendar.Calendar();
			uiCalendar.setValueExpression("value", eFactory.createValueExpression(fc.getELContext(), binding, Date.class));
			uiCalendar.setValue(viewVariable.getValue());
			uiCalendar.setStyle("width: 40px");
			if (dcemGui.displayMode() == DisplayModes.INPUT_DISABLED || dcemGui.displayMode() == DisplayModes.INPUT_ONLY_DISABLED) {
				uiCalendar.setDisabled(true);
			}
			return uiCalendar;
		case ENUM:
			HtmlSelectOneMenu select = new SelectOneMenu();
			select.setLabel(viewVariable.getDisplayName());
			select.setValueExpression("value", eFactory.createValueExpression(fc.getELContext(), binding, String.class));
			select.setValueExpression("appendTo", eFactory.createValueExpression(fc.getELContext(), "@this", String.class));
			// populate the drop down list

			Object[] enumItems = viewVariable.getKlass().getEnumConstants();
			List<SelectItem> list = new ArrayList<SelectItem>(enumItems.length);
			UISelectItems items = new UISelectItems();
			for (Object item : enumItems) {
				list.add(new SelectItem(item, ((Enum) item).name()));
			}
			items.setValue(list);
			select.getChildren().add(items);
			select.setValue(viewVariable.getValue());
			if (viewVariable.getStyle() != null) {
				select.setStyle(viewVariable.getStyle());
			}
			if (viewVariable.getStyleClass() != null) {
				select.setStyleClass(viewVariable.getStyleClass());
			}
			return select;

		default:
			HtmlInputText inputUknown = new InputText();
			inputUknown.setValue("??? Unknown Class Type ???");
			return inputUknown;
		}

	}

	public static List<Object> evalAsListString(String p_expression) {
		FacesContext context = FacesContext.getCurrentInstance();
		ExpressionFactory expressionFactory = context.getApplication().getExpressionFactory();
		ELContext elContext = context.getELContext();
		ValueExpression vex = expressionFactory.createValueExpression(elContext, p_expression, List.class);
		return (List<Object>) vex.getValue(elContext);
	}

	public static String evalAsString(String p_expression) {
		FacesContext context = FacesContext.getCurrentInstance();
		ExpressionFactory expressionFactory = context.getApplication().getExpressionFactory();
		ELContext elContext = context.getELContext();
		ValueExpression vex = expressionFactory.createValueExpression(elContext, p_expression, String.class);
		String result = null;
		try {
			result = (String) vex.getValue(elContext);
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return result;
	}

	public static Object evalAsObject(String p_expression) {
		FacesContext context = FacesContext.getCurrentInstance();
		ExpressionFactory expressionFactory = context.getApplication().getExpressionFactory();
		ELContext elContext = context.getELContext();
		ValueExpression vex = expressionFactory.createValueExpression(elContext, p_expression, Object.class);
		Object result = null;
		result = vex.getValue(elContext);
		return result;
	}

	/**
	 * This is a convenience method that parses an expression into a
	 * {@link MethodExpression} for later evaluation. Use this method for
	 * expressions that refer to methods. If the expression is a {@code String}
	 * literal, a {@link MethodExpression} is created, which when invoked, returns
	 * the {@code String} literal, coerced to expectedReturnType. An
	 * {@link ELException} is thrown if expectedReturnType is {@code void} or if the
	 * coercion of the {@code String} literal to the expectedReturnType yields an
	 * error. This method should perform syntactic validation of the expression. If
	 * in doing so it detects errors, it should raise an {@link ELException}.
	 *
	 * @param methodExpression   The expression to parse.
	 * 
	 * @param expectedReturnType The expected return type for the method to be
	 *                           found. After evaluating the expression, the
	 *                           {@link MethodExpression} must check that the return
	 *                           type of the actual method matches this type.
	 *                           Passing in a value of {@code null} indicates the
	 *                           caller does not care what the return type is, and
	 *                           the check is disabled.
	 * 
	 * @param expectedParamTypes The expected parameter types for the method to be
	 *                           found. Must be an array with no elements if there
	 *                           are no parameters expected. It is illegal to pass
	 *                           {@code null}, unless the method is specified with
	 *                           arguments in the EL expression, in which case these
	 *                           arguments are used for method selection, and this
	 *                           parameter is ignored.
	 * @return The parsed expression.
	 */
	static MethodExpression createMethodExpression(String methodExpression, Class<?> expectedReturnType, Class<?>[] expectedParamTypes) {
		FacesContext context = FacesContext.getCurrentInstance();
		return context.getApplication().getExpressionFactory().createMethodExpression(context.getELContext(), methodExpression, expectedReturnType,
				expectedParamTypes);
	}

	/**
	 * @param request
	 * @return null if the http basic authentication header is missing
	 * @throws Exception
	 */
	public static LoginAuthenticator getHttpBasicAuthentication(HttpServletRequest request) throws Exception {
		// TODO Testing
		String authorization = request.getHeader("Authorization");
		if (authorization == null) {
			return null;
		}
		return getHttpBasicAuthentication(authorization);
	}

	/**
	 * @param request
	 * @return null if the http basic authentication header is missing
	 * @throws Exception
	 */
	public static LoginAuthenticator getHttpBasicAuthentication(String authorization) throws Exception {
		// TODO Testing
		if (authorization.length() < 7 || !authorization.substring(0, 6).equalsIgnoreCase("Basic ")) {
			throw new Exception("Unsupported AuthenticationType: " + authorization);
		}
		String basicAuthEncoded = authorization.substring(6);
		String basicAuthClearText = new String(Base64.getDecoder().decode(basicAuthEncoded), "UTF8");
		if (basicAuthClearText.length() < 2) {
			throw new Exception("Authentication credentials are too short.");
		}
		int idxSeparator = basicAuthClearText.indexOf(':');
		if (idxSeparator == -1) {
			throw new Exception("UserId separator not found.");
		}
		LoginAuthenticator loginAuthenticator = new LoginAuthenticator(basicAuthClearText.substring(0, idxSeparator),
				basicAuthClearText.substring(idxSeparator + 1));
		return loginAuthenticator;
	}

	/**
	 * @param klass
	 * @return
	 */
	public static LinkedList<Class<?>> getHierarchyList(Class<?> klass) {

		LinkedList<Class<?>> list = new LinkedList<Class<?>>();
		Class<?> currentClass = klass;
		do {
			list.addFirst(currentClass);

		} while ((currentClass = currentClass.getSuperclass()) != null);
		return list;

	}

	/**
	 * @param key
	 * @return
	 */
	public static String resourceKeyToName(String key) {
		StringBuilder sb = new StringBuilder(key.length() + 4);
		boolean preLowercase = false;
		sb.append(Character.toUpperCase(key.charAt(0)));
		for (int i = 1; i < key.length(); i++) {
			if (Character.isUpperCase(key.charAt(i))) {
				if (preLowercase) {
					sb.append(' ');
				}
				preLowercase = false;
			} else if (Character.isLowerCase(key.charAt(i))) {
				preLowercase = true;
			} else {
				preLowercase = false;
			}
			sb.append(key.charAt(i));
		}
		return sb.toString();
	}

	/**
	 * @param key
	 * @return
	 */
	public static String nameToResourceKey(String key) {
		StringBuilder sb = new StringBuilder(key.length());
		sb.append(Character.toLowerCase(key.charAt(0)));
		for (int i = 1; i < key.length(); i++) {
			if (key.charAt(i) == ' ') {
				if (i == key.length()) { // if last character
					break;
				}

				if (Character.isLowerCase(key.charAt(i - 1)) && Character.isUpperCase(key.charAt(i + 1))) {
					continue;
				} else if (key.charAt(i - 1) == ' ') {
					continue;
				}
			} else {
				sb.append(key.charAt(i));
			}
		}
		return sb.toString();
	}

	public static ViewVariable getViewVariableFromId(List<ViewVariable> variables, String id) {
		for (ViewVariable viewVariable : variables) {
			if (viewVariable.getId().equals(id)) {
				return viewVariable;
			}
		}
		return null;
	}

	public static Date setDayBegin(Date date) {
		assert (date != null) : "date cannot be null";
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return setDayBegin(cal);
	}

	public static Date setDayBegin(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, cal.getMinimum(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, cal.getMinimum(Calendar.MINUTE));
		cal.set(Calendar.SECOND, cal.getMinimum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getMinimum(Calendar.MILLISECOND));
		return cal.getTime();
	}

	public static Date setDayEnd(Date date) {
		assert (date != null) : "date cannot be null";
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return setDayEnd(cal);
	}

	public static Date setDayEnd(Calendar cal) {
		cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
		cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
		cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
		return cal.getTime();
	}

	public static Date getLastDateOfMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		return cal.getTime();
	}

	public static String processTemplate(String template, Map<String, String> map) throws DcemException {

		int fromIndex = 0;
		int indexStart = 0;
		int indexEnd = 0;
		StringBuffer sb = new StringBuffer();
		String key;
		String value;
		while (true) {
			indexStart = template.indexOf("#{", fromIndex);
			if (indexStart == -1) {
				sb.append(template.substring(fromIndex));
				break;
			} else { // start found
				sb.append(template.substring(fromIndex, indexStart));
				indexStart += 2;
				indexEnd = template.indexOf("}", indexStart);
				if (indexEnd == -1) {
					throw new DcemException(DcemErrorCodes.TEMPLATE_BAD_FORMAT, "No end bracket found");
				}
				// a key was found
				key = template.substring(indexStart, indexEnd);
				value = map.get(key);
				if (value == null) {
					throw new DcemException(DcemErrorCodes.TEMPLATE_VALUE_MISSING, "for " + key);
				} else {
					sb.append(value);
				}
				indexEnd++;
				fromIndex = indexEnd;
			}
		}
		return sb.toString();

	}

	public static ConstraintViolationException getConstainViolation(Throwable throwable) {
		while (throwable.getCause() != null) {
			throwable = (Exception) throwable.getCause();
			if (throwable.getClass().equals(org.hibernate.exception.ConstraintViolationException.class)) {
				return (ConstraintViolationException) throwable;
			}
		}
		return null;

	}

	/**
	 * @param clazz
	 * @param resourceBundle
	 * @param viewName
	 * @param restrictions
	 * @return
	 * @throws DcemException
	 */
	public static List<ViewVariable> getViewVariables(Class<?> clazz, ResourceBundle resourceBundle, String viewName, List<RoleRestriction> restrictions)
			throws DcemException {

		List<ViewVariable> viewVariables = new LinkedList<>();
		LinkedList<Class<?>> list = DcemUtils.getHierarchyList(clazz);
		Field[] fields;
		for (Class<?> currentClass : list) {
			fields = currentClass.getDeclaredFields();
			for (Field field : fields) {
				if (Modifier.isStatic(field.getModifiers())) {
					continue;
				}
				ViewVariable viewVariable = DcemUtils.convertFieldToViewVariable(field, resourceBundle, viewName, null);
				if (viewVariable == null) {
					continue;
				}

				if (restrictions != null) {
					for (RoleRestriction roleRestriction : restrictions) {
						if (viewVariable.getId().equals(roleRestriction.getVariableName())) {
							viewVariable.setRestricted(true);
							if (roleRestriction.getFilterItem() == null) {
								throw new DcemException(DcemErrorCodes.ROLE_RESTRICTIOIN_EMPTY_FILTER, "Role Restriction: Filter is null");
							}
							viewVariable.setFilterItem(roleRestriction.getFilterItem());
							break;
						}
					}
				}
				viewVariables.add(viewVariable);
				if (viewVariable.isRestricted() == false
						&& (viewVariable.getDcemGui().displayMode() == DisplayModes.ALL || viewVariable.getDcemGui().displayMode() == DisplayModes.TABLE_ONLY
								|| viewVariable.getDcemGui().displayMode() == DisplayModes.INPUT_DISABLED)) {
					viewVariable.setVisible(true);
				} else {
					viewVariable.setVisible(false);
				}
			}
		}
		return viewVariables;
	}

	public static void setRestricedVariables(EntityInterface entity, OperatorSessionBean operatorSessionBean, DcemAction reveal, DcemAction manage)
			throws Exception {
		if (entity.isRestricted() == true) {
			LinkedList<Class<?>> list = DcemUtils.getHierarchyList(entity.getClass());
			Field[] fields;
			for (Class<?> currentClass : list) {
				fields = currentClass.getDeclaredFields();
				for (Field field : fields) {
					if (Modifier.isStatic(field.getModifiers())) {
						continue;
					}
					DcemGui dcemGui = field.getAnnotation(DcemGui.class);
					if (dcemGui == null) {
						continue;
					}
					if (dcemGui.restricted()) {
						if (operatorSessionBean.isPermission(reveal) == false) {
							Method method = null;
							// for the moment only strings !!!
							method = getSetterMethodForField(field, entity.getClass(), String.class);
							method.invoke(entity, (String) DcemConstants.RESTRICTED_REPLACEMENT);
						}
					}
				}
			}
		}
		return;
	}

	/**
	 * Returns the corresponding getter method for a field from the specified class
	 *
	 * @param field
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static Method getGetterMethodForField(Field field, Class<?> clazz) throws NoSuchMethodException, SecurityException {
		String fieldName = field.getName();
		String getterMethodName = "get" + fieldName.toUpperCase().charAt(0) + fieldName.substring(1);
		try {
			return clazz.getMethod(getterMethodName);
		} catch (NoSuchMethodException e) {
			getterMethodName = "is" + fieldName.toUpperCase().charAt(0) + fieldName.substring(1);
			return clazz.getMethod(getterMethodName);
		}
	}

	/**
	 * Returns the corresponding getter method for a field from the specified class
	 *
	 * @param field
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static Method getGetterMethodFromString(String fieldName, Class<?> clazz) throws NoSuchMethodException, SecurityException {
		String getterMethodName = "get" + fieldName.toUpperCase().charAt(0) + fieldName.substring(1);
		try {
			return clazz.getMethod(getterMethodName);
		} catch (NoSuchMethodException e) {
			getterMethodName = "is" + fieldName.toUpperCase().charAt(0) + fieldName.substring(1);
			try {
				return clazz.getMethod(getterMethodName);
			} catch (NoSuchMethodException e2) {
				getterMethodName = "get" + fieldName;
				return clazz.getMethod(getterMethodName);
			}
		}
	}

	/**
	 * Returns the corresponding getter method for a field from this object
	 *
	 * @param field
	 * @return
	 * @throws NoSuchMethodException
	 * @throws SecurityException
	 */
	public static Method getSetterMethodForField(Field field, Class<?> clazz, Class<?> argType) throws Exception {
		String fieldName = field.getName();
		String setterMethodName = "set" + fieldName.toUpperCase().charAt(0) + fieldName.substring(1);
		do {
			try {
				Method method = clazz.getMethod(setterMethodName, argType);
				return method;
			} catch (NoSuchMethodException e) {
				// Check if the argument is a primitive
				if (org.apache.commons.lang3.ClassUtils.wrapperToPrimitive(argType) != null) {
					argType = org.apache.commons.lang3.ClassUtils.wrapperToPrimitive(argType);
					try {
						Method method = clazz.getMethod(setterMethodName, argType);
						return method;
					} catch (NoSuchMethodException e2) {
						argType = argType.getSuperclass();
					}
				} else {
					// Check for interfaces
					for (Class<?> baseArgType : org.apache.commons.lang3.ClassUtils.getAllInterfaces(argType)) {
						try {
							Method method = clazz.getMethod(setterMethodName, baseArgType);
							return method;
						} catch (NoSuchMethodException e2) {
						}
					}
					argType = null;
				}
			}
		} while (argType != null);
		throw new NoSuchMethodException(setterMethodName);
	}

	static public void copyObject(Object sourceObject, Object destObject) {

		Method[] gettersAndSetters = sourceObject.getClass().getMethods();
		for (int i = 0; i < gettersAndSetters.length; i++) {
			String methodName = gettersAndSetters[i].getName();
			try {
				if (methodName.startsWith("get")) {
					destObject.getClass().getMethod(methodName.replaceFirst("get", "set"), gettersAndSetters[i].getReturnType()).invoke(destObject,
							gettersAndSetters[i].invoke(sourceObject));
				} else if (methodName.startsWith("is")) {
					destObject.getClass().getMethod(methodName.replaceFirst("is", "set"), gettersAndSetters[i].getReturnType()).invoke(destObject,
							gettersAndSetters[i].invoke(sourceObject));
				}
			} catch (Exception exp) {
				logger.debug(exp);
			}
		}
		return;
	}

	static String getOriginalClassName(String name) {
		StringBuffer sb = new StringBuffer(name.length());
		int ind = name.indexOf('$');
		sb.append(Character.toLowerCase(name.charAt(0)));
		if (ind == -1) {
			sb.append(name.substring(1));
		} else {
			sb.append(name.substring(1, ind));
		}
		return sb.toString();
	}

	public static String getViolationsInfo(Set<ConstraintViolation<Object>> violations) {
		StringBuffer sb = new StringBuffer();
		for (ConstraintViolation<Object> constrain : violations) {
			sb.append(constrain.getPropertyPath().toString());
			sb.append(" >> ");
			sb.append(constrain.getMessage());
			sb.append(" ");
		}
		return sb.toString();
	}

	public static SupportedLanguage getSuppotedLanguage(String lang) {
		for (SupportedLanguage supportedLanguage : supportedLanguages) {
			if (supportedLanguage.getLocale().getLanguage().equals(lang)) {
				return supportedLanguage;
			}
		}
		return SupportedLanguage.English; // English is the default language
	}

	public static void zipFile(final ZipOutputStream zipOutputStream, ByteArrayOutputStream baos, final File file, String entryName, long maxFileSize)
			throws Exception {
		ZipEntry zipEntry = new ZipEntry(entryName);
		zipOutputStream.putNextEntry(zipEntry);
		RandomAccessFile randomAccessFile = null;
		try {
			// if (maxFileSize > 0 && file.length() > maxFileSize) {
			//
			// zipOutputStream.write(msg.getBytes("UTF-8"));
			// randomAccessFile = new RandomAccessFile(file, "r");
			// randomAccessFile.seek(file.length() - maxFileSize);
			//
			// } else {
			randomAccessFile = new RandomAccessFile(file, "r");
			// }
			byte[] buffer = new byte[1024 * 8];
			int read = -1;
			while ((read = randomAccessFile.read(buffer)) != -1) {
				zipOutputStream.write(buffer, 0, read);
				if (baos.size() > maxFileSize) {
					String msg = "\r\n*****\r\n***** File truncated. FileSize=" + file.length() + ", FileTruncTo=" + maxFileSize + ", FileName="
							+ file.getAbsolutePath() + " ***** \r\n";
					logger.warn(msg);
					zipOutputStream.write(msg.getBytes("UTF-8"));
					break;
				}
			}

		} catch (IOException exp) {
			throw exp;
		} finally {
			try {
				randomAccessFile.close();
			} catch (Exception exp) {
			}
		}
		zipOutputStream.closeEntry();

	}

	public static Object getFieldInstance(Object obj, String fieldPath) {
		String fields[] = fieldPath.split("#");
		for (String field : fields) {
			obj = getField(obj, obj.getClass(), field);
			if (obj == null) {
				return null;
			}
		}
		return obj;
	}

	public static Object getField(Object obj, Class<?> clazz, String fieldName) {
		for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
			try {
				Field field;
				field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
				return field.get(obj);
			} catch (Exception e) {
			}
		}
		return null;
	}

	public static Locale getLocaleFromDisplayName(String displayLanguage) {
		for (Locale locale : Locale.getAvailableLocales()) {
			if (locale.getDisplayLanguage().equals(displayLanguage)) {
				return locale;
			}
		}
		return null;
	}

	public static Properties getSystemProperties() {
		try {
			Properties properties = System.getProperties();
			if (logger.isDebugEnabled()) {
				dumpVars("Property: ", new HashMap(properties));
			}
			return properties;
		} catch (Exception e) {
			logger.warn("Cannot retrieve system properties", e);
		}
		return new Properties();
	}

	public static Map<String, String> getSystemEnv() {
		try {
			Map<String, String> environment = System.getenv();
			if (logger.isDebugEnabled()) {
				dumpVars("Environment: ", environment);
			}
			return environment;
		} catch (Exception e) {
			logger.warn("Cannot retrieve system environment", e);
		}
		return null;
	}

	private static void dumpVars(String prefix, Map<String, ?> m) {
		List<String> keys = new ArrayList<String>(m.keySet());
		Collections.sort(keys);
		for (String k : keys) {
			logger.debug(prefix + k + ":\t" + m.get(k));
		}
	}

	public static byte[] createQRCode(String qrCodeData, int qrCodeheight, int qrCodewidth) throws WriterException, IOException {
		Hashtable<EncodeHintType, Object> hintMap = new Hashtable<>();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		BitMatrix matrix = new MultiFormatWriter().encode(qrCodeData, BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(matrix, "PNG", os);
		return os.toByteArray();

	}

	public static Exception reloadTaskNodes(Class<?> klass) {
		return reloadTaskNodes(klass, TenantIdResolver.getCurrentTenantName(), null);
	}

	public static Exception reloadTaskNodes(Class<?> klass, String tenantName, String info) {
		return reloadTaskNodes(klass.getAnnotation(Named.class).value(), tenantName, info);
	}

	public static Exception reloadTaskNodes(Class<?> klass, String tenantName) {
		return reloadTaskNodes(klass.getAnnotation(Named.class).value(), tenantName, null);
	}

	public static Exception reloadTaskNodes(String className, String tenantName, String info) {
		IExecutorService executorService = DcemCluster.getDcemCluster().getExecutorService();
		Set<Member> members = DcemCluster.getDcemCluster().getMembers();
		Exception exception = null;
		for (Member member : members) {
			Future<Exception> future = executorService.submitToMember(new ReloadTask(className, tenantName, info), member);
			try {
				exception = future.get();
			} catch (Exception e) {
				logger.warn("Member: " + member.getAddress() + "  Reloading: " + className + ", " + e.toString());
				return e;
			}
			if (exception != null) {
				logger.warn("Member: " + member.getAddress() + "  Reloading: " + className + ", " + exception.toString());
				return exception;
			}
		}
		return null;
	}

	public static Exception reloadTaskNode(Class<?> klass, String tenantName, String info, Member member) {
		IExecutorService executorService = DcemCluster.getDcemCluster().getExecutorService();
		Future<Exception> future = executorService.submitToMember(new ReloadTask(klass.getAnnotation(Named.class).value(), tenantName, info), member);
		Exception exception = null;
		try {
			exception = future.get();
		} catch (Exception e) {
			logger.warn("Member: " + member.getAddress() + "  Reloading: " + klass.getName() + ", " + e.toString());
			return e;
		}
		if (exception != null) {
			logger.warn("Member: " + member.getAddress() + "  Reloading: " + klass.getName() + ", " + exception.toString());
			return exception;
		}
		return null;
	}

	// public static List<DcemUploadFile> getMailContents(Message message) throws Exception {
	//
	// if (message.isMimeType("text/plain") || message.isMimeType("text/html")) {
	// File tempFile = File.createTempFile("dcem-", "-mail");
	// Files.write(tempFile.toPath(), message.getContent().toString().getBytes(Charsets.UTF_8));
	// List<DcemUploadFile> files = new ArrayList<>(1);
	// DcemMediaType dcemMediaType = DcemMediaType.XHTML;
	// if (message.isMimeType("text/plain")) {
	// dcemMediaType = DcemMediaType.TEXT;
	// }
	// files.add(new DcemUploadFile("MailBody", tempFile, dcemMediaType));
	// return files;
	// } else if (message.isMimeType("multipart/*")) {
	// Multipart multiPart = (Multipart) message.getContent();
	// List<DcemUploadFile> files = new ArrayList<>(multiPart.getCount());
	// File tempFile;
	// DcemMediaType dcemMediaType = null;
	// for (int i = 0; i < multiPart.getCount(); i++) {
	// tempFile = File.createTempFile("dcem-", "-mail");
	// MimeBodyPart bodyPart = (MimeBodyPart) multiPart.getBodyPart(i);
	// if (bodyPart.getContentType().toLowerCase().startsWith("text/plain")) {
	// dcemMediaType = DcemMediaType.TEXT;
	// } else if (bodyPart.getContentType().toLowerCase().startsWith("text/html")) {
	// dcemMediaType = DcemMediaType.XHTML;
	// } else if (bodyPart.getContent() instanceof MimeMultipart) {
	// System.out.println("DcemUtils.getMailContents()");
	// } else {
	// dcemMediaType = null;
	// }
	// bodyPart.saveFile(tempFile);
	// files.add(new DcemUploadFile(bodyPart.getFileName(), tempFile, dcemMediaType));
	// }
	// return files;
	// } else {
	// return null;
	// }
	// }
	//
	// private static String getMimeMultipart(MimeMultipart mimeMultipart, List<DcemUploadFile> files) throws Exception {
	// StringBuffer sb = new StringBuffer();
	// int count = mimeMultipart.getCount();
	// for (int i = 0; i < count; i++) {
	// BodyPart bodyPart = mimeMultipart.getBodyPart(i);
	// if (bodyPart.isMimeType("text/plain")) {
	// sb.append('\n');
	// sb.append(bodyPart.getContent());
	// break; // without break same text appears twice in my tests
	// } else if (bodyPart.isMimeType("text/html")) {
	// sb.append((String) bodyPart.getContent());
	// } else if (bodyPart.getContent() instanceof MimeMultipart) {
	// sb.append(getmMimeMultipart((MimeMultipart) bodyPart.getContent()));
	// }
	// }
	// return sb.toString();
	// }

	public static String getMailBodyContent(Message message) throws Exception {
		if (message.isMimeType("text/plain") || message.isMimeType("text/html")) {
			return message.getContent().toString();
		} else if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			return getTextFromMimeMultipart(mimeMultipart);
		} else {
			return "";
		}
	}

	private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws Exception {
		StringBuffer sb = new StringBuffer();
		int count = mimeMultipart.getCount();
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				sb.append('\n');
				sb.append(bodyPart.getContent());
				break; // without break same text appears twice in my tests
			} else if (bodyPart.isMimeType("text/html")) {
				sb.append((String) bodyPart.getContent());
			} else if (bodyPart.getContent() instanceof MimeMultipart) {
				sb.append(getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent()));
			}
		}
		return sb.toString();
	}

	static DateFormat dayFormat = new SimpleDateFormat(DcemConstants.DAY_FORMAT);

	public static Date dateConverter(String dateStr) {
		Date date = new Date();
		try {
			if (dateStr.length() > 10) {
				date = Date.from(java.time.ZonedDateTime.parse(dateStr).toInstant());
			} else {
				date = dayFormat.parse(dateStr);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}

	public static byte[] resizeImage(byte[] image, int maxWidth, int maxHeight, int maxLength) throws DcemException {
		return resizeImage(image, maxWidth, maxHeight, maxLength, true);
	}

	public static byte[] resizeImage(byte[] image, int maxWidth, int maxHeight, int maxLength, boolean center) throws DcemException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(image);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024 * 4);
		BufferedImage inputImage;
		try {
			inputImage = ImageIO.read(inputStream);
		} catch (IOException e1) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, null, e1);
		}
		BufferedImage outputImage = resizeImage(inputImage, maxWidth, maxHeight, center);
		try {
			ImageIO.write(outputImage, "jpeg", outputStream);
		} catch (Exception e) {
			logger.error("Couldn't resize image", e);
		}
		if (outputStream.toByteArray().length > maxLength) {
			throw new DcemException(DcemErrorCodes.IMAGE_TOO_BIG, "wrong widht or height");
		}
		return outputStream.toByteArray();
	}

	public static byte[] convertImageToJpeg(byte[] image) throws Exception {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(image);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024 * 4);
		BufferedImage inputImage;
		inputImage = ImageIO.read(inputStream);
		ImageIO.write(inputImage, "jpeg", outputStream);
		return outputStream.toByteArray();
	}

	public static BufferedImage resizeImage(BufferedImage inputImage, int targetWidth, int targetHeight, boolean center) throws DcemException {
		int imageWidth = inputImage.getWidth();
		int imageHeight = inputImage.getHeight();

		double originalAspect = (double) imageWidth / imageHeight;
		double targetAspect = (double) targetWidth / targetHeight;

		int scaledWidth = imageWidth;
		int scaledHeight = imageHeight;

		if (originalAspect > targetAspect) {
			// Image is wider than target, scale by height
			scaledHeight = targetHeight;
			scaledWidth = (int) (targetHeight * originalAspect);
		} else {
			// Image is taller than target, scale by width
			scaledWidth = targetWidth;
			scaledHeight = (int) (targetWidth / originalAspect);
		}
		ResampleOp resampleOp = new ResampleOp(scaledWidth, scaledHeight);
		// Always use BufferedImage.TYPE_INT_RGB for JPEG output
		BufferedImage destImage = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
		BufferedImage resizedImage = resampleOp.filter(inputImage, destImage);

		// Crop the image from bottom and right side
		int x = 0;
		if (center) {
			x = (scaledWidth - targetWidth) / 2; // Center the crop horizontally
		}
		int y = 0;
		return resizedImage.getSubimage(x, y, targetWidth, targetHeight);
	}

	public static byte[] resizeImage(byte[] image, int maxLength) throws Exception {
		if (maxLength != 0) {
			return DcemUtils.resizeImage(image, DcemConstants.PHOTO_WIDTH, DcemConstants.PHOTO_HEIGHT, maxLength);
		}
		return DcemUtils.resizeImage(image, DcemConstants.PHOTO_WIDTH, DcemConstants.PHOTO_HEIGHT, DcemConstants.PHOTO_MAX);

	}

	public static TimeZone getTimeZoneFromCountry(String country) {
		TimeZone timeZone;
		if (country == null || country.isEmpty()) {
			return TimeZone.getDefault();
		}
		switch (country) {
		case "DE":
		case "FR":
		case "IT":
		case "MT":
			timeZone = TimeZone.getTimeZone("Europe/Berlin");
			break;
		case "GB":
			timeZone = TimeZone.getTimeZone("Europe/London");
			break;
		default:
			timeZone = TimeZone.getDefault();
			break;
		}
		return timeZone;

	}

	public static Date getDayEnd(LocalDate localDate) {
		Instant instant = Instant.from(localDate.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()));
		return Date.from(instant);
	}

	public static Date convertToDate(LocalDateTime localDateTime) {
		if (localDateTime == null) {
			return null;
		}
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	public static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.contains("win");
	}

	public static LocalDateTime convertEpoch(long epoch) {
		Instant instant = Instant.ofEpochMilli(epoch);
		return instant.atZone(ZoneId.of("UTC")).toLocalDateTime();
	}

	public static String getContinentFromTimezone(TimeZone timezone) {
		return getContinentFromTimezone(timezone.getID());
	}

	public static String getContinentFromTimezone(String timezoneAsString) {
		int ind = timezoneAsString.indexOf("/");
		if (ind > 0) {
			return timezoneAsString.substring(0, ind);
		} else {
			return DcemConstants.TIME_ZONE_OTHER;
		}
	}

	public static List<SelectItem> getContinentTimezones() {
		String[] timezones = TimeZone.getAvailableIDs();
		List<SelectItem> list = new ArrayList<SelectItem>();
		HashSet<String> continents = new HashSet<String>();
		for (String id : timezones) {
			String continent = getContinentFromTimezone(id);
			if (continents.contains(continent) == false && continent.equals(DcemConstants.TIME_ZONE_OTHER) == false) {
				continents.add(continent);
				list.add(new SelectItem(continent, continent));
			}
		}
		list.add(new SelectItem(DcemConstants.TIME_ZONE_OTHER, DcemConstants.TIME_ZONE_OTHER));
		return list;
	}

	public static List<SelectItem> getCountryTimezones(String continentTimezone) {
		List<SelectItem> list = new ArrayList<SelectItem>();
		if (continentTimezone == null) {
			return list;
		}
		String[] timezones = TimeZone.getAvailableIDs();
		for (String id : timezones) {
			String continent = getContinentFromTimezone(id);
			if (continentTimezone.equals(DcemConstants.TIME_ZONE_OTHER) && continent.equals(continentTimezone)) {
				list.add(new SelectItem(id, id.replace('_', ' ')));
			} else if (continent.equals(continentTimezone)) {
				list.add(new SelectItem(id, id.substring(continentTimezone.length() + 1).replace('_', ' ')));
			}
		}
		return list;
	}

	public static String formatDate(Locale locale, LocalDate date) {
		return Objects.isNull(date) == true ? "" : DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale).format(date);
	}

	public static String formatDateTime(String locale, LocalDateTime dateTime) {
		return formatDateTime(new Locale(locale), dateTime);
	}

	public static String formatDateTime(Locale locale, LocalDateTime dateTime) {
		return Objects.isNull(dateTime) == true ? "" : DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale).format(dateTime);
	}

	public static String formatDate(String locale, LocalDate date) {
		return formatDate(new Locale(locale), date);
	}

	public static String decideFontColor(String hexColor) {
		int[] rgb = hexToRGB(hexColor);
		double luminance = calculateLuminance(rgb[0], rgb[1], rgb[2]);
		return (luminance > 0.5) ? "black" : "white";
	}

	private static int[] hexToRGB(String hexColor) {
		hexColor = hexColor.replace("#", "");
		int r = Integer.parseInt(hexColor.substring(0, 2), 16);
		int g = Integer.parseInt(hexColor.substring(2, 4), 16);
		int b = Integer.parseInt(hexColor.substring(4, 6), 16);
		return new int[] { r, g, b };
	}

	private static double calculateLuminance(int red, int green, int blue) {
		double r = red / 255.0;
		double g = green / 255.0;
		double b = blue / 255.0;
		return 0.2126 * r + 0.7152 * g + 0.0722 * b;
	}

}
