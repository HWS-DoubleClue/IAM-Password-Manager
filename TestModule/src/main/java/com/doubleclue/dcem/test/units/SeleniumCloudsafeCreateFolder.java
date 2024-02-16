package com.doubleclue.dcem.test.units;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.subjects.UserSubject;
import com.doubleclue.dcem.as.entities.CloudSafeEntity;
import com.doubleclue.dcem.as.logic.CloudSafeLogic;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.SeleniumApplication;
import com.doubleclue.dcem.test.logic.TestModule;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;

@ApplicationScoped
@Named("SeleniumCloudsafeCreateFolder")
public class SeleniumCloudsafeCreateFolder extends AbstractTestUnit {

	@Inject
	RoleLogic roleLogic;

	@Inject
	TestModule testModule;

	@Inject
	UserLogic userLogic;

	@Inject
	CloudSafeLogic cloudSafeLogic;

	@Inject
	AddTenUsersTest addTenUsersTest;

	@Inject
	UserSubject userSubject;

	@Inject
	AdminModule adminModule;

	@Inject
	SeleniumApplication seleniumApplication;

	private static final String FOLDERNAME = "TestFolder";

	@Override
	public String getDescription() {
		return "This unit will execute a short selenium test to create an unprotected Folder in CloudSafe";
	}
	
	@Override
	public String getAuthor() {
		return "Alexander Reißig";
	}

	@Override
	public List<String> getDependencies() {
		return null;
	}	
	
	@Override
	public TestUnitGroupEnum getParent() {
		return TestUnitGroupEnum.SELENIUM;
	}

	@Override
	public boolean isRunnableTest() {
		return true;
	}

	@Override
	public String start() throws Exception {
		seleniumApplication.initSeleniumTest();
		DcemUser seleniumUser = seleniumApplication.getSeleniumUser();

		// Check if the test folder exists for test user and if yes, delete it
		List<CloudSafeEntity> cloudSafeEntityList = cloudSafeLogic.getAllUserCloudSafe(seleniumUser, null);
		for (CloudSafeEntity listEntity : cloudSafeEntityList) {
			if (listEntity.getName().equals(FOLDERNAME)) {
				cloudSafeLogic.deleteCloudSafe(listEntity, seleniumUser, false);
			}
		}

		// Perform the GUI test to create a test folder
		try {
			WebDriver driver = seleniumApplication.initDriver();
			seleniumApplication.performStandardLogin(true);
			driver.findElement(By.id("menuForm:cloudsafeID")).click();
			Thread.sleep(500);
			driver.findElement(By.id("cloudSafeForm:addFolder")).click();
			Thread.sleep(500);
			driver.switchTo().activeElement();
			seleniumApplication.writeInput(driver.findElement(By.id("processFolderForm:addFolderName")), FOLDERNAME);
			driver.findElement(By.xpath("/html/body/div[3]/div/span/span/form[11]/div/div[2]/button")).click();
		} catch (NoSuchElementException exception) {
			throw new Exception("Test failed, Selenium could not find a requested WebElement");
		} catch (Exception exception) {
			throw new Exception("Test failed, Selenium could not find a requested WebElement", exception);
		} finally {
			seleniumApplication.closeDriver();
		}

		// Check if test folder was successfully created for test user
		cloudSafeEntityList = cloudSafeLogic.getAllUserCloudSafe(seleniumUser, null);
		boolean folderCreated = false;
		for (CloudSafeEntity listEntity : cloudSafeEntityList) {
			if (listEntity.getName().equals(FOLDERNAME) && listEntity.isFolder() == true) {
				setInfo("OK, Folder was created");
				folderCreated = true;
				break;
			}
		}
		if (folderCreated == false) {
			throw new Exception("Test failed, Folder was not created");
		}
		return null;
	}
}
