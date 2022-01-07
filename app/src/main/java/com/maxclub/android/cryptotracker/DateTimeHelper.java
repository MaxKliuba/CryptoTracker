package com.maxclub.android.cryptotracker;

import android.content.Context;
import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeHelper {

    public static String getFormattedTime(Context context, Date date) {
        String pattern = DateFormat.is24HourFormat(context) ? "HH:mm:ss" : "hh:mm:ss a";
        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(pattern, Locale.getDefault());
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));

        return simpleDateFormat.format(date);
    }
}
