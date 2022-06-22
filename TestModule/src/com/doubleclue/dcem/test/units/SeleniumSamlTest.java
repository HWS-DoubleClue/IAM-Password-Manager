package com.doubleclue.dcem.test.units;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.doubleclue.dcem.as.policy.PolicyLogic;
import com.doubleclue.dcem.core.DcemConstants;
import com.doubleclue.dcem.core.cluster.DcemCluster;
import com.doubleclue.dcem.saml.gui.SpMetadataDialog;
import com.doubleclue.dcem.saml.logic.SamlLogic;
import com.doubleclue.dcem.saml.logic.SamlModule;
import com.doubleclue.dcem.test.gui.SamlTestServiceView;
import com.doubleclue.dcem.test.logic.AbstractTestUnit;
import com.doubleclue.dcem.test.logic.SeleniumApplication;
import com.doubleclue.dcem.test.logic.TestUnitGroupEnum;

@ApplicationScoped
@Named("SeleniumSamlTest")
public class SeleniumSamlTest extends AbstractTestUnit {
	private static final String POLICY_NAME = "Test_SP_Policy";
	private static final String SERVICE_PROVIDER_NAME = "Test_Service_Provider";
	private static final String ATTRIBUTE_STATIC_TEXT = "Saml Attribute Input Test";
	private static final String ATTRIBUTE_DOMAIN_ATTRIBUTE = "SN";

	@Inject
	SamlModule samlModule;

	@Inject
	SamlLogic samlLogic;

	@Inject
	PolicyLogic policyLogic;

	@Inject
	SamlTestServiceView samlTestServiceView;

	@Inject
	SeleniumApplication seleniumApplication;

	@Inject
	SpMetadataDialog spMetadataDialog;

	@Override
	public String getDescription() {
		return "This unit will setup DCEM for saml using the GUI, simulate a SP and test DCEM as an IdP";
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
		// Get a list of all possible SAML attributes
		List<String> serviceProviderAttributeList = new ArrayList<String>();
		for (SelectItem selectItem : spMetadataDialog.getUserPropertyTypes()) {
			serviceProviderAttributeList.add(selectItem.getLabel());
		}

		seleniumApplication.initSeleniumTest();
		boolean finalPageReached = false;
		try {
			// Delete test policy and service provider in DCEM if already present
			samlLogic.deleteSpMetadataEntity(SamlTestServiceView.SP_ENTITY_ID);
			policyLogic.deletePolicyEntity(POLICY_NAME);

			// Start the Selenium web driver and log in
			WebDriver driver = seleniumApplication.initDriver();
			seleniumApplication.performStandardLogin(false);

			// Configure SSO Domain in SAML preferences
			driver.findElement(By.xpath("//*[@id=\"menuForm:hbPanelMenu\"]/div[6]")).click();
			Thread.sleep(250);
			driver.findElement(By.xpath("//*[@id=\"menuForm:hbPanelMenu_5\"]/ul/li[2]")).click();
			seleniumApplication.writeInput(driver.findElement(By.xpath("//*[@id=\"preferencesForm:ssoDomain\"]")),
					"https://" + DcemCluster.getDcemCluster().getNodeName() + ":8443");
			seleniumApplication.writeInput(driver.findElement(By.xpath("//*[@id=\"preferencesForm:idpEntityId\"]")),
					DcemCluster.getDcemCluster().getNodeName() + ":8443");
			driver.findElement(By.xpath("/html/body/div[2]/div/span/form/div/div/button")).click();
			Thread.sleep(250);

			// Add a test service provider
			driver.findElement(By.xpath("//*[@id=\"menuForm:hbPanelMenu_5\"]/ul/li[1]")).click();
			driver.findElement(By.xpath("/html/body/div[2]/div/span/div[2]/form/button[1]")).click();
			seleniumApplication.switchToDialog();
			driver.findElement(By.xpath("/html/body/form[1]/table/tbody/tr/td/div/div[2]/table/tbody/tr[1]")).click();
			driver.findElement(By.xpath("/html/body/form[1]/button[1]")).click();
			seleniumApplication.writeInput(driver.findElement(By.xpath("/html/body/form[1]/table/tbody/tr[1]/td[2]/input")), SERVICE_PROVIDER_NAME);
			seleniumApplication.writeInput(driver.findElement(By.xpath("/html/body/form[1]/div[2]/div/div[1]/table/tbody/tr[6]/td/textarea")),
					samlTestServiceView.getSpMetadataXml());
			Thread.sleep(250);

			// Add attributes for verifying the content of the response and the logged in user
			for (String serviceProviderAttribute : serviceProviderAttributeList) {
				driver.findElement(By.xpath("/html/body/form[1]/div[2]/ul/li[4]")).click();
				driver.findElement(By.xpath("/html/body/form[1]/div[2]/div/div[4]/button[1]")).click();
				Thread.sleep(250);
				seleniumApplication.writeInput(driver.findElement(By.xpath("/html/body/form[2]/div/div[2]/table/tbody/tr[1]/td[2]/input")),
						serviceProviderAttribute);
				driver.findElement(By.xpath("//*[@id=\"attributeForm:attrType\"]")).click();
				Thread.sleep(250);
				seleniumApplication.selectItemInListOrTable(driver.findElement(By.xpath("//*[@id=\"attributeForm:attrType_items\"]")), serviceProviderAttribute,
						"li");
				if (serviceProviderAttribute.equals("Static Text")) {
					seleniumApplication.writeInput(driver.findElement(By.xpath("//*[@id=\"attributeForm:attrValue\"]")), ATTRIBUTE_STATIC_TEXT);
				}
				if (serviceProviderAttribute.equals("Domain Attribute")) {
					seleniumApplication.writeInput(driver.findElement(By.xpath("//*[@id=\"attributeForm:attrValue\"]")), ATTRIBUTE_DOMAIN_ATTRIBUTE);
				}
				driver.findElement(By.xpath("//*[@id=\"attributeForm:attributeOkBtn\"]")).click();
				driver.findElement(By.xpath("/html/body/form[1]/div[2]/ul/li[3]")).click();
			}
			driver.findElement(By.xpath("//*[@id=\"regForm:ok\"]")).click();
			seleniumApplication.switchToBaseWindow();

			// Add a test policy for SAML and apply it to the test service
			driver.findElement(By.xpath("//*[@id=\"menuForm:hbPanelMenu\"]/div[3]")).click();
			Thread.sleep(250);
			driver.findElement(By.xpath("/html/body/div[1]/form/div[2]/div[3]/div/ul/li[7]/a")).click();
			driver.findElement(By.xpath("/html/body/div[2]/div/span/div[2]/form/div/div/div[1]/button[1]")).click();
			seleniumApplication.switchToDialog();
			seleniumApplication.writeInput(driver.findElement(By.xpath("//*[@id=\"policyForm:name\"]")), POLICY_NAME);
			driver.findElement(By.xpath("/html/body/span/form/table/tbody/tr[7]/td[2]/table/tbody/tr[1]/td[2]/div/div[2]")).click();
			driver.findElement(By.xpath("/html/body/span/form/table/tbody/tr[7]/td[2]/table/tbody/tr[1]/td[3]/div/div[2]")).click();
			driver.findElement(By.xpath("/html/body/span/form/table/tbody/tr[7]/td[2]/table/tbody/tr[2]/td[1]/div/div[2]")).click();
			driver.findElement(By.xpath("//*[@id=\"policyForm:ok\"]")).click();
			seleniumApplication.switchToBaseWindow();
			driver.findElement(By.xpath("//*[@id=\"policyForm:tabs\"]/ul/li[2]")).click();
			seleniumApplication.selectItemInListOrTable(driver.findElement(By.xpath("//*[@id=\"policyForm:tabs:treeTable\"]")), SERVICE_PROVIDER_NAME, "tr");
			driver.findElement(By.xpath("/html/body/div[2]/div/span/div[2]/form/div/div/div[2]/button[1]")).click();
			seleniumApplication.switchToDialog();
			driver.findElement(By.xpath("/html/body/span/form/table/tbody/tr[3]/td[2]/div")).click();
			seleniumApplication.selectItemInListOrTable(driver.findElement(By.xpath("/html/body/div[3]/div/ul")), POLICY_NAME, "li");
			driver.findElement(By.xpath("//*[@id=\"policyForm:ok\"]")).click();
			seleniumApplication.switchToBaseWindow();

			// Logout and close current window
			driver.findElement(By.xpath("/html/body/div[2]/form/table/tbody/tr/td[5]/a[3]")).click();
			driver.findElement(By.xpath("/html/body/div[2]/form/table/tbody/tr/td[5]/a[3]/div[1]/div/a")).click();
			seleniumApplication.closeDriver();

			// Perform the SAML test using the test service
			driver = seleniumApplication.initDriver();
			driver.get("https://localhost:8443" + DcemConstants.DEFAULT_WEB_NAME + "/testservice/saml/login_.xhtml");
			seleniumApplication.chromeContinueToUnsafeWebsite();
			driver.findElement(By.xpath("//*[@id=\"loginForm:loginWithDoubleClue\"]")).click();
			seleniumApplication.chromeContinueToUnsafeWebsite();
			seleniumApplication.writeInput(driver.findElement(By.id("loginForm:username")), SeleniumApplication.getSeleniumuserid());
			seleniumApplication.writeInput(driver.findElement(By.id("loginForm:password")), SeleniumApplication.getSeleniumuserpwd());
			driver.findElement(By.xpath("//*[@id=\"loginForm:useAlternative\"]/div[2]")).click();
			driver.findElement(By.xpath("//*[@id=\"loginForm:login\"]")).click();
			driver.findElement(By.xpath("/html/body/div[2]/div[2]/form[2]/div[2]/div[2]/div/div/table/tbody/tr[1]/td[2]/button")).click();

			// Check the return page
			List<WebElement> listTextElement = driver.findElements(By.xpath("//*[contains(text(),'Test Service Provider Return Page')]"));
			if (listTextElement.size() > 0) {
				finalPageReached = true;
			}
			List<WebElement> listErrorElement = driver.findElements(By.xpath("//*[contains(text(),'Error')]"));
			if (listErrorElement.size() > 0) {
				throw new Exception(listErrorElement.get(0).getText());
			}
		} catch (NoSuchElementException exception) {
			throw new Exception("Test failed, Selenium could not find a requested WebElement");
		} catch (Exception exp) {
			throw new Exception("Test failed, " + exp.getLocalizedMessage());
		} finally {
			seleniumApplication.closeDriver();
		}

		if (finalPageReached == true) {
			setInfo("Test passed, final page was reached and all attributes were correct");
		} else {
			throw new Exception("Test failed, final page was not reached");
		}
		return null;
	}

	public static String getPolicyName() {
		return POLICY_NAME;
	}

	public static String getServiceProviderName() {
		return SERVICE_PROVIDER_NAME;
	}

	public static String getAttributeStaticText() {
		return ATTRIBUTE_STATIC_TEXT;
	}
}
