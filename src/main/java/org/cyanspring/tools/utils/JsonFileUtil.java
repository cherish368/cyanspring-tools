package org.cyanspring.tools.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class JsonFileUtil {
    private static final Logger log = LoggerFactory.getLogger(JsonFileUtil.class);

    /**
     * 生成.json格式文件
     */
    public static boolean createJsonFile(String jsonString, String fullPath) {
        boolean flag = true;
        try {
            File file = new File(fullPath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            if (jsonString.indexOf("'") != -1) {
                jsonString = jsonString.replaceAll("'", "\\'");
            }
            if (jsonString.indexOf("\"") != -1) {
                jsonString = jsonString.replaceAll("\"", "\\\"");
            }
            if (jsonString.indexOf("\r\n") != -1) {
                jsonString = jsonString.replaceAll("\r\n", "\\u000d\\u000a");
            }
            if (jsonString.indexOf("\n") != -1) {
                jsonString = jsonString.replaceAll("\n", "\\u000a");
            }
            // 格式化json字符串
            jsonString = JsonFormatUtil.formatJson(jsonString);
            Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            write.write(jsonString);
            write.flush();
            write.close();
        } catch (Exception e) {
            flag = false;
            log.error(e.getMessage());
        }
        return flag;
    }

    /**
     * 读取json文件，返回json串
     *
     * @param fullPath
     * @return
     */
    public static <T> Map<String, List<T>> readJsonFile(String fullPath, Class<T> clazz) {
        StringBuffer stringbuffer = new StringBuffer();
        try {
            FileInputStream fis = new FileInputStream(fullPath);
            InputStreamReader inputStreamReader = new InputStreamReader(fis, "UTF-8");
            BufferedReader in = new BufferedReader(inputStreamReader);
            String str;
            while ((str = in.readLine()) != null) {
                stringbuffer.append(str);
            }
            in.close();
        } catch (IOException e) {
            e.getStackTrace();
        }
        String jsonData = stringbuffer.toString();
        JSONArray objects = JSON.parseArray(jsonData);
        net.sf.json.JSONObject jsonObject = net.sf.json.JSONObject.fromObject(objects.get(0));
        Iterator<String> keyIter = jsonObject.keys();
        Map<String, List<T>> valueMap = new LinkedHashMap<>();
        while (keyIter.hasNext()) {
            String key = keyIter.next();
            List<T> value = JSONObject.parseArray(jsonObject.get(key).toString(), clazz);
            valueMap.put(key, value);
        }
        return valueMap;

    }
}
