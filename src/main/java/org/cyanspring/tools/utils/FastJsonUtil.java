package org.cyanspring.tools.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.List;
import java.util.Map;

public class FastJsonUtil {
    private static final SerializeConfig config;
    private static final PropertyFilter profilter;
    private static final String DATE_FORMAT_FULL = "yyyy-MM-dd HH:mm:ss";

    static {
        config = new SerializeConfig();
        JSON.DEFFAULT_DATE_FORMAT = FastJsonUtil.DATE_FORMAT_FULL;
        profilter = new PropertyFilter() {//||name.equalsIgnoreCase("key")
            @Override
            public boolean apply(Object object, String name, Object value) {
                if (name.equalsIgnoreCase("params") || name.equalsIgnoreCase("created")
                        || name.equalsIgnoreCase("tableName") || name.equalsIgnoreCase("byField")
                        || name.equalsIgnoreCase("optName")
                        || name.equalsIgnoreCase("insert")) {
                    return false;
                }
                return true;
            }
        };
    }

    private static final SerializerFeature[] features = {
            SerializerFeature.WriteDateUseDateFormat
    };

    public static String beanToJson(Object object) {
        return JSON.toJSONString(object, config, profilter, features);
    }

    public static String toJSONNoFeatures(Object object) {
        return JSON.toJSONString(object, config);
    }

    public static <T> T jsonToBean(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    // 转换为数组
    public static <T> Object[] jsonToArray(String text) {
        return jsonToArray(text, null);
    }

    // 转换为数组
    public static <T> Object[] jsonToArray(String text, Class<T> clazz) {
        return JSON.parseArray(text, clazz).toArray();
    }

    // 转换为List
    public static <T> List<T> jsonToList(String text, Class<T> clazz) {
        return JSON.parseArray(text, clazz);
    }

    /**
     * 将string转化为序列化的json字符串
     *
     * @param text
     * @return
     */
    public static Object jsonToBean(String text) {
        Object objectJson = JSON.parse(text);
        return objectJson;
    }

    /**
     * json字符串转化为map
     *
     * @param s
     * @return
     */
    public static Map beanListToJson(String s) {
        Map m = JSONObject.parseObject(s);
        return m;
    }

    /**
     * 将map转化为string
     *
     * @param m
     * @return
     */
    public static String beanListToJson(Map m) {
        String s = JSONObject.toJSONString(m);
        return s;
    }
}
