package Pages;

import java.io.IOException;
import org.openqa.selenium.By;
import TestBase.TestBase;
import Utils.ActionHelper;

public class LoginPage {
    private final ActionHelper actions;
    private final TestBase testBase;
    
    private static final By emailField = By.name("email");
    private static final By passwordField = By.name("password");
    private static final By loginBtn = By.xpath("//*[@id=\"__next\"]/main/div/main/div/div/div/form/fieldset/div/button");
    private static final By errorMsg = By.xpath("//*[@id=\"__next\"]/main/div/main/div/div/div/form/fieldset/div/div[3]/p");
//    @FindBy(css = "#mantine-9r36oamyq") // More reliable CSS selector
//    private WebElement emailField;
//    
//    @FindBy(css = "input[type='email']") // More reliable CSS selector
//    private WebElement passwordField;
//    
//    @FindBy(css = "input[type='email']") // More reliable CSS selector
//    private WebElement loginBtn;

    public LoginPage(TestBase testBase) throws IOException {
        this.testBase = testBase;
        this.actions = new ActionHelper(testBase);
        //PageFactory.initElements(testBase.getDriver(), this);
        waitForPageReady();
    }

    private void waitForPageReady() throws IOException {
        // Wait for both page state and element interactability
        actions.waitForPageReadyState("LoginPage");
        //actions.waitForElementInteractable(emailField, "Email Field", Duration.ofSeconds(15));
    }

    public LoginPage enterEmail(String email) throws IOException {
        actions.clearAndType(emailField, email, "Email Field");
        return this;
    }
    
    public LoginPage enterPsd(String password) throws IOException {
        actions.clearAndType(passwordField, password, "Password Field");
        return this;
    }
    
    public MemberPage clickLogin() throws IOException {
        actions.click(loginBtn, null);
		return new MemberPage(testBase);
    }
    
    public boolean isErrDisplayed() {
		actions.isElementDisplayed(errorMsg, "Error Message");
    	return true;
    	
    }
    
    
    
}