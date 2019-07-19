package org.cyanspring.tools.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MessageText {
    private static final Logger log = LoggerFactory
            .getLogger(MessageText.class);
    private Map<String, Map<Integer, String>> langMap = new HashMap<>();
    private List<String> supportedLang = new ArrayList<>(Arrays.asList("en",
            "cn"));

    public MessageText() {
        for (String lang : supportedLang) {
            Map<Integer, String> map = new HashMap<>();
            for (Message message : Message.values()) {
                map.put(message.getCode(), message.getText(lang));
            }
            langMap.put(lang, map);
        }
    }

    public Map<Integer, String> getLangText(String lang) {
        if (!supportedLang.contains(lang)) {
            return null;
        }

        return langMap.get(lang);
    }
}
