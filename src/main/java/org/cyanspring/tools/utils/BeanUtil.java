package org.cyanspring.tools.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BeanUtil {
    private static Log log = LogFactory.getLog(BeanUtil.class);

    public static String getObjectName(Object po) {
        String returnString = toFirstCharLow(po.getClass().getSimpleName());
        return returnString;
    }

    private static String toFirstCharLow(String str) {
        String tmp1 = str.substring(0, 1);
        String tmp2 = str.substring(1, str.length());
        return tmp1.toLowerCase() + tmp2;
    }

    private static String toFirstCharUpper(String str) {
        String tmp1 = str.substring(0, 1);
        String tmp2 = str.substring(1, str.length());
        return tmp1.toUpperCase() + tmp2;
    }

    public static <M> M getInstance(String packName, String clzName) {
        return getInstance(packName + "." + toFirstCharUpper(clzName));//组装包和类名
    }

    public static <M> M getInstance(String clzName) {
        M temp = null;
        try {
            temp = (M) Class.forName(clzName).newInstance();
        } catch (Exception err) {
            if (log.isErrorEnabled()) {
                log.error(err);
            }
        }
        return temp;
    }

    public static <M> M getInstance(Class clazz) {
        return getInstance(clazz.getName());
    }
}