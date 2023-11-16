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
import com.doubleclue.dcem.core.exceptions.DcemException;
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
import com.microsoft.graph.models.Admin;

@SuppressWarnings("serial")
@Named("createCrudView")
@SessionScoped
public class CreateCrudView extends DcemView {

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
	private static final String SRC_MAIN_RESOURCE = "/src/main/resources/";
	private static final String SRC_MAIN_JAVA = "/src/main/java/";
	private static final String SRC = "/src/";
	private static final String RESOURCE = "/resources/";

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
			map.put("ViewPath", DcemConstants.AUTO_VIEW_PATH);
			map.put("EntityClass", clazz.getSimpleName());
		} else {
			viewXhtml = "\"/modules/" + selectedDcemModule.getId() + "/" + entityName + ".xhtml\"";
			map.put("ViewPath", viewXhtml);
			map.put("EntityClass", "null");
		}

		createJavaFile(clazz, map, entityName, FREEMARKER_SUBJECT_TEMPLATE, DevObjectTypes.Subject);
		createJavaFile(clazz, map, entityName, FREEMARKER_VIEW_TEMPLATE, DevObjectTypes.View);
		createJavaFile(clazz, map, entityName, FREEMARKER_LOGIC_TEMPLATE, DevObjectTypes.Logic);

		// createView(clazz, sources, map, entityName);
		if (autoDialog == false) {
			createDialogTable(clazz, entityName, entityName, map);
			createJavaFile(clazz, map, entityName, FREEMARKER_DIALOG_TEMPLATE, DevObjectTypes.Dialog);
			createJavaFile(clazz, map, entityName, FREEMARKER_XHTML_TEMPLATE, DevObjectTypes.DialogXhtml);
		}
		return;
	}

	private void createDialogTable(Class<?> clazz, String viewName, String entityName, HashMap<String, String> map) throws Exception {
		ResourceBundle resourceBundle = ResourceBundle.getBundle(selectedDcemModule.getResourceName(), operatorSessionBean.getLocale());
		List<ViewVariable> viewVariables = DcemUtils.getViewVariables(clazz, resourceBundle, viewName, null);
		StringBuffer sb = new StringBuffer();
		String entityNameVariable = Character.toLowerCase(entityName.charAt(0)) + entityName.substring(1);
		String resourcBundleName = Character.toUpperCase(selectedDcemModule.getId().charAt(0)) + selectedDcemModule.getId().substring(1) + "Msg";

		for (ViewVariable viewVariable : viewVariables) {
			if (viewVariable.isVisible() == false) {
				continue;
			}
			if (viewVariable.getDcemGui().displayMode() == DisplayModes.TABLE_ONLY) {
				continue;
			}
			String variableIdUpper = Character.toUpperCase(viewVariable.getId().charAt(0)) + viewVariable.getId().substring(1);
			sb.append("\n");
			sb.append(TABS);
			sb.append("<p:outputLabel for=\"@next\" value=\"#{" + resourcBundleName + "['" + viewVariable.getId() + "']} \" />\n");
			sb.append(TABS);
			switch (viewVariable.getVariableType()) {
			case STRING:
				sb.append("<p:inputText id=\"" + viewVariable.getId() + "\" required=\"true\" value=\"#{" + entityNameVariable + "Dialog.actionObject."
						+ viewVariable.getId() + "}\" />\n");
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
						+ "}\" " + "  />\n");
				break;
			case ENUM:
				sb.append("<p:selectOneMenu id=\"" + viewVariable.getId() + "\" value=\"#{" + entityNameVariable + "Dialog.actionObject." + viewVariable.getId()
						+ "}\" " + "  >\n");
				sb.append(TABS);
				sb.append("\t<f:selectItems value=\"#{" + entityNameVariable + "Dialog." + viewVariable.getId() + "Enums}\"" + " />\n");
				sb.append(TABS);
				sb.append("</p:selectOneMenu>\n");

				StringBuffer sbJava = new StringBuffer();
				sbJava.append("public List<SelectItem> get");
				sbJava.append(variableIdUpper);
				sbJava.append("Enums () {\n");
				sbJava.append(TABS);
				sbJava.append("ResourceBundle resourceBundle = JsfUtils.getBundle(" + map.get(MAP_MODULE_CLASS_SIMPLE)
						+ ".RESOURCE_NAME, operatorSessionBean.getLocale());\n");
				sbJava.append(TABS);
				sbJava.append("List<SelectItem> list = new ArrayList<>();\n");
				sbJava.append(TABS);
				sbJava.append("for (" + viewVariable.getKlass().getName() + " enumObject :  " + viewVariable.getKlass().getName() + ".values()) {\n");
				sbJava.append(TABS);
				sbJava.append("\t list.add (new SelectItem (enumObject.name() , JsfUtils.getStringSafely(resourceBundle, enumObject.name())));\n");
				sbJava.append(TABS + "}\n" + TABS + "return list;\n}\n");

				map.put(MAP_DIALOG_METHODS, sbJava.toString());
				break;
			case UNKNOWN:
				String subVariable = viewVariable.getDcemGui().subClass();
				if (subVariable == null || subVariable.isEmpty()) {
					JsfUtils.addWarnMessage("Uknown Class Variable without a 'subClass definition: " + viewVariable.getId());
				}
				break;
			default:
				System.out.println("CreateCrudView.createDialogTable() Unknown Type: " + viewVariable.toString());
				sb.append("<p:inputText id=\"" + viewVariable.getId() + "\" value=\"#{" + entityNameVariable + "Dialog.actionObject." + viewVariable.getId()
						+ "}\" />\n");
				break;
			}
		}
		map.put(MAP_DIALOG_TABLE, sb.toString());
	}

	private void createJavaFile(Class<?> clazz, HashMap<String, String> map, String entityName, String templateName, DevObjectTypes devObjectTypes) {
		// create subject Class
		FileWriter writer = null;
		String packageName = null;
		switch (devObjectTypes) {
		case Dialog:
			dcemApplication.removeFreeMarkerTemplate(clazz.getName() + devObjectTypes.name());
		case View:
			packageName = "/gui/";
			break;
		case Logic:
			packageName = "/logic/";
			break;
		case Subject:
			dcemApplication.removeFreeMarkerTemplate(clazz.getName() + devObjectTypes.name());
			packageName = "/subjects/";
			break;
		case DialogXhtml:

			break;
		}
		try {
			InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(templateName);
			if (inputStream == null) {
				JsfUtils.addErrorMessage(templateName + " Nor found");
				return;
			}
			String templateContent = KaraUtils.readInputStreamText(inputStream);

			freemarker.template.Template template = dcemApplication.getTemplateFromConfig(clazz.getName() + devObjectTypes.name(), templateContent);
			File viewFile;
			if (devObjectTypes == DevObjectTypes.DialogXhtml) {
				String xhtmlDirectory = moduleResources + "/META-INF/resources/mgt/modules/" + selectedDcemModule.getId() + "/";
				viewFile = new File(xhtmlDirectory + entityName + ".xhtml");
			} else {
				viewFile = new File(moduleSources + packageName + entityName + devObjectTypes.name() + ".java");
			}
			writer = new FileWriter(viewFile);
			template.process(map, writer);
			writer.close();
			JsfUtils.addInfoMessage(devObjectTypes.name() + ": created at " + viewFile.toString());
		} catch (Exception e) {
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e2) {
				}
			}
			logger.error(devObjectTypes, e);
			JsfUtils.addErrorMessage("Something went wron while creating : " + devObjectTypes + " Exception: " + e);
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
				packageName = "com.doubleclue.dcen.core.entities";
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

			entitySources = moduleSources + "entities" + File.separator;
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

}
