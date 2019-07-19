package org.cyanspring.tools.common;

import org.cyanspring.tools.utils.BeanUtil;
import org.cyanspring.tools.utils.StringUtil;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SOMList<T extends BaseEntity> {
    protected String jsonStr;

    public SOMList() {

    }

    public SOMList(String jsonStr, Class<T> clazz) {
        if (!StringUtils.isEmpty(jsonStr)) {
            this.jsonStr = jsonStr;
            items = new ArrayList<T>();
            String[] array = jsonStr.split(";");
            T model;
            Method[] props = clazz.getMethods();

            for (String str : array) {
                model = BeanUtil.getInstance(clazz);
                model.createByJson(str, props);
                items.add(model);
            }
        }
    }

    public List<T> builder(List<String> list, Class<T> clazz) {
        if (list != null && !list.isEmpty()) {
            T model;
            items = new ArrayList<T>();
            for (String str : list) {
                model = build(str, clazz);
                items.add(model);
            }
        }
        return items;
    }

    public List<T> builder(String jsonStr, Class<T> clazz) {
        if (!StringUtil.isEmpty(jsonStr)) {
            this.jsonStr = jsonStr;
            items = new ArrayList<T>();
            String[] array = jsonStr.split(";");
            T model;
            for (String str : array) {
                model = build(str, clazz);
                items.add(model);
            }
        }
        return items;
    }

    protected T build(String str, Class<T> clazz) {
        T model = BeanUtil.getInstance(clazz);
        model.createByJson(str);
        return model;
    }

    protected List<T> items;

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

    public String toString() {
        if (items != null && !items.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            boolean isFirst = true;
            for (T item : items) {
                if (isFirst) {
                    buffer.append(item.toString());
                    isFirst = false;
                } else {
                    buffer.append(";").append(item.toString());
                }
            }
            return buffer.toString();
        } else
            return "";
    }
}