package com.pakago.electronic.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ResourceUtils {
    public static String getValue(String key) {
        return getResource().getProperty(key);
    }

    private static Properties getResource() {
        Properties prop = new Properties();
        try {
            String propFileName = "application.properties";

            InputStream inputStream = ResourceUtils.class.getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
        } catch (IOException ex) {
            return null;
        }

        return prop;
    }
}
