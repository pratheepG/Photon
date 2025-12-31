package com.photon.alerts.utils;

import java.util.Map;

public class TemplateProcessor {

    public static String resolveTemplate(String template, Map<String, String> values) {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            template = template.replace("${" + entry.getKey() + "}", entry.getValue());
        }
        return template;
    }
}