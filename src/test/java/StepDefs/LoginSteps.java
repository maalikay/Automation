package StepDefs;

import Pages.HomePage;
import Pages.LoginPage;
import Pages.MemberPage;
import TestBase.TestBase;
import Utils.ConfigReader;
import Utils.ExtentReportManager;
import Utils.PasswordManager;
import Utils.ScreenshotUtils;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import static org.testng.Assert.*;

import java.io.IOException;

public class LoginSteps {
    //private final TestBase testBase;
    private final HomePage homePage;
    private final LoginPage loginPage;
    private final MemberPage memberPage;
    private final WebDriver driver;

    public LoginSteps(TestBase testBase) throws IOException {
        this.driver = testBase.getDriver();
        this.homePage = new HomePage(testBase);
        this.loginPage = new LoginPage(testBase);
        this.memberPage = new MemberPage(testBase);
    }

    @Given("I am on the landing page")
    public void navigateToLogin() {
    	String screenshotFile = ScreenshotUtils.capture(driver, "HomePage");
        ExtentReportManager.logPass("Navigated to Homepage KBTest", screenshotFile);
    }

    @Given("I click the login button")
    public void clickLoginBtn() throws IOException {
        homePage.clickLogin();
        String screenshotFile = ScreenshotUtils.capture(driver, "LoginPage");
        ExtentReportManager.logPass("Login verification successful", screenshotFile);
        //ExtentReportManager.logPass("Clicked login button", "screenshots");
    }
    
    @And("I click homepage icon")
    public void clickHome() throws IOException {
        homePage.clickHome();
        String screenshotFile = ScreenshotUtils.capture(driver, "HomePage");
        ExtentReportManager.logPass("Clicked Home Icon", screenshotFile);
    }

    @When("I provide valid login credential")
    public void loginToApp() throws IOException {
        String username = ConfigReader.getProperty("username");
        String encryptedPassword = ConfigReader.getProperty("password");
        String password = PasswordManager.decrypt(encryptedPassword);
        
        loginPage.enterEmail(username)
                 .enterPsd(password)
                 .clickLogin();
        
        String screenshotFile = ScreenshotUtils.capture(driver, "LoginPage");
        ExtentReportManager.logPass("Submitted Login Credentials", screenshotFile);
    }
    
    @When("I provide invalid login credential")
    public void loginToAppInv() throws IOException {
        String username = ConfigReader.getProperty("username1");
        String encryptedPassword = ConfigReader.getProperty("password1");
        String password = PasswordManager.decrypt(encryptedPassword);
        
        loginPage.enterEmail(username)
                 .enterPsd(password)
                 .clickLogin();
        
        String screenshotFile = ScreenshotUtils.capture(driver, "LoginPage");
        ExtentReportManager.logPass("Submitted Invalid Login Credentials", screenshotFile);
    }

    @Then("I should be logged into the application")
    public void verifySuccessfulLogin() throws IOException {
        try {
            assertTrue(memberPage.isWelcomeBannerDisplayed(), 
                     "Welcome banner displayed after login");
            String screenshotFile = ScreenshotUtils.capture(driver, "LoginPage");
            ExtentReportManager.logPass("Welcome Banner is Displayed", screenshotFile);
        } catch (AssertionError e) {
            String screenshotPath = ScreenshotUtils.capture(driver, "login_failure");
            ExtentReportManager.logFail("Login verification failed: " + e.getMessage(), screenshotPath);
            throw e;
        }
    }

    @Then("I should not be logged into the application")
    public void verifyUnSuccessfulLogin() throws IOException {
        try {
            assertTrue(loginPage.isErrDisplayed(), 
                     "Error displayed after login");
            ExtentReportManager.logPass("Login verification successful", "screenshots");
        } catch (AssertionError e) {
            String screenshotPath = ScreenshotUtils.capture(driver, "login_failure");
            ExtentReportManager.logFail("Login verification failed: " + e.getMessage(), screenshotPath);
            throw e;
        }
    }
    
    @Then("I should be back on homepage")
    public void backToHome() throws IOException {
        try {
            assertTrue(homePage.isBannerPresent(), 
                     "Welcome banner displayed");
            ExtentReportManager.logPass("Back to Homepage success.", "screenshots");
        } catch (AssertionError e) {
            String screenshotPath = ScreenshotUtils.capture(driver, "login_failure");
            ExtentReportManager.logFail("Login verification failed: " + e.getMessage(), screenshotPath);
            throw e;
        }
    }
    // Additional common steps
//    @Then("I should see an error message {string}")
//    public void verifyErrorMessage(String expectedMessage) {
//        String actualMessage = loginPage.getErrorMessage();
//        assertEquals(actualMessage, expectedMessage, 
//                   "Error message validation failed");
//        ExtentReportManager.logPass("Error message validation passed");
//    }
}