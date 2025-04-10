package Pages;

import java.io.IOException;

import org.openqa.selenium.By;

import TestBase.TestBase;
import Utils.ActionHelper;
import Utils.ExtentReportManager;

public class MemberPage {
    private final ActionHelper actions;
    private final TestBase testBase;
    private static final By WELCOME_BANNER = By.xpath("//*[@id=\"__next\"]/main/div/main/div/div/div");
    private static final By createNew = By.xpath("//*[@id=\"__next\"]/main/div/main/div/div/div/a");
    
    public MemberPage(TestBase testBase) throws IOException {
        this.testBase = testBase;
    	this.actions = new ActionHelper(testBase);
        verifyPageLoaded();
    }

    private void verifyPageLoaded() throws IOException {
        actions.waitForPageReadyState("MemberPage");
        ExtentReportManager.logInfo("Member dashboard loaded successfully");
    }

    public boolean isWelcomeBannerDisplayed() {
        try {
            boolean isVisible = actions.isElementDisplayed(WELCOME_BANNER, "Welcome Banner");
            if(isVisible) {
                ExtentReportManager.logPass("Welcome banner is displayed", null);
            }
            return isVisible;
        } catch (Exception e) {
            ExtentReportManager.logFail("Welcome banner check failed: " + e.getMessage(), null);
            return false;
        }
    }
    
    public OrgPage clickCreateNewOrg() throws IOException {
    	actions.click(createNew, "Create new Organization");
    	return new OrgPage(testBase);
    }

//    public ProfilePage navigateToProfile() {
//        actions.click(PROFILE_BUTTON, "Profile Button");
//        return new ProfilePage(testBase);
//    }
}