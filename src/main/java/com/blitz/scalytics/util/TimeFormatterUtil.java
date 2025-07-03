package com.blitz.scalytics.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeFormatterUtil {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a")
            .withZone(ZoneId.of("Asia/Kolkata"));

    public static String formatInstant(Instant instant) {
        return formatter.format(instant);
    }
}

