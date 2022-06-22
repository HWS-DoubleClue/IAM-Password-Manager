package com.doubleclue.dcem.test.logic;

import java.time.Duration;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import com.doubleclue.dcem.admin.logic.AdminModule;
import com.doubleclue.dcem.admin.subjects.UserSubject;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.entities.DcemAction;
import com.doubleclue.dcem.core.entities.DcemUser;
import com.doubleclue.dcem.core.gui.SupportedLanguage;
import com.doubleclue.dcem.core.logic.RoleLogic;
import com.doubleclue.dcem.core.logic.UserLogic;
import com.doubleclue.dcem.test.units.AddTenUsersTest;

@ApplicationScoped
@Named("SeleniumApplication")
public class SeleniumApplication {

	@Inject
	TestModule testModule;

	@Inject
	UserLogic userLogic;

	@Inject
	AddTenUsersTest addTenUsersTest;

	@Inject
	RoleLogic roleLogic;

	@Inject
	UserSubject userSubject;

	@Inject
	AdminModule adminModule;

	private WebDriver driver;
	private DcemUser seleniumUser;
	private static final String seleniumUserId = "seleniumUser";
	private static final String seleniumUserName = "Selenium Operator";
	private static final String seleniumUserPrincipalName = "Selenium Principal Name";
	private static final String seleniumUserEmail = "seleniumUser@dummy.com";
	private static final String seleniumUserPwd = "12345";
	private static final String seleniumUserMobile = "0123456789";
	private static final String seleniumUserTelephone = "9876543210";
	private String driverBaseWindow;

	public void initSeleniumTest() throws Exception {
		System.setProperty("webdriver.chrome.driver",
				System.getProperty("user.dir") + "\\..\\TestModule\\src\\META-INF\\resources\\drivers\\seleniumWebdriver\\chromedriver.exe");
		seleniumUser = initSeleniumUser();
	}

	public WebDriver initDriver() {
		driver = new ChromeDriver();
		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
		driver.manage().window().maximize();
		driverBaseWindow = driver.getWindowHandle();
		return driver;
	}

	public void closeDriver() {
		if (driver != null) {
			driver.close();
		}
	}

	private DcemUser initSeleniumUser() throws Exception {
		DcemUser dcemUser = null;

		dcemUser = userLogic.getUser(seleniumUserId);
		if (dcemUser == null) {
			dcemUser = new DcemUser(seleniumUserId);
			dcemUser.setDisplayName(seleniumUserName);
			dcemUser.setUserPrincipalName(seleniumUserPrincipalName);
			dcemUser.setEmail(seleniumUserEmail);
			dcemUser.setMobileNumber(seleniumUserMobile);
			dcemUser.setTelephoneNumber(seleniumUserTelephone);
			dcemUser.setLanguage(SupportedLanguage.English);
			dcemUser.setInitialPassword(seleniumUserPwd);
			dcemUser.setDcemRole(roleLogic.getDcemRole(DcemConstants.SYSTEM_ROLE_SUPERADMIN));
			userLogic.addOrUpdateUser(dcemUser, new DcemAction(userSubject, DcemConstants.ACTION_ADD), true, false,
					adminModule.getPreferences().getUserPasswordLength(), false);
		} else {
			dcemUser.setDisplayName(seleniumUserName);
			dcemUser.setUserPrincipalName(seleniumUserPrincipalName);
			dcemUser.setEmail(seleniumUserEmail);
			dcemUser.setMobileNumber(seleniumUserMobile);
			dcemUser.setTelephoneNumber(seleniumUserTelephone);
			dcemUser.setLanguage(SupportedLanguage.English);
			dcemUser.setInitialPassword(seleniumUserPwd);
			dcemUser.setDcemRole(roleLogic.getDcemRole(DcemConstants.SYSTEM_ROLE_SUPERADMIN));
			userLogic.addOrUpdateUser(dcemUser, new DcemAction(userSubject, DcemConstants.ACTION_EDIT), true, false,
					adminModule.getPreferences().getUserPasswordLength(), false);
		}
		return dcemUser;
	}

	public void performStandardLogin(Boolean logIntoUserportal) throws Exception {
		String area = "/mgt";
		if (logIntoUserportal == true) {
			area = "/userportal";
		}
		driver.get("https://localhost:8443" + DcemConstants.DEFAULT_WEB_NAME + area + "/login.xhtml");
		chromeContinueToUnsafeWebsite();
		writeInput(driver.findElement(By.id("loginForm:name")), seleniumUserId);
		writeInput(driver.findElement(By.id("loginForm:password")), seleniumUserPwd);
		if (logIntoUserportal == true) {
			driver.findElement(By.xpath("/html/body/div[2]/div[2]/form[2]/div[1]/div/div[3]/div/div[1]/div[2]")).click();
			driver.findElement(By.xpath("//*[@id=\"loginForm:login\"]")).click();
			driver.findElement(By.xpath("/html/body/div[2]/div[2]/form[2]/div[2]/div[2]/div/div/table/tbody/tr[1]/td[2]/button")).click();
		} else {
			driver.findElement(By.xpath("//*[@id=\"loginForm:useAlternative\"]/div[2]")).click();
			driver.findElement(By.xpath("//*[@id=\"loginForm:login\"]")).click();
			driver.findElement(By.xpath("/html/body/div[2]/div[2]/form[2]/div[3]/div[2]/div/div/table/tbody/tr[1]/td[2]/button")).click();
		}
	}

	public void writeInput(WebElement webElement, String inputString) throws Exception {
		webElement.click();
		webElement.clear();
		webElement.sendKeys(inputString);
	}

	public void selectItemInListOrTable(WebElement listOrTable, String targetName, String tagName) {
		List<WebElement> entries = listOrTable.findElements(By.tagName(tagName));
		for (WebElement element : entries) {
			if (element.getText().contains(targetName)) {
				element.click();
				break;
			}
		}
	}

	public void switchToBaseWindow() throws Exception {
		Thread.sleep(500);
		driver.switchTo().window(driverBaseWindow);
	}

	public void switchToDialog() throws Exception {
		Thread.sleep(500);
		driver.switchTo().frame(0);
	}

	public void chromeContinueToUnsafeWebsite() {
		driver.findElement(By.id("details-button")).click();
		driver.findElement(By.id("proceed-link")).click();
	}

	public WebDriver getDriver() {
		return driver;
	}

	public void setDriver(WebDriver driver) {
		this.driver = driver;
	}

	public DcemUser getSeleniumUser() {
		return seleniumUser;
	}

	public void setSeleniumUser(DcemUser seleniumUser) {
		this.seleniumUser = seleniumUser;
	}

	public static String getSeleniumuserid() {
		return seleniumUserId;
	}

	public static String getSeleniumusername() {
		return seleniumUserName;
	}

	public static String getSeleniumuserpwd() {
		return seleniumUserPwd;
	}

	public String getDriverBaseWindow() {
		return driverBaseWindow;
	}

	public void setDriverBaseWindow(String driverBaseWindow) {
		this.driverBaseWindow = driverBaseWindow;
	}

	public static String getSeleniumuseremail() {
		return seleniumUserEmail;
	}

	public static String getSeleniumusermobile() {
		return seleniumUserMobile;
	}

	public static String getSeleniumusertelephone() {
		return seleniumUserTelephone;
	}
}
