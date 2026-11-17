package com.petcare.util;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DISPLAY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static String formatDisplayDate(Date date) {
        if (date == null) return "-";
        return date.toLocalDate().format(DISPLAY_DATE_FORMATTER);
    }

    public static String formatTime(Time time) {
        if (time == null) return "-";
        return time.toLocalTime().format(TIME_FORMATTER);
    }

    public static Date toSqlDate(LocalDate localDate) {
        return localDate != null ? Date.valueOf(localDate) : null;
    }

    public static Time toSqlTime(LocalTime localTime) {
        return localTime != null ? Time.valueOf(localTime) : null;
    }
}
