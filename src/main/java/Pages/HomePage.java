package Pages;

import java.io.IOException;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import TestBase.TestBase;
import Utils.ActionHelper;

public class HomePage {
    private final ActionHelper actions;
    private final TestBase testBase;
    
    private static final By loginButton = By.xpath("//*[@id=\"__next\"]/main/div/header/div[2]/div/div/a[1]/button");
    private static final By header = By.cssSelector("#__next > main > div > header > div:nth-child(2) > a > div > img");
    private static final By title = By.cssSelector("#__next > main > div > header > div:nth-child(2) > a > div > img");
    private static final By logo = By.xpath("//*[@id=\"__next\"]/main/div/header/div[1]/a/div/div");
    private static final By banner = By.xpath("//*[@id=\"__next\"]/main/div/main/div/div/div/div[1]");
//    @FindBy(css = "#__next > main > div > header > div.m_4081bf90.mantine-Group-root.mantine-visible-from-md > div > div > a:nth-child(1) > button") // Improved CSS selector
//    private WebElement loginButton;
//    
//    @FindBy(css = "#__next > main > div > header > div:nth-child(2) > a > div > img") // Improved CSS selector
//    private WebElement header;
//    
//    @FindBy(xpath = "//*[@id=\"__next\"]/main/div/main/div/div/div/div[1]/div/a/button/span/span")
//    private WebElement gotoExample;

    public HomePage(TestBase testBase) throws IOException {
        this.testBase = testBase;
        this.actions = new ActionHelper(testBase);
        //PageFactory.initElements(testBase.getDriver(), this);
        verifyPageLoaded();
    }

    private void verifyPageLoaded() throws IOException {
        actions.waitForVisible(header, "Header section");
    }

    public LoginPage clickLogin() throws IOException {
        actions.scrollToElement(loginButton, "Login Button");
        actions.click(loginButton, "Login Button");
        return new LoginPage(testBase);
    }
    
    public HomePage clickHome() throws IOException {
    	actions.click(logo, "Header Logo");
		return new HomePage(testBase);
    }
    
    public boolean isBannerPresent() {
    	actions.isElementDisplayed(banner, "Welcome Banner");
    	return true;
    }
    
}