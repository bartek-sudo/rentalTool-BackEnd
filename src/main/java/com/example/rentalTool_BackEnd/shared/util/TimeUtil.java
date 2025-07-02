package com.example.rentalTool_BackEnd.shared.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtil {
    private final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    public static String getTimeInStandardFormat(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZoneId.systemDefault()).format(formatter);
    }

    public static String getCurrentTimeWithFormat() {
        return LocalDateTime.now().format(formatter);
    }
}
