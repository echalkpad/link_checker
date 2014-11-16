package com.eogren.link_checker.messaging.common;

public class Utils {
    public static final String SCRAPER_TOPIC = "scrapeReports";

    public static String getStringWithPrefix(String prefix, String str) {
        if (prefix != null) {
            return prefix + "-" + str;
        }

        return str;
    }
}
