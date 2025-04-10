package Listeners;

import org.openqa.selenium.WebDriver;
import org.testng.ITestListener;
import org.testng.ITestResult;
import Utils.ExtentReportManager;
import Utils.ScreenshotUtils;
import TestBase.TestBase;

public class ExtentTestListener implements ITestListener {
    //private final WebDriver driver = null;
    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        String description = "Scenario: " + testName;
        String browser = getBrowserFromTestInstance(result);
        String env = System.getProperty("env", "DEV");
        
        ExtentReportManager.startTest(testName, description, browser, env);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
    	String screenshotFile = ScreenshotUtils.capture(testBase, "LoginPage");
        ExtentReportManager.logPass("Test Passed Successfully", screenshotFile);
    	//ExtentReportManager.logPass("Test Passed Successfully", "screenshots");
        ExtentReportManager.flushReport();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        captureFinalScreenshot(result);
        ExtentReportManager.logFail("Test Failed", "screenshots");
        ExtentReportManager.flushReport();
    }

    private String getBrowserFromTestInstance(ITestResult result) {
        try {
            TestBase testBase = (TestBase) result.getInstance();
            return testBase.getBrowserName();
        } catch (Exception e) {
            return "Unknown Browser";
        }
    }

    private void captureFinalScreenshot(ITestResult result) {
        try {
            TestBase testBase = (TestBase) result.getInstance();
            WebDriver driver = testBase.getDriver();
            if (driver != null) {
                String ssPath = ScreenshotUtils.capture(driver, "final_failure");
                ExtentReportManager.logFail("Failure Screenshot", ssPath);
            }
        } catch (Exception e) {
            ExtentReportManager.logWarning("Failed to capture final screenshot: " + e.getMessage(), null);
        }
    }
}