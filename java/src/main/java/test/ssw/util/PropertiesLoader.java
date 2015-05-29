package test.ssw.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertiesLoader {

    Logger             log = LoggerFactory.getLogger(PropertiesLoader.class);
    private String     propFilename;
    private Properties properties;

    public PropertiesLoader(String propFilename) {
        this.propFilename = propFilename;
    }

    public String get(String key) {
        return get(key, null);
    }

    public String get(String key, String defaultValue) {
        String prop = properties.getProperty(key);
        if (prop != null) return prop;
        if (defaultValue != null) return defaultValue;
        return null;
    }

    public Integer getInt(String key) {
        return getInt(key, null);
    }

    public Integer getInt(String key, Integer defaultValue) {
        String prop = properties.getProperty(key);
        if (prop != null) return new Integer(prop);
        if (defaultValue != null) return defaultValue;
        return null;
    }

    /**
     * Load properties from the given filename
     * 
     * @throws IOException
     */
    public void loadProperties() throws IOException {

        log.debug("Loading properties file '" + propFilename + "'...");
        properties = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFilename);

        if (inputStream != null) properties.load(inputStream);
        else throw new FileNotFoundException("Property file '" + propFilename + "' not found in the classpath");
    }
}
