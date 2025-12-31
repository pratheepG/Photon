package com.photon.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PhotonUtils {

    private static final List<String> DATE_PATTERNS = Arrays.asList(
            "dd/MM/yyyy",
            "dd-MM-yyyy",
            "yyyy-MM-dd",
            "yyyy/MM/dd",
            "dd.MM.yyyy",
            "yyyy.MM.dd",
            "dd MMM yyyy",       // 30 Aug 2025
            "dd-MMM-yyyy",       // 30-Aug-2025
            "yyyyMMdd",          // 20250830
            "ddMMyyyy"           // 30082025
    );
    private static final List<String> DATETIME_PATTERNS = Arrays.asList(
            "dd/MM/yyyy HH:mm:ss",
            "dd-MM-yyyy HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy/MM/dd HH:mm:ss",
            "yyyy-MM-dd'T'HH:mm:ss"
    );
    private static final long MINUTES_PER_HOUR = 60;
    private static final long MINUTES_PER_DAY = 24 * MINUTES_PER_HOUR;

    public static <T> T requireNonNull(T obj) {
        if (obj == null) {
            throw new IllegalStateException(obj.getClass().getSimpleName()+" is Null");
        }
        return obj;
    }

    /**
     * Parse a flexible date or datetime string into java.util.Date
     *
     * @param input {String}
     * @return Date
     * @throws IllegalArgumentException if input cannot be parsed with the given pattern
     */
    public static Date parseDate(String input) {
        for (String pattern : DATETIME_PATTERNS) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                LocalDateTime dateTime = LocalDateTime.parse(input, formatter);
                return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException ignored) {}
        }
        for (String pattern : DATE_PATTERNS) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                LocalDate localDate = LocalDate.parse(input, formatter);
                return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException ignored) {}
        }
        throw new IllegalArgumentException("Unsupported date format: " + input);
    }

    /**
     * Parse a date string into java.util.Date using a specific pattern.
     *
     * @param input   the date string
     * @param pattern the pattern to parse with (e.g. "dd/MM/yyyy", "yyyy-MM-dd HH:mm:ss")
     * @return parsed java.util.Date
     * @throws IllegalArgumentException if input cannot be parsed with the given pattern
     */
    public static Date parseDate(String input, String pattern) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        try {
            LocalDateTime dateTime = LocalDateTime.parse(input, formatter);
            return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException e1) {
            try {
                LocalDate localDate = LocalDate.parse(input, formatter);
                return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException e2) {
                throw new IllegalArgumentException("Unsupported date format for input: " + input + " with pattern: " + pattern);
            }
        }
    }

    /**
     * Converts a Long value representing minutes into a human-readable string.
     * The output will be in the largest relevant unit (Days, Hours, or Minutes).
     *
     * @param totalMinutes The duration in minutes.
     * @return A formatted string (e.g., "5 Days", "3 Hours", "30 Minutes").
     */
    public static String convertMinutesToFriendlyDuration(Long totalMinutes) {
        if (totalMinutes == null || totalMinutes <= 0) {
            return "0 Minutes";
        }

        if (totalMinutes >= MINUTES_PER_DAY) {
            long days = totalMinutes / MINUTES_PER_DAY;
            return formatUnit(days, "Day");
        }

        if (totalMinutes >= MINUTES_PER_HOUR) {
            long hours = totalMinutes / MINUTES_PER_HOUR;
            return formatUnit(hours, "Hour");
        }

        return formatUnit(totalMinutes, "Minute");
    }

    /**
     * Helper method to correctly pluralize the unit name.
     */
    private static String formatUnit(long value, String unit) {
        if (value == 1) {
            return value + " " + unit;
        } else {
            return value + " " + unit + "s";
        }
    }

}