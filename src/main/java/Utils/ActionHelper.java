package Utils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import TestBase.TestBase;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

/**
 * Centralized Selenium interaction methods with built-in reporting and error handling
 * Features explicit waits, retry mechanisms, and Extent Reports integration
 */
public class ActionHelper {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private static final Duration DEFAULT_WAIT = Duration.ofSeconds(15);
    private static final int MAX_STALE_RETRIES = 2;

    /**
     * Initializes ActionHelper with WebDriver from TestBase
     * @param testBase Active TestBase instance providing WebDriver
     */
    public ActionHelper(TestBase testBase) {
        this.driver = testBase.getDriver();
        this.wait = new WebDriverWait(driver, DEFAULT_WAIT);
    }

    // Core Element Interactions ///////////////////////////////////////////////
    
    /**
     * Clicks an element with retry logic for stale elements
     * @param locator Element locator
     * @param elementName Descriptive name for reporting
     * @throws IOException 
     */
    public void click(By locator, String elementName) throws IOException {
        for (int attempt = 0; attempt < MAX_STALE_RETRIES; attempt++) {
            try {
                WebElement element = waitForClickable(locator, elementName);
                element.click();
                ExtentReportManager.logPass("Clicked: " + elementName, elementName);
                return;
            } catch (StaleElementReferenceException e) {
                handleStaleElement(attempt, elementName, e);
            } catch (Exception e) {
                handleInteractionFailure("click", elementName, e);
                throw e;
            }
        }
    }

    /**
     * Enters text into a field with input validation
     * @param locator Field locator
     * @param text Text to input
     * @param elementName Descriptive name for reporting
     * @throws IOException 
     */
    public void sendKeys(By locator, String text, String elementName) throws IOException {
        try {
            WebElement element = waitForVisible(locator, elementName);
            element.clear();
            element.sendKeys(text);
            String maskedText = maskSensitiveData(text, elementName);
            ExtentReportManager.logInfo("Entered text in '" + elementName + "': " + maskedText);
        } catch (Exception e) {
            handleInteractionFailure("text entry", elementName, e);
            throw e;
        }
    }

    // Dropdown Handlers ///////////////////////////////////////////////////////
    
    /**
     * Selects dropdown option by visible text
     * @param locator Dropdown locator
     * @param text Visible text to select
     * @param elementName Descriptive name for reporting
     * @throws IOException 
     */
    public void selectByText(By locator, String text, String elementName) throws IOException {
        try {
            WebElement element = waitForPresent(locator, elementName);
            new Select(element).selectByVisibleText(text);
            ExtentReportManager.logPass("Selected '" + text + "' in " + elementName, elementName);
        } catch (Exception e) {
            handleInteractionFailure("dropdown selection", elementName, e);
            throw e;
        }
    }

    /**
     * Selects dropdown option by value attribute
     * @param locator Dropdown locator
     * @param value Value attribute to select
     * @param elementName Descriptive name for reporting
     * @throws IOException 
     */
    public void selectByValue(By locator, String value, String elementName) throws IOException {
        try {
            WebElement element = waitForPresent(locator, elementName);
            new Select(element).selectByValue(value);
            ExtentReportManager.logPass("Selected value '" + value + "' in " + elementName, elementName);
        } catch (Exception e) {
            handleInteractionFailure("dropdown selection", elementName, e);
            throw e;
        }
    }
    
    /**
     * Clears existing text and types new text with validation
     * @param locator Element locator
     * @param text Text to input
     * @param elementName Descriptive name for reporting
     * @throws IOException 
     */
    public void clearAndType(By locator, String text, String elementName) throws IOException {
        try {
            WebElement element = waitForVisible(locator, elementName);
            
            // Clear existing text
            element.clear();
            ExtentReportManager.logInfo("Cleared field: " + elementName);
            
            // Verify field is empty
            if(!element.getAttribute("value").isEmpty()) {
                clearUsingJavaScript(locator, elementName);
            }
            
            // Type new text
            element.sendKeys(text);
            ExtentReportManager.logInfo("Typed text in " + elementName + ": " + maskSensitiveData(text, elementName));
            
            // Verify text entry
            if(!element.getAttribute("value").equals(text)) {
                throw new RuntimeException("Text entry verification failed");
            }
            
        } catch (Exception e) {
            handleInteractionFailure("clear and type", elementName, e);
        }
    }
    
    public void navigateTo(String url) {
        driver.get(url);
        ExtentReportManager.logInfo("Navigated to: " + url);
    }


    // Wait Handlers ///////////////////////////////////////////////////////////
    
    /**
     * Waits for element to be visible and clickable
     * @param locator Element locator
     * @param elementName Descriptive name for reporting
     * @return Interactive WebElement
     * @throws IOException 
     */
    public WebElement waitForClickable(By locator, String elementName) throws IOException {
        try {
            ExtentReportManager.logInfo("Waiting for clickability: " + elementName);
            return wait.until(ExpectedConditions.elementToBeClickable(locator));
        } catch (Exception e) {
            handleInteractionFailure("wait for clickable", elementName, e);
            throw e;
        }
    }

    /**
     * Waits for element visibility
     * @param locator Element locator
     * @param elementName Descriptive name for reporting
     * @return Visible WebElement
     * @throws IOException 
     */
    public WebElement waitForVisible(By locator, String elementName) throws IOException {
        try {
            ExtentReportManager.logInfo("Waiting for visibility: " + elementName);
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        } catch (Exception e) {
            handleInteractionFailure("wait for visible", elementName, e);
            throw e;
        }
    }
    
// Wait Handlers ///////////////////////////////////////////////////////////
    
    /**
     * Waits for element presence in DOM (may not be visible)
     * @param locator Element locator
     * @param elementName Descriptive name for reporting
     * @return WebElement once present
     * @throws IOException 
     */
    public WebElement waitForPresent(By locator, String elementName) throws IOException {
        try {
            ExtentReportManager.logInfo("Waiting for presence: " + elementName);
            return wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception e) {
            handleInteractionFailure("wait for presence", elementName, e);
            throw e;
        }
    }


    // JavaScript Interactions /////////////////////////////////////////////////
    
    /**
     * JavaScript click for problematic elements
     * @param locator Element locator
     * @param elementName Descriptive name for reporting
     * @throws IOException 
     */
    public void jsClick(By locator, String elementName) throws IOException {
        try {
            WebElement element = waitForPresent(locator, elementName);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            ExtentReportManager.logPass("JS clicked: " + elementName, elementName);
        } catch (Exception e) {
            handleInteractionFailure("JavaScript click", elementName, e);
            throw e;
        }
    }

    /**
     * Scrolls to element using JavaScript
     * @param locator Element locator
     * @param elementName Descriptive name for reporting
     * @throws IOException 
     */
    public void scrollToElement(By locator, String elementName) throws IOException {
        try {
            WebElement element = waitForPresent(locator, elementName);
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", 
                element
            );
            ExtentReportManager.logInfo("Scrolled to: " + elementName);
        } catch (Exception e) {
            handleInteractionFailure("scroll to element", elementName, e);
        }
    }
    
    /**
     * Force clear using JavaScript for stubborn fields
     * @param locator Element locator
     * @param elementName Descriptive name for reporting
     * @throws IOException 
     */
    public void clearUsingJavaScript(By locator, String elementName) throws IOException {
        try {
            WebElement element = waitForPresent(locator, elementName);
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = '';", element
            );
            ExtentReportManager.logInfo("JavaScript cleared field: " + elementName);
        } catch (Exception e) {
            handleInteractionFailure("JavaScript clear", elementName, e);
        }
    }

    // Utility Methods /////////////////////////////////////////////////////////
    
    /**
     * Handles failed interactions with screenshot capture
     * @param actionType Type of failed action for logging
     * @param elementName Descriptive element name
     * @param e Exception thrown
     * @throws IOException 
     */
    private void handleInteractionFailure(String actionType, String elementName, Exception e) throws IOException {
        String ssPath = ScreenshotUtils.capture(driver, "failure_" + elementName);
        ExtentReportManager.logFail("Failure screenshot", ssPath);
    	String errorMessage = String.format("Failed to %s: %s - %s", 
            actionType, elementName, e.getMessage());
        
        ExtentReportManager.logFail(errorMessage, errorMessage);
        //CaptureScreenshot.take(driver, "failure_" + elementName);
        throw new WebDriverException(errorMessage, e);
    }

    /**
     * Masks sensitive data in logs
     * @param text Original text
     * @param elementName Field name for context
     * @return Masked or original text
     */
    private String maskSensitiveData(String text, String elementName) {
        if (elementName.toLowerCase().contains("password") || text == null) {
            return "*******";
        }
        return text;
    }

    /**
     * Handles stale element retry logic
     * @param attempt Current retry attempt
     * @param elementName Descriptive element name
     * @param e Original exception
     */
    private void handleStaleElement(int attempt, String elementName, Exception e) {
        if (attempt == MAX_STALE_RETRIES - 1) {
            String error = "Stale element after " + MAX_STALE_RETRIES + " attempts: " + elementName;
            ExtentReportManager.logFail(error, error);
            throw new StaleElementReferenceException(error, e);
        }
        ExtentReportManager.logWarning("Stale element detected, retrying: " + elementName, elementName);
    }

    // Screenshot Utility //////////////////////////////////////////////////////
    
    /**
     * Nested class for screenshot handling
     */
    public String captureScreenshot(String name) {
        return ScreenshotUtils.capture(driver, name);
    }
    
    /**
     * Checks if element exists in DOM (may not be visible)
     * @param locator Element locator
     * @param elementName Descriptive name for reporting
     * @return true if element is present
     */
    public boolean isElementPresent(By locator, String elementName) {
        try {
            new WebDriverWait(driver, Duration.ofMillis(500))
                .until(ExpectedConditions.presenceOfElementLocated(locator));
            ExtentReportManager.logInfo(elementName + " is present in DOM");
            return true;
        } catch (Exception e) {
            ExtentReportManager.logInfo(elementName + " not found in DOM");
            return false;
        }
    }

    /**
     * Checks if element is visible and displayed
     * @param locator Element locator
     * @param elementName Descriptive name for reporting
     * @return true if element is visible
     */
    public boolean isElementDisplayed(By locator, String elementName) {
        try {
            boolean displayed = driver.findElement(locator).isDisplayed();
            String message = elementName + (displayed ? " is visible" : " exists but hidden");
            ExtentReportManager.logInfo(message);
            return displayed;
        } catch (NoSuchElementException e) {
            ExtentReportManager.logWarning(elementName + " not found when checking visibility", elementName);
            return false;
        }
    }

    /**
     * Checks if element is interactable (present and enabled)
     * @param locator Element locator
     * @param elementName Descriptive name for reporting
     * @return true if element is enabled
     */
    public boolean isElementEnabled(By locator, String elementName) {
        if (!isElementPresent(locator, elementName)) return false;
        
        try {
            boolean enabled = driver.findElement(locator).isEnabled();
            String message = elementName + (enabled ? " is enabled" : " is disabled");
            ExtentReportManager.logInfo(message);
            return enabled;
        } catch (StaleElementReferenceException e) {
            ExtentReportManager.logWarning(elementName + " became stale during enabled check", elementName);
            return isElementEnabled(locator, elementName); // Recursive retry
        }
    }

    /**
     * Waits for element to meet specified state
     * @param locator Element locator
     * @param elementName Descriptive name for reporting
     * @param condition Expected condition (VISIBLE, CLICKABLE, etc)
     * @param timeoutSeconds Maximum wait time
     * @return WebElement meeting the condition
     */
    public WebElement waitForElementCondition(By locator, String elementName, 
                                            ElementCondition condition, 
                                            int timeoutSeconds) {
        WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
        String conditionName = condition.toString().toLowerCase();
        
        try {
            ExtentReportManager.logInfo("Waiting for element to be " + conditionName + ": " + elementName);
            
            switch(condition) {
                case VISIBLE:
                    return customWait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                case CLICKABLE:
                    return customWait.until(ExpectedConditions.elementToBeClickable(locator));
                case PRESENT:
                    return customWait.until(ExpectedConditions.presenceOfElementLocated(locator));
                default:
                    throw new IllegalArgumentException("Unsupported condition: " + condition);
            }
        } catch (TimeoutException e) {
            String error = elementName + " not " + conditionName + " within " + timeoutSeconds + "s";
            ExtentReportManager.logFail(error, error);
            throw e;
        }
    }

    /**
     * Gets element text with validation
     * @param locator Element locator
     * @param elementName Descriptive name for reporting
     * @return Element text or empty string
     */
    public String getElementText(By locator, String elementName) {
        try {
            String text = waitForVisible(locator, elementName).getText().trim();
            ExtentReportManager.logInfo(elementName + " text: " + text);
            return text;
        } catch (Exception e) {
            ExtentReportManager.logWarning("Failed to get text from " + elementName, elementName);
            return "";
        }
    }

    /**
     * Gets element attribute value
     * @param locator Element locator
     * @param attribute Attribute name
     * @param elementName Descriptive name for reporting
     * @return Attribute value or empty string
     */
    public String getElementAttribute(By locator, String attribute, String elementName) {
        try {
            String value = waitForPresent(locator, elementName).getAttribute(attribute);
            ExtentReportManager.logInfo(elementName + " attribute '" + attribute + "': " + value);
            return value != null ? value : "";
        } catch (Exception e) {
            ExtentReportManager.logWarning("Failed to get attribute from " + elementName, elementName);
            return "";
        }
    }

    public enum ElementCondition {
        VISIBLE, CLICKABLE, PRESENT
    }
    
    /**
     * Waits for the page to reach complete ready state
     * @param pageDescription Descriptive name of the page for reporting
     * @throws TimeoutException if page doesn't load within configured timeout
     */
    public void waitForPageReadyState(String pageDescription) {
        int timeout = Integer.parseInt(ConfigReader.getProperty("page.load.timeout", "30"));
        WebDriverWait pageWait = new WebDriverWait(driver, Duration.ofSeconds(timeout));
        
        try {
            pageWait.until(d -> {
            	String readyState = (String) executeJavascript("return document.readyState");
                return "complete".equals(readyState);
            });
            ExtentReportManager.logPass(pageDescription + " loaded successfully", pageDescription);
        } catch (TimeoutException e) {
            String errorMsg = pageDescription + " failed to load within " + timeout + " seconds";
            ExtentReportManager.logFail(errorMsg, errorMsg);
            throw new PageNotLoadedException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error checking page state: " + e.getMessage();
            ExtentReportManager.logFail(errorMsg, errorMsg);
            throw new WebDriverException(errorMsg, e);
        }
    }
    
    public static class PageNotLoadedException extends RuntimeException {
        public PageNotLoadedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Executes JavaScript safely
     * @param script JavaScript to execute
     * @return Execution result
     */
    private Object executeJavascript(String script) {
        try {
            return ((JavascriptExecutor) driver).executeScript(script);
        } catch (JavascriptException e) {
            ExtentReportManager.logWarning("JavaScript execution failed: " + e.getMessage(), script);
            throw e;
        }
    }

}