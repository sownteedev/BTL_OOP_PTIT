package com.oop.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeHelper {

    public static String getDate(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        String formattedDate = sdf.format(date);
        return formattedDate;
    }
}
