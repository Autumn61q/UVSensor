package com.example.uvsensor.service.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class getCurrentTime {
    public static String get() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HH-mm-ss-SSS", Locale.getDefault());
        Date time = new Date(System.currentTimeMillis());

        return formatter.format(time);
    }

    public static String int2readable(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMdd HH:mm:ss.SSS", java.util.Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }
}
