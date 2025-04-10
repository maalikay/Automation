package Utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.ExtentSparkReporterConfig;
import com.aventstack.extentreports.reporter.configuration.Theme;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages thread-safe Extent Reports for test automation framework.
 * Handles report creation, test logging, and report cleanup.
 * Supports parallel test execution through ThreadLocal management.
 */
public class ExtentReportManager {
	// Individual report components
    private static final Map<Long, ExtentReports> individualReports = new HashMap<>();
    private static final ThreadLocal<ExtentTest> individualTest = new ThreadLocal<>();
    
    // Consolidated report components
    private static ExtentReports consolidatedReport;
    private static ExtentSparkReporter consolidatedSpark;
    private static final ThreadLocal<ExtentTest> consolidatedTest = new ThreadLocal<>();
    
    // Shared configuration
    private static final String TIMESTAMP = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    public static final String REPORT_BASE_PATH = "target/extent-reports/run_" + TIMESTAMP + "/";
    public static final String SCREENSHOT_DIR = "screenshots";

    static {
        initializeConsolidatedReport();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (consolidatedReport != null) {
                consolidatedReport.flush();
            }
        }));
    }

    /**
     * Initializes the consolidated report once per JVM
     */
    private static synchronized void initializeConsolidatedReport() {
        if (consolidatedReport == null) {
            String consolidatedPath = REPORT_BASE_PATH + "00_Consolidated_Report.html";
            consolidatedSpark = new ExtentSparkReporter(consolidatedPath);
            
            // Configure consolidated report
            ExtentSparkReporterConfig consolidatedConfig = consolidatedSpark.config();
            consolidatedConfig.setTheme(Theme.STANDARD);
            consolidatedConfig.setDocumentTitle("Consolidated Report - " + TIMESTAMP);
            consolidatedConfig.setReportName("Automation Results");
            
            consolidatedReport = new ExtentReports();
            consolidatedReport.attachReporter(consolidatedSpark);
            consolidatedReport.setSystemInfo("OS", System.getProperty("os.name"));
        }
    }

    /**
     * Creates individual report instance per thread
     */
    private static ExtentReports createIndividualReport() {
        Long threadId = Thread.currentThread().getId();
        String reportPath = REPORT_BASE_PATH + "individual_report_" + threadId + ".html";
        
        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        ExtentSparkReporterConfig config = spark.config();
        config.setTheme(Theme.STANDARD);
        config.setDocumentTitle("Thread " + threadId + " Report");
        config.setReportName("Test Execution - " + TIMESTAMP);
        
        ExtentReports report = new ExtentReports();
        report.attachReporter(spark);
        report.setSystemInfo("Thread ID", String.valueOf(threadId));
        return report;
    }

    /**
     * Starts test in both individual and consolidated reports
     */
    public static synchronized void startTest(String testName, String description, 
                                           String browser, String env) {
        // Individual report setup
        ExtentReports individualReport = individualReports.computeIfAbsent(
            Thread.currentThread().getId(), 
            k -> createIndividualReport()
        );
        
        ExtentTest testInstance = individualReport.createTest(testName, description)
            .assignDevice(browser)
            .assignCategory(env);
        individualTest.set(testInstance);

        // Consolidated report setup
        ExtentTest consolidatedTestInstance = consolidatedReport.createTest(testName, description)
            .assignDevice(browser)
            .assignCategory(env);
        consolidatedTest.set(consolidatedTestInstance);
    }
    
    private static void logToBothReports(java.util.function.Consumer<ExtentTest> logAction) {
        if (individualTest.get() != null) {
            logAction.accept(individualTest.get());
        }
        if (consolidatedTest.get() != null) {
            logAction.accept(consolidatedTest.get());
        }
    }

    /**
     * Logs info with screenshot to both reports
     */
    public static synchronized void logInfo(String message, String screenshotFile) {
        if (screenshotFile != null) {
            logToBothReports(test -> test.info(message + " " + buildScreenshotLink(screenshotFile)));
        } else {
            logToBothReports(test -> test.info(message));
        }
    }

    
    // Text-only logging methods
    public static synchronized void logTxtInfo(String message) {
        if (individualTest.get() != null) individualTest.get().info(message);
        if (consolidatedTest.get() != null) consolidatedTest.get().info(message);
    }

   
    /**
     * Logs a PASS status with message and screenshot in both reports
     * @param message Success message to log
     * @param screenshotFile Name of the screenshot file
     */
    public static synchronized void logPass(String message, String screenshotFile) {
        if (screenshotFile != null) {
            logToBothReports(test -> test.pass(message + " " + buildScreenshotLink(screenshotFile)));
        } else {
            logToBothReports(test -> test.pass(message));
        }
    }


    
    /**
     * Logs a FAIL status with message and screenshot in both reports
     * @param message Failure message to log
     * @param screenshotFile Name of the screenshot file
     */
    public static synchronized void logFail(String message, String screenshotFile) {
        if (screenshotFile != null) {
            logToBothReports(test -> test.fail(message + " " + buildScreenshotLink(screenshotFile)));
        } else {
            logToBothReports(test -> test.fail(message));
        }
    }


  
    /**
     * Logs a WARNING status with message and screenshot in both reports
     * @param message Warning message to log
     * @param screenshotFile Name of the screenshot file
     */
    public static synchronized void logWarning(String message, String screenshotFile) {
        if (screenshotFile != null) {
            logToBothReports(test -> test.warning(message + " " + buildScreenshotLink(screenshotFile)));
        } else {
            logToBothReports(test -> test.warning(message));
        }
    }


    /**
     * Logs an INFO message without screenshot in both reports
     * @param message Information message to log
     */
    public static synchronized void logInfo(String message) {
        if (individualTest.get() != null) {
            individualTest.get().info(message);
        }
        if (consolidatedTest.get() != null) {
            consolidatedTest.get().info(message);
        }
    }

    /**
     * Adds system/environment information to both reports
     * @param key Information key (e.g., "Browser")
     * @param value Information value (e.g., "Chrome 101")
     */
    public static synchronized void addSystemInfo(String key, String value) {
        Long threadId = Thread.currentThread().getId();
        ExtentReports individual = individualReports.get(threadId);
        if (individual != null) {
            individual.setSystemInfo(key, value);
        }
        if (consolidatedReport != null) {
            consolidatedReport.setSystemInfo(key, value);
        }
    }


    /**
     * Flushes the report and cleans up ThreadLocal resources
     * Removes current thread's report from the map
     */
    public static synchronized void flushReport() {
        Long threadId = Thread.currentThread().getId();
        
        // Flush individual report
        ExtentReports individual = individualReports.get(threadId);
        if (individual != null) {
            individual.flush();
            individualReports.remove(threadId);
        }
        
        // Clear thread-local references
        individualTest.remove();
        consolidatedTest.remove();
    }
    
    /**
     * Assigns device information safely
     */
    public static synchronized void assignDevice(String browserName) {
        if (individualTest.get() != null) {
            individualTest.get().assignDevice(browserName);
        }
        if (consolidatedTest.get() != null) {
            consolidatedTest.get().assignDevice(browserName);
        }
    }
    
    /**
     * returns image links
     * For integrating links in reports. We will use this in our logs.
     */
    private static String buildScreenshotLink(String fileName) {
        return "<a href='screenshots/" + fileName + "' target='_blank'>📸 View Screenshot</a>";
    }


   
}