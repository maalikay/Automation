package StepDefs.Common;

import TestBase.TestBase;
import Utils.ActionHelper;
import Utils.ConfigReader;
import io.cucumber.java.en.Given;

public abstract class CommonSteps {
    
    protected final TestBase testBase;
    protected final ActionHelper actions;
    
    public CommonSteps(TestBase testBase) {
        this.testBase = testBase;
        //this.actions = actions;
    }

    // =============== Common Steps ===============
    
    @Given("I am on the {string} page")
    public void navigateToPage(String pageName) {
        String url = getPageUrl(pageName);
        actions.navigateTo(url);
        actions.waitForPageReadyState(pageName);
    }

    private String getPageUrl(String pageName) {
        switch(pageName.toLowerCase()) {
            case "homepage":
                return ConfigReader.getProperty("url");
            case "dashboard":
                return ConfigReader.getProperty("dashboard.url");
            default:
                throw new IllegalArgumentException("Unknown page: " + pageName);
        }
    }
}