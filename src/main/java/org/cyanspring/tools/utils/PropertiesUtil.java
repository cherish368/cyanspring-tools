package org.cyanspring.tools.utils;

import org.cyanspring.tools.common.Attribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Properties;
import java.util.ResourceBundle;

public class PropertiesUtil {

    private static final Logger log = LoggerFactory.getLogger(PropertiesUtil.class);
    private static final Properties prop = new Properties();

    /**
     * 根据配置文件对实体固定属性赋值
     * 获取配置文件实体
     *
     * @param configName
     * @param t
     * @param <T>
     * @return
     */
    public static <T> T getProperties(String path, String configName, T t) {
        ResourceBundle resource = ResourceBundle.getBundle(path + configName);
        try {
            Class clazz = t.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                boolean flag = field.isAnnotationPresent(Attribute.class);
                if (flag) {
                    String name = field.getName();
                    String string = resource.getString(configName + "." + name);
                    Object obj = field.get(t);
                    if (!StringUtils.isEmpty(string) && obj == null) {
                        log.info("PropertiesUtil field {}", string);
                        field.set(t, string);
                    }
                }
            }
        } catch (IllegalAccessException e) {
            log.error(e.getMessage());
        }
        return t;
    }


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
