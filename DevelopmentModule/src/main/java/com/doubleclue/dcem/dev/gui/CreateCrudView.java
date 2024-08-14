package com.doubleclue.dcem.dev.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Entity;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.gui.ViewVariable;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.core.utils.DcemUtils;
import com.doubleclue.dcem.core.utils.DisplayModes;
import com.doubleclue.dcem.dev.logic.DevObjectTypes;
import com.doubleclue.dcem.dev.subjects.CreateCrudSubject;
import com.doubleclue.dcem.system.logic.SystemModule;
import com.doubleclue.utils.KaraUtils;

@SuppressWarnings("serial")
@Named("createCrudView")
@SessionScoped
public class CreateCrudView extends DcemView {

	private static final String CLASS_FILE_NAME = "ClassFileName";
	private static String FREEMARKER_RESOURCES = "com/doubleclue/dcem/dev/resources/freemarker/";
	private static String FREEMARKER_SUBJECT_TEMPLATE = FREEMARKER_RESOURCES + "Subject.ftl";
	private static String FREEMARKER_VIEW_TEMPLATE = FREEMARKER_RESOURCES + "View.ftl";
	private static String FREEMARKER_DIALOG_TEMPLATE = FREEMARKER_RESOURCES + "Dialog.ftl";
	private static String FREEMARKER_LOGIC_TEMPLATE = FREEMARKER_RESOURCES + "Logic.ftl";
	private static String FREEMARKER_XHTML_TEMPLATE = FREEMARKER_RESOURCES + "DialogXhtml.ftl";

	private static String TABS = "\t\t\t\t";
	private final static String MAP_MODULE_CLASS_SIMPLE = "ModuleClass";

	private static final String MAP_DIALOG_TABLE = "dialogTable";
	private static final String MAP_DIALOG_METHODS = "dialogMethods";
	private static final String MAP_DIALOG_VARIABLES = "dialogVariables";
	private static final String SRC_MAIN_RESOURCE = "/src/main/resources/";
	private static final String SRC_MAIN_JAVA = "/src/main/java/";
	private static final String SRC = "/src/";
	private static final String ADMIN = "/admin/";
	private static final String CORE = "/core/";
	private static final String RESOURCE = "/resources/";
	private static final String MAP_LOGIC_METHODS = "logicMethods";
	private static final String ENTITY = "Entity";
	private static final String NAMED_CLASS = "namedClassName";

	@Inject
	private CreateCrudSubject createCredSubject;

	@Inject
	DcemApplicationBean applicationBean;

	String dcemModuleString;
	DcemModule selectedDcemModule = null;
	String entityClassName = "com.doubleclue.dcem.";
	String viewIcon;
	boolean autoDialog;
	boolean autoView;
	boolean overwriteAllFiles;
	List<String> entities;
	String moduleSources;
	String moduleResources;
	String moduleDirectory;
	String entitySources;
	String entityPackage;

	@PostConstruct
	private void init() {
		subject = createCredSubject;
	}

	public void actionOk() throws Exception {
		if (selectedDcemModule == null) {
			JsfUtils.addErrorMessage("Please select a Module");
			return;
		}
		Class<?> clazz;
		try {
			clazz = Class.forName(entityPackage + entityClassName);
		} catch (Throwable e) {
			JsfUtils.addErrorMessage("Entity not found: " + e);
			return;
		}
		try {
			Entity entity = clazz.getAnnotation(Entity.class);
		} catch (Exception e) {
			JsfUtils.addErrorMessage("Entity Class is not annotated with @Entity: " + e);
			return;
		}

		if (entityClassName.endsWith(ENTITY) == false) {
			JsfUtils.addErrorMessage("Entity Class must ends eith 'Entity'");
			return;
		}

		HashMap<String, String> map = new HashMap<>();
		map.put("ModuleId", selectedDcemModule.getId());
		map.put(MAP_MODULE_CLASS_SIMPLE, selectedDcemModule.getClass().getSuperclass().getSimpleName());
		String entityName = clazz.getSimpleName();
		map.put("EntityName", entityName);
		String entityNameVariable = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1);
		map.put("EntityNameVariable", entityNameVariable);
		map.put("IconName", viewIcon);
		String viewXhtml = null;

		if (autoView == true) {
			map.put("ViewPath", "\"" + DcemConstants.AUTO_VIEW_PATH + "\"");
			map.put("EntityClass", clazz.getSimpleName());
		} else {
			viewXhtml = "\"/modules/" + selectedDcemModule.getId() + "/" + entityName + ".xhtml\"";
			map.put("ViewPath", viewXhtml);
			map.put("EntityClass", "null");
		}
		String classFileName = entityName.substring(0, entityName.length() - ENTITY.length());
		if (autoDialog == false) {
			map.put("DialogPath", "\"/modules/" + selectedDcemModule.getId() + "/" + classFileName + "Dialog.xhtml\"");
		} else {
			map.put("DialogPath", "\"null\"");
		}
		map.put(CLASS_FILE_NAME, classFileName);
		map.put(NAMED_CLASS,  Character.toLowerCase(classFileName.charAt(0)) + classFileName.substring(1));

		createJavaFile(clazz, map, entityName, FREEMARKER_SUBJECT_TEMPLATE, DevObjectTypes.Subject);
		createJavaFile(clazz, map, entityName, FREEMARKER_VIEW_TEMPLATE, DevObjectTypes.View);

		// createView(clazz, sources, map, entityName);
		if (autoDialog == false) {
			createDialogTable(clazz, entityName, entityName, map);
			createJavaFile(clazz, map, entityName, FREEMARKER_DIALOG_TEMPLATE, DevObjectTypes.Dialog);
			createJavaFile(clazz, map, entityName, FREEMARKER_XHTML_TEMPLATE, DevObjectTypes.DialogXhtml);
		}
		createJavaFile(clazz, map, entityName, FREEMARKER_LOGIC_TEMPLATE, DevObjectTypes.Logic);
		return;
	}

	private void createDialogTable(Class<?> clazz, String viewName, String entityName, HashMap<String, String> map) throws Exception {
		ResourceBundle resourceBundle = ResourceBundle.getBundle(selectedDcemModule.getResourceName(), operatorSessionBean.getLocale());
		List<ViewVariable> viewVariables = DcemUtils.getViewVariables(clazz, resourceBundle, viewName, null);
		StringBuffer sb = new StringBuffer();
		String entityNameVariable = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1);
		String resourcBundleName = Character.toUpperCase(selectedDcemModule.getId().charAt(0)) + selectedDcemModule.getId().substring(1) + "Msg";
		StringBuffer dialogMethods = new StringBuffer();
		StringBuffer dialogVariables = new StringBuffer();
		StringBuffer logicMethods = new StringBuffer();
		for (ViewVariable viewVariable : viewVariables) {
			if (viewVariable.isVisible() == false) {
				continue;
			}
			if (viewVariable.getDcemGui().displayMode() == DisplayModes.TABLE_ONLY) {
				continue;
			}
			String variableId = viewVariable.getId();
			String addName = "Name";
			int ind = variableId.indexOf(".");
			if (ind != -1) {
				char ch = Character.toUpperCase(variableId.charAt(ind + 1));
				variableId = variableId.substring(0, ind) + ch + variableId.substring(ind + 2);
				addName = "";
			}
			String variableIdUpper = Character.toUpperCase(variableId.charAt(0)) + variableId.substring(1);
			sb.append("\n");
			sb.append(TABS);
			sb.append("<p:outputLabel for=\"@next\" value=\"#{" + resourcBundleName + "['" + viewVariable.getId() + "']} \" />\n");
			sb.append(TABS);
			String required = viewVariable.getDcemGui().required() ? "true" : "false";
			switch (viewVariable.getVariableType()) {
			case STRING:
				if (viewVariable.getDcemGui().autoComplete()) {
					sb.append("<p:autoComplete id=\"" + viewVariable.getId() + "\" minQueryLength=\"1\" queryDelay=\"1000\"" + " value=\"#{"
							+ entityNameVariable + "Dialog." + variableId + addName + "}\" completeMethod=\"#{" + entityNameVariable + "Dialog.autoComplete"
							+ variableIdUpper + "}\" />\n ");
					dialogMethods.append("\tpublic String get");
					dialogMethods.append(variableIdUpper + addName);
					dialogMethods.append("() {\n");
					dialogMethods.append(TABS);
					dialogMethods.append("return ");
					dialogMethods.append(variableId + addName);
					dialogMethods.append(";\n}\n\n");

					dialogMethods.append("\tpublic void set");
					dialogMethods.append(variableIdUpper + addName);
					dialogMethods.append("(String name) {\n");
					dialogMethods.append(TABS);
					dialogMethods.append("this." + variableId + addName + " = name;\n");
					dialogMethods.append("\treturn;\n\t}\n");

					dialogVariables.append("String ");
					dialogVariables.append(variableId + addName + ";\n");

					addAutoComplete(dialogMethods, logicMethods, viewVariable, variableIdUpper, entityName);
				} else if (viewVariable.getDcemGui().choose().length > 0) {

				} else {
					sb.append("<p:inputText id=\"" + viewVariable.getId() + "\" required=\"" + required + "\" value=\"#{" + entityNameVariable);
					sb.append("Dialog.actionObject." + viewVariable.getId() + "}\" />\n");
				}
				break;
			case BOOLEAN:
				sb.append("<p:toggleSwitch id=\"" + viewVariable.getId() + "\" value=\"#{" + entityNameVariable + "Dialog.actionObject." + viewVariable.getId()
						+ "}\" />\n");
				break;
			case DATE_TIME:
				sb.append("<p:datePicker id=\"" + viewVariable.getId() + "\" value=\"#{" + entityNameVariable + "Dialog.actionObject." + viewVariable.getId()
						+ "}\" " + " pattern=\"#{operatorSession.dateTimePattern}\" showTime=\"true\" yearNavigator=\"true\" />\n");
				break;
			case DATE:
				sb.append("<p:datePicker id=\"" + viewVariable.getId() + "\" value=\"#{" + entityNameVariable + "Dialog.actionObject." + viewVariable.getId()
						+ "}\" " + " pattern=\"#{operatorSession.dateTimePattern}\" showTime=\"false\" yearNavigator=\"true\" />\n");
				break;
			case TIME:
				sb.append("<p:datePicker id=\"" + viewVariable.getId() + "\" value=\"#{" + entityNameVariable + "Dialog.actionObject." + viewVariable.getId()
						+ "}\" " + " timeOnly=\"true\" pattern=\"HH:mm\" />\n");
				break;
			case NUMBER:
				sb.append("<p:inputNumber id=\"" + viewVariable.getId() + "\" value=\"#{" + entityNameVariable + "Dialog.actionObject." + viewVariable.getId()
						+ "}\" required=\"" + required + "\" />\n");
				break;
			case ENUM:
				sb.append("<p:selectOneMenu id=\"" + viewVariable.getId() + "\" value=\"#{" + entityNameVariable + "Dialog.actionObject." + viewVariable.getId()
						+ "}\" " + "  >\n");
				sb.append(TABS);
				sb.append("\t<f:selectItems value=\"#{" + entityNameVariable + "Dialog." + viewVariable.getId() + "Enums}\"" + " />\n");
				sb.append(TABS);
				sb.append("</p:selectOneMenu>\n");

				dialogMethods.append("public List<SelectItem> get");
				dialogMethods.append(variableIdUpper);
				dialogMethods.append("Enums () {\n");
				dialogMethods.append(TABS);
				dialogMethods.append("ResourceBundle resourceBundle = JsfUtils.getBundle(" + map.get(MAP_MODULE_CLASS_SIMPLE)
						+ ".RESOURCE_NAME, operatorSessionBean.getLocale());\n");
				dialogMethods.append(TABS);
				dialogMethods.append("List<SelectItem> list = new ArrayList<>();\n");
				dialogMethods.append(TABS);
				dialogMethods.append("for (" + viewVariable.getKlass().getName() + " enumObject :  " + viewVariable.getKlass().getName() + ".values()) {\n");
				dialogMethods.append(TABS);
				dialogMethods.append("\t list.add (new SelectItem (enumObject.name() , JsfUtils.getStringSafely(resourceBundle, enumObject.name())));\n");
				dialogMethods.append(TABS + "}\n" + TABS + "return list;\n}\n");
				break;
			case UNKNOWN:
				String subVariable = viewVariable.getDcemGui().subClass();
				if (subVariable == null || subVariable.isEmpty()) {
					JsfUtils.addWarnMessage("Uknown Class Variable without a 'subClass definition: " + viewVariable.getId());
				}
				sb.append("<p:outputLabel id=\"" + viewVariable.getId() + "\" style=\"color: red\" value=\" No subClass defined \" />\n");
				break;
			default:
				System.out.println("CreateCrudView.createDialogTable() Unknown Type: " + viewVariable.toString());
				sb.append("<p:inputText id=\"" + viewVariable.getId() + "\" value=\"#{" + entityNameVariable + "Dialog.actionObject." + viewVariable.getId()
						+ "}\" />\n");
				break;
			}
		}
		map.put(MAP_LOGIC_METHODS, logicMethods.toString());
		map.put(MAP_DIALOG_METHODS, dialogMethods.toString());
		map.put(MAP_DIALOG_VARIABLES, dialogVariables.toString());
		map.put(MAP_DIALOG_TABLE, sb.toString());
	}

	private void addAutoComplete(StringBuffer dialogMethods, StringBuffer logicMethods, ViewVariable viewVariable, String variableIdUpper, String entityName) {
		String entityNameVariable = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1);
		dialogMethods.append("public List<String> autoComplete" + variableIdUpper);
		dialogMethods.append("(String name) {\n");
		dialogMethods.append("\ttry {\n");
		dialogMethods.append(TABS);
		dialogMethods.append("return " + entityNameVariable + "Logic.getAutoCompleteList" + variableIdUpper + " (name, 50);\n");
		dialogMethods.append("\t} catch (Throwable e) {\r\n" + "		JsfUtils.addErrorMessage(e.getMessage());\r\n"
				+ "		logger.error(\"autocomplete \" + name, e);\r\n" + "		return null;\r\n" + "	}\n}\n");

		logicMethods.append("\tpublic List<String> getAutoCompleteList" + variableIdUpper + " (String name, int max) {\n");
		logicMethods.append(TABS);
		logicMethods.append("TypedQuery<String> query = em.createNamedQuery(" + entityName + ".GET_AUTO_COMPLETR_" + variableIdUpper + ", String.class);\n");
		logicMethods.append(TABS);
		logicMethods.append("query.setParameter(1, \"%\" + name + \"%\");\n");
		logicMethods.append(TABS);
		logicMethods.append("query.setMaxResults(max);\n");
		logicMethods.append(TABS);
		logicMethods.append("return query.getResultList();\n}\n");
		return;
	}

	private void createJavaFile(Class<?> clazz, HashMap<String, String> map, String entityName, String templateName, DevObjectTypes devObjectTypes) {
		// create subject Class
		FileWriter writer = null;
		String packageName = null;
		String classFileName = map.get(CLASS_FILE_NAME);
		switch (devObjectTypes) {
		case Dialog:
			// dcemApplication.removeFreeMarkerTemplate(clazz.getName() + devObjectTypes.name());
		case View:
			packageName = "/gui/";
			break;
		case Logic:
			packageName = "/logic/";
			break;
		case Subject:
			// dcemApplication.removeFreeMarkerTemplate(clazz.getName() + devObjectTypes.name());
			packageName = "/subjects/";
			break;
		case DialogXhtml:
			break;
		}
		try {
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(templateName);
			if (inputStream == null) {
				JsfUtils.addErrorMessage(templateName + " Not found");
				return;
			}
			String templateContent = KaraUtils.readInputStreamText(inputStream);
			freemarker.template.Template template = dcemApplication.getTemplateFromConfig(clazz.getName() + devObjectTypes.name(), templateContent);
			File viewFile;
			if (devObjectTypes == DevObjectTypes.DialogXhtml) {
				String dialogPath = map.get("DialogPath");
				String xhtmlDirectory = moduleResources + "META-INF/resources/mgt" + dialogPath.substring(1, dialogPath.length() - 1);
				viewFile = new File(xhtmlDirectory);
			} else {
				viewFile = new File(moduleSources + packageName + classFileName + devObjectTypes.name() + ".java");
			}
			if (overwriteAllFiles == false) {
				if (viewFile.exists()) {
					throw new DevException("File Already exists : " + viewFile.getAbsolutePath());
				}
			}
			writer = new FileWriter(viewFile);
			template.process(map, writer);
			writer.close();
			JsfUtils.addInfoMessage(devObjectTypes.name() + ": created at " + viewFile.toString());
		} catch (DevException e) {
			JsfUtils.addErrorMessage(e.getMessage());
		} catch (Exception e) {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e2) {
				}
			}
			logger.error(devObjectTypes, e);
			JsfUtils.addErrorMessage("Something went wrong while creating : " + devObjectTypes + " Exception: " + e);
			return;
		}
	}

	public void onChangeModule() {
		selectedDcemModule = null;
		List<DcemModule> modules = applicationBean.getSortedModules();
		for (DcemModule dcemModule2 : modules) {
			if (dcemModule2.getId().equals(dcemModuleString)) {
				selectedDcemModule = dcemModule2;
				break;
			}
		}
		if (selectedDcemModule == null) {
			JsfUtils.addErrorMessage("Please select a Module");
			return;
		}
		try {
			String packageName = selectedDcemModule.getClass().getSuperclass().getName();
			if (selectedDcemModule.getId() == AdminModule.MODULE_ID || selectedDcemModule.getId() == SystemModule.MODULE_ID) {
				packageName = "com.doubleclue.dcem.core";
			} else {
				int ind = packageName.lastIndexOf(".");
				ind = packageName.lastIndexOf(".", ind - 1);
				packageName = packageName.substring(0, ind);
			}
			entityPackage = packageName + ".entities.";
			String resource = "/" + selectedDcemModule.getClass().getSuperclass().getName().replace('.', '/') + ".class";
			URL scannedUrl = this.getClass().getResource(resource);
			String fileString = scannedUrl.getFile();
			String sources = fileString.replace("/bin/", SRC);

			int ind2 = sources.lastIndexOf("/" + selectedDcemModule.getId() + "/");
			if (ind2 == -1) {
				ind2 = sources.lastIndexOf("/com/doubleclue/dcem/");
				int ind3 = sources.indexOf("/", ind2 + "/com/doubleclue/dcem/".length());
				moduleSources = sources.substring(0, ind3 + 1);
			} else {
				moduleSources = sources.substring(0, ind2 + selectedDcemModule.getId().length() + 2);
			}

			File moduleSourcesDir = new File(moduleSources);
			if (moduleSourcesDir.exists() == false) {
				moduleSources = moduleSources.replace("/src/", SRC_MAIN_JAVA);
				moduleResources = moduleSources.substring(0, moduleSources.indexOf(SRC_MAIN_JAVA)) + SRC_MAIN_RESOURCE;
				moduleDirectory = moduleResources.substring(0, moduleResources.length() - SRC_MAIN_RESOURCE.length());
			} else {
				moduleResources = moduleSources.substring(0, moduleSources.indexOf(SRC)) + RESOURCE;
				moduleDirectory = moduleResources.substring(0, moduleResources.length() - RESOURCE.length());
			}
			System.out.println("CreateCrudView.onChangeModule() moduleSources: " + moduleSources);

			if (selectedDcemModule.getId() == AdminModule.MODULE_ID || selectedDcemModule.getId() == SystemModule.MODULE_ID) {
				entitySources = moduleSources.substring(0, moduleSources.lastIndexOf(ADMIN)) + CORE + "entities" + File.separator;
			} else {
				entitySources = moduleSources + "entities" + File.separator;
			}
			File entityDirectory = new File(entitySources);
			entities = new ArrayList<String>();
			for (String name : entityDirectory.list()) {
				int indx = name.lastIndexOf('.');
				if (indx == -1) {
					continue;
				}
				name = name.substring(0, indx);
				if (name.endsWith("_")) {
					continue;
				}
				Class entityClass = Class.forName(entityPackage + name);
				if (entityClass.getAnnotation(Entity.class) != null) {
					entities.add(name);
					try {
						entityClass = Class.forName(entityPackage + name + "_");
					} catch (Exception e) {
						JsfUtils.addErrorMessage("Entity META Data is Missing for : " + name + "_ . Please create the Meta-Files.");
					}
				}

			}
		} catch (Exception e) {
			JsfUtils.addErrorMessage("couldn't Find module sources: " + e.toString());
		}
	}

	public List<SelectItem> getModules() {
		List<DcemModule> modules = applicationBean.getSortedModules();
		List<SelectItem> items = new ArrayList<SelectItem>();
		for (DcemModule dcemModule : modules) {
			items.add(new SelectItem(dcemModule.getId(), dcemModule.getName()));
		}
		return items;
	}

	public String getDcemModuleString() {
		return dcemModuleString;
	}

	public void setDcemModuleString(String dcemModuleString) {
		this.dcemModuleString = dcemModuleString;
	}

	public boolean isAutoDialog() {
		return autoDialog;
	}

	public void setAutoDialog(boolean autoDiag) {
		this.autoDialog = autoDiag;
	}

	public boolean isAutoView() {
		return autoView;
	}

	public void setAutoView(boolean autoView) {
		this.autoView = autoView;
	}

	public String getEntityClassName() {
		return entityClassName;
	}

	public void setEntityClassName(String entityClassName) {
		this.entityClassName = entityClassName;
	}

	public String getViewIcon() {
		return viewIcon;
	}

	public void setViewIcon(String viewIcon) {
		this.viewIcon = viewIcon;
	}

	public List<String> getEntities() {
		return entities;
	}

	public void setEntities(List<String> entities) {
		this.entities = entities;
	}

	public String getModuleSources() {
		return moduleSources;
	}

	public void setModuleSources(String moduleSources) {
		this.moduleSources = moduleSources;
	}

	public String getModuleResources() {
		return moduleResources;
	}

	public void setModuleResources(String moduleResources) {
		this.moduleResources = moduleResources;
	}

	public String getEntityPackage() {
		return entityPackage;
	}

	public void setEntityPackage(String entityPackage) {
		this.entityPackage = entityPackage;
	}

	public DcemModule getSelectedDcemModule() {
		return selectedDcemModule;
	}

	public void setSelectedDcemModule(DcemModule selectedDcemModule) {
		this.selectedDcemModule = selectedDcemModule;
	}

	public String getModuleDirectory() {
		return moduleDirectory;
	}

	public void setModuleDirectory(String moduleDirectory) {
		this.moduleDirectory = moduleDirectory;
	}

	public boolean isOverwriteAllFiles() {
		return overwriteAllFiles;
	}

	public void setOverwriteAllFiles(boolean overwriteAllFiles) {
		this.overwriteAllFiles = overwriteAllFiles;
	}

}
