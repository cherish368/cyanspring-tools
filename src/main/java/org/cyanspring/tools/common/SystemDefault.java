package org.cyanspring.tools.common;

public class SystemDefault {
    private static String language = "en";

    protected static void setValues(
            String language
    ) {
        SystemDefault.language = language;
    }

    public static String getLanguage() {
        return language;
    }

}
