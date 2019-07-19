package org.cyanspring.tools.common;

import java.util.HashMap;


public enum Message {
    // 0 - 10000 reserved
    // 10000 - 20000 system
    // 20000 -  max     business
    OK(0, MessageType.INFO, new String[][]{{"en", "OK"},
            {"cn", "成功"}}),
    FAILED(1, MessageType.ERROR, new String[][]{{"en", "FAILED"},
            {"cn", "失败"}}),

    START_TIME_ERROR(2, MessageType.ERROR,
            new String[][]{{"en", "StartTime error"},
                    {"cn", "起始时间错误"}}),
    END_TIME_ERROR(3, MessageType.ERROR,
            new String[][]{{"en", "EndTime error"},
                    {"cn", "结束时间错误"}}),
    START_END_TIME_ERROR(3, MessageType.ERROR,
            new String[][]{{"en", "StartTime larger than EndTime"},
                    {"cn", "起始时间大于结束时间"}}),
    SYMBOL_NO_EXIST(4, MessageType.ERROR,
            new String[][]{{"en", "Symbol not exist"},
                    {"cn", "标的不存在"}}),
    FILE_NO_EXIST(5, MessageType.ERROR,
            new String[][]{{"en", "File not exist"},
                    {"cn", "导入文件不存在"}}),
    SELECT_VALUE_UPDATE(6, MessageType.ERROR,
            new String[][]{{"en", "Select one value to update"},
                    {"cn", "至少选择一个值更新"}}),
    TIME_VALUE_ERROR(7, MessageType.ERROR,
            new String[][]{{"en", "Time value error"},
                    {"cn", "时间错误，找不到上一个间隔时间K线"}}),
    // end enum
    ;

    private int code;
    private MessageType type;
    private HashMap<String, String> langText = new HashMap<String, String>();

    private void validateText() throws Exception {
        for (String text : langText.values()) {
            if (text.contains("|"))
                throw new Exception("Message can't contain '|': " + text);

        }
    }

    Message(int code, MessageType type, String[][] langTexts) {
        this.code = code;
        this.type = type;
        for (String[] pair : langTexts) {
            langText.put(pair[0], pair[1]);
        }
    }

    public int getCode() {
        return this.code;
    }

    public String getText(String lang) {
        return langText.get(lang);
    }

    public String getText() {
        return langText.get(SystemDefault.getLanguage());
    }

    public String toString(String lang) {
        String txt = langText.get(lang);
        return "" + this.code + "|" + txt;
    }

    @Override
    public String toString() {
        return toString(SystemDefault.getLanguage());
    }

    // static

    static private HashMap<Integer, Message> map = new HashMap<Integer, Message>();

    static private String getKey(int code, String lang) {
        return "" + code + "-" + lang;
    }

    static private void addMessage(Message message, String lang, String text) throws Exception {
    }

    static public Message get(int code) {
        return map.get(code);
    }

    static public void validate() throws Exception {
        for (Message msg : Message.values()) {
            if (map.containsKey(msg.getCode()))
                throw new Exception("Message code already exists: " + msg.getCode());

            msg.validateText();
            map.put(new Integer(msg.getCode()), msg);
        }

    }

    public static void main(String args[]) throws Exception {
    }
}
