package Pages;

import java.io.IOException;

import org.openqa.selenium.By;

import TestBase.TestBase;
import Utils.ActionHelper;
import Utils.ExtentReportManager;

public class OrgPage {
	private final ActionHelper actions;
	private final TestBase testBase;
	private static final By orgName = By.name("name");
    private static final By createBtn = By.xpath("//*[@id=\"__next\"]/main/div/main/div/div/div/form/button/span");
    
    public OrgPage(TestBase testBase) throws IOException {
        this.testBase = testBase;
    	this.actions = new ActionHelper(testBase);
		verifyPageLoaded();
    }

    private void verifyPageLoaded() throws IOException {
        actions.waitForPageReadyState("MemberPage");
        ExtentReportManager.logInfo("Member dashboard loaded successfully");
    }
    
    public OrgPage provideName(String name) throws IOException {
    	actions.clearAndType(orgName, "TestOrg", "Create Org");
    	ExtentReportManager.logInfo("Provided name and clicked create.");
    	return this;
    }
    
    public OrgPage clickCreateBtn() throws IOException {
    	actions.click(createBtn, "Create Btn");
    	ExtentReportManager.logInfo("Clicked the Create new Organization button.");
    	return this;
    }
    
}
