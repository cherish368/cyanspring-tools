package org.cyanspring.tools.common;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cyanspring.tools.utils.StringUtil;
import org.cyanspring.tools.utils.TimeUtil;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class BaseEntity implements Serializable {
    private static Log log = LogFactory.getLog(BaseEntity.class);
    protected HashMap<String, Object> fieldsExpand;
    protected HashMap<String, Object> fields;

    public BaseEntity() {
    }


    public void createByJson(String json) {
        Method[] props = this.getClass().getMethods();
        createByJson(json, props);
    }

    public void createByJson(String json, Method[] props) {
        if (!StringUtil.isEmpty(json)) {
            String[] array = json.split(",");
            for (int i = 0; i < array.length; i++) {
                String[] kvs = array[i].split(":");
                if (kvs.length == 2) {
                    sotValue(kvs[0], kvs[1], props);
                }
            }
        }
    }

    public Object gotExtendValue(String name) {
        return fieldsExpand.get(name);
    }

    public void sotValue(String name, Object data) {
        Method[] props = this.getClass().getMethods();
        sotValue(name, data, props);
    }

    public void sotValue(String name, Object data, Method[] props) {
        try {
            if (data != null) {
                Method field = gotField(name, props);
                if (field != null) {
                    Class[] parameterTypes = field.getParameterTypes();
                    if (parameterTypes.length > 0) {
                        Object tmp = gotValue(parameterTypes[0], data);
                        if (tmp != null)
                            field.invoke(this, tmp);
                    }
                } else {// 添加自定义属性
                    fieldsExpand.put(name, data);
                }
            }
        } catch (Exception err) {
            log.error(err);
        }
    }

    protected Object gotValue(Class clazz, Object data) {
        if (String.class == clazz) {
            return data;
        } else {
            String tmp = StringUtil.toStr(data);
            if (tmp.equals(""))
                return null;
            else if (Integer.class == clazz)
                return StringUtil.toInt(tmp);
            else if (Double.class == clazz)
                return StringUtil.toDouble(tmp);
            else if (Float.class == clazz)
                return StringUtil.toFloat(tmp);
            else if (Date.class == clazz) {
                if (tmp.length() <= 10)
                    return StringUtil.toDate(tmp);
                else
                    return StringUtil.toLongDate(tmp);
            } else if (Boolean.class == clazz)
                return StringUtil.toBollean(tmp);
        }
        return null;
    }

    protected Method gotField(String name, Method[] props) {
        String fName = "";
        for (int i = 0; i < props.length; i++) {
            fName = props[i].getName();
            if (name.equals(fName.substring(3))
                    && fName.substring(0, 3).equals("set")) {//toLower(
                return props[i];
            }
        }
        return null;
    }

    private String toLower(String name) {
        return name;
    }

    private void init() {
        if (fieldsExpand == null)
            fieldsExpand = new HashMap<String, Object>();
        if (fields == null)
            fields = new HashMap<String, Object>();
    }

    public HashMap<String, Object> fillFileds() {
        HashMap<String, Object> hm = new HashMap<String, Object>();
        String name = "", key = "";
        Object val = null;
        Method[] props = this.getClass().getMethods();
        for (int i = 0; i < props.length; i++) { //没有set方法好像执行错误
            name = props[i].getName();
            if (name.substring(0, 3).equals("get") && (!name.equals("getClass"))
                    && (!name.equals("getKey")) && (!name.equals("getSjc")) && (!name.equals("getTableName"))
                    && (!"getParams".equals(name))) {
                try {
                    key = toLower(name.substring(3));
                    if (key.equals("fields"))
                        continue;
                    val = props[i].invoke(this);
                    hm.put(key, val);
                    fields.put(key, 1);
                } catch (Exception err) {
                    if (log.isErrorEnabled()) {
                        log.error(err);
                    }
                }
            }
        }
        Iterator<Entry<String, Object>> iter = fieldsExpand.entrySet().iterator();
        Entry<String, Object> entry;
        while (iter.hasNext()) {
            entry = (Entry<String, Object>) iter.next();
            hm.put(entry.getKey(), entry.getValue());
        }
        return hm;
    }

    public String toXml() {
        StringBuffer xml = new StringBuffer();
        xml.append(getStartTag());
        HashMap<String, Object> fields = fillFileds();
        Iterator iter = fields.entrySet().iterator();
        Entry<String, Object> entry;
        while (iter.hasNext()) {
            entry = (Entry<String, Object>) iter.next();
            if (entry.getValue() != null) {
                xml.append(gotTag(entry));
            }
        }
        xml.append(getEndTag());
        return xml.toString();
    }

    protected String gotTag(Entry<String, Object> entry) {
        String left = gotStartTag(entry.getKey());
        String right = gotEndTag(entry.getKey());
        String val = "";
        if (entry.getValue() != null) {
            if (entry.getValue() instanceof Date) {
                Date date = (Date) entry.getValue();
                if (date.getHours() != 0 || date.getMinutes() != 0)
                    val = TimeUtil.formatDate(date, "yyyy-MM-dd HH:mm");
                else
                    val = TimeUtil.formatDate(date, "yyyy-MM-dd HH:mm:ss");
            } else {
                val = StringUtil.toStr(entry.getValue());
                if (StringUtil.isEmpty(val))
                    return "";
            }
        }
        return left + val + right;
    }

    protected String getStartTag() {
        return "<REQUEST>\n";
    }

    protected String gotStartTag(String left) {
        return "<PARAM NAME=\"" + left.toUpperCase() + "\">#";
    }

    protected String gotEndTag(String right) {
        return "</PARAM>\n";
    }

    protected String getEndTag() {
        return "</REQUEST>\n<RESPONSE>\n</RESPONSE>";
    }
}