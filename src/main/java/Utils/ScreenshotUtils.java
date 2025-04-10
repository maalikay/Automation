package Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class ScreenshotUtils {
    public static String capture(WebDriver driver, String baseName) {
        try {
            // 1. Create screenshots directory inside report folder
            File screenshotDir = new File(ExtentReportManager.REPORT_BASE_PATH, "screenshots");
            if (!screenshotDir.exists()) screenshotDir.mkdirs();

            // 2. Generate filename WITHOUT directory
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new Date());
            String safeName = baseName.replaceAll("[^a-zA-Z0-9-]", "_");
            String fileName = safeName + "_" + timestamp + ".png";
            
            // 3. Save screenshot directly to screenshots directory
            File srcFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destFile = new File(screenshotDir, fileName);
            FileUtils.copyFile(srcFile, destFile);
            
            // 4. Return ONLY filename (no path)
            return fileName;
            
        } catch (Exception e) {
            ExtentReportManager.logWarning("Screenshot failed: " + e.getMessage(), baseName);
            return null;
        }
    }
}