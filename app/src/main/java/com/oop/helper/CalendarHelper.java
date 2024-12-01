package com.oop.helper;

import androidx.core.util.Pair;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CalendarHelper {
    public static Pair<Long, Long> getWeekRangeInMillis(int weekOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -weekOffset);
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startOfWeek = calendar.getTimeInMillis();

        calendar.add(Calendar.DATE, 6);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        long endOfWeek = calendar.getTimeInMillis();

        return new Pair<>(startOfWeek, endOfWeek);
    }
    public static String getWeekRange(int weekOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.WEEK_OF_YEAR, -weekOffset);  // Trừ đi số tuần từ hiện tại
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);  // Thiết lập ngày đầu tuần (thứ Hai)

        Date startOfWeek = calendar.getTime();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
        String startDate = dateFormat.format(startOfWeek);

        calendar.add(Calendar.DATE, 6);  // Ngày kết thúc của tuần (thứ Bảy)
        Date endOfWeek = calendar.getTime();
        String endDate = dateFormat.format(endOfWeek);

        return startDate + " - " + endDate;  // Trả về dãy ngày trong tuần
    }
    public static Pair<Long, Long> getMonthRangeInMillis(int monthOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -monthOffset);
        calendar.set(Calendar.DAY_OF_MONTH, 1);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long startOfMonth = calendar.getTimeInMillis();

        int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, lastDayOfMonth);

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);

        long endOfMonth = calendar.getTimeInMillis();

        return new Pair<>(startOfMonth, endOfMonth);
    }
    public static String getMonthName(int monthOffset) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -monthOffset);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM");
        return dateFormat.format(calendar.getTime());
    }
}
