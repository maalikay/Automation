package StepDefs;

import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import org.openqa.selenium.WebDriver;
import TestBase.TestBase;
import Utils.ExtentReportManager;
import Utils.ScreenshotUtils;

public class Hooks {
    private final TestBase testBase;
    private final ThreadLocal<Scenario> currentScenario = new ThreadLocal<>();

    public Hooks(TestBase testBase) {
        this.testBase = testBase;
    }

    @Before(order = 0)
    public void setupReporting(Scenario scenario) {
        currentScenario.set(scenario);
        String sanitizedName = scenario.getName().replaceAll("[^a-zA-Z0-9-]", "_");
        ExtentReportManager.startTest(
            sanitizedName,
            "Feature: " + getFeatureName(scenario),
            "BROWSER_NOT_INITIALIZED",
            System.getProperty("env", "DEV")
        );
    }

    @Before(order = 1)
    public void setupBrowser() {
        try {
            testBase.initializeBrowser();
            
            String browserName = testBase.getBrowserName();
            if (browserName == null || browserName.isEmpty()) {
                browserName = "UNKNOWN_BROWSER";
                ExtentReportManager.logWarning("Browser name not detected", browserName);
            }
            
            // Use direct device assignment in ExtentReportManager
            ExtentReportManager.addSystemInfo("Browser", browserName);
            
        } catch (Exception e) {
            String errorMsg = "Browser initialization failed: " + e.getMessage();
            ExtentReportManager.logFail(errorMsg, errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }

    @AfterStep
    public void captureStepEvidence() {
        try {
            WebDriver driver = testBase.getDriver();
            if (!isDriverActive(driver)) return;
            
            String ssName = "STEP_" + currentScenario.get().getName().replaceAll(" ", "_");
            String ssPath = ScreenshotUtils.capture(driver, ssName);
            
            if (ssPath != null) {
                ExtentReportManager.logInfo("Step executed", ssPath);
            }
        } catch (Exception e) {
            ExtentReportManager.logWarning("Step evidence failed: " + e.getMessage(), null);
        }
    }

    @After
    public void finalizeScenario() {
        try {
            Scenario scenario = currentScenario.get();
            WebDriver driver = testBase.getDriver();
            
            if (isDriverActive(driver)) {
                String ssName = "FINAL_" + scenario.getName().replaceAll(" ", "_");
                String ssPath = ScreenshotUtils.capture(driver, ssName);
                if (ssPath != null) {
                    ExtentReportManager.logInfo("Final state", ssPath);
                }
            }
            
            if (scenario.isFailed()) {
                ExtentReportManager.logFail("Scenario Failed", null);
            }
        } finally {
            testBase.tearDown();
            //ExtentReportManager.flushReport(); //removing this to avoid double flushing when using listener
            currentScenario.remove();
        }
    }

    private boolean isDriverActive(WebDriver driver) {
        try {
            return driver != null && driver.getTitle() != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String getFeatureName(Scenario scenario) {
        return scenario.getId().split(";")[0].replaceAll("[^a-zA-Z0-9-]", "_");
    }
}