package Utils;

import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static final String CONFIG_PATH = "/config/config.properties";
    private static final ThreadLocal<Properties> properties = new ThreadLocal<>();
    private static final Object lock = new Object();

    // Private constructor to prevent instantiation
    private ConfigReader() {}

    /**
     * Thread-safe initialization of configuration properties
     */
    private static void initialize() {
        if (properties.get() == null) {
            synchronized (lock) {
                if (properties.get() == null) {
                    loadProperties();
                }
            }
        }
    }

    /**
     * Load properties from config file with error handling
     */
    private static void loadProperties() {
        Properties props = new Properties();
        try (InputStream input = ConfigReader.class.getResourceAsStream(CONFIG_PATH)) {
            if (input == null) {
                throw new IllegalStateException("Configuration file not found: " + CONFIG_PATH);
            }
            props.load(input);
            properties.set(props);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load configuration: " + e.getMessage(), e);
        }
    }

    /**
     * Get property value with validation
     * @param key Property key to retrieve
     * @return Non-empty property value
     */
    public static String getProperty(String key) {
        initialize();
        String value = properties.get().getProperty(key);
        
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing required property: " + key);
        }
        
        return value.trim();
    }


    /**
     * Get property with default value fallback
     * @param key Property key to retrieve
     * @param defaultValue Fallback value if not found
     * @return Property value or defaultValue
     */
    public static String getProperty(String key, String defaultValue) {
        initialize();
        String value = properties.get().getProperty(key, defaultValue);
        return value != null ? value.trim() : defaultValue;
    }
}