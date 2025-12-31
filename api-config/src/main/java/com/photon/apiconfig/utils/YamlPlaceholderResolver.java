package com.photon.apiconfig.utils;

import java.io.*;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YamlPlaceholderResolver {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$\\{([^:{}]+)(?::([^}]*))?}");

    public static void resolveAndWriteYaml(String inputFilePath, String outputFilePath) throws IOException {
        String yamlText = Files.readString(new File(inputFilePath).toPath());

        String resolvedYaml = resolvePlaceholders(yamlText);

        File outputFile = new File(outputFilePath);
        if (outputFile.exists()) {
            outputFile.delete();
        }

        Files.writeString(outputFile.toPath(), resolvedYaml);
    }

    private static String resolvePlaceholders(String text) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            String defaultValue = matcher.group(2);

            String value = System.getenv(key);
            if (value == null || value.isEmpty()) {
                value = defaultValue != null ? defaultValue : matcher.group(0);
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }
}