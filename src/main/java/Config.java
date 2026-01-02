import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static Properties properties;
    private static final String DEFAULT_FILE = "config.properties";

    private static void load(String filename) {
        properties = new Properties();
        try (FileInputStream fis = new FileInputStream(filename)) {
            properties.load(fis);
        } catch (IOException e) {
            throw new RuntimeException("Impossibile leggere " + filename, e);
        }
    }

    // GET
    public static String get(String key) {
        if (properties == null) {
            load(DEFAULT_FILE);
        }

        return properties.getProperty(key);
    }
}