package com.doubleclue.dcem.dev.gui;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.doubleclue.dcem.core.gui.DcemApplicationBean;
import com.doubleclue.dcem.core.gui.DcemView;
import com.doubleclue.dcem.core.gui.JsfUtils;
import com.doubleclue.dcem.core.logic.module.DcemModule;
import com.doubleclue.dcem.dev.logic.DevelopmentConstants;
import com.doubleclue.dcem.dev.subjects.CreateModuleSubject;
import com.doubleclue.dcem.dev.utils.DevUtils;
import com.doubleclue.utils.ProductVersion;

@SuppressWarnings("serial")
@Named("createModuleView")
@SessionScoped
public class CreateModuleView extends DcemView {

	private static String FREEMARKER_RESOURCES = "com/doubleclue/dcem/dev/resources/freemarker/";
	private static String FREEMARKER_MODULE_TEMPLATE = FREEMARKER_RESOURCES + "Module.java.ftl";
	private static String FREEMARKER_PREFERENCES_TEMPLATE = FREEMARKER_RESOURCES + "Preferences.java.ftl";
	private static String FREEMARKER_CSS_TEMPLATE = FREEMARKER_RESOURCES + "Styles.css.ftl";


	private final String IAM_DIRECTORY = "IAM-Password-Manager";
	private final String DCEM_PACKAGENAME = "com/doubleclue/dcem/";

	@Inject
	private CreateModuleSubject createModuleSubject;

	@Inject
	DcemApplicationBean applicationBean;

	String dcemModuleString;
	DcemModule selectedDcemModule = null;

	String moduleId;
	String moduleName;

	boolean overwriteAllFiles;
	String rootSources;
	String moduleResources;
	String moduleDirectory;
	String entitySources;

	@PostConstruct
	private void init() {
		subject = createModuleSubject;
		File file = new File(DevelopmentConstants.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String directory = file.getAbsolutePath();
		int ind = directory.indexOf(IAM_DIRECTORY);
		if (ind != -1) {
			rootSources = directory.substring(0, ind);
		}
	}

	public void actionOk() throws Exception {

		moduleName = Character.toUpperCase(moduleName.charAt(0)) + moduleName.substring(1);
		String moduleFullName = moduleName + "Module";
		String packageName = DCEM_PACKAGENAME + moduleId + "/";

		try {
			HashMap<String, String> map = new HashMap<>();
			File moduleDirectoy = new File(rootSources + moduleFullName);
			if (overwriteAllFiles == true) {
				moduleDirectoy.delete();
			}
			moduleDirectoy.mkdir();
			File sourceDirectory = new File(moduleDirectoy, "/src/main/java");
			sourceDirectory.mkdirs();

			File resourceDirectory = new File(moduleDirectoy, "/src/main/resources");
			resourceDirectory.mkdirs();
			// writing pom file
			FileWriter writer;
			
			map.put("moduleId", Character.toLowerCase(moduleId.charAt(0)) + moduleId.substring(1));
			map.put("ModuleId", Character.toUpperCase(moduleId.charAt(0)) + moduleId.substring(1));
			ProductVersion productVersion = applicationBean.getProductVersion();
			map.put("DcemVersion", productVersion.getVersionStr());
			map.put("ModuleName", moduleName);
			map.put("ModuleFullName", moduleFullName);

			writeToFile (moduleDirectoy, "pom.xml", null, map);
			
			// create directories
			File entitiesPackage = new File(sourceDirectory, packageName + "entities");
			entitiesPackage.mkdirs();

			File guiPackage = new File(sourceDirectory, packageName + "gui");
			guiPackage.mkdirs();

			File logicPackage = new File(sourceDirectory, packageName + "logic");
			logicPackage.mkdirs();

			
			File preferencesPackage = new File(sourceDirectory, packageName + "preferences");
			preferencesPackage.mkdirs();
			
			writeToFile (preferencesPackage, map.get("ModuleId") + "Preferences.java", FREEMARKER_PREFERENCES_TEMPLATE, map);
			
			File subjectPackage = new File(sourceDirectory, packageName + "subjects");
			subjectPackage.mkdirs();
			writeToFile (subjectPackage, "PreferencesSubject.java", null, map);

			writeToFile (logicPackage,  moduleFullName + ".java", FREEMARKER_MODULE_TEMPLATE,  map);

			File textResourceDirectory = new File(resourceDirectory, packageName + "resources");
			textResourceDirectory.mkdirs();
			
			writeToFile (textResourceDirectory, "Messages.properties", null, map);
			writeToFile (textResourceDirectory, "Messages_de.properties", null, map);
			

			File metaInfFile = new File(resourceDirectory, "META-INF");
			metaInfFile.mkdir();
			File metaInfResourcesFile = new File(metaInfFile, "resources");
			File cssFile = new File(metaInfResourcesFile, "css");
			cssFile.mkdirs();
			writeToFile (cssFile, moduleId + "Styles.css", FREEMARKER_CSS_TEMPLATE, map);

			File htmlFile = new File(metaInfResourcesFile, "mgt/modules/" + moduleId);
			htmlFile.mkdirs();
			writeToFile (metaInfFile, "faces-config.xml", null, map);
			writeToFile (metaInfFile, "persistence.xml", null, map);
			writeToFile (metaInfFile, "beans.xml", null, map);
						
			JsfUtils.addInfoMessage("READY See Module at: " + moduleDirectoy.getAbsolutePath() + ". Now import Project as 'Existing Maven Projects'");

		} catch (Exception e) {
			JsfUtils.addErrorMessage(e.toString());
			return;
		}
	}

	public CreateModuleSubject getCreateModuleSubject() {
		return createModuleSubject;
	}

	public void setCreateModuleSubject(CreateModuleSubject createModuleSubject) {
		this.createModuleSubject = createModuleSubject;
	}

	public String getModuleId() {
		return moduleId;
	}

	public void setModuleId(String moduleId) {
		this.moduleId = moduleId;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

	public boolean isOverwriteAllFiles() {
		return overwriteAllFiles;
	}

	public void setOverwriteAllFiles(boolean overwriteAllFiles) {
		this.overwriteAllFiles = overwriteAllFiles;
	}

	private void writeToFile(File packageFile, String filename, String templateName,  HashMap<String, String> map) throws Exception {
		File moduleFile = new File(packageFile, filename);
		if (moduleFile.exists() == false || overwriteAllFiles == true) {
			if (templateName == null) {
				templateName = FREEMARKER_RESOURCES + filename + ".ftl";
			}
			String content = DevUtils.getTemplateContent(templateName);
			freemarker.template.Template template = dcemApplication.getTemplateFromConfig(filename, content);
			FileWriter writer = new FileWriter(moduleFile);
			template.process(map, writer);
			JsfUtils.addInfoMessage(filename + " created succesful");
			writer.close();
		}
	}

}
