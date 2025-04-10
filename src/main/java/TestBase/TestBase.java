package TestBase;

import java.time.Duration;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import io.github.bonigarcia.wdm.WebDriverManager;
import Utils.ActionHelper;
import Utils.ConfigReader;
import Utils.ExtentReportManager;

public class TestBase {
    // ThreadLocal for parallel execution safety
    protected static ThreadLocal<WebDriver> driver = new ThreadLocal<>();
    //protected final ActionHelper actions;
    // Default configuration values
    private static final String DEFAULT_BROWSER = "chrome";
    private static final String DEFAULT_URL = "https://example.com";
    private static final int DEFAULT_TIMEOUT = 15;
    
 // Add browser name tracking
    private static String browserName;

    /**
     * Initializes the browser based on configuration
     */
    public void initializeBrowser() {
        try{
        	String browser = ConfigReader.getProperty("browser", DEFAULT_BROWSER).toLowerCase();
        boolean headless = Boolean.parseBoolean(
            ConfigReader.getProperty("headless", "false")
        );

        switch(browser) {
            case "chrome":
                initializeChrome(headless);
                break;
            case "firefox":
                initializeFirefox(headless);
                break;
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
        TestBase.browserName = browser;
        configureBrowserSettings();
        navigateToBaseUrl();
        } catch (Exception e) {
        	ExtentReportManager.logFail("Browser initialization failed: " + e.getMessage(), null);
            throw e;
        }
    }

    private void initializeChrome(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        if(headless) options.addArguments("--headless=new");
        options.addArguments("--disable-notifications");
        driver.set(new ChromeDriver(options));
    }

    private void initializeFirefox(boolean headless) {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        if(headless) options.addArguments("-headless");
        driver.set(new FirefoxDriver(options));
    }

    private void configureBrowserSettings() {
        getDriver().manage().window().maximize();
        getDriver().manage().timeouts()
            .implicitlyWait(getImplicitWait())
            .pageLoadTimeout(getPageLoadTimeout());
    }

    private Duration getImplicitWait() {
        return Duration.ofSeconds(
            Integer.parseInt(ConfigReader.getProperty("timeout.implicit", String.valueOf(DEFAULT_TIMEOUT)))
        );
    }

    private Duration getPageLoadTimeout() {
        return Duration.ofSeconds(
            Integer.parseInt(ConfigReader.getProperty("timeout.pageload", String.valueOf(DEFAULT_TIMEOUT * 2)))
        );
    }

    private void navigateToBaseUrl() {
        String baseUrl = ConfigReader.getProperty("url", DEFAULT_URL);
        getDriver().get(baseUrl);
    }

    /**
     * Provides access to the thread-safe WebDriver instance
     */
    //Commented this out. We will use all browser interactions through actionhelper.
    public WebDriver getDriver() {
    	System.err.println("WARNING: getDriver() is deprecated. Use ActionHelper instead!");
    	return driver.get();
    }

    /**
     * Cleanup method for test teardown
     */
    public void tearDown() {
        if (driver.get() != null) {
            driver.get().quit();
            driver.remove();
        }
    }

    /**
     * Utility for grid configuration (if needed)
     */
    public boolean isRemoteExecution() {
        return Boolean.parseBoolean(
            ConfigReader.getProperty("remote", "false")
        );
    }
    
    public String getBrowserName() {
        return (TestBase.browserName != null) 
            ? TestBase.browserName 
            : "NOT_INITIALIZED";
    }
}