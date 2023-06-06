package com.doubleclue.dcem.core.utils;

import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
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
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.persistence.Convert;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.SingularAttribute;
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
import com.doubleclue.dcem.core.jpa.EpochDateConverter;
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
					Field fieldx = metaClass.getField(field.getName());
					attributes.add((SingularAttribute<?, ?>) metaClass.getField(field.getName()).get(metaClass));
					// attributes.add((SingularAttribute<?, ?>)
					// metaClass.getDeclaredField(field.getName()).get(metaClass));
				} else {
					// attributes.add((SingularAttribute<?, ?>)
					// metaClass.getDeclaredField(dcemGui.dbMetaAttributeName()).get(metaClass));
					attributes.add((SingularAttribute<?, ?>) metaClass.getField(dcemGui.dbMetaAttributeName()).get(metaClass));
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
		} else if ((cls.equals(Date.class)) || (cls.equals(Timestamp.class))
				|| (cls.equals(java.sql.Date.class) || (cls.equals(LocalDateTime.class) || (cls.equals(LocalDate.class))))) {
			Convert convert = field.getAnnotation(Convert.class);
			if (convert != null && convert.converter().equals(EpochDateConverter.class)) {
				variableType = VariableType.EPOCH_DATE;
			} else {
				variableType = VariableType.DATE;
			}
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
				if (cls.equals(List.class)) {
					variableType = VariableType.LIST;
					if (dcemGui.filterValue().isEmpty() == false) {
						filterValue = dcemGui.filterValue();
					}
					Type [] types = ((ParameterizedType)field.getGenericType()).getActualTypeArguments();
//					types[0];
//					((ParameterizedType) field.getClass()
//				            .getGenericSuperclass()).getActualTypeArguments();
					
					
				}
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
						ViewVariable retViewVariable = new ViewVariable(field.getName() + "." + subViewVariable.getId(), displayName, helpText, variableType,
								dcemGui, value, attributes);
						retViewVariable.setKlass(subViewVariable.getKlass());
						if (objectKlass == null) {
							FilterItem filterItem = new FilterItem(retViewVariable.getId(), filterValue, filterToValue, dcemGui.filterOperator(), 0,
									dcemGui.sortOrder());
							retViewVariable.setFilterItem(filterItem);
						}
						return retViewVariable;
					} else {
						variableType = subViewVariable.getVariableType();
						FilterItem filterItem = new FilterItem(field.getName(), filterValue, filterToValue, dcemGui.filterOperator(), 0, dcemGui.sortOrder());
						ViewVariable retViewVariable = new ViewVariable(field.getName(), displayName, helpText, variableType, dcemGui, value, attributes,
								filterItem);
						retViewVariable.setKlass(subViewVariable.getKlass());
						return retViewVariable;
					}
				} catch (NoSuchFieldException | SecurityException exp2) {
					logger.error("SubClass field not found: " + subClass, exp2);
					return null;
				}

			} else {
				variableType = VariableType.OTHER;
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
		viewVariable.setKlass(cls);
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
			if (viewVariable.getDcemGui().displayMode() == DisplayModes.TABLE_ONLY) {
				continue;
			}
			if (viewVariable.getDcemGui().separator().isEmpty() == false) {
				HtmlOutputText sep = new HtmlOutputText();
				sep.setValue("-");
				sep.setStyle("display:block; width: 100%; background-color: lightgrey");
				htmlPanelGrid.getChildren().add(sep);
				HtmlOutputText sep2 = new HtmlOutputText();
				sep2.setValue(viewVariable.getDcemGui().separator());
				sep2.setStyle("display:block; width: 100%; background-color: lightgrey");
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
					helpButton.setStyle("padding:2px;color:#005078;cursor:pointer;font-size: 1.2em;margin-left: 0.5em;");
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
	 * @param oldObject Object
	 * @param newObject Object
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws DcemException
	 */
	public static String compareObjects(final Object oldObject, final Object newObject) throws DcemException {
		try {
			String unicodeRightArrow = " > ";
			String fin = ";  ";
			StringBuilder stringBuilder = new StringBuilder();
			Class<?> oldObjectClass = oldObject.getClass();
			Class<?> newObjectClass = newObject.getClass();
			Field newField;
			int modifiers;
			for (Field oldField : oldObjectClass.getDeclaredFields()) {
				modifiers = oldField.getModifiers();
				if (Modifier.isStatic(modifiers) || (Modifier.isFinal(modifiers)) || (Modifier.isTransient(modifiers))) {
					continue;
				}
				if (oldField.isAnnotationPresent(javax.persistence.Version.class)) {
					continue;
				}
				newField = newObjectClass.getDeclaredField(oldField.getName());
				newField.setAccessible(true);
				oldField.setAccessible(true);
				Class<?> cls = oldField.getType();
				if (cls.equals(String.class)) {
					String oldString = (((String) oldField.get(oldObject)) != null) ? (String) oldField.get(oldObject) : "null";
					String newString = (((String) newField.get(newObject)) != null) ? (String) newField.get(newObject) : "null";
					if (oldString.equals(newString) == false) {
						stringBuilder.append(oldField.getName());
						stringBuilder.append(": ");
						DcemGui dcemGui = oldField.getAnnotation(DcemGui.class);
						if (dcemGui != null && dcemGui.password()) {
							stringBuilder.append(newString.equals("null") ? newString : ":PASSWORD:");
						} else {
							stringBuilder.append(oldString);
							stringBuilder.append(unicodeRightArrow);
							stringBuilder.append(newString);
						}
						stringBuilder.append(fin);
					}
				} else if ((cls.equals(Integer.class)) || (cls.equals(int.class))) {
					Integer oldInteger = (Integer) oldField.get(oldObject);
					Integer newInteger = (Integer) newField.get(newObject);
					if ((oldInteger == null && newInteger != null) || (oldInteger != null && newInteger == null)
							|| (oldInteger != null && newInteger != null && oldInteger.intValue() != newInteger.intValue())) {
						stringBuilder.append(oldField.getName());
						stringBuilder.append(": ");
						stringBuilder.append((oldInteger != null ? oldInteger : "null"));
						stringBuilder.append(unicodeRightArrow);
						stringBuilder.append((newInteger != null ? newInteger : "null"));
						stringBuilder.append(fin);
					}
				} else if ((cls.equals(Long.class)) || (cls.equals(long.class))) {
					if (!oldField.getName().equals("jpaVersion")) {
						Long oldInteger = (Long) oldField.get(oldObject);
						Long newInteger = (Long) newField.get(newObject);
						if ((oldInteger == null && newInteger != null) || (oldInteger != null && newInteger == null)
								|| (oldInteger != null && newInteger != null && oldInteger.longValue() != newInteger.longValue())) {
							stringBuilder.append(oldField.getName());
							stringBuilder.append(": ");
							stringBuilder.append((oldInteger != null ? oldInteger : "null"));
							stringBuilder.append(unicodeRightArrow);
							stringBuilder.append((newInteger != null ? newInteger : "null"));
							stringBuilder.append(fin);
						}
					}
				} else if ((cls.equals(Byte.class)) || (cls.equals(byte.class))) {
					Byte oldByte = (Byte) oldField.get(oldObject);
					Byte newByte = (Byte) newField.get(newObject);
					if ((oldByte == null && newByte != null) || (oldByte != null && newByte == null)
							|| (oldByte != null && newByte != null && oldByte.byteValue() != newByte.byteValue())) {
						stringBuilder.append(oldField.getName());
						stringBuilder.append(": ");
						stringBuilder.append((oldByte != null ? oldByte : "null"));
						stringBuilder.append(unicodeRightArrow);
						stringBuilder.append((newByte != null ? newByte : "null"));
						stringBuilder.append(fin);
					}
				} else if (cls.equals(boolean.class)) {
					Boolean oldBoolean = (Boolean) oldField.get(oldObject);
					Boolean newBoolean = (Boolean) newField.get(newObject);
					if ((oldBoolean == null && newBoolean != null) || (oldBoolean != null && newBoolean == null)
							|| (oldBoolean != null && newBoolean != null && oldBoolean.booleanValue() != newBoolean.booleanValue())) {
						stringBuilder.append(oldField.getName());
						stringBuilder.append(": ");
						stringBuilder.append((oldBoolean != null ? oldBoolean : "null"));
						stringBuilder.append(unicodeRightArrow);
						stringBuilder.append((newBoolean != null ? newBoolean : "null"));
						stringBuilder.append(fin);
					}
				} else if (cls.equals(Date.class)) {
					Date oldDate = (Date) oldField.get(oldObject);
					Date newDate = (Date) oldField.get(newObject);
					if ((oldDate == null && newDate != null) || (oldDate != null && newDate == null)
							|| (oldDate != null && newDate != null && oldDate.compareTo(newDate) != 0)) {
						stringBuilder.append(oldField.getName());
						stringBuilder.append(": ");
						stringBuilder.append((oldDate != null ? DateUtils.formatDateTime(oldDate) : "null"));
						stringBuilder.append(unicodeRightArrow);
						stringBuilder.append((newDate != null ? DateUtils.formatDateTime(newDate) : "null"));
						stringBuilder.append(fin);
					}
				} else if (cls.equals(Map.class)) {
					Map<Object, Object> oldMap = (Map) oldField.get(oldObject);
					Map<Object, Object> newMap = (Map) oldField.get(newObject);
					List<Object> oldMapKeys = new ArrayList<Object>(oldMap.keySet());
					for (Object object : oldMapKeys) {
						if (oldMap.get(object).equals(newMap.get(object)) == false) {
							stringBuilder.append(object.toString());
							stringBuilder.append(": ");
							stringBuilder.append(oldMap.get(object).toString());
							stringBuilder.append(unicodeRightArrow);
							stringBuilder.append(newMap.get(object).toString());
							stringBuilder.append(fin);
						}
					}
				} else {
					if (oldField.getAnnotation(DcemGui.class) != null || oldField.getName().equals(DcemConstants.USERPORTAL_PREFERNCESES_TYPE_COMPARE)) {
						Object oldFieldValue = oldField.get(oldObject);
						Object newFieldValue = oldField.get(newObject);
						if (oldFieldValue == null && newFieldValue == null) {
							continue;
						}
						if (oldFieldValue == null && newFieldValue != null) {
							stringBuilder.append(oldField.getName());
							stringBuilder.append(": ");
							stringBuilder.append("NULL");
							stringBuilder.append(unicodeRightArrow);
							stringBuilder.append(newFieldValue.toString());
							stringBuilder.append(fin);
						} else if (oldFieldValue != null && newFieldValue == null) {
							stringBuilder.append(oldField.getName());
							stringBuilder.append(": ");
							stringBuilder.append(oldFieldValue.toString());
							stringBuilder.append(unicodeRightArrow);
							stringBuilder.append("NULL");
							stringBuilder.append(fin);
						} else {
							if (oldFieldValue.equals(newFieldValue) == false) {
								stringBuilder.append(oldField.getName());
								stringBuilder.append(": ");
								stringBuilder.append(oldFieldValue.toString());
								stringBuilder.append(unicodeRightArrow);
								stringBuilder.append(newFieldValue.toString());
								stringBuilder.append(fin);
							}
						}
					}
				}
			}
			return stringBuilder.toString();
		} catch (Exception exp) {
			throw new DcemException(DcemErrorCodes.COMPARE_OBJECTS, oldObject.getClass().getName(), exp);
		}
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

	public static String processTemplate(String template, HashMap<String, String> map) throws DcemException {

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
					if (operatorSessionBean.isPermission(reveal, manage) == false) {
						Method method = null;
						// for the moment only strings !!!
						method = getSetterMethodForField(field, entity.getClass(), String.class);
						method.invoke(entity, (String) "---");
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

			} catch (NoSuchMethodException exp) {

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

		//
		//
		// Map<Member, Future<Exception>> results = executorService.submitToAllMembers(new ReloadTask(className, tenantName));
		// Exception exception = null;
		// for (Member member : results.keySet()) {
		// Future<Exception> future = results.get(member);
		// try {
		// exception = future.get();
		// if (exception != null) {
		// logger.warn("Member: " + member.getAddress() + " Reloading: " + className + ", " + exception.toString());
		// return exception;
		// }
		// } catch (Exception e) {
		// logger.warn("Member: " + member.getAddress() + " Reloading: " + className + ", " + e.toString());
		// return e;
		// }
		// }
		return null;
	}

	public static String getMailBodyContent(Message message) throws MessagingException, IOException {
		message.getContentType();
		if (message.isMimeType("text/plain") || message.isMimeType("text/html")) {
			return message.getContent().toString();
		} else if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			return getTextFromMimeMultipart(mimeMultipart);
		} else {
			return "";
		}
	}

	private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
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

	public static byte[] resizeImage(byte[] image, int maxWidth, int maxHeight, int maxLenght, boolean force) throws DcemException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(image);
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(image.length);
		BufferedImage inputImage;
		try {
			inputImage = ImageIO.read(inputStream);
		} catch (IOException e1) {
			throw new DcemException(DcemErrorCodes.UNEXPECTED_ERROR, null, e1);
		}
		// check size of image
		int imageWidth = inputImage.getWidth();
		int imageHeight = inputImage.getHeight();
		if (force == false && imageHeight <= maxHeight && imageWidth <= maxHeight) {
			if (image.length > maxLenght) {
				throw new DcemException(DcemErrorCodes.IMAGE_TOO_BIG, "wrong widht or height");
			}
			return image;
		}
		if (imageWidth > maxWidth || imageHeight > maxHeight) {
			float factx = (float) imageWidth / maxWidth;
			float facty = (float) imageHeight / maxHeight;
			float fact = (factx > facty) ? factx : facty;
			imageWidth = (int) ((int) imageWidth / fact);
			imageHeight = (int) ((int) imageHeight / fact);
		}
		BufferedImageOp resampler = new ResampleOp(imageWidth, imageHeight);
		BufferedImage outputImage = resampler.filter(inputImage, null);
		try {
			ImageIO.write(outputImage, "png", outputStream);
		} catch (Exception e) {
			logger.error("Couldn't resize image", e);
			
		}
		if (outputStream.toByteArray().length > maxLenght) {
			throw new DcemException(DcemErrorCodes.IMAGE_TOO_BIG, "wrong widht or height");
		}
		return outputStream.toByteArray();
	}

	public static byte[] resizeImage(byte[] image, int maxLength) throws Exception {
		try {
			return DcemUtils.resizeImage(image, DcemConstants.PHOTO_WIDTH, DcemConstants.PHOTO_HEIGHT, DcemConstants.PHOTO_MAX, false);
		} catch (DcemException exp) {
			if (exp.getErrorCode() == DcemErrorCodes.IMAGE_TOO_BIG) {
				return DcemUtils.resizeImage(image, DcemConstants.PHOTO_WIDTH_MIN, DcemConstants.PHOTO_HEIGHT_MIN, DcemConstants.PHOTO_MAX, true);
			}
			throw exp;
		}
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
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	public static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		return os.contains("win");
	}

}
