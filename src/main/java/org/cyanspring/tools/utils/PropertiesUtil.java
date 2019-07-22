package org.cyanspring.tools.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.ResourceBundle;

public class PropertiesUtil {

    private static final Logger log = LoggerFactory.getLogger(PropertiesUtil.class);
    private static final Properties prop = new Properties();


    public static String getAttribute(String configName, String key) {
        String osName = System.getProperty("os.name");
        try {
            if (osName.toLowerCase().startsWith("win")) {
                ResourceBundle resource = ResourceBundle.getBundle(configName);
                String value = resource.getString(key);
                log.info("path:" + value);
                return value;
            } else {
                String prevFix = System.getProperty("user.dir") + "/";
                String path = prevFix + configName + ".properties";
                FileInputStream fis = new FileInputStream(path);
                prop.load(fis);
                String value = prop.getProperty(key);
                log.info("path:" + value);
                return value;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        String nameProperties = "redis";
        String nameExport = "export.path";
        String nameImport = "import.path";
        String fullPath = PropertiesUtil.getAttribute(nameProperties, nameImport);
        System.out.println(fullPath);
    }
}
